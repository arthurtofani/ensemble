package ensemble.audio.dsp;

import com.sun.jna.Library;

public interface AubioJNA extends Library {
    public void librarymethod(String whatToSay);
}


/*import com.sun.jna.Native;

public class AubioJNA {
	    public static void main(String args[]){
	        MySharedLibrary lib = (MySharedLibrary) Native.loadLibrary("testejna", MySharedLibrary.class);
	        lib.librarymethod("Java Rocks");
	    }
	
}*/
/* type aliases */
/*typedef unsigned int uint_t;
typedef int sint_t;
typedef float smpl_t;

 fvec 
fvec_t * new_fvec(uint_t length, uint_t channels);
void del_fvec(fvec_t *s);
smpl_t fvec_read_sample(fvec_t *s, uint_t channel, uint_t position);
void fvec_write_sample(fvec_t *s, smpl_t data, uint_t channel, uint_t position);
smpl_t * fvec_get_channel(fvec_t *s, uint_t channel);
void fvec_put_channel(fvec_t *s, smpl_t * data, uint_t channel);
smpl_t ** fvec_get_data(fvec_t *s);

 cvec 
cvec_t * new_cvec(uint_t length, uint_t channels);
void del_cvec(cvec_t *s);
void cvec_write_norm(cvec_t *s, smpl_t data, uint_t channel, uint_t position);
void cvec_write_phas(cvec_t *s, smpl_t data, uint_t channel, uint_t position);
smpl_t cvec_read_norm(cvec_t *s, uint_t channel, uint_t position);
smpl_t cvec_read_phas(cvec_t *s, uint_t channel, uint_t position);
void cvec_put_norm_channel(cvec_t *s, smpl_t * data, uint_t channel);
void cvec_put_phas_channel(cvec_t *s, smpl_t * data, uint_t channel);
smpl_t * cvec_get_norm_channel(cvec_t *s, uint_t channel);
smpl_t * cvec_get_phas_channel(cvec_t *s, uint_t channel);
smpl_t ** cvec_get_norm(cvec_t *s);
smpl_t ** cvec_get_phas(cvec_t *s);


 fft 
aubio_fft_t * new_aubio_fft(uint_t size, uint_t channels);
void del_aubio_fft(aubio_fft_t * s);
void aubio_fft_do (aubio_fft_t *s, fvec_t * input, cvec_t * spectrum);
void aubio_fft_rdo (aubio_fft_t *s, cvec_t * spectrum, fvec_t * output);
void aubio_fft_do_complex (aubio_fft_t *s, fvec_t * input, fvec_t * compspec);
void aubio_fft_rdo_complex (aubio_fft_t *s, fvec_t * compspec, fvec_t * output);
void aubio_fft_get_spectrum(fvec_t * compspec, cvec_t * spectrum);
void aubio_fft_get_realimag(cvec_t * spectrum, fvec_t * compspec);
void aubio_fft_get_phas(fvec_t * compspec, cvec_t * spectrum);
void aubio_fft_get_imag(cvec_t * spectrum, fvec_t * compspec);
void aubio_fft_get_norm(fvec_t * compspec, cvec_t * spectrum);
void aubio_fft_get_real(cvec_t * spectrum, fvec_t * compspec);

 filter 
aubio_filter_t * new_aubio_filter(uint_t order, uint_t channels);
void aubio_filter_do(aubio_filter_t * b, fvec_t * in);
void aubio_filter_do_outplace(aubio_filter_t * b, fvec_t * in, fvec_t * out);
void aubio_filter_do_filtfilt(aubio_filter_t * b, fvec_t * in, fvec_t * tmp);
void del_aubio_filter(aubio_filter_t * b);

 a_weighting 
aubio_filter_t * new_aubio_filter_a_weighting (uint_t channels, uint_t samplerate);
uint_t aubio_filter_set_a_weighting (aubio_filter_t * b, uint_t samplerate);

 c_weighting 
aubio_filter_t * new_aubio_filter_c_weighting (uint_t channels, uint_t samplerate);
uint_t aubio_filter_set_c_weighting (aubio_filter_t * b, uint_t samplerate);

 biquad 
aubio_filter_t * new_aubio_filter_biquad(lsmp_t b1, lsmp_t b2, lsmp_t b3, lsmp_t a2, lsmp_t a3, uint_t channels);
uint_t aubio_filter_set_biquad (aubio_filter_t * b, lsmp_t b1, lsmp_t b2, lsmp_t b3, lsmp_t a2, lsmp_t a3);

 mathutils 
fvec_t * new_aubio_window(char * wintype, uint_t size);
smpl_t aubio_unwrap2pi (smpl_t phase);
smpl_t aubio_bintomidi(smpl_t bin, smpl_t samplerate, smpl_t fftsize);
smpl_t aubio_miditobin(smpl_t midi, smpl_t samplerate, smpl_t fftsize);
smpl_t aubio_bintofreq(smpl_t bin, smpl_t samplerate, smpl_t fftsize);
smpl_t aubio_freqtobin(smpl_t freq, smpl_t samplerate, smpl_t fftsize);
smpl_t aubio_freqtomidi(smpl_t freq);
smpl_t aubio_miditofreq(smpl_t midi);
uint_t aubio_silence_detection(fvec_t * ibuf, smpl_t threshold);
smpl_t aubio_level_detection(fvec_t * ibuf, smpl_t threshold);
smpl_t aubio_zero_crossing_rate(fvec_t * input);

 mfcc 
aubio_mfcc_t * new_aubio_mfcc (uint_t win_s, uint_t samplerate, uint_t n_filters, uint_t n_coefs);
void del_aubio_mfcc(aubio_mfcc_t *mf);
void aubio_mfcc_do(aubio_mfcc_t *mf, cvec_t *in, fvec_t *out);

 pvoc 
aubio_pvoc_t * new_aubio_pvoc (uint_t win_s, uint_t hop_s, uint_t channels);
void del_aubio_pvoc(aubio_pvoc_t *pv);
void aubio_pvoc_do(aubio_pvoc_t *pv, fvec_t *in, cvec_t * fftgrain);
void aubio_pvoc_rdo(aubio_pvoc_t *pv, cvec_t * fftgrain, fvec_t *out);

 pitch detection 
aubio_pitch_t *new_aubio_pitch (char *pitch_mode,
    uint_t bufsize, uint_t hopsize, uint_t channels, uint_t samplerate);
void aubio_pitch_do (aubio_pitch_t * p, fvec_t * ibuf, fvec_t * obuf);
uint_t aubio_pitch_set_tolerance(aubio_pitch_t *p, smpl_t thres);
uint_t aubio_pitch_set_unit(aubio_pitch_t *p, char * pitch_unit);
void del_aubio_pitch(aubio_pitch_t * p);

 tempo 
aubio_tempo_t * new_aubio_tempo (char_t * mode,
    uint_t buf_size, uint_t hop_size, uint_t channels, uint_t samplerate);
void aubio_tempo_do (aubio_tempo_t *o, fvec_t * input, fvec_t * tempo);
uint_t aubio_tempo_set_silence(aubio_tempo_t * o, smpl_t silence);
uint_t aubio_tempo_set_threshold(aubio_tempo_t * o, smpl_t threshold);
smpl_t aubio_tempo_get_bpm(aubio_tempo_t * bt);
smpl_t aubio_tempo_get_confidence(aubio_tempo_t * bt);
void del_aubio_tempo(aubio_tempo_t * o);*/


