package ensemble.audio.dsp;

import java.util.Arrays;

import be.hogent.tarsos.dsp.util.FFT;

// TODO: Auto-generated Javadoc
/**
 * The Class SmbtPitchProcessing.
 */
public class SmbtPitchProcessing {



/****************************************************************************
*
* NAME: smbPitchShift.cpp
* VERSION: 1.2
* HOME URL: http://www.dspdimension.com
* KNOWN BUGS: none
*
* SYNOPSIS: Routine for doing pitch shifting while maintaining
* duration using the Short Time Fourier Transform.
*
* DESCRIPTION: The routine takes a pitchShift factor value which is between 0.5
* (one octave down) and 2. (one octave up). A value of exactly 1 does not change
* the pitch. numSampsToProcess tells the routine how many samples in indata[0...
* numSampsToProcess-1] should be pitch shifted and moved to outdata[0 ...
* numSampsToProcess-1]. The two buffers can be identical (ie. it can process the
* data in-place). fftFrameSize defines the FFT frame size used for the
* processing. Typical values are 1024, 2048 and 4096. It may be any value <=
* MAX_FRAME_LENGTH but it MUST be a power of 2. osamp is the STFT
* oversampling factor which also determines the overlap between adjacent STFT
* frames. It should at least be 4 for moderate scaling ratios. A value of 32 is
* recommended for best quality. sampleRate takes the sample rate for the signal 
* in unit Hz, ie. 44100 for 44.1 kHz audio. The data passed to the routine in 
* indata[] should be in the range [-1.0, 1.0), which is also the output range 
* for the data, make sure you scale the data accordingly (for 16bit signed integers
* you would have to divide (and multiply) by 32768). 
*
* COPYRIGHT 1999-2009 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
*
* 						The Wide Open License (WOL)
*
* Permission to use, copy, modify, distribute and sell this software and its
* documentation for any purpose is hereby granted without fee, provided that
* the above copyright notice and this license appear in all source copies. 
* THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY OF
* ANY KIND. See http://www.dspguru.com/wol.htm for more information.
*
*****************************************************************************/ 


	private double M_PI = Math.PI;
	
	/** The max frame length. */
	private static int  MAX_FRAME_LENGTH = 16384;//8192;



// -----------------------------------------------------------------------------------------------------------------
	 /**
 * Smb pitch shift.
 *
 * @param pitchShift the pitch shift
 * @param numSampsToProcess the num samps to process
 * @param fftFrameSize the fft frame size
 * @param osamp the osamp
 * @param sampleRate the sample rate
 * @param indata the indata
 * @param outdata the outdata
 */
public void smbPitchShift(float pitchShift, int numSampsToProcess, int fftFrameSize, int osamp, int sampleRate, double[] indata, double[] outdata)
	 {
		float[] auxIn = new float[indata.length];
		float[] auxOut = new float[outdata.length];

		for (int i = 0; i < indata.length; i++) {
			auxIn[i] = (float) indata[i];
		}

		smbPitchShift((float)pitchShift,  (long) numSampsToProcess, (long) fftFrameSize, (long) osamp, (float)sampleRate, auxIn, auxOut);
	 
		 for(int i = 0; i<outdata.length; i++){
			 
			 outdata[i] = (double) auxOut[i];
		 }
	 }

