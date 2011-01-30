package mms.processing;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import aubio.*;

import mms.Parameters;
import mms.processing.audio.Aubio_FFT;
import mms.processing.audio.Aubio_Onset;
import mms.processing.audio.Aubio_PhaseVocoder;
import mms.processing.audio.Aubio_PitchDetection;
import mms.processing.audio.LibXtract_RMS;
import mms.processing.audio.OnsetsDS;
import mms.tools.AudioInputFile;

// TODO Mem�ria para guardar os �ltimos frames, utilizados em processamentos com janela deslizantes
public class ProcessFactory {
	
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
				System.out.println("Library [" + libraries[i] + "] loaded in " + (end_time-start_time) + " ms !");
				loadedLibraries.add(libraries[i]);
			} catch (UnsatisfiedLinkError e) {
	    		System.out.println("Failed to load the library [" + libraries[i] + "]");
	    		System.out.println(e.toString());
	    	}
		}

	}
	
	public static Process createAudioProcessor(AudioOperation operation, Parameters arguments) {
		
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
		proc.configure(arguments);
		proc.start();

		return proc;
	}
	
	public static void main(String[] args) {
		
    	int N = 512;
    	double Fs = 44100;

		// Open input file
		AudioInputFile in_file = null;
		try {
			in_file = new AudioInputFile("sine_440_660.wav", true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		double[] chunk = in_file.readNextChunk(N);
//		System.out.println("Chunk");
//		for (int i = 0; i < N; i++) {
//			System.out.printf("%.2f\n", chunk[i]);
//		}
      
//		Process fftproc = ProcessFactory.createAudioProcessor(AudioOperation.FFT, null);
//		
//		Parameters fft_args = new Parameters();
//		fft_args.put("size", String.valueOf(N));
//		fft_args.put("output_type", "polar");
//		fft_args.put("sample_rate", String.valueOf(Fs));
//		double[] fftbuf = (double[])fftproc.process(fft_args, chunk);
//
//		// FFT Results
//		System.out.println("FFT Results");
//		for (int i = 0; i < N/2+1; i++) {
//			System.out.printf("%.2f Hz \t %.2f \t %.2f\n", i*Fs/N, fftbuf[i*2], fftbuf[i*2+1]);
//		}
//    
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
		
		// Pitch Detection
		Parameters pd_args = new Parameters();
		pd_args.put("bufsize", String.valueOf(N));
		pd_args.put("hopsize", String.valueOf(256));
		pd_args.put("sample_rate", String.valueOf(Fs));
		pd_args.put("type", "yin");
		pd_args.put("mode", "freq");
		Process pdproc = ProcessFactory.createAudioProcessor(AudioOperation.PITCH_DETECTION, pd_args);
		float res = (Float)pdproc.process(pd_args, chunk);
		
		System.out.println("res = " + res);
		    	
    }
	
}
