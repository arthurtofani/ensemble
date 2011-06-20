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

package ensemble.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;
import ensemble.Command;
import ensemble.Constants;
import ensemble.MusicalAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RouterAgent extends Agent {
	
    //----------------------------------------------------------
	// OSC
    private int 		oscSendPort 	= 57110;
    private int 		oscListenPort 	= 57111;
	private OSCClient 	oscClient;
	private OSCServer 	oscServer;
	
	
	@Override
	protected void setup() {
		
		// Starts OSC
		try {
			oscClient = OSCClient.newUsing(OSCChannel.UDP);
			oscClient.setTarget(new InetSocketAddress(InetAddress.getLocalHost(), oscSendPort));
			oscClient.start();
			
			oscServer = OSCServer.newUsing(OSCChannel.UDP, oscListenPort);
			oscServer.addOSCListener(new Listener());
			oscServer.start();
		}
		catch( IOException e1 ) {
			e1.printStackTrace();
			return;
		}
		
		// Console GUI
//		ConsoleGUI console = new ConsoleGUI(this);
//		console.setVisible(true);

        // Receive messages
		this.addBehaviour(new ReceiveMessages(this));

		System.out.println("[" + getLocalName() + "] Router agent started");
        
	}
	
	@Override
	protected void takeDown() {
		
		// Stops the OSC client
		try {
			oscClient.stop();
			oscClient.dispose();
			oscServer.stop();
			oscServer.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void processCommand(Command cmd) {

		if (cmd.getRecipient() == null || cmd.getSender() == null || cmd.getCommand() == null) {
			System.err.println("[" + getName() + "] Command contains NULL objects");
			return;
		}
		
        // Verifies the destination of the command
        String[] str = cmd.getRecipient().split("/");
        if (str.length < 2) {
            System.err.println("[" + getName() + "] Malformed address: " + cmd.getRecipient());
            return;
        }
		
        // Fowards the command
        if (str[1].equals(Constants.FRAMEWORK_NAME)) {
        	
//            System.out.println("[Router] Command received: " + cmd);
    		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    		msg.addReceiver(new AID(str[2], AID.ISLOCALNAME));
    		msg.setConversationId("CommandRouter");
    		msg.setContent(cmd.toString());
        	send(msg);
            
        } else if (str[1].equals("console")) {
        	
//            System.out.println("[Router] Command to CONSOLE received: " + cmd);
    		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    		msg.addReceiver(new AID("Sniffer", AID.ISLOCALNAME));
    		msg.setConversationId("CommandRouter");
    		msg.setContent(cmd.toString());
        	send(msg);
            	            
        } else {
        	
//            System.out.println("[Router] Command to OSC received: " + cmd);
            sendOSCMessage(cmd);
            
        }
	}

	//--------------------------------------------------------------------------------
	// JADE Message Control 
	//--------------------------------------------------------------------------------

	private final class ReceiveMessages extends CyclicBehaviour {

		MessageTemplate mt;
		
		public ReceiveMessages(Agent a) {
			super(a);
			mt = MessageTemplate.MatchConversationId("CommandRouter");
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getPerformative() != ACLMessage.FAILURE) {
					String sender = msg.getSender().getLocalName();
					Command cmd = Command.parse(msg.getContent());
					if (cmd != null) {
						processCommand(cmd);
					}
				}
			}
			else {
				block();
			}
		}
	
	}
	
	//--------------------------------------------------------------------------------
	// OSC Control 
	//--------------------------------------------------------------------------------
	
	private void sendOSCMessage(Command cmd) {
		if (cmd.getCommand().equals("OSC")) {
			String[] str = cmd.getParameter("CONTENT").split(" ");
			Object[] obj = new Object[str.length];
			for (int i = 0; i < str.length; i++) {
				try {
					obj[i] = Float.parseFloat(str[i]);
				} catch (Exception e) {
					obj[i] = str[i];
				}
			}
			OSCMessage msg = new OSCMessage(cmd.getRecipient(), obj);
			try {
				oscClient.send(msg);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class Listener implements OSCListener {

		@Override
		public void messageReceived(OSCMessage m, SocketAddress addr, long time) {
			// Obtém o agente e componente destino
			String[] address = m.getName().split("/");
			if (address.length <= 1 || !address[1].equals(Constants.FRAMEWORK_NAME)) {
//	    		MusicalAgent.logger.info("[OSCService] " + "Malformed address: " + m.getName());
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < m.getArgCount(); i++) {
				sb.append(m.getArg(i));
				sb.append(" ");
			}
			Command cmd = Command.parse(sb.toString());
			cmd.setRecipient(m.getName());
			cmd.setSender("/osc");
			processCommand(cmd);
		}
		
	}
	
}
