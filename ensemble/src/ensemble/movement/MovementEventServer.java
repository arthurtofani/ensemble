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

package ensemble.movement;

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
import ensemble.memory.EventMemory;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.world.Law;
import ensemble.world.Vector;
import ensemble.world.World;


/**
 * O MovementServer representa as leis físicas do movimento no mundo virtual.
 */
public class MovementEventServer extends EventServer {
		
	private static Lock lock = new ReentrantLock();

	private boolean osc = true;
	
	private World 	world;
	private Law 	movLaw;

	/**
	 * List of agents with movement commands enabled
	 */
	HashMap<String,Vector> acc_command = new HashMap<String, Vector>();
	HashMap<String,Double> stop_command = new HashMap<String,Double>();

	@Override
	public boolean configure() {
		setCommType("ensemble.comm.direct.CommDirect");
		setEventType(MovementConstants.EVT_TYPE_MOVEMENT);
		String[] period = getParameters().get(Constants.PARAM_PERIOD, "100 1000").split(" ");
		setEventExchange(Long.valueOf(period[0]), Long.valueOf(period[1]));
		return true;
	}
	
	@Override
	public boolean init() {
		
		this.world = envAgent.getWorld();
		this.movLaw = world.getLaw(MovementConstants.EVT_TYPE_MOVEMENT);

		return true;
		
	}
	
	@Override
	public boolean finit() {
		
		return true;
		
	}
	
	private Memory createEntityMemory(String entityName) {
		
		// Gets the Movement Memory
		Memory movMemory = (Memory)world.getEntityStateAttribute(entityName, MovementConstants.EVT_TYPE_MOVEMENT);
		// If there is no memory, creates one for this entity
		if (movMemory == null) {
			// Cria uma memória para o atuador
			try {
				// Criar a instância do componente
				Class esClass = Class.forName("ensemble.memory.EventMemory");
				movMemory = (Memory)esClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			Parameters memParameters = new Parameters();
			memParameters.put(Constants.PARAM_MEMORY_NAME, entityName);
			memParameters.put(Constants.PARAM_MEMORY_PAST, "1.0");
			memParameters.put(Constants.PARAM_MEMORY_FUTURE, "0");
			movMemory.setParameters(memParameters);
			movMemory.setAgent(envAgent);
			movMemory.configure();
			movMemory.start();
			world.addEntityStateAttribute(entityName, MovementConstants.EVT_TYPE_MOVEMENT, movMemory);
		}

		return movMemory;
		
	}

	@Override
	protected Parameters actuatorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		// Gets the Movement Memory
		Memory movMemory = (Memory)world.getEntityStateAttribute(agentName, MovementConstants.EVT_TYPE_MOVEMENT);
		// If there is no memory, creates one for this entity
		if (movMemory == null) {
			movMemory = createEntityMemory(agentName);
		}

		MovementState movState = new MovementState(world.dimensions);
		movState.instant = clock.getCurrentTime(TimeUnit.SECONDS);

		// Get the initial parameters
		// Verifies if there is an initial position for the entity and writes it in the memory
		Vector position = (Vector)world.getEntityStateAttribute(agentName, Constants.PARAM_POSITION);
		if (position != null) { 
			movState.position = position;
		} else {
			movState.position = new Vector(world.dimensions);
		}
		if (userParam.containsKey(MovementConstants.PARAM_VEL)) {
			movState.velocity = Vector.parse(userParam.get(MovementConstants.PARAM_VEL));
		} else {
			movState.velocity = new Vector(world.dimensions);
		}
		if (userParam.containsKey(MovementConstants.PARAM_ACC)) {
			movState.acceleration = Vector.parse(userParam.get(MovementConstants.PARAM_ACC));
		} else {
			movState.acceleration = new Vector(world.dimensions);
		}
		movState.orientation = new Vector(world.dimensions);
		movState.angularVelocity = new Vector(world.dimensions);

		// Writes the new movement state
		movMemory.writeMemory(movState);

		// Inserts an attribute in the Entity State
    	world.addEntityStateAttribute(agentName, MovementConstants.EVT_TYPE_MOVEMENT, movMemory);
    	
    	// Informs the agent, if it has any sensor, about the its position
//    	informAgent(agentName, movState);

		// Mensagem OSC
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
    	}
    	
