package ensemble.apps.pp;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Hashtable;

import jjack.JackCallback;
import jjack.JackPortFlags;
import jjack.jjack;
import jjack.jjackConstants;
import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.router.MessageConstants;

public class PP_IntegratedMInstrumentReasoning extends Reasoning {

	
	private double MIN_PEAK = 0.4;
	private double MAX_PEAK = 0.7;
	
	//Messages
	private Sensor 		antenna;
	private Memory 		antennaMemory;
	private Actuator 	messenger;
	private Memory 		messengerMemory;

	private boolean checkPeak = false;

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
				mouth.registerListener(this);
				mouths.put(actuatorName, mouth);
				mouthMemories.put(actuatorName, getAgent().getKB().getMemory(mouth.getComponentName()));
				period = Double.valueOf(mouth.getParameter(Constants.PARAM_PERIOD))/1000.0;
				// Activates the JACK client
				if (jjack.jack_activate(client) != 0) {
					System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot activate JACK client... JACK will not be available!");
					return;
				}
				ports.put(actuatorName, jjack.jack_port_register(client, 
											actuatorName,
											jjackConstants.JACK_DEFAULT_AUDIO_TYPE, 
											JackPortFlags.JackPortIsInput));
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
					if (jjack.jack_connect(client, capture_ports[0], client_name+":"+actuatorName) != 0) {
						System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot connect playback ports");
						return;
					}
				}
			}
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			messenger = (Actuator)evtHdl;
			messenger.registerListener(this);
			messengerMemory = getAgent().getKB().getMemory(messenger.getComponentName());
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
		/*else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			messenger = (Actuator)evtHdl;
			messenger.registerListener(this);
			messengerMemory = getAgent().getKB().getMemory(messenger.getComponentName());
		}*/
	}

	@Override
	public void needAction(Actuator sourceActuator, double instant,
			double duration) throws Exception {

//		System.out.println("needAction() - t = " + instant + " até " + (instant+duration));
		// Teoricamente, já vai estar escrito na memória o que deve ser enviado,
		// pois foi preenchido pelo callback do JACK
		mouths.get(sourceActuator.getComponentName()).act();
		
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
					double peak = AnalysisProcessing.peakFollower(44100,
							dBuffer, nframes);
					
					if (peak > MIN_PEAK) {

						Command cmd = new Command(MessageConstants.CMD_RECEIVE);
						cmd.addParameter(MessageConstants.PARAM_TYPE,
								MessageConstants.DIRECTION_TYPE);
						cmd.addParameter(MessageConstants.PARAM_DOMAIN,
								MessageConstants.INTERNAL_DOMAIN);
						cmd.addParameter(MessageConstants.PARAM_ACTION,
								MessageConstants.DIRECTION_CHANGE);

						if (peak < MAX_PEAK)
							cmd.addParameter(MessageConstants.PARAM_ARGS,
									MessageConstants.DIRECTION_UP);
						else
							cmd.addParameter(MessageConstants.PARAM_ARGS,
									MessageConstants.DIRECTION_DOWN);

						System.out.println("PEAK:" + peak + " " + cmd.getParameter(MessageConstants.PARAM_ARGS));
						messengerMemory.writeMemory(cmd);
						messenger.act();
					}
				
					
					
					//mouthMemory.writeMemory(dBuffer, instant, duration, TimeUnit.SECONDS);
 					
				} catch (MemoryException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
			instant = instant + duration;
			
			return 0;
			
		}
			return nframes;

	};

	
}
	}
