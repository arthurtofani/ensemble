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

package ensemble.audio;

import ensemble.Event;

// TODO: Auto-generated Javadoc
/**
 * The Class AudioEvent.
 */
public class AudioEvent extends Event {
	
	// Tamanho e Formato do Chunk
	/** The sample rate. */
	public int		sampleRate;
	
	// 0 - none, 1 - ambisonics 1st order, 2 - ambisonics 2nd order
	/** The codification. */
	public int 		codification;
	
	/** The num channels. */
	public int 		numChannels;

	// Dados do Chunk (audio, MIDI etc.)
	/** The chunk. */
	public double[][] chunk;
	
	/**
	 * Instantiates a new audio event.
	 *
	 * @param sampleRate the sample rate
	 * @param chunkLenght the chunk lenght
	 * @param numChannels the num channels
	 */
	public AudioEvent(int sampleRate, int chunkLenght, int numChannels) {
		this.sampleRate 	= sampleRate;
		this.chunk 			= new double[numChannels][chunkLenght];
	}
	
}
