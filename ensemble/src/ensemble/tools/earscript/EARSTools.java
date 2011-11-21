package ensemble.tools.earscript;

import java.util.List;

import org.w3c.dom.Element;


public class EARSTools {

	// Configuration File's Constants
	static final String CONF_WORLD_VALUES = "WORLD_VALUES";
	static final String CONF_WORLD_VALUE = "WORLD_VALUE";
	 static final String CONF_EVENT_SERVERS = "EVENT_SERVERS";
	 static final String CONF_EVENT_SERVER = "EVENT_SERVER";
	 static final String CONF_STATE_VALUES = "STATE_VALUES";
	 static final String CONF_STATE_VALUE = "STATE_VALUE";
	 static final String CONF_PARAMS_DEFINITIONS = "PARAMS_DEFINITIONS";
	 static final String CONF_PARAMETER = "PARAMETER";
	 static final String CONF_ALL_VALUES = "ALL_VALUES";
	 static final String CONF_PARAM_VALUE = "PARAM_VALUE";
	 static final String CONF_NATURAL_VALUES = "NATURAL_VALUES";
	 static final String CONF_NATURAL = "NATURAL";
	 static final String CONF_BASE_ACTIONS = "BASE_ACTIONS";
	 static final String CONF_ACTION = "ACTION";
	 static final String CONF_ACTION_DESCRIPTION = "ACTION_DESCRIPTION";
	 static final String CONF_ACTION_PARAMS = "ACTION_PARAMS";
	 static final String CONF_ACTION_PARAM = "ACTION_PARAM";
	 static final String CONF_SCRIPT = "SCRIPT";
	 static final String CONF_SCRIPT_ACTIONS = "COMPONENTS";
	 static final String CONF_SCRIPT_ACTION = "SCRIPT_ACTION";

	 static final String OPERATOR_SEQ = "SEQ";
	 static final String OPERATOR_PAR = "PAR";
	 static final String OPERATOR_CHOICE = "CHOICE";
	 static final String OPERATOR_REPEAT = "REPEAT";
	 static final String OPERATOR_TEST = "TEST";
	 static final String OPERATOR_DO = "DO";
	 static final String OPERATOR_IF = "IF";
	 static final String OPERATOR_THEN = "THEN";
	 static final String OPERATOR_ELSE = "ELSE";
	 
	 
	 static final String ARG_NAME = "Name";
	 static final String ARG_TYPE = "Type";
	 
	 static final String ARG_CLASS = "Class";
	 static final String ARG_SCOPE = "Scope";
	 static final String ARG_ORDER = "Order";
	 static final String ARG_VALUE = "Value";
	 static final String ARG_VALUES = "Values";
	 static final String ARG_COMMAND = "Command";
	 static final String ARG_DEFAULT = "Default";
	 static final String ARG_OPTIONAL = "Optional";
	 static final String ARG_AGENT = "Agent";
	 
	enum ParameterValueType {
		NUMERIC, STRING
	};

	public class EARScriptElement {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	// EVENT SERVER
	public class EventServer extends EARScriptElement {

		private String classRef;

		private List<StateValue> stateValues;

		private List<Parameter> params;

		private List<BaseAction> baseActions;

		public void setClassRef(String classRef) {
			this.classRef = classRef;
		}

		public String getClassRef() {
			return classRef;
		}

		public void setStateValues(List<StateValue> stateValues) {
			this.stateValues = stateValues;
		}

		public List<StateValue> getStateValues() {
			return stateValues;
		}

		public void setParams(List<Parameter> params) {
			this.params = params;
		}

		public List<Parameter> getParams() {
			return params;
		}

		public void setBaseActions(List<BaseAction> baseActions) {
			this.baseActions = baseActions;
		}

		public List<BaseAction> getBaseActions() {
			return baseActions;
		}

	}

	public class StateValue extends EARScriptElement {

		private Parameter type;
		private String scope;
		
		public void setScope(String scope) {
			this.scope = scope;
		}
		public String getScope() {
			return scope;
		}
		public void setType(Parameter type) {
			this.type = type;
		}
		public Parameter getType() {
			return type;
		}

	}

	// PARAMETERS
	public class Parameter extends EARScriptElement {

		private List<ParameterValueDefinition> valuesDefs;

		private List<ParameterNaturalValue> naturalValues;

		public void setValuesDefs(List<ParameterValueDefinition> valuesDefs) {
			this.valuesDefs = valuesDefs;
		}

		public List<ParameterValueDefinition> getValuesDefs() {
			return valuesDefs;
		}

		public void setNaturalValues(List<ParameterNaturalValue> naturalValues) {
			this.naturalValues = naturalValues;
		}

		public List<ParameterNaturalValue> getNaturalValues() {
			return naturalValues;
		}

	}

	public class ParameterValueDefinition extends EARScriptElement {

		private int order;
		private String type;

		public void setOrder(int order) {
			this.order = order;
		}

		public int getOrder() {
			return order;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

	}

	// ACTIONS

	public class BaseAction extends EARScriptElement {

		private String commandName;
		private String commandDescription;
		private List<ActionParameterDefinition> params;

		public void setCommandName(String commandName) {
			this.commandName = commandName;
		}

		public String getCommandName() {
			return commandName;
		}

		public void setCommandDescription(String commandDescription) {
			this.commandDescription = commandDescription;
		}

		public String getCommandDescription() {
			return commandDescription;
		}

		public void setParams(List<ActionParameterDefinition> params) {
			this.params = params;
		}

		public List<ActionParameterDefinition> getParams() {
			return params;
		}

	}

	public class ActionParameterDefinition extends EARScriptElement {

		private Parameter type;
		private ParameterNaturalValue defaultValue;

		public void setType(Parameter type) {
			this.type = type;
		}

		public Parameter getType() {
			return type;
		}

		public void setDefaultValue(ParameterNaturalValue defaultValue) {
			this.defaultValue = defaultValue;
		}

		public ParameterNaturalValue getDefaultValue() {
			return defaultValue;
		}

	}

	public class ParameterNaturalValue extends EARScriptElement {

		private List<String> values;

		public void setValues(List<String> values) {
			this.values = values;
		}

		public List<String> getValues() {
			return values;
		}

	}

	public class EARScriptAction extends EARScriptElement {

		private Element content;

	}

	// WORLD VALUES
	public class WorldValue extends EARScriptElement {

		private String type;
		private String defaultValue;

		public void setType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

	}
}
