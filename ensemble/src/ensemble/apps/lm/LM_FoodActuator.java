package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Parameters;

public class LM_FoodActuator extends Actuator {

	@Override
	public boolean configure() {
		setEventType("ENERGY");
		return true;
	}

}
