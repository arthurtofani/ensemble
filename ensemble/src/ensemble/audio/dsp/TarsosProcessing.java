package ensemble.audio.dsp;

import be.hogent.tarsos.dsp.pitch.DynamicWavelet;

public class TarsosProcessing {

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
