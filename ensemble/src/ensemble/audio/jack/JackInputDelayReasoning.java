
package ensemble.audio.jack;

import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Date;
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
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.router.MessageConstants;


// TODO: Auto-generated Javadoc
/**
 * The Class JackInputDelayReasoning.
 */
public class JackInputDelayReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// JACK
	/** The client_name. */
	String 						client_name;
	
	/** The client. */
	long					 	client;
	
	/** The callback start time. */
	double 						callbackStartTime;
	
	/** The period. */
	double 						period;
	
	/** The step. */
	double 						step = 1/44100.0;
	
	/** The mapping. */
	Hashtable<String,String> mapping = new Hashtable<String, String>();
	
	/** The ports. */
	Hashtable<String, Long> ports = new Hashtable<String,Long>(2);
	
	// Actuator
	/** The mouths. */
	Hashtable<String,Actuator> mouths = new Hashtable<String, Actuator>(2);
	
	/** The mouth memories. */
	Hashtable<String,Memory> mouthMemories = new Hashtable<String, Memory>(2);
	
	/** The antenna. */
	private Sensor 		antenna;
	
	/** The antenna memory. */
	private Memory 		antennaMemory;
	
	
	/** The delay. */
	private double				delay = 0;
	
	/** The delay changed. */
	private boolean				delayChanged = false;
	
	/** The delay ready. */
	private boolean				delayReady = false;
	
	/** The delay last. */
	private double				delayLast = 0;
	
	/** The delay diff. */
	private double				delayDiff = 0; 
	
	/** The active. */
	private boolean				active = true;
	
	/** The allow change. */
	private boolean 			allowChange;
	
	/** The time diff. */
	private long				timeDiff;
	
	/** The last change. */
	private Date				lastChange;

	/** The gain. */
	private double gain = 1.0;
	
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

		
		active = true;
		allowChange = true;
		
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
		}

	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerDeregistered(ensemble.EventHandler)
	 */
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

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	@Override
	public void needAction(Actuator sourceActuator, double instant,
			double duration) throws Exception {

//		System.out.println("needAction() - t = " + instant + " até " + (instant+duration));
		// Teoricamente, já vai estar escrito na memória o que deve ser enviado,
		// pois foi preenchido pelo callback do JACK
		mouths.get(sourceActuator.getComponentName()).act();
		
	}
	
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		if (sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)) {
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			//System.out.println("Mensagem =  "+ str);
			Command cmd = Command.parse(str);
			if (cmd != null && cmd.getParameter(MessageConstants.PARAM_TYPE)!=null 
					&& cmd.getParameter(MessageConstants.PARAM_TYPE).equals(
							MessageConstants.CONTROL_OSC_TYPE)) {
				// CONTROL OSC

				// DELAY
				if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.CONTROL_OSC_DELAY)) {
					if (cmd.containsParameter(MessageConstants.PARAM_ARGS)) {

						//System.out.println("[" + getAgent().getAgentName()
						//		+ "] delay...");

						String[] val = cmd.getParameter(
								MessageConstants.PARAM_ARGS).split(" ");
						double novoDelay = Double.parseDouble(val[0]);

						novoDelay = novoDelay * 0.8;

						if (novoDelay > 0.8)
							novoDelay = 0.8;
						if (novoDelay <= 0)
							novoDelay = 0.01;
						
						delayDiff = novoDelay - delay;
						delay = novoDelay;
						delayChanged = true;
						System.out.println("DELAY:" + delay);
					}
				}
				//VOLUME
				else if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.CONTROL_OSC_VOLUME)) {
					if (cmd.containsParameter(MessageConstants.PARAM_ARGS)) {

						System.out.println("[" + getAgent().getAgentName()
								+ "] volume...");

						String[] val = cmd.getParameter(
								MessageConstants.PARAM_ARGS).split(" ");

						double novoGain = Double.parseDouble(val[0]);

						if (novoGain <= 0.18)
							novoGain = 0;
						gain = novoGain;

						
						System.out.println("VOLUME:" + gain);
					}
				}

			}else if (cmd != null && cmd.getParameter(MessageConstants.PARAM_TYPE)!=null 
					&& cmd.getParameter(MessageConstants.PARAM_TYPE).equals(
							MessageConstants.ANDOSC_TYPE)) {
				
				if (cmd.getParameter(MessageConstants.PARAM_ACTION)
						.equals(MessageConstants.ANDOSC_ACCELEROMETER)) {

					String[] val = cmd
							.getParameter(MessageConstants.PARAM_ARGS).split(
									" ");

					// SWITCH
					float orientacaoX = Float.parseFloat(val[0]);

					/*
					 * System.out.println("X: " + orientacaoX + "allowChange " +
					 * allowChange);
					 */

					if (orientacaoX <= -7 && allowChange) {
						active = !active;
						allowChange = false;
						lastChange = new Date();
						

						//System.out.println("Mudanca de estado:" +active);

					} else if (orientacaoX >= -0.5 && !allowChange
							&& lastChange != null) {
						timeDiff = Math.abs((new Date()).getTime()
								- lastChange.getTime());
						if (timeDiff > 800) {
							allowChange = true;
							// System.out.println("PODE chutar de novo!! " +
							// timeDiff);
						}

					}

				}
				
			}
		}
	}

	/**
	 * The Class Process.
	 */
	class Process implements JackCallback {

		/** The d buffer. */
		double[] dBuffer;
		
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
				// It must be 2 frames plus the latency in the future
				instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) + 
							(period * 2) + 
							(nframes * step);
				dBuffer = new double[nframes];
				firstCall = false;
			}
			
			double duration = (double)(nframes) * step;
