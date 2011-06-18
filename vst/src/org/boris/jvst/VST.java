/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.jvst;

import org.boris.jvst.constant.AEffectOpcodes;
import org.boris.jvst.constant.AudioMasterOpcodes;
import org.boris.jvst.constant.AudioMasterOpcodesX;
import org.boris.jvst.util.LongMap;

public class VST
{
    private static LongMap effects = new LongMap();
    private static IAudioHost callback;

    public static AEffect load(String filename) throws VSTException {
        long library = JNI.loadLibrary(filename);
        if (library == 0)
            throw new VSTException("Unable to load VST libary");
        long ptr = JNI.loadEffect(library);
        if (ptr == 0) {
            JNI.freeLibrary(library);
            throw new VSTException("Unable to create effect");
        }
        AEffect a = new AEffect();
        JNI.fillEffect(library, ptr, a);

        // Check for the correct magic value
        if (a.magic != AEffect.kEffectMagic) {
            dispose(a);
            throw new VSTException("Invalid magic value");
        }

        // Store reference for use in callback
        effects.put(ptr, a);

        return a;
    }

    public static void dispose(AEffect effect) {
        effects.remove(effect.ptr);
        JNI.dispatcher(effect.ptr, AEffectOpcodes.effClose, 0, 0, 0, 0);
        JNI.freeLibrary(effect.library);
    }

    public static void setHost(IAudioHost host) {
        callback = host;
    }

    static long callback(long effect, long opcode, long index, long value,
            long ptr, float opt) {
        System.out.println("Callback : " + opcode + ", " + index + ", " +
                value + ", " + ptr + ", " + opt);

        if (callback == null) {
            if (opcode == AudioMasterOpcodes.Version)
                return 2400;
            return 0;
        }

        AEffect a = (AEffect) effects.get(effect);

        switch ((int) opcode) {
        case AudioMasterOpcodes.Version:
            return callback.getVersion();
        case AudioMasterOpcodes.Automate:
            callback.automate(a, index, value);
            break;
        case AudioMasterOpcodes.Idle:
            callback.idle();
            break;
        case AudioMasterOpcodesX.SizeWindow:
            return callback.sizeWindow((int) index, (int) value) ? 1 : 0;
        case AudioMasterOpcodesX.GetSampleRate:
            return callback.getSampleRate();
        case AudioMasterOpcodesX.GetBlockSize:
            return callback.getBlockSize();
        case AudioMasterOpcodesX.GetInputLatency:
            return callback.getInputLatency();
        case AudioMasterOpcodesX.GetOutputLatency:
            return callback.getOutputLatency();
        case AudioMasterOpcodesX.UpdateDisplay:
            callback.updateDisplay(a);
            break;
        case AudioMasterOpcodesX.BeginEdit:
            callback.beginEdit(a, index);
            break;
        case AudioMasterOpcodesX.EndEdit:
            callback.endEdit(a, index);
            break;
        }
        return 0;
    }
}
