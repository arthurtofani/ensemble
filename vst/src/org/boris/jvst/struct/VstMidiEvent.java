/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.jvst.struct;

public class VstMidiEvent
{
    public int type;
    public int byteSize;
    public int deltaFrames;
    public int flags;
    public int noteLength;
    public int noteOffset;
    public int midiData[];
    public int detune;
    public int noteOffVelocity;
    public int reserved1;
    public int reserved2;
}
