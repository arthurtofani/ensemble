import org.boris.jvst.AEffect;
import org.boris.jvst.VST;
import org.boris.jvst.VSTException;
import org.boris.jvst.struct.VstParameterProperties;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			//AEffect a = VST.load("mda Delay.dll");
			//AEffect a = VST.load("mda Overdrive.dll");
			//AEffect a = VST.load("mda Multiband.dll");
			//AEffect a = VST.load("mda Talkbox.dll");
			//AEffect a = VST.load("mda RePsycho!.dll");
			//AEffect a = VST.load("mda ThruZero.dll");
			//AEffect a = VST.load("EngineersFilter.dll");
			//AEffect a = VST.load("mda Tracker.dll");
			//AEffect a = VST.load("tuner.dll");
			
			//AEffect a = VST.load("Auto-Filter.dll");
			AEffect a = VST.load("PitchShifter.dll");
			
			
			a.open();
			a.setSampleRate(44100.0f);
			a.setBlockSize(512);
			// attempt some processing
	        int blocksize = 512;
	        float[][] inputs = new float[a.numInputs][];
	        for (int i = 0; i < a.numInputs; i++) {
	            inputs[i] = new float[blocksize];
	            for (int j = 0; j < blocksize; j++)
	                inputs[i][j] = (float) Math
	                        .sin(j * Math.PI * 2 * 440 / 44100.0);
	            
	        }
	        
	      /*//ThruZero Flanger Parameters
	      //Rate (sec)
	        a.setParameter(0, new Float(0.5));
	      //Depth (ms)
	      // a.setParameter(1, new Float(0.4));
	      //Mix (%)
	        a.setParameter(2, new Float(0.2));
	      //Feedback (%)
	        a.setParameter(3, new Float(-0.01));*/
	        
	        
	      /*//RePsycho Parameters
	        //Tune (semi)
	        a.setParameter(0, new Float(1.1));
	        //Fine (cent)
	        //a.setParameter(1, new Float(0.7));
	        //Decay (%)
	        //a.setParameter(2, new Float(0.1));
	        //MIX (%)
	        a.setParameter(5, new Float(1));*/
	        
	       /* //Talkbox Parameters
	        //Wet (%)
	        a.setParameter(0, new Float(0.3));
	        //Dry (%)
	        a.setParameter(1, new Float(0.7));*/
	      
	        /*//Multiband Parameters
	        //Listen Output
	        a.setParameter(0, new Float(0.3));
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
	        a.setParameter(9, new Float(0.1));
	        //Release (ms)
	        a.setParameter(10, new Float(0.5));
	        //Stereo (% Width)
	        //a.setParameter(11, new Float(0.5));	        
	        //Process 
	        //a.setParameter(12, new Float(-1));
*/	          
	       /*//Overdrive Parameters
	        //Drive (%)
	        a.setParameter(0, new Float(0.5));
	        //Muffle (%)
	        a.setParameter(1, new Float(0.3));	        
	        //Output  (dB) 
	        a.setParameter(2, new Float(0.9));*/
	        
	        /*//Delay Parameters
	        //L Delay (ms)
	        a.setParameter(0, new Float(5));
	        //Feedback (%)
	        a.setParameter(2, new Float(0.8));	        
	        //Fb Tone  Lo <> Hi
	        a.setParameter(3, new Float(3));
	        //FX Mix  (%) 
	        a.setParameter(4, new Float(1.2));
	        //Output  (dB) 
	        a.setParameter(5, new Float(0.7));*/
	        
	        
	        //Auto filter Parameters
	        //Filter = Band-Pass %
	       /* a.setParameter(0, new Float(0.1));
	        //Frequency = 1349  Hz %
	        a.setParameter(1, new Float(0.14));	        
	        // Feedback = 50.00 % %
	        a.setParameter(2, new Float(0.35));
	        //Rate = 7.943 Hz %
	        //a.setParameter(3, new Float(1.2));
	        //R. Hold = On %
	        //a.setParameter(4, new Float(0.7));
	        
	        //Parameter[5]: Sync. = On %
	        a.setParameter(5, new Float(0));
	        //Parameter[6]: T.S.N = 1 %
	        //Parameter[7]: T.S.D = 16 %
	        //Parameter[8]: Spread = 1.000 %
	        //Parameter[9]: LFO Depth = 50.00 % %
	        
	        //Parameter[10]: Attack = 2.000 s %
	        a.setParameter(10, new Float(0.1));
	        //Parameter[11]: Release = 10.00 s %
	        a.setParameter(11, new Float(0.1));
	        //Parameter[12]: Env Depth = 0.000 % %
	        a.setParameter(12, new Float(0.42));
	        //Parameter[13]: Mix = 60:40 % %
	        a.setParameter(13, new Float(0.88));
	        //Parameter[14]: Level = 0.000 dB %
*/	        
	        a.setProgram(3);
	        a.setParameter(0, new Float(0.01));
	        
	        
	        float[][] outputs = new float[a.numOutputs][];
	        for (int i = 0; i < a.numOutputs; i++) {
	            outputs[i] = new float[blocksize];
	            for (int j = 0; j < blocksize; j++)
	                outputs[i][j] = 0;
	        }
	        
	        
	        for (int i = 0; i < a.numParams; i++) {
	        	System.out.println("Parameter[" +i+"]: " + a.getParameterName(i)+ " = "+ a.getParameterDisplay(i) + " " +  a.getParameterLabel(i));
	        	
	        	//VstParameterProperties properties = a.getParameterProperties(i);
	        	
	        }
	       
	        
	        a.processReplacing(inputs, outputs, blocksize);
	        
	        
	        for (int i = 0; i < a.numOutputs; i++) {
	            
	            for (int j = 0; j < blocksize; j++)
	            	System.out.println("input[" +i+"]"+ "[" +j+"] ="+ inputs[i][j] + "  ouput[" +i+"]"+ "[" +j+"] ="+ outputs[i][j] );
	            	
	        }
	        
	        VST.dispose(a);
		} catch (VSTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
