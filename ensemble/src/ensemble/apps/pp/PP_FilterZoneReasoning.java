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
import ensemble.audio.dsp.FftProcessing;
import ensemble.audio.dsp.FilterProcessing;
import ensemble.audio.vst.VstConstants.FilterMode;
import ensemble.audio.vst.VstProcessReasoning;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.world.Vector;


public class PP_FilterZoneReasoning extends Reasoning{

	// Audio
	Actuator 	mouth;
	Memory 		mouthMemory;
	Sensor 		ear;
	Memory 		earMemory;
	int 		chunk_size;
	float 		sampleRate;
	double 		frame_duration;
	
	
	//Time Related
	double 		start_time;
	
	//Auxiliar Memory
	Memory internalMemory;

	
	// Movement
	Actuator	legs;
	Memory 		legsMemory;
	Sensor 		eyes;
	Memory 		eyesMemory;

	
	//private World 	world;
	private Vector 	actual_pos = null;
	
	// Zones
	enum WorldZone {
		NOT_DEFINED,
		BRIGHT,
		GRAY,
		DARK
	}

	
	WorldZone zone = WorldZone.NOT_DEFINED;
	
	//VST definitions
	
	enum ZoneMode {
		NOT_DEFINED,
		FIXED			
	}

	ZoneMode zoneMode = ZoneMode.FIXED;


	//Repertoire
	
	public Hashtable<String, String> audioFileReference =  new Hashtable<String, String>();
	public Hashtable<String, String> audioFileReference1 =  new Hashtable<String, String>();
	public Hashtable<String, String> audioFileReference2 =  new Hashtable<String, String>();
	public Hashtable<String, String> audioFileReference3 =  new Hashtable<String, String>();
	public Hashtable<String, String> audioFileReference4 =  new Hashtable<String, String>();
	
	public String[] audioFileList;

	
	// Reasoning state
	enum ReasoningState {
		NOT_DEFINED,
		LISTENING,
		RECORDING,
		PROCESSING,
		PLAYING,
		ERROR
	}
	
	ReasoningState state = ReasoningState.NOT_DEFINED;
	
	
	
	
	@Override
	public boolean init() {

		return true;
		
	}
	
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
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if (sourceSensor == ear) {
			
		} else if (sourceSensor == eyes) {
			
		
		}
	}

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
				VstProcessReasoning vstFilterProcess = new VstProcessReasoning();
				
				FilterProcessing filterProcess = new FilterProcessing();
				WorldZone currentZone = getZone(10);
				
				
				switch(currentZone){
				
				case BRIGHT:
					vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.HIGH_PASS);
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					//filterProcess.FourPolesHighPass(dBuffer, dTransBuffer, chunk_size, 600);
					//filterProcess.ProcessLPF(dBuffer, dTransBuffer, chunk_size, 600, sampleRate);
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				case GRAY:
					vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.MID_PASS);
					//vstFilterProcess.filter(dBuffer, chunk_size);
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				case DARK:
					
					/*vstFilterProcess.waveshaper(dBuffer, chunk_size, 0.5);
					
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					vstFilterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, 1400, sampleRate);
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					 */
					
					vstFilterProcess.ProcessFilter(dBuffer, dTransBuffer, chunk_size, FilterMode.LOW_PASS);
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
			} catch (VSTException e) {
				// TODO Auto-generated catch block
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

	@Override
	public void processCommand(Command cmd) {
		if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_OFF)) {
			zoneMode = ZoneMode.NOT_DEFINED;
		} else if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_ON)) {
			zoneMode = ZoneMode.FIXED;
		} 
	}
	
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