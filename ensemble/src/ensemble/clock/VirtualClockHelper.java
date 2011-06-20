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

public interface VirtualClockHelper extends ServiceHelper {

	public void updateClock(long units);

	public void updateClock();
	
	public double getCurrentTime(TimeUnit unit);

	// TODO Deve ser tipo o TimerTask do Java
//	public void schedule(Agent a, Behaviour b, long wakeupTime);
	public void schedule(Agent a, Runnable b, long wakeupTime);
	
	public void execute(Agent a, Runnable b);
	
}
