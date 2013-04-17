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

//import java.io.PrintWriter;

import java.util.ArrayList;

import ensemble.Constants.EA_STATE;
import ensemble.Constants.EH_STATUS;
import ensemble.clock.TimeUnit;
import ensemble.memory.MemoryException;

// TODO: Auto-generated Javadoc
/**
 * The Class Sensor.
 */
public class Sensor extends EventHandler implements Sensing {
	
	/** The early_events. */
	protected ArrayList<Event> early_events = new ArrayList<Event>();
	
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#start()
	 */
	@Override
	public final boolean start() {

		// Sets component type
		setType(Constants.COMP_SENSOR);
		
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addParameter("TYPE", getComponentType());
		cmd.addParameter("EVT_TYPE", parameters.get("EVT_TYPE"));
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		if (!super.start()) {
			return false;
		}
		
		myComm.sensing = true;

		// Calls user initialization code
		if (!init()) {
			return false;
		}
		
		// Sets the agent's state to INITIALIZED
		setState(EA_STATE.INITIALIZED);
		
		cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("STATE", "INITIALIZED");
		sendCommand(cmd);

		return true;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#stop()
	 */
	@Override
	public final boolean stop() {

		// Calls user initialization code
		if (!finit()) {
			return false;
		}
		
		if (!super.stop()) {
			return false;
		}
		
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		sendCommand(cmd);
		
		return true; 
		
	}
	
	// Chamado por um raciocínio para registrá-lo como listener dos eventos
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#registerListener(ensemble.Reasoning)
	 */
	public void registerListener(Reasoning reasoning) {
		listeners.add(reasoning);
	}
	
	// Método chamado pelo Comm ao receber um evento
	/* (non-Javadoc)
	 * @see ensemble.Sensing#sense(ensemble.Event)
	 */
	public void sense(Event evt) {
		
		if (status == EH_STATUS.REGISTERED && evt.eventType.equals(eventType)) {
//			MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Event received");
//			System.out.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Event received - " + evt.objContent);
			
			// Chama o método do usuário
			// TODO Melhor antes ou depois? Ou ter os dois?
			try {
				process(evt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Armazenar o evento na Base de Conhecimentos
			try {
				if (myMemory == null) {
					System.err.println("ERROR: no memory registered");
				} else {
					myMemory.writeMemory(evt.objContent, evt.instant, evt.duration, TimeUnit.SECONDS);
				}
//				System.out.println("[" + getAgent().getAgentName() + "] Guardei na memória um evento no instante " + evt.instant + " de duração " + evt.duration);
			} catch (MemoryException e1) {
//				MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
			}
			
			// Avisar os raciocínios registrados
			for (Reasoning reasoning : listeners) {
				try {
					reasoning.newSense(this, evt.instant, evt.duration);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			// No caso de event BATCH, envia o ACK
			if (getAgent().getProperty(Constants.PROCESS_MODE, null).equals(Constants.MODE_BATCH)) {
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_BATCH_EVENT_ACK);
				sendCommand(cmd);
			}
			
//			MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Processei evento " + evt.timestamp);
		}
		else if (evt.eventType.equals(eventType)) {
			// Stores this event for later, since it was sent between the REGSITER-EVENT and REGISTER-EVENT-ACK
			early_events.add(evt);
		}
		
	}

}
