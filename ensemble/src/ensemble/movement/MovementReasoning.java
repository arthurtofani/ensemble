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

import java.util.ArrayList;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.EventHandler;
import ensemble.KnowledgeBase;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.world.Vector;


// TODO: Auto-generated Javadoc
/**
 * Given a set of waypoints and a time constraint to get there, this reasoning tries to walk.
 *
 * @author lfthomaz
 */
public class MovementReasoning extends Reasoning {

	/** The Constant EPSILON. */
	public static final double EPSILON = 1e-14;

	//	private KnowledgeBase kb;
	
	/** The legs. */
	private Actuator	legs;
	
	/** The eyes. */
	private Sensor 		eyes;
	
	/** The legs memory. */
	private Memory 		legsMemory;
	
	/** The eyes memory. */
	private Memory 		eyesMemory;
	
	// Waypoints
	/** The waypoints. */
	private ArrayList<Vector> 	waypoints = new ArrayList<Vector>();
	
	/** The time_constrains. */
	private ArrayList<Double> 	time_constrains = new ArrayList<Double>();
	
	/** The loop. */
	private boolean 			loop = false;
	
	/** The active_waypoint. */
	private int					active_waypoint = 0;;
	
	/** The precision. */
	private double 				precision = 0.01;
	
	/** The last_distance. */
	private double 				last_distance = Double.MAX_VALUE;
	
	/** The total_distance. */
	private double 				total_distance = 0.0;
	
	/** The last_acc. */
	private Vector 				last_acc;
	
	/** The inverted. */
	private boolean 			inverted;
	
	// 
	/** The actual_pos. */
	private Vector 				actual_pos = null;
	
	/** The actual_vel. */
	private Vector 				actual_vel = null;
	
	/** The actual_ori. */
	private Vector 				actual_ori = null;
	
	// 
	/** The max aceleration. */
	private double MAX_ACELERATION = 10.0;
	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	public boolean init() {
		
//		kb = getAgent().getKB();
		
		String str = getParameter("waypoints", null);
		if (str != null) {
			String[] wps = str.split(":");
			for (int i = 0; i < wps.length; i++) {
				String[] wp = wps[i].split(" "); 
				waypoints.add(Vector.parse(wp[0]));
				time_constrains.add(Double.valueOf(wp[1]));
//				System.out.println("add wp " + waypoints.get(i) + " - time " + time_constrains.get(i));
			}
			loop = Boolean.parseBoolean(getParameter("loop", "false"));
//			System.out.println("loop = " + loop);
		}
		
		
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			legs = (Actuator)evtHdl;
			legs.registerListener(this);
			legsMemory = getAgent().getKB().getMemory(legs.getComponentName());
		}
		else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			eyes = (Sensor)evtHdl;
			eyes.registerListener(this);
			eyesMemory = getAgent().getKB().getMemory(eyes.getComponentName());
		}
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		String str = (String)eyesMemory.readMemory(instant, TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
			actual_vel = Vector.parse(cmd.getParameter(MovementConstants.PARAM_VEL));
			actual_ori = Vector.parse(cmd.getParameter(MovementConstants.PARAM_ORI));
		}