package aubio.tests;

import aubio.SWIGTYPE_p_aubio_biquad_t;
import aubio.SWIGTYPE_p_aubio_mfft_t;
import aubio.SWIGTYPE_p_cvec_t;
import aubio.SWIGTYPE_p_fvec_t;
import aubio.aubiowrapper;

public class Test {
	
    public static void main(String[] args) {
  
    	int N = 2048;
    	double fs = 44100;
		double step = 1/fs;
		double duration = 0.1;  

    	try {
    		System.loadLibrary("mmsaubio");
    	} catch (UnsatisfiedLinkError e) {
    		System.out.println("Failed to load the library \"mmsaubio\"");
    		System.out.println(e.toString());
    	} 

    	SWIGTYPE_p_aubio_mfft_t mfft_t = aubiowrapper.new_aubio_mfft(N, 1);
    
		// Generates a sine wave
		double[] buffer = new double[(int)(fs * duration)];
		double t = 0;
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = 0.8 * Math.sin(2 * Math.PI * 440 * t);
			t = t + step;
		}

		// Creates the input vector
		System.out.println("Input vector");
		SWIGTYPE_p_fvec_t in = aubiowrapper.new_fvec((int)(fs * duration), 1);
		for (int i = 0; i < (int)(fs * duration); i++) {
			aubiowrapper.fvec_write_sample(in, (float)buffer[i], 0, i);
//			System.out.printf("%.2f\n", chunk[i]);
		}
      
		float res = aubiowrapper.vec_median(in);
		System.out.println(res);
		
		SWIGTYPE_p_aubio_biquad_t biquad = aubiowrapper.new_aubio_biquad(1, 0, -1, 0.1, 0.9);
		aubiowrapper.aubio_biquad_do(biquad, in);
		System.out.println("Biquad Filter Results");
		for (int i = 0; i < (int)(fs * duration); i++) {
			System.out.printf("%.2f\n", aubiowrapper.fvec_read_sample(in, 0, i));
		}

//		// Creates the input vector
//		System.out.println("Input vector");
//		SWIGTYPE_p_fvec_t in = aubiowrapper.new_fvec(N, 1);
//		for (int i = 0; i < N; i++) {
//			aubiowrapper.fvec_write_sample(in, (float)buffer[i], 0, i);
////			System.out.printf("%.2f\n", chunk[i]);
//		}
//      
//		// Creates the output vector
//		SWIGTYPE_p_cvec_t out = aubiowrapper.new_cvec(N, 1);
//      
//		// FFT
//		aubiowrapper.aubio_mfft_do(mfft_t, in, out);
//      
//		// Results
//		System.out.println("FFT Results");
//		for (int i = 0; i < N/2+1; i++) {
//			float norm = aubiowrapper.cvec_read_norm(out, 0, i);
//			float phas = aubiowrapper.cvec_read_phas(out, 0, i);
//			double mag = Math.sqrt((norm * norm) + (phas * phas));
////			System.out.printf("%.2f Hz \t %.2f \t %.2f\n", i*Fs/N, norm, phas);
//			System.out.printf("%.2f Hz \t %.2f\n", i*fs/N, mag);
//		}
//    
//		// Creates the seconde output vector
//		SWIGTYPE_p_fvec_t out2 = aubiowrapper.new_fvec(N, 1);
//		
//		aubiowrapper.aubio_mfft_rdo(mfft_t, out, out2);
		
//		// Results
//		System.out.println("IFFT Results");
//		for (int i = 0; i < N; i++) {
//			System.out.printf("%.2f\n", aubiowrapper.fvec_read_sample(out2, 0, i));
//		}

    }
	
}
