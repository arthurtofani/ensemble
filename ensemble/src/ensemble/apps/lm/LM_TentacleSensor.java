package ensemble.apps.lm;

import ensemble.Event;
import ensemble.MusicalAgent;
import ensemble.Parameters;
import ensemble.Sensor;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_TentacleSensor.
 */
public class LM_TentacleSensor extends Sensor {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("MOVEMENT");
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {
		getAgent().getKB().updateFact("SpeciePresent", "0");
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventHandler#process(ensemble.Event)
	 */
	@Override
	protected void process(Event evt) {
		
		System.out.println(getAgent().getAgentName() + ":" + getComponentName() + " recebeu um evento: " + (String)evt.objContent);
		//int note = Integer.parseInt(evt.content);
		//getAgent().getKB().writeFact("SpeciePresent", note);
		
	}

}
