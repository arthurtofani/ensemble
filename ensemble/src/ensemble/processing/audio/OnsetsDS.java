/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.processing.audio;

import java.util.ArrayList;

import ensemble.Parameters;
import ensemble.processing.Processor;
import ensemble.processing.ProcessorFactory;
import ensemble.processing.ProcessorFactory.AudioOperation;


// TODO: Auto-generated Javadoc
/**
 * The Class OnsetsDS.
 */
public class OnsetsDS extends Processor {

	/** The Constant ods_log1. */
	private final static double ods_log1 = -2.30258509;
	
	/** The Constant PI. */
	private final static double PI = 3.1415926535898;
	
	/** The Constant MINUSPI. */
	private final static double MINUSPI = -3.1415926535898;
	
	/** The Constant TWOPI. */
	private final static double TWOPI = 6.28318530717952646;
	
	/** The Constant INV_TWOPI. */
	private final static double INV_TWOPI = 0.1591549430919;
	
	/** The Constant ODS_LOG_LOWER_LIMIT. */
	private final static double ODS_LOG_LOWER_LIMIT = 2e-42;
	
	/** The Constant ODS_LOGOF_LOG_LOWER_LIMIT. */
	private final static double ODS_LOGOF_LOG_LOWER_LIMIT = -96.0154267;
	
	/** The Constant ODS_ABSINVOF_LOGOF_LOG_LOWER_LIMIT. */
	private final static double ODS_ABSINVOF_LOGOF_LOG_LOWER_LIMIT = 0.010414993;

	/** The n. */
	private int N;
	
	/** The ods. */
	private OnsetsDS_struct ods;
	
	/** The fftproc. */
	private Processor fftproc;
	
	/**
	 * The Enum output_type.
	 */
	enum output_type {
		
		/** The time. */
		TIME,
		
		/** The sample. */
		SAMPLE,
		
		/** The boolean. */
		BOOLEAN
	}
	
	/**
	* Types of incoming FFT data format. OnsetsDS needs to know where the FFT
	* data comes from in order to interpret it correctly.
	*/
	enum onsetsds_fft_types {
		
		/** The OD s_ ff t_ s c3_ complex. */
		ODS_FFT_SC3_COMPLEX,	  ///< SuperCollider, cartesian co-ords ("SCComplexBuf") - NB it's more efficient to provide polar data from SC
		/** The OD s_ ff t_ s c3_ polar. */
  	ODS_FFT_SC3_POLAR,	  ///< SuperCollider, polar co-ords ("SCPolarBuf")
		/** The OD s_ ff t_ fft w3_ hc. */
  	ODS_FFT_FFTW3_HC, ///< FFTW <a href="http://www.fftw.org/fftw3_doc/The-Halfcomplex_002dformat-DFT.html">"halfcomplex"</a> format 
		/** The OD s_ ff t_ fft w3_ r2 c. */
 ODS_FFT_FFTW3_R2C,   ///< FFTW regular format, typically produced using <a href="http://www.fftw.org/fftw3_doc/One_002dDimensional-DFTs-of-Real-Data.html#One_002dDimensional-DFTs-of-Real-Data">real-to-complex</a> transform
		/** The OD s_ ff t_ ss t_ r2 c. */
   ODS_FFT_SST_R2C,
		
		/** The OD s_ ff t_ aubi o_ r2 c. */
		ODS_FFT_AUBIO_R2C,
		
		/** The ods fft libxtract. */
		ODS_FFT_LIBXTRACT
	}

	/**
	 * Types of onset detection function.
	 */
	enum onsetsds_odf_types {
		
		/** The ods odf power. */
		ODS_ODF_POWER,    ///< Power
		/** The ods odf magsum. */
    ODS_ODF_MAGSUM,   ///< Sum of magnitudes
		/** The ods odf complex. */
   ODS_ODF_COMPLEX,  ///< Complex-domain deviation
		/** The ods odf rcomplex. */
  ODS_ODF_RCOMPLEX, ///< Complex-domain deviation, rectified (only increases counted)
		/** The ods odf phase. */
 ODS_ODF_PHASE,    ///< Phase deviation
		/** The ods odf wphase. */
    ODS_ODF_WPHASE,   ///< Weighted phase deviation
		/** The ods odf mkl. */
   ODS_ODF_MKL       ///< Modified Kullback-Liebler deviation
	}

