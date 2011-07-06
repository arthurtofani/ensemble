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
	        
	        if(vstDll.indexOf("Delay")>=0){
	        	
	        	 //Delay Parameters
	        	//L Delay (ms)
		        a.setParameter(0, new Float(5));
		        //Feedback (%)
		        a.setParameter(2, new Float(0.8));	        
		        //Fb Tone  Lo <> Hi
		        a.setParameter(3, new Float(3));
		        //FX Mix  (%) 
		        a.setParameter(4, new Float(1.2));
		        //Output  (dB) 
		        a.setParameter(5, new Float(0.7));
	        }else if(vstDll.indexOf("Overdrive")>=0){
	        	//Drive (%)
		        a.setParameter(0, new Float(0.5));
		        //Muffle (%)
		        a.setParameter(1, new Float(0.3));	        
		        //Output  (dB) 
		        a.setParameter(2, new Float(0.9));
	        }else if(vstDll.indexOf("Multiband")>=0){
	        	
	        	//Multiband Parameters
	        	 //Listen Output
	        	a.setParameter(0, new Float(0));
		        //L <> M (Hz)
		        a.setParameter(1, new Float(0.4));	        
		        //M <> H (Hz)
		        a.setParameter(2, new Float(1));	        
		        //L Comp (dB)
		        //a.setParameter(3, new Float(0.3));	        
		        //M Comp (dB)
		        //a.setParameter(4, new Float(0.3));	   
		        //H Comp   (dB) 
		        //a.setParameter(5, new Float(0.3));	   
		        //L Out  (dB) 
		        a.setParameter(6, new Float(-1));
		        //M Out  (dB) 
		        a.setParameter(7, new Float(-0.7));
		        //H Out  (dB) 
		        a.setParameter(8, new Float(0.8));
		        //Attack (µs)
		        //a.setParameter(9, new Float(40));
		        //Release (ms)
		        a.setParameter(10, new Float(0.5));
	        }else if(vstDll.indexOf("Talkbox")>=0){

	        	//Talkbox Parameters
	        	//Wet (%)
	        	a.setParameter(0, new Float(0.3));
	        	//Dry (%)
	        	a.setParameter(1, new Float(0.7));
	        }else if(vstDll.indexOf("RePsycho")>=0){
	        	//RePsycho Parameters
	        	//Tune (semi)
	        	a.setParameter(0, new Float(1.1));
		        //Fine (cent)
		        //a.setParameter(1, new Float(0.7));
		        //Decay (%)
		        //a.setParameter(2, new Float(0.1));
		        //MIX (%)
		        a.setParameter(5, new Float(1));
	        }else if(vstDll.indexOf("ThruZero")>=0){
	        	

	        	//ThruZero Flanger Parameters
	  	      //Rate (sec)
	  	        a.setParameter(0, new Float(0.5));
	  	      //Depth (ms)
	  	      // a.setParameter(1, new Float(0.4));
	  	      //Mix (%)
	  	        a.setParameter(2, new Float(0.2));
	  	      //Feedback (%)
	  	        a.setParameter(3, new Float(-0.01));
	  	        
	        }
	        
	        
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
