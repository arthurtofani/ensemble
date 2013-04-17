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

import jade.util.Logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Constants.ES_STATE;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.clock.VirtualClockHelper;
import ensemble.comm.Comm;
import ensemble.memory.Memory;
import ensemble.router.RouterClient;


// TODO: Auto-generated Javadoc
/**
 * The Class EventServer.
 */
public abstract class EventServer implements LifeCycle, Sensing, Acting, RouterClient {

//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	/** The lock. */
private Lock lock = new ReentrantLock();
	
	/** The output lock. */
	private Lock outputLock = new ReentrantLock();

	/** The parameters. */
	protected Parameters parameters = null;

	/** The env agent. */
	protected EnvironmentAgent envAgent;

	/** The clock. */
	protected VirtualClockHelper clock;

	/** The event server state. */
	private ES_STATE 	eventServerState 	= ES_STATE.CREATED; 
	
	/** The event type. */
	private String 		eventType 			= "DUMMY";
	
	/** The event exchange. */
	private String		eventExchange 		= Constants.EVT_EXC_SPORADIC;
	
	/** The comm type. */
	private String 		commType 			= "ensemble.comm.direct.CommDirect";
	
	/** The is batch. */
	private boolean		isBatch				= false;

	// Periodic Events' variables
	/** The period. */
	protected long 		period;
	
	/** The send deadline. */
	protected long 		sendDeadline;
	
	/** The receive deadline. */
	protected long 		receiveDeadline;
	
	/** The period deadline. */
	protected long 		periodDeadline;
	
	/** The start time. */
	protected long 		startTime;
	
	/** The frame time. */
	protected long 		frameTime;
	
	/** The wait time. */
	protected long 		waitTime;
	
	/** The working frame. */
	protected int 		workingFrame;
	
	/** The happening frame. */
	protected int 		happeningFrame;
	
	/** The receiving frame. */
	protected int 		receivingFrame;
	
	/** The next state change. */
	private	long 		nextStateChange;
	
	// TODO Melhorar forma de armazenamento
	/** The actuators. */
	protected ConcurrentHashMap<String, Parameters> actuators = new ConcurrentHashMap<String, Parameters>();

	/** The sensors. */
	protected ConcurrentHashMap<String, Parameters> sensors   = new ConcurrentHashMap<String, Parameters>();
	
	/** The event handlers. */
	protected ConcurrentHashMap<String, Parameters> eventHandlers = new ConcurrentHashMap<String, Parameters>();
	
	/** The early events. */
	protected ArrayList<Event> earlyEvents = new ArrayList<Event>();
	
	/** The comm. */
	protected Comm comm;

	/** The my sensor. */
	protected Sensor mySensor;
	
	/** The my actuator. */
	protected Actuator myActuator;
	
	/** The input events. */
	private ArrayList<Event> inputEvents = new ArrayList<Event>();
	
	/** The output events. */
	private ArrayList<Event> outputEvents = new ArrayList<Event>();
	
	//--------------------------------------------------------------------------------
	// Event Server initialization
	//--------------------------------------------------------------------------------
	