	/**
	* Types of whitening - may not all be implemented yet.
	*/
	enum onsetsds_wh_types {
		
		/** The ods wh none. */
		ODS_WH_NONE, ///< No whitening - onsetsds_whiten() becomes a no-op
		/** The OD s_ w h_ adap t_ ma x1. */
 ODS_WH_ADAPT_MAX1, ///< Adaptive whitening - tracks recent-peak-magnitude in each bin, normalises that to 1
		/** The ods wh normmax. */
 ODS_WH_NORMMAX, ///< Simple normalisation - each frame is normalised (independent of others) so largest magnitude becomes 1. Not implemented.
		/** The ods wh normmean. */
 ODS_WH_NORMMEAN ///< Simple normalisation - each frame is normalised (independent of others) so mean magnitude becomes 1. Not implemented.
	}
	
	/**
	 * The Class OdsPolarBin.
	 */
	class OdsPolarBin { 
		
		/** The phase. */
		double mag, phase; 
	}

	/**
	 * The Class OdsPolarBuf.
	 */
	class OdsPolarBuf {
		
		/** The nyq. */
		double dc, nyq;
		
		/** The bin. */
		OdsPolarBin[] bin;
	}

	/// The main data structure for the onset detection routine
	/**
	 * The Class OnsetsDS_struct.
	 */
	class OnsetsDS_struct {
		
		/** The fft. */
		double[] fft;
		
		/** The psp. */
		double[] psp; ///< Peak Spectral Profile - size is numbins+2, data is stored in order dc through to nyquist
		
		/** The odfvals. */
		double[] odfvals; // odfvals[0] will be the current val, odfvals[1] prev, etc
		
		/** The sortbuf. */
		double[] sortbuf; // Used to calculate the median
		
		/** The other. */
		double[] other; // Typically stores data about the previous frame
		
		/** The curr. */
		OdsPolarBuf curr; // Current FFT frame, as polar
		
		/** The thresh. */
		double 
			srate, ///< The sampling rate of the input audio. Set by onsetsds_init()
			// Adaptive whitening params
			relaxtime, ///< Do NOT set this directly. Use onsetsds_setrelax() which will also update relaxcoef.
			relaxcoef, ///< Relaxation coefficient (memory coefficient). See also onsetsds_setrelax()
			floor,  ///< floor - the lowest value that a PSP magnitude can take.
			/// A parameter for the ODF. For most this is a magnitude threshold for a single bin to be considered;
			/// but for #ODS_ODF_MKL it is the "epsilon" parameter.
			odfparam,
			/// Value used internally to scale ODF value according to the FFT frame size. Automatically set by onsetsds_init()
			normfactor,
			// ODF val after median processing
			odfvalpost,
			// Previous val is needed for threshold-crossing detection
			odfvalpostprev,
			/// Threshold (of ODF value, after median processing) for detection.
			/// Values between 0 and 1 are expected, but outside this range may
			/// sometimes be appropriate too.
			thresh;
		
		/** The odftype. */
		onsetsds_odf_types 	odftype;    ///< Choose from #onsetsds_odf_types
		
		/** The whtype. */
		onsetsds_wh_types	whtype;     ///< Choose from #onsetsds_wh_types
		
		/** The fftformat. */
		onsetsds_fft_types  fftformat;  ///< Choose from #onsetsds_fft_types
		
		/** The med_odd. */
		boolean whiten,  ///< Whether to apply whitening - onsetsds_init() decides this on your behalf
			 detected,///< Output val - true if onset detected in curr frame
			 /*
			 NOT YET USED: Whether to convert magnitudes to log domain before processing. This is done as follows:
			 Magnitudes below a log-lower-limit threshold (ODS_LOG_LOWER_LIMIT) are pushed up to that threshold (to avoid log(0) infinity problems),
			 then the log is taken. The values are re-scaled to a similar range as the linear-domain values (assumed to lie
			 between zero and approximately one) by subtracting log(ODS_LOG_LOWER_LIMIT) and then dividing by abs(log(ODS_LOG_LOWER_LIMIT)).
			 */
			 logmags,
			 med_odd; ///< Whether median span is odd or not (used internally)

