package ensemble.apps.lm;

import ensemble.Event;
import ensemble.MusicalAgent;
import ensemble.Parameters;
import ensemble.Sensor;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_FoodSensor.
 */
public class LM_FoodSensor extends Sensor {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("ENERGY");
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventHandler#process(ensemble.Event)
	 */
	@Override
	protected void process(Event evt) {

		// Ao receber um sense de comida, adiciona a quantidade de energia
		float energy = Float.valueOf(getAgent().getKB().readFact("Energy")); 
		float food = Float.valueOf((String)evt.objContent);		
		getAgent().getKB().updateFact("Energy", (String.valueOf(energy + food)));
		
	}

}
