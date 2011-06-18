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

public interface IAudioHost
{
    void automate(AEffect effect, long parameter, float value);

    long getVersion();

    void idle();

    boolean sizeWindow(int width, int height);

    long getSampleRate();

    long getBlockSize();

    long getInputLatency();

    long getOutputLatency();

    String getVendorString();

    String getProductString();

    long getVendorVersion();

    boolean canDo(String canDo);

    long getLanguage();

    String getDirectory();

    void updateDisplay(AEffect effect);

    void beginEdit(AEffect effect, long parameter);

    void endEdit(AEffect effect, long parameter);
}