		/** The gapleft. */
		int 
			/// Number of frames used in median calculation
			medspan, 
			/// Size of enforced gap between detections, measured in FFT frames.
			mingap, gapleft;
		
		/** The numbins. */
		int fftsize, numbins; // numbins is the count not including DC/nyq
	}
	
	/**
	 * Ods_abs.
	 *
	 * @param a the a
	 * @return the double
	 */
	private double ods_abs(double a) {
		return ((a)<0? -(a) : (a));
	}
	
	/**
	 * Ods_max.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double
	 */
	private double ods_max(double a, double b) {
		return (((a) > (b)) ? (a) : (b));
	}
	
	/**
	 * Ods_min.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the double
	 */
	private double ods_min(double a, double b) {
		return (((a) < (b)) ? (a) : (b));
	}

	/**
	 * Onsetsds_phase_rewrap.
	 *
	 * @param phase the phase
	 * @return the double
	 */
	private double onsetsds_phase_rewrap(double phase){
		return (phase>MINUSPI && phase<PI) ? phase : phase + TWOPI * (1.f + Math.floor((MINUSPI - phase) * INV_TWOPI));
	}
	
	/**
	 * Onsetsds_memneeded.
	 *
	 * @param odftype the odftype
	 * @param fftsize the fftsize
	 * @param medspan the medspan
	 * @return the int
	 */
	private int onsetsds_memneeded (onsetsds_odf_types odftype, int fftsize, int medspan){

		/*
		Need memory for:
		- median calculation (2 * medspan floats)
		- storing old values (whether as OdsPolarBuf or as weirder float lists)
		- storing the OdsPolarBuf (size is NOT sizeof(OdsPolarBuf) but is fftsize)
		- storing the PSP (numbins + 2 values)
		All these are floats.
		*/

		int numbins = (fftsize >> 1) - 1; // No of bins, not counting DC/nyq
		System.out.println("numbins = " + numbins);

		switch(odftype){
			case ODS_ODF_POWER:
			case ODS_ODF_MAGSUM:

				// No old FFT frames needed, easy:
				return (medspan+medspan + fftsize + numbins + 2);

			case ODS_ODF_COMPLEX:
			case ODS_ODF_RCOMPLEX:

				return (medspan+medspan + fftsize + numbins + 2
						// For each bin (NOT dc/nyq) we store mag, phase and d_phase
						+ numbins + numbins + numbins);

			case ODS_ODF_PHASE:
			case ODS_ODF_WPHASE:

				return (medspan+medspan + fftsize + numbins + 2
						// For each bin (NOT dc/nyq) we store phase and d_phase
						+ numbins + numbins);

			case ODS_ODF_MKL:

				return (medspan+medspan + fftsize + numbins + 2
						// For each bin (NOT dc/nyq) we store mag
						+ numbins);
			default:	

				return -1; //bleh
		}

	}

	/**
	 * Onsetsds_setrelax.
	 *
	 * @param ods the ods
	 * @param time the time
	 * @param hopsize the hopsize
	 */
	void onsetsds_setrelax(OnsetsDS_struct ods, double time, int hopsize){
		ods.relaxtime = time;
		ods.relaxcoef = (time == 0.0f) ? 0.0f : Math.exp((ods_log1 * hopsize)/(time * ods.srate));
	}
	
