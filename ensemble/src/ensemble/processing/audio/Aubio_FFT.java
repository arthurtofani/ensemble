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

import java.util.concurrent.Semaphore;

import ensemble.Parameters;
import ensemble.processing.Processor;

import aubio.SWIGTYPE_p_aubio_mfft_t;
import aubio.SWIGTYPE_p_cvec_t;
import aubio.SWIGTYPE_p_fvec_t;
import aubio.aubiowrapper;


public class Aubio_FFT extends Processor {

	// Mutex needed for safe threaded FFTW initialization
	private static final Semaphore mutex = new Semaphore(1);

	private final String PARAM_SIZE 		= "size";
	private final String PARAM_SAMPLE_RATE	= "sample_rate";
	private final String PARAM_OUTPUT_TYPE 	= "output_type";
	private final String PARAM_INVERSE		= "inverse";
	
	// FFT
	private SWIGTYPE_p_aubio_mfft_t mfft_t;
	private int default_fft_size = 512;
	private int fft_size;
	private double Fs;
	private String fft_output;
	private boolean inverse;
	private SWIGTYPE_p_fvec_t in_fvec = null;
	private SWIGTYPE_p_cvec_t out_cvec = null;
	private SWIGTYPE_p_cvec_t in_cvec = null;
	private SWIGTYPE_p_fvec_t out_fvec = null;

	@Override
	public boolean init() {
		
		// Validate arguments
		fft_size = Integer.valueOf(arguments.get(PARAM_SIZE, "512"));
		Fs = Double.valueOf(arguments.get(PARAM_SAMPLE_RATE, "44100"));
		fft_output = arguments.get(PARAM_OUTPUT_TYPE, "polar"); // real, complex, polar
		inverse = Boolean.parseBoolean(arguments.get(PARAM_INVERSE, "false"));

		// Initialize FFT plan
		try {
			mutex.acquire();
			mfft_t = aubiowrapper.new_aubio_mfft(default_fft_size, 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mutex.release();
		}
//		System.out.println("FFT plan created - size = " + default_fft_size);
		
		in_fvec = aubiowrapper.new_fvec(fft_size, 1);
		out_cvec = aubiowrapper.new_cvec(fft_size, 1);
		in_cvec = aubiowrapper.new_cvec(fft_size, 1);
		out_fvec = aubiowrapper.new_fvec(fft_size, 1);

		return true;
		
	}

	@Override
	public Object process(Parameters arguments, Object in) {
		
		double[] out = null;
				
		// Valide input data
		if (!(in instanceof double[])) {
			System.out.println("FFT: input must be a double[]");
			return null;
		}
		double[] chunk = (double[])in;
		
		// Forward FFT
		if (!inverse) {
	
			// Creates the input vector
			for (int i = 0; i < fft_size; i++) {
				aubiowrapper.fvec_write_sample(in_fvec, (float)chunk[i], 0, i);
			}
	      
			// FFT
			aubiowrapper.aubio_mfft_do(mfft_t, in_fvec, out_cvec);
	      
			// Results
			if (fft_output.equals("real")) {
				out = new double[fft_size/2+1];
				for (int i = 0; i < fft_size/2+1; i++) {
					float norm = aubiowrapper.cvec_read_norm(out_cvec, 0, i);
					float phas = aubiowrapper.cvec_read_phas(out_cvec, 0, i);
	//				System.out.println(norm + " |_ " + phas);
					double mag = norm/******************************************************************************

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
Math.sqrt((norm * norm) + (phas * phas))*/;
					out[i] = mag;
				}
			} else if (fft_output.equals("complex")) {
				
			} else if (fft_output.equals("polar")) {
				out = new double[fft_size + 2];
				for (int i = 0; i < fft_size/2+1; i++) {
					out[(i*2)] = aubiowrapper.cvec_read_norm(out_cvec, 0, i);
					out[(i*2)+1] = aubiowrapper.cvec_read_phas(out_cvec, 0, i);
				}
			}

		// Inverse FFT
		} else {
			// Creates the input vector
			for (int i = 0; i < fft_size/2+1; i++) {
				aubiowrapper.cvec_write_norm(in_cvec, (float)chunk[i*2], 0, i);
				aubiowrapper.cvec_write_phas(in_cvec, (float)chunk[i*2+1], 0, i);
			}
	      
			// FFT
			aubiowrapper.aubio_mfft_rdo(mfft_t, in_cvec, out_fvec);
	      
			// Results
			out = new double[fft_size];
			for (int i = 0; i < fft_size; i++) {
				out[i] = aubiowrapper.fvec_read_sample(out_fvec, 0, i);
			}
			
		}
		
		return out;

	}

	@Override
	public boolean finit() {
		
		if (in_fvec != null) {
			aubiowrapper.del_fvec(in_fvec);
		}
		if (out_fvec != null) {
			aubiowrapper.del_fvec(out_fvec);
		}
		if (in_cvec != null) {
			aubiowrapper.del_cvec(in_cvec);
		}
		if (out_cvec != null) {
			aubiowrapper.del_cvec(out_cvec);
		}
		
		try {
			mutex.acquire();
			aubiowrapper.del_aubio_mfft(mfft_t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mutex.release();
		}
		
		return true;
	}
	    
}
