package ensemble.router;

import jade.core.Agent;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Command;
import ensemble.Constants;
import ensemble.Event;
import ensemble.EventHandlerInfo;
import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementState;
import ensemble.world.Law;
import ensemble.world.Vector;
import ensemble.world.World;

/**
 * O MessageEventServer representa mensagens e comandos no mundo virtual.
 */
public class MessageEventServer extends EventServer {
		
	private static Lock lock = new ReentrantLock();

	private boolean osc = true;
	
	private World 	world;
	private Law 	msgLaw;

	/**
	 * List of agents with commands enabled
	 */
	HashMap<String,Vector> acc_command = new HashMap<String, Vector>();
	HashMap<String,Double> stop_command = new HashMap<String,Double>();

	@Override
	public boolean configure() {
		setCommType("ensemble.comm.direct.CommDirect");
		setEventType(MessageConstants.EVT_TYPE_MESSAGE);
		String[] period = getParameters().get(Constants.PARAM_PERIOD, "100 1000").split(" ");
		setEventExchange(Long.valueOf(period[0]), Long.valueOf(period[1]));
		return true;
	}
	
	@Override
	public boolean init() {
		
		this.world = envAgent.getWorld();
		this.msgLaw = world.getLaw(MessageConstants.EVT_TYPE_MESSAGE);

		return true;
		
	}
	
	@Override
	public boolean finit() {
		
		return true;
		
	}
	
	private Memory createEntityMemory(String entityName) {
		
		// Gets the Movement Memory
		Memory msgMemory = (Memory)world.getEntityStateAttribute(entityName, MessageConstants.EVT_TYPE_MESSAGE);
		// If there is no memory, creates one for this entity
		if (msgMemory == null) {
			// Cria uma memória para o atuador
			try {
				// Criar a instância do componente
				Class esClass = Class.forName("ensemble.memory.EventMemory");
				msgMemory = (Memory)esClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			Parameters memParameters = new Parameters();
			memParameters.put(Constants.PARAM_MEMORY_NAME, entityName);
			memParameters.put(Constants.PARAM_MEMORY_PAST, "1.0");
			memParameters.put(Constants.PARAM_MEMORY_FUTURE, "0");
			msgMemory.setParameters(memParameters);
			msgMemory.setAgent(envAgent);
			msgMemory.configure();
			msgMemory.start();
			world.addEntityStateAttribute(entityName, MessageConstants.EVT_TYPE_MESSAGE, msgMemory);
		}

		return msgMemory;
		
	}

	@Override
	protected Parameters actuatorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		// Gets the Movement Memory
		Memory msgMemory = (Memory)world.getEntityStateAttribute(agentName, MessageConstants.EVT_TYPE_MESSAGE);
		// If there is no memory, creates one for this entity
		if (msgMemory == null) {
			msgMemory = createEntityMemory(agentName);
		}

		MessageState msgState = new MessageState();
		msgState.instant = clock.getCurrentTime(TimeUnit.SECONDS);

		// Inserts an attribute in the Entity State
    	world.addEntityStateAttribute(agentName, MessageConstants.EVT_TYPE_MESSAGE, msgMemory);
    	
    	// Informs the agent, if it has any sensor, about the its position
//    	informAgent(agentName, movState);

		/*// Mensagem OSC
    	if (osc) {
    		// Register the agent
    		String str = "agent " + agentName + " src";
    		for (int i = 0; i < movState.position.dimensions; i++) {
				str += " " + movState.position.getValue(i);
			}
    		Command cmd = new Command(getAddress(), "/pd", "OSC");
    		cmd.addParameter("CONTENT", str);
    		sendCommand(cmd); 
    		// Informs the position
    		sendOSCPosition(agentName, movState.position);
    	}*/
    	
		return userParam;
		
	}
	
	@Override
	protected Parameters sensorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		// Gets the Movement Memory
		Memory msgMemory = (Memory)world.getEntityStateAttribute(agentName, MessageConstants.EVT_TYPE_MESSAGE);
		// If there is no memory, creates one for this entity
		if (msgMemory == null) {
			msgMemory = createEntityMemory(agentName);
 		}