	/**
	 * Onsetsds_init.
	 *
	 * @param ods the ods
	 * @param fftformat the fftformat
	 * @param odftype the odftype
	 * @param fftsize the fftsize
	 * @param medspan the medspan
	 * @param srate the srate
	 */
	private void onsetsds_init(OnsetsDS_struct ods, onsetsds_fft_types fftformat, onsetsds_odf_types odftype, int fftsize, int medspan, double srate){

		ods.srate = srate;
		
		int numbins  = (fftsize >> 1) - 1; // No of bins, not counting DC/nyq
		int realnumbins = numbins + 2;

		// Also point the other pointers to the right places
		ods.curr = new OdsPolarBuf();
		ods.curr.bin = new OdsPolarBin[numbins];
		for (int i = 0; i < numbins; i++) {
			ods.curr.bin[i] = new OdsPolarBin();
		}
		ods.psp      = new double[realnumbins];
		ods.odfvals  = new double[medspan];
		ods.sortbuf  = new double[medspan];
		switch(odftype) {
		case ODS_ODF_COMPLEX:
		case ODS_ODF_RCOMPLEX:
			ods.other = new double[numbins*3];
			break;
		case ODS_ODF_PHASE:
		case ODS_ODF_WPHASE:
			ods.other = new double[numbins*2];
			break;
		case ODS_ODF_MKL:
			ods.other = new double[numbins];
			break;
		}
		
		// Default settings for Adaptive Whitening, user can set own values after init
		onsetsds_setrelax(ods, 1.f, fftsize>>1);
		ods.floor    = 0.1f;
		
		switch(odftype){
		case ODS_ODF_POWER:
		ods.odfparam = 0.01f; // "powthresh" in SC code
		ods.normfactor = 2560.f / (realnumbins * fftsize);
		break;
		case ODS_ODF_MAGSUM:
		ods.odfparam = 0.01f; // "powthresh" in SC code
		ods.normfactor = 113.137085f / (realnumbins * Math.sqrt(fftsize));
		break;
		case ODS_ODF_COMPLEX:
		ods.odfparam = 0.01f; // "powthresh" in SC code
		ods.normfactor = 231.70475f / Math.pow(fftsize, 1.5);// / fftsize;
		break;
		case ODS_ODF_RCOMPLEX:
		ods.odfparam = 0.01f; // "powthresh" in SC code
		ods.normfactor = 231.70475f / Math.pow(fftsize, 1.5);// / fftsize;
		break;
		case ODS_ODF_PHASE:
		ods.odfparam = 0.01f; // "powthresh" in SC code
		ods.normfactor = 5.12f / fftsize;// / fftsize;
		break;
		case ODS_ODF_WPHASE:
		ods.odfparam = 0.0001f; // "powthresh" in SC code. For WPHASE it's kind of superfluous.
		ods.normfactor = 115.852375f / Math.pow(fftsize, 1.5);// / fftsize;
		break;
		case ODS_ODF_MKL:
		ods.odfparam = 0.01f; // EPSILON parameter. Brossier recommends 1e-6 but I (ICMC 2007) found larger vals (e.g 0.01) to work better
		ods.normfactor = 7.68f * 0.25f / fftsize;
		break;
		default:
		System.out.printf("onsetsds_init ERROR: \"odftype\" is not a recognised value\n");
		}
		
		ods.odfvalpost = 0.f;
		ods.odfvalpostprev = 0.f;
		ods.thresh   = 0.5f;
		ods.logmags = false;
		
		ods.odftype  = odftype;
		ods.whtype   = onsetsds_wh_types.ODS_WH_ADAPT_MAX1;
		ods.fftformat = fftformat;
		
		ods.whiten   = (odftype != onsetsds_odf_types.ODS_ODF_MKL); // Deactivate whitening for MKL by default
		ods.detected = false;
		ods.med_odd  = (medspan & 1) != 0;
		
		ods.medspan  = medspan;
		
		ods.mingap   = 0;
		ods.gapleft  = 0;
		
		ods.fftsize  = fftsize;
		ods.numbins  = numbins;
		
		//printf("End of _init: normfactor is %g\n", ods.normfactor);
		
	}

	/**
	 * Onsetsds_process.
	 *
	 * @param ods the ods
	 * @param fftbuf the fftbuf
	 * @return true, if successful
	 */
	private boolean onsetsds_process(OnsetsDS_struct ods, double[] fftbuf) {
		onsetsds_loadframe(ods, fftbuf);

		onsetsds_whiten(ods);
		onsetsds_odf(ods);
		onsetsds_detect(ods);
		
		return ods.detected;
	}	
	
