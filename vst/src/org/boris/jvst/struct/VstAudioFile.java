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

public class VstAudioFile
{
    public int flags;
    public byte[] hostOwned;
    public byte[] plugOwned;
    public String name;
    public int uniqueId;
    public double sampleRate;
    public int numChannels;
    public double numFrames;
    public int format;
    public double editCursorPosition;
    public double selectionStart;
    public double selectionSize;
    public int selectedChannelsMask;
    public int numMarkers;
    public int timeRulerUnit;
    public double timeRulerOffset;
    public double tempo;
    public int timeSigNumerator;
    public int timeSigDenominator;
    public int ticksPerBlackNote;
    public int smpteFrameRate;
    public char future[];
}
