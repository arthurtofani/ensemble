package ensemble.apps.eg;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.Parameters;
import ensemble.memory.Memory;

public class GeneratorActuator extends Actuator{

	
	@Override
	public boolean init() {
		
		Parameters memParameters = new Parameters();
		Memory auxMemory = getAgent().getKB().createMemory(getComponentName()+ Constants.SUF_AUXILIAR_MEMORY, memParameters);
		System.out.println(getComponentName());
		if (auxMemory == null) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] It was not possible to create an auxiliar memory! Deregistering...");
			deregister();
		}
		// TODO Auto-generated method stub
		return super.init();
	}
	
	
}
