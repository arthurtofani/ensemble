package mms;

import mms.Constants.EA_STATE;
import mms.router.RouterClient;

public abstract class MusicalAgentComponent implements LifeCycle, RouterClient {

	private String 			myName;
	private MusicalAgent 	myAgent;
	private String 			myType;
	private EA_STATE 		myState = EA_STATE.CREATED;
	
	protected Parameters parameters = new Parameters();
	
	public final String getComponentName() {
		return myName;
	}
	
	protected final void setComponentName(String myName) {
		if (myState == EA_STATE.CREATED) {
			this.myName = myName;
		} else {
			System.err.println("Cannot set name after being initialized...");
		}
	}
	
	public final MusicalAgent getAgent() {
		return myAgent;
	}

	protected final void setAgent(MusicalAgent myAgent) {
		if (myState == EA_STATE.CREATED) {
			this.myAgent = myAgent;
		} else {
			System.err.println("Cannot set agent after being initialized...");
		}
	}

	public final String getType() {
		return myType;
	}
	
	protected final void setType(String myType) {
		if (myState == EA_STATE.CREATED) {
			this.myType = myType;
		} else {
			System.err.println("Cannot set name after being initialized...");
		}
	}
	
	public final EA_STATE getState() {
		return myState;
	}

	protected final void setState(EA_STATE myState) {
		this.myState = myState;
	}

	public final void addParameter(String key, String value) {
		parameters.put(key, value);
	}

	public final void addParameters(Parameters newParameters) {
		if (newParameters != null) {
			parameters.putAll(newParameters);
		}
	}
	
	public final String getParameter(String key) {
		return parameters.get(key);
	}
	
	public final String getParameter(String key, String defaultValue) {
		if (parameters.containsKey(key)) {
			return parameters.get(key);
 		} else {
 			return defaultValue;
 		}
	}

	//--------------------------------------------------------------------------------
	// Life Cycle
	//--------------------------------------------------------------------------------
	
	@Override
	public final void setParameters(Parameters parameters) {
		addParameters(parameters);
	}
	
	@Override
	public final Parameters getParameters() {
		return parameters;
	}
	
	// ---------------------------------------------- 
	// Command Interface 
	// ---------------------------------------------- 

	@Override
	public final String getAddress() {
		return "/" + Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/" + getComponentName();
	}

	@Override
	public void receiveCommand(Command cmd) {
        System.out.println("[" + getAddress() +"] Command received: " + cmd);
        processCommand(cmd);
	}
	
	@Override
	public void sendCommand(Command cmd) {
		getAgent().sendCommand(cmd);
	}

	//--------------------------------------------------------------------------------
	// User implemented method
	//--------------------------------------------------------------------------------

	@Override
	public boolean configure() {
		return true;
	}

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean finit() {
		return true;
	}
	
	/**
	 * 
	 */
	@Override
	public void processCommand(Command cmd) {
		System.out.println("[" + this.getAgent().getAgentName() + ":" + getComponentName()  +"] " + "User command received: " + cmd);
	}

	/**
	 * Called when a parameter has been updated
	 * @param paramName
	 */
	public void parameterUpdated(String paramName) {
	
	}
	
}