		return userParam;
		
	}
	
	@Override
	protected Parameters sensorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		// Gets the Movement Memory
		Memory movMemory = (Memory)world.getEntityStateAttribute(agentName, MovementConstants.EVT_TYPE_MOVEMENT);
		// If there is no memory, creates one for this entity
		if (movMemory == null) {
			movMemory = createEntityMemory(agentName);
 		}

		// Verifies if there is an initial position for the entity and writes it in the memory
		MovementState movState = new MovementState(world.dimensions);
		movState.instant = clock.getCurrentTime(TimeUnit.SECONDS);
		Vector position = (Vector)world.getEntityStateAttribute(agentName, "POSITION");
		if (position != null) { 
			movState.position = position;
		} else {
			movState.position = new Vector(world.dimensions);
		}
		movState.velocity = new Vector(world.dimensions);
		movState.acceleration = new Vector(world.dimensions);
		movState.orientation = new Vector(world.dimensions);
		movState.angularVelocity = new Vector(world.dimensions);
		try {
			movMemory.writeMemory(movState);
		} catch (MemoryException e) {
			e.printStackTrace();
		}

		// Sends the position to the sensor
		informAgent(agentName, eventHandlerName, movState);
				
		// Sends an OSC message
		if (osc) {
			sendOSCPosition(agentName, movState.position);
		}

		return userParam;
	}
	
	private void informAgent(String agentName, String sensorName, MovementState state) {
		Event evt = new Event();
		Command cmd2 = new Command(MovementConstants.CMD_INFO);
		cmd2.addParameter(MovementConstants.PARAM_POS, state.position.toString());
		cmd2.addParameter(MovementConstants.PARAM_VEL, state.velocity.toString());
		cmd2.addParameter(MovementConstants.PARAM_ORI, state.orientation.toString());
		evt.objContent = cmd2.toString();
		addOutputEvent(agentName, sensorName, evt);
		act();
	}
	
	private void executeMovementInstruction(String entityName, String instruction, Parameters parameters) {

//		System.out.println("ENTROU execute() - " + entityName + ", " + instruction + ", " + parameters);
		
		lock.lock();
		try {
			
			double t = clock.getCurrentTime(TimeUnit.SECONDS);
		
			// Gets the entity's movement state
			Memory movMemory = (Memory)world.getEntityStateAttribute(entityName, MovementConstants.EVT_TYPE_MOVEMENT);
			if (movMemory != null) {
				
				MovementState oldState = ((MovementState)movMemory.readMemory(t, TimeUnit.SECONDS));
				if (oldState == null) {
					return;
				}
					
	//			System.out.println("old state = " + oldState.instant + " " + oldState.position + " " + oldState.velocity + " " + oldState.acceleration);
	
				// Process the movement instruction, creating a new state
				MovementState newState = new MovementState(world.dimensions);
				movLaw.changeState(oldState, clock.getCurrentTime(TimeUnit.SECONDS), newState);
				if (instruction.equals(MovementConstants.CMD_WALK)) {
					// TODO Aqui deveria avaliar a possibilidade da mudança (mudanças bruscas não poderiam acontecer)
					newState.acceleration = Vector.parse(parameters.get(MovementConstants.PARAM_ACC));
					if (parameters.containsKey(MovementConstants.PARAM_DUR)) {
						System.out.println("PARAM_DUR = " + parameters.get(MovementConstants.PARAM_DUR));
						stop_command.put(entityName, (t+Double.valueOf(parameters.get(MovementConstants.PARAM_DUR))));
					}
				}
				else if (instruction.equals(MovementConstants.CMD_TURN)) {
					newState.angularVelocity = Vector.parse(parameters.get(MovementConstants.PARAM_ANG_VEL));
					stop_command.put(entityName, (t+Double.valueOf(parameters.get(MovementConstants.PARAM_DUR))));
				}
				else if (instruction.equals(MovementConstants.CMD_STOP)) {
					newState.velocity.zero();
					newState.acceleration.zero();
					newState.angularVelocity.zero();
					stop_command.remove(entityName);
				}
				else if (instruction.equals(MovementConstants.CMD_TRANSPORT)) {
					newState.position = Vector.parse(parameters.get(MovementConstants.PARAM_POS));
					newState.acceleration.zero();
					newState.angularVelocity.zero();
					stop_command.remove(entityName);
				}
				
	//			System.out.println("new state = " + newState.instant + " " + newState.position + " " + newState.velocity + " " + newState.acceleration);
				
				// Registers the new state in memory
				try {
					movMemory.writeMemory(newState);
				} catch (MemoryException e) {
					e.printStackTrace();
				}
	
				// Creates a response event if there is a sensor registered
				String[] sensors = searchRegisteredEventHandler(entityName, "", MovementConstants.EVT_TYPE_MOVEMENT, Constants.COMP_SENSOR);
				for (int i = 0; i < sensors.length; i++) {
					EventHandlerInfo info = EventHandlerInfo.parse(sensors[i]);
					informAgent(info.agentName, info.componentName, newState);
				}
				
				// Sends an OSC message
				if (osc) {
					sendOSCPosition(entityName, newState.position);
				}
			}
			
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
				executeMovementInstruction(evt.oriAgentName, cmd.getCommand(), cmd.getParameters());
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
				
				Memory movMemory = (Memory)world.getEntityStateAttribute(entity, MovementConstants.EVT_TYPE_MOVEMENT);
				
				if (movMemory != null) {

					MovementState oldState = (MovementState)movMemory.readMemory(t, TimeUnit.SECONDS);
					
					// If necessary, updates the movement state
					if (oldState != null && (oldState.acceleration.magnitude > 0 || 
											oldState.velocity.magnitude > 0 || 
											oldState.angularVelocity.magnitude > 0)) {

						MovementState newState = new MovementState(world.dimensions);
						movLaw.changeState(oldState, clock.getCurrentTime(TimeUnit.SECONDS), newState);
						
						// Checks if the movement has a duration and updates the acceleration 
						if (stop_command.containsKey(entity)) {
							double dur = stop_command.get(entity);
							if (dur >= newState.instant && dur < t) {
								newState.acceleration.zero();
								newState.angularVelocity.zero();
								stop_command.remove(entity);
							}
						}

						try {
							movMemory.writeMemory(newState);
						} catch (MemoryException e) {
							e.printStackTrace();
						}

						// Sends an OSC message
						if (osc) {
							sendOSCPosition(entity, newState.position);
						}
						
						// Creates a response event if there is a sensor registered
						String[] sensors = searchRegisteredEventHandler(entity, "", MovementConstants.EVT_TYPE_MOVEMENT, Constants.COMP_SENSOR);
						for (int i = 0; i < sensors.length; i++) {
							EventHandlerInfo info = EventHandlerInfo.parse(sensors[i]);
							informAgent(info.agentName, info.componentName, newState);
						}

//						System.out.printf("pos = %s, vel = %s, acc = %s\n", newState.position, newState.velocity, newState.acceleration);
						
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

		executeMovementInstruction(cmd.getParameter("AGENT"), cmd.getCommand(), cmd.getParameters());

	}
	
}