	/**
	 * Onsetsds_loadframe.
	 *
	 * @param ods the ods
	 * @param fftbuf the fftbuf
	 */
	private void onsetsds_loadframe(OnsetsDS_struct ods, double[] fftbuf){
		
		double pos, pos2, imag, real;
		int i;
		
		switch(ods.fftformat){
				
			case ODS_FFT_SST_R2C:
				
				ods.curr.dc  = fftbuf[0];
				ods.curr.nyq = fftbuf[ods.fftsize];
				
				// Then convert cartesian to polar:
				for(i=0; i<ods.numbins; i++){
					real = fftbuf[2*(i+1)];
					imag = fftbuf[(2*(i+1))+1];
					ods.curr.bin[i].mag   = Math.hypot(imag, real);
					ods.curr.bin[i].phase = Math.atan2(imag, real);
				}
				break;
		
//			case ODS_FFT_FFTW3_HC:
//				
//				ods.curr.dc  = fftbuf[0];
//				ods.curr.nyq = fftbuf[ods.fftsize>>1];
//				
//				// Then convert cartesian to polar:
//				// (Starting positions: real and imag for bin 1)
//				pos  = fftbuf + 1;
//				pos2 = fftbuf + ods.fftsize - 1;
//				for(i=0; i<ods.numbins; i++){
//					real = *(pos++);
//					imag = *(pos2--);
//					ods.curr.bin[i].mag   = hypotf(imag, real);
//					ods.curr.bin[i].phase = atan2f(imag, real);
//				}
//				break;
//				
			case ODS_FFT_AUBIO_R2C:
			
				ods.curr.dc  = fftbuf[0];
				ods.curr.nyq = fftbuf[ods.fftsize];
				
				// Then convert cartesian to polar:
				for(i=0; i<ods.numbins; i++){
					ods.curr.bin[i].mag   = fftbuf[i*2];
					ods.curr.bin[i].phase = fftbuf[(i*2)+1];
				}
				break;
				
			case ODS_FFT_LIBXTRACT:
				
				
				break;
				
		}
		
		// Conversion to log-domain magnitudes, including re-scaling to aim back at the zero-to-one range.
		// Not well tested yet.
		if(ods.logmags){
			for(i=0; i<ods.numbins; i++){
				ods.curr.bin[i].mag = 
					(Math.log(ods_max(ods.curr.bin[i].mag, ODS_LOG_LOWER_LIMIT)) - ODS_LOGOF_LOG_LOWER_LIMIT) * ODS_ABSINVOF_LOGOF_LOG_LOWER_LIMIT;
			}
			ods.curr.dc = 
				(Math.log(ods_max(ods_abs(ods.curr.dc ), ODS_LOG_LOWER_LIMIT)) - ODS_LOGOF_LOG_LOWER_LIMIT) * ODS_ABSINVOF_LOGOF_LOG_LOWER_LIMIT;
			ods.curr.nyq = 
				(Math.log(ods_max(ods_abs(ods.curr.nyq), ODS_LOG_LOWER_LIMIT)) - ODS_LOGOF_LOG_LOWER_LIMIT) * ODS_ABSINVOF_LOGOF_LOG_LOWER_LIMIT;
		}
		
	}

	/**
	 * Onsetsds_whiten.
	 *
	 * @param ods the ods
	 */
	private void onsetsds_whiten(OnsetsDS_struct ods){
		
		if(ods.whtype == onsetsds_wh_types.ODS_WH_NONE){
			//printf("onsetsds_whiten(): ODS_WH_NONE, skipping\n");
			return;
		}
		
		// NB: Apart from the above, ods.whtype is currently IGNORED and only one mode is used.
		
		double val,oldval, relaxcoef, floor;
		int numbins, i;
		OdsPolarBuf curr;
		
		relaxcoef = ods.relaxcoef;
		numbins = ods.numbins;
		curr = ods.curr;
		floor = ods.floor;

//		System.out.printf("onsetsds_whiten: relaxcoef=%g, relaxtime=%g, floor=%g\n", relaxcoef, ods.relaxtime, floor);

		////////////////////// For each bin, update the record of the peak value /////////////////////
		
		val = Math.abs(curr.dc);	// Grab current magnitude
		oldval = ods.psp[0];
		// If it beats the amplitude stored then that's our new amplitude;
		// otherwise our new amplitude is a decayed version of the old one
		if(val < oldval) {
			val = val + (oldval - val) * relaxcoef;
		}
		ods.psp[0] = val; // Store the "amplitude trace" back
		
		val = Math.abs(curr.nyq);
		oldval = ods.psp[numbins+1];
		if(val < oldval) {
			val = val + (oldval - val) * relaxcoef;
		}
		ods.psp[numbins+1] = val;
		
		for(i=0; i<numbins; ++i){
			val = Math.abs(curr.bin[i].mag);
			oldval = ods.psp[i+1];
			if(val < oldval) {
				val = val + (oldval - val) * relaxcoef;
			}
			ods.psp[i+1] = val;
		}
		
		//////////////////////////// Now for each bin, rescale the current magnitude ////////////////////////////
		curr.dc  /= ods_max(floor, ods.psp[0]);
		curr.nyq /= ods_max(floor, ods.psp[numbins+1]);
		for(i=0; i<numbins; ++i){
			curr.bin[i].mag /= ods_max(floor, ods.psp[i+1]);
		}
	}

