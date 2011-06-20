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

import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class AudioTools {

	private static final int N = 200;
	
	public static float[] calculateEnergy(float[] chunk) {
		
		float[] E0 = new float[chunk.length];
		
		for (int n = 0; n < chunk.length; n++) {
			E0[n] = 0.0f;
			for (int m = -N/2; m < N/2; m++) {
				// Calcula a janela de Hamming
//				float w = 0.54f - 0.46f * (float)Math.cos((2 * Math.PI * m) / (N - 1));
				float w = 1f;
				if ((n+m) >= 0 && (n+m) < chunk.length) {
					E0[n] = E0[n] + (float)(Math.pow(chunk[n + m], 2) * w);
				}
			}
			E0[n] = E0[n] / N;
			//E0[n] = (float) Math.log(E0[n]);
		}

		return E0;
		
	}
	
	public static float[] calculateDerivative(float[] chunk, float delta_t) {
		
		float[] der = new float[chunk.length];
		
		for (int i = 0; i < der.length; i++) {
			der[i] = 0.0f;
			for (int j = -N/2; j < N/2; j++) {
				if ((i+j) >= 0 && (i+j) < chunk.length - 1) {
//					der[i] = der[i] + (float)((chunk[i+j+1] - chunk[i+j]) / delta_t);
					der[i] = der[i] + (float)((chunk[i+j+1] - chunk[i+j]) / chunk[i+j]);
				}
			}
			der[i] = der[i] / N;
//				if (i+1 < der.length) {
//				der[i] = (float)((chunk[i+1] - chunk[i]) / delta_t);
//			} else {
//				der[i] = (float)(-chunk[i] / delta_t);
//			}
		}
		
		return der;
		
	}
	
	/**Encontra os poss�veis onset em um sinal de �udio
	 * @param chunk
	 * @param threshold
	 * @return samples do chunk em que foram localizados onsets
	 */
	public static int[] onsetDetection(float[] chunk, float threshold) {
		
		ArrayList<Integer> onsets = new ArrayList<Integer>();

		float[] E0 = calculateEnergy(chunk);
		float[] der = calculateDerivative(E0, 1.0f / 44100);
		
		boolean onset_found = false;
		for (int i = 0; i < chunk.length; i++) {
//			System.out.println("Sample " + i + " de valor " + chunk[i]);
			if (onset_found) {
				if (der[i] < threshold) {
					onset_found = false;
//					System.out.println("Sai do onset_found no sample " + i + " de valor " + chunk[i]);
				}
			} else {
				if (der[i] >= threshold) {
					onset_found = true;
					onsets.add(i);
//					System.out.println("Achei um onset no sample " + i + " de valor " + der[i]);
				}
			}
			
		}
		
		int[] ret = new int[onsets.size()];
		for (int i = 0; i < onsets.size(); i++) {
			ret[i] = onsets.get(i);
		}
		
		return ret;
		
	}
	
   public static byte[] convertDoubleByte(double[] buffer, int offset, int length) {
    	if (offset < 0 || offset >= buffer.length) {
    		return null;
    	}
    	byte[] ret = null;
    	if (length > buffer.length - offset) {
    		ret = new byte[(buffer.length - offset)*2];
    	} else {
    		ret = new byte[length*2];
    	}
		
		for (int ret_offset = 0; ret_offset < ret.length; ret_offset = ret_offset + 2) {
			double dSample = Math.min(1.0, Math.max(-1.0, buffer[offset + (ret_offset/2)])); // saturation
			int nSample = (int) Math.round(dSample * 32767.0); // scaling and conversion to integer
			ret[ret_offset+0] = (byte)(nSample & 0xFF); // low
			ret[ret_offset+1] = (byte)((nSample >> 8) & 0xFF); // high
		}
		return ret;
	}
	    
   public static double[] convertByteDouble(byte[] buffer, int offset, int length) {
    	// TODO Verificar quantos bytes por frame e o tamanho do buffer e o offset
    	if (offset < 0 || offset >= buffer.length) {
    		return null;
    	}
    	double[] ret = null;
    	if (length > buffer.length - offset) {
    		ret = new double[(buffer.length - offset)/2];
    	} else {
    		ret = new double[length/2];
    	}
		for (int ret_offset = 0; ret_offset < buffer.length; ret_offset = ret_offset + 2) {
        	ret[ret_offset/2] = ((buffer[offset + (ret_offset + 0)] & 0xFF) | (buffer[offset + (ret_offset + 1)] << 8)) / 32768.0;
		}
    	return ret;
   }
	    
	public static void main(String[] args) {
		
		// Arquivo de �udio
		AudioInputStream ais;
		int bytesPerFrame;
		float sampleRate = 0;
		int sampleSizeInBits;
		boolean bigEndian;
		AudioFormat.Encoding enc;
		
		// Buffers de trabalho
		float[] wavetable 	= null;
		
		// Abre o arquivo de �udio e carrega a wavetable
		try {
			File file = new File("cowbell_maior.wav");
			ais = AudioSystem.getAudioInputStream(file);
			bytesPerFrame = ais.getFormat().getFrameSize();
			sampleRate = ais.getFormat().getFrameRate();
			sampleSizeInBits = ais.getFormat().getSampleSizeInBits();
			bigEndian = ais.getFormat().isBigEndian();
			enc = ais.getFormat().getEncoding();
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				bytesPerFrame = 1;
			}
			wavetable = new float[(int)ais.getFrameLength()];
			byte[] buffer = new byte[(int)ais.getFrameLength() * bytesPerFrame];
			ais.read(buffer, 0, buffer.length);
			for (int offset = 0; offset < buffer.length; offset = offset + 2) {
				// Transforma de byte para float, 16 bits, little endian
				wavetable[offset/2] = ((buffer[offset + 0] & 0xFF) | (buffer[offset + 1] << 8)) / 32768.0F;
			}	
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		// Calcula os valores para cada chunk do wavetable
		
		int chunk_size = 4096;
//		int chunk_size = wavetable.length;
		for (int j = 0; j < wavetable.length; j=j+chunk_size) {
							
			System.out.println("Chunk " + j);
			
			float[] chunk = new float[chunk_size];
			for (int i = 0; i < chunk.length; i++) {
				if (j+i < wavetable.length) {
					chunk[i] = wavetable[j+i];
				} else {
					chunk[i] = 0.0f;
				}
			}
			
			int[] onsets = onsetDetection(chunk, 0.01f);
			
//			// Imprime o maior valor de energia nesse chunk
//			float max = Float.MIN_VALUE;
//			float instant = 0f;
//			for (int i = 0; i < der.length; i++) {
//				if (der[i] > max) {
//					max = der[i];
//					instant = (float)(i * (1.0 / 44100));
//				}
//			}
			
			for (int i = 0; i < onsets.length; i++) {
				System.out.println("\tAmostra " + onsets[i]);
				System.out.println("\tInstante = " + (float)(onsets[i] * (1.0 / 44100)));
				System.out.println("\tInstante global = " + (float)(((j + onsets[i]) * (1.0 / 44100))));
//				System.out.println("\tMAX = " + max);
			}
			
//			// Faz os gr�ficos
//			double[] x = new double[chunk.length];
//			double[] y = new double[chunk.length];
//			double time_per_sample = 1.0f / sampleRate;
//			for (int i = 0; i < x.length; i++) {
//				x[i] = i * time_per_sample;
//				y[i] = (double)chunk[i];
//			}
//			
//			PlotGraph graph = new PlotGraph(x, y);
//			graph.setLine(3);
//			graph.setPoint(0);
//			graph.plot();
//
//			y = new double[chunk.length];
//			for (int i = 0; i < x.length; i++) {
//				y[i] = (double)E0[i];
//			}
//			
//			PlotGraph graph2 = new PlotGraph(x, y);
//			graph2.setLine(3);
//			graph2.setPoint(0);
//			graph2.plot();
//			
//			y = new double[chunk.length];
//			for (int i = 0; i < x.length; i++) {
//				y[i] = (double)der[i];
//			}
//			
//			PlotGraph graph3 = new PlotGraph(x, y);
//			graph3.setLine(3);
//			graph3.setPoint(0);
//			graph3.plot();
		}
	
	}
	
}
