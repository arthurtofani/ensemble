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

public class VstTimeInfo
{
    public double samplePos;
    public double sampleRate;
    public double nanoSeconds;
    public double ppqPos;
    public double tempo;
    public double barStartPos;
    public double cycleStartPos;
    public double cycleEndPos;
    public int timeSigNumerator;
    public int timeSigDenominator;
    public int smpteOffset;
    public int smpteFrameRate;
    public int samplesToNextClock;
    public int flags;
}
