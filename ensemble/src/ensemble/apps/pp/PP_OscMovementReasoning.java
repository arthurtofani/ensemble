package ensemble.apps.pp;

 
import java.util.ArrayList;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.EventHandler;
import ensemble.KnowledgeBase;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.apps.pp.PP_FilterZoneReasoning.WorldZone;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.router.MessageConstants;
import ensemble.world.Vector;


/**
 * Given a set of waypoints and a time constraint to get there, this reasoning tries to walk
 * 
 *
 */
public class PP_OscMovementReasoning extends Reasoning {

	public static final double EPSILON = 1e-14;

	//	private KnowledgeBase kb;
	
	private Actuator	legs;
	private Sensor 		eyes;
	private Sensor 		antenna;
	
	private Memory 		legsMemory;
	private Memory 		eyesMemory;
	private Memory 		antennaMemory;
	
	// Waypoints
	private ArrayList<Vector> 	waypoints = new ArrayList<Vector>();
	private ArrayList<Double> 	time_constrains = new ArrayList<Double>();
	private boolean 			loop = false;
	private int					active_waypoint = 0;;
	private double 				precision = 0.01;
	private double 				last_distance = Double.MAX_VALUE;
	private double 				total_distance = 0.0;
	private Vector 				last_acc;
	private boolean 			inverted;
	
	// 
	private Vector 				actual_pos = null;
	private Vector 				actual_vel = null;
	private Vector 				actual_ori = null;
	
	// 
	private double MAX_ACELERATION = 10.0;
	
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
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}

	}

	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if (sourceSensor.getEventType().equals(
				MovementConstants.EVT_TYPE_MOVEMENT))
		{
		String str = (String)eyesMemory.readMemory(instant, TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
			actual_vel = Vector.parse(cmd.getParameter(MovementConstants.PARAM_VEL));
			actual_ori = Vector.parse(cmd.getParameter(MovementConstants.PARAM_ORI));
		}
		}else if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			
			
			if (cmd != null && cmd.getCommand()!=MessageConstants.CMD_INFO) {
				
				
				
				if (cmd.getParameter(MessageConstants.PARAM_TYPE).equals(MessageConstants.ANDOSC_TYPE)) {
					
					
					if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.ANDOSC_TOUCH_POS)) {
						
						/*System.out.println("Recebeu mensagem "
								+ cmd.getParameter(MessageConstants.PARAM_TYPE));
						*/	
						String[] val = cmd.getParameter(MessageConstants.PARAM_ARGS).split(" ");
						if(val.length ==2)
						{
						double valX = Double.parseDouble(val[0]);
						double valY = Double.parseDouble(val[1]);
						sendTransportCommand(getOscVector(100, valX,valY));
						}
					}
				}
			}
		}
		// System.out.println(getAgent().getAgentName() + " - new position " + actual_pos + " velocity " + actual_vel);

	}
	
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
	
	private void sendTransportCommand(Vector pos) {
		
		
		String cmd = MovementConstants.CMD_TRANSPORT+ 
			" :" + MovementConstants.PARAM_POS + " (" + pos.getValue(0)+ ";" + pos.getValue(1)+ ";" + pos.getValue(2)+ ")";
		
		System.out.println("transport command: " + cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
	
private Vector getOscVector(int size, double oscX, double oscY ){
		
			
		/*String str = (String)eyesMemory.readMemory(eyesMemory.getLastInstant(), TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
		}
		
*/		
		int x = 0;
		double valX = 50 - (oscX/232)*100;
		double valY = 50 - (oscY/296)*100;
		double valZ = 0;
					
		Vector v = new Vector(valY,valX, valZ );
		System.out.println("X: " + valX + " Y:" + valY + " Z:"+ valZ );
			return v; 
		
	}
		
	
}
