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

package ensemble.audio.jack;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Hashtable;

import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;

import jade.util.Logger;
import jjack.JackCallback;
import jjack.JackPortFlags;
import jjack.SWIGTYPE_p_jack_client_t;
import jjack.SWIGTYPE_p_jack_port_t;
import jjack.jjack;
import jjack.jjackConstants;


// TODO: Auto-generated Javadoc
/**
 * The Class JACKOutputReasoning.
 */
public class JACKOutputReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// JACK
	/** The client_name. */
	String 						client_name;
	
	/** The client. */
	long					 	client;
	
	/** The callback start time. */
	double 						callbackStartTime;
	
	/** The step. */
	double 						step = 1/44100.0;
	
	/** The mapping. */
	Hashtable<String,String> mapping = new Hashtable<String, String>();
	
	/** The ports. */
	Hashtable<String, Long> ports = new Hashtable<String,Long>(2);
	
	// Sensor
	/** The ear memories. */
	Hashtable<String,Memory> earMemories = new Hashtable<String, Memory>(2);

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {
		
		String[] str = getParameter("mapping", "").split(";");
		for (int i = 0; i < str.length; i++) {
			String[] str2 = str[i].split("-");
			if (str2.length == 1) {
				mapping.put(str2[0], "");
			}
			else if (str2.length == 2) {
				mapping.put(str2[0], str2[1]);
			} 
			else {
				System.err.println("[" + this.getAgent().getAgentName() + ":" + getComponentName()+ "] " + "no mapping in parameters!");
			}
		}
		
		// JACK
		client_name = Constants.FRAMEWORK_NAME+"_"+getAgent().getAgentName()+"_"+getComponentName();
		client = jjack.jack_client_open(client_name, new Process());
		if (client == 0) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] JACK server not running... JACK will not be available!");
            return false;
		}
		// Activates the JACK client
		if (jjack.jack_activate(client) != 0) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot activate JACK client... JACK will not be available!");
			return false;
		}

		return true;
		
	}

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#finit()
	 */
	@Override
	public boolean finit() {

		jjack.jack_client_close(client);
		
		return true;
		
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			Sensor ear = (Sensor)evtHdl;
			String sensorName = ear.getComponentName();
			if (mapping.containsKey(sensorName)) {
				earMemories.put(ear.getComponentName(), getAgent().getKB().getMemory(ear.getComponentName()));
				// Creats a JACK client
				ports.put(sensorName, jjack.jack_port_register(client, 
											sensorName,
											jjackConstants.JACK_DEFAULT_AUDIO_TYPE, 
											JackPortFlags.JackPortIsOutput));
				// If specified, connects the port
				String connectPort = mapping.get(sensorName);
				if (connectPort != null && !connectPort.equals("")) {
					// Searches the desired playback port
					String[] playback_ports = jjack.jack_get_ports(client, connectPort, null, JackPortFlags.JackPortIsInput);
					if (playback_ports == null) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot find any physical playback ports");
						return;
					}
					if (playback_ports.length > 1) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] More than one port with that name");
//						return;
					}
					// Connects the port
					if (jjack.jack_connect(client, client_name+":"+sensorName, playback_ports[0]) != 0) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot connect playback ports");
						return;
					}
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerDeregistered(ensemble.EventHandler)
	 */
	@Override
	protected void eventHandlerDeregistered(EventHandler evtHdl) {
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			String sensorName = evtHdl.getComponentName();
			if (ports.containsKey(sensorName)) {
				ports.remove(sensorName);
				jjack.jack_port_unregister(client, ports.get(sensorName));
			}
		}
	}
		
	/**
	 * The Class Process.
	 */
	class Process implements JackCallback {

		/** The first call. */
		boolean firstCall = true;
		
		/** The instant. */
		double instant = 0;

		/* (non-Javadoc)
		 * @see jjack.JackCallback#process(int, double)
		 */
		@Override
		public int process(int nframes, double time) {

			if (firstCall) {
				instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) - (nframes * step);
				firstCall = false;
			}
			
			double duration = (double)(nframes) * step;
			
			for (String sensorName : ports.keySet()) {
				Memory earMemory = earMemories.get(sensorName);
				double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);
				FloatBuffer fOut = jjack.jack_port_get_buffer(ports.get(sensorName), nframes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
				int ptr = 0;
				while (fOut.remaining() > 0) {
					fOut.put((float)buf[ptr++]);
				}
			}
			
			instant = instant + duration;
			
			return 0;
			
		}

	};
	
}
