package ensemble.tools.earscript;

import java.util.List;

import org.w3c.dom.Element;


// TODO: Auto-generated Javadoc
/**
 * The Class EARSTools.
 */
public class EARSTools {

	// Configuration File's Constants
	/** The Constant CONF_WORLD_VALUES. */
	static final String CONF_WORLD_VALUES = "WORLD_VALUES";
	
	/** The Constant CONF_WORLD_VALUE. */
	static final String CONF_WORLD_VALUE = "WORLD_VALUE";
	 
 	/** The Constant CONF_EVENT_SERVERS. */
 	static final String CONF_EVENT_SERVERS = "EVENT_SERVERS";
	 
 	/** The Constant CONF_EVENT_SERVER. */
 	static final String CONF_EVENT_SERVER = "EVENT_SERVER";
	 
 	/** The Constant CONF_STATE_VALUES. */
 	static final String CONF_STATE_VALUES = "STATE_VALUES";
	 
 	/** The Constant CONF_STATE_VALUE. */
 	static final String CONF_STATE_VALUE = "STATE_VALUE";
	 
 	/** The Constant CONF_PARAMS_DEFINITIONS. */
 	static final String CONF_PARAMS_DEFINITIONS = "PARAMS_DEFINITIONS";
	 
 	/** The Constant CONF_PARAMETER. */
 	static final String CONF_PARAMETER = "PARAMETER";
	 
 	/** The Constant CONF_ALL_VALUES. */
 	static final String CONF_ALL_VALUES = "ALL_VALUES";
	 
 	/** The Constant CONF_PARAM_VALUE. */
 	static final String CONF_PARAM_VALUE = "PARAM_VALUE";
	 
 	/** The Constant CONF_NATURAL_VALUES. */
 	static final String CONF_NATURAL_VALUES = "NATURAL_VALUES";
	 
 	/** The Constant CONF_NATURAL. */
 	static final String CONF_NATURAL = "NATURAL";
	 
 	/** The Constant CONF_BASE_ACTIONS. */
 	static final String CONF_BASE_ACTIONS = "BASE_ACTIONS";
	 
 	/** The Constant CONF_ACTION. */
 	static final String CONF_ACTION = "ACTION";
	 
 	/** The Constant CONF_ACTION_DESCRIPTION. */
 	static final String CONF_ACTION_DESCRIPTION = "ACTION_DESCRIPTION";
	 
 	/** The Constant CONF_ACTION_PARAMS. */
 	static final String CONF_ACTION_PARAMS = "ACTION_PARAMS";
	 
 	/** The Constant CONF_ACTION_PARAM. */
 	static final String CONF_ACTION_PARAM = "ACTION_PARAM";
	 
 	/** The Constant CONF_SCRIPT. */
 	static final String CONF_SCRIPT = "SCRIPT";
	 
 	/** The Constant CONF_SCRIPT_ACTIONS. */
 	static final String CONF_SCRIPT_ACTIONS = "COMPONENTS";
	 
 	/** The Constant CONF_SCRIPT_ACTION. */
 	static final String CONF_SCRIPT_ACTION = "SCRIPT_ACTION";

	 /** The Constant OPERATOR_SEQ. */
 	static final String OPERATOR_SEQ = "SEQ";
	 
 	/** The Constant OPERATOR_PAR. */
 	static final String OPERATOR_PAR = "PAR";
	 
 	/** The Constant OPERATOR_CHOICE. */
 	static final String OPERATOR_CHOICE = "CHOICE";
	 
 	/** The Constant OPERATOR_REPEAT. */
 	static final String OPERATOR_REPEAT = "REPEAT";
	 
 	/** The Constant OPERATOR_TEST. */
 	static final String OPERATOR_TEST = "TEST";
	 
 	/** The Constant OPERATOR_DO. */
 	static final String OPERATOR_DO = "DO";
	 
 	/** The Constant OPERATOR_IF. */
 	static final String OPERATOR_IF = "IF";
	 
 	/** The Constant OPERATOR_THEN. */
 	static final String OPERATOR_THEN = "THEN";
	 
 	/** The Constant OPERATOR_ELSE. */
 	static final String OPERATOR_ELSE = "ELSE";
	 
	 
	 /** The Constant ARG_NAME. */
 	static final String ARG_NAME = "Name";
	 
