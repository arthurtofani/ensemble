package ensemble.audio.dsp;

// TODO: Auto-generated Javadoc
/**
 * The Class FilterProcessing.
 */
public class FilterProcessing {
	
	/** The pi. */
	double pi= 3.14159265;
	

	/** The in_1. */
	static double in_1=0;
	
	/** The in_2. */
	static double in_2=0;
	
	/** The out_1. */
	static double out_1=0;
	
	/** The out_2. */
	static double out_2=0;
	
	
	/**
	 * Gets the lP coefficients butterworth2 pole.
	 *
	 * @param samplerate the samplerate
	 * @param cutoff the cutoff
	 * @param ax the ax
	 * @param by the by
	 * @return the lP coefficients butterworth2 pole
	 */
	public static void getLPCoefficientsButterworth2Pole(int samplerate,
			double cutoff, double[] ax, double[] by) {
		double PI = 3.1415926535897932385;
		double sqrt2 = 1.4142135623730950488;

		double QcRaw = (2 * PI * cutoff) / samplerate; // Find cutoff frequency
														// in [0..PI]
		double QcWarp = Math.tan(QcRaw); // Warp cutoff frequency

		double gain = 1 / (1 + sqrt2 / QcWarp + 2 / (QcWarp * QcWarp));
		by[2] = (1 - sqrt2 / QcWarp + 2 / (QcWarp * QcWarp)) * gain;
		by[1] = (2 - 2 * 2 / (QcWarp * QcWarp)) * gain;
		by[0] = 1;
		ax[0] = 1 * gain;
		ax[1] = 2 * gain;
		ax[2] = 1 * gain;
	}
	
	/**
	 * Filter.
	 *
	 * @param samples the samples
	 * @param count the count
	 */
	public void filter(double[] samples, int count)
	{
		double[] xv = new double[3];
		double[] yv = new double[3];
		
	   double[] ax = new double[3];
	   double[] by = new double[3];

	   getLPCoefficientsButterworth2Pole(44100, 5000, ax, by);

	   for (int i=0;i<count;i++)
	   {
	       xv[2] = xv[1]; xv[1] = xv[0];
	       xv[0] = samples[i];
	       yv[2] = yv[1]; yv[1] = yv[0];

	       yv[0] =   (ax[0] * xv[0] + ax[1] * xv[1] + ax[2] * xv[2]
	                    - by[1] * yv[0]
	                    - by[2] * yv[1]);

	       samples[i] = yv[0];
	   }
	}

	/**
	 * Four poles low pass.
	 *
	 * @param samples the samples
	 * @param out the out
	 * @param count the count
	 * @param freq the freq
	 */
	public void FourPolesLowPass(double[] samples, double[] out, int count, double freq){

		double[] coef = new double[9];
		double[] d = new double[4];
		double omega = freq; // peak freq
		double g = 0.9; // peak mag

		// calculating coefficients:

		double k, p, q, a;
		double a0, a1, a2, a3, a4;

		k = (4.0 * g - 3.0) / (g + 1.0);
		p = 1.0 - 0.25 * k;
		p *= p;

		// LP:
		a = 1.0 / (Math.tan(0.5 * omega) * (1.0 + p));
		p = 1.0 + a;
		q = 1.0 - a;

		a0 = 1.0 / (k + p * p * p * p);
		a1 = 4.0 * (k + p * p * p * q);
		a2 = 6.0 * (k + p * p * q * q);
		a3 = 4.0 * (k + p * q * q * q);
		a4 = (k + q * q * q * q);
		p = a0 * (k + 1.0);

		coef[0] = p;
		coef[1] = 4.0 * p;
		coef[2] = 6.0 * p;
		coef[3] = 4.0 * p;
		coef[4] = p;
		coef[5] = -a1 * a0;
		coef[6] = -a2 * a0;
		coef[7] = -a3 * a0;
		coef[8] = -a4 * a0;

		
		for (int i = 0; i < count; i++) {
			out[i] = coef[0] * samples[i] + d[0];
			d[0] = coef[1] * samples[i] + coef[5] * out[i] + d[1];
			d[1] = coef[2] * samples[i] + coef[6] * out[i] + d[2];
			d[2] = coef[3] * samples[i] + coef[7] * out[i] + d[3];
			d[3] = coef[4] * samples[i] + coef[8] * out[i];
		}
		
}

