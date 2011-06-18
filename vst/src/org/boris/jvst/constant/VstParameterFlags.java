/*******************************************************************************
 * This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Peter Smith
 *******************************************************************************/
package org.boris.jvst.constant;

public class VstParameterFlags
{
    public static final int kVstParameterIsSwitch = 1 << 0;
    public static final int kVstParameterUsesIntegerMinMax = 1 << 1;
    public static final int kVstParameterUsesFloatStep = 1 << 2;
    public static final int kVstParameterUsesIntStep = 1 << 3;
    public static final int kVstParameterSupportsDisplayIndex = 1 << 4;
    public static final int kVstParameterSupportsDisplayCategory = 1 << 5;
    public static final int kVstParameterCanRamp = 1 << 6;
}
