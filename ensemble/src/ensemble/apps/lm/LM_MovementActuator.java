package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Parameters;

public class LM_MovementActuator extends Actuator {

	@Override
	public boolean configure() {
		setEventType("MOVEMENT");
		return true;
	}

}