		// Verifies if there is an initial msg for the entity and writes it in the memory
		MessageState msgState = new MessageState();
		msgState.instant = clock.getCurrentTime(TimeUnit.SECONDS);
		msgState.domain = MessageConstants.DEFAULT_DOMAIN;
		msgState.type = MessageConstants.DEFAULT_TYPE;
		msgState.action = MessageConstants.DEFAULT_ACTION;
		
		try {
			msgMemory.writeMemory(msgState);
		} catch (MemoryException e) {
			e.printStackTrace();
		}

		// Sends the initial msg to the sensor
		informAgent(agentName, eventHandlerName, msgState);
				
		/*// Sends an OSC message
		if (osc) {
			sendOSCPosition(agentName, movState.position);
		}
*/
		return userParam;
	}
	
	private void informAgent(String agentName, String sensorName, MessageState state) {
		Event evt = new Event();
		Command cmd2 = new Command(MessageConstants.CMD_INFO);
		cmd2.addParameter(MessageConstants.PARAM_DOMAIN, state.domain);
		cmd2.addParameter(MessageConstants.PARAM_TYPE, state.type);
		cmd2.addParameter(MessageConstants.PARAM_ACTION, state.action);
		cmd2.addParameter(MessageConstants.PARAM_ARGS, state.args);
		
		evt.objContent = cmd2.toString();
		addOutputEvent(agentName, sensorName, evt);
		act();
	}
	
	private void executeMessageInstruction(String entityName, String instruction, Parameters parameters) {

		//System.out.println("ENTROU execute() - " + entityName + ", " + instruction + ", " + parameters);
		
		
		lock.lock();
		try {
			String entitySearched = new String();
			double t = clock.getCurrentTime(TimeUnit.SECONDS);
		
			//Recognize the type of message
			if(parameters.get(MessageConstants.PARAM_DOMAIN)!=null && parameters.get(MessageConstants.PARAM_DOMAIN)==MessageConstants.EXT_OSC_DOMAIN){
				
				if(parameters.get(MessageConstants.PARAM_TYPE)!=null && parameters.get(MessageConstants.PARAM_TYPE)==MessageConstants.SPIN_OSC_TYPE){
					
					if(parameters.get(MessageConstants.PARAM_ACTION)==MessageConstants.SPIN_OSC_POSITION) {
						int idNumber = Integer.parseInt(parameters.get(MessageConstants.SPIN_OSC_IDNUMBER));
						if(entityName ==null){
							entitySearched = "M" + idNumber;
						}else entitySearched = entityName;
					}
				}
			}
			
			String[] sensors1 = searchRegisteredEventHandler(entitySearched, "", MessageConstants.EVT_TYPE_MESSAGE, Constants.COMP_SENSOR);
			//for (int i = 0; i < sensors1.length; i++) {
				//System.out.println(sensors1[i]);
			//}
			
			
			// Gets the entity's msg state
			/*Memory msgMemory = (Memory)world.getEntityStateAttribute(entitySearched, MessageConstants.EVT_TYPE_MESSAGE);
			if (msgMemory != null) {
				
				MessageState oldState = ((MessageState)msgMemory.readMemory(t, TimeUnit.SECONDS));
				if (oldState == null) {
					return;
				}
					
				System.out.println("old state = " + oldState.instant + " " + oldState.action + " " + oldState.domain + " " + oldState.args);
	*/
				// Process the message instruction, creating a new state
				MessageState newState = new MessageState();
				//msgLaw.changeState(oldState, clock.getCurrentTime(TimeUnit.SECONDS), newState);
				if (instruction.equals(MessageConstants.CMD_RECEIVE)) {
					
					newState.domain = parameters.get(MessageConstants.PARAM_DOMAIN);
					newState.type = parameters.get(MessageConstants.PARAM_TYPE);
					newState.action = parameters.get(MessageConstants.PARAM_ACTION);
					newState.args = parameters.get(MessageConstants.PARAM_ARGS);
				}
				
				//System.out.println("new state = " + newState.instant + " " + newState.domain + " " + newState.action + " " + newState.args);
				
				// Registers the new state in memory
				/*try {
					msgMemory.writeMemory(newState);
				} catch (MemoryException e) {
					e.printStackTrace();
				}*/
	
				// Creates a response event if there is a sensor registered
				String[] sensors = searchRegisteredEventHandler(entitySearched, "", MessageConstants.EVT_TYPE_MESSAGE, Constants.COMP_SENSOR);
				for (int i = 0; i < sensors.length; i++) {
					EventHandlerInfo info = EventHandlerInfo.parse(sensors[i]);
					informAgent(info.agentName, info.componentName, newState);
				}
				
				// Sends an OSC message
				/*if (osc) {
					sendOSCPosition(entityName, newState.position);
				}*/
			//}
			
		} finally {
			lock.unlock();
		}
		
	}
	
	@Override
	// TODO Só pode mudar o estado no próximo frame
	public void processSense(Event evt) {
		
			// Process the command
			Command cmd = Command.parse((String)evt.objContent);
//			System.out.println("processSense(): " + cmd);
			if (cmd != null) {
//				System.out.println("Processing command of '" + evt.oriAgentName + "' at t = " + t + " " + evt.objContent);
				executeMessageInstruction(evt.oriAgentName, cmd.getCommand(), cmd.getParameters());
			}

			
	}
	
	@Override
	public void process() {

		double t = clock.getCurrentTime(TimeUnit.SECONDS);
		
		lock.lock();
		try {

			// Process the movement of each entity
			Set<String> entities = world.getEntityList();
			for (String entity : entities) {
				
				Memory msgMemory = (Memory)world.getEntityStateAttribute(entity, MessageConstants.EVT_TYPE_MESSAGE);
				
				if (msgMemory != null) {

					MessageState oldState = (MessageState)msgMemory.readMemory(t, TimeUnit.SECONDS);
					
					// If necessary, updates the  state
					if (oldState != null ) {

						MessageState newState = new MessageState();
						//msgLaw.changeState(oldState, clock.getCurrentTime(TimeUnit.SECONDS), newState);
						
						try {
							msgMemory.writeMemory(newState);
						} catch (MemoryException e) {
							e.printStackTrace();
						}

						// Sends an OSC message
						/*if (osc) {
							sendOSCPosition(entity, newState.position);
						}
						*/
						// Creates a response event if there is a sensor registered
						String[] sensors = searchRegisteredEventHandler(entity, "", MessageConstants.EVT_TYPE_MESSAGE, Constants.COMP_SENSOR);
						for (int i = 0; i < sensors.length; i++) {
							EventHandlerInfo info = EventHandlerInfo.parse(sensors[i]);
							informAgent(info.agentName, info.componentName, newState);
						}
			
					}
				}
			}
					
			// Sends events
			try {
				act();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} finally {
			lock.unlock();
		}
		
	}

	/**
	 * Sends entity's position via OSC protocol
	 * @param state
	 */
	private void sendOSCPosition(String entityName, Vector position) {

		// Envia a nova posição via OSC
//		Command cmd = new Command("MOVEMENT_UPDATE");
//		cmd.addParameter("NAME", entityName);
//		cmd.addParameter("POS", state.position.toString());
//		cmd.addParameter("VEL", state.velocity.toString());
//		cmd.addParameter("ACC", state.acceleration.toString());
		String str = "pos " + entityName;
		for (int i = 0; i < position.dimensions; i++) {
			str += " " + position.getValue(i);
		}
		Command cmd = new Command(getAddress(), "/pd", "OSC");
		cmd.addParameter("CONTENT", str);
		sendCommand(cmd);

	}
	
	@Override
	public void processCommand(Command cmd) {

		executeMessageInstruction(cmd.getParameter("AGENT"), cmd.getCommand(), cmd.getParameters());

	}
	
}
