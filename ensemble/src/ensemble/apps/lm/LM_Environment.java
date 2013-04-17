package ensemble.apps.lm;

import ensemble.EnvironmentAgent;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_Environment.
 */
public class LM_Environment extends EnvironmentAgent {

	/* (non-Javadoc)
	 * @see ensemble.EnsembleAgent#configure()
	 */
	@Override
	public boolean configure() {

		this.addEventServer("ensemble.apps.lm.LM_MovementEventServer", new Parameters());
		this.addEventServer("ensemble.apps.lm.LM_SoundEventServer", new Parameters());
//		this.addEventServer("ensemble.apps.lm.LM_EnergyEventServer", new Parameters());
		this.addEventServer("ensemble.apps.lm.LM_LifeCycleEventServer", new Parameters());
		
		return true;

	}

	/* (non-Javadoc)
	 * @see ensemble.EnvironmentAgent#preUpdateClock()
	 */
	@Override
	protected void preUpdateClock() {
		
		world.getWorldGUI().update();
		
	}
	
}
