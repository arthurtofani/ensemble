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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import ensemble.Event;
import ensemble.Sensor;
import ensemble.clock.TimeUnit;

public class DummySensor extends Sensor {

	PrintWriter file_perf;
	
	@Override
	public boolean init() {
		try {
			file_perf = new PrintWriter(new FileOutputStream("tests/out_"+getAgent().getAgentName()+".txt"), false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	protected void process(Event evt) throws Exception {
//		System.out.println("[Sensor] process()");
		// Performance
		long wf = (long)Math.ceil((getAgent().getClock().getCurrentTime(TimeUnit.MILLISECONDS) - startTime) / period);
		if (wf == evt.frame) {
			file_perf.printf("%d\n", wf);
			file_perf.flush();
		}
	}
	
}
