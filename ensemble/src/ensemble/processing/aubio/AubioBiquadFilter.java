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

package ensemble.processing.aubio;

import aubio.SWIGTYPE_p_aubio_biquad_t;
import aubio.SWIGTYPE_p_fvec_t;
import aubio.aubiowrapper;
import ensemble.Parameters;
import ensemble.processing.Processor;

public class AubioBiquadFilter extends Processor {

	int chunk_size;
	private SWIGTYPE_p_aubio_biquad_t biquad;
	private SWIGTYPE_p_fvec_t in_fvec = null;
	
	@Override
	public boolean init() {

		chunk_size = Integer.valueOf(parameters.get("CHUNK_SIZE"));
		in_fvec = aubiowrapper.new_fvec(chunk_size, 1);
		
		long b1 = Long.valueOf(parameters.get("b1", "0"));
		long b2 = Long.valueOf(parameters.get("b2", "0"));
		long b3 = Long.valueOf(parameters.get("b3", "0"));
		long a2 = Long.valueOf(parameters.get("a2", "0"));
		long a3 = Long.valueOf(parameters.get("a3", "0"));
		
		biquad = aubiowrapper.new_aubio_biquad(b1, b2, b3, a2, a3);
		
		return true;
	}

	@Override
	public boolean finit() {

//		aubiowrapper.del_aubio_biquad(biquad);
		aubiowrapper.del_fvec(in_fvec);
		
		return true;
	}

	@Override
	public Object process(Parameters arguments, Object in) {

		if (!(in instanceof double[])) {
			System.err.println("[AubioBiquadFilter] input object is not a double[]");
			return null;
		}
		
		double[] in_buf = (double[])in;
		// Creates the input vector
		for (int i = 0; i < chunk_size; i++) {
			aubiowrapper.fvec_write_sample(in_fvec, (float)in_buf[i], 0, i);
		}
		aubiowrapper.aubio_biquad_do(biquad, in_fvec);
		
		return in;
	}
	
}
