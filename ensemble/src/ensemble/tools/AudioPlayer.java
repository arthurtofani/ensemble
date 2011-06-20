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

package ensemble.tools;

import java.util.LinkedList;
import java.util.Queue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Sensor;
import ensemble.audio.AudioEvent;


public class AudioPlayer {
	
	SourceDataLine	line 			= null;
	long 			currentChunk	= 1;
	long 			playedChunk		= 1;
	
	// TODO Utilizar uma estrutura de armazenar chunks de forma mais eficiente
	Queue<byte[]> queue = new LinkedList<byte[]>();

	public AudioPlayer() {
	}
	
	public void initAudioDevice() {
		AudioFormat format = new AudioFormat(44100f, 16, 1, true, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
		    // Handle the error.
		    }
		    // Obtain and open the line.
		try {
		    line = (SourceDataLine) AudioSystem.getLine(info);
		    line.open(format);
		    line.start();
		} catch (LineUnavailableException ex) {
		   	// Handle the error.
		}
	}
	
	public void stopAudioDevice() {
		line.stop();
	}
	
	public void playChunk(double[] chunk) {
		byte[] buffer = AudioTools.convertDoubleByte(chunk, 0, chunk.length);
		line.write(buffer, 0, buffer.length);
		currentChunk++;
	}

}
