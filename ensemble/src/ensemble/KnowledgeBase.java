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

import java.util.HashMap;
import java.util.Hashtable;

import ensemble.Constants.EA_STATE;
import ensemble.audio.AudioConstants;
import ensemble.memory.AudioMemory;
import ensemble.memory.EventMemory;
import ensemble.memory.Memory;


public class KnowledgeBase extends MusicalAgentComponent {
	
	//--------------------------------------------------------------------------------
	// Facts
	//--------------------------------------------------------------------------------
	
	class Fact {

		public String 	name;
		public String 	value;
		public boolean 	isPublic;
		
	}
	
	class EventFact {

		public String 	name;
		public Object	value;
		public long 	timestamp;
		
	}
	
	// Tabela de Fatos do Agente
	private HashMap<String, Fact> facts = new HashMap<String, Fact>();

	// Armazena as memórias
	private HashMap<String, Memory> memories = new HashMap<String, Memory>();

	@Override
	public final boolean start() {
		// Sets component type
		setType(Constants.COMP_KB);
		
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addParameter("TYPE", getComponentType());
		cmd.addUserParameters(parameters);
		String param_facts = "{";
		for (String fact : facts.keySet()) {
			param_facts += fact + "=" + facts.get(fact).value + " ";
		}
		param_facts += "}";
		cmd.addParameter("FACTS", param_facts);
		sendCommand(cmd);

		// Calls user initialization code
		if (!init()) {
			return false;
		}
		
		// Sets the agent's state to INITIALIZED
		setState(EA_STATE.INITIALIZED);

		// Informs the console
		cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("STATE", "INITIALIZED");
		sendCommand(cmd);

		return true;
	}

	@Override
	public final boolean stop() {
		// Calls user finalization method
		if (!finit()) {
			return false;
		}
		
		// Sets the agent's state to 
		setState(Constants.EA_STATE.FINALIZED);
		
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		sendCommand(cmd);
		
		return true;
	}
	
	/**
	 * Registers a fact in the Knowledge Base. If it already exists, than update it's value and visibility.
	 * @param fact
	 * @param value
	 * @param isPublic
	 */
	public void registerFact(String fact, String value, boolean isPublic) {
		
		if (!facts.containsKey(fact)) {
			
			Fact newFact = new Fact();
			newFact.name = fact;
			newFact.value = value;
			newFact.isPublic = isPublic;
			facts.put(fact, newFact);
			
			// Se o fato faz parte do fenótipo do Agente, registrar no Ambiente
			if (newFact.isPublic) {
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_PUBLIC_FACT_UPDATE);
				cmd.addParameter(Constants.PARAM_FACT_NAME, fact);
				cmd.addParameter(Constants.PARAM_FACT_VALUE, value);
				sendCommand(cmd);
			}

		} else {
			
			updateFact(fact, value);
			
		}
		
	}
	
	/**
	 * Updates the value of a fact in the Knowledge Base only if it exists.
	 * @param fact the fact name.
	 * @param value teh new value of this fact.
	 */
	public void updateFact(String fact, String value) {

		if (facts.containsKey(fact)) {
			
			Fact aux = facts.get(fact);
			aux.value = value;
			
			// Se o fato faz parte do fenótipo do Agente, enviar atualização 
			if (aux.isPublic) {
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_PUBLIC_FACT_UPDATE);
				cmd.addParameter(Constants.PARAM_FACT_NAME, fact);
				cmd.addParameter(Constants.PARAM_FACT_VALUE, value);
				getAgent().sendCommand(cmd);
			}
			
		}
		
	}
	
	/** 
	 * Reads a fact from the Knowledge Base.
	 * @param fact the fact name.
	 * @return the value of the fact. If the fact does not exist, then returns null.
	 */
	public String readFact(String fact) {
		
		String ret = null;
		
		if (facts.containsKey(fact)) {
			Fact aux = facts.get(fact);
			ret =  aux.value;
		}
		
		return ret;
		
	}
	
	//--------------------------------------------------------------------------------
	// Memory
	//--------------------------------------------------------------------------------
	
	public Memory createMemory(String name, Parameters parameters) {

		Memory newMemory = null;
		
		// Checar se já existe uma memória registrada com esse nome
		if (!memories.containsKey(name)) {
			String className = "ensemble.memory.EventMemory";
			if (parameters.containsKey(Constants.PARAM_MEMORY_CLASS)) {
				className = parameters.get(Constants.PARAM_MEMORY_CLASS);
			}
			else if (parameters.containsKey(Constants.PARAM_EVT_TYPE)) {
				// TODO Senão existir o tipo solicitado, criar uma EventMemory
				if (parameters.get(Constants.PARAM_EVT_TYPE).equals(AudioConstants.EVT_TYPE_AUDIO)) {
					className = "ensemble.memory.AudioMemory";
				} 
			}
			try {
				// Criar a instância do componente
				Class esClass = Class.forName(className);
				newMemory = (Memory)esClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			parameters.put(Constants.PARAM_MEMORY_NAME, name);
			newMemory.setParameters(parameters);
			newMemory.setAgent(getAgent());
			newMemory.configure();
			newMemory.start();
			if (newMemory.getName() != null) { 
				memories.put(newMemory.getName(), newMemory);
			}
		} else {
			newMemory = memories.get(name);
		}
		
		return newMemory;
		
	}
	
	public Memory getMemory(String name) {
		
		return memories.get(name);
		
	}
	
	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

}