 	/** The Constant ARG_TYPE. */
 	static final String ARG_TYPE = "Type";
	 
	 /** The Constant ARG_CLASS. */
 	static final String ARG_CLASS = "Class";
	 
 	/** The Constant ARG_SCOPE. */
 	static final String ARG_SCOPE = "Scope";
	 
 	/** The Constant ARG_ORDER. */
 	static final String ARG_ORDER = "Order";
	 
 	/** The Constant ARG_VALUE. */
 	static final String ARG_VALUE = "Value";
	 
 	/** The Constant ARG_VALUES. */
 	static final String ARG_VALUES = "Values";
	 
 	/** The Constant ARG_COMMAND. */
 	static final String ARG_COMMAND = "Command";
	 
 	/** The Constant ARG_DEFAULT. */
 	static final String ARG_DEFAULT = "Default";
	 
 	/** The Constant ARG_OPTIONAL. */
 	static final String ARG_OPTIONAL = "Optional";
	 
 	/** The Constant ARG_AGENT. */
 	static final String ARG_AGENT = "Agent";
	 
	/**
	 * The Enum ParameterValueType.
	 */
	enum ParameterValueType {
		
		/** The numeric. */
		NUMERIC, 
 /** The string. */
 STRING
	};

	/**
	 * The Class EARScriptElement.
	 */
	public class EARScriptElement {

		/** The name. */
		private String name;

		/**
		 * Sets the name.
		 *
		 * @param name the new name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return name;
		}

	}

	// EVENT SERVER
	/**
	 * The Class EventServer.
	 */
	public class EventServer extends EARScriptElement {

		/** The class ref. */
		private String classRef;

		/** The state values. */
		private List<StateValue> stateValues;

		/** The params. */
		private List<Parameter> params;

		/** The base actions. */
		private List<BaseAction> baseActions;

		/**
		 * Sets the class ref.
		 *
		 * @param classRef the new class ref
		 */
		public void setClassRef(String classRef) {
			this.classRef = classRef;
		}

		/**
		 * Gets the class ref.
		 *
		 * @return the class ref
		 */
		public String getClassRef() {
			return classRef;
		}

		/**
		 * Sets the state values.
		 *
		 * @param stateValues the new state values
		 */
		public void setStateValues(List<StateValue> stateValues) {
			this.stateValues = stateValues;
		}

		/**
		 * Gets the state values.
		 *
		 * @return the state values
		 */
		public List<StateValue> getStateValues() {
			return stateValues;
		}

		/**
		 * Sets the params.
		 *
		 * @param params the new params
		 */
		public void setParams(List<Parameter> params) {
			this.params = params;
		}

		/**
		 * Gets the params.
		 *
		 * @return the params
		 */
		public List<Parameter> getParams() {
			return params;
		}

		/**
		 * Sets the base actions.
		 *
		 * @param baseActions the new base actions
		 */
		public void setBaseActions(List<BaseAction> baseActions) {
			this.baseActions = baseActions;
		}

		/**
		 * Gets the base actions.
		 *
		 * @return the base actions
		 */
		public List<BaseAction> getBaseActions() {
			return baseActions;
		}

	}

	/**
	 * The Class StateValue.
	 */
	public class StateValue extends EARScriptElement {

		/** The type. */
		private Parameter type;
		
		/** The scope. */
		private String scope;
		
		/**
		 * Sets the scope.
		 *
		 * @param scope the new scope
		 */
		public void setScope(String scope) {
			this.scope = scope;
		}
		
		/**
		 * Gets the scope.
		 *
		 * @return the scope
		 */
		public String getScope() {
			return scope;
		}
		
		/**
		 * Sets the type.
		 *
		 * @param type the new type
		 */
		public void setType(Parameter type) {
			this.type = type;
		}
		
		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public Parameter getType() {
			return type;
		}

	}

	// PARAMETERS
	/**
	 * The Class Parameter.
	 */
	public class Parameter extends EARScriptElement {

		/** The values defs. */
		private List<ParameterValueDefinition> valuesDefs;

		/** The natural values. */
		private List<ParameterNaturalValue> naturalValues;

		/**
		 * Sets the values defs.
		 *
		 * @param valuesDefs the new values defs
		 */
		public void setValuesDefs(List<ParameterValueDefinition> valuesDefs) {
			this.valuesDefs = valuesDefs;
		}

