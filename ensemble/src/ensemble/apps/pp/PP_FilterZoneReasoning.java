package ensemble.apps.pp;


import java.util.Hashtable;

import org.boris.jvst.VSTException;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.audio.dsp.FftProcessing;
import ensemble.audio.dsp.FilterProcessing;
import ensemble.audio.dsp.PitchShiftProcessing;
import ensemble.audio.vst.VstConstants.FilterMode;
import ensemble.audio.vst.VstProcessReasoning;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.world.Vector;


// TODO: Auto-generated Javadoc
/**
 * The Class PP_FilterZoneReasoning.
 */
public class PP_FilterZoneReasoning extends Reasoning{

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
	/** The actual_pos. */
	private Vector 	actual_pos = null;
	
	// Zones
	/**
	 * The Enum WorldZone.
	 */
	enum WorldZone {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The bright. */
		BRIGHT,
		
		/** The gray. */
		GRAY,
		
		/** The dark. */
		DARK
	}

	
	/** The zone. */
	WorldZone zone = WorldZone.NOT_DEFINED;
	
	//VST definitions
	
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


	//Repertoire
	
	/** The audio file reference. */
	public Hashtable<String, String> audioFileReference =  new Hashtable<String, String>();
	
	/** The audio file reference1. */
	public Hashtable<String, String> audioFileReference1 =  new Hashtable<String, String>();
	
	/** The audio file reference2. */
	public Hashtable<String, String> audioFileReference2 =  new Hashtable<String, String>();
	
	/** The audio file reference3. */
	public Hashtable<String, String> audioFileReference3 =  new Hashtable<String, String>();
	
	/** The audio file reference4. */
	public Hashtable<String, String> audioFileReference4 =  new Hashtable<String, String>();
	
	/** The audio file list. */
	public String[] audioFileList;

	
	// Reasoning state
	/**
	 * The Enum ReasoningState.
	 */
	enum ReasoningState {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The listening. */
		LISTENING,
		
		/** The recording. */
		RECORDING,
		
		/** The processing. */
		PROCESSING,
		
		/** The playing. */
		PLAYING,
		
		/** The error. */
		ERROR
	}
	
	/** The state. */
	ReasoningState state = ReasoningState.NOT_DEFINED;
	
	
	
	
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
	
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if (sourceSensor == ear) {
			
		} else if (sourceSensor == eyes) {
			
		
		}
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	public void needAction(Actuator sourceActuator, double instant, double duration) {

/*		switch (state) {

		case PLAYING:
			//Stops the input bypass
			Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/JACKInputToMemoryReasoning", "STOP");
			sendCommand(cmd);		
			
			// Acts
			//mouth.act();
			
			break;
	
		}*/
		
		switch (zoneMode) {
		
		case FIXED:

			try {

				double[] dBuffer = new double[chunk_size];
				double[] dTransBuffer = new double[chunk_size];
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				
				//get actual zone
				//VstProcessReasoning vstFilterProcess = new VstProcessReasoning();
				
				FilterProcessing filterProcess = new FilterProcessing();
				WorldZone currentZone = getZone(10);
				
				PitchShiftProcessing pitchProcess = new PitchShiftProcessing();
				
				
				switch(currentZone){
				
				case BRIGHT:
					//vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.HIGH_PASS);
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					//filterProcess.FourPolesHighPass(dBuffer, dTransBuffer, chunk_size, 600);
					filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, 400, 44100);
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					break;
				case GRAY:
					//vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.MID_PASS);
					//vstFilterProcess.filter(dBuffer, chunk_size);
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				case DARK:
					
					/*vstFilterProcess.waveshaper(dBuffer, chunk_size, 0.5);
					
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					vstFilterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, 1400, sampleRate);
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					 */
					//filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, 500, 44100);
					
					pitchProcess.PitchShift(dBuffer, dTransBuffer, chunk_size, 1, 44100);
					
					//vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.LOW_PASS);
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
		
					break;
					
				default:	
					dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);

					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				
				}
	

				//System.out.println("Instant: " + instant + " Duration: " + duration );
				
				//System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
				mouth.act();
					
			} catch (MemoryException e) {
				e.printStackTrace();
			
			}

			
			break;

		case NOT_DEFINED:
		
		try {
			
			double[] dBuffer = new double[chunk_size];
			
			dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);

			mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
			
			System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
			
			mouth.act();
				
		} catch (MemoryException e) {
			e.printStackTrace();
		} 
		break;
	}
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
	
	/**
	 * Gets the zone.
	 *
	 * @param size the size
	 * @return the zone
	 */
	public WorldZone getZone(int size){
		
		
		
		String str = (String)eyesMemory.readMemory(eyesMemory.getLastInstant(), TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
		}
		
		int x = 0;
		double valX = actual_pos.getValue(x);
		
		//System.out.println("pos = " + actual_pos.toString() );
		
		if(valX <-(size/6)){

			System.out.println("BRIGHT");
				
			return WorldZone.BRIGHT;
			
		}else if(valX > size/6 ){
			
			System.out.println("DARK");
			
			return WorldZone.DARK;			
			
		}else{ 
			
			System.out.println("GRAY");
			
			return WorldZone.GRAY; 
		
		
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	public void process() throws Exception {

		switch (state) {
		
		case LISTENING:
			
			break;
			
		case RECORDING:
			
			break;

		case PLAYING:
			break;
			
		case PROCESSING:
		
			break;
		}
	}
	
	
	
}