	/**
	 * Onsetsds_odf.
	 *
	 * @param ods the ods
	 */
	private void onsetsds_odf(OnsetsDS_struct ods){
		
		int numbins = ods.numbins;
		OdsPolarBuf curr = ods.curr;
		double[] val = ods.odfvals;
		int tbpointer;
		double deviation, diff, curmag;
		double totdev;
		
		boolean rectify = true;
		
		// Here we shunt the "old" ODF values down one place
		System.arraycopy(val, 0, val, 1, ods.medspan - 1);
		
		// Now calculate a new value and store in ods.odfvals[0]
		switch(ods.odftype){
			case ODS_ODF_POWER:
				
				val[0] = (curr.nyq  *  curr.nyq)  +  (curr.dc  *  curr.dc);
				for(int i = 0; i < numbins; i++){
					val[0] += curr.bin[i].mag  *  curr.bin[i].mag;
				}
				break;
				
			case ODS_ODF_MAGSUM:
		
				val[0] = ods_abs(curr.nyq) + ods_abs(curr.dc);
				for(int i = 0; i < numbins; i++){
					val[0] += ods_abs(curr.bin[i].mag);
				}
				break;
				
			case ODS_ODF_COMPLEX:
				rectify = false;
				// ...and then drop through to:
			case ODS_ODF_RCOMPLEX:
				
				// Note: "other" buf is stored in this format: mag[0],phase[0],d_phase[0],mag[1],phase[1],d_phase[1], ...
				
				// Iterate through, calculating the deviation from expected value.
				totdev = 0.0;
				tbpointer = 0;
				double predmag, predphase, yesterphase, yesterphasediff;
				for (int i = 0; i < numbins; ++i) {
					curmag = ods_abs(curr.bin[i].mag);
				
					// Predict mag as yestermag
					predmag         = ods.other[tbpointer++];
					yesterphase     = ods.other[tbpointer++];
					yesterphasediff = ods.other[tbpointer++];
					
					// Thresholding as Brossier did - discard (ignore) bin's deviation if bin's power is minimal
					if(curmag > ods.odfparam) {
						// If rectifying, ignore decreasing bins
						if((!rectify) || !(curmag < predmag)){
							
							// Predict phase as yesterval + yesterfirstdiff
							predphase = yesterphase + yesterphasediff;
							
							// Here temporarily using the "deviation" var to store the phase difference
							//  so that the rewrap macro can use it more efficiently
							deviation = predphase - curr.bin[i].phase;
							
							// Deviation is Euclidean distance between predicted and actual.
							// In polar coords: sqrt(r1^2 +  r2^2 - r1r2 cos (theta1 - theta2))
							deviation = Math.sqrt(predmag * predmag + curmag * curmag
											  - predmag * curmag * Math.cos(onsetsds_phase_rewrap(deviation))
											);			
							
							totdev += deviation;
						}
					}
				}
				
				// totdev will be the output, but first we need to fill tempbuf with today's values, ready for tomorrow.
				tbpointer = 0;
				for (int i = 0; i < numbins; ++i) {
					ods.other[tbpointer++] = ods_abs(curr.bin[i].mag); // Storing mag
					diff = curr.bin[i].phase - ods.other[tbpointer]; // Retrieving yesterphase from buf
					ods.other[tbpointer++] = curr.bin[i].phase; // Storing phase
					// Wrap onto +-PI range
					diff = onsetsds_phase_rewrap(diff);
					
					ods.other[tbpointer++] = diff; // Storing first diff to buf
					
				}
				val[0] = totdev;
				
				break;
				
			case ODS_ODF_PHASE:
				rectify = false; // So, actually, "rectify" means "useweighting" in this context
				// ...and then drop through to:
			case ODS_ODF_WPHASE:
				
				// Note: "other" buf is stored in this format: phase[0],d_phase[0],phase[1],d_phase[1], ...
				
				// Iterate through, calculating the deviation from expected value.
				totdev = 0.0;
				tbpointer = 0;
				for (int i = 0; i < numbins; ++i) {
					// Thresholding as Brossier did - discard (ignore) bin's phase deviation if bin's power is low
					if(ods_abs(curr.bin[i].mag) > ods.odfparam) {
						
						// Deviation is the *second difference* of the phase, which is calc'ed as curval - yesterval - yesterfirstdiff
						deviation = curr.bin[i].phase - ods.other[tbpointer++] - ods.other[tbpointer++];
						// Wrap onto +-PI range
						deviation = onsetsds_phase_rewrap(deviation);
						
						if(rectify){ // "rectify" meaning "useweighting"...
							totdev += Math.abs(deviation * ods_abs(curr.bin[i].mag));
						} else {
							totdev += Math.abs(deviation);
						}
					}
				}
				
				// totdev will be the output, but first we need to fill tempbuf with today's values, ready for tomorrow.
				tbpointer = 0;
				for (int i = 0; i < numbins; ++i) {
					diff = curr.bin[i].phase - ods.other[tbpointer]; // Retrieving yesterphase from buf
					ods.other[tbpointer++] = curr.bin[i].phase; // Storing phase
					// Wrap onto +-PI range
					diff = onsetsds_phase_rewrap(diff);
					
					ods.other[tbpointer++] = diff; // Storing first diff to buf
					
				}
				val[0] = totdev;
				break;
				
				
			case ODS_ODF_MKL:
				
				// Iterate through, calculating the Modified Kullback-Liebler distance
				totdev = 0.0;
				tbpointer = 0;
				double yestermag;
				for (int i = 0; i < numbins; ++i) {
					curmag = ods_abs(curr.bin[i].mag);
					yestermag = ods.other[tbpointer];
					
					// Here's the main implementation of Brossier's MKL eq'n (eqn 2.9 from his thesis):
					deviation = ods_abs(curmag) / (ods_abs(yestermag) + ods.odfparam);
					totdev += Math.log(1.f + deviation);
					
					// Store the mag as yestermag
					ods.other[tbpointer++] = curmag;
				}
				val[0] = totdev;
				break;
		
		}
			
		ods.odfvals[0] *= ods.normfactor;
	}
	// End of ODF function

