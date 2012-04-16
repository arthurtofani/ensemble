package ensemble.audio.dsp;

public class PitchShifting {

	private int blockSize;
	private int sampleRate;
	private double parameter1 = 1;
	
	
	public PitchShifting(int sRate, int bSize) {
		sampleRate = sRate;
		blockSize = bSize;
	}
	
	
	public int getBlockSize() {
		return blockSize;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	public void setParams(double param1) {
		parameter1 = param1;
	}
	
	public double getParameter1() {
		return parameter1;
	}

	/**
	 * Shifts the pitch of the incoming signal.
	 */
	public void perform(double[] buffer) {
		// Pitch Shifting parameters
		int N = this.getBlockSize();						// block length
		int Sa = 256;// N / 2;										// analysis hop size
		double alpha = this.getParameter1() * 1.75 + 0.25;	// pitch scaling factor
		int L = 128;//(int) (256 * alpha / 2);					// overlap interval
		int M = (int) Math.ceil(this.getBlockSize() / Sa);
		int Ss = (int) Math.round(Sa*alpha);
		buffer = (double[])resizeArray(buffer,M*Sa+N);
		//buffer[M*Sa+N] = 0;


		//===================================================================
		// TimeScaleSOLA loop.
		//===================================================================
		// Time Stretching using alpha2 = 1/alpha
		double alpha2 = 1.0/alpha;
		for (int ni = 0; ni < M-1; ni++) {
			// grain
			int grainStart = ni*Sa+1;
			int grainEnd = N+ni*Sa;
			int grainLength = grainEnd - grainStart + 1;
			double[] grain = new double[grainLength];
			System.arraycopy(buffer, grainStart, grain, 0, grainLength);
			
			// overlap
			double[] overlap = new double[L];
			
			
			double[] XCORRsegment = xcorr(grain, overlap);
			int km = getIndexOfMax(XCORRsegment);
			
			// fadeout
			double fadeStep = 1.0 / (overlap.length-(ni*Ss-(L-1)+km-1));
			int fadeLength = (int) (1.0 / fadeStep) + 1;
			double[] fadeout = new double[fadeLength];
			for (int i =0; i < fadeout.length; i++)
				fadeout[i] = 1 - i*fadeStep;
			
			// fadein
			double[] fadein = new double[fadeLength];
			for (int i =0; i < fadein.length; i++)
				fadein[i] = 0 + i*fadeStep;
			
			// tail
			double[] Tail = dotMultiply(overlap, fadeout);
			double[] Begin = dotMultiply(grain, fadein);
			
		}
	}
	
	/**
	 * Calculates the correlation between two vectors.
	 * @param x1
	 * @param x2
	 * @return
	 */
	private double[] xcorr(double[] x1, double[] x2)
	{
		//System.out.println("x1:" + x1.length + " x2:" + x2.length);
		int L = x1.length;
		x2 = (double[])resizeArray(x2,L);
		
		double[] r = new double[L];
		for (int k = 0; k < L; k++) {
			r[k] = 0;
			for (int n=0; n < L-k; n++)
				r[k] += x1[n]*x2[n+k];
			r[k] /= L; 
		}
		return r;
	}
	
	/**
	 * Returns the (first) index of the maximum value of an array.
	 * @param a
	 * @return
	 */
	private int getIndexOfMax(double[] a) {
		int k = 0;
		for (int i = 0; i < a.length; i++)
			if (a[i] > a[k])
				k = i;
		return k;
	}
	
	/**
	 * Dot-Multiply: each position of the resulting array is the multiplication of the corresponding position of the input arrays.
	 * @param x1
	 * @param x2
	 * @return
	 */
	private double[] dotMultiply(double[] x1, double[] x2) {
		double[] r = new double[x1.length];
		for (int i = 0; i < r.length; i++)
			r[i] = (double) (x1[i] * x2[i]);
		return r;
	}

	
	/**
	* Reallocates an array with a new size, and copies the contents
	* of the old array to the new array.
	* @param oldArray  the old array, to be reallocated.
	* @param newSize   the new array size.
	* @return          A new array with the same contents.
	*/
	private static Object resizeArray (Object oldArray, int newSize) {
	   int oldSize = java.lang.reflect.Array.getLength(oldArray);
	   Class elementType = oldArray.getClass().getComponentType();
	   Object newArray = java.lang.reflect.Array.newInstance(
	         elementType,newSize);
	   int preserveLength = Math.min(oldSize,newSize);
	   if (preserveLength > 0)
	      System.arraycopy (oldArray,0,newArray,0,preserveLength);
	   return newArray; }
}
