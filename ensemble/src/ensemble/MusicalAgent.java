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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Constants.EA_STATE;
import ensemble.Constants.EH_STATUS;
import ensemble.Constants.MA_STATE;
import ensemble.clock.TimeUnit;
import ensemble.world.Vector;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

// TODO: Auto-generated Javadoc
/**
 * The Class MusicalAgent.
 */
public class MusicalAgent extends EnsembleAgent {

	//--------------------------------------------------------------------------------
	// Agent attributes
	//--------------------------------------------------------------------------------
	
	/** The tbf. */
	ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	/** The lock. */
	private Lock lock = new ReentrantLock();

	/** The environment agent. */
	protected String environmentAgent;
	
	/** The components. */
	private ConcurrentHashMap<String, MusicalAgentComponent> components = new ConcurrentHashMap<String, MusicalAgentComponent>();
	
	/** The state. */
	public MA_STATE state = MA_STATE.CREATED;
	
	/** The kb. */
	protected KnowledgeBase kb = null;
	
	// ---------------------------------------------- 
	// Batch processing control variables 
	// ---------------------------------------------- 

	/** Contador de raciocínios do agente. */
	private int numberReasoning = 0;
	
	/** Contador de raciocínios prontos. */
	private int numberReasoningReady = 0;
	
	/** Contador de eventos enviados. */
	private int numberEventsSent = 0;
	
	/** Contador de pedidos de registro de EventHandlers no momento da inicializção. */
	private int numberEventHandlersRequest = 0;
	
	/** Contador de EventHandlers já registrados. */
	private int numberEventHandlersRegistered = 0;
	
	/** Controle se o agente deve morrer no próximo turno. */
	private boolean dieNextTurn = false;
	
	//--------------------------------------------------------------------------------
	// Agent initialization and termination
	//--------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#start()
	 */
	@Override
	public final boolean start() {

		Command cmd;
		
		// Sends a message to the console
		cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		lock.lock();
		try {

			// Registrar-se no Ambiente (necessário tanto em BATCH como em REAL_TIME)
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(Constants.ENVIRONMENT_AGENT);
			template.addServices(sd);
			try {
				boolean envFound = false;
				while (!envFound) {
					DFAgentDescription[] result = DFService.search(this, template);
					if (result.length == 1) {
						environmentAgent = result[0].getName().getLocalName();
						envFound = true;
						cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_AGENT_REGISTER);
						cmd.addParameter(Constants.PARAM_POSITION, parameters.get(Constants.PARAM_POSITION, "(0;0;0)"));
						sendCommand(cmd);
					} else {
						// TODO jeito ruim de ficar tentando registrar o Agente
//						MusicalAgent.logger.info("[" + getAgent().getAgentName() + "] " + "Environment Agent not found! Trying again...");
						Thread.sleep(500);
					}
				}
			} catch (Exception fe) {
//				logger.severe("[" + this.getAgentName() + "] " + "Environment Agent not available");
				System.out.println("[" + this.getAgentName() + "] " + "Environment Agent not available");
				this.doDelete();
				return false;
			}
			
			// If no kb has been configured, creates a generic kb
			if (kb == null) {
				addKB("ensemble.KnowledgeBase", new Parameters());
			}
			kb.start();

			// TODO Registras os fatos públicos do KB
			
			// Executa o método de inicialização do usuário
			// TODO Tratar o caso retornar false
			if (!init()) {
				return false;
			}
			
			// Inicializa os componentes
			Collection<MusicalAgentComponent> comps = components.values();
			for (Iterator<MusicalAgentComponent> iterator = comps.iterator(); iterator.hasNext();) {
				MusicalAgentComponent comp = iterator.next();
				if (comp.getState() == EA_STATE.CREATED) { 
					boolean result = comp.start();
					if (!result) {
						System.out.println("[" + this.getAgentName() + "] Component '" + comp.getComponentName() + "' not initialized");
						removeComponent(comp.getComponentName());
					} else {
						// Descobre qual o tipo do componente
						// Se for EventHandler, deve registrar no Ambiente responsável
						// e avisar os raciocínios existentes sobre o novo EventHandler
						if (comp instanceof EventHandler) {
							// incrementa o contador de registros
							numberEventHandlersRequest++;
							// solicita o registro
							((EventHandler)comp).register();
						}
						else if (comp instanceof Reasoning) {
							numberReasoning++;
						}
					}
				}
			}
			
			// Aguarda o registro de todos os EventHandlers, caso seja necessário
			this.addBehaviour(new CheckRegister(this));			
			
			// Fim da inicialização do Agente Musical
			state = MA_STATE.INITIALIZED;
			
			// Enviar mensagem para o console
			cmd = new Command(getAddress(), "/console", "UPDATE");
			cmd.addParameter("AGENT", getAgent().getAgentName());
			cmd.addParameter("STATE", "INITIALIZED");
			sendCommand(cmd);
			
		} finally {
			lock.unlock();
		}
		
