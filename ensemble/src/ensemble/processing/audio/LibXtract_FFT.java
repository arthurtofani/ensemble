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

import xtract.core.floatArray;
import xtract.core.xtract;
import xtract.core.xtract_features_;
import xtract.core.xtract_spectrum_;

public class LibXtract_FFT extends Processor {

	// Mutex needed for safe threaded FFTW initialization
	private static final Semaphore mutex = new Semaphore(1);
	
	private final String PARAM_SIZE 		= "size";
	private final String PARAM_SAMPLE_RATE	= "sample_rate";
	private final String PARAM_OUTPUT_TYPE 	= "output_type";
	private final String PARAM_INVERSE		= "inverse";

	// FFT
	private static int default_fft_size = 512;
	private int fft_size;
	private double Fs;
	private String fft_output;
	private boolean inverse;

	private floatArray vector;
	private	floatArray argf = new floatArray(3);
	private floatArray spectrum;

	@Override
	public boolean init() {
		
		// Validate arguments
		fft_size = Integer.valueOf(arguments.get(PARAM_SIZE, "512"));
		Fs = Double.valueOf(arguments.get(PARAM_SAMPLE_RATE, "44100"));
		fft_output = arguments.get(PARAM_OUTPUT_TYPE, "real"); // real, complex, polar
		inverse = Boolean.parseBoolean(arguments.get(PARAM_INVERSE, "false"));

		vector = new floatArray(fft_size);
		spectrum = new floatArray(fft_size);
		
		try {
			mutex.acquire();
			xtract.xtract_init_fft(fft_size, xtract_features_.XTRACT_SPECTRUM.swigValue());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mutex.release();
		}

		return true;
		
	}

	@Override
	public boolean finit() {
		
		try {
			mutex.acquire();
			xtract.xtract_free_fft();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			mutex.release();
		}

		return true;
		
	}

	@Override
	public Object process(Parameters arguments, Object in) {

		// Validate input data
		if (!(in instanceof double[])) {
			System.out.println("SPECTRUM: input must be a double[]");
			return null;
		}
		double[] chunk = (double[])in;
		

		// Creates fft plan only if needed
		if (fft_size != default_fft_size) {
			xtract.xtract_init_fft(fft_size, xtract_features_.XTRACT_SPECTRUM.swigValue());
			vector = new floatArray(fft_size);
			spectrum = new floatArray(fft_size);
			default_fft_size = fft_size;
		}

		// Input vector
		for (int i = 0; i < fft_size; i++) {
			vector.setitem(i, (float)chunk[i]);
		}

		// Spectrum
		argf.setitem(0, (float)Fs/fft_size);
		argf.setitem(1, xtract_spectrum_.XTRACT_MAGNITUDE_SPECTRUM.swigValue());
		argf.setitem(2, 0.0f);
		xtract.xtract_spectrum(vector.cast(), fft_size, argf.cast(), spectrum.cast());

		// Results
		double[] out = new double[fft_size/2];
		for (int i = 0; i < fft_size/2; i++) {
			out[i] = spectrum.getitem(i);
		}
		
		return out;

	}

}
