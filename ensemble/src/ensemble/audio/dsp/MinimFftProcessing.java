package ensemble.audio.dsp;

import java.util.Random;

import ddf.minim.analysis.FFT;

// TODO: Auto-generated Javadoc
/**
 * The Class MinimFftProcessing.
 */
public class MinimFftProcessing {

	
	
	
	/**
	 * Gets the band fft.
	 *
	 * @param buffer the buffer
	 * @param sampleRate the sample rate
	 * @param timeSize the time size
	 * @param chunksize the chunksize
	 * @return the band fft
	 */
	public static double getBandFft(double[] buffer, int sampleRate,int timeSize, int chunksize)
	{
		Random rnd = new Random();
		float[] input = new float[2048];
		for (int i = 0; i < 2048; i++){
			input[i]= rnd.nextInt(1000)/1000;
			//input[i]= (float) buffer[i];			
		} 
		System.out.println("timeSize" + timeSize);
		FFT fft = new FFT(2048, sampleRate);
		//fft.forward(input);
		// fft.getAverageCenterFrequency(fft.freqToIndex(440)); 
		//fft.freqToIndex(440);
		
		return fft.getBand(20);
	}
}