//		System.out.println(getAgent().getAgentName() + " - new position " + actual_pos + " velocity " + actual_vel);

	}
	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		
		if (cmd.getCommand().equals(MovementConstants.CMD_WALK)) {
			if (cmd.containsParameter(MovementConstants.PARAM_POS) && cmd.containsParameter(MovementConstants.PARAM_TIME)) {
				sendStopCommand();
				waypoints.clear();
				time_constrains.clear();
				active_waypoint = 0;
				last_distance = Double.MAX_VALUE;
				loop = false;
				time_constrains.add(Double.valueOf(cmd.getParameter(MovementConstants.PARAM_TIME)));
				waypoints.add(Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS)));
			}
		}
		else if (cmd.getCommand().equals("ADD_WAYPOINT")) {
			System.out.println("[" + getAgent().getAgentName() + "] Add waypoint...");
			time_constrains.add(Double.valueOf(cmd.getParameter("time")));
			waypoints.add(Vector.parse(cmd.getParameter("wp")));
		}
		else if (cmd.getCommand().equals(MovementConstants.CMD_STOP)) {
			System.out.println("[" + getAgent().getAgentName() + "] Stoping...");
			waypoints.clear();
			time_constrains.clear();
			active_waypoint = 0;
			sendStopCommand();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	@Override
	public void process() {

		if (actual_pos == null) {
			String str = (String)eyesMemory.readMemory(eyesMemory.getLastInstant(), TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			if (cmd != null) {
				actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
				actual_vel = Vector.parse(cmd.getParameter(MovementConstants.PARAM_VEL));
				actual_ori = Vector.parse(cmd.getParameter(MovementConstants.PARAM_ORI));
			}
		}

		if (legsMemory != null && actual_pos != null && waypoints.size() != 0) {
			// Tenho destino?
			if (active_waypoint < waypoints.size()) {
				Vector dest_pos = waypoints.get(active_waypoint);
				double actual_distance = actual_pos.getDistance(dest_pos);
//				System.out.println("distance = " + actual_distance);
//				System.out.println("last_distance = " + last_distance);
				// Se passei da metade, desacelarar?
				if (!inverted && actual_distance < (total_distance/2)) {
					// Vou inverter a minha aceleração
//					System.out.println("METADE DO CAMINHO!!!");
					inverted = true;
					last_acc.inverse();
					sendAccCommand(last_acc, 0.0);
				}
				// Cheguei?
				if (actual_distance < precision || last_distance < actual_distance) {
					System.out.println("Cheguei no waypoint " + active_waypoint + " - " + waypoints.get(active_waypoint));
					// Parar o agente
					sendStopCommand();
					// Mudar o waypoint
					last_distance = Double.MAX_VALUE;
					active_waypoint++;
//					System.out.println("active wp = " + active_waypoint);
					if (active_waypoint == waypoints.size() && loop) {
						active_waypoint = 0;
					} else if (active_waypoint == waypoints.size() && !loop) {
						waypoints.clear();
						time_constrains.clear();
					}
				}
				else {
					// Estou parado ou passei
					if (actual_vel != null) {
						if (actual_vel.getMagnitude() == 0) {
							// TODO Mudar para o m�todo de Newton!!!
							// Calcular quanto e por quanto tempo devo acelerar
							double t = time_constrains.get(active_waypoint);
//							System.out.println("actual_pos = " + actual_pos + " - dest_pos = " + dest_pos + " - time_constraint = " + time_constrain);
							double acc_mag = MAX_ACELERATION;
							double t1 = 0.2; 
							boolean found = false;
							int iterations = 0;
							
							acc_mag = 1;
						    while (Math.abs((2*actual_distance-acc_mag*t*t) / (-t*t)) > EPSILON || iterations > 10) {
						        acc_mag = acc_mag - (2*actual_distance-acc_mag*t*t) / (-t*t);
//								System.out.println("["+iterations+"] acc_mag = " + acc_mag);
						        iterations++;
						    }
						    acc_mag = Math.min(acc_mag, MAX_ACELERATION);

							// Calcular a direção na qual deve andar
							total_distance = actual_pos.getDistance(dest_pos);
							Vector acc = new Vector((dest_pos.getValue(0)-actual_pos.getValue(0)), 
									(dest_pos.getValue(1)-actual_pos.getValue(1)), 
									(dest_pos.getValue(2)-actual_pos.getValue(2)));
							acc.normalizeVector();
							acc.product(acc_mag);
//							System.out.println("acc_vec = " + acc);
							// Enviar comando
							last_acc = acc;
							inverted = false;
							sendAccCommand(acc, 0.0);
//							sendAccCommand(acc, t1);
						} 
						else if (actual_vel.getMagnitude() > 0 && last_distance < actual_distance) {
							sendStopCommand();
						}
					}
					last_distance = actual_distance;
				}
			} 
			// Não tenho destino
			else {
				// Se estiver em movimento, parar
				if (actual_vel != null && actual_vel.getMagnitude() > 0) {
					sendStopCommand();
				}
			}
		
		}
		
	}
	
	/**
	 * Send stop command.
	 */
	private void sendStopCommand() {
		String cmd = MovementConstants.CMD_STOP;
//		System.out.println(cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send acc command.
	 *
	 * @param acc the acc
	 * @param dur the dur
	 */
	private void sendAccCommand(Vector acc, double dur) {
		String cmd = MovementConstants.CMD_WALK + 
			" :" + MovementConstants.PARAM_ACC + " " + acc.toString();
		if (dur > 0.0) {
			cmd += " :" + MovementConstants.PARAM_DUR + " " + Double.toString(dur);
		}
//		System.out.println("acc command: " + cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
		
}
