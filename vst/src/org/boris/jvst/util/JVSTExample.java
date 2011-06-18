/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.jvst.util;

import org.boris.jvst.AEffect;
import org.boris.jvst.VST;

public class JVSTExample
{
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: jvstexample <vst plugin>");
        }
        AEffect a = VST.load(args[0]);
        a.open();
        a.setSampleRate(44100.0f);
        a.setBlockSize(512);

        // attempt some processing
        int blocksize = 512;
        float[][] inputs = new float[a.numInputs][];
        for (int i = 0; i < a.numInputs; i++) {
            inputs[i] = new float[blocksize];
            for (int j = 0; j < blocksize; j++)
                inputs[i][j] = (float) Math
                        .sin(j * Math.PI * 2 * 440 / 44100.0);
        }
        float[][] outputs = new float[a.numOutputs][];
        for (int i = 0; i < a.numOutputs; i++) {
            outputs[i] = new float[blocksize];
            for (int j = 0; j < blocksize; j++)
                outputs[i][j] = 0;
        }

        a.processReplacing(inputs, outputs, blocksize);

        VST.dispose(a);
    }
}
