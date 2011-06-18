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

import org.boris.jvst.AEffect;

public class AEffectFlag
{
    public static final int effFlagsHasEditor = 1 << 0;
    public static final int effFlagsCanReplacing = 1 << 4;
    public static final int effFlagsProgramChunks = 1 << 5;
    public static final int effFlagsIsSynth = 1 << 8;
    public static final int effFlagsNoSoundInStop = 1 << 9;
    public static final int effFlagsCanDoubleReplacing = 1 << 12;
    public static final int effFlagsHasClip = 1 << 1;
    public static final int effFlagsHasVu = 1 << 2;
    public static final int effFlagsCanMono = 1 << 3;
    public static final int effFlagsExtIsAsync = 1 << 10;
    public static final int effFlagsExtHasBuffer = 1 << 11;

    public static boolean hasEditor(AEffect a) {
        return (a.flags & effFlagsHasEditor) != 0;
    }

    public static boolean canReplacing(AEffect a) {
        return (a.flags & effFlagsCanReplacing) != 0;
    }

    public static boolean programChunks(AEffect a) {
        return (a.flags & effFlagsProgramChunks) != 0;
    }

    public static boolean isSynth(AEffect a) {
        return (a.flags & effFlagsIsSynth) != 0;
    }

    public static boolean noSoundInStop(AEffect a) {
        return (a.flags & effFlagsNoSoundInStop) != 0;
    }

    public static boolean canDoubleReplacing(AEffect a) {
        return (a.flags & effFlagsCanDoubleReplacing) != 0;
    }

    public static boolean hasClip(AEffect a) {
        return (a.flags & effFlagsHasClip) != 0;
    }

    public static boolean hasVu(AEffect a) {
        return (a.flags & effFlagsHasVu) != 0;
    }

    public static boolean canMono(AEffect a) {
        return (a.flags & effFlagsCanMono) != 0;
    }

    public static boolean extIsAsync(AEffect a) {
        return (a.flags & effFlagsExtIsAsync) != 0;
    }

    public static boolean extHasBuffer(AEffect a) {
        return (a.flags & effFlagsExtHasBuffer) != 0;
    }
}
