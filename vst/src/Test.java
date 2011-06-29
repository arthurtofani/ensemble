import org.boris.jvst.AEffect;
import org.boris.jvst.VST;
import org.boris.jvst.VSTException;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			AEffect a = VST.load("Freeverb2.dll");
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
	        float[][] outputs = new float[a.numOutputs][];
	        for (int i = 0; i < a.numOutputs; i++) {
	            outputs[i] = new float[blocksize];
	            for (int j = 0; j < blocksize; j++)
	                outputs[i][j] = 0;
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