	/**
	 * Four poles high pass.
	 *
	 * @param samples the samples
	 * @param out the out
	 * @param count the count
	 * @param freq the freq
	 */
	public void FourPolesHighPass(double[] samples, double[] out, int count, double freq){

		double[] coef = new double[9];
		double[] d = new double[4];
		double omega = freq; // peak freq
		double g = 0.9; // peak mag

		// calculating coefficients:

		double k, p, q, a;
		double a0, a1, a2, a3, a4;

		k = (4.0 * g - 3.0) / (g + 1.0);
		p = 1.0 - 0.25 * k;
		p *= p;

		//HP:
		a=Math.tan(0.5*omega)/(1.0+p);
		p=a+1.0;
		q=a-1.0;
		        
		a0=1.0/(p*p*p*p+k);
		a1=4.0*(p*p*p*q-k);
		a2=6.0*(p*p*q*q+k);
		a3=4.0*(p*q*q*q-k);
		a4=    (q*q*q*q+k);
		p=a0*(k+1.0);
		        
		coef[0]=p;
		coef[1]=-4.0*p;
		coef[2]=6.0*p;
		coef[3]=-4.0*p;
		coef[4]=p;
		coef[5]=-a1*a0;
		coef[6]=-a2*a0;
		coef[7]=-a3*a0;
		coef[8]=-a4*a0;
		
		for (int i = 0; i < count; i++) {
			out[i] = coef[0] * samples[i] + d[0];
			d[0] = coef[1] * samples[i] + coef[5] * out[i] + d[1];
			d[1] = coef[2] * samples[i] + coef[6] * out[i] + d[2];
			d[2] = coef[3] * samples[i] + coef[7] * out[i] + d[3];
			d[3] = coef[4] * samples[i] + coef[8] * out[i];
		}
		
}
	
