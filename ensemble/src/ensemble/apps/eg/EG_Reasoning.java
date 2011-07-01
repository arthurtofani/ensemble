package ensemble.apps.eg;
import java.util.Collections;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Parameters;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;

public class EG_Reasoning extends Reasoning{

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
		return false;
		
		
		
	}
	
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		// Checks if it is a sound Actuator
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			internalMemory = getAgent().getKB().getMemory( mouth.getComponentName() + Constants.SUF_AUXILIAR_MEMORY);
			mouthMemory = getAgent().getKB().getMemory( mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			//start_time = Integer.parseInt(mouth.getParameter(Constants.PARAM_START_TIME, "0"));
			frame_duration = chunk_size / sampleRate;
			
//			// Creates a new Memory for auxiliary purposes
//			getAgent().getKB().createMemory(getComponentName() + Constants.SUF_AUXILIAR_MEMORY, getParameters());
//			internalMemory = getAgent().getKB().createMemory(getComponentName(), getParameters());
//			if (internalMemory == null) {
//				System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] It was not possible to create a memory! Deregistering...");
//				evtHdl.deregister();
//				
//			}
			
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			//Checks if it is a sound sensor
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement Actuator
		} else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement sensor
		}
	
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if (sourceSensor == ear) {
			
		} else if (sourceSensor == eyes) {
		
		}
	}

	public void needAction(Actuator sourceActuator, double instant, double duration) {

		switch (state) {

		case PLAYING:
			
			
			// Acts
			//mouth.act();
			
			break;
	
		}
		
		try {
			
			double[] dBuffer = new double[chunk_size];
			//double[] dTransBuffer = new double[nframes];
			
			//new VstProcessReasoning().ProcessAudio("lib\\vst\\mda Overdrive.dll", dBuffer, dTransBuffer, nframes);
			dBuffer = (double[])internalMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			//System.out.println("Instant: " + instant + " Duration: " + duration );
			//0.011609977324263039
			mouthMemory.writeMemory(dBuffer, instant, duration, TimeUnit.SECONDS);
			
			mouth.act();
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		System.out.println("REAS time = " + (System.currentTimeMillis() - start));
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