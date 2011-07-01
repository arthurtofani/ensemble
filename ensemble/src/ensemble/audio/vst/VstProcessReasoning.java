package ensemble.audio.vst;

import org.boris.jvst.AEffect;
import org.boris.jvst.VST;
import org.boris.jvst.VSTException;

public class VstProcessReasoning {

	public void ProcessAudio(String vstDll, double[] dBuffer, double[] dTransBuffer, int nframes ) throws VSTException{
		
		//double[] dTransBuffer = new double[nframes];
		//int numInputs = 0;
		AEffect a;
			a = VST.load(vstDll);
			a.open();
			a.setSampleRate(44100.0f);
			a.setBlockSize(nframes);
			//numInputs = a.numInputs;
			float[][] inputs = new float[a.numInputs][];
	        for (int i = 0; i < a.numInputs; i++) {
	            inputs[i] = new float[nframes];
	            for (int j = 0; j < nframes; j++)
	                inputs[i][j] = (float) dBuffer[j];
	        }
	        float[][] outputs = new float[a.numOutputs][];
	        for (int i = 0; i < a.numOutputs; i++) {
	            outputs[i] = new float[nframes];
	            for (int j = 0; j < nframes; j++)
	                outputs[i][j] = 0;
	        }
	        //a.setParameter(0, new Float(1));
	        a.processReplacing(inputs, outputs, nframes);

	        VST.dispose(a); 
		
	      //for (int i = 0; i < a.numOutputs; i++) {
			 for (int j = 0; j < nframes; j++){
				 
				 dTransBuffer[j] = new Double(outputs[0][j]);
				 //System.out.println(" dBuffer " + (dBuffer[j]) + " dTransBuffer " + (dTransBuffer[j]));
			 }
		//}
			 
			// return dTransBuffer;
	}
}
