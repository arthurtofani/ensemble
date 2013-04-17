package ensemble.apps.eg;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.memory.Memory;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneratorActuator.
 */
public class GeneratorActuator extends Actuator{

	

	
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#eventHandlerRegistered()
	 */
	@Override
	protected void eventHandlerRegistered() {
		
		Memory auxMemory = getAgent().getKB().createMemory(getComponentName()+ Constants.SUF_AUXILIAR_MEMORY, getParameters());
		//System.out.println(getComponentName());
		if (auxMemory == null) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] It was not possible to create an auxiliar memory! Deregistering...");
			deregister();
		}

	super.eventHandlerRegistered();
	}
	
}
