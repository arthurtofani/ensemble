package ensemble.apps.pp;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.router.MessageConstants;
import ensemble.world.Vector;
import ensemble.world.World;

public class PP_MovementReasoning extends Reasoning {

	private Actuator	legs;
	private Sensor 		eyes;
	private Sensor 		antenna;
	
	private Memory 		legsMemory;
	private Memory 		eyesMemory;
	private Memory 		antennaMemory;
	
	// Waypoints
	private ArrayList<Vector> waypoints = new ArrayList<Vector>();
	private ArrayList<Double> 	time_constrains = new ArrayList<Double>();
	private boolean 			loop = false;
	private int					active_waypoint = 0;;
	private double 				precision = 3.0;
	private double 				last_distance = 0.0; 
	
	// 
	private int 				state = 0;
	private Vector 			actual_pos = null;
	private Vector 			actual_vel = null;
	private Vector 			actual_ori = null;
	
	// 
	private double MAX_ACELERATION = 10.0;
	
	private static final String INST_TYPE_SEQUENCE = "SEQUENCE";
	private static final String INST_TYPE_REACH = "REACH";
	private static final String CONF_MUSICAL_AGENT_CLASS = "MUSICAL_AGENT_CLASS";
	private static final String CONF_MUSICAL_AGENT = "MUSICAL_AGENT";
	private static final String CONF_ACCELERATION = "ACCELERATION";
	
	
	public boolean init() {
		
		
		// Recupera instrucoes e monta a lista
		String instructionXmlStr = getAgent().getKB().readFact("instructions");

		if (instructionXmlStr != null) {

			Element elem_mms = loadXMLFile(instructionXmlStr)
					.getDocumentElement();

			NodeList nl = elem_mms
					.getElementsByTagName(CONF_MUSICAL_AGENT_CLASS);
			System.out.println("[INSTRUCTIONS TEST] NUMBER OF AGENT CLASSES : "
					+ nl.getLength());
			// itera pelas classes
			for (int index = 0; index < nl.getLength(); index++) {
				Element elem_arg = (Element)nl.item(index);
				
				
				// SEQUENCIA
				NodeList nlGroups = elem_arg.getElementsByTagName(INST_TYPE_SEQUENCE);
				
				System.out.println("[INSTRUCTIONS TEST] NUMBER OF SEQUENCE BLOCKS : "
						+ nlGroups.getLength());
				// itera pelos grupos de instrucoes
				for (int i = 0; i < nlGroups.getLength(); i++) {

					
					if (nlGroups.item(i).getNodeName() != null
							&& nlGroups.item(i).getNodeName() == INST_TYPE_SEQUENCE) {
						
						//REACH
						NodeList steps = ((Element)nlGroups.item(i)).getElementsByTagName(INST_TYPE_REACH);
						System.out.println("[INSTRUCTIONS TEST] NUMBER OF STEPS : "
								+ steps.getLength());
										
						// itera pelas sequencias
						for (int j = 0; j < steps.getLength();j++)
						{
							// REACH
							
								String x = readAttribute(steps.item(j), "X",
										null);
								String y = readAttribute(steps.item(j), "Y",
										null);
								String z = readAttribute(steps.item(j), "Z",
										null);
								String vel = readAttribute(steps.item(j),
										CONF_ACCELERATION, null);
								// (20;20;0) 2.0
								if (x != null && x != null && z != null) {
									waypoints.add(Vector.parse("(" + x + ";"
											+ y + ";" + z + ")"));
									time_constrains.add(Double.valueOf(vel));
									System.out.println("[INSTRUCTIONS TEST] ADDED POSITION :("
											+ x + ";" + y + ";" + z + ")");

								}
							
						}

					}
				}
			}
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
				MovementConstants.EVT_TYPE_MOVEMENT)) {
			String str = (String) eyesMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			if (cmd != null) {
				actual_pos = Vector.parse(cmd
						.getParameter(MovementConstants.PARAM_POS));
				actual_vel = Vector.parse(cmd
						.getParameter(MovementConstants.PARAM_VEL));
				actual_ori = Vector.parse(cmd
						.getParameter(MovementConstants.PARAM_ORI));
			}
		}else if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			
			if (cmd != null && cmd.getCommand()!=MessageConstants.CMD_INFO) {
			System.out.println("Recebeu mensagem " + cmd.getParameter(MessageConstants.PARAM_ARGS));
			
			}
			//sendTransportCommand(new Vector(3));
			
		}
		
		//System.out.println("New position " + actual_pos + " velocity " + actual_vel);

	}
	
	@Override
	public void process() {

		//System.out.println("Process " + actual_pos + " - " + waypoints.get(active_waypoint));
		
		if (legsMemory != null && actual_pos != null && waypoints.size() != 0) {
			// Tenho destino?
			if (active_waypoint < waypoints.size()) {
				Vector dest_pos = waypoints.get(active_waypoint);
				double actual_distance = actual_pos.getDistance(dest_pos);
				System.out.println("Process " + actual_pos + " - " + waypoints.get(active_waypoint)+ " - Distance : " + actual_distance);
				
				last_distance=actual_distance; 
				// Cheguei?
				if (actual_distance < precision) {
					//System.out.println("Cheguei no waypoint " + active_waypoint + " - " + waypoints.get(active_waypoint));
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
					//if (actual_vel.getMagnitude() == 0 || (actual_vel.getMagnitude() > 0 && last_distance > actual_distance)) {
						// TODO Mudar para o m�todo de Newton!!!
						// Calcular quanto e por quanto tempo devo acelerar
						double time_constrain = time_constrains.get(active_waypoint);
						System.out.println("actual_pos = " + actual_pos + " - dest_pos = " + dest_pos + " - time_constraint = " + time_constrain);
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
						System.out.println("acc_vec = " + acc);
						// Enviar comando
						sendAccCommand(acc, t1);
						
					//}
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
	
	@SuppressWarnings("unused")
	private void sendTransportCommand(Vector pos) {
		String cmd = MovementConstants.CMD_TRANSPORT+ 
			" :" + MovementConstants.PARAM_POS + " (0;0;0)";
		
		System.out.println("transport command: " + cmd);
		try {
			legsMemory.writeMemory(cmd);
			legs.act();
		} catch (MemoryException e) {
			e.printStackTrace();
		}
	}
	
	//Metodos auxiliares
		private static Document loadXMLFile(String xmlFile) {
		
		Document doc = null;
		
		//System.out.println("Loading instruction file for MMS: " + xmlFile);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(xmlFile);
			
			
		} catch (Exception e) {
			System.err.println("Error while loading XML instruction file!");
			System.err.println(e.toString());
			System.exit(-1);
		}
		
		return doc;
		
	}
		
		private static String readAttribute(Node elem, String attributeName, String defaultValue) {

			String ret;
			
			String attrib = ((Element)elem).getAttribute(attributeName);
			if (attrib != null && !attrib.equals("")) {
				ret = attrib;
			} else {
				System.out.println("\tParameter " + attributeName + " not found in configuration file...");
				ret = defaultValue;
			}
			
			return ret;

		}
}
