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


// TODO Mem�ria para guardar os �ltimos frames, utilizados em processamentos com janela deslizantes
public class ProcessorFactory {
	
	public enum AudioOperation {
		FFT,
		FILTER,
		RMS,
		ONSET_DETECTION,
		PITCH_DETECTION,
		LOUDNESS,
		RESAMPLE,
		CONVOLUTION,
		MFCC,
		DCT,
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
	
	public static Processor createAudioProcessor(AudioOperation operation, Parameters arguments) {
		
		Processor proc = null;
		
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
	
	public static void deleteAudioProcessor(Processor proc) {
		proc.stop();
	}

	
	public static void main(String[] args) {
		
    	final int N = 2048;
    	final double Fs = 44100;

		// Open input file
		AudioInputFile in_file = null;
		try {
			in_file = new AudioInputFile("media/sine_440_660.wav", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		final double[] chunk = in_file.readNextChunk(N);
//		System.out.println("Chunk");
//		for (int i = 0; i < N; i++) {
//			System.out.printf("%.2f\n", chunk[i]);
//		}
      
		Parameters parameters = new Parameters();
		parameters.put("size", String.valueOf(N));
		parameters.put("sample_rate", String.valueOf(Fs));
		final Processor fftproc = ProcessorFactory.createAudioProcessor(AudioOperation.FFT, parameters);
		final Processor fftproc2 = ProcessorFactory.createAudioProcessor(AudioOperation.FFT, parameters);

		new Thread() {
			public void run() {
				System.out.println("Entrei 1");
				for (int i = 0; i < 1000; i++) {
					Parameters fft_args = new Parameters();
					fft_args.put("size", String.valueOf(N));
					fft_args.put("output_type", "polar");
					fft_args.put("sample_rate", String.valueOf(Fs));
					double[] fftbuf = (double[])fftproc.process(fft_args, chunk);
				}
//				// FFT Results
//				System.out.println("FFT Results");
//				for (int i = 0; i < N/2+1; i++) {
//					System.out.printf("%.2f Hz \t %.2f \t %.2f\n", i*Fs/N, fftbuf[i*2], fftbuf[i*2+1]);
//				}
		    
			};
		}.start();

		new Thread() {
			public void run() {
				System.out.println("Entrei 2");
				for (int i = 0; i < 1000; i++) {
					Parameters fft_args = new Parameters();
					fft_args.put("size", String.valueOf(N));
					fft_args.put("output_type", "polar");
					fft_args.put("sample_rate", String.valueOf(Fs));
					double[] fftbuf = (double[])fftproc2.process(fft_args, chunk);
				}

				// FFT Results
//				System.out.println("FFT Results");
//				for (int i = 0; i < N/2+1; i++) {
//					System.out.printf("%.2f Hz \t %.2f \t %.2f\n", i*Fs/N, fftbuf[i*2], fftbuf[i*2+1]);
//				}
		    
			};
		}.start();

//		// IFFT Results
//		Parameters ifft_args = new Parameters();
//		ifft_args.put("size", String.valueOf(N));
//		ifft_args.put("inverse", "true");
//		double[] real = (double[])fftproc.process(ifft_args, fftbuf);
//		
//		// Results
//		System.out.println("IFFT Results");
//		for (int i = 0; i < N; i++) {
//			System.out.printf("%.2f\n", real[i]);
//		}
		
//		// Pitch Detection
//		Parameters pd_args = new Parameters();
//		pd_args.put("bufsize", String.valueOf(N));
//		pd_args.put("hopsize", String.valueOf(256));
//		pd_args.put("sample_rate", String.valueOf(Fs));
//		pd_args.put("type", "yin");
//		pd_args.put("mode", "freq");
//		Process pdproc = ProcessFactory.createAudioProcessor(AudioOperation.PITCH_DETECTION, pd_args);
//		float res = (Float)pdproc.process(pd_args, chunk);
//		
//		System.out.println("res = " + res);
		    	
    }
	
}
