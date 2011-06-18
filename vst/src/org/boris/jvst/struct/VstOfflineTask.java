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

public class VstOfflineTask
{
    public String processName;

    // audio access
    public double readPosition;
    public double writePosition;
    public int readCount;
    public int writeCount;
    public int sizeInputBuffer;
    public int sizeOutputBuffer;
    public byte[] inputBuffer;
    public byte[] outputBuffer;
    public double positionToProcessFrom;
    public double numFramesToProcess;
    public double maxFramesToWrite;

    // other data access
    public byte[] extraBuffer;
    public int value;
    public int index;

    // file attributes
    public double numFramesInSourceFile;
    public double sourceSampleRate;
    public double destinationSampleRate;
    public int numSourceChannels;
    public int numDestinationChannels;
    public int sourceFormat;
    public int destinationFormat;
    public String outputText;

    // progress notification
    public double progress;
    public int progressMode;
    public String progressText;

    public int flags;
    public int returnValue;
    public long hostOwned;
    public long plugOwned;

    public char future[];
}
