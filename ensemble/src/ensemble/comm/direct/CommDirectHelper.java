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

package ensemble.comm.direct;

import ensemble.Event;
import ensemble.comm.Comm;
import jade.core.ServiceHelper;

// TODO: Auto-generated Javadoc
/**
 * The Interface CommDirectHelper.
 */
public interface CommDirectHelper extends ServiceHelper {

	/**
	 * Register.
	 *
	 * @param agentName the agent name
	 * @param accessPoint the access point
	 * @param comm the comm
	 */
	public void register(String agentName, String accessPoint, Comm comm);
	
	/**
	 * Deregister.
	 *
	 * @param agentName the agent name
	 * @param accessPoint the access point
	 */
	public void deregister(String agentName, String accessPoint);

	/**
	 * Send.
	 *
	 * @param evt the evt
	 */
	public void send(Event evt);
	
}	