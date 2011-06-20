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

public abstract class MusicalAgentComponent implements LifeCycle, RouterClient {

	private String 			myName;
	private MusicalAgent 	myAgent;
	private String 			myType;
	private EA_STATE 		myState = EA_STATE.CREATED;
	
	protected Parameters parameters = new Parameters();
	
	public final String getComponentName() {
		return myName;
	}
	
	public final String getComponentType() {
		return myType;
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
	
	@Override
	public final void sendCommand(Command cmd) {
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
	
	@Override
	public void processCommand(Command cmd) {
	}
	
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}

}

