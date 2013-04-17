package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_FoodActuator.
 */
public class LM_FoodActuator extends Actuator {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("ENERGY");
		return true;
	}

}