		return true;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public final boolean stop() {
		
		// Calls the user implemented finalization method
		if (!finit()) { 
			return false;
		}
		
		// Stops the KnowledgeBase
		kb.stop();
		
		// Warns the Sniffer
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		sendCommand(cmd);

		return true;
		
	}

	/**
	 * Adds the kb.
	 *
	 * @param className the class name
	 * @param parameters the parameters
	 */
	public final void addKB(String className, Parameters parameters) {
		if (state == MA_STATE.CREATED) {
			try {
				Class kbClass = Class.forName(className);
				kb = (KnowledgeBase) kbClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			kb.setComponentName(Constants.KNOWLEDGE_BASE);
			kb.setAgent(this);
			kb.setParameters(parameters);
			kb.configure();
		} else {
			System.err.println("[" + getAgentName() + "] Trying to add a World in runtime!");
		}
	}
	
	/**
	 * Adiciona um componente ao agente, seja um raciocínio, sensor, atuador etc. Deve configurar o componente e iniciar sua execução.
	 *
	 * @param compName the comp name
	 * @param className the class name
	 * @param arguments the arguments
	 */
	public final void addComponent(String compName, String className, Parameters arguments) {

		lock.lock();
		try {
			try {
				// Criar a instância do componente
				Class esClass = Class.forName(className);
				MusicalAgentComponent comp;
				comp = (MusicalAgentComponent)esClass.newInstance();

				// Verificar as condições para a criação desse componente
				if (compName.equals(Constants.COMP_ACTUATOR) || compName.equals(Constants.COMP_SENSOR) || 
					compName.equals(Constants.COMP_REASONING) || compName.equals(Constants.COMP_KB)) {
					System.out.println("[" + this.getAgentName() + "] Component '" + compName + "' using a reserved name");
					return;
				}
				if (components.containsKey(compName)) {
					System.out.println("[" + this.getAgentName() + "] Component '" + compName + "' already exists");
					return;
				}
				
				// Configura o componente
				comp.setComponentName(compName);
				comp.setAgent(this);
				comp.setParameters(arguments);
				comp.configure();
				
				// Sets the position of the EventHandler in Agent's "body"
				if (comp instanceof EventHandler) {
					if (arguments.containsKey(Constants.PARAM_REL_POS)) {
						Vector position = Vector.parse(arguments.get(Constants.PARAM_REL_POS));
						((EventHandler)comp).setRelativePosition(position);
					}
				}
				
				// Adicionar o componente na tabela
				components.put(compName, comp);

				// Caso o Agente Musical já tiver sido inicializado, inicializar o componente
				if (state == MA_STATE.REGISTERED) {
					boolean result = comp.start();
					if (!result) {
						System.out.println("[" + this.getAgentName() + "] Component '" + compName + "' not initialized");
						removeComponent(compName);
						return;
					}
					// Descobre qual o tipo do componente
					// Se for EventHandler, deve registrar no Ambiente responsável
					// e avisar os raciocínios existentes sobre o novo EventHandler
					if (comp instanceof EventHandler) {
						// incrementa o contador de registros
						numberEventHandlersRequest++;
						// solicita o registro
						((EventHandler)comp).register();
					}
					else if (comp instanceof Reasoning) {
						numberReasoning++;
					}
					// TODO Broadcast aos componentes existentes sobre o novo componente
				}
//				logger.info("[" + getAgentName() + "] " + "Component " + comp.getName() + " added");
				
				// Warns the Reasoning about all EventHandlers present that are already registered
				if (comp instanceof Reasoning) {
					for (MusicalAgentComponent existingComp : components.values()) {
						if (existingComp instanceof EventHandler && 
								((EventHandler)existingComp).getStatus() == EH_STATUS.REGISTERED) {
							try {
								((Reasoning)comp).eventHandlerRegistered((EventHandler)existingComp);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				
			} catch (ClassNotFoundException e) {
	//			e.printStackTrace();
				System.err.println("ERROR: Not possible to create an instance of " + className);
			} catch (InstantiationException e) {
				e.printStackTrace();
	//			System.err.println("ERROR: Not possible to create an instance of " + className);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
	//			System.err.println("ERROR: Not possible to create an instance of " + className);
			}
		} finally {
			lock.unlock();
		}

	}
	
	/**
	 * Remove um componente do Agente.
	 *
	 * @param compName the comp name
	 */
	public final void removeComponent(String compName) {

		if (!components.containsKey(compName)) {
			System.out.println("[" + this.getAgentName() + "] " + "Component '" + compName + "' does not exists in Agent " + getAgentName());
			return;
		}
		
		MusicalAgentComponent comp = components.get(compName);

		// No caso de ser um EventHandler, deve solicitar o deregistro e só depois remover o componente
		if (comp instanceof EventHandler) {
			
			// Desregistrar o componente, no caso de ser um sensor/atuador
			((EventHandler)comp).deregister();

		} else {
			
			if (comp instanceof Reasoning) {
				numberReasoning--;
			}
			
			comp.stop();
			
			components.remove(compName);
			
			System.out.println("[" + this.getAgentName() + "] " + "Component '" + comp.getComponentName() + "' removed");
			
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Agent Getters
	//--------------------------------------------------------------------------------
	
	/**
	 * Gets the mA state.
	 *
	 * @return the mA state
	 */
	public final MA_STATE getMAState() {
		return state;
	}
	
	/**
	 * Gets the kb.
	 *
	 * @return the kb
	 */
	public final KnowledgeBase getKB() {
		if (kb == null) {
			System.err.println("[" + getAddress() +"] No knowledge base present!");
		}
		return kb;
	}
	
	/**
	 * Gets the environment agent.
	 *
	 * @return the environment agent
	 */
	public final String getEnvironmentAgent() {
		return environmentAgent;
	}
	
	//--------------------------------------------------------------------------------
	// Command Interface 
	//--------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public final void receiveCommand(Command cmd) {
      
//		System.out.printf("[%s] Command received: %s - %s\n", getAddress(), cmd.getRecipient(), cmd);
        // Se for para o Agente, processa o comando, se for para algum de seus componentes, rotear
        String[] str = cmd.getRecipient().split("/");
        if (str.length == 3) {
        	processControlCommand(cmd);
        } 
        else if (str.length > 3) {
        	if (components.containsKey(str[3])) {
        		MusicalAgentComponent comp = components.get(str[3]);
        		comp.receiveCommand(cmd);
        	} else {
        		System.out.println("[" + getAddress() +"] Component '" + str[3] + "' does not exist");
        	}
        }
	}
	
	//--------------------------------------------------------------------------------
	// Agent Message Handling (CommMsg)
	//--------------------------------------------------------------------------------

	/**
	 * Responsible for validating the commands and their obligatory arguments and executing it.
	 *
	 * @param cmd the cmd
	 */
	protected final void processControlCommand(Command cmd) {

		String command = cmd.getCommand();
		// Registro efetuado com sucesso
		if (command.equals(Constants.CMD_ADD_COMPONENT)) {
			
			String compName = cmd.getParameter("NAME");
			String compClass = cmd.getParameter("CLASS");
			Parameters parameters = cmd.getUserParameters();
			if (compName != null && compClass != null) {
				addComponent(compName, compClass, parameters);
			} else {
				System.out.println("[" + this.getAgentName() + "] Command " + Constants.CMD_ADD_COMPONENT + " does not have obligatory arguments (NAME, CLASS)");
			}

		}
		else if (command.equals(Constants.CMD_REMOVE_COMPONENT)) {
			
			String compName = cmd.getParameter("NAME");
			if (compName != null) {
				removeComponent(compName);
			} else {
				System.out.println("[" + this.getAgentName() + "] Command " + Constants.CMD_ADD_COMPONENT + " does not have obligatory arguments (NAME)");
			}
			
		}
		else if (command.equals(Constants.CMD_AGENT_READY_ACK)) {
			// No caso de processamento BATCH
			if (isBatch) {
				// Programa os raciocínios para despertarem no turno indicado
				double value = Double.valueOf(cmd.getParameter(Constants.PARAM_TURN));
				long turn = (long)value;
				for (Enumeration<MusicalAgentComponent> e = components.elements() ; e.hasMoreElements() ;) {
					MusicalAgentComponent comp = e.nextElement();
					if (comp instanceof Reasoning) {
						((Reasoning)comp).setWakeUp(turn);
					}
				}
				// Inicia a checagem de fim de turno
				addBehaviour(new CheckEndTurn(getAgent()));
			}
			state = MA_STATE.REGISTERED;
		}
		// Confirmação de registro do Atuador/Sensor
		// TODO Agente Musical deve passar os parâmetros para os EventHandler, e não tratar aqui!!!
		else if (command.equals(Constants.CMD_EVENT_REGISTER_ACK)) {
			String componentName 		= cmd.getParameter(Constants.PARAM_COMP_NAME);
			String eventExecution 		= cmd.getParameter(Constants.PARAM_EVT_EXECUTION);
			Parameters serverParameters = cmd.getParameters();
			Parameters extraParameters 	= cmd.getUserParameters();

			// Avisar o componente sobre o registro
			EventHandler comp = (EventHandler)components.get(componentName);
			comp.confirmRegistration(eventExecution, serverParameters, extraParameters);

			// Warns all Reasonings about the new EventHandler
			for (MusicalAgentComponent existingComp : components.values()) {
				if (existingComp instanceof Reasoning) {
					try {
						((Reasoning)existingComp).eventHandlerRegistered((EventHandler)comp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

//			MusicalAgent.logger.info("[" + getAgentName() + "] " + "EventHandler '" + comp.getComponentName() + "' registered");

			
		} else if (command.equals(Constants.CMD_EVENT_DEREGISTER_ACK)) {
			
			String componentName 		= cmd.getParameter(Constants.PARAM_COMP_NAME);

			// Avisar o componente sobre o deregistro
			EventHandler comp = (EventHandler)components.get(componentName);

			// Informs all reasonings about the deregistration 
			for (MusicalAgentComponent existingComp : components.values()) {
				if (existingComp instanceof Reasoning) {
					try {
						((Reasoning)existingComp).eventHandlerDeregistered((EventHandler)comp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			comp.confirmDeregistration();
			
		}
		else if (command.equals(Constants.CMD_KILL_AGENT) || 
				command.equals(Constants.CMD_DESTROY_AGENT)) {

			if (isBatch) {
				dieNextTurn = true;
			} else {
				this.state = MA_STATE.TERMINATING;
				// Enviar mensagem para o console
				cmd = new Command(getAddress(), "/console", "UPDATE");
				cmd.addParameter("AGENT", getAgent().getAgentName());
				cmd.addParameter("STATE", "TERMINATING");
				sendCommand(cmd);
				// Deregisters componentes
				for (MusicalAgentComponent existingComp : components.values()) {
					removeComponent(existingComp.getComponentName());
				}
				// Wait until everything is deregistered
				addBehaviour(new CheckDeregister(this));
			}
			
		}
		else if (command.equals(Constants.CMD_PARAMETER)) {
			String param = cmd.getParameter("NAME");
			String value = cmd.getParameter("VALUE");
			if (param != null && value != null && parameters.containsKey(param)) {
				// TODO Alguns parâmetros não podem ser mudados!
				// Calls user method
				if (!parameterUpdate(param, value)) {
					return;
				}
				parameters.put(param, value);
				// Sends a message to the console 
				cmd = new Command(getAddress(), "/console", "UPDATE");
				cmd.addParameter("AGENT", getAgent().getAgentName());
				cmd.addParameter("NAME", param);
				cmd.addParameter("VALUE", value);
				sendCommand(cmd);
			}
		}
		else {
			processCommand(cmd);
		}

	
	}
	
	//--------------------------------------------------------------------------------
	// 
	//--------------------------------------------------------------------------------

	/**
	 * Classe interna que verifica se todos os componentes estão inicializados e registrados.
	 */
	// TODO NÃO existe um timeout para o registro dos componentes, no caso de não existir um ES compatível
	private final class CheckRegister extends CyclicBehaviour {

		/**
		 * Instantiates a new check register.
		 *
		 * @param a the a
		 */
		public CheckRegister(Agent a) {
			super(a);
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			if (numberEventHandlersRegistered == numberEventHandlersRequest) {
				// Envia um OK para Ambiente
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_AGENT_READY);
				sendCommand(cmd);
				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);
			}
			
		}
		
	}

	/**
	 * The Class CheckEndTurn.
	 */
	private final class CheckEndTurn extends OneShotBehaviour {
		
		/** The a. */
		Agent a;
		
		/**
		 * Instantiates a new check end turn.
		 *
		 * @param a the a
		 */
		public CheckEndTurn(Agent a) {
			super(a);
			this.a = a;
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			
			// Caso todos tenham terminado o processamento, envia a mensagem para o Ambiente
			if (numberReasoning == numberReasoningReady) {
			
				if (dieNextTurn) {
					// TODO Teria que deregistrar também os raciocínios do próximo turno!!!!
					// Agenda a morte do Agente para o próximo turno
					state = MA_STATE.TERMINATING;
					// Deregister all eventHandlers
					getAgent().getClock().schedule(getAgent(), new Deregister(), (long)getAgent().getClock().getCurrentTime(TimeUnit.TURNS) + 1);
					// Wait until everything is deregistered
					addBehaviour(new CheckDeregister(a));
				} else {
					// Schedules reasonings for the next turn
					long next_turn = (long)getClock().getCurrentTime(TimeUnit.TURNS) + 1; 
					for (Enumeration<MusicalAgentComponent> e = components.elements() ; e.hasMoreElements() ;) {
						MusicalAgentComponent comp = e.nextElement();
						if (comp instanceof Reasoning) {
							((Reasoning)comp).setWakeUp(next_turn);
						}
					}
				}

				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_BATCH_TURN);
				cmd.addParameter(Constants.PARAM_NUMBER_EVT_SENT, Integer.toString(numberEventsSent));
				sendCommand(cmd);

				numberReasoningReady = 0;
				numberEventsSent = 0;
				
//				MusicalAgent.logger.info("[" + getAgentName() + "] " + "Enviei fim de turno");
				
			}
			
		}
	
	}

	/**
	 * Classe interna que verifica se todos os componentes estão inicializados e registrados.
	 */
	// TODO NÃO existe um timeout para o registro dos componentes, no caso de não existir um ES compatível
	private final class CheckDeregister extends CyclicBehaviour {

		/**
		 * Instantiates a new check deregister.
		 *
		 * @param a the a
		 */
		public CheckDeregister(Agent a) {
			super(a);
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			if (numberEventHandlersRegistered == 0) {
				// Informs the EA that the agent is being deregistered
				sendCommand(new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_AGENT_DEREGISTER));
				// Kills the Musical Agent
				myAgent.addBehaviour(new KillAgent());
				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);
			}
			
		}
		
	}

	/**
	 * Verifica se todos os componentes estão finalizados e registrados.
	 */
	private final class Deregister implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// Deregisters EventHandlers
			for (MusicalAgentComponent existingComp : components.values()) {
				if (existingComp instanceof EventHandler) {
					try {
						((EventHandler)existingComp).deregister();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	/**
	 * Kills this agent.
	 */
	private final class KillAgent extends OneShotBehaviour {

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		@Override
		public void action() {
			
//			MusicalAgent.logger.info("[" + getAgent().getAgentName() + " Killing agent '" + getAgent().getAgentName() + "'");
			// Calls JADE finalization method
			doDelete();
			
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Agent 
	//--------------------------------------------------------------------------------
	/**
	 * Event handler registered.
	 *
	 * @param compName the comp name
	 */
	protected final synchronized void eventHandlerRegistered(String compName) {
		
		numberEventHandlersRegistered++;
		
//		MusicalAgent.logger.info("[" + this.getAgentName() + "] " + "Component " + compName + " registered");

	}
	
	/**
	 * Event handler deregistered.
	 *
	 * @param compName the comp name
	 */
	protected final synchronized void eventHandlerDeregistered(String compName) {
		
		// Removes the event handler and terminates it
		MusicalAgentComponent comp = components.remove(compName);
		comp.stop();
		
		numberEventHandlersRegistered--;
		
//		MusicalAgent.logger.info("[" + this.getAgentName() + "] " + "Component " + compName + " deregistered");

	}

	/**
	 * Event sent.
	 */
	protected final synchronized void eventSent() {
		
		numberEventsSent++;
		
	}
	
	// TODO Se não tiver reasoning nenhum, ele deveria mandar um fim de turno imediatamente (CyclicBehaviour igual ao de eventso?)
	/**
	 * Reasoning process done.
	 *
	 * @param reasoningName the reasoning name
	 */
	protected final synchronized void reasoningProcessDone(String reasoningName) {
		
		numberReasoningReady++;
		
		// Checar o fim de turno
		addBehaviour(new CheckEndTurn(this));

	}

}
