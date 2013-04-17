package ensemble.audio.vst;

import org.boris.jvst.AEffect;
import org.boris.jvst.VST;
import org.boris.jvst.VSTException;

import ensemble.audio.vst.VstConstants.FilterMode;

// TODO: Auto-generated Javadoc
/**
 * The Class VstProcessReasoning.
 */
public class VstProcessReasoning {

	/**
	 * Process audio.
	 *
	 * @param vstDll the vst dll
	 * @param dBuffer the d buffer
	 * @param dTransBuffer the d trans buffer
	 * @param nframes the nframes
	 * @throws VSTException the vST exception
	 */
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
		       /* a.setParameter(0, new Float(5));
		        //Feedback (%)
		        a.setParameter(2, new Float(0.8));	        
		        //Fb Tone  Lo <> Hi
		        a.setParameter(3, new Float(3));
		        //FX Mix  (%) 
		        a.setParameter(4, new Float(1.2));*/
		        //Output  (dB) 
		        a.setParameter(5, new Float(0.4));
	        }else if(vstDll.indexOf("Overdrive")>=0){
	        	//Drive (%)
	        	 a.setParameter(0, new Float(0.2));
	 	        //Muffle (%)
	 	        a.setParameter(1, new Float(0.3));	        
	 	        //Output  (dB) 
	 	        a.setParameter(2, new Float(0.7));
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
	  	        
	        }else if(vstDll.indexOf("MadShifta")>=0){
	        	
	        	
	        }else if(vstDll.indexOf("PitchShifter")>=0){
	        	a.setProgram(3);
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
	
	
/**
 * Process filter.
 *
 * @param dBuffer the d buffer
 * @param dTransBuffer the d trans buffer
 * @param nframes the nframes
 * @param filterMode the filter mode
 * @throws VSTException the vST exception
 */
public void ProcessFilter(double[] dBuffer, double[] dTransBuffer, int nframes, FilterMode filterMode ) throws VSTException{
		
		//double[] dTransBuffer = new double[nframes];
		//int numInputs = 0;
		AEffect a;
		a = VST.load("lib\\vst\\Auto-Filter.dll");
		
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
	        
	        //Auto filter Parameters
	        //Filter = Band-Pass %
	        //a.setParameter(0, new Float(0));
	        //Frequency = 1349  Hz %
	        //a.setParameter(1, new Float(0.1));	        
	        // Feedback = 50.00 % %
	        //a.setParameter(2, new Float(3));
	        //Rate = 7.943 Hz %
	        //a.setParameter(3, new Float(1.2));
	        //R. Hold = On %
	        //a.setParameter(4, new Float(0.7));
	        //R. Hold = On %
	        //a.setParameter(5, new Float(0.7));
	        //T.S.N = 1 %
	        //a.setParameter(6, new Float(0.7));
	        
	        switch (filterMode) {
			case LOW_PASS:
				
				
				/*Parameter[0]: Filter = Low-Pass %
				Parameter[1]: Frequency = 115.6 Hz %
				Parameter[2]: Feedback = 35.00 % %
				Parameter[3]: Rate = 7.943 Hz %
				Parameter[4]: R. Hold = On %
				Parameter[5]: Sync. = Off %
				Parameter[6]: T.S.N = 1 %
				Parameter[7]: T.S.D = 16 %
				Parameter[8]: Spread = 1.000 %
				Parameter[9]: LFO Depth = 50.00 % %
				Parameter[10]: Attack = 0.200 s %
				Parameter[11]: Release = 0.158 s %
				Parameter[12]: Env Depth = -16.0 % %
				Parameter[13]: Mix = 12:88 % %
				Parameter[14]: Level = 0.000 dB %
				*/
				
				
				 //Auto filter Parameters for LOW PASS
		        //Filter %
		        a.setParameter(0, new Float(0.1));
		        //Frequency
		        a.setParameter(1, new Float(0.14));	        
		        // Feedback 
		        a.setParameter(2, new Float(0.35));
		        
		        //Parameter[5]: Sync. = Off %
		        a.setParameter(5, new Float(0));
		       
		        //Parameter[10]: Attack
		        a.setParameter(10, new Float(0.1));
		        //Parameter[11]: Release
		        a.setParameter(11, new Float(0.1));
		        //Parameter[12]: Env Depth 
		        a.setParameter(12, new Float(0.42));
		        //Parameter[13]: Mix 
		        a.setParameter(13, new Float(0.88));
		       
				//a.setProgram(3);
		        
				break;

			case MID_PASS:
				
				//Filter = Band-Pass %
				 a.setProgram(3);
			    
				break;
				
			case HIGH_PASS:
				
				//Filter = High-Pass %
		        a.setParameter(0, new Float(0.2));
		        
				break;
				
			default:
				break;
			}
	        
	        a.processReplacing(inputs, outputs, nframes);

	        VST.dispose(a); 
		
	      //for (int i = 0; i < a.numOutputs; i++) {
			 for (int j = 0; j < nframes; j++){
				 
				 dTransBuffer[j] = new Double(outputs[0][j]);
				 //System.out.println(" dBuffer " + (dBuffer[j]) + " dTransBuffer " + (dTransBuffer[j]));
			 }
			 

	}

	
	/**
	 * Waveshaper.
	 *
	 * @param samples the samples
	 * @param count the count
	 * @param amount the amount
	 */
	public void waveshaper(double[] samples, int count, double amount) {
		double k;

		k = 2 * amount / (1 - amount);

		for (int i = 0; i < count; i++) {
			samples[i] = (1 + k) * samples[i] / (1 + k * Math.abs(samples[i]));
		}
	}

	

}
