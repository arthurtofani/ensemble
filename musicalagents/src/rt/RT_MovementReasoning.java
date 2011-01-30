package rt;

import java.util.ArrayList;

import javax.activity.ActivityRequiredException;

import mms.Actuator;
import mms.Constants;
import mms.Event;
import mms.EventHandler;
import mms.MusicalAgent;
import mms.Reasoning;
import mms.Sensor;
import mms.clock.TimeUnit;
import mms.commands.Command;
import mms.kb.Memory;
import mms.kb.MemoryException;
import mms.world.Vector;

/**
 * Given a set of waypoints and a time constraint to get there, this reasoning tries to walk
 * @author lfthomaz
 *
 */
public class RT_MovementReasoning extends Reasoning {

	private Actuator	legs;
	private Sensor 		eyes;
	
	private Memory 		legsMemory;
	private Memory 		eyesMemory;
	
	// Waypoints
	private ArrayList<Vector> waypoints = new ArrayList<Vector>();
	private ArrayList<Double> 	time_constrains = new ArrayList<Double>();
	private boolean 			loop = false;
	private int					active_waypoint = 0;;
	private double 				precision = 1.0;
	private double 				last_distance = 0.0; 
	
	// 
	private int 				state = 0;
	private Vector 			actual_pos = null;
	private Vector 			actual_vel = null;
	private Vector 			actual_ori = null;
	
	// 
	private double MAX_ACELERATION = 10.0;
	
	public boolean init() {
		
		// Adiciona alguns waypoints para fazer um círculo
//		waypoints.add(new Vector(0,-40,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(28.2842712474,-28.2842712474,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(40,0,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(28.2842712474,28.2842712474,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(0,40,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(-28.2842712474,28.2842712474,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(-40,0,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(-28.2842712474,-28.2842712474,0));
//		time_constrains.add(4.0);
//		waypoints.add(new Vector(30,40,0));
//		time_constrains.add(8.0);
//		waypoints.add(new Vector(-40,-30,0));
//		time_constrains.add(7.0);
//		waypoints.add(new Vector(60,-80,0));
//		time_constrains.add(10.0);
//		loop = true;
		
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

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals("MOVEMENT")) {
			legs = (Actuator)evtHdl;
			legs.registerListener(this);
			legsMemory = getAgent().getKB().getMemory(legs.getName());
		}
		else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals("MOVEMENT")) {
			eyes = (Sensor)evtHdl;
			eyes.registerListener(this);
			eyesMemory = getAgent().getKB().getMemory(eyes.getName());
		}
	}

	@Override
	public void newSense(String eventType, double instant, double duration) {
		
		String str = (String)eyesMemory.readMemory(instant, TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter("pos"));
			actual_vel = Vector.parse(cmd.getParameter("vel"));
			actual_ori = Vector.parse(cmd.getParameter("ori"));
		}