	/**
	 * Selection sort.
	 *
	 * @param array the array
	 * @param length the length
	 */
	void selectionSort(double[] array, int length)
	{
	  // Algo is simply based on http://en.wikibooks.org/wiki/Algorithm_implementation/Sorting/Selection_sort
	  int max, i;
	  double temp;
	  while(length > 0)
	  {
	    max = 0;
	    for(i = 1; i < length; i++)
	      if(array[i] > array[max])
	        max = i;
	    temp = array[length-1];
	    array[length-1] = array[max];
	    array[max] = temp;
	    length--;
	  }
	}

	/**
	 * Onsetsds_detect.
	 *
	 * @param ods the ods
	 */
	private void onsetsds_detect(OnsetsDS_struct ods){
		
		// Shift the yesterval to its rightful place
		ods.odfvalpostprev = ods.odfvalpost;
		
		///////// MEDIAN REMOVAL ////////////
		
		double[] sortbuf = ods.sortbuf;
		int medspan = ods.medspan;
		
		// Copy odfvals to sortbuf
		System.arraycopy(ods.odfvals, 0, sortbuf, 0, medspan);
		
		// Sort sortbuf
		selectionSort(sortbuf, medspan);
				
		// Subtract the middlest value === the median
		if(ods.med_odd) {
			ods.odfvalpost = ods.odfvals[0] 
				   - sortbuf[(medspan - 1) >> 1];
		} else {
			ods.odfvalpost = ods.odfvals[0] 
				   - ((sortbuf[medspan >> 1]
					 + sortbuf[(medspan >> 1) - 1]) * 0.5f);	   
		}

		// Detection not allowed if we're too close to a previous detection.
		if(ods.gapleft != 0) {
			ods.gapleft--;
			ods.detected = false;
		} else {
			// Now do the detection.
			ods.detected = (ods.odfvalpost > ods.thresh) && (ods.odfvalpostprev <= ods.thresh);
			if(ods.detected){
				ods.gapleft = ods.mingap;
			}
		}
	}
	
