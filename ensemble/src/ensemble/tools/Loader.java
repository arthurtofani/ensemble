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

package ensemble.tools;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.tools.sniffer.ExitAction;
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

import ensemble.Constants;
import ensemble.EnvironmentAgent;
import ensemble.MusicalAgent;
import ensemble.Parameters;


/*** Runs the system.
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
	private static final String CONF_QUANTITY = "QUANTITY";
	
//	private Logger logger = Logger.getLogger("");

	// JADE Variables
	private static Runtime rt = null;
	private static Profile p = null;
	private static ContainerController cc = null;
	
	//--------------------------------------------------------------------------------
	// System initialization / termination
	//--------------------------------------------------------------------------------
	
	private static void startJADE(Element elem_ensemble, boolean nogui) {

		// Cria o Container JADE
		rt = Runtime.instance();
		p = new ProfileImpl();
		p.setParameter(Profile.MAIN_HOST, "localhost");
		String services = "ensemble.clock.VirtualClockService;" +
							"ensemble.comm.direct.CommDirectService;";

		// Load Global Parameters	
		NodeList nl = elem_ensemble.getElementsByTagName(CONF_GLOBAL_PARAMETERS);
		if (nl.getLength() == 1) {
			Element elem_gp = (Element)nl.item(0);
			p.setParameter(Constants.CLOCK_MODE, readAttribute(elem_gp, Constants.CLOCK_MODE, Constants.CLOCK_CPU));
			p.setParameter(Constants.PROCESS_MODE, readAttribute(elem_gp, Constants.PROCESS_MODE, Constants.MODE_REAL_TIME));
			p.setParameter(Constants.SCHEDULER_THREADS, readAttribute(elem_gp, Constants.SCHEDULER_THREADS, "5"));
		}
	
		p.setParameter(Profile.SERVICES, services);

		cc = rt.createMainContainer(p);
		
		// Creates special agents (they live outside the Virtual Environment)
		AgentController ac;
		try {
			// Command Router
			ac = cc.createNewAgent("Router", "ensemble.router.RouterAgent", null);
			ac.start();
			// Sniffer
			if (!nogui) {
				ac = cc.createNewAgent("Sniffer", "ensemble.sniffer.Sniffer", null);
				ac.start();
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
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
//			System.out.println("\tParameter " + attributeName + " not found in configuration file...");
			ret = defaultValue;
		}
		
		return ret;

	}
	
	private static Parameters readArguments(Element elem) {

		Parameters parameters = new Parameters();
		
		NodeList nl_attrib = elem.getElementsByTagName(CONF_ARG);
		for (int j = 0; j < nl_attrib.getLength(); j++) {
			Element elem_arg = (Element)nl_attrib.item(j);
			if (elem_arg.getParentNode() == elem) {
				parameters.put(readAttribute(elem_arg, CONF_NAME, null), readAttribute(elem_arg, CONF_VALUE, null));
			}
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
		
		System.out.println("[Loader] Loading configuration file for Ensemble: " + xmlFile);
		
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(xmlFile);
		} catch (Exception e) {
			System.err.println("Error while loading XML file!");
			System.err.println(e.toString());
			System.exit(1);
		}
		
		return doc;
		
	}
	
	private static void loadSystem(Element elem_ensemble) {
		
		NodeList nl;

		// Load Environment Agent
		nl = elem_ensemble.getElementsByTagName(CONF_ENVIRONMENT_AGENT_CLASS);
		if (nl.getLength() == 1) {
			Element elem_ea = (Element)nl.item(0);

			String ea_name = Constants.ENVIRONMENT_AGENT;
			String ea_class = readAttribute(elem_ea, CONF_CLASS, "ensemble.EnvironmentAgent");
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
					String world_class = readAttribute(elem_gp, CONF_CLASS, null);
					if (world_class == null) {
						System.err.println("ERROR: World class not defined");
						world_class = "ensemble.world.World";
					}
					Parameters world_param = readArguments(elem_gp);
					ea.addWorld(world_class, world_param);
					nl = elem_gp.getElementsByTagName("LAW");
					if (nl.getLength() > 0) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element elem_law = (Element)nl.item(i);
							String law_class = readAttribute(elem_law, CONF_CLASS, null);
							Parameters law_param = readArguments(elem_law);
							ea.getWorld().addLaw(law_class, law_param);
						}
					}
				}
								
				// Load Event Servers
				nl = elem_ea.getElementsByTagName(CONF_EVENT_SERVER);
				for (int i = 0; i < nl.getLength(); i++) {
					Element elem_es = (Element)nl.item(i);
					String es_class = readAttribute(elem_es, CONF_CLASS, null);
					String es_comm_class = readAttribute(elem_es, CONF_COMM, "ensemble.comm.CommMessage");
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
		nl = elem_ensemble.getElementsByTagName(CONF_MUSICAL_AGENT);
		for (int i = 0; i < nl.getLength(); i++) {

			Element elem_ma = (Element)nl.item(i);
			String ma_class = readAttribute(elem_ma, CONF_CLASS, "ensemble.MusicalAgent");
			String ma_name_pre = readAttribute(elem_ma, CONF_NAME, ma_class);
			int ma_quantiy = Integer.valueOf(readAttribute(elem_ma, CONF_QUANTITY, "1"));
			
			for (int qty = 0; qty < ma_quantiy; qty++) {
				
				String ma_name;
				if (ma_quantiy > 1) {
					ma_name = ma_name_pre + "_" + qty;
				} else {
					ma_name = ma_name_pre;
				}
				
				// Reads Musical Agent instance arguments
				Parameters parameters = readArguments(elem_ma);
				
				// Le os facts a serem carregados na KB
				Parameters facts = readFacts(elem_ma);
	
				try {
					// Procurar a classe correspondente a esta instância
					boolean found_class = false;
					NodeList nl_ma_class = elem_ensemble.getElementsByTagName(CONF_MUSICAL_AGENT_CLASS);
					for (int j = 0; j < nl_ma_class.getLength(); j++) {
						Element elem_ma_class = (Element)nl_ma_class.item(j);
						String ma_class_name = readAttribute(elem_ma_class, CONF_NAME, null);
						if (ma_class_name.equals(ma_class)) {
							found_class = true;
							// Criar nova instância do MA solicitado
							String ma_class_class = readAttribute(elem_ma_class, CONF_CLASS, "ensemble.rt.MusicalAgent");
							Class maClass = Class.forName(ma_class_class);
							MusicalAgent ma = (MusicalAgent)maClass.newInstance();
							Parameters class_parameters = readArguments(elem_ma_class);
							parameters.merge(readArguments(elem_ma_class));
							// Coloca os argumentos (os da instância tem precedência sobre os da classe
							Object[] arguments;
							arguments = new Object[1];
							arguments[0] = parameters;
							ma.setArguments(arguments);
							
//							System.out.println("[Loader] Class " + ma_class_name);
							
							// Preenche a KB do agente
							NodeList nl_ma_class_kb = elem_ma_class.getElementsByTagName(CONF_KB);
							if (nl_ma_class_kb.getLength() == 1) {
								Element elem_ma_class_kb = (Element)nl_ma_class_kb.item(0);
								String kb_class = readAttribute(elem_ma_class_kb, CONF_CLASS, "ensemble.KnowledgeBase");
								Parameters args = readArguments(elem_ma_class_kb);
								ma.addKB(kb_class, args);
//								System.out.println("\tKNOWLEDGE_BASE" + "KnowledgeBase");
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
//									System.out.println("\tREASONING " + comp_name);
									Parameters args_comp = readComponentArguments(elem_ma, comp_name);
									args.merge(args_comp);
									String comp_class = readAttribute(elem_reasoning, CONF_CLASS, null);
									args.put(Constants.PARAM_REASONING_MODE, readAttribute(elem_reasoning, Constants.PARAM_REASONING_MODE, "REACTIVE"));
									if (args.get(Constants.PARAM_REASONING_MODE).equals("PERIODIC")) {
										args.put(Constants.PARAM_PERIOD, readAttribute(elem_reasoning, Constants.PARAM_PERIOD, "100"));
									}
									ma.addComponent(comp_name, comp_class, args);
								} 
								
								// Inserir os Sensors
								NodeList nl_sensors = elem_ma_class_comps.getElementsByTagName(CONF_COMP_SENSOR);
								for (int k = 0; k < nl_sensors.getLength(); k++) {
									Element elem_sensor = (Element)nl_sensors.item(k);
									Parameters args = readArguments(elem_sensor);
									String comp_name = readAttribute(elem_sensor, CONF_NAME, "Sensor");
//									System.out.println("\tSENSOR " + comp_name);
									Parameters args_comp = readComponentArguments(elem_ma, comp_name);
									args.merge(args_comp);
									String comp_class = readAttribute(elem_sensor, CONF_CLASS, "ensemble.Sensor");
									args.put(Constants.PARAM_EVT_TYPE, readAttribute(elem_sensor, CONF_COMP_EVENT_TYPE, "DUMMY"));
									args.put(Constants.PARAM_COMM_CLASS, readAttribute(elem_sensor, CONF_COMM, "ensemble.comm.CommMessage"));
									args.put(Constants.PARAM_MEMORY_CLASS, readAttribute(elem_sensor, Constants.PARAM_MEMORY_CLASS, null));
									args.put(Constants.PARAM_MEMORY_FUTURE, readAttribute(elem_sensor, Constants.PARAM_MEMORY_FUTURE, "1"));
									args.put(Constants.PARAM_MEMORY_PAST, readAttribute(elem_sensor, Constants.PARAM_MEMORY_PAST, "1"));
									args.put(Constants.PARAM_REL_POS, readAttribute(elem_sensor, Constants.PARAM_POSITION, "(0;0;0)"));
									ma.addComponent(comp_name, comp_class, args);
								}
	
								// Inserir os Actuators
								NodeList nl_actuators = elem_ma_class_comps.getElementsByTagName(CONF_COMP_ACTUATOR);
								for (int k = 0; k < nl_actuators.getLength(); k++) {
									Element elem_actuator = (Element)nl_actuators.item(k);
									Parameters args = readArguments(elem_actuator);
									String comp_name = readAttribute(elem_actuator, CONF_NAME, "Actuator");
//									System.out.println("\tACTUATOR " + comp_name);
									Parameters args_comp = readComponentArguments(elem_ma, comp_name);
									args.merge(args_comp);
									String comp_class = readAttribute(elem_actuator, CONF_CLASS, "ensemble.Actuator");
									args.put(Constants.PARAM_EVT_TYPE, readAttribute(elem_actuator, CONF_COMP_EVENT_TYPE, "DUMMY"));
									args.put(Constants.PARAM_COMM_CLASS, readAttribute(elem_actuator, CONF_COMM, "ensemble.comm.CommMessage"));
									args.put(Constants.PARAM_MEMORY_CLASS, readAttribute(elem_actuator, Constants.PARAM_MEMORY_CLASS, null));
									args.put(Constants.PARAM_MEMORY_FUTURE, readAttribute(elem_actuator, Constants.PARAM_MEMORY_FUTURE, "1"));
									args.put(Constants.PARAM_MEMORY_PAST, readAttribute(elem_actuator, Constants.PARAM_MEMORY_PAST, "1"));
									args.put(Constants.PARAM_REL_POS, readAttribute(elem_actuator, Constants.PARAM_POSITION, "(0;0;0)"));
									ma.addComponent(comp_name, comp_class, args);
								}
								
							}
	
							// Inserir o Agente no Jade
							AgentController ac = cc.acceptNewAgent(ma_name, ma);
							ac.start();
							
							break;
						}
					}

					if (!found_class) {
						System.out.println("[Loader] ERROR: there is no " + ma_class + " class defined");
					}
					
				} catch (ClassNotFoundException e) {
					System.err.println("[Loader] FATAL ERROR: Class " + ma_class + " not found");
					System.exit(-1);
				} catch (InstantiationException e) {
					System.err.println("[Loader] FATAL ERROR: Not possible to create an instance of " + ma_class);
					System.exit(-1);
				} catch (IllegalAccessException e) {
					System.err.println("[Loader] FATAL ERROR: Not possible to create an instance of " + ma_class);
					System.exit(-1);
				} catch (StaleProxyException e) {
					System.err.println("[Loader] FATAL ERROR: Not possible to insert agent " + ma_class + " in JADE");
					System.exit(-1);
				}
				
			}
			
		}
		
	}
	
	/**
	 * Gracefully exit the Ensemble and Jade system
	 */
	private void terminate() {
		stopJADE();
		System.out.println("[Loader] Exiting Ensemble...");
		System.exit(0);
	}
	
	//--------------------------------------------------------------------------------
	// Main method
	//--------------------------------------------------------------------------------

	public static void main(String[] args) {

//		// check the program arguments
		boolean nogui = false;
		String xml_filename = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-f")) {
				try {
					xml_filename = args[i+1];
				} catch (Exception e) {
					System.out.println("ERROR: no xml file was specified");
					System.exit(-1);
				}
				i++;
			}
			else if (args[i].equals("-nogui")) {
				nogui = true; 
			}
		}

		if (xml_filename != null) {
			System.out.println("------------ Loading Ensemble ------------");
			Element elem_ensemble = Loader.loadXMLFile(xml_filename).getDocumentElement();
			Loader.startJADE(elem_ensemble, nogui);
			Loader.loadSystem(elem_ensemble);
		} else {
			System.out.println("Loader usage: java Loader [-f <ensemble.properties> [-nogui]]");
			System.exit(-1);
		}
		
		// TODO Colocar alguma condição para que o usuário possa encerrar a execução do sistema
//		while (true) {}

	}
	
}