 /**
  * Smb pitch shift.
  *
  * @param pitchShift the pitch shift
  * @param numSampsToProcess the num samps to process
  * @param fftFrameSize the fft frame size
  * @param osamp the osamp
  * @param sampleRate the sample rate
  * @param indata the indata
  * @param outdata the outdata
  */
 public void smbPitchShift(float pitchShift, long numSampsToProcess, long fftFrameSize, long osamp, float sampleRate, float[] indata, float[] outdata)
{
/*
	Routine smbPitchShift(). See top of file for explanation
	Purpose: doing pitch shifting while maintaining duration using the Short
	Time Fourier Transform.
	Author: (c)1999-2009 Stephan M. Bernsee <smb [AT] dspdimension [DOT] com>
*/


	float[] gInFIFO = new float[MAX_FRAME_LENGTH];
	float[] gOutFIFO = new float[MAX_FRAME_LENGTH];
	float[] gFFTworksp = new float[2*MAX_FRAME_LENGTH];
	float[] gLastPhase = new float[MAX_FRAME_LENGTH/2+1];
	float[] gSumPhase = new float[MAX_FRAME_LENGTH/2+1];
	float[] gOutputAccum = new float[2*MAX_FRAME_LENGTH];
	float[] gAnaFreq = new float[MAX_FRAME_LENGTH];
	float[] gAnaMagn = new float[MAX_FRAME_LENGTH];
	float[] gSynFreq = new float[MAX_FRAME_LENGTH];
	float[] gSynMagn = new float[MAX_FRAME_LENGTH];
	int gRover = 0;
	int gInit = 0;
	double magn;
	double phase;
	double tmp;
	double window;
	double real;
	double imag;
	double freqPerBin;
	double expct;
	
	/*long i;
	long k;*/
	long qpd;
	long index;
	long inFifoLatency;
	long stepSize;
	long fftFrameSize2;
	
	/* set up some handy variables */
	fftFrameSize2 = fftFrameSize/2;
	stepSize = fftFrameSize/osamp;
	freqPerBin = sampleRate/(double)fftFrameSize;
	expct = 2.*M_PI*(double)stepSize/(double)fftFrameSize;
	inFifoLatency = fftFrameSize-stepSize;
	if (gRover == 0) gRover = (int) inFifoLatency;

	/* initialize our static arrays */
	if (gInit == 0) {
		gInFIFO =  new float[MAX_FRAME_LENGTH];
		gOutFIFO =  new float[MAX_FRAME_LENGTH];
		gLastPhase =  new float[MAX_FRAME_LENGTH/2+1];
		gSumPhase =  new float[MAX_FRAME_LENGTH/2+1];
		gAnaFreq =  new float[MAX_FRAME_LENGTH];
		gAnaMagn =  new float[MAX_FRAME_LENGTH];
		
		gInit = 1;
	}

	/* main processing loop */
	for (int i = 0; i < numSampsToProcess; i++){

		/* As long as we have not yet collected enough data just read in */
		gInFIFO[gRover] = indata[i];
		outdata[i] = gOutFIFO[(int) (gRover-inFifoLatency)];
		gRover++;

		/* now we have enough data for processing */
		if (gRover >= fftFrameSize) {
			gRover = (int) inFifoLatency;

			/* do windowing and re,im interleave */
			for (int j = 0; j < fftFrameSize;j++) {
				window = -.5*Math.cos(2.*M_PI*(double)j/(double)fftFrameSize)+.5;
				gFFTworksp[2*j] = (float) (gInFIFO[j] * window);
				gFFTworksp[2*j+1] = 0;
			}


			/* ***************** ANALYSIS ******************* */
			/* do transform */
			FFT fft = new FFT((int) fftFrameSize);
			fft.forwardTransform(gFFTworksp);
			//smbFft(gFFTworksp, fftFrameSize, -1);

			/* this is the analysis step */
			for (int k = 0; k <= fftFrameSize2; k++) {

				/* de-interlace FFT buffer */
				real = gFFTworksp[2*k];
				imag = gFFTworksp[2*k+1];

				/* compute magnitude and phase */
				magn = 2.*Math.sqrt(real*real + imag*imag);
				phase = Math.atan2(imag,real);

				/* compute phase difference */
				tmp = phase - gLastPhase[k];
				gLastPhase[k] = (float) phase;

				/* subtract expected phase difference */
				tmp -= (double)k*expct;

				/* map delta phase into +/- Pi interval */
				qpd = (long) (tmp/M_PI);
				if (qpd >= 0) qpd += qpd&1;
				else qpd -= qpd&1;
				tmp -= M_PI*(double)qpd;

				/* get deviation from bin frequency from the +/- Pi interval */
				tmp = osamp*tmp/(2.*M_PI);

				/* compute the k-th partials' true frequency */
				tmp = (double)k*freqPerBin + tmp*freqPerBin;

				/* store magnitude and true frequency in analysis arrays */
				gAnaMagn[k] = (float) magn;
				gAnaFreq[k] = (float) tmp;

			}

			/* ***************** PROCESSING ******************* */
			/* this does the actual pitch shifting */
			gSynMagn = new float[(int) fftFrameSize];
			gSynFreq = new float[(int) fftFrameSize];
			
			for (int k = 0; k <= fftFrameSize2; k++) { 
				index = (long) (k*pitchShift);
				if (index <= fftFrameSize2) { 
					gSynMagn[(int) index] += gAnaMagn[k]; 
					gSynFreq[(int) index] = gAnaFreq[k] * pitchShift; 
				} 
			}
			
			/* ***************** SYNTHESIS ******************* */
			/* this is the synthesis step */
			for (int k = 0; k <= fftFrameSize2; k++) {

				/* get magnitude and true frequency from synthesis arrays */
				magn = gSynMagn[k];
				tmp = gSynFreq[k];

				/* subtract bin mid frequency */
				tmp -= (double)k*freqPerBin;

				/* get bin deviation from freq deviation */
				tmp /= freqPerBin;

				/* take osamp into account */
				tmp = 2.*M_PI*tmp/osamp;

				/* add the overlap phase advance back in */
				tmp += (double)k*expct;

				/* accumulate delta phase to get bin phase */
				gSumPhase[k] += tmp;
				phase = gSumPhase[k];

				/* get real and imag part and re-interleave */
				gFFTworksp[2*k] = (float) (magn*Math.cos(phase));
				gFFTworksp[2*k+1] = (float) (magn*Math.sin(phase));
			} 

			/* zero negative frequencies */
			for (int w = (int) (fftFrameSize+2); w < 2*fftFrameSize; w++) gFFTworksp[w] = 0;

			/* do inverse transform */
			//smbFft(gFFTworksp, fftFrameSize, 1);
			fft.backwardsTransform(gFFTworksp);
			/* do windowing and add to output accumulator */ 
			for(int k2=0; k2 < fftFrameSize; k2++) {
				window = -.5*Math.cos(2.*M_PI*(double)k2/(double)fftFrameSize)+.5;
				gOutputAccum[k2] += 2.*window*gFFTworksp[2*k2]/(fftFrameSize2*osamp);
			}
			
			for (int k3 = 0; k3 < stepSize; k3++) gOutFIFO[k3] = gOutputAccum[k3];

			/* shift accumulator */
			//memmove(gOutputAccum, gOutputAccum+stepSize, fftFrameSize*sizeof(float));
			gOutputAccum = Arrays.copyOfRange(gOutputAccum, (int)stepSize, (int)(stepSize+fftFrameSize));
			/* move input FIFO */
			for (int y = 0; y < inFifoLatency; y++) gInFIFO[y] = gInFIFO[(int) (y+stepSize)];
		}
	}
}

// -----------------------------------------------------------------------------------------------------------------

/*
void smbFft(float[] fftBuffer, long fftFrameSize, long sign)
 
	FFT routine, (C)1996 S.M.Bernsee. Sign = -1 is FFT, 1 is iFFT (inverse)
	Fills fftBuffer[0...2*fftFrameSize-1] with the Fourier transform of the
	time domain data in fftBuffer[0...2*fftFrameSize-1]. The FFT array takes
	and returns the cosine and sine parts in an interleaved manner, ie.
	fftBuffer[0] = cosPart[0], fftBuffer[1] = sinPart[0], asf. fftFrameSize
	must be a power of 2. It expects a complex input signal (see footnote 2),
	ie. when working with 'common' audio signals our input signal has to be
	passed as {in[0],0.,in[1],0.,in[2],0.,...} asf. In that case, the transform
	of the frequencies of interest is in fftBuffer[0...fftFrameSize].

{
	float wr, wi, arg, temp;
	float[] p1, p2;
	float tr, ti, ur, ui;
	float[] p1r, p1i, p2r, p2i;
	long i, bitm, j, le, le2, k;

	for (i = 2; i < 2*fftFrameSize-2; i += 2) {
		for (bitm = 2, j = 0; bitm < 2*fftFrameSize; bitm <<= 1) {
			if (i == bitm) j++;
			j <<= 1;
		}
		if (i < j) {
			p1 = fftBuffer[i]; 
			p2 = fftBuffer[j];
			temp = p1; 
			*(p1++) = *p2;
			*(p2++) = temp; temp = *p1;
			*p1 = *p2; *p2 = temp;
		}
	}
	for (k = 0, le = 2; k < (long)(Math.log(fftFrameSize)/Math.log(2.)+.5); k++) {
		le <<= 1;
		le2 = le>>1;
		ur = (float) 1.0;
		ui = (float) 0.0;
		arg = (float) (M_PI / (le2>>1));
		wr = (float) Math.cos(arg);
		wi = (float) (sign*Math.sin(arg));
		for (j = 0; j < le2; j += 2) {
			p1r = fftBuffer+j; 
			p1i = p1r+1;
			p2r = p1r+le2; p2i = p2r+1;
			for (i = j; i < 2*fftFrameSize; i += le) {
				tr = *p2r * ur - *p2i * ui;
				ti = *p2r * ui + *p2i * ur;
				*p2r = *p1r - tr; *p2i = *p1i - ti;
				*p1r += tr; *p1i += ti;
				p1r += le; p1i += le;
				p2r += le; p2i += le;
			}
			tr = ur*wr - ui*wi;
			ui = ur*wi + ui*wr;
			ur = tr;
		}
	}
}*/


// -----------------------------------------------------------------------------------------------------------------

/*

    12/12/02, smb
    
    PLEASE NOTE:
    
    There have been some reports on domain errors when the atan2() function was used
    as in the above code. Usually, a domain error should not interrupt the program flow
    (maybe except in Debug mode) but rather be handled "silently" and a global variable
    should be set according to this error. However, on some occasions people ran into
    this kind of scenario, so a replacement atan2() function is provided here.
    
    If you are experiencing domain errors and your program stops, simply replace all
    instances of atan2() with calls to the smbAtan2() function below.
    
*/


/**
 * Smb atan2.
 *
 * @param x the x
 * @param y the y
 * @return the double
 */
private double smbAtan2(double x, double y)
{
  double signx;
  if (x > 0.) signx = 1.;  
  else signx = -1.;
  
  if (x == 0.) return 0.;
  if (y == 0.) return signx * M_PI / 2.;
  
  return Math.atan2(x, y);
}


}