	/**
	 * Hann_window.
	 *
	 * @param in the in
	 * @param offset the offset
	 * @param length the length
	 * @return the double[]
	 */
	public double[] hann_window(double[] in, int offset, int length) {
	    double[] out = new double[length];

		double a = 2.0 * Math.PI / (length-1);
	    for (int i = 0; i < length; i++) {
			out[i] = 0.5 * (1 - Math.cos(a * i)) * in[offset+i];
		}
	    
	    return out;
	}

	/** The sample_rate. */
	double 		sample_rate;
	
	/** The frame_size. */
	int 		frame_size;
	
	/** The onset_output. */
	output_type onset_output;
	
	// TODO Deixar mais configurável
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	public boolean init() {

		// Arguments
		sample_rate = Double.valueOf(parameters.get("sample_rate", "44100.0"));
		frame_size = Integer.valueOf(parameters.get("frame_size", "512"));
		String 	onset_output_str = parameters.get("onset_output", "sample"); // boolean (a bool valeu for each frame), time, sample
		if (onset_output_str.equals("sample")) {
			onset_output = output_type.SAMPLE;
		} else {
			onset_output = output_type.TIME;
		}

		N = 512;
		
		Parameters fft_args = new Parameters();
		fft_args.put("size", String.valueOf(N));
		fftproc = ProcessorFactory.createAudioProcessor(AudioOperation.FFT, fft_args);

		// An instance of the OnsetsDS struct, declared/allocated somewhere in your code,
		// however you want to do it.
		ods = new OnsetsDS_struct();
		onsetsds_odf_types odftype = onsetsds_odf_types.ODS_ODF_COMPLEX;
		
		// Now initialise the OnsetsDS struct and its associated memory
		onsetsds_init(ods, onsetsds_fft_types.ODS_FFT_AUBIO_R2C, odftype, N, 11, 44100f);
		
		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.processing.Processor#process(ensemble.Parameters, java.lang.Object)
	 */
	@Override
	public Object process(Parameters arguments, Object in) {

		ArrayList<Double> res = new ArrayList<Double>();
		
		// Arguments
		double 	start_instant = Double.valueOf(arguments.get("start_instant", "0.0"));

		// Input vector
		double[] chunk = (double[])in;
		int numberOfFrames = (int)Math.floor((double)chunk.length / frame_size);
		
		double t = start_instant;
		int ptr = 0;
		while (ptr + frame_size < chunk.length) {
//			System.out.println("ptr = " + ptr);
		   	t = start_instant + ((double)ptr/44100);

			// Grab your 512-point, 50%-overlap, nicely-windowed FFT data, into "fftdata"
			double[] chunk_win = hann_window(chunk, ptr, frame_size);
			
			Parameters fft_args = new Parameters();
			fft_args.put("size", String.valueOf(N));
			double[] fftbuf = (double[])fftproc.process(fft_args, chunk_win);
			
			// Then detect. "onset" will be true when there's an onset, false otherwise
			boolean onset = onsetsds_process(ods, fftbuf);
			if (onset) {
//				ret[i] = true;
				switch (onset_output) {
				case SAMPLE:
					res.add((double)ptr);
					break;

				case TIME:	
					res.add(t);
//					System.out.println("t = " + t + " s - onset = " + onset);
					break;
				}
//				System.out.println("ptr = " + ptr);
			}

		   	ptr += (frame_size / 2);
		}
		
		double[] ret = new double[res.size()];
		for (int i = 0; i < res.size(); i++) {
			ret[i] = res.get(i);
		}
		
		return ret;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		
		return true;
		
	}

}