//			System.out.println("Java::callback - t = " + instant + " até " + (instant+duration));
			
			
			if (active) {

				for (String actuatorName : ports.keySet()) {
					FloatBuffer fIn = jjack
							.jack_port_get_buffer(ports.get(actuatorName),
									nframes).order(ByteOrder.LITTLE_ENDIAN)
							.asFloatBuffer();
					int ptr = 0;
					while (fIn.remaining() > 0) {
						dBuffer[ptr++] = (double) fIn.get()*gain;
					}
					Memory mouthMemory = mouthMemories.get(actuatorName);
					try {
						// double[] dTransBuffer = new double[nframes];
						//
						// new
						// VstProcessReasoning().ProcessAudio("lib\\vst\\mda Overdrive.dll",
						// dBuffer, dTransBuffer, nframes);
						//
						// double peak = AnalysisProcessing.peakFollower(44100,
						// dBuffer, nframes);
						// System.out.println("Peak: " + peak);

						if (delayChanged) {

							delayLast = dBuffer[nframes - 1];

							for (int i = 0; i < nframes; i++) {
								dBuffer[i] = dBuffer[i] * (1 - (i / nframes));
							}

							delayChanged = false;
							delayReady = true;
						} else if (delayReady) {

							if (delayDiff > 0) {
								int numFrames = (int) (delayDiff * 44100);
								double[] interpolation = new double[numFrames];
								for (int i = 0; i < numFrames; i++) {
									interpolation[i] = delayLast
											* (1 - (i / numFrames))
											+ dBuffer[nframes - 1]
											* (i / numFrames);
								}
								mouthMemory.writeMemory(interpolation, instant,
										numFrames, TimeUnit.SAMPLES);
								System.out.println("entrou!! ! ! " + numFrames);
							}
							/*
							 * for(int i = 0; i<nframes;i++){ dBuffer[i] =
							 * dBuffer[i] * (i/nframes); }
							 */

							for (int i = 0; i < nframes; i++) {
								dBuffer[i] = dBuffer[i] * (1 - (i / nframes));
							}
							delayReady = false;
						}

						
						mouthMemory.writeMemory(dBuffer, instant + delay,
								duration, TimeUnit.SECONDS);

					} catch (MemoryException e) {
						e.printStackTrace();
					}
					// catch (VSTException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}
			}
			instant = instant + duration;
			
			return 0;
			
		}

	};

	
}

