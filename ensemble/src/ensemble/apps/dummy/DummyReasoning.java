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

package ensemble.apps.dummy;

import ensemble.Actuator;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;

public class DummyReasoning extends Reasoning {

	Actuator actuator;
	Memory actuatorMemory;
	
	Sensor sensor;
	Memory sensorMemory;
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) throws Exception {
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals("DUMMY")) {
			evtHdl.registerListener(this);
			actuatorMemory = getAgent().getKB().getMemory(evtHdl.getComponentName());
			actuator = (Actuator)evtHdl;
		}
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals("DUMMY")) {
			evtHdl.registerListener(this);
			sensorMemory = getAgent().getKB().getMemory(evtHdl.getComponentName());
			sensor = (Sensor)evtHdl;
		}
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) throws Exception {
//		System.out.println("newSense()");
		double[] buf = (double[])sensorMemory.readMemory(instant, duration, TimeUnit.SECONDS);
	}
	
	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) throws Exception {
		double[] buf = new double[1024];
		// Armazena o chunk de saída na memória e atua
		try {
			actuatorMemory.writeMemory(buf, instant, duration, TimeUnit.SECONDS);
			actuator.act();
		} catch (MemoryException e1) {
//			MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
		}
	}

}
