/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.router;

import ensemble.Command;

// TODO: Auto-generated Javadoc
/**
 * The Interface RouterClient.
 *
 * @author lfthomaz
 */
public interface RouterClient {

	/**
	 * Returns this component framework address.
	 * @return the component address
	 */
    public String getAddress();

    /**
     * An user-implemented method to process a received command.
     *
     * @param cmd the cmd
     */
    public void processCommand(Command cmd);
    
    /**
     * A framework-implemented method that receives a command sent by the RouterAgent and calls processCommand.
     *
     * @param cmd the cmd
     */
    public void receiveCommand(Command cmd);

    /**
     * A framework-implemented method that sends a command to its destination, via RouterAgent if the recipient is another agent.
     *
     * @param cmd the cmd
     */
    public void sendCommand(Command cmd);
    

}
