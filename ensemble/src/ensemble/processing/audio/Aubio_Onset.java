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

package ensemble.processing.audio;

import ensemble.Parameters;
import jaubio.SWIGTYPE_p_aubio_onsetdetection_t;
import jaubio.SWIGTYPE_p_aubio_pvoc_t;
import jaubio.SWIGTYPE_p_cvec_t;
import jaubio.SWIGTYPE_p_fvec_t;
import jaubio.aubio_onsetdetection_type;
import jaubio.aubiowrapper;
import ensemble.processing.Process;

public class Aubio_Onset extends Process {

	private static int fftsize = 512;
	private static int hopsize = 256;

	private SWIGTYPE_p_aubio_onsetdetection_t o;
	private SWIGTYPE_p_aubio_pvoc_t pv;

	private SWIGTYPE_p_fvec_t ibuf;
	private SWIGTYPE_p_fvec_t onset;
	private SWIGTYPE_p_cvec_t fftgrain;
	
	@Override
	public boolean init() {

		pv = aubiowrapper.new_aubio_pvoc(512, 256, 1);
		// Initialize Onset Detection
		o = aubiowrapper.new_aubio_onsetdetection(aubio_onsetdetection_type.aubio_onset_complex, fftsize, 1);

		// Creates the input vector
		ibuf = aubiowrapper.new_fvec(fftsize, 1);
		onset = aubiowrapper.new_fvec(fftsize, 1);
		
		// Creates the output vector
		fftgrain = aubiowrapper.new_cvec(fftsize, 1);

		return true;
		
	}

	@Override
	public boolean finit() {

		aubiowrapper.del_fvec(ibuf);
		aubiowrapper.del_fvec(onset);
		aubiowrapper.del_cvec(fftgrain);
		
		aubiowrapper.del_aubio_pvoc(pv);
		
		return true;
		
	}

	@Override
	public Object process(Parameters arguments, Object in) {
		
		double[] chunk = (double[])in;
    
		int ptr = 0;
		while (ptr + fftsize < chunk.length) {
			// ibuf
			for (int i = 0; i < fftsize; i++) {
				aubiowrapper.fvec_write_sample(ibuf, (float)chunk[ptr+i], 0, i);
			}
			aubiowrapper.aubio_pvoc_do (pv, ibuf, fftgrain);
			aubiowrapper.aubio_onsetdetection(o, fftgrain, onset);
		    boolean isonset = aubiowrapper.aubio_peakpick_pimrt(onset, 	);
			if (isonset) {
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
 test for silence */
				if (aubiowrapper.aubio_silence_detection(ibuf, silence)==1) {
					isonset=0;
				} else {
					for (pos = 0; pos < overlap_size; pos++){
						obuf->data[0][pos] = woodblock->data[0][pos];
					}
				}
			}
		      
		}
		return null;
	}

}
