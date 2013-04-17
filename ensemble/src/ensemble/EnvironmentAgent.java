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
import ensemble.clock.TimeUnit;
import ensemble.world.World;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

// TODO: Auto-generated Javadoc
/**
 * An agent that represents the environment.
 * 
 * @author Leandro Ferrari Thomaz
 * 
 */
public class EnvironmentAgent extends EnsembleAgent {

	// ----------------------------------------------
	// General variables
	// ----------------------------------------------

	/** Estado do Agente Ambiente. */
	private EA_STATE state = EA_STATE.CREATED;

	/** The dfd. */
	protected DFAgentDescription dfd;

	/** The tbf. */
	ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	/** Lock. */
	private Lock lock = new ReentrantLock();
	
	/** The batch lock. */
	private Lock batchLock = new ReentrantLock();

	/** Descrição do Mundo Virtual. */
	protected World world = null;

	/** Event Servers registrados (por tipo de evento). */
	protected ConcurrentHashMap<String, EventServer> eventServers = new ConcurrentHashMap<String, EventServer>();

	/** Tabela de fatos públicos dos agentes (Fenótipo). */
	// TODO Faz mais sentido estar no World
	public ConcurrentHashMap<String, String> agentsPublicFacts = new ConcurrentHashMap<String, String>();

	/** Contador para manter nome dos agentes único. */
	private int numberCreatedAgents;

	// ----------------------------------------------
	// Batch processing control variables
	// ----------------------------------------------

	/** The last update time. */
	private long lastUpdateTime = System.currentTimeMillis();
	
	/** Tempo mínimo de espera entre cada turno. */
	private long waitTimeTurn;
	
	/** Controla se deve esperar todos os agentes serem criados para iniciar a simulação. */
	private boolean waitAllAgents;
	
	/** Número inicial de Agentes Musicais. */
	private long initialAgents;
	
	/** Número de agentes registrados no Ambiente. */
	private int registeredAgents;
	
	/** Número de agentes registrados no Ambiente para o próximo turno. */
	private int registeredAgentsNextTurn;
	
	/** Número de agentes registrados que finalizaram suas ações no turno atual. */
	private int registeredAgentsReady;
	
	/** Número de eventos enviados pelos Agentes Musicais no turno atual. */
	private int agentEventsSent;
	
	/** Número de eventos processados pelos EventServers no turno atual. */
	private int agentEventsProcessed;
	
	/** Número de eventos enviados pelos EventServers para os Agentes no turno atual. */
	private int evtSrvEventsSent;
	
	/** Número de eventos processados pelos Agentes Musicais no turno atual. */
	private int evtSrvEventsProcessed;

	// --------------------------------------------------------------------------------
	// Agente getters / setters
	// --------------------------------------------------------------------------------

	/**
	 * Gets the eA state.
	 *
	 * @return the eA state
	 */
	public final EA_STATE getEAState() {
		return state;
	}
	
	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public final World getWorld() {
		return world;
	}

	/**
	 * Gets the event server.
	 *
	 * @param eventType the event type
	 * @return the event server
	 */
	public final EventServer getEventServer(String eventType) {
		return eventServers.get(eventType);
	}

