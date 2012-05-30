package ensemble.audio.dsp;

import ensemble.audio.dsp.jna.soundtouch.SoundTouchLibrary;

public class SoundTouchProcessing {
	public void test(){

		System.out.println(SoundTouchLibrary.soundtouch_getVersionId());
	}
}