		/**
		 * Gets the values defs.
		 *
		 * @return the values defs
		 */
		public List<ParameterValueDefinition> getValuesDefs() {
			return valuesDefs;
		}

		/**
		 * Sets the natural values.
		 *
		 * @param naturalValues the new natural values
		 */
		public void setNaturalValues(List<ParameterNaturalValue> naturalValues) {
			this.naturalValues = naturalValues;
		}

		/**
		 * Gets the natural values.
		 *
		 * @return the natural values
		 */
		public List<ParameterNaturalValue> getNaturalValues() {
			return naturalValues;
		}

	}

	/**
	 * The Class ParameterValueDefinition.
	 */
	public class ParameterValueDefinition extends EARScriptElement {

		/** The order. */
		private int order;
		
		/** The type. */
		private String type;

		/**
		 * Sets the order.
		 *
		 * @param order the new order
		 */
		public void setOrder(int order) {
			this.order = order;
		}

		/**
		 * Gets the order.
		 *
		 * @return the order
		 */
		public int getOrder() {
			return order;
		}

		/**
		 * Sets the type.
		 *
		 * @param type the new type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type;
		}

	}

	// ACTIONS

	/**
	 * The Class BaseAction.
	 */
	public class BaseAction extends EARScriptElement {

		/** The command name. */
		private String commandName;
		
		/** The command description. */
		private String commandDescription;
		
		/** The params. */
		private List<ActionParameterDefinition> params;

		/**
		 * Sets the command name.
		 *
		 * @param commandName the new command name
		 */
		public void setCommandName(String commandName) {
			this.commandName = commandName;
		}

		/**
		 * Gets the command name.
		 *
		 * @return the command name
		 */
		public String getCommandName() {
			return commandName;
		}

		/**
		 * Sets the command description.
		 *
		 * @param commandDescription the new command description
		 */
		public void setCommandDescription(String commandDescription) {
			this.commandDescription = commandDescription;
		}

		/**
		 * Gets the command description.
		 *
		 * @return the command description
		 */
		public String getCommandDescription() {
			return commandDescription;
		}

		/**
		 * Sets the params.
		 *
		 * @param params the new params
		 */
		public void setParams(List<ActionParameterDefinition> params) {
			this.params = params;
		}

		/**
		 * Gets the params.
		 *
		 * @return the params
		 */
		public List<ActionParameterDefinition> getParams() {
			return params;
		}

	}

	/**
	 * The Class ActionParameterDefinition.
	 */
	public class ActionParameterDefinition extends EARScriptElement {

		/** The type. */
		private Parameter type;
		
		/** The default value. */
		private ParameterNaturalValue defaultValue;

		/**
		 * Sets the type.
		 *
		 * @param type the new type
		 */
		public void setType(Parameter type) {
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public Parameter getType() {
			return type;
		}

		/**
		 * Sets the default value.
		 *
		 * @param defaultValue the new default value
		 */
		public void setDefaultValue(ParameterNaturalValue defaultValue) {
			this.defaultValue = defaultValue;
		}

		/**
		 * Gets the default value.
		 *
		 * @return the default value
		 */
		public ParameterNaturalValue getDefaultValue() {
			return defaultValue;
		}

	}

	/**
	 * The Class ParameterNaturalValue.
	 */
	public class ParameterNaturalValue extends EARScriptElement {

		/** The values. */
		private List<String> values;

		/**
		 * Sets the values.
		 *
		 * @param values the new values
		 */
		public void setValues(List<String> values) {
			this.values = values;
		}

		/**
		 * Gets the values.
		 *
		 * @return the values
		 */
		public List<String> getValues() {
			return values;
		}

	}

	/**
	 * The Class EARScriptAction.
	 */
	public class EARScriptAction extends EARScriptElement {

		/** The content. */
		private Element content;

	}

	// WORLD VALUES
	/**
	 * The Class WorldValue.
	 */
	public class WorldValue extends EARScriptElement {

		/** The type. */
		private String type;
		
		/** The default value. */
		private String defaultValue;

		/**
		 * Sets the type.
		 *
		 * @param type the new type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Sets the default value.
		 *
		 * @param defaultValue the new default value
		 */
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		/**
		 * Gets the default value.
		 *
		 * @return the default value
		 */
		public String getDefaultValue() {
			return defaultValue;
		}

	}
}
