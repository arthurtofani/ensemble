package mms.tools;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mms.Constants;
import mms.EnvironmentAgent;
import mms.MusicalAgent;
import mms.Parameters;
import mms.commands.Console;

/**
 * Runs the system.
 * @author Leandro
 *
 */
// TODO Criação de um GUI para ver o estado de todos os agentes presentes no sistema e para a criação/destruição de agentes
public class Loader {

	// Configuration File's Constants
	private static final String CONF_GLOBAL_PARAMETERS = "GLOBAL_PARAMETERS";
	private static final String CONF_ENVIRONMENT_AGENT_CLASS = "ENVIRONMENT_AGENT_CLASS";
	private static final String CONF_MUSICAL_AGENT_CLASS = "MUSICAL_AGENT_CLASS";
	private static final String CONF_MUSICAL_AGENT = "MUSICAL_AGENT";
	private static final String CONF_EVENT_SERVER = "EVENT_SERVER";
	private static final String CONF_COMPONENTS= "COMPONENTS";
	private static final String CONF_COMP_REASONING = "REASONING";
	private static final String CONF_COMP_SENSOR = "SENSOR";
	private static final String CONF_COMP_ACTUATOR = "ACTUATOR";
	private static final String CONF_COMP_EVENT_TYPE = "EVENT_TYPE";
	private static final String CONF_COMP = "COMP";
	private static final String CONF_NAME = "NAME";
	private static final String CONF_CLASS = "CLASS";
	private static final String CONF_COMM = "COMM";
	private static final String CONF_PERIOD = "PERIOD";
	private static final String CONF_ARG = "ARG";
	private static final String CONF_ARG_COMP = "ARG_COMP";
	private static final String CONF_VALUE = "VALUE";
	private static final String CONF_KB = "KB";
	private static final String CONF_FACT = "FACT";
	private static final String CONF_PUBLIC = "PUBLIC";
	private static final String CONF_REAS_CYCLIC = "CYCLIC";
	
//	private Logger logger = Logger.getLogger("");

	// JADE Variables
	private static Runtime rt = null;
	private static Profile p = null;
	private static ContainerController cc = null;
	
	//--------------------------------------------------------------------------------
	// System initialization / termination
	//--------------------------------------------------------------------------------
	
	private static void startJADE() {

		// Cria o Container JADE
		rt = Runtime.instance();
		p = new ProfileImpl();
		p.setParameter(Profile.MAIN_HOST, "localhost");
		p.setParameter(Profile.SERVICES, "mms.clock.VirtualClockService;mms.comm.direct.CommDirectService;mms.commands.RouterService;mms.osc.OSCServerService");
		cc = rt.createMainContainer(p);
		
	}
	
	private static void stopJADE() {
		
		rt.shutDown();
		
	}
	
	private static String readAttribute(Element elem, String attributeName, String defaultValue) {

		String ret;
		
		String attrib = elem.getAttribute(attributeName);
		if (attrib != null && !attrib.equals("")) {
			ret = attrib;
		} else {
			System.out.println("\tParameter " + attributeName + " not found in configuration file...");
			ret = defaultValue;
		}
		
		return ret;

	}
	
	private static Parameters readArguments(Element elem) {

		Parameters parameters = new Parameters();
		
		NodeList nl_attrib = elem.getElementsByTagName(CONF_ARG);
		for (int j = 0; j < nl_attrib.getLength(); j++) {
			Element elem_arg = (Element)nl_attrib.item(j);
			parameters.put(readAttribute(elem_arg, CONF_NAME, null), readAttribute(elem_arg, CONF_VALUE, null));
		}
		
		return parameters;
	
	}
	
	private static Parameters readComponentArguments(Element elem, String component) {

		Parameters parameters = new Parameters();
		
		NodeList nl_attrib = elem.getElementsByTagName(CONF_ARG_COMP);
		for (int j = 0; j < nl_attrib.getLength(); j++) {
			Element elem_arg = (Element)nl_attrib.item(j);
			String comp = readAttribute(elem_arg, CONF_COMP, ""); 
			if (comp.equals(component)) {
				parameters.put(readAttribute(elem_arg, CONF_NAME, null), readAttribute(elem_arg, CONF_VALUE, null));
			}
		}
		
		return parameters;
	
	}
	
