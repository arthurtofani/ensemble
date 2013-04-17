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

package ensemble.processing;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import ensemble.Parameters;
import ensemble.audio.file.AudioInputFile;
import ensemble.processing.audio.Aubio_FFT;
import ensemble.processing.audio.Aubio_Onset;
import ensemble.processing.audio.Aubio_PhaseVocoder;
import ensemble.processing.audio.Aubio_PitchDetection;
import ensemble.processing.audio.LibXtract_FFT;
import ensemble.processing.audio.LibXtract_RMS;
import ensemble.processing.audio.OnsetsDS;


// TODO: Auto-generated Javadoc
// TODO Mem�ria para guardar os �ltimos frames, utilizados em processamentos com janela deslizantes
/**
 * The Class ProcessFactory_new.
 */
public abstract class ProcessFactory_new {
	
	/**
	 * The Enum AudioOperation.
	 */
	public enum AudioOperation {
		
		/** The fft. */
		FFT,
		
		/** The filter. */
		FILTER,
		
		/** The rms. */
		RMS,
		
		/** The onset detection. */
		ONSET_DETECTION,
		
		/** The pitch detection. */
		PITCH_DETECTION,
		
		/** The loudness. */
		LOUDNESS,
		
		/** The resample. */
		RESAMPLE,
		
		/** The convolution. */
		CONVOLUTION,
		
		/** The mfcc. */
		MFCC,
		
		/** The dct. */
		DCT,
		
		/** The phase vocoder. */
		PHASE_VOCODER
	}

	/**
	 * List of known audio processing libraries.
	 */
	private static String[] libraries = {"jxtract", "jaubio"};
	
	/**
	 * Loaded libraries.
	 */
	private static Set<String> loadedLibraries = new HashSet<String>();

	// Static initializer code
	static {
		// Try to load external libraries
		for (int i = 0; i < libraries.length; i++) {
			try {
				long start_time = System.currentTimeMillis();
				System.loadLibrary(libraries[i]);
				long end_time = System.currentTimeMillis();
				System.out.println("Library [" + libraries[i] + "] loaded in " + (end_time-start_time) + " ms");
				loadedLibraries.add(libraries[i]);
			} catch (UnsatisfiedLinkError e) {
	    		System.out.println("Failed to load the library [" + libraries[i] + "]");
	    		System.out.println(e.toString());
	    	}
		}

	}
	
	/**
	 * Creates the processor.
	 *
	 * @param library the library
	 * @param operation the operation
	 * @param arguments the arguments
	 * @return the process
	 */
	public static Process createProcessor(String library, String operation, Parameters arguments) {
		
		Process proc = null;
		
		switch (operation) {
		
		case FFT:

			// Validate arguments
			
			// Validate input data

			// Creates the corresponding FFT object
			proc = new Aubio_FFT();
//			proc = new LibXtract_FFT();
			break;
			
		case PHASE_VOCODER:
			
			proc = new Aubio_PhaseVocoder();
			break;

		case ONSET_DETECTION:

			proc = new OnsetsDS();
			break;
		
		case PITCH_DETECTION:

			proc = new Aubio_PitchDetection();
			break;
			
		case RMS:
			
			proc = new LibXtract_RMS();
			break;
		
		}
		
		// Initializes de Process object
		proc.setParameters(arguments);
		proc.configure();
		proc.start();

		return proc;
	}
	
	/**
	 * Delete processor.
	 *
	 * @param proc the proc
	 */
	public static void deleteProcessor(Process proc) {
		proc.stop();
	}
	
}
