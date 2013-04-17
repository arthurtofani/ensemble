package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_MovementActuator.
 */
public class LM_MovementActuator extends Actuator {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("MOVEMENT");
		return true;
	}

}
