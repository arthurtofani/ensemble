package mms;

import java.util.HashMap;
import java.util.Hashtable;

import mms.Constants.EA_STATE;
import mms.audio.AudioConstants;
import mms.memory.AudioMemory;
import mms.memory.EventMemory;
import mms.memory.Memory;

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
		cmd.addParameter("PARAMETERS", parameters.toString());
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
	
	// TODO Deixar o tipo genérico
	// TODO Como colocar os parâmetros relativos ao áudio (step, frame) na chamada do construtor?
	public Memory createMemory(String name, String eventType, double expiration, Parameters parameters) {

		Memory mem = null;
		
		// Checar se já existe uma memória registrada com esse nome
		if (!memories.containsKey(name)) {
			// Caso negativo, criar uma nova memória
			// TODO Senão existir o tipo solicitado, criar uma SimpleMemory
			if (eventType.equals(AudioConstants.EVT_TYPE_AUDIO)) {
//				mem = new AudioMemory(myAgent, name, expiration, expiration, parameters);
				mem = new AudioMemory();
				mem.start(getAgent(), name, expiration, expiration, parameters);
				memories.put(name, mem);
//				System.out.println("Criei memória de AUDIO para " + name);
			} else {
				mem = new EventMemory();
				mem.start(getAgent(), name, expiration, expiration, parameters);
				memories.put(name, mem);
//				System.out.println("Criei memória de Simple para " + name);
			}
		} else {
			mem = memories.get(name);
		}
		
		return mem;
		
	}
	
	public void addMemory(Memory memory) {
		
		if(!memories.containsKey(memory.getName())) {
			memories.put(memory.getName(), memory);
		}
		
	}
	
	public Memory getMemory(String name) {
		
		return memories.get(name);
		
	}
	
	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

}
