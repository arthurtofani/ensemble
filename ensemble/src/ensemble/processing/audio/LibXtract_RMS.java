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
*****************************************************************************

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

package ensemble.processing.audio;

import ensemble.Parameters;
import ensemble.processing.Processor;
import xtract.core.floatArray;
import xtract.core.xtract;


public class LibXtract_RMS extends Processor {

	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean finit() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object process(Parameters arguments, Object in) {

		double[] chunk = (double[])in;
	
		// Create the input vector
		floatArray input = new floatArray(chunk.length);
		for (int i = 0; i < chunk.length; i++) {
			input.setitem(i, (float)chunk[i]);
		}

		float[] ret_f = new float[chunk.length];

		xtract.xtract_rms_amplitude(input.cast(), chunk.length, null, ret_f);
	
		return (double)ret_f[0];

	}

}