	private static Parameters readFacts(Element elem) {

		Parameters parameters = new Parameters();
		
		NodeList nl_attrib = elem.getElementsByTagName(CONF_FACT);
		for (int j = 0; j < nl_attrib.getLength(); j++) {
			Element elem_arg = (Element)nl_attrib.item(j);
			parameters.put(readAttribute(elem_arg, CONF_NAME, null), readAttribute(elem_arg, CONF_VALUE, null));
		}
		
		return parameters;
	
	}
	
	private static Document loadXMLFile(String xmlFile) {
		
		Document doc = null;
		
		System.out.println("Loading configuration file for MMS: " + xmlFile);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(xmlFile);
		} catch (Exception e) {
			System.err.println("Error while loading XML file!");
			System.err.println(e.toString());
			System.exit(-1);
		}
		
		return doc;
		
	}
	
	private static void loadSystem(Document doc) {
		
		NodeList nl;
		Element elem_mms = doc.getDocumentElement();

		// Load Global Parameters	
		nl = elem_mms.getElementsByTagName(CONF_GLOBAL_PARAMETERS);
		if (nl.getLength() == 1) {
			Element elem_gp = (Element)nl.item(0);
			
			p.setParameter(Constants.CLOCK_MODE, readAttribute(elem_gp, Constants.CLOCK_MODE, Constants.CLOCK_CPU));
			p.setParameter(Constants.PROCESS_MODE, readAttribute(elem_gp, Constants.PROCESS_MODE, Constants.MODE_REAL_TIME));
			p.setParameter(Constants.OSC, readAttribute(elem_gp, Constants.OSC, "FALSE"));
		}
		
		// Load Environment Agent
		nl = elem_mms.getElementsByTagName(CONF_ENVIRONMENT_AGENT_CLASS);
		if (nl.getLength() == 1) {
			Element elem_ea = (Element)nl.item(0);

			String ea_name = readAttribute(elem_ea, CONF_NAME, "Environment");
			String ea_class = readAttribute(elem_ea, CONF_CLASS, "mms.EnvironmentAgent");
			Parameters ea_parameters = readArguments(elem_ea);
			
			try {
				// Criar nova instância do EA solicitado
				Class eaClass = Class.forName(ea_class);
				EnvironmentAgent ea = (EnvironmentAgent)eaClass.newInstance();
				Object[] arguments;
				arguments = new Object[1];
				arguments[0] = ea_parameters;
				ea.setArguments(arguments);
				
				// Load World Parameters
				nl = elem_ea.getElementsByTagName("WORLD");
				if (nl.getLength() == 1) {
					Element elem_gp = (Element)nl.item(0);
					ea_parameters.put(Constants.CLASS_WORLD, readAttribute(elem_gp, Constants.CLASS_WORLD, "mms.world.World"));
					ea_parameters.put(Constants.CLASS_ENTITY_STATE, readAttribute(elem_gp, Constants.CLASS_WORLD, "mms.world.EntityState"));
					nl = elem_gp.getElementsByTagName("LAW");
					String laws = "";
					for (int i = 0; i < nl.getLength(); i++) {
						Element elem_law = (Element)nl.item(i);
						laws = laws + readAttribute(elem_law, CONF_CLASS, null);
						if (i != nl.getLength()-1) {
							laws = laws + " ";
						}
					}
					ea_parameters.put("LAW", laws);
				}
								
				// Load Event Servers
				nl = elem_ea.getElementsByTagName(CONF_EVENT_SERVER);
				for (int i = 0; i < nl.getLength(); i++) {
					Element elem_es = (Element)nl.item(i);
					String es_class = readAttribute(elem_es, CONF_CLASS, null);
					String es_comm_class = readAttribute(elem_es, CONF_COMM, "mms.comm.CommMessage");
					String es_period = readAttribute(elem_es, CONF_PERIOD, "");
					if (es_class == null) {
						System.err.println("ERROR: Event Server class not defined");
					} else {
						Parameters parameters = new Parameters();
						parameters.put(Constants.PARAM_COMM_CLASS, es_comm_class);
						parameters.put(Constants.PARAM_PERIOD, es_period);
						parameters.merge(readArguments(elem_es));
						ea.addEventServer(es_class, parameters);
					}
				}

				// Inserir o Agente no Jade
				AgentController ac = cc.acceptNewAgent(ea_name, ea);
				ac.start();

			} catch (ClassNotFoundException e) {
				System.err.println("FATAL ERROR: Class " + ea_class + " not found");
				System.exit(-1);
			} catch (InstantiationException e) {
				System.err.println("FATAL ERROR: Not possible to create an instance of " + ea_class);
				System.exit(-1);
			} catch (IllegalAccessException e) {
				System.err.println("FATAL ERROR: Not possible to create an instance of " + ea_class);
				System.exit(-1);
			} catch (StaleProxyException e) {
				System.err.println("FATAL ERROR: Not possible to insert agent " + ea_class + " in JADE");
				System.exit(-1);
			}

		} else {
			System.err.println("\tERROR: No Environment Agent defined...");
			stopJADE();
			System.exit(-1);
		}
		
		// Load Musical Agents
		nl = elem_mms.getElementsByTagName(CONF_MUSICAL_AGENT);
		for (int i = 0; i < nl.getLength(); i++) {

			Element elem_ma = (Element)nl.item(i);
			String ma_class = readAttribute(elem_ma, CONF_CLASS, null);
			String ma_name = readAttribute(elem_ma, CONF_NAME, ma_class+"_"+i);
			Parameters parameters = readArguments(elem_ma);
			
			// Le os facts a serem carregados na KB
			Parameters facts = readFacts(elem_ma);

			try {
				// Procurar a classe correspondente a esta instância
				NodeList nl_ma_class = elem_mms.getElementsByTagName(CONF_MUSICAL_AGENT_CLASS);
				for (int j = 0; j < nl_ma_class.getLength(); j++) {
					Element elem_ma_class = (Element)nl_ma_class.item(j);
					String ma_class_name = readAttribute(elem_ma_class, CONF_NAME, null);
					if (ma_class_name.equals(ma_class)) {
						
						// Criar nova instância do MA solicitado
						String ma_class_class = readAttribute(elem_ma_class, CONF_CLASS, "mms.rt.MusicalAgent");
						Class maClass = Class.forName(ma_class_class);
						MusicalAgent ma = (MusicalAgent)maClass.newInstance();
						Object[] arguments;
						arguments = new Object[1];
						arguments[0] = parameters;
						ma.setArguments(arguments);
						
						System.out.println("Class " + ma_class_name);
						
						// Preenche a KB do agente
						NodeList nl_ma_class_kb = elem_ma_class.getElementsByTagName(CONF_KB);
						if (nl_ma_class_kb.getLength() == 1) {
							Element elem_ma_class_kb = (Element)nl_ma_class_kb.item(0);
							NodeList nl_facts = elem_ma_class_kb.getElementsByTagName(CONF_FACT);
							for (int k = 0; k < nl_facts.getLength(); k++) {
								// Cria o fact na KB
								Element elem_fact = (Element)nl_facts.item(k);
								String fact_name = elem_fact.getAttribute(CONF_NAME);
								String fact_value = elem_fact.getAttribute(CONF_VALUE);
								Boolean fact_public = Boolean.valueOf(elem_fact.getAttribute(CONF_PUBLIC));
								ma.getKB().registerFact(fact_name, fact_value, fact_public);
								// Verifica se existe algum fact a ser sobrescrito para esta instância
								if (facts.containsKey(fact_name)) {
									ma.getKB().updateFact(fact_name, facts.get(fact_name));
								}
							}
						}
						
						// Inserir Componentes Musicais no MA
						NodeList nl_ma_class_comps = elem_ma_class.getElementsByTagName(CONF_COMPONENTS);
						if (nl_ma_class_comps.getLength() == 1) {
							Element elem_ma_class_comps = (Element)nl_ma_class_comps.item(0);
							
							// Inserir os Reasonings
							NodeList nl_reasonings = elem_ma_class_comps.getElementsByTagName(CONF_COMP_REASONING);
							for (int k = 0; k < nl_reasonings.getLength(); k++) {
								Element elem_reasoning = (Element)nl_reasonings.item(k);
								Parameters args = readArguments(elem_reasoning);
								String comp_name = readAttribute(elem_reasoning, CONF_NAME, null);
								System.out.println("\tREASONING " + comp_name);
								Parameters args_comp = readComponentArguments(elem_ma, comp_name);
								args.merge(args_comp);
								String comp_class = readAttribute(elem_reasoning, CONF_CLASS, null);
								args.put(Constants.PARAM_REAS_CYCLIC, readAttribute(elem_reasoning, CONF_REAS_CYCLIC, "false"));
								ma.addComponent(comp_name, comp_class, args);
							} 
							
							// Inserir os Sensors
							NodeList nl_sensors = elem_ma_class_comps.getElementsByTagName(CONF_COMP_SENSOR);
							for (int k = 0; k < nl_sensors.getLength(); k++) {
								Element elem_sensor = (Element)nl_sensors.item(k);
								Parameters args = readArguments(elem_sensor);
								String comp_name = readAttribute(elem_sensor, CONF_NAME, "Sensor");
								System.out.println("\tSENSOR " + comp_name);
								Parameters args_comp = readComponentArguments(elem_ma, comp_name);
								args.merge(args_comp);
								String comp_class = readAttribute(elem_sensor, CONF_CLASS, "mms.Sensor");
								String comp_event_type = readAttribute(elem_sensor, CONF_COMP_EVENT_TYPE, "DUMMY");
								args.put(Constants.PARAM_EVT_TYPE, comp_event_type);
								args.put(Constants.PARAM_COMM_CLASS, comp_event_type);
								ma.addComponent(comp_name, comp_class, args);
							}

							// Inserir os Actuators
							NodeList nl_actuators = elem_ma_class_comps.getElementsByTagName(CONF_COMP_ACTUATOR);
							for (int k = 0; k < nl_actuators.getLength(); k++) {
								Element elem_actuator = (Element)nl_actuators.item(k);
								Parameters args = readArguments(elem_actuator);
								String comp_name = readAttribute(elem_actuator, CONF_NAME, "Actuator");
								System.out.println("\tACTUATOR " + comp_name);
								Parameters args_comp = readComponentArguments(elem_ma, comp_name);
								args.merge(args_comp);
								String comp_class = readAttribute(elem_actuator, CONF_CLASS, "mms.Actuator");
								String comp_event_type = readAttribute(elem_actuator, CONF_COMP_EVENT_TYPE, "DUMMY");
								args.put(Constants.PARAM_EVT_TYPE, comp_event_type);
								args.put(Constants.PARAM_COMM_CLASS, comp_event_type);
								ma.addComponent(comp_name, comp_class, args);
							}
							
						}

						// Inserir o Agente no Jade
						AgentController ac = cc.acceptNewAgent(ma_name, ma);
						ac.start();
						
						break;
					}
				}
				
				
			} catch (ClassNotFoundException e) {
				System.err.println("FATAL ERROR: Class " + ma_class + " not found");
				System.exit(-1);
			} catch (InstantiationException e) {
				System.err.println("FATAL ERROR: Not possible to create an instance of " + ma_class);
				System.exit(-1);
			} catch (IllegalAccessException e) {
				System.err.println("FATAL ERROR: Not possible to create an instance of " + ma_class);
				System.exit(-1);
			} catch (StaleProxyException e) {
				System.err.println("FATAL ERROR: Not possible to insert agent " + ma_class + " in JADE");
				System.exit(-1);
			}
		}
		
	}
	
	/**
	 * Gracefully exit the MMS and Jade system
	 */
	private void terminate() {
		stopJADE();
		System.out.println("Exiting MMS...");
		System.exit(0);
	}
	
	//--------------------------------------------------------------------------------
	// Factory methods / Change agents during running time
	//--------------------------------------------------------------------------------
	
	public void createEnvironmentAgent(String agentClass, String agentName, Parameters parameters) {
		
		// Checa se a classe é realmente um EnvironmentAgent
		
		// Cria uma instância no Jade
		
		// SINGLETON
		
	}
	
	public void createEventServer() {
		
	}
	
	public void deleteEventServer() {
		
	}
	
	public void createAgent() {

		// Passar o pedido para o Agente Ambiente
		
	}
	
	public void deleteAgent() {

		// Passar o pedido para o Agente Ambiente
		
	}
	
	public void addComponent(String musicalAgent, String componentClass, String componentName, Parameters parameters) {
		
	}
	
	public void removeComponent() {
		
	}
	
	//--------------------------------------------------------------------------------
	// Main method
	//--------------------------------------------------------------------------------

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// check the program arguments
		if (args.length != 1) {
			System.out.println("Loader usage: java Loader <mms.properties>");
			System.exit(-1);
		}
		
		// load the configuration file
		Properties properties = new Properties();
		try {
			FileInputStream in = new FileInputStream(new File(args[0]));
			properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("Loader: file '" + args[0] + "' not found!");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Loader: problems reading file '" + args[0] + "'!");
			System.exit(-1);
		}
				
		// Start the system
		System.out.println("------------ Loading MMS ------------");
		Loader.startJADE();
		Loader.loadSystem(Loader.loadXMLFile(args[0]));
		
		// TODO Colocar alguma condição para que o usuário possa encerrar a execução do sistema
//		while (true) {}

	}
	
}