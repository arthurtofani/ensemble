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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Hashtable;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.MusicalAgent;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.jack.JACKOutputReasoning.Process;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.tools.AudioTools;

import jade.core.ServiceException;
import jade.domain.introspection.SuspendedAgent;
import jade.util.Logger;
import jjack.JackCallback;
import jjack.JackPortFlags;
import jjack.SWIGTYPE_p_jack_client_t;
import jjack.SWIGTYPE_p_jack_port_t;
import jjack.jjack;
import jjack.jjackConstants;


public class JACKInputReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// JACK
	String 						client_name;
	long					 	client;
	double 						callbackStartTime;
	double 						period;
	double 						step = 1/44100.0;
	Hashtable<String,String> mapping = new Hashtable<String, String>();
	Hashtable<String, Long> ports = new Hashtable<String,Long>(2);
	
	// Actuator
	Hashtable<String,Actuator> mouths = new Hashtable<String, Actuator>(2);
	Hashtable<String,Memory> mouthMemories = new Hashtable<String, Memory>(2);
	
	@Override
	public boolean init() {
		
		String[] str = getParameter("mapping", "").split("-");
		if (str.length == 1) {
			mapping.put(str[0], "");
		}
		else if (str.length == 2) {
			mapping.put(str[0], str[1]);
		} 
		else {
			System.err.println("[" + this.getAgent().getAgentName() + ":" + getComponentName()+ "] " + "no mapping in parameters!");
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

	@Override
	public boolean finit() {
		
		jjack.jack_client_close(client);

		return true;
		
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			Actuator mouth = (Actuator)evtHdl;
			String actuatorName = mouth.getComponentName();
			if (mapping.containsKey(actuatorName)) {
				mouths.put(actuatorName, mouth);
				mouthMemories.put(actuatorName, getAgent().getKB().getMemory(mouth.getComponentName()));
				period = Double.valueOf(mouth.getParameter(Constants.PARAM_PERIOD))/1000.0;
				// Creats a JACK client
				ports.put(actuatorName, jjack.jack_port_register(client, 
											actuatorName,
											jjackConstants.JACK_DEFAULT_AUDIO_TYPE, 
											JackPortFlags.JackPortIsOutput));
				// If specified, connects the port
				String connectPort = mapping.get(actuatorName);
				if (connectPort != null && !connectPort.equals("")) {
					// Searches the desired playback port
					String[] capture_ports = jjack.jack_get_ports(client, connectPort, null, JackPortFlags.JackPortIsOutput);
					if (capture_ports == null) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot find any physical capture ports");
						return;
					}
					if (capture_ports.length > 1) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] More than one port with that name");
//						return;
					}
					// Connects the port
					if (jjack.jack_connect(client, client_name+":"+actuatorName, capture_ports[0]) != 0) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot connect playback ports");
						return;
					}
				}
			}
		}

	}

	@Override
	protected void eventHandlerDeregistered(EventHandler evtHdl) {
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			String actuatorName = evtHdl.getComponentName();
			if (ports.containsKey(actuatorName)) {
				mouths.remove(actuatorName);
				ports.remove(actuatorName);
				jjack.jack_port_unregister(client, ports.get(actuatorName));
			}
		}
	}

	@Override
	public void needAction(Actuator sourceActuator, double instant,
			double duration) throws Exception {

//		System.out.println("needAction() - t = " + instant + " até " + (instant+duration));
		// Teoricamente, já vai estar escrito na memória o que deve ser enviado,
		// pois foi preenchido pelo callback do JACK
		mouths.get(sourceActuator).act();
		
	}
	
	class Process implements JackCallback {

		double[] dBuffer;
		boolean firstCall = true;
		double instant = 0;

		@Override
		public int process(int nframes, double time) {

			if (firstCall) {
				// It must be 2 frames plus the latency in the future
				instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) + 
							(period * 2) + 
							(nframes * step);
				dBuffer = new double[nframes];
				firstCall = false;
			}
			
			double duration = (double)(nframes) * step;
//			System.out.println("Java::callback - t = " + instant + " até " + (instant+duration));
			
			for (String actuatorName : ports.keySet()) {
				FloatBuffer fIn = jjack.jack_port_get_buffer(ports.get(actuatorName), nframes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
				int ptr = 0;
				while (fIn.remaining() > 0) {
					dBuffer[ptr++] = (double)fIn.get();
				}
				Memory mouthMemory = mouthMemories.get(actuatorName);
				try {
					mouthMemory.writeMemory(dBuffer, instant, duration, TimeUnit.SECONDS);
//					System.out.println(now + " " + getAgent().getClock().getCurrentTime() + " Escrevi do instante " + (instant+period) + " até " + (instant+period+duration));
				} catch (MemoryException e) {
					e.printStackTrace();
				}
			}
			
			instant = instant + duration;
			
			return 0;
			
		}

	};

	
}
