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

public class VSTException extends Exception
{
    public VSTException() {
    }

    public VSTException(String message, Throwable cause) {
        super(message, cause);
    }

    public VSTException(String message) {
        super(message);
    }

    public VSTException(Throwable cause) {
        super(cause);
    }
}