	/**
	 * Called by the system to initalize an EventServer.
	 * Should not be called by the user!
	 *
	 * @return true, if successful
	 */
	@Override
	public final boolean start() {
		
		// TODO Verificar se está configurado, caso contrário, não libera a inicialização
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", envAgent.getAgentName());
		cmd.addParameter("EVENT_SERVER", this.getEventType());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addUserParameters(parameters);
		sendCommand(cmd);
		
		// Gets the Environment Agent
		if (envAgent == null) {
			System.err.println("[EventServer] There is no EnvironmentAgent in the parameters!");
			return false;
		}

		// Gets the Event Type
//		eventType = parameters.get(Constants.PARAM_ES_EVT_TYPE);
		
		// Obtém o clock
		this.clock = envAgent.getClock();
		
		isBatch = envAgent.getProperty(Constants.PROCESS_MODE, null).equals(Constants.MODE_BATCH);
		
		eventServerState = ES_STATE.CONFIGURED;
				
		// Inicializa a interface de comunicação
		try {
			Class commClass = Class.forName(commType);
			comm = (Comm)commClass.newInstance();
			Parameters commParam = new Parameters();
			commParam.put(Constants.PARAM_COMM_AGENT, envAgent);
			commParam.put(Constants.PARAM_COMM_SENSING, this);
			commParam.put(Constants.PARAM_COMM_ACTING, this);
			commParam.put(Constants.PARAM_COMM_ACCESS_POINT, getEventType());
			comm.setParameters(commParam);
			comm.configure();
			if (!comm.start()) {
				return false;
			}
			
		} catch (Exception e) {
//    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Comm class " + commType + " not found!");
			return false;
		}
		
		// Registra o tipo de evento no diretório do Jade
		envAgent.registerService(envAgent.getAgentName()+"-"+getEventType(), getEventType());

		// Chama o método de inicialização do usuário
		if (!init()) {
			return false;
		}
		
		if (eventExchange.equals(Constants.EVT_EXC_SPORADIC)) {
			// Nothing to do
		}
		// Caso seja uma troca de eventos híbrida, inicializar o processamento periódico
		else if (eventExchange.equals(Constants.EVT_EXC_HYBRID)) {
			
			startTime = waitTime + (long)clock.getCurrentTime(TimeUnit.MILLISECONDS);
//			System.out.println((long)clock.getCurrentTime(TimeUnit.MILLISECONDS) + "\t startTime = " + startTime);

			nextStateChange = startTime;
			clock.execute(envAgent, new HybridScheduler());
			
		}
		// Caso seja uma troca de eventos periódica, inicializar a máquina de estados
		else if (eventExchange.equals(Constants.EVT_EXC_PERIODIC)) {

			this.happeningFrame 	= -1;
			this.workingFrame 		= 0;
			this.receivingFrame 	= 0;
			
			// Criar dois WakeBehaviour, um para chamar o process(), outro para o action()
			// TODO Quanto tempo devo deixar para chamar a primeira vez? Talvez tanto faz, pq setExchange roda antes de tudo
			//startTime = clock.getCurrentTime();
			
			// TODO Adicionar parâmetro que permite esperar o registro de todos os atuadores/sensores!
			// waitTime = getParameter();
			// Transforma o waitTime em nanosegundos
			// TODO talvez seja melhor que o waitTime seja um sleep, e n�o um valor adicionado ao currentTime
			startTime = waitTime + (long)clock.getCurrentTime(TimeUnit.MILLISECONDS);
			
//			System.out.println((long)clock.getCurrentTime(TimeUnit.MILLISECONDS) + "\t waitTime = " + waitTime);
			System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + (long)clock.getCurrentTime(TimeUnit.MILLISECONDS) + "\t startTime = " + startTime);

			frameTime = startTime;
//			envAgent.addBehaviour(new ActionScheduler());
			clock.execute(envAgent, new PeriodicScheduler());
			
		}
		
		eventServerState = ES_STATE.INITIALIZED;

//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Initialized");
//		System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Initialized");

		cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", envAgent.getAgentName());
		cmd.addParameter("EVENT_SERVER", this.getEventType());
		cmd.addParameter("STATE", "INITIALIZED");
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		return true;
		
	}
	
	// TODO Implementar a finalização segura de um EventServer
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public final boolean stop() {
		
//		System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Finalizing the Event Server...");

		// Chamar o código do usuário
		try {
			finit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Finalizar o PeriodScheduler, se for o caso
		eventServerState = ES_STATE.TERMINATED;

		Command cmd = new Command(getAddress(), "/console", "TERMINATED");
		cmd.addParameter("AGENT", envAgent.getAgentName());
		cmd.addParameter("EVENT_SERVER", this.getEventType());
		cmd.addParameter("STATE", "INITIALIZED");
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		// Deregistrar todos os atuadores
		for (Enumeration<String> a = actuators.keys(); a.hasMoreElements();) {
			String[] a_key = a.nextElement().split(":");
			deregisterEventHandler(a_key[0], a_key[1], Constants.COMP_ACTUATOR);
		}
		
		// Deregistrar todo os sensores
		for (Enumeration<String> s = sensors.keys(); s.hasMoreElements();) {
			String[] s_key = s.nextElement().split(":");
			deregisterEventHandler(s_key[0], s_key[1], Constants.COMP_SENSOR);
		}
		
		// Stops the communication interface
		comm.stop();
		
		System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Finalized");
		
		cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", Constants.ENVIRONMENT_AGENT);
		cmd.addParameter("EVENT_SERVER", getEventType());
		sendCommand(cmd);
		
		return true;
		
	}
	
	//--------------------------------------------------------------------------------
	// Getters / Setters
	//--------------------------------------------------------------------------------
	
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

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#setParameters(ensemble.Parameters)
	 */
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#getParameters()
	 */
	public Parameters getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the env agent.
	 *
	 * @param envAgent the new env agent
	 */
	public void setEnvAgent(EnvironmentAgent envAgent) {
		this.envAgent = envAgent;
	}

	/**
	 * Sets the comm type.
	 *
	 * @param commType the new comm type
	 */
	public void setCommType(String commType) {
		if (eventServerState == ES_STATE.CREATED) {
			this.commType = commType;
		} else {
//    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Trying to set commClass after initialization!");
		}
	}

	/**
	 * Gets the comm type.
	 *
	 * @return the comm type
	 */
	public String getCommType() {
		return commType;
	}
	
	/**
	 * Sets the event type.
	 *
	 * @param eventType the new event type
	 */
	public void setEventType(String eventType) {
		if (eventServerState == ES_STATE.CREATED) {
			this.eventType = eventType;
		} else {
//    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Trying to set eventType after initialization!");
		}
	}

	/**
	 * Gets the event type.
	 *
	 * @return the event type
	 */
	public String getEventType() {
		return eventType;
	}
	
//	public void setEventExchange(String eventExchange) {
//		if (eventServerState == ES_STATE.CREATED) {
//			this.eventExchange = eventExchange;
//		} else {
////    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Trying to set eventExchange after initialization!");
//		}
//	}
	
	/**
 * Sets the event exchange.
 *
 * @param period the period
 * @param waitTime the wait time
 */
public void setEventExchange(long period, long waitTime) {
		if (eventServerState == ES_STATE.CREATED) {
			this.eventExchange = Constants.EVT_EXC_HYBRID;
			this.period = period;
			this.waitTime = waitTime;
		}
	}
	
	// TODO Como mudar o período durante a execução
	/**
	 * Sets the event exchange.
	 *
	 * @param period the period
	 * @param receiveDeadline the receive deadline
	 * @param sendDeadline the send deadline
	 * @param waitTime the wait time
	 * @return true, if successful
	 */
	public boolean setEventExchange(long period, long receiveDeadline, long sendDeadline, long waitTime) {
		
		if (eventServerState == ES_STATE.CREATED &&
				((0 <= receiveDeadline) && (receiveDeadline <= sendDeadline) && (sendDeadline <= period))) {
			this.eventExchange 		= Constants.EVT_EXC_PERIODIC;
			this.period 			= period;
			this.receiveDeadline 	= receiveDeadline;
			this.sendDeadline 		= sendDeadline;
			this.periodDeadline 	= period;
			this.waitTime 			= waitTime;
		} else {
//    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Trying to set eventExchange after initialization!");
			return false;
		}

		return true;
		
	}

	/**
	 * Gets the event exchange.
	 *
	 * @return the event exchange
	 */
	public String getEventExchange() {
		return eventExchange;
	}
	
	//------------------------------
	
	/**
	 * Searches the registered event handlers using filters.
	 *
	 * @param agentName the agent name
	 * @param componentName the component name
	 * @param eventType the event type
	 * @param ehType the eh type
	 * @return the string[]
	 */
	public String[] searchRegisteredEventHandler(String agentName, String componentName, String eventType, String ehType) {
		ArrayList<String> list = new ArrayList<String>();
		Enumeration<String> ehs = eventHandlers.keys();
		while (ehs.hasMoreElements()) {
			String str = ehs.nextElement();
			String[] str_split = str.split(":");
			if ((agentName.isEmpty() || agentName.equals(str_split[0])) &&
					(componentName.isEmpty() || componentName.equals(str_split[1])) &&
					(eventType.isEmpty() || eventType.equals(str_split[2])) &&
					(ehType.isEmpty() || ehType.equals(str_split[3]))) {
				list.add(str);
			}
		}
		String[] ret = new String[list.size()];
		return list.toArray(ret);
	}
	
	/**
	 * Register event handler.
	 *
	 * @param agentName the agent name
	 * @param eventHandlerName the event handler name
	 * @param eventHandlerType the event handler type
	 * @param userParameters the user parameters
	 */
	public void registerEventHandler(String agentName, String eventHandlerName, String eventHandlerType, Parameters userParameters) {

//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + "] " + "Recebi pedido de registro de " + agentName + ":" + eventHandlerName);

		// Envia os parâmetros necessários
		Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + agentName, Constants.CMD_EVENT_REGISTER_ACK);
		Parameters extraParameters = null; 

		if (eventHandlerType.equals(Constants.COMP_ACTUATOR)) {
		
			actuators.put(agentName + ":" + eventHandlerName, userParameters);
			eventHandlers.put(agentName+":"+eventHandlerName+":"+eventType+":"+Constants.COMP_ACTUATOR, userParameters);
			
			// Passar o controle para o código do usuário, caso seja necessário adicionar algum parâmetro extra
			// Ex.: o tamanho do chunk no caso de eventos de áudio
			try {
				extraParameters = actuatorRegistered(agentName, eventHandlerName, userParameters);
			} catch (Exception e) {
				e.printStackTrace();
//				MusicalAgent.logger.warning("[" + envAgent.getAgentName() + "] " + "Erro ao registrar o atuador");
			}

		}
		else if (eventHandlerType.equals(Constants.COMP_SENSOR)) {
			
			sensors.put(agentName + ":" + eventHandlerName, userParameters);
			eventHandlers.put(agentName+":"+eventHandlerName+":"+eventType+":"+Constants.COMP_SENSOR, userParameters);
			
			// Passar o controle para o código do usuário, caso seja necessário adicionar algum parâmetro extra
			// Ex.: o tamanho do chunk no caso de eventos de áudio
			try {
				extraParameters = sensorRegistered(agentName, eventHandlerName, userParameters);
			} catch (Exception e) {
				e.printStackTrace();
//				MusicalAgent.logger.warning("[" + envAgent.getAgentName() + "] " + "Erro ao registrar o sensor");
			}

		}
		else {
//			MusicalAgent.logger.warning("[" + envAgent.getAgentName() + "] " + "RegisterEventHandler received an strange component!");
			return;
		}
		
		// Send the register ack command
		// Periodic event
		if (getEventExchange() == Constants.EVT_EXC_PERIODIC) {
			cmd.addParameter(Constants.PARAM_COMP_NAME, eventHandlerName);
			cmd.addParameter(Constants.PARAM_EVT_EXECUTION, Constants.EVT_EXC_PERIODIC);
			cmd.addParameter(Constants.PARAM_PERIOD, Long.toString(period));
			cmd.addParameter(Constants.PARAM_RCV_DEADLINE, Long.toString(receiveDeadline));
			cmd.addParameter(Constants.PARAM_START_TIME, Long.toString(startTime)); 
			// Deve começar no próximo frame
			cmd.addParameter(Constants.PARAM_WORKING_FRAME, Long.toString(workingFrame+1));
		} else {
			// Sporadic and hybrid event
			cmd.addParameter(Constants.PARAM_COMP_NAME, eventHandlerName);
			cmd.addParameter(Constants.PARAM_EVT_EXECUTION, Constants.EVT_EXC_SPORADIC);
		}
			
		// Add extra parameters, if there is any
		if (extraParameters != null) {
			cmd.addUserParameters(extraParameters);
		}
		
		// Send the message
		sendCommand(cmd);

	}
	
	/**
	 * Deregister event handler.
	 *
	 * @param agentName the agent name
	 * @param eventHandlerName the event handler name
	 * @param eventHandlerType the event handler type
	 */
	public void deregisterEventHandler(String agentName, String eventHandlerName, String eventHandlerType) {
		
		if (eventHandlerType.equals(Constants.COMP_ACTUATOR)) {
			
			try {
				actuatorDeregistered(agentName, eventHandlerName);
				actuators.remove(agentName + ":" + eventHandlerName);
				eventHandlers.remove(agentName+":"+eventHandlerName+":"+eventType+":"+Constants.COMP_ACTUATOR);
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + agentName, Constants.CMD_EVENT_DEREGISTER_ACK);
				cmd.addParameter(Constants.PARAM_COMP_NAME, eventHandlerName);
				sendCommand(cmd);
				System.out.println("[" + envAgent.getAgentName() + "] Actuator " + agentName + ":" + eventHandlerName + " deregistered");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		} else if (eventHandlerType.equals(Constants.COMP_SENSOR)) {
		
			try {
				sensorDeregistered(agentName, eventHandlerName);
				sensors.remove(agentName + ":" + eventHandlerName);
				eventHandlers.remove(agentName+":"+eventHandlerName+":"+eventType+":"+Constants.COMP_SENSOR);
				Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + agentName, Constants.CMD_EVENT_DEREGISTER_ACK);
				cmd.addParameter(Constants.PARAM_COMP_NAME, eventHandlerName);
				sendCommand(cmd);
				System.out.println("[" + envAgent.getAgentName() + "] Sensor " + agentName + ":" + eventHandlerName + " deregistered");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + "] " + "Desregistro de " + agentName + ":" + eventHandlerName);

	}
	
	/**
	 * Creates the memory.
	 *
	 * @param name the name
	 * @param parameters the parameters
	 * @return the memory
	 */
	public Memory createMemory(String name, Parameters parameters) {

		Memory newMemory = null;
		
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
		newMemory.setAgent(envAgent);
		newMemory.configure();
		newMemory.start();
		
		return newMemory;
		
	}

	//--------------------------------------------------------------------------------
	// Métodos de recebimento e envio de Eventos
	//--------------------------------------------------------------------------------

	/**
	 * Sense early events.
	 *
	 * @param evt the evt
	 */
	protected void senseEarlyEvents(Event evt) {
		try {
			processSense(evt);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Processei o evento adiantado de " + evt.oriAgentName + ":" + evt.oriAgentCompName);
	}
	
	// TODO Pode ser um problema se o processSense() demorar muito! Mudar para Threads!!
	/* (non-Javadoc)
	 * @see ensemble.Sensing#sense(ensemble.Event)
	 */
	public void sense(Event evt) {
		
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Event received: " + evt);
//		System.out.println(clock.getCurrentTime() + " [" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Event received: " + evt);

		// Verifica se o evento é do mesmo tipo do ES
		if (!evt.eventType.equals(getEventType())) {
//			MusicalAgent.logger.warning("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Evento não é do tipo correto");
			return;
		}
			
		// Verifica a que janela pertence o evento
		if (!isBatch && getEventExchange().equals(Constants.EVT_EXC_PERIODIC)) {
			if (evt.frame < workingFrame) {
//				MusicalAgent.logger.warning("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Frame atrasado");
//				System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + evt.oriAgentName + ":" + evt.oriAgentCompName + " - Late frame: received frame = " + evt.frame + ", expected = " + workingFrame);
				return;
			}
			else if ((evt.frame == workingFrame && eventServerState != ES_STATE.WAITING_AGENTS)) {
//				MusicalAgent.logger.warning("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Frame atrasado");
//				System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + evt.oriAgentName + ":" + evt.oriAgentCompName + " -  Same frame, late arrival: received frame = " + evt.frame + ", expected = " + workingFrame);
				return;
			}
			else if (evt.frame > workingFrame) {
				lock.lock();
				try {
					boolean flag = (eventServerState != ES_STATE.WAITING_AGENTS); 
					if (flag) {
						earlyEvents.add(evt);
					}
				} finally {
					lock.unlock();
				}
//				MusicalAgent.logger.warning("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Frame adiantado");
				if (evt.frame-workingFrame > 1) {
//					System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Early frame: received frame = " + evt.frame + ", expected = " + workingFrame);
				}
				return;
			}
		}
		
		// chama o método implementado pelo usuário
		try {
			processSense(evt);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// avisa o ambiente que o evento foi processado (importante para Batch)
		if (isBatch) {
			envAgent.eventProcessed();
		}

//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Processei o evento");

	}
	
	/**
	 * Sends automatically all events in the outputEvents queue.
	 */
	public void act() {

//		double rand = Math.random();
//		System.out.println("[" + getEventType() + "] Entrei no act() - rand " + rand + " - " + outputEvents.size());
		
		outputLock.lock();
		try {

//			System.out.println("[" + getEventType() + "] Entrei no act()+lock()");
			
			for (Event evt : outputEvents) {

				sendEvent(evt);
			
			}
			// Apaga a fila
//			System.out.println("Vou dar clear - rand " + rand + " - " + outputEvents.size());
			outputEvents.clear();

		} finally {
			outputLock.unlock();
		}

	}
	
	/**
	 * Sends a single Event.
	 *
	 * @param evt the evt
	 */
	protected void sendEvent(Event evt) {
		
		// Completa os dados do evento
		evt.oriAgentName	 	= envAgent.getAgentName();
		evt.oriAgentCompName 	= getEventType();
		evt.eventType 		 	= getEventType();
		if (getEventExchange().equals(Constants.EVT_EXC_PERIODIC)) {
			evt.frame 			= workingFrame;
			evt.instant 		= (double)(startTime + (workingFrame * period))/1000;
			evt.duration 		= (double)period/1000;
		} else {
			evt.frame 			= -1;
			evt.instant 		= clock.getCurrentTime(TimeUnit.SECONDS);
			evt.duration 		= 0;
		}
		evt.timestamp 			= (long)clock.getCurrentTime(TimeUnit.MILLISECONDS); 
        
		// Chama o método implementado pelo usuário
		try {
			evt = processAction(evt);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Envia o evento
	    comm.send(evt);
	    
        // Avisa o Agente Ambiente do evento enviado
        envAgent.eventSent();

//	   		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Gerei um evento");

	}
	
	/**
	 * Adds a event to a queue to be sent the next time act() is called.
	 * It can be used by a sporadic event server to send differents events for differents recipients at the same time
	 *
	 * @param destAgentName the dest agent name
	 * @param destCompName the dest comp name
	 * @param evt the evt
	 */
	protected void addOutputEvent(String destAgentName, String destCompName, Event evt) {
		
		outputLock.lock();
		try {
			evt.destAgentName = destAgentName;
			evt.destAgentCompName = destCompName;
			outputEvents.add(evt);
		} finally {
			outputLock.unlock();
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Hybrid Exchange Methods
	//--------------------------------------------------------------------------------
	
	/**
	 * The Class HybridScheduler.
	 */
	private class HybridScheduler implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			try {
				long currentTime = (long)clock.getCurrentTime(TimeUnit.MILLISECONDS); 
				long elapsedTime = currentTime - nextStateChange;
				process();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			nextStateChange = nextStateChange + period;
			
			if (eventServerState != ES_STATE.TERMINATED) {
				clock.schedule(envAgent, this, nextStateChange);
			}

		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Periodic Exchange Methods
	//--------------------------------------------------------------------------------
	
//	private class ActionScheduler extends OneShotBehaviour {
	/**
	 * The Class PeriodicScheduler.
	 */
	private class PeriodicScheduler implements Runnable {

//		long wakeTime;
//		
//		public ActionScheduler(long wakeTime) {
//			this.wakeTime = wakeTime;
//		}
		
//		public void action() {
		/* (non-Javadoc)
 * @see java.lang.Runnable#run()
 */
public void run() {

//			int num = (int)Math.floor((Math.random() * 100));

//			System.out.println(eventServerState);
//			System.out.println(clock.getCurrentTime() + "\t [EventServer:"+ eventType + "] \t Entrei no action() - " + num);
			
			long currentTime = (long)clock.getCurrentTime(TimeUnit.MILLISECONDS); 
			long elapsedTime = currentTime - nextStateChange;
//			if (currentTime - nextStateChange > 20 && eventServerState != ES_STATE.WAITING_BEGIN && eventServerState != ES_STATE.INITIALIZED && eventServerState != ES_STATE.CONFIGURED) {
//				System.err.println(clock.getCurrentTime() + " ********* WARNING (" + (currentTime - nextStateChange) + ") - " + eventServerState + " ********** " + num );
//				System.exit(-1);
//			}
			
//			System.out.println(clock.getCurrentTime() + " \t 1");
			
			switch (eventServerState) {

			case INITIALIZED:
			
				eventServerState = ES_STATE.WAITING_BEGIN;
//				System.out.println("--------------------------------------------------------------------------------");
//				System.out.println(clock.getCurrentTime() + " >> 1 - WAITING_BEGIN << ");
//	    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + ">> 1 - WAITING_BEGIN <<");
				nextStateChange = startTime;
				receivingFrame++;
				
				break;

			// Frame start
			case WAITING_BEGIN:
			case SENDING_RESPONSE:

				workingFrame++;
				happeningFrame++;

				// Muda o estado do agente e volta a receber eventos
				eventServerState = ES_STATE.WAITING_AGENTS;

//				System.out.println("--------------------------------------------------------------------------------");
//				System.out.println(clock.getCurrentTime() + " >> 2 - WAITING_AGENTS << (" + elapsedTime + ") (workingFrame " + workingFrame + ") (t " + ((double)(period * workingFrame)/1000) + "s)");
//	    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + ">> 2 - WAITING_AGENTS << (" + elapsedTime + ") (workingFrame " + workingFrame + ") (t " + ((startTime + (double)(period * workingFrame))/1000) + "s)");;
				nextStateChange = frameTime + receiveDeadline;
				
				break;

			// ReceiveDeadline
			case WAITING_AGENTS:
				// TODO Talvez não seja o melhor lugar para atualizar isso!
				// TODO E se o per�odo for muito curto e n�o der tempo de atualizar? Ele vai se perder!!!
				receivingFrame++;	
				
				// Muda o estado do agente e para de receber eventos
				eventServerState = ES_STATE.PROCESSING;
				// TODO Tirei para que possa receber eventos futuros
				// comm.sensing = false;

				// Insere os eventos adiantados (para essa workingFrame)
				try {
					lock.lock();
					for (Iterator<Event> iterator = earlyEvents.iterator(); iterator.hasNext();) {
						Event evt = iterator.next();
						if (evt.frame == workingFrame) {
							senseEarlyEvents(evt);
						}
					}
					earlyEvents.clear();
				} finally {
					lock.unlock();
				}
				
				// Inicia o processamento dos eventos
//				System.out.println(clock.getCurrentTime() + " >> 3 - PROCESSING << (" + elapsedTime + ") ");
//	    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + ">> 3 - PROCESSING << (" + elapsedTime + ") ");
				
//				System.out.println("Entrou no process()\t " + clock.getCurrentTime());
				long time = (long)clock.getCurrentTime(TimeUnit.MILLISECONDS);
				try {
					process();
				} catch (Exception e) {
					e.printStackTrace();
				}
//	    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "process() demorou " + (clock.getCurrentTime() - time));
//				System.out.println(clock.getCurrentTime() + "\t process() demorou " + (clock.getCurrentTime() - time));
//				System.out.println("Saiu do process()\t " + clock.getCurrentTime());
				
				// Agenda a pr�xima mudan�a de estado
				nextStateChange = frameTime + sendDeadline;

				break;

			// SendDeadline
			case PROCESSING:
				// If sendDeadline = periodDeadline, the user sends the events by himself and we go directly to WAITING_AGENTS
				if (sendDeadline == periodDeadline) {
					workingFrame++;
					happeningFrame++;
					frameTime = frameTime + period;

					// Muda o estado do agente e volta a receber eventos
					eventServerState = ES_STATE.WAITING_AGENTS;

//					System.out.println("--------------------------------------------------------------------------------");
//		    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + ">> 2 - WAITING_AGENTS << (" + elapsedTime + ") (workingFrame " + workingFrame + ") (t " + ((startTime + (double)(period * workingFrame))/1000) + "s)");;
					nextStateChange = frameTime + receiveDeadline;
				}
				else {
					eventServerState = ES_STATE.SENDING_RESPONSE;
					frameTime = frameTime + period;
//					System.out.println(clock.getCurrentTime() + " >> 4 - SENDING_RESPONSE << (" + elapsedTime + ") ");
//		    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + ">> 4 - SENDING_RESPONSE << (" + elapsedTime + ") ");
					nextStateChange = frameTime;
					act();
				}
		        break;
			}
			
//			System.out.println(clock.getCurrentTime() + " \t 2");

			// Agenda a próxima mudança de estado
//			System.out.println(clock.getCurrentTime() + "\t [EventServer:"+ eventType + "] \t Vou agendar \t - " + num);
			if (eventServerState != ES_STATE.TERMINATED) {
				clock.schedule(envAgent, this, nextStateChange);
			}
//			System.out.println("Vou dormir até \t" + nextStateChange);			
//			System.out.println(clock.getCurrentTime() + " \t 3");
//			System.out.println("-------");

//			System.out.println("Sai do action()\t " + clock.getCurrentTime() + " - " + num);
			
		}
		
	}
	
	//--------------------------------------------------------------------------------
	// Command Interface
	//--------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#getAddress()
	 */
	@Override
	public final String getAddress() {
		return "/" + Constants.FRAMEWORK_NAME + "/" + envAgent.getAgentName() + "/" + getEventType();
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public final void receiveCommand(Command cmd) {
//        System.out.println("[" + getAddress() +"] Command received: " + cmd);
		// Command is a change of parameter
		if (cmd.getCommand().equals(Constants.CMD_PARAMETER)) {
			String param = cmd.getParameter("NAME");
			String value = cmd.getParameter("VALUE");
			if (param != null && value != null && parameters.containsKey(param)) {
				// Change of period
				if (param.equals(Constants.PARAM_PERIOD)) {

				} 
				// Change of event type
				else if (param.equals(Constants.PARAM_EVT_TYPE)) {
					// ignores! 
				}
				// Calls user method
				else {
					if (!parameterUpdate(param, value)) {
						return;
					}
					parameters.put(param, value);
					// Let the console knows about the updated parameter
					cmd = new Command(getAddress(), "/console", "UPDATE");
					cmd.addParameter("AGENT", Constants.ENVIRONMENT_AGENT);
					cmd.addParameter("EVENT_SERVER", getEventType());
					cmd.addParameter("NAME", param);
					cmd.addParameter("VALUE", value);
					sendCommand(cmd);
				}
			}
		} else {
		    processCommand(cmd);
		}
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#sendCommand(ensemble.Command)
	 */
	@Override
	public final void sendCommand(Command cmd) {
		envAgent.sendCommand(cmd);
	}
	
	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}
	
	/**
	 * Método chamado ao receber um evento. Deve armazenar os eventos recebidos em uma estrutura de dados, para depois ser processado.
	 *
	 * @param evt the evt
	 * @throws Exception the exception
	 */
	protected void processSense(Event evt) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "processSense()");
	}

	/**
	 * Método chamado para enviar um evento pelo process().
	 * No caso de eventos frequentes, é chamado automaticamente quando o sendDeadline é atingindo.
	 *
	 * @param evt the evt
	 * @return the event
	 * @throws Exception the exception
	 */
	protected Event processAction(Event evt) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "processAction()");
		return evt;
	}
	
	/**
	 * Método chamado para efetuar algum processamento sobre os eventos recebidos, e enviar eventos em seguida.
	 * No caso de eventos frequentes, é chamado automaticamente quando o receiveDeadline é atingindo.
	 *
	 * @throws Exception the exception
	 */
	// TODO Pode lançar um TimeOutException caso não seja executada no tempo necessário!
	protected void process() throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "process()");
	}

	/**
	 * Actuator registered.
	 *
	 * @param agentName the agent name
	 * @param actuatorName the actuator name
	 * @param userParam the user param
	 * @return the parameters
	 * @throws Exception the exception
	 */
	protected Parameters actuatorRegistered(String agentName, String actuatorName, Parameters userParam) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "actuatorRegistered()");
		return null;
	}
	
	/**
	 * Actuator deregistered.
	 *
	 * @param agentName the agent name
	 * @param actuatorName the actuator name
	 * @throws Exception the exception
	 */
	protected void actuatorDeregistered(String agentName, String actuatorName) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "actuatorDeregistered()");
	}
	
	/**
	 * Sensor registered.
	 *
	 * @param agentName the agent name
	 * @param sensorName the sensor name
	 * @param userParam the user param
	 * @return the parameters
	 * @throws Exception the exception
	 */
	protected Parameters sensorRegistered(String agentName, String sensorName, Parameters userParam) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "sensorRegistered()");
		return null;
	}
	
	/**
	 * Sensor deregistered.
	 *
	 * @param agentName the agent name
	 * @param sensorName the sensor name
	 * @throws Exception the exception
	 */
	protected void sensorDeregistered(String agentName, String sensorName) throws Exception {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "sensorDeregistered()");
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
	}

}
