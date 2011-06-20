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

import java.util.HashMap;

import jade.util.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.MusicalAgent;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.tools.AudioTools;


public class JavaOutputReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// Memories
	private HashMap<String, Memory> earMemories = new HashMap<String, Memory>();

	// Channel mapping
	private HashMap<String, Integer> earChannels = new HashMap<String, Integer>();
	
	// JavaSound
	private AudioFormat 					format;
	private DataLine.Info 					info;
	private HashMap<String, SourceDataLine> lines = new HashMap<String, SourceDataLine>();
	
	@Override
	public boolean init() {
		
		// Initializes the Audio System
		// TODO Parametrizar os valores!!!
		format = new AudioFormat(44100f, 16, 1, true, false);
		info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
//			logger.severe("[" + getComponentName() + "] " + "Line not supported");
			return false;
		}
		
		return true;
		
	}

	@Override
	public boolean finit() {

		for (SourceDataLine line : lines.values()) {
			line.stop();
			line.close();
		}

		return true;
		
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {

		if (evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {

			// Stores ear's memory
			Sensor ear = (Sensor)evtHdl;
			ear.registerListener(this);
			earMemories.put(ear.getComponentName(), getAgent().getKB().getMemory(ear.getComponentName()));
		
			// Assigns a channel in the audio interface to this ear
			int channel = 0;
			if (ear.getParameters().containsKey("channel")) {
//				System.out.println("CHANNEL parameter detected in ear = " + ear.getParameter("channel"));
				String channel_param = ear.getParameter("channel");
				if (channel_param.equals("LEFT")) {
					channel = 0;
				} else if (channel_param.equals("RIGHT")) {
					channel = 1;
				}
				earChannels.put(ear.getComponentName(), channel);
			} else {
				// Gets the next available channel
				// TODO O QUE FAZER AQUI? TEMOS QUE TER UMA LISTA dos canais já utilizados!
				System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] No channel parameters!");
			}

			// Obtain and open the line.
			try {
			    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
			    line.open(format);
				// Channel control
				if (line.isControlSupported(FloatControl.Type.PAN)) {
		            FloatControl pan = (FloatControl) line.getControl(FloatControl.Type.PAN);
		            if (channel == 0) {
		                pan.setValue(-1.0f);
		            } else if (channel == 1) { 
		                pan.setValue(1.0f);
		            } else {
		            	pan.setValue(0.0f);
		            }
				}
			    line.start();
			    // Stores the line
			    lines.put(ear.getComponentName(), line);
			} catch (LineUnavailableException ex) {
//				logger.severe("[" + getComponentName() + "] " + "Line Unavailable");
			}
			
		}
		
	}

	// TODO Não pode tocar imediatamente, tem que respeitar o tempo, senão não podemos sincronazar com outros agentes e com outros acontecimentos no ambiente
	// TODO O JavaSound consegue fazer isso?!?!?!? No lo creo!
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		if (sourceSensor.getEventType().equals("AUDIO")) {
			
			String earName = sourceSensor.getComponentName();
			Memory earMemory = earMemories.get(earName);
			SourceDataLine line = lines.get(earName);
			if (line != null) {
				double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);
		
				byte[] buffer = AudioTools.convertDoubleByte(buf, 0, buf.length);
				line.write(buffer, 0, buffer.length);
	
	//			logger.info("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Inseri chunk " + instant + " na fila para tocar");
			}
			
		}

	}

}
