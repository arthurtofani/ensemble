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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.sound.sampled.SourceDataLine;

import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.MusicalAgent;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.tools.AudioTools;

//import jade.util.Logger;


public class AudioFileOutputReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// Memories
	private HashMap<String, Memory> earMemories = new HashMap<String, Memory>();
	private HashMap<String, Integer> numChannels = new HashMap<String, Integer>();

	// Files
	private HashMap<String, FileOutputStream> outFiles = new HashMap<String, FileOutputStream>();

	// Parameters
	int 	device;
	int 	channel;
	int 	maxChannels;
	
	@Override
	public boolean init() {
		
		return true;
		
	}

	public boolean finit() {

		Collection<FileOutputStream> files = outFiles.values();
		for (FileOutputStream file : files) {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
	
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {

			// Stores ear's memory
			Sensor ear = (Sensor)evtHdl;
			ear.registerListener(this);
			earMemories.put(ear.getComponentName(), getAgent().getKB().getMemory(ear.getComponentName()));
		
			// Assigns a channel in the audio interface to this ear
			if (ear.getParameters().containsKey("CHANNELS")) {
				int channels = Integer.valueOf(ear.getParameter("CHANNELS"));
				numChannels.put(ear.getComponentName(), channels);
				// Creates a file
				for (int n = 0; n < channels; n++) {
					try {
						FileOutputStream file = new FileOutputStream(getAgent().getAgentName()+"_"+n+"_out.dat");
						outFiles.put(ear.getComponentName()+"_"+n, file);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			} 
			else {
				try {
					FileOutputStream file = new FileOutputStream(getAgent().getAgentName()+"_out.dat");
					outFiles.put(ear.getComponentName(), file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}			
			
		}
			
	}

	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		String earName = sourceSensor.getComponentName();
		Memory earMemory = earMemories.get(earName);
		int channels = numChannels.get(earName);
		if (channels > 1) {
			double[][] buf = (double[][])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			for (int n = 0; n < channels; n++) {
//				System.out.println("Vou gravar chunk do canal " + n);
				FileOutputStream file = outFiles.get(earName+"_"+n);
				if (file != null) {
					try {
						file.write(AudioTools.convertDoubleByte(buf[n], 0, buf[n].length));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		else {
			double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			FileOutputStream file = outFiles.get(earName);
			if (file != null) {
				try {
					file.write(AudioTools.convertDoubleByte(buf, 0, buf.length));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
			
	}
	
	@Override
	public void processCommand(Command cmd) {
	}

}
