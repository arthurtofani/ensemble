package ensemble.apps.lm;

import ensemble.EnvironmentAgent;
import ensemble.Parameters;

public class LM_Environment extends EnvironmentAgent {

	@Override
	public boolean configure() {

		this.addEventServer("ensemble.apps.lm.LM_MovementEventServer", new Parameters());
		this.addEventServer("ensemble.apps.lm.LM_SoundEventServer", new Parameters());
//		this.addEventServer("ensemble.apps.lm.LM_EnergyEventServer", new Parameters());
		this.addEventServer("ensemble.apps.lm.LM_LifeCycleEventServer", new Parameters());
		
		return true;

	}

	@Override
	protected void preUpdateClock() {
		
		world.getWorldGUI().update();
		
	}
	
}
