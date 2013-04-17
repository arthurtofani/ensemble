package ensemble.apps.pp;


import java.util.Hashtable;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.audio.dsp.FilterProcessing;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.router.MessageConstants;


// TODO: Auto-generated Javadoc
/**
 * The Class PP_SingleFilterReasoning.
 */
public class PP_SingleFilterReasoning extends Reasoning{

	// Audio
	/** The mouth. */
	Actuator 	mouth;
	
	/** The mouth memory. */
	Memory 		mouthMemory;
	
	/** The ear. */
	Sensor 		ear;
	
	/** The ear memory. */
	Memory 		earMemory;
	
	/** The chunk_size. */
	int 		chunk_size;
	
	/** The sample rate. */
	float 		sampleRate;
	
	/** The frame_duration. */
	double 		frame_duration;
	
	/** The antenna. */
	private Sensor 		antenna;
	
	/** The antenna memory. */
	private Memory 		antennaMemory;
	
	//Time Related
	/** The start_time. */
	double 		start_time;
	
	//Auxiliar Memory
	/** The internal memory. */
	Memory internalMemory;

	
	// Movement
	/** The legs. */
	Actuator	legs;
	
	/** The legs memory. */
	Memory 		legsMemory;
	
	/** The eyes. */
	Sensor 		eyes;
	
	/** The eyes memory. */
	Memory 		eyesMemory;

	
	//private World 	world;
	//private Vector 	actual_pos = null;
	
	
	
	/**
	 * The Enum ZoneMode.
	 */
	enum ZoneMode {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The fixed. */
		FIXED			
	}

	/** The zone mode. */
	ZoneMode zoneMode = ZoneMode.FIXED;


	/** The current freq. */
	static int currentFreq = 300;
	
	/** The rez. */
	static double rez = 1;

	// Reasoning state
	/**
	 * The Enum FilterType.
	 */
	enum FilterType {
		
		/** The lowpass. */
		LOWPASS,
		
		/** The highpass. */
		HIGHPASS,
		
		/** The bypass. */
		BYPASS
		}
	
	/** The filter type. */
	static FilterType filterType= FilterType.BYPASS;
	

	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {

		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
				
		// Checks if it is a sound Actuator
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			internalMemory = getAgent().getKB().getMemory( mouth.getComponentName() + Constants.SUF_AUXILIAR_MEMORY);
			mouthMemory = getAgent().getKB().getMemory( mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			frame_duration = chunk_size / sampleRate;


		
			
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			//Checks if it is a sound sensor
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement Actuator
			
		} else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement sensor
			eyes = (Sensor)evtHdl;
			eyes.registerListener(this);
			eyesMemory = getAgent().getKB().getMemory(eyes.getComponentName());
		}
		else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}

	
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			
			
			if (cmd != null && cmd.getCommand()!=MessageConstants.CMD_INFO) {
				
				if (cmd.getParameter(MessageConstants.PARAM_TYPE).equals(MessageConstants.ANDOSC_TYPE)) {
					
					
					if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.ANDOSC_ORIENTATION)) {
						
						/*System.out.println("Recebeu mensagem "
								+ cmd.getParameter(MessageConstants.PARAM_TYPE));
						*/	
						String[] val = cmd.getParameter(MessageConstants.PARAM_ARGS).split(" ");
						if(val.length ==3)
						{
						double valX = Double.parseDouble(val[0]);
						double valY = Double.parseDouble(val[1]);
						double valZ = Double.parseDouble(val[2]);
						
						currentFreq = getFreq(valX);
						rez = geRez(valY);
						}
					}else if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.ANDOSC_TOUCH_POS)) {
						if(filterType == FilterType.BYPASS){
							filterType = FilterType.LOWPASS;
						}else if(filterType == FilterType.LOWPASS){
							filterType = FilterType.HIGHPASS;
						}else filterType = FilterType.BYPASS;
					}
				}
			}
		}
	}

	/**
	 * Ge rez.
	 *
	 * @param valY the val y
	 * @return the double
	 */
	private double geRez(double valY) {
		//O valor vira entre -180 e 180
		double aux =  (valY + 180);
		double val = ( (Math.sqrt(2)-0.1) *aux /360) + 0.1;
		
		return val;
	}

	/**
	 * Gets the freq.
	 *
	 * @param valX the val x
	 * @return the freq
	 */
	private int getFreq(double valX) {
		//Consideramos a frequencia de 100 a 1000 Hz
		//O valor vira entre 0 e 360
		int val = (int) ((900*valX /360) + 100);
		return val;
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	public void needAction(Actuator sourceActuator, double instant, double duration) {
		
			try {

				double[] dBuffer = new double[chunk_size];
				double[] dTransBuffer = new double[chunk_size];
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				
				
				FilterProcessing filterProcess = new FilterProcessing();
				
				//filterProcess.FourPolesHighPass(dBuffer, dTransBuffer, chunk_size, 600);
				//filterProcess.ProcessLPF(dBuffer, dTransBuffer, chunk_size, 600, sampleRate);
				
				//filterProcess.FourPolesLowPass(dBuffer, dTransBuffer, chunk_size, 1700);
				//filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, 400, 44100);
				//filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, 800, 44100);
				
				//Teste peaks
				//double peak = AnalysisProcessing.peakFollower(44100, dBuffer, chunk_size);	
				//System.out.println("Peak: " + peak);
				
				switch(filterType){
				case BYPASS:
					dTransBuffer = dBuffer;
					//System.out.println("BYPASS - freq: " + currentFreq);
					break;
				case LOWPASS:	
					filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, currentFreq, 44100);
					//System.out.println("LOWPASS - freq: " + currentFreq);
					break;
				case HIGHPASS:	
					filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, currentFreq, 44100);
					//System.out.println("HIGHPASS - freq: " + currentFreq);
					break;	
				}
				
				mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);

					//System.out.println("Instant: " + instant + " Duration: " + duration );
				
				//System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
				mouth.act();
					
			} catch (MemoryException e) {
				e.printStackTrace();}

			
//		System.out.println("REAS time = " + (System.currentTimeMillis() - start));
	}

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_OFF)) {
			zoneMode = ZoneMode.NOT_DEFINED;
		} else if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_ON)) {
			zoneMode = ZoneMode.FIXED;
		} 
	}
	
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	public void process() throws Exception {

		
	}
	
	
	
}