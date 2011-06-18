/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution; and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.jvst.constant;

public class VstTimeInfoFlags
{
    public static final int kVstTransportChanged = 1;
    public static final int kVstTransportPlaying = 1 << 1;
    public static final int kVstTransportCycleActive = 1 << 2;
    public static final int kVstTransportRecording = 1 << 3;
    public static final int kVstAutomationWriting = 1 << 6;
    public static final int kVstAutomationReading = 1 << 7;
    public static final int kVstNanosValid = 1 << 8;
    public static final int kVstPpqPosValid = 1 << 9;
    public static final int kVstTempoValid = 1 << 10;
    public static final int kVstBarsValid = 1 << 11;
    public static final int kVstCyclePosValid = 1 << 12;
    public static final int kVstTimeSigValid = 1 << 13;
    public static final int kVstSmpteValid = 1 << 14;
    public static final int kVstClockValid = 1 << 15;
}
