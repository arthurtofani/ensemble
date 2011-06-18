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

import org.boris.jvst.struct.ERect;
import org.boris.jvst.struct.VstParameterProperties;
import org.boris.jvst.struct.VstPinProperties;
import org.boris.jvst.util.LibraryLoader;

class JNI
{
    private static String VERSION = "0.0.1";

    static {
        if (!LibraryLoader.load("jvst.dll"))
            LibraryLoader.load("jvst-" + VERSION, JNI.class, true);
    }

    static native long loadLibrary(String filename);

    static native void freeLibrary(long library);

    static native long loadEffect(long library);

    static native void fillEffect(long library, long ptr, AEffect effect);

    static native int dispatcher(long ptr, int opcode, int index,
            long valuePtr, long dPtr, float opt);

    static native int canDo(long ptr, String doStr);

    static native VstPinProperties getPinProperties(long ptr, int index,
            boolean input);

    static native VstParameterProperties getParameterProperties(long ptr,
            int index);

    static native ERect editGetRect(long ptr);

    static native String dispatcherS(long ptr, int opcode, int index);

    static native void setParameter(long ptr, int index, float value);

    static native float getParameter(long ptr, int index);

    static native void processReplacing(long ptr, float[][] inputs,
            float[][] outputs, int blocksize);
}
