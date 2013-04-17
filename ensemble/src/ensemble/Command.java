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

package ensemble;

import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class Command.
 */
public class Command {

	/** The command. */
	private String 		command;
	
	/** The parameters. */
	private Parameters	parameters = new Parameters();
	
	/** The user parameters. */
	private Parameters	userParameters = new Parameters();
	
	/**
	 * Instantiates a new command.
	 *
	 * @param command the command
	 */
	public Command(String command) {
		this.command = command;
	}
	
	/**
	 * Instantiates a new command.
	 *
	 * @param sender the sender
	 * @param recipient the recipient
	 * @param command the command
	 */
	public Command(String sender, String recipient, String command) {
		this.command = command;
		addParameter("sender", sender);
		addParameter("recipient", recipient);
	}
	
	/**
	 * Gets the recipient.
	 *
	 * @return the recipient
	 */
	public String getRecipient() {
		return getParameter("recipient");
	}
	
	/**
	 * Sets the recipient.
	 *
	 * @param recipient the new recipient
	 */
	public void setRecipient(String recipient) {
		addParameter("recipient", recipient);
	}
	
	/**
	 * Gets the sender.
	 *
	 * @return the sender
	 */
	public String getSender() {
		return getParameter("sender");
	}
	
	/**
	 * Sets the sender.
	 *
	 * @param sender the new sender
	 */
	public void setSender(String sender) {
		addParameter("sender", sender);
	}
	
	/**
	 * Gets the command.
	 *
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Contains parameter.
	 *
	 * @param parameter the parameter
	 * @return true, if successful
	 */
	public boolean containsParameter(String parameter) {	
		return parameters.containsKey(parameter);
	}
	
	/**
	 * Contains user parameter.
	 *
	 * @param parameter the parameter
	 * @return true, if successful
	 */
	public boolean containsUserParameter(String parameter) {	
		return userParameters.containsKey(parameter);
	}
	
	/**
	 * Adds the parameter.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addParameter(String key, String value) {
		if (parameters == null) {
			parameters = new Parameters();	
		}
		parameters.put(key, value);
	}
	
	/**
	 * Adds the parameters.
	 *
	 * @param hash the hash
	 */
	public void addParameters(Parameters hash) {
		if (hash != null) {
			Set<String> set = hash.keySet();
			for (String key : set) {
				parameters.put(key, hash.get(key));
			}
		}
	}

	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @return the parameter
	 */
	public String getParameter(String key) {
		if (parameters != null) {
			String ret = parameters.get(key);
			if (ret == null || ret.equals("")) {
//				System.out.println("[Command] Parâmetro '" + key + "' inexistente");
			}
			return ret;
		} else {
			return null;
		}
	}
	
	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public Parameters getParameters() {
		return parameters;
	}
	
	/**
	 * Adds the user parameter.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void addUserParameter(String key, String value) {
		userParameters.put(key, value);				
	}
	
	/**
	 * Adds the user parameters.
	 *
	 * @param hash the hash
	 */
	public void addUserParameters(Parameters hash) {
		if (hash != null) {
			Set<String> set = hash.keySet();
			for (String key : set) {
				userParameters.put(key, hash.get(key));
			}
		}
	}
	
	/**
	 * Gets the user parameter.
	 *
	 * @param key the key
	 * @return the user parameter
	 */
	public String getUserParameter(String key) {
		return userParameters.get(key);
	}
	
	/**
	 * Gets the user parameters.
	 *
	 * @return the user parameters
	 */
	public Parameters getUserParameters() {
		return userParameters;
	}
	
	/**
	 * Parses the.
	 *
	 * @param str the str
	 * @return the command
	 */
	public static Command parse(String str) {
		
		String[] strSplited = str.split(" :");

		if (strSplited == null || strSplited.length < 1) {
			System.out.println("[ERROR] parse() - Malformed command: " + str);
			return null;
		}

		Command cmd = new Command(strSplited[0].trim());

		for (int i = 1; i < strSplited.length; i++) {
			String[] parameter = strSplited[i].split(" ");

//			if (parameter.length != 2) {
//				System.out.println("[ERROR] parse() - Malformed command: " + str);
//				return null;
//			}
			
			String key = parameter[0].trim();
			String value = "";
			for (int j = 1; j < parameter.length; j++) {
				value += parameter[j] + " ";
			}
			value = value.trim();
			if (key.startsWith("X-")) {
				key = key.replaceFirst("X-", "");
				cmd.addUserParameter(key, value);
			} else {
				cmd.addParameter(key, value);
			}
			
		}
		
		return cmd;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		
		if (command == null || command.trim().equals("")) {
			System.out.println("[ERROR] toString() - Malformed command!");
			return null;
		}
		
		String ret = command;
		
		Set<String> set = parameters.keySet();
		for (String key : set) {
			String value = parameters.get(key);
			ret = ret + " :" + key + " " + value;
		}
		
		set = userParameters.keySet();
		for (String key : set) {
			String value = userParameters.get(key);
			ret = ret + " :X-" + key + " " + value;
		}
		
		return ret;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		
//		String str = "EVENT-REGISTER :compSendr Foot :compType ACTUATOR :compEvtType MOVEMENT :pos_x 2 :pos_y 4";
//		Command cmd = Command.parse(str);
//		if (cmd != null) {
//			System.out.println(cmd.toString());
//		}
		
		
		String input = "TESTE :name Leandro 232;676 :teste (12;23.0;0.1) :surname \"Ferrári Thomaz\" :ble bla";
		Command cmd = Command.parse(input);
		System.out.println(cmd);
//		Pattern p_param = Pattern.compile(":(\\w*)(\\s([\\S&&[^:]])*)*");
//		Matcher matcher = p_param.matcher(input);
//		while (matcher.find()) {
//			System.out.println("Found: " + matcher.group());
//		}
//		System.out.println();
//		Pattern p_param_2 = Pattern.compile(":(\\w*)\\s\"[\\s\\p{L}\\p{N}\\p{S}\\p{P}&&[^\"]]*\"");
//		matcher = p_param_2.matcher(input);
//		while (matcher.find()) {
//			System.out.println("Found: " + matcher.group());
//		}		
	}
	
}
