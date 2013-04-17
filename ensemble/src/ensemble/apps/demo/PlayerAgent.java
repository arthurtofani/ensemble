package ensemble.apps.demo;

import ensemble.Constants;
import ensemble.MusicalAgent;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerAgent.
 */
public class PlayerAgent extends MusicalAgent {
	
	/* (non-Javadoc)
	 * @see ensemble.EnsembleAgent#configure()
	 */
	@Override
	public boolean configure() {

		Parameters arguments;
		
		arguments = new Parameters();
		arguments.put(Constants.PARAM_EVT_TYPE, "AUDIO");
		arguments.put(Constants.PARAM_POSITION, getParameter(Constants.PARAM_POSITION));
		addComponent("Mouth", "ensemble.Actuator", arguments);
		
		arguments = new Parameters();
		arguments.put(Constants.PARAM_EVT_TYPE, "MOVEMENT");
		addComponent("Legs", "ensemble.Actuator", arguments);
		
		arguments = new Parameters();
		arguments.put(Constants.PARAM_EVT_TYPE, "MOVEMENT");
		addComponent("Eyes", "ensemble.Sensor", arguments);
		
		arguments = new Parameters();
		addComponent("AudioReasoning", "ensemble.audio.file.AudioFileInputReasoning", arguments);
				
		arguments = new Parameters();
		arguments.put("REASONING_MODE", "PERIODIC");
		arguments.put("PERIOD", "50");
		arguments.put("waypoints", "(30;40;0) 5.0:(-40;-30;0) 5.0:(20;-40;0) 6.0");
		arguments.put("loop", "true");
		addComponent("MovementReasoning", "ensemble.movement.MovementReasoning", arguments);

		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EnsembleAgent#init()
	 */
	@Override
	public boolean init() {
		getKB().registerFact("filename", getParameter("filename"), false);
		return true;
	}
	
}
