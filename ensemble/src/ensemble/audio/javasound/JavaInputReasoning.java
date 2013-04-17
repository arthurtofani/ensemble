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

package ensemble.audio.javasound;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Constants.EA_STATE;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.tools.AudioTools;


// TODO: Auto-generated Javadoc
/**
 * The Class JavaInputReasoning.
 */
public class JavaInputReasoning extends Reasoning {

	/** Actuator used to send audio events. */
	Actuator 	mouth;
	
	/** Actuator memory. */
	Memory 		mouthMemory;
	
	/** Number of samples in a frame. */
	int chunk_size;
	
	/** The period. */
	double period;

	/** Mic line. */
	TargetDataLine 		targetDataLine;
	
	/** The queue. */
	ArrayList<double[]> queue = new ArrayList<double[]>();
	
	/** The buffer. */
	byte[] 				buffer;
	
	/**
	 * Init the Mic Line.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean init() {
		AudioFormat audioFormat = new AudioFormat(44100f, 16, 1, true, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		if (!AudioSystem.isLineSupported(info)) {
//			getAgent().logger.severe("[" + getComponentName() + "] " + "Line not supported");
			return false;
		}
		try
		{
			targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
			targetDataLine.open(audioFormat);
			System.out.println("TargetDataLine initialized!");
		}
		catch (LineUnavailableException e)
		{
//			getAgent().logger.severe("[" + getComponentName() + "] " + "Line not available");
			return false;
		}
		return true;
	}
	
	/**
	 * Finalizes the Mic Line.
	 *
	 * @return true, if successful
	 */
	@Override
	public boolean finit() {
		targetDataLine.stop();
		targetDataLine.close();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {

		// Checar se é um atuador de som e adicionar na lista
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			mouthMemory = getAgent().getKB().getMemory(mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			period = Double.valueOf(mouth.getParameter("PERIOD"))/1000.0;
			buffer = new byte[chunk_size*2];
		}

	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) {
		System.out.println("[" + (long)getAgent().getClock().getCurrentTime(TimeUnit.MILLISECONDS) + "] instant = " + instant + " queue size = " + queue.size());
		if (!queue.isEmpty()) {
			double[] chunk = queue.remove(0); 
			try {
				mouthMemory.writeMemory(chunk, instant + (2*period), duration, TimeUnit.SECONDS);
				mouth.act();
			} catch (MemoryException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("ERROR: Queue is empty!");
		}
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	@Override
	public void process() throws Exception {
		if (getState() == EA_STATE.INITIALIZED && targetDataLine.isRunning() && buffer != null) {
			int frames = targetDataLine.read(buffer, 0, buffer.length);
			double[] samples = AudioTools.convertByteDouble(buffer, 0, buffer.length);
//			System.out.println("samples = " + samples.length);
			queue.add(samples);
		}
	}
	
}
