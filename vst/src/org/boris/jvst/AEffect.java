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
import org.boris.jvst.constant.AEffectXOpcodes;
import org.boris.jvst.struct.ERect;
import org.boris.jvst.struct.VstParameterProperties;
import org.boris.jvst.struct.VstPinProperties;

public class AEffect
{
    // The correct magic value
    public static final int kEffectMagic = 0x56737450;

    // The data values - filled in by the JNI library
    public int magic = 0;
    public int numPrograms = 0;
    public int numParams = 0;
    public int numInputs = 0;
    public int numOutputs = 0;
    public int flags = 0;
    public int initialDelay = 0;
    public int uniqueID = 0;
    public int version = 0;

    // internal pointers
    long library;
    long ptr;

    public int getVersion() {
        return JNI
                .dispatcher(ptr, AEffectXOpcodes.effGetVstVersion, 0, 0, 0, 0);
    }

    public boolean canDo(String doStr) {
        return JNI.canDo(ptr, doStr) != 0;
    }

    public VstPinProperties getInputPinProperties(int index) {
        return JNI.getPinProperties(ptr, index, true);
    }

    public VstPinProperties getOutputPinProperties(int index) {
        return JNI.getPinProperties(ptr, index, false);
    }

    public VstParameterProperties getParameterProperties(int index) {
        return JNI.getParameterProperties(ptr, index);
    }

    public void setProgram(int program) {
        JNI.dispatcher(ptr, AEffectOpcodes.effSetProgram, 0, program, 0, 0);
    }

    public int getProgram() {
        return JNI.dispatcher(ptr, AEffectOpcodes.effGetProgram, 0, 0, 0, 0);
    }

    public String getProgramName() {
        return JNI.dispatcherS(ptr, AEffectOpcodes.effGetProgramName, 0);
    }

    public String getParameterName(int index) {
        return JNI.dispatcherS(ptr, AEffectOpcodes.effGetParamName, index);
    }

    public String getParameterLabel(int index) {
        return JNI.dispatcherS(ptr, AEffectOpcodes.effGetParamLabel, index);
    }

    public String getParameterDisplay(int index) {
        return JNI.dispatcherS(ptr, AEffectOpcodes.effGetParamDisplay, index);
    }

    public void setParameter(int index, float value) {
        JNI.setParameter(ptr, index, value);
    }

    public float getParameter(int index) {
        return JNI.getParameter(ptr, index);
    }

    public String getEffectName() {
        return JNI.dispatcherS(ptr, AEffectXOpcodes.effGetEffectName, 0);
    }

    public void open() {
        JNI.dispatcher(ptr, AEffectOpcodes.effOpen, 0, 0, 0, 0);
    }

    public void suspend() {
        JNI.dispatcher(ptr, AEffectOpcodes.effMainsChanged, 0, 0, 0, 0);
    }

    public void resume() {
        JNI.dispatcher(ptr, AEffectOpcodes.effMainsChanged, 0, 1, 0, 0);
    }

    public void setSampleRate(float rate) {
        JNI.dispatcher(ptr, AEffectOpcodes.effSetSampleRate, 0, 0, 0, rate);
    }

    public void setBlockSize(int size) {
        JNI.dispatcher(ptr, AEffectOpcodes.effSetBlockSize, 0, size, 0, 0);
    }

    public void editOpen(long hwnd) {
        JNI.dispatcher(ptr, AEffectOpcodes.effEditOpen, 0, 0, hwnd, 0);
    }

    public ERect editGetRect() {
        return JNI.editGetRect(ptr);
    }

    public void editClose() {
        JNI.dispatcher(ptr, AEffectOpcodes.effEditClose, 0, 0, 0, 0);
    }

    public void processReplacing(float[][] inputs, float[][] outputs,
            int blocksize) {
        JNI.processReplacing(ptr, inputs, outputs, blocksize);
    }
}
