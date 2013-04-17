package ensemble.audio.dsp;

import ensemble.audio.dsp.jna.soundtouch.SoundTouchLibrary;

// TODO: Auto-generated Javadoc
/**
 * The Class SoundTouchProcessing.
 */
public class SoundTouchProcessing {
	
	/**
	 * Test.
	 */
	public void test(){

		System.out.println(SoundTouchLibrary.soundtouch_getVersionId());
	}
}
