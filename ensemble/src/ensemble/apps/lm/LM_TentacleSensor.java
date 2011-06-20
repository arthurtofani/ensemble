package ensemble.apps.lm;

import ensemble.Event;
import ensemble.MusicalAgent;
import ensemble.Parameters;
import ensemble.Sensor;

public class LM_TentacleSensor extends Sensor {

	@Override
	public boolean configure() {
		setEventType("MOVEMENT");
		return true;
	}

	@Override
	public boolean init() {
		getAgent().getKB().updateFact("SpeciePresent", "0");
		return true;
	}

	@Override
	protected void process(Event evt) {
		
		System.out.println(getAgent().getAgentName() + ":" + getComponentName() + " recebeu um evento: " + (String)evt.objContent);
		//int note = Integer.parseInt(evt.content);
		//getAgent().getKB().writeFact("SpeciePresent", note);
		
	}

}
