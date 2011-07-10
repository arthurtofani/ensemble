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

package ensemble.audio.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioInputFile {

	File file = null;
	AudioInputStream audioInputStream;
	
	boolean 	onLoop = false;
	boolean 	hasEnded = false;

	int 		bytesPerFrame;
	float 		sampleRate;
	int 		sampleSizeInBits;
	boolean 	isBigEndian;
	Encoding 	enc;

	private byte[]	buffer;
	
	private int chunk_counter = 0;
	
	public AudioInputFile(String filename,  boolean onLoop) throws FileNotFoundException {
	
		this.onLoop = onLoop;
		openFile(filename);
		
	}
	
	private void openFile(String filename) throws FileNotFoundException {
		
		// Abre o arquivo de áudio para leitura
		try {
			file = new File(filename);
			audioInputStream = AudioSystem.getAudioInputStream(file);
			bytesPerFrame = audioInputStream.getFormat().getFrameSize();
			sampleRate = audioInputStream.getFormat().getFrameRate();
			sampleSizeInBits = audioInputStream.getFormat().getSampleSizeInBits();
			isBigEndian = audioInputStream.getFormat().isBigEndian();
			enc = audioInputStream.getFormat().getEncoding();
			hasEnded=false;
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				bytesPerFrame = 1;
			} 
		} catch (Exception e) {
			throw (new FileNotFoundException());
		}
		
	}
	
	/**
	 * Returns the total number of samples in this file
	 * @return
	 */
	public long getNumberSamples() {
		return (audioInputStream.getFrameLength());
	}
	
	public float getSampleRate() {
		return sampleRate;
	}
	
	/**
	 * Reads a random segment of data from the audio file
	 * @param offset start sample of the segment
	 * @param chunkSize size of the segment
	 * @return an audio segment
	 */
	// TODO Como fazer isso de forma eficiente?
	public double[] readChunk(int offset, int chunkSize) {
		
//		audioInputStream.
		
		return null;
		
	}
	
	/**
	 * Reads the next segment of the audio file
	 * @param chunkSize the size of the segment
	 * @return an audio segment
	 */
	// TODO Se tiver algum problema na leitura do arquivo, emite um log e devolve um buffer vazio
	public double[] readNextChunk(int chunkSize) {
		
		double[] chunk = new double[chunkSize];

		if (file != null) {

			byte[] buffer = new byte[chunkSize * bytesPerFrame];

			// TODO Se loop estiver ligado, recome�ar a ler o arquivo
			int size = 0;
			try {
				size = audioInputStream.read(buffer, 0, buffer.length);
			} catch (Exception e) {
				e.printStackTrace();
			}

			int offset = 0;
			int counter = 0;
			while (counter < chunkSize) {
				
				// Se chegou ao fim do arquivo e o loop estiver ligado, voltar ao começo do arquivo
				if (offset > size) {
					if (onLoop) {
						try {
//							System.out.println("Recommencing file!");
							chunk_counter = 0;
//							audioInputStream.reset();
							try {
								audioInputStream = AudioSystem.getAudioInputStream(file);
							} catch (UnsupportedAudioFileException e) {
								e.printStackTrace();
							}
							buffer = new byte[(chunkSize-counter+1) * bytesPerFrame];
							size = audioInputStream.read(buffer, 0, buffer.length);
							offset = 0;
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(-1);
						}
					} else {
						//Acabou o arquivo
						hasEnded = true;
						break;
					}
				}

				float sample = 0.0f;
				switch (sampleSizeInBits) {
				
					case 8:	
						sample = buffer[offset] / 128.0F;
						offset++;
						break;
					
					case 16:	
						if (isBigEndian) {
							sample =
								(  (buffer[offset + 0] << 8)
								 | (buffer[offset + 1] & 0xFF) )
								/ 32768.0F;
						} else {
							sample =
								(  (buffer[offset + 0] & 0xFF)
								 | (buffer[offset + 1] << 8) )
								/ 32768.0F;
						}
						offset = offset + 2;
						break;
					
					case 24:	
						if (isBigEndian) {
							sample =
								(   (buffer[offset + 0] << 16)
								 | ((buffer[offset + 1] & 0xFF) << 8)
								 |  (buffer[offset + 2] & 0xFF) )
								/ 8388606.0F;
						} else {
							sample =
								(   (buffer[offset + 0] & 0xFF)
								 | ((buffer[offset + 1] & 0xFF) << 8)
								 |  (buffer[offset + 2] << 16) )
								/ 8388606.0F;
						}
						offset = offset + 3;
						break;
				
					case 32:	
						if (isBigEndian) {
							sample =
								(   (buffer[offset + 0] << 24)
								 | ((buffer[offset + 1] & 0xFF) << 16)
								 | ((buffer[offset + 2] & 0xFF) << 8)
								 |  (buffer[offset + 3] & 0xFF) )
								/ 2147483648.0F;
						} else {
							sample =
								(   (buffer[offset + 0] & 0xFF)
								 | ((buffer[offset + 1] & 0xFF) << 8)
								 | ((buffer[offset + 2] & 0xFF) << 16)
								 |  (buffer[offset + 3] << 24) )
								/ 2147483648.0F;
						}
						offset = offset + 4;
						break;
				
					default:
						break;
				}
				
				chunk[counter] = sample;
				counter++;
				
			}				
		
			chunk_counter++;
//			System.out.println("Chunk " + chunk_counter);

		}
		
		return chunk;
	}
	
}
