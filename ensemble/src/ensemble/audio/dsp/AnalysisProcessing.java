package ensemble.audio.dsp;

import java.util.Random;

public class AnalysisProcessing {

	private static final double VERY_SMALL_FLOAT = (float) 0.00001;

	public static double peakFollower(int samplerate, double[] input, int count) {

		// halfLife = time in seconds for output to decay to half value after an
		// impulse

		double halfLife = 0.1;
		double output = 0;

		double scalar = Math.pow(0.5, 1.0 / (halfLife * samplerate));

		for (int i = 0; i < count; i++) {
			if (input[i] < 0.0)
				input[i] = -input[i]; /* Absolute value. */

			if (input[i] >= output) {
				/* When we hit a peak, ride the peak to the top. */
				output = input[i];
			} else {
				/* Exponential decay of output when signal is low. */
				output = output * scalar;
				/*
				 * * When current gets close to 0.0, set current to 0.0 to
				 * prevent FP underflow* which can cause a severe performance
				 * degradation due to a flood* of interrupts.
				 */
				if (output < VERY_SMALL_FLOAT)
					output = 0.0;
			}
		}
		return output;
	}
	
	
	public static int pitchFollower(int samplerate, double[] input, int count) {
		
		Random rand = new Random();
		return rand.nextInt(4001);
		
	}
	/*
	 * 

VUMeterGFX

slider1:50<1,300,1>Response (MS)
slider2:5<1,10,0.1>Release (Slow/Fast)

@init
//st - sample time, sc - db scale, rp - right channel y pading, r - radius
sc = 6/log(2);
rp = 261;
r = 200;
yl = yr = ylt = yrt = 74;
xl = xr = 66;
ms  = slider1;
cs = 0;
suml = sumr = 0;
rms_i = 0;
i_max = 36;

@slider
rel = slider2;
ms = slider1;
st = ms*srate/1000;
hold = (0.001*ms*srate)*36;
cs = 0;
suml = sumr = 0;

@block
rmsl = floor(sc*log(sqrt(suml/cs))*100)/100;
rmsr = floor(sc*log(sqrt(sumr/cs))*100)/100;

rms_i == i_max ? (
  rmsl_gfx = rmsl;
  rmsr_gfx = rmsr;
  rms_i = 0;
);
rms_i += 1;

bscnt > st ? (

  ool = log(pvl)*sc;
  oor = log(pvr)*sc;
  
  //get x from exp scale
  xlt = floor(exp(log(1.055)*2.1*ool)*285);
  xrt = floor(exp(log(1.055)*2.1*oor)*285);  
  
  //get y from x and radius - r     
  l=sqrt(sqr(r)+sqr(212-xlt));
  h=((l-r)*r/l);
  m=sqrt(sqr(l-r)-sqr(h));
  ylt=35+h;
  xlt < 212 ? xlt=xlt+m : xlt=xlt-m;
  
  l=sqrt(sqr(r)+sqr(212-xrt));
  h=((l-r)*r/l);
  m=sqrt(sqr(l-r)-sqr(h));
  yrt=35+h;
  xrt < 212 ? xrt=xrt+m : xrt=xrt-m;
  
  //update x,y,out
  old_xl < xlt ? (xl = min(max(xlt,66),375); yl = ylt; olt = ool;);
  old_xr < xrt ? (xr = min(max(xrt,66),375); yr = yrt; ort = oor;);
  bscnt = pvl = pvr = 0;
);

//indicator fall-back
fallback = rel/2*samplesblock/1024;
fbi_l = exp(xl/512)*fallback;
fbi_r = exp(xr/512)*fallback;
xl > 66 ? xl -= fbi_l;
xr > 66 ? xr -= fbi_r;

old_xl = xl;
old_xr = xr;

bscnt += samplesblock;

//limit x
xl = min(max(xl,66),375);
xr = min(max(xr,66),375);

//get y after fall-back
yl=35;
l=sqrt(sqr(r)+sqr(212-xl));
h=((l-r)*r/l);
yl=floor(yl+h);

yr=35;
l=sqrt(sqr(r)+sqr(212-xr));
h=((l-r)*r/l);
yr=floor(yr+h);


@sample
pvl = max(pvl,abs(spl0));
pvr = max(pvr,abs(spl1));
cs == hold ? (
cs = 0;
suml = 0;
sumr = 0;
) : (
cs += 1;
suml += sqr(abs(spl0));
sumr += sqr(abs(spl1));
);

	 */
}
