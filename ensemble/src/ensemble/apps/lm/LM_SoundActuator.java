package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Event;
import ensemble.Parameters;

public class LM_SoundActuator extends Actuator {

	@Override
	public boolean configure() {
		setEventType("SOUND");
		return true;
	}

	@Override
	public boolean init() {
		//getAgent().getKB().writeEventRepository(getEventType(), new String());
		return true;
	}

	@Override
	public void process(Event evt) {
		
		// Ao cantar, basta pegar a primeira nota do genoma musical
		
		// evt = new Event();
		//evt.content = (String)getAgent().getKB().readEventRepository("SOUND");
		
		// return evt;
		
	}

}
