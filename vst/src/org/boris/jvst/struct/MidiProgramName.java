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

public class MidiProgramName
{
    public int thisProgramIndex;
    public String name;
    public char midiProgram;
    public char midiBankMsb;
    public char midiBankLsb;
    public char reserved;
    public int parentCategoryIndex;
    public int flags;
}
