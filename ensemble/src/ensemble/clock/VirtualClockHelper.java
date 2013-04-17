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

package ensemble.clock;

import jade.core.Agent;
import jade.core.ServiceHelper;
import jade.core.behaviours.Behaviour;

// TODO: Auto-generated Javadoc
/**
 * The Interface VirtualClockHelper.
 */
public interface VirtualClockHelper extends ServiceHelper {

	/**
	 * Update clock.
	 *
	 * @param units the units
	 */
	public void updateClock(long units);

	/**
	 * Update clock.
	 */
	public void updateClock();
	
	/**
	 * Gets the current time.
	 *
	 * @param unit the unit
	 * @return the current time
	 */
	public double getCurrentTime(TimeUnit unit);

	// TODO Deve ser tipo o TimerTask do Java
//	public void schedule(Agent a, Behaviour b, long wakeupTime);
	/**
	 * Schedule.
	 *
	 * @param a the a
	 * @param b the b
	 * @param wakeupTime the wakeup time
	 */
	public void schedule(Agent a, Runnable b, long wakeupTime);
	
	/**
	 * Execute.
	 *
	 * @param a the a
	 * @param b the b
	 */
	public void execute(Agent a, Runnable b);
	
}
