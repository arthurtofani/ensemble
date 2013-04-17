package ensemble.audio.dsp;

import be.hogent.tarsos.dsp.pitch.DynamicWavelet;

// TODO: Auto-generated Javadoc
/**
 * The Class TarsosProcessing.
 */
public class TarsosProcessing {

	/**
	 * Pitch track.
	 *
	 * @param samples the samples
	 * @param count the count
	 * @param sampleRate the sample rate
	 * @return the float
	 */
	public float pitchTrack(double[] samples, int count,  float sampleRate){
		
		float[] aux = new float[count];
		for(int i = 0; i<count; i++)
		{
			aux[i]= (float)samples[i];
		}
		//AudioDispatcher dispatcher = AudioDispatcher.fromFloatArray(fIn, 44100, duration, 0);
		DynamicWavelet dw = new DynamicWavelet(sampleRate, count);
		return dw.getPitch(aux);
		
	}

}
