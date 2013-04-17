package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Event;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_SoundActuator.
 */
public class LM_SoundActuator extends Actuator {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("SOUND");
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {
		//getAgent().getKB().writeEventRepository(getEventType(), new String());
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventHandler#process(ensemble.Event)
	 */
	@Override
	public void process(Event evt) {
		
		// Ao cantar, basta pegar a primeira nota do genoma musical
		
		// evt = new Event();
		//evt.content = (String)getAgent().getKB().readEventRepository("SOUND");
		
		// return evt;
		
	}

}
