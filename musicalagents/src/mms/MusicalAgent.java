package mms;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import mms.Constants.MA_STATE;
import mms.clock.TimeUnit;
import mms.commands.Command;
import mms.commands.CommandClientInterface;
import mms.kb.KnowledgeBase;
import mms.world.Vector;
//import mms.osc.OSCServerHelper;
//import mms.osc.OSCServerService;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MusicalAgent extends MMSAgent {

	//--------------------------------------------------------------------------------
	// Agent attributes
	//--------------------------------------------------------------------------------
	
	ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	/**
	 *  Lock
	 */
	private Lock lock = new ReentrantLock();

	/**
	 * EnvironmentAgent's name
	 */
	protected String environmentAgent;
	
	/**
	 * Components List
	 */
	private ConcurrentHashMap<String, MusicalAgentComponent> components = new ConcurrentHashMap<String, MusicalAgentComponent>();
	
	/**
	 * Musical Agent's State
	 */
	public MA_STATE state = MA_STATE.CREATED;
	
	/**
	 * Musical Agent's Knowledge Base
	 */
	protected KnowledgeBase kb = new KnowledgeBase(this);
	
	// ---------------------------------------------- 
	// Batch processing control variables 
	// ---------------------------------------------- 

	/**
	 * Contador de raciocínios do agente
	 */
	private int numberReasoning = 0;
	/**
	 * Contador de raciocínios prontos
	 */
	private int numberReasoningReady = 0;
	/**
	 * Contador de eventos enviados
	 */
	private int numberEventsSent = 0;
	/**
	 * Contador de pedidos de registro de EventHandlers no momento da inicializção
	 */
	private int numberEventHandlersRequest = 0;
	/**
	 * Contador de EventHandlers já registrados
	 */
	private int numberEventHandlersRegistered = 0;
	/**
	 * Controle se o agente deve morrer no próximo turno
	 */
	private boolean dieNextTurn = false;
	
	//--------------------------------------------------------------------------------
	// Agent initialization and termination
	//--------------------------------------------------------------------------------
	
	/**
	 * Inicializa o Agente Musical
	 */
	protected final void start() {

		lock.lock();
		try {

			// Inicia a recepção de Mensagens de Controle 
			this.addBehaviour(tbf.wrap(new ReceiveMessages(this)));
	
			// Registrar-se no Ambiente (necessário tanto em BATCH como em REAL_TIME)
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(Constants.EVT_ENVIRONMENT);
			template.addServices(sd);
			try {
				boolean envFound = false;
				while (!envFound) {
					DFAgentDescription[] result = DFService.search(this, template);
					if (result.length == 1) {
						environmentAgent = result[0].getName().getLocalName();
						envFound = true;
						Command cmd = new Command(Constants.CMD_AGENT_REGISTER);
						sendMessage(cmd);
					} else {
						// TODO jeito ruim de ficar tentando registrar o Agente
						MusicalAgent.logger.info("[" + getAgent().getLocalName() + "] " + "Environment Agent not found! Trying again...");
						Thread.sleep(500);
					}
				}
			} catch (Exception fe) {
//				logger.severe("[" + this.getLocalName() + "] " + "Environment Agent not available");
				System.out.println("[" + this.getLocalName() + "] " + "Environment Agent not available");
				this.doDelete();
			}
			
			// TODO Registras os fatos públicos do KB
			
			// Executa o método de inicialização do usuário
			// TODO Tratar o caso retornar false
			init();
			
			// Inicializa os componentes
			Collection<MusicalAgentComponent> comps = components.values();
			for (Iterator<MusicalAgentComponent> iterator = comps.iterator(); iterator.hasNext();) {
				MusicalAgentComponent comp = iterator.next();
				boolean result = comp.start();
				if (!result) {
					System.out.println("[" + this.getLocalName() + "] Component '" + comp.getName() + "' not initialized");
					removeComponent(comp.getName());
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
			
			// Aguarda o registro de todos os EventHandlers, caso seja necessário
			this.addBehaviour(new CheckRegister(this));			
			
			// Fim da inicialização do Agente Musical
			state = MA_STATE.INITIALIZED;
//			logger.info("[" + this.getLocalName() + "] " + "Initialized");
			System.out.println("[" + this.getLocalName() + "] " + "Initialized");
			
		} finally {
			lock.unlock();
		}

	}
	
	@Override
	protected final void stop() {
		// Calls JADE finalization method
		this.doDelete();
	}

	/** 
	 * Adiciona um componente ao agente, seja um raciocínio, sensor, atuador etc. Deve configurar o componente e iniciar sua execução.
	 * @param compName
	 * @param className
	 * @param arguments
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
						compName.equals(Constants.COMP_SENSOR) || compName.equals(Constants.COMP_ANALYZER) || 
						compName.equals(Constants.COMP_REASONING) || compName.equals(Constants.COMP_KB)) {
					System.out.println("[" + this.getLocalName() + "] Component '" + compName + "' using a reserved name");
					return;
				}
				if (components.containsKey(compName)) {
					System.out.println("[" + this.getLocalName() + "] Component '" + compName + "' already exists");
					return;
				}
				
				// Configura o componente
				comp.setName(compName);
				comp.setAgent(this);
				comp.addParameters(arguments);
				comp.configure(arguments);

				// Sets the position of the EventHandler in Agent's "body"
				if (comp instanceof EventHandler) {
					if (arguments.containsKey(Constants.PARAM_POSITION)) {
						Vector position = Vector.parse(arguments.get(Constants.PARAM_POSITION));
						((EventHandler)comp).setPosition(position);
					}
				}
				
				// Adicionar o componente na tabela
				components.put(compName, comp);

				// Caso o Agente Ambiente já tiver sido inicializado, inicializar o componente
				if (state == MA_STATE.REGISTERED) {
					boolean result = comp.start();
					if (!result) {
						System.out.println("[" + this.getLocalName() + "] Component '" + compName + "' not initialized");
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
				
				// Warns the Reasoning about all EventHandlers present
				if (comp instanceof Reasoning) {
					for (MusicalAgentComponent existingComp : components.values()) {
						if (existingComp instanceof EventHandler) {
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
	 * Remove um componente do Agente
	 * @param name nome do componente a ser removido
	 */
	public final void removeComponent(String compName) {

		if (!components.containsKey(compName)) {
			System.out.println("[" + this.getLocalName() + "] " + "Component '" + compName + "' does not exists in Agent " + getAgentName());
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
			
			System.out.println("[" + this.getLocalName() + "] " + "Component '" + comp.getName() + "' removed");
			
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Agent Getters
	//--------------------------------------------------------------------------------

	public final MusicalAgent getAgent() {
		return this;
	}
	
	public final KnowledgeBase getKB() {
		return kb;
	}
	
	public final String getEnvironmentAgent() {
		return environmentAgent;
	}
	
	public final String getAgentName() {
		return getLocalName();
	}
	
	//--------------------------------------------------------------------------------
	// Agent Message Handling (CommMsg)
	//--------------------------------------------------------------------------------
	
	@Override
	public void input(CommandClientInterface cmdInterface, Command cmd) {
        System.out.println("[" + getAddress() +"] Command received: " + cmd);
        // Se for para o Agente, processa o comando, se for para algum de seus componentes, rotear
        String[] recipient = cmd.getRecipient().split(":");
        if (recipient.length == 2) {
        	processMessage(cmd.getSource(), cmd);
        } 
        else if (recipient.length == 3) {
        	if (components.containsKey(recipient[2])) {
        		MusicalAgentComponent comp = components.get(recipient[2]);
        		// Se for mudança de parâmetros, faz diretamente, caso contrário envia o comando para o componente
        		if (cmd.getCommand().equals(Constants.CMD_PARAM)) {
        			String param = cmd.getParameter("NAME");
        			String value = cmd.getParameter("VALUE");
        			if (param != null && value != null) {
        				comp.addParameter(param, value);
        				comp.parameterUpdated(param);
        			}
        		}
        		else {
        			comp.processCommand(cmd);
        		}
        	} 
        	else if (recipient[2].equals(Constants.COMP_KB) && cmd.getCommand().equals(Constants.CMD_FACT)) {
    			String fact = cmd.getParameter("NAME");
    			String value = cmd.getParameter("VALUE");
    			if (fact != null && value != null) {
    				getKB().updateFact(fact, value);
    			}
        	}
        	else {
        		System.out.println("[" + getAddress() +"] Component does not exist: " + recipient[2]);
        	}
        }
	}
	
	/**
	 * Responsible for validating the commands and their obligatory arguments and executing it.
	 * @param sender
	 * @param cmd
	 */
	protected final void processMessage(String sender, Command cmd) {

		String command = cmd.getCommand();
		// Registro efetuado com sucesso
		if (command.equals(Constants.CMD_ADD_COMPONENT)) {
			
			String compName = cmd.getParameter("NAME");
			String compClass = cmd.getParameter("CLASS");
			Parameters parameters = cmd.getParameters();
			if (compName != null && compClass != null) {
				addComponent(compName, compClass, parameters);
			} else {
				System.out.println("[" + this.getLocalName() + "] Command " + Constants.CMD_ADD_COMPONENT + " does not have obligatory arguments (NAME, CLASS)");
			}

		}
		else if (command.equals(Constants.CMD_REMOVE_COMPONENT)) {
			
			String compName = cmd.getParameter("NAME");
			if (compName != null) {
				removeComponent(compName);
			} else {
				System.out.println("[" + this.getLocalName() + "] Command " + Constants.CMD_ADD_COMPONENT + " does not have obligatory arguments (NAME)");
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

			MusicalAgent.logger.info("[" + getAgentName() + "] " + "EventHandler '" + comp.getName() + "' registered");

			
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
		else if (command.equals(Constants.CMD_KILL_AGENT)) {

			if (isBatch) {
				dieNextTurn = true;
			} else {
				this.state = MA_STATE.TERMINATING;
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
				// Wait until everything is deregistered
				addBehaviour(new CheckDeregister(this));
			}
			
		}
		else {
				
			System.out.println("[" + getLocalName() + "] " + "Command not recognized: " + command);
				
		}

	
	}

	/**
	 * Envia um comando para o Agente Ambiente
	 */
	public final void sendMessage(Command command) {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(environmentAgent, AID.ISLOCALNAME));
		msg.setConversationId("CommMsg");
		msg.setContent(command.toString());
		this.send(msg);
		MusicalAgent.logger.info("[" + this.getAID().getLocalName() + "] " + "Message sent to " + environmentAgent + " (" + msg.getContent() + ")");
		
	}

	/**
	 * Classe interna responsável por receber e tratar as mensagens
	 */
	private final class ReceiveMessages extends CyclicBehaviour {

		MessageTemplate mt;
		
		public ReceiveMessages(Agent a) {
			super(a);
			mt = MessageTemplate.MatchConversationId("CommMsg");
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				MusicalAgent.logger.info("[" + getAID().getLocalName() + "] " + "Message received from " + msg.getSender().getLocalName() + " (" + msg.getContent() + ")");

				// TODO switch com os poss�veis comandos
				String sender = msg.getSender().getLocalName();
				Command cmd = Command.parse(msg.getContent());
				if (cmd != null) {
					processMessage(sender, cmd);
				}
			}
			else {
				block();
			}
		}
	
	}
	
	/**
	 * Classe interna que verifica se todos os componentes estão inicializados e registrados
	 */
	// TODO NÃO existe um timeout para o registro dos componentes, no caso de não existir um ES compatível
	private final class CheckRegister extends CyclicBehaviour {

		public CheckRegister(Agent a) {
			super(a);
		}
		
		public void action() {
			if (numberEventHandlersRegistered == numberEventHandlersRequest) {
				// Envia um OK para Ambiente
				Command cmd = new Command(Constants.CMD_AGENT_READY);
				sendMessage(cmd);
				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);
			}
			
		}
		
	}

	private final class CheckEndTurn extends OneShotBehaviour {
		
		Agent a;
		
		public CheckEndTurn(Agent a) {
			super(a);
			this.a = a;
		}
		
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

				Command cmd = new Command(Constants.CMD_BATCH_TURN);
				cmd.addParameter(Constants.PARAM_NUMBER_EVT_SENT, Integer.toString(numberEventsSent));
				sendMessage(cmd);

				numberReasoningReady = 0;
				numberEventsSent = 0;
				
				MusicalAgent.logger.info("[" + getLocalName() + "] " + "Enviei fim de turno");
				
			}
			
		}
	
	}

	/**
	 * Classe interna que verifica se todos os componentes estão inicializados e registrados
	 */
	// TODO NÃO existe um timeout para o registro dos componentes, no caso de não existir um ES compatível
	private final class CheckDeregister extends CyclicBehaviour {

		public CheckDeregister(Agent a) {
			super(a);
		}
		
		public void action() {
			if (numberEventHandlersRegistered == 0) {
				// Informs the EA that the agent is being deregistered
				sendMessage(new Command(Constants.CMD_AGENT_DEREGISTER));
				// Kills the Musical Agent
				myAgent.addBehaviour(new KillAgent());
				// Finaliza o behaviour cíclico
				myAgent.removeBehaviour(this);
			}
			
		}
		
	}

	/**
	 * Verifica se todos os componentes estão finalizados e registrados
	 */
	private final class Deregister implements Runnable {

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
	 * Kills this agent
	 */
	private final class KillAgent extends OneShotBehaviour {

		@Override
		public void action() {
			MusicalAgent.logger.info("[" + getAgent().getLocalName() + " Killing agent '" + getAgent().getLocalName() + "'");
			
			// Calls the user implemented finalization method
			finit();
			
			// Calls the MMS finalization method
			stop();
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Agent 
	//--------------------------------------------------------------------------------
	protected final synchronized void eventHandlerRegistered(String compName) {
		
		numberEventHandlersRegistered++;
		
		MusicalAgent.logger.info("[" + this.getLocalName() + "] " + "Component " + compName + " registered");

	}
	
	protected final synchronized void eventHandlerDeregistered(String compName) {
		
		numberEventHandlersRegistered--;
		
		MusicalAgentComponent comp = components.remove(compName);
		comp.stop();
		
		MusicalAgent.logger.info("[" + this.getLocalName() + "] " + "Component " + compName + " deregistered");

	}

	protected final synchronized void eventSent() {
		
		numberEventsSent++;
		
	}
	
	// TODO Se não tiver reasoning nenhum, ele deveria mandar um fim de turno imediatamente (CyclicBehaviour igual ao de eventso?)
	protected final synchronized void reasoningProcessDone(String reasoningName) {
		
		numberReasoningReady++;
		// Checar o fim de turno
		addBehaviour(new CheckEndTurn(this));

	}

	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------
	
}
