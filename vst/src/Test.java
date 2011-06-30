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
			//AEffect a = VST.load("Freeverb2.dll");
			AEffect a = VST.load("mda Delay");
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
	        
	        a.setParameter(0, new Float(1));
	        a.setParameter(4, new Float(0.5));
	        
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