//		System.out.println("New position " + actual_pos + " velocity " + actual_vel);

	}
	
	@Override
	public void process() {
		// Percorre os waypoints programados através de um movimento uniforme (pequena aceleração para obter um velocidade constante)
		// Definir uma aceleração máxima para o agente
//		System.out.println("[" + getAgent().getLocalName() + "] Entrei!" );
//		if (legsMemory != null && actual_pos != null && active_waypoint < waypoints.size()) {
//			System.out.println("[" + getAgent().getLocalName() + "] " + legsMemory + " " + actual_pos + "" + active_waypoint + " " + waypoints.size());
			// Verificar se já cheguei onde queria
//			Vector dest_pos = waypoints.get(active_waypoint);
//			double dist_to_wp = actual_pos.getDistance(dest_pos);
//			System.out.println("Distance = " + dist_to_wp);
//			if (actual_pos.getDistance(waypoints.get(active_waypoint)) < precision) {
//				System.out.println("Cheguei no waypoint " + active_waypoint + " - " + waypoints.get(active_waypoint));
//				String cmd = "STOP";
//				try {
//					legsMemory.writeMemory(cmd);
//				} catch (MemoryException e) {
//					e.printStackTrace();
//				}
//				legs.act();
//				active_waypoint++;
//				if (active_waypoint == waypoints.size() && loop) {
//					System.out.println("ZEREI!");
//					active_waypoint = 0;
//				}
//			} else {
//				// Calcular a aceleração necessária para chegar no wp, considerando o tempo previsto
//				// TODO Como vamos considerar o atrito aqui?!?! Em um primeiro instante, sem atrito!
//				double time = time_constrains.get(active_waypoint);
//				System.out.println("actual_pos = " + actual_pos + " - dest_pos = " + dest_pos + " - time_constraint = " + time);
////				System.out.println("dist_to_wp = " + dist_to_wp);
//				double acc_mag = (2 * dist_to_wp) / (time * time);
////				System.out.println("acc_mag = " + acc_mag);
//				double acc_dur = 0;
//				Vector acc = new Vector((dest_pos.getX()-actual_pos.getX()), 
//						(dest_pos.getY()-actual_pos.getY()), 
//						(dest_pos.getZ()-actual_pos.getZ()))
//						.getNormalizedVector();
////				System.out.println("acc_vec = " + acc);
//				acc = acc.product(acc_mag);
//				System.out.println("acc_vec = " + acc);
//
//				// Enviar o comand
//				String cmd = "WALK :acc " + acc.toString() + " :dur " + acc_dur;
//				try {
//					legsMemory.writeMemory(cmd);
//				} catch (MemoryException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				legs.act();
//			}
//				
//		}
				
//		try {
//			
//			if (state == 0) {
//				String cmd = "WALK :acc (10.0;0.0;0) :dur 2";
////					String cmd = "TURN :ang_vel (0;0;0.1745329252) :dur 1";
//				legsMemory.writeMemory(cmd);
//				legs.act();
//				state = 3;
//			} else if (state == 1) {
//				// Vira no eixo Z com uma velocida de 1 rad/s
////					String cmd = "TURN :ang_vel (0;0;0.1745329252) :dur 1";
//				String cmd = "WALK :acc (-2.0;0;0) :dur 1";
//				legsMemory.writeMemory(cmd);
//				legs.act();
//				state = 3;
//			} else if (state == 2) {
//				String cmd = "STOP";
//				legsMemory.writeMemory(cmd);
//				legs.act();
//				state = 3;
//			}
//			Thread.sleep(10000);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if (legsMemory != null && actual_pos != null && waypoints.size() != 0) {
			// Tenho destino?
			if (active_waypoint < waypoints.size()) {
				Vector dest_pos = waypoints.get(active_waypoint);
				double actual_distance = actual_pos.getDistance(dest_pos);
				// Cheguei?
				if (actual_distance < precision) {
//					System.out.println("Cheguei no waypoint " + active_waypoint + " - " + waypoints.get(active_waypoint));
					// Parar o agente
					sendStopCommand();
					// Mudar o waypoint
					last_distance = 0.0;
					active_waypoint++;
//					System.out.println("active wp = " + active_waypoint);
					if (active_waypoint == waypoints.size() && loop) {
						active_waypoint = 0;
					}
				}
				else {
					// Estou parado ou passei
					if (actual_vel.getMagnitude() == 0 || (actual_vel.getMagnitude() > 0 && last_distance > actual_distance)) {
						// TODO Mudar para o m�todo de Newton!!!
						// Calcular quanto e por quanto tempo devo acelerar
						double time_constrain = time_constrains.get(active_waypoint);
//						System.out.println("actual_pos = " + actual_pos + " - dest_pos = " + dest_pos + " - time_constraint = " + time);
//						System.out.println("dist_to_wp = " + dist_to_wp);
						double acc_mag = MAX_ACELERATION;
						double t1 = 0.2; 
						boolean found = false;
						int iterations = 0;
						
						while (!found && iterations < 10) {
							// Movimento é composto de um MUV + MU
							// S = (a*t1ˆ2)/2 + a*t1*t2, sendo que t1+t2 deve ser aprox. o time_constraint
							// Se t1+t2 for maior, vou aumentar o t1, se for menor, vou diminuir acc_mag
							double t2 = (actual_distance - (acc_mag * t1 * t1 / 2)) / (acc_mag*t1);
							if (Math.abs(time_constrain-t1-t2) < 0.1) {
								found = true;
							} else {
								if (t1+t2 < time_constrain) {
									acc_mag = acc_mag - 2.0;
								} else {
									t1 = t1 + 0.2;
								}
							}
							iterations++;
						}

						// Calcular a direção na qual deve andar
						Vector acc = new Vector((dest_pos.getValue(0)-actual_pos.getValue(0)), 
								(dest_pos.getValue(1)-actual_pos.getValue(1)), 
								(dest_pos.getValue(2)-actual_pos.getValue(2)));
						acc.normalizeVector();
						acc.product(acc_mag);
//						System.out.println("acc_vec = " + acc);
						// Enviar comando
						sendAccCommand(acc, t1);
						
					}
				}
			}
			else {
				// Estou em movimento?
				if (actual_vel.getMagnitude() > 0) {
					sendStopCommand();
				}
			}
		
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	private void sendStopCommand() {
		String cmd = "STOP";
//		System.out.println(cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
	
	private void sendAccCommand(Vector acc, double dur) {
		String cmd = "WALK :acc " + acc.toString() + " :dur " + Double.toString(dur);
//		System.out.println(cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
	
}