	/**
	 * Low pass.
	 *
	 * @param samples the samples
	 * @param out the out
	 * @param count the count
	 * @param freq the freq
	 * @param sampleRate the sample rate
	 */
	public void lowPass(double[] samples, double[] out, int count, double freq, float sampleRate){
/*
		r  = rez amount, from sqrt(2) to ~ 0.1
		f  = cutoff frequency
		(from ~0 Hz to SampleRate/2 - though many
		synths seem to filter only  up to SampleRate/4)
		
		The filter algo:
		out(n) = a1 * in + a2 * in(n-1) + a3 * in(n-2) - b1*out(n-1) - b2*out(n-2)
*/		
		double r = 1; //Math.sqrt(2);
		
		//Lowpass:
		      double c = 1.0 / Math.tan(pi * freq / sampleRate) * 0.957;
		      double a1 = 1.0 / ( 1.0 + r * c + c * c);
		      double a2 = 2* a1;
		      double a3 = a1;
		      double b1 = 2.0 * ( 1.0 - c*c) * a1;
		      double b2 = ( 1.0 - r * c + c * c) * a1;
		      
		      out[0]= a1 * samples[0] + a2 * in_1 + a3 * in_2 - b1*out_1 - b2*out_2;
		      out[1]= a1 * samples[1] + a2 * samples[0] + a3 * in_1 - b1*out[0] - b2*out_1;
		      
		      //double max = 0;
		      //System.out.println("in-1" + in_1 + "in-2" + in_2 + "out-1" + out_1 + "out-2" + out_2); 
		      for (int i = 2; i < count; i++) {
		    	  out[i] = a1 * samples[i] + a2 * samples[i-1] + a3 * samples[i-2] - b1*out[i-1] - b2*out[i-2];
		    	  //if(out[i]< max)max = out[i];
		      }
		      //System.out.println("sampleRate = "+ sampleRate +  "f = "+ freq + "  Coefs: a1 " + a1  +" a2"+ a2 +" a3"+ a3 +" b1 "+ b1 +" b2 " +b2);
		      //System.out.println("out-1 = " + out[count-1]);
		      //System.out.println("out-2 = " + out[count-2]);
		      //System.out.println("MAX = " + max);
		      
		      in_1 = samples[count-1];
		      in_2 = samples[count-2];
		      out_1 = out[count-1];
		      out_2 = out[count-2];
		      
	}

	
	/**
	 * High pass.
	 *
	 * @param samples the samples
	 * @param out the out
	 * @param count the count
	 * @param freq the freq
	 * @param sampleRate the sample rate
	 */
	public void highPass(double[] samples, double[] out, int count, double freq, float sampleRate){
		/*
				r  = rez amount, from sqrt(2) to ~ 0.1
				f  = cutoff frequency
				(from ~0 Hz to SampleRate/2 - though many
				synths seem to filter only  up to SampleRate/4)
				
				The filter algo:
				out(n) = a1 * in + a2 * in(n-1) + a3 * in(n-2) - b1*out(n-1) - b2*out(n-2)
				
				Hipass:
			      c = tan(pi * f / sample_rate);
			
			      a1 = 1.0 / ( 1.0 + r * c + c * c);
			      a2 = -2*a1;
			      a3 = a1;
			      b1 = 2.0 * ( c*c - 1.0) * a1;
			      b2 = ( 1.0 - r * c + c * c) * a1;
		*/		
				double r = 1; //Math.sqrt(2);
				
				//Hipass:
				      double c = Math.tan(pi * freq / sampleRate) * 0.957;
				      double a1 = 1.0 / ( 1.0 + r * c + c * c);
				      double a2 = -2* a1;
				      double a3 = a1;
				      double b1 = 2.0 * (c*c - 1.0) * a1;
				      double b2 = ( 1.0 - r * c + c * c) * a1;
				      
				      out[0]= a1 * samples[0] + a2 * in_1 + a3 * in_2 - b1*out_1 - b2*out_2;
				      out[1]= a1 * samples[1] + a2 * samples[0] + a3 * in_1 - b1*out[0] - b2*out_1;
				      
				      //System.out.println("in-1" + in_1 + "in-2" + in_2 + "out-1" + out_1 + "out-2" + out_2); 
				      for (int i = 2; i < count; i++) {
				    	  out[i] = a1 * samples[i] + a2 * samples[i-1] + a3 * samples[i-2] - b1*out[i-1] - b2*out[i-2];
					  
				      }
				      //System.out.println("sampleRate = "+ sampleRate +  "f = "+ freq + "  Coefs: a1 " + a1  +" a2"+ a2 +" a3"+ a3 +" b1 "+ b1 +" b2 " +b2);
				      //System.out.println("out-1 = " + out[count-1]);
				      //System.out.println("out-2 = " + out[count-2]);
				      
				      
				      in_1 = samples[count-1];
				      in_2 = samples[count-2];
				      out_1 = out[count-1];
				      out_2 = out[count-2];
				      
			}

	
	/**
	 * Sets the lpf.
	 *
	 * @param fCut the f cut
	 * @param fSampling the f sampling
	 * @param a0 the a0
	 * @param a1 the a1
	 * @param b1 the b1
	 */
	public void SetLPF(double fCut, double fSampling, double a0, double a1, double b1)
	{
	    double w = 2.0 * fSampling;
	    double Norm;

	    fCut *= 2.0F * pi;
	    Norm = 1.0 / (fCut + w);
	    b1 = (w - fCut) * Norm;	   
		a0 = a1 = fCut * Norm;
	}

	/**
	 * Sets the hpf.
	 *
	 * @param fCut the f cut
	 * @param fSampling the f sampling
	 * @param a0 the a0
	 * @param a1 the a1
	 * @param b1 the b1
	 */
	void SetHPF(double fCut, double fSampling, double a0, double a1, double b1)
	{
	    double w = 2.0 * fSampling;
	    double Norm;

	    fCut *= 2.0F * pi;
	    Norm = 1.0 / (fCut + w);
	    a0 = w * Norm;
	    a1 = -a0;
	    b1 = (w - fCut) * Norm;
	}

	/**
	 * Process lpf.
	 *
	 * @param samples the samples
	 * @param out the out
	 * @param count the count
	 * @param freq the freq
	 * @param sampleRate the sample rate
	 */
	public void ProcessLPF(double[] samples, double[] out, int count,
			double freq, double sampleRate) {

		double a0 = 0;
		double a1 = 0;
		double b1 = 0;
		
		 double w = 2.0 * sampleRate;
		    double Norm;

		    freq *= 2.0F * pi;
		    
		SetLPF(freq, sampleRate, a0, a1, b1);

		// out[n] = in[n]*a0 + in[n-1]*a1 + out[n-1]*b1;

		for (int i = 1; i < count; i++) {
			Norm = 1.0 / (freq + w);
		    a0 = w * Norm;
		    a1 = -a0;
		    b1 = (w - freq) * Norm;
		    System.out.println(a0 + " " + a1 + " " + b1);
			out[i] = samples[i] * a0 + samples[i - 1] * a1 + out[i - 1] * b1;
			System.out.println(out[i]);
		}
	}
}
