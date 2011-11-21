package ensemble.tools.earscript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ensemble.Reasoning;
import ensemble.tools.earscript.EARSTools.EventServer;
import ensemble.tools.earscript.EARSTools.Parameter;
import ensemble.tools.earscript.EARSTools.StateValue;
import ensemble.tools.earscript.EARSTools.WorldValue;
import ensemble.tools.earscript.EARSTools;

public class Earscript_Reasoning extends Reasoning {

	
	List<EventServer> eventServers;

	static ArrayList<WorldValue> worldValues;

	public static EARSTools tools;
	
	public boolean init() {

		String xml_filename = getParameter("scriptPath", "");

		tools = new EARSTools();
		
		// Recupera o script do arquivo XML
		if (xml_filename != "") {
			System.out.println("------------ Loading EARSCRIPT ------------");
			Element elem_EARScript = loadXMLFile(xml_filename)
					.getDocumentElement();
			startEARScript(elem_EARScript);
		} else {
			System.out.println("EARScript needs a valid XML File");
			return false;
		}

		// Inicializa a estrutura de servidores de eventos, comandos e demais

		return true;

	}

	private static void startEARScript(Element elem_EARScript) {

		// Load World Values
		NodeList nl = elem_EARScript
				.getElementsByTagName(EARSTools.CONF_WORLD_VALUES);
		if (nl.getLength() == 1) {
			Element elem_wv = (Element) nl.item(0);
			loadWorldValues(elem_wv);
		}

		// Load Event Servers
		nl = elem_EARScript
		.getElementsByTagName(EARSTools.CONF_EVENT_SERVERS);
if (nl.getLength() == 1) {
	Element elem_es = (Element) nl.item(0);
	loadEventServers(elem_es);
}

		Iterator<WorldValue> itr = worldValues.iterator(); 
		while(itr.hasNext()) {
			WorldValue aux = itr.next();				    
			System.out.println("WorldValue - Name:" + aux.getName() + " Type:" + aux.getType());
			
		} 
		
	}

private static void loadWorldValues(Element elem_wv) {

		

		NodeList nl_world_values = elem_wv
				.getElementsByTagName(EARSTools.CONF_WORLD_VALUE);
		// Initiates the World Values list
		ArrayList<WorldValue> wvList = new ArrayList<WorldValue>();

		for (int j = 0; j < nl_world_values.getLength(); j++) {
			Element elem_arg = (Element) nl_world_values.item(j);
			if (elem_arg.getParentNode() == elem_wv) {

				WorldValue newValue = tools.new WorldValue();
				// Value name - Mandatory
				String wv_name = readAttribute(elem_arg, EARSTools.ARG_NAME, null);
				if (wv_name != null) {
					newValue.setName(wv_name);
					// World Value Type
					String wv_type = readAttribute(elem_arg, EARSTools.ARG_TYPE, null);
					newValue.setType(wv_type);
				}
				wvList.add(newValue);
			}
		}
worldValues = wvList;

	}
	
	private static void loadEventServers(Element elem_EARScript) {
		

		NodeList nl_event_servers = elem_EARScript
				.getElementsByTagName(EARSTools.CONF_EVENT_SERVERS);
		
		// Initiates the Event Server list
		ArrayList<EventServer> esList = new ArrayList<EventServer>();
		
		for (int j = 0; j < nl_event_servers.getLength(); j++) {
			Element elem_arg = (Element) nl_event_servers.item(j);
			
			EventServer newEventServer = tools.new EventServer();
			
			// Value name - Mandatory
			String es_class = readAttribute(elem_arg, EARSTools.ARG_CLASS, null);
			if (es_class != null) {
				newEventServer.setClassRef(es_class);
				
			}


			//Parameters
			NodeList nl_parameters = elem_EARScript
			.getElementsByTagName(EARSTools.CONF_PARAMS_DEFINITIONS);
			

			// Initiates the State Values list
			ArrayList<Parameter> paramList = new ArrayList<Parameter>();

			
			for (int k = 0; k < nl_parameters.getLength(); k++) {
				EARSTools.Parameter newParam = tools.new Parameter();
				
				Element elem_sv = (Element) nl_parameters.item(k);
				
				String param_name = readAttribute(elem_sv, EARSTools.ARG_NAME, null);
				if (param_name != null) {
					newParam.setName(param_name);
				}
				
				
				
			}
				
			
			//State values
			NodeList nl_es_state_values = elem_EARScript
			.getElementsByTagName(EARSTools.CONF_STATE_VALUES);
			

			// Initiates the State Values list
			ArrayList<StateValue> svList = new ArrayList<StateValue>();
			
			
			for (int k = 0; k < nl_es_state_values.getLength(); k++) {
							
				
				EARSTools.StateValue newStateValue = tools.new StateValue();
				
				Element elem_sv = (Element) nl_es_state_values.item(k);
				
				String sv_name = readAttribute(elem_sv, EARSTools.ARG_NAME, null);
				if (sv_name != null) {
					newStateValue.setName(sv_name);
				}
				
				String sv_scope = readAttribute(elem_sv, EARSTools.ARG_SCOPE, null);
				if (sv_scope != null) {
					newStateValue.setScope(sv_scope);
				}

				String sv_type = readAttribute(elem_sv, EARSTools.ARG_TYPE, null);
				if (sv_type != null) {
					//newStateValue.setType(sv_type);
				}
			
				svList.add(newStateValue);
			}
			
			//Adiciona a lista de state values ao servidor de eventos
			newEventServer.setStateValues(svList);
		}
	}

	

	private static Document loadXMLFile(String xmlFile) {

		Document doc = null;

		System.out.println("[EARScript] Loading script file" + xmlFile
				+ " for Ensemble Agent: " + xmlFile);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(xmlFile);
		} catch (Exception e) {
			System.err.println("Error while loading XML script file!");
			System.err.println(e.toString());
			System.exit(1);
		}

		return doc;

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
}