	// --------------------------------------------------------------------------------
	// Initialization
	// --------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#start()
	 */
	@Override
	public boolean start() {

//		logger.info("[" + getAgentName() + "] " + "Starting initialization...");
		// System.out.println("EA start()");

		// Sends a message to the console
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		lock.lock();
		try {

			// Obtém as propriedades da simulação
			waitAllAgents = Boolean.valueOf(getParameters().get(
					Constants.WAIT_ALL_AGENTS, "TRUE"));
			waitTimeTurn = Long.valueOf(getParameters().get(
					Constants.WAIT_TIME_TURN, "100"));

			// If no world has been configured, creates a generic world
			if (world == null) {
				addWorld("ensemble.world.World", new Parameters());
			}
			world.start();

			// Executa o método de inicialização do usuário
			init();

			// Inicia os EventServers
			Collection<EventServer> servers = eventServers.values();
			for (Iterator<EventServer> iterator = servers.iterator(); iterator
					.hasNext();) {
				EventServer eventServer = iterator.next();
				eventServer.start();
			}

			// Registra o Ambiente no DS
			this.registerService(getAgentName(), Constants.ENVIRONMENT_AGENT);

			state = EA_STATE.INITIALIZED;

//			logger.info("[" + getAgentName() + "] " + "Initialized");
//			 System.out.println("[" + this.getAID().getAgentName() + "] " + "Initialized");

			cmd = new Command(getAddress(), "/console", "UPDATE");
			cmd.addParameter("AGENT", getAgent().getAgentName());
			cmd.addParameter("STATE", "INITIALIZED"); 
			sendCommand(cmd);

		} finally {
			lock.unlock();
		}

		// Inicia a simulação
		// Caso solicitado, aguarda a criação de todos os agentes (para
		// processamento Batch)
		if (isBatch) {
			if (waitAllAgents) {
				// TODO Timeout para o caso de algum agente travar na
				// inicialização
				this.addBehaviour(new CheckRegister(this));
			} else {
				this.addBehaviour(new CheckEndTurn(this));
			}
		}

		return true;

	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public boolean stop() {

		// Calls the user implemented finalization method
		finit();

		// Stops EventServers
		for (EventServer es : eventServers.values()) {
			es.stop();
		}

		// Stops World
		world.stop();

		// Warns the Sniffer
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		sendCommand(cmd);

		return true;
	}

	/**
	 * Adds the world.
	 *
	 * @param className the class name
	 * @param parameters the parameters
	 */
	public void addWorld(String className, Parameters parameters) {
		if (state == EA_STATE.CREATED) {
			try {
				Class worldClass = Class.forName(className);
				world = (World) worldClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
			world.setParameters(parameters);
			world.setEnvAgent(this);
			world.configure();
		} else {
			System.err.println("[" + getAgentName() + "] Trying to add a World in runtime!");
		}
	}
	
	/**
	 * Registra um tipo de evento tratado por este Agente Ambiente no diretório
	 * do JADE.
	 *
	 * @param name nome do EventServer que trata o evento
	 * @param type tipo do evento
	 */
	protected final void registerService(String name, String type) {

		ServiceDescription sd = new ServiceDescription();
		sd.setName(name);
		sd.setType(type);

		try {
			if (dfd == null) {
				dfd = new DFAgentDescription();
				dfd.setName(this.getAID());
				dfd.addServices(sd);
				DFService.register(this, dfd);
			} else {
				dfd.addServices(sd);
				DFService.modify(this, dfd);
			}
//			logger.info("[" + getAgentName() + "] " + "Event type " + type + " registered in the DS");
		} catch (FIPAException fe) {
			System.out
					.println("ERROR: It was not possible to register the service '"
							+ type + "'");
			System.out.println(fe.toString());
		}

	}

	/**
	 * Cria uma instância de um EventServer esporádico ou frequente.
	 *
	 * @param className classe Java do EventServer
	 * @param esParam the es param
	 */
	public final void addEventServer(String className, Parameters esParam) {

		lock.lock();
		try {
			try {
				// Criar instância do EventServer
				Class esClass = Class.forName(className);
				EventServer es = (EventServer) esClass.newInstance();
				// Configurar o EventServer
				es.setEnvAgent(this);
				es.setParameters(esParam);
				es.configure();
				// Adicionar na tabela
				if (eventServers.containsKey(es.getEventType())) {
					System.err.println("ERROR: There is already an Event Server with event type "
									+ es.getEventType());
					return;
				} else {
					eventServers.put(es.getEventType(), es);
				}
				// Caso o Agente Ambiente já tiver sido inicializado,
				// inicializar o EventServer
				if (state == EA_STATE.INITIALIZED) {
					es.start();
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.err
						.println("ERROR: Not possible to create an instance of "
								+ className);
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.err
						.println("ERROR: Not possible to create an instance of "
								+ className);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				System.err
						.println("ERROR: Not possible to create an instance of "
								+ className);
			}
		} finally {
			lock.unlock();
		}

	}

	/**
	 * Removes an EventServer from the EnvironmentAgent.
	 *
	 * @param eventType the event type
	 */
	public final void removeEventServer(String eventType) {

		if (eventServers.containsKey(eventType)) {
			EventServer server = eventServers.remove(eventType);
			server.stop();
		} else {
			System.err.println("[" + getAgentName() + "] Event server "
					+ eventType + " does not exist.");
		}

	}

	// --------------------------------------------------------------------------------
	// Message handling
	// --------------------------------------------------------------------------------

	// /**
	// * Envia um comando a um agente
	// * @param receiver agente destino do comando
	// * @param command comando a ser enviado
	// */
	// public final void sendMessage(String receiver, Command command) {
	//
	// String[] str = new String[1];
	// str[0] = receiver;
	// sendMessage(str, command);
	//
	// }

	// /**
	// * Envia um comando a mais de um agente
	// * @param receiver agentes destino do comando
	// * @param command comando a ser enviado
	// */
	// protected final void sendMessage(String[] receiver, Command command) {
	//
	// ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	// String receivers = "";
	// for (int i = 0; i < receiver.length; i++) {
	// msg.addReceiver(new AID(receiver[i], AID.ISLOCALNAME));
	// receivers = receivers + receiver[i] + " ";
	// }
	// msg.setConversationId("CommMsg");
	// msg.setContent(command.toString());
	// this.send(msg);
	//
	// MusicalAgent.logger.info("[" + getAgentName() + "] " + "Message sent to "
	// + receivers + "(" + msg.getContent() + ")");
	//
	// }
	//
	/**
	 * Process control command.
	 *
	 * @param cmd the cmd
	 */
	protected final void processControlCommand(Command cmd) {

		String command = cmd.getCommand();
		
		if (command.equals(Constants.CMD_START_SIMULATION)) {

			// TODO Deve habilitar todos os start(), inclusive dos agentes musicais!
			
		} else if (command.equals(Constants.CMD_STOP_SIMULATION)) {

			this.state = EA_STATE.TERMINATING;
			// Destroy Agents
			for (String agent : getWorld().getEntityList()) {
				destroyAgent(agent);
			}
			// Wait until everything is deregistered
			addBehaviour(new CheckAgentDeregister());
			
		} else if (command.equals(Constants.CMD_CREATE_AGENT)) {

			String agentName = cmd.getParameter("NAME");
			String agentClass = cmd.getParameter("CLASS");
			Parameters parameters = cmd.getUserParameters();
			createMusicalAgent(agentName, agentClass, parameters);

		} else if (command.equals(Constants.CMD_DESTROY_AGENT)) {

			String agentName = cmd.getParameter("NAME");
			destroyAgent(agentName);

		} else if (command.equals(Constants.CMD_ADD_EVENT_SERVER)) {

			String className = cmd.getParameter("NAME");
			Parameters parameters = cmd.getUserParameters();
			addEventServer(className, parameters);

		} else if (command.equals(Constants.CMD_REMOVE_EVENT_SERVER)) {

			String esName = cmd.getParameter("NAME");
			removeEventServer(esName);

		} else if (command.equals(Constants.CMD_EVENT_REGISTER)) {

			String componentName = cmd.getParameter(Constants.PARAM_COMP_NAME);
			String eventHandlerType = cmd
					.getParameter(Constants.PARAM_COMP_TYPE);
			String eventType = cmd.getParameter(Constants.PARAM_EVT_TYPE);
			Parameters userParam = cmd.getUserParameters();

			// Repassar pedido de registro para o EventServer responsável
			EventServer evtServer = eventServers.get(eventType);
			if (evtServer != null) {
				String sender = cmd.getParameter("sender").split("/")[2];
				evtServer.registerEventHandler(sender, componentName,
						eventHandlerType, userParam);
			} else {
//				logger.info("[" + getAgentName() + "] " + "EventServer " + eventType + " not found");
			}

		} else if (command.equals(Constants.CMD_EVENT_DEREGISTER)) {

			String sender = cmd.getParameter("sender").split("/")[2];
			String componentName = cmd.getParameter(Constants.PARAM_COMP_NAME);
			String eventHandlerType = cmd
					.getParameter(Constants.PARAM_COMP_TYPE);
			String eventType = cmd.getParameter(Constants.PARAM_EVT_TYPE);

			// Repassar pedido de registro para o EventServer respons�vel
			EventServer evtServer = eventServers.get(eventType);
			if (evtServer != null) {
				evtServer.deregisterEventHandler(sender, componentName,
						eventHandlerType);
			}

		} else if (command.equals(Constants.CMD_AGENT_REGISTER)) {

			String sender = cmd.getParameter("sender").split("/")[2];
//			MusicalAgent.logger.info("[" + getAgentName() + "] " + "Recebi pedido de registro de " + sender);
			registerAgent(sender, cmd.getParameters());

		} else if (command.equals(Constants.CMD_AGENT_DEREGISTER)) {

			String sender = cmd.getParameter("sender").split("/")[2];
//			MusicalAgent.logger.info("[" + getAgentName() + "] " + "Recebi pedido de desregistro de " + sender);
			deregisterAgent(sender);

		} else if (command.equals(Constants.CMD_AGENT_READY)) {

			String sender = cmd.getParameter("sender").split("/")[2];
//			MusicalAgent.logger.info("[" + getAgentName() + "] " + "Agent " + sender + " ready for the simulation");
			prepareAgent(sender);

		} else if (command.equals(Constants.CMD_BATCH_TURN)) {

			if (isBatch) {
				String sender = cmd.getParameter("sender").split("/")[2];
//				MusicalAgent.logger.info("[" + getAgentName() + "] " + "End of turn message received from " + sender);
				int numberEventsSent = Integer.valueOf(cmd.getParameter(Constants.PARAM_NUMBER_EVT_SENT));
				agentProcessed(numberEventsSent);
			}

		} else if (command.equals(Constants.CMD_BATCH_EVENT_ACK)) {

			if (isBatch) {
				eventAgentProcessed();
			}

		} else if (command.equals(Constants.CMD_PUBLIC_FACT_UPDATE)) {

			String sender = cmd.getParameter("sender").split("/")[2];
			String fact = cmd.getParameter(Constants.PARAM_FACT_NAME);
			String value = cmd.getParameter(Constants.PARAM_FACT_VALUE);
			agentsPublicFacts.put(sender + ":" + fact, value);

		} else if (command.equals(Constants.CMD_PARAMETER)) {
			String param = cmd.getParameter("NAME");
			String value = cmd.getParameter("VALUE");
			if (param != null && value != null && parameters.containsKey(param)) {
				// TODO Alguns parâmetros não podem ser mudados!
				if (!parameterUpdate(param, value)) {
					return;
				}
				parameters.put(param, value);
				// Calls user method
				// Sends a message to the console 
				cmd = new Command(getAddress(), "/console", "UPDATE");
				cmd.addParameter("AGENT", getAgent().getAgentName());
				cmd.addParameter("NAME", param);
				cmd.addParameter("VALUE", value);
				sendCommand(cmd);
			}

		} else {

			processCommand(cmd);

		}

	}

	// /**
	// * Behaviour cíclico interno do agente responsável por receber e tratar as
	// mensagens enviadas ao ambiente pelos agentes musicais.
	// */
	// private final class ReceiveMessages extends CyclicBehaviour {
	//
	// MessageTemplate mt;
	//
	// public ReceiveMessages(Agent a) {
	// super(a);
	// mt = MessageTemplate.MatchConversationId("CommMsg");
	// }
	//
	// public void action() {
	//
	// ACLMessage msg = myAgent.receive(mt);
	// if (msg != null) {
	//
	// MusicalAgent.logger.info("[" + getAgentName() + "] " +
	// "Message received from " + msg.getSender().getLocalName() + " (" +
	// msg.getContent() + ")");
	// String sender = msg.getSender().getLocalName();
	// Command cmd = Command.parse(msg.getContent());
	// // TODO Podemos criar aqui uma thread, assim tratamos msgs em paralelo
	// if (cmd != null) {
	// processMessage(sender, cmd);
	// }
	//
	// } else {
	//
	// block();
	//
	// }
	//
	// }
	//
	// }

	// ----------------------------------------------
	// Command Interface
	// ----------------------------------------------

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public final void receiveCommand(Command cmd) {

//		 System.out.printf("[%s] Command received: %s - %s\n", getAddress(), cmd.getRecipient(), cmd);
		// Se for para o Agente, processa o comando, se for para algum de seus
		// componentes, rotear
		String[] str = cmd.getRecipient().split("/");
		if (str.length == 3) {
			// TODO Aqui deve ver se é um comando que ele pode processar, senão,
			// passa para o processCommand()
			processControlCommand(cmd);
		} else if (str.length > 3) {
			if (str[3].equals(Constants.WORLD)) {
				getWorld().receiveCommand(cmd);
			}
			else if (eventServers.containsKey(str[3])) {
				EventServer es = eventServers.get(str[3]);
				// Se for mudança de parâmetros, faz diretamente, caso contrário
				// envia o comando para o componente
				// if (cmd.getCommand().equals(Constants.CMD_PARAM)) {
				// String param = cmd.getParameter("NAME");
				// String value = cmd.getParameter("VALUE");
				// if (param != null && value != null) {
				// comp.addParameter(param, value);
				// comp.parameterUpdated(param);
				// }
				// }
				// else {
				es.receiveCommand(cmd);
				// }
			} else {
				System.out.println("[" + getAddress() + "] Event Server '"
						+ str[3] + "' does not exist");
			}
		}

	}

	// --------------------------------------------------------------------------------
	// Agent management (create, destroy, register, deregister)
	// --------------------------------------------------------------------------------

	/**
	 * Cria um novo Agente no Ambiente. Se o nome do agente é null ou vazio,
	 * cria um nome sequencial, baseado na classe.
	 *
	 * @param agentName the agent name
	 * @param agentClass the agent class
	 * @param parameters the parameters
	 * @return the string
	 */
	public final String createMusicalAgent(String agentName, String agentClass,
			Parameters parameters) {

		Object[] arguments = null;
		if (parameters != null) {
			arguments = new Object[1];
			arguments[0] = parameters;
		}

		if (agentName == null || agentName.equals("")) {
			numberCreatedAgents++;
			agentName = new String(agentClass + "_" + numberCreatedAgents);
		}

		try {
			ContainerController cc = getContainerController();
			Class maClass = Class.forName(agentClass);
			MusicalAgent ma = (MusicalAgent) maClass.newInstance();
			ma.setArguments(arguments);
			AgentController ac = cc.acceptNewAgent(agentName, ma);
			ac.start();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

//		logger.info("[" + getAgentName() + "] " + "Created a new agent named " + agentName);

		return agentName;

	}

	/**
	 * Kills a musical agent.
	 *
	 * @param agentName Musical Agent's name
	 */
	public final void destroyAgent(String agentName) {

		Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME
				+ "/" + agentName, Constants.CMD_KILL_AGENT);
		sendCommand(cmd);

		// MusicalAgent.logger.info("[" + this.getAID().getAgentName() +
		// "] Sent KILL_AGENT to " + agentName);

	}

	/**
	 * Registra um agente musical no Ambiente.
	 *
	 * @param agentName the agent name
	 * @param parameters the parameters
	 */
	protected final void registerAgent(String agentName, Parameters parameters) {

		// Adiciona o agente ao mundo virtual
		world.addEntity(agentName, parameters);

	}

	/**
	 * Prepare agent.
	 *
	 * @param agentName the agent name
	 */
	protected final void prepareAgent(String agentName) {

		Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME
				+ "/" + agentName, Constants.CMD_AGENT_READY_ACK);

		// No caso de processamento BATCH
		if (isBatch) {

			batchLock.lock();
			try {
				// Adicionar Agente à lista de registrados para começar no
				// próximo turno
				registeredAgentsNextTurn++;

				// Enviar mensagem de ACK, indicando que o agente deve acordar
				// no próximo turno
				cmd.addParameter(
						Constants.PARAM_TURN,
						String.valueOf((getClock().getCurrentTime(
								TimeUnit.TURNS) + 1)));

				this.addBehaviour(new CheckEndTurn(this));

			} finally {
				batchLock.unlock();
			}

		}

		// Envia a resposta
		// sendMessage(agentName, cmd);
		sendCommand(cmd);

	}

	/**
	 * Retira um agente musical do registro de agentes ativos no ambiente.
	 *
	 * @param agentName the agent name
	 */
	protected final void deregisterAgent(String agentName) {

		if (isBatch) {

			batchLock.lock();
			try {

				// Retirar Agente da lista de registrados
				registeredAgents--;
				registeredAgentsNextTurn--;

				this.addBehaviour(new CheckEndTurn(this));

			} finally {
				batchLock.unlock();
			}

		}

		world.removeEntity(agentName);

	}

	/**
	 * Checks if all Musical Agents were destroyed.
	 */
	private final class CheckAgentDeregister extends CyclicBehaviour {

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {

			if (world.getEntityList().size() == 0) {

				myAgent.addBehaviour(new KillAgent());
				
				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);

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
			
//			MusicalAgent.logger.info("[" + getAgent().getName() + " Killing agent '" + getAgent().getAgentName() + "'");
			// Calls JADE finalization method
			doDelete();
			
		}
		
	}
	
	// --------------------------------------------------------------------------------
	// Batch Mode related methods
	// --------------------------------------------------------------------------------

	/**
	 * Classe interna responsável por verificar se uma simulação pode ser
	 * iniciada, ou seja, se todos os agentes estão prontos.
	 */
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

			// Enquanto não tiver nenhum agente registrado para o próximo turno,
			// continua checando ou atualiza o clock
			if (initialAgents == registeredAgents) {

				// Atualiza o clock virtual
				myAgent.addBehaviour(new CheckEndTurn(myAgent));

				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);

			}

		}

	}

	/**
	 * Classe internar responsável por verificar se todos os agentes já agiram
	 * no turno atual.
	 * 
	 * @author lfthomaz
	 * 
	 */
	private final class CheckEndTurn extends OneShotBehaviour {

		/**
		 * Instantiates a new check end turn.
		 *
		 * @param a the a
		 */
		public CheckEndTurn(Agent a) {
			super(a);
		}

		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {

			batchLock.lock();
			try {
				// TODO Aguardar um n�mero de agentes pr�-definidos estarem
				// registrados
				// Aguarda a finaliza��o dos Agentes Musicais e o processamento
				// de todos os eventos pelos EventServers
				if (// registeredAgentsNextTurn > 0 &&
				registeredAgentsReady >= registeredAgents
						&& agentEventsProcessed >= agentEventsSent) {

					// Inicia o processamento dos EventServer (para atualizar o
					// ambiente)
					// TODO tornar o processamento paralelo ou n�o �
					// necessario??
					// TODO o usu�rio pode escolher a ordem em que os
					// EventServers ser�o processados!!!
					for (Enumeration<EventServer> e = eventServers.elements(); e.hasMoreElements();) {
						EventServer evtServer = e.nextElement();
						try {
							evtServer.process();
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}

					// Atualizar as vari�veis de controle
					registeredAgents = registeredAgentsNextTurn;
					registeredAgentsReady = 0;
					agentEventsSent = 0;
					agentEventsProcessed = 0;
					evtSrvEventsSent = 0;
					evtSrvEventsProcessed = 0;

					// Chama um processo do usu�rio antes de mudar o clock (bom
					// para atualizar o GUI)
					preUpdateClock();

					// Deve aguardar os ACKs dos agentes, caso tenha enviado
					// eventos
					// TODO M�quina de estados?!!??

					// Suspende a simula��o por um tempo
					// TODO poder� ser programado pelo usu�rio, para ter um
					// tempo m�nimo para atualizar o turno
					long elapsedTime = System.currentTimeMillis()
							- lastUpdateTime;
					if (elapsedTime < waitTimeTurn) {
						try {
							Thread.sleep(waitTimeTurn - elapsedTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// Se n�o existe nenhum agente registrado para o pr�ximo
					// turno, j� pode agendar um novo CheckEndTurn
					if (isBatch && registeredAgentsNextTurn == 0) {
						addBehaviour(new CheckEndTurn(myAgent));
					}

					// Atualiza o clock virtual
					getClock().updateClock(1);

					lastUpdateTime = System.currentTimeMillis();
				}
			} finally {
				batchLock.unlock();
			}
		}

	}

	/**
	 * Registra a mensagem de um agente indicando que terminou o turno.
	 *
	 * @param events the events
	 */
	private final void agentProcessed(int events) {
		batchLock.lock();
		try {
			registeredAgentsReady++;
			agentEventsSent = agentEventsSent + events;
			this.addBehaviour(new CheckEndTurn(this));
		} finally {
			batchLock.unlock();
		}

	}

	/**
	 * Registra a informação do EventServer que um evento foi processado.
	 */
	public final void eventProcessed() {

		batchLock.lock();
		try {
			agentEventsProcessed++;

			this.addBehaviour(new CheckEndTurn(this));
		} finally {
			batchLock.unlock();
		}

	}

	/**
	 * Registra que um evento foi enviado pelo EventServer.
	 */
	public final void eventSent() {

		batchLock.lock();
		try {
			// Incrementar contador de eventos enviados pelo EventServer
			evtSrvEventsSent++;
		} finally {
			batchLock.unlock();
		}

	}

	/**
	 * Registra que um Agente terminou de processar.
	 */
	private final void eventAgentProcessed() {

		batchLock.lock();
		try {
			evtSrvEventsProcessed++;

			this.addBehaviour(new CheckEndTurn(this));
		} finally {
			batchLock.unlock();
		}

	}

	// --------------------------------------------------------------------------------
	// User implemented methods
	// --------------------------------------------------------------------------------

	/**
	 * Método executado pelo Ambiente, quando em modo BATCH, imediatamente antes
	 * de alterar o turno.
	 */
	protected void preUpdateClock() {
	}

}
