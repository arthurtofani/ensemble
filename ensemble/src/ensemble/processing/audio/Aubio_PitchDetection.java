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
import ensemble.processing.Processor;
import aubio.SWIGTYPE_p_aubio_pitchdetection_t;
import aubio.SWIGTYPE_p_fvec_t;
import aubio.aubio_pitchdetection_mode;
import aubio.aubio_pitchdetection_type;
import aubio.aubiowrapper;

// TODO: Auto-generated Javadoc
/**
 * The Class Aubio_PitchDetection.
 */
public class Aubio_PitchDetection extends Processor {

	/** The Constant ARG_BUFSIZE. */
	private static final String ARG_BUFSIZE = "bufsize";
	
	/** The Constant ARG_HOPSIZE. */
	private static final String ARG_HOPSIZE = "hopsize";
	
	/** The Constant ARG_SAMPLE_RATE. */
	private static final String ARG_SAMPLE_RATE = "sample_rate";
	
	/** The Constant ARG_TYPE. */
	private static final String ARG_TYPE = "type";
	
	/** The Constant ARG_MODE. */
	private static final String ARG_MODE = "mode";
	
	/** The p_t. */
	private SWIGTYPE_p_aubio_pitchdetection_t p_t;
	
	/** The default_bufsize. */
	private int default_bufsize = 512;
	
	/** The default_hopsize. */
	private int default_hopsize = 256;
	
	/** The default_sample_rate. */
	private int default_sample_rate = 44100;
	
	/** The default_type. */
	private aubio_pitchdetection_type default_type = aubio_pitchdetection_type.aubio_pitch_schmitt;
	
	/** The default_mode. */
	private aubio_pitchdetection_mode default_mode = aubio_pitchdetection_mode.aubio_pitchm_midi;
	
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	public boolean init() {
		
		long start = System.currentTimeMillis();
		p_t = aubiowrapper.new_aubio_pitchdetection(default_bufsize, default_hopsize, 1, default_sample_rate, default_type, default_mode);
		long end = System.currentTimeMillis();
		System.out.println("time = " + (end-start));
		
		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.processing.Processor#process(ensemble.Parameters, java.lang.Object)
	 */
	@Override
	public Object process(Parameters arguments, Object in) {

		// Validate arguments
		int bufsize = Integer.valueOf(arguments.get(ARG_BUFSIZE, "512"));
//		double Fs = Double.valueOf(arguments.get(PARAM_SAMPLE_RATE, "44100"));
//		String fft_output = arguments.get(PARAM_OUTPUT_TYPE, "polar"); // real, complex, polar
//		boolean inverse = Boolean.parseBoolean(arguments.get(PARAM_INVERSE, "false"));

		// Valide input data
		if (!(in instanceof double[])) {
			System.out.println("PITCH_DETECTION: input must be a double[]");
			return null;
		}
		double[] chunk = (double[])in;
		
		// Creates the input vector
		SWIGTYPE_p_fvec_t in_fvec = aubiowrapper.new_fvec(default_bufsize, 1);
		for (int i = 0; i < default_bufsize; i++) {
			aubiowrapper.fvec_write_sample(in_fvec, (float)chunk[i], 0, i);
		}
		
		float smpl_t = aubiowrapper.aubio_pitchdetection(p_t, in_fvec);
		
		return smpl_t;
		
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {

		return true;

	}

}
