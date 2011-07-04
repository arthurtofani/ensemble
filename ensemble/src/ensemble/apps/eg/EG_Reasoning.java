package ensemble.apps.eg;
import java.util.Hashtable;

import org.boris.jvst.VSTException;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.vst.VstProcessReasoning;
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

	
	// InputMode
	enum InputMode {
		NOT_DEFINED,
		FILE_ONLY,
		MIC_ONLY,
		FILE_ON_PLAY,
		SUM_ON_PLAY,
		VARIABLE
	}
	
	InputMode inputMode = InputMode.NOT_DEFINED;
	
	//VST definitions
	
	enum VSTMode {
		NOT_DEFINED,
		FIXED,
		CHAIN,
		VARIABLE		
	}

	VSTMode vstMode = VSTMode.NOT_DEFINED;

	//VST LIST
	public Hashtable<String, String> vstReference =  new Hashtable<String, String>();
	
	public String[] vstList;

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
		

		vstReference.put("OVERDRIVE", "lib\\vst\\mda Overdrive.dll");
		vstReference.put("DELAY", "lib\\vst\\mda Delay.dll");
		vstReference.put("FILTER", "lib\\vst\\mda MultiBand.dll");
		vstReference.put("TALKBOX", "lib\\vst\\mda TalkBox.dll");
		vstReference.put("REPYSCHO", "lib\\vst\\mda RePsycho!.dll");
		vstReference.put("FLANGER", "lib\\vst\\mda ThruZero.dll");
		vstReference.put("REVERB", "lib\\vst\\DX Reverb Light.dll");
		vstReference.put("EFILTER", "lib\\vst\\EngineersFilter.dll");
		
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

			//Defines the InputMode
			String str = getParameter("inputMode", "");
			System.out.println("inputMode: " + str);
			if(str.equalsIgnoreCase("FILE_ONLY")){
				inputMode= InputMode.FILE_ONLY;
			}else if(str.equalsIgnoreCase("MIC_ONLY")){
				inputMode= InputMode.MIC_ONLY;
			}
			System.out.println("inputMode: " + inputMode.toString());

			//Defines the VSTMode
			str = getParameter("vstMode", "");
			//System.out.println("vstMode: " + str);			
			if(str.equalsIgnoreCase("FIXED")){
				vstMode= VSTMode.FIXED;
			}
			
			// Recover the vstPlugins
			String[] vsts = getParameter("vstPlugins", "").split(";");
			
			if(vsts !=null && vsts.length>0){
				vstList = new String[vsts.length];
			}
			
			for (int i = 0; i<vsts.length; i++){
				if( vstReference.containsKey(vsts[i])){
					vstList[i] = vstReference.get(vsts[i]);
					//System.out.println("vst[" + i +"]: " + vstList[i]);
				}				
			}
			
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
			//Stops the input bypass
			Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/JACKInputToMemoryReasoning", "STOP");
			sendCommand(cmd);		
			
			// Acts
			//mouth.act();
			
			break;
	
		}
		
		switch (inputMode) {
		
		case FILE_ONLY:

			try {

				double[] dBuffer = new double[chunk_size];
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				if (vstList != null && vstList.length > 0) {

					switch (vstMode) {

					case FIXED:

						double[] dTransBuffer = new double[chunk_size];

						new VstProcessReasoning().ProcessAudio(vstList[0],dBuffer, dTransBuffer, chunk_size);
						mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);

						break;

					case NOT_DEFINED:
						mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
						break;
					}

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
		case MIC_ONLY:
			
			try {

				double[] dBuffer = new double[chunk_size];
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				if (vstList != null && vstList.length > 0) {

					switch (vstMode) {

					case FIXED:

						double[] dTransBuffer = new double[chunk_size];
						long start = System.currentTimeMillis();
						new VstProcessReasoning().ProcessAudio(vstList[0],dBuffer, dTransBuffer, chunk_size);
						long end = System.currentTimeMillis();
						System.out.println("start = " + start +" end = " + end + " dif= " + (end-start) );
						mouthMemory.writeMemory(dTransBuffer, instant , duration, TimeUnit.SECONDS);

						break;

					case NOT_DEFINED:
						mouthMemory.writeMemory(dBuffer, instant, duration, TimeUnit.SECONDS);
						break;
					}

				}
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

			//TESTE COMMAND	
			//			if(instant >12){
//				//Stops the input bypass
//				Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/MicInputReasoning", "STOP");
//				sendCommand(cmd);
//				}else if(instant >20){
//					
//					Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/MicInputReasoning", "START");
//					sendCommand(cmd);
//					
//				}
					
			
			mouth.act();
				
		} catch (MemoryException e) {
			e.printStackTrace();
		} 
		break;
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
			//Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/MovementReasoning", "WALK");
//			
//			cmd.addParameter(MovementConstants.PARAM_POS, "("+x+";"+ycmd.addParameter(MovementConstants.PARAM_TIME, "sendCommand(cmd);" +"
			break;
			
		case PROCESSING:
		
			break;
		}
	}
	
	
	
}