/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble;

import ensemble.Constants.EA_STATE;
import ensemble.router.RouterClient;

// TODO: Auto-generated Javadoc
/**
 * The Class MusicalAgentComponent.
 */
public abstract class MusicalAgentComponent implements LifeCycle, RouterClient {

	/** The my name. */
	private String 			myName;
	
	/** The my agent. */
	private MusicalAgent 	myAgent;
	
	/** The my type. */
	private String 			myType;
	
	/** The my state. */
	private EA_STATE 		myState = EA_STATE.CREATED;
	
	/** The parameters. */
	protected Parameters parameters = new Parameters();
	
	/**
	 * Gets the component name.
	 *
	 * @return the component name
	 */
	public final String getComponentName() {
		return myName;
	}
	
	/**
	 * Gets the component type.
	 *
	 * @return the component type
	 */
	public final String getComponentType() {
		return myType;
	}
	
	/**
	 * Sets the component name.
	 *
	 * @param myName the new component name
	 */
	protected final void setComponentName(String myName) {
		if (myState == EA_STATE.CREATED) {
			this.myName = myName;
		} else {
			System.err.println("Cannot set name after being initialized...");
		}
	}
	
	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	public final MusicalAgent getAgent() {
		return myAgent;
	}

	/**
	 * Sets the agent.
	 *
	 * @param myAgent the new agent
	 */
	protected final void setAgent(MusicalAgent myAgent) {
		if (myState == EA_STATE.CREATED) {
			this.myAgent = myAgent;
		} else {
			System.err.println("Cannot set agent after being initialized...");
		}
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public final String getType() {
		return myType;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param myType the new type
	 */
	protected final void setType(String myType) {
		if (myState == EA_STATE.CREATED) {
			this.myType = myType;
		} else {
			System.err.println("Cannot set name after being initialized...");
		}
	}
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public final EA_STATE getState() {
		return myState;
	}

	/**
	 * Sets the state.
	 *
	 * @param myState the new state
	 */
	protected final void setState(EA_STATE myState) {
		this.myState = myState;
	}

	/**
	 * Adds the parameter.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public final void addParameter(String key, String value) {
		parameters.put(key, value);
	}

	/**
	 * Adds the parameters.
	 *
	 * @param newParameters the new parameters
	 */
	public final void addParameters(Parameters newParameters) {
		if (newParameters != null) {
			parameters.putAll(newParameters);
		}
	}
	
	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @return the parameter
	 */
	public final String getParameter(String key) {
		return parameters.get(key);
	}
	
	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the parameter
	 */
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
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#setParameters(ensemble.Parameters)
	 */
	@Override
	public final void setParameters(Parameters parameters) {
		addParameters(parameters);
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#getParameters()
	 */
	@Override
	public final Parameters getParameters() {
		return parameters;
	}
	
	// ---------------------------------------------- 
	// Command Interface 
	// ---------------------------------------------- 

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#getAddress()
	 */
	@Override
	public final String getAddress() {
		return "/" + Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/" + getComponentName();
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public final void receiveCommand(Command cmd) {
//        System.out.println("[" + getAddress() +"] Command received: " + cmd);
		if (cmd.getCommand().equals(Constants.CMD_PARAMETER)) {
			String param = cmd.getParameter("NAME");
			String value = cmd.getParameter("VALUE");
			if (param != null && value != null && parameters.containsKey(param)) {
				// Calls user method
				if (!parameterUpdate(param, value)) {
					return;
				}
				// TODO Alguns parâmetros não podem ser mudados!
				parameters.put(param, value);
				// Let the console knows about the updated parameter
				cmd = new Command(getAddress(), "/console", "UPDATE");
				cmd.addParameter("AGENT", getAgent().getAgentName());
				cmd.addParameter("COMPONENT", getComponentName());
				cmd.addParameter("NAME", param);
				cmd.addParameter("VALUE", value);
				sendCommand(cmd);
			}
		}
		else {
			processCommand(cmd);
		}
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#sendCommand(ensemble.Command)
	 */
	@Override
	public final void sendCommand(Command cmd) {
		getAgent().sendCommand(cmd);
	}

	//--------------------------------------------------------------------------------
	// User implemented method
	//--------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}

}

