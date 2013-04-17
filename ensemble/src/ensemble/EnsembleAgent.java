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

import ensemble.clock.VirtualClockHelper;
import ensemble.clock.VirtualClockService;
import ensemble.router.RouterClient;
import jade.core.AID;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class EnsembleAgent, extends the JADE class
 * Agent.
 */
public abstract class EnsembleAgent extends Agent implements LifeCycle, RouterClient {

//	public static final Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	/** The tbf. */
ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	
	/** The rcv cmd behavior. */
	private ReceiveCommand rcvCmdBehavior;
	
	/** Agent Parameters. */
	protected Parameters parameters = null;

	/**
	 * Parameters getter.
	 *
	 * @return initialized parameters
	 */
	@Override
	public final Parameters getParameters() {
		return parameters;
	}

	/**
	 * Parameters setter.
	 *
	 * @param parameters the new parameters
	 * @return initialized parameters
	 */
	@Override
	public final void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @return the parameter
	 */
	public final String getParameter(String key) {
		return parameters.get(key);
	}
	
	/**
	 * Gets the parameter.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the parameter
	 */
	public final String getParameter(String key, String defaultValue) {
		if (parameters.containsKey(key)) {
			return parameters.get(key);
 		} else {
 			return defaultValue;
 		}
	}

	/** Clock Service. */
	private VirtualClockHelper clock;
	
	/**
	 * Gets agent's name.
	 *
	 * @return the agent
	 */
	public final EnsembleAgent getAgent() {
		return this;
	}

	/**
	 * Gets agent's name.
	 *
	 * @return the agent name
	 */
	public final String getAgentName() {
		return getLocalName();
	}
	
	/**
	 * Clock getter.
	 *
	 * @return ensemble clock service
	 */
	public final VirtualClockHelper getClock() {
		return clock;
	}
	
	/** Router Service. */
//	private RouterHelper router;
//	
//	public RouterHelper getRouter() {
//		return router;
//	}
	
	protected boolean isBatch = false;
	
	/**
	 * Initialization method called by JADE.
	 */
	protected void setup() {
		
//		System.out.println("[" + getAgentName() + "] EnsembleAgent setup()");
		
		// 1. Obtém os parâmetros de entrada do Agente
		Object[] arguments = getArguments();
		if (arguments != null && arguments[0] instanceof Parameters) {
			parameters = (Parameters)arguments[0];
		}
		isBatch = getProperty(Constants.PROCESS_MODE, null).equals(Constants.MODE_BATCH);
	
		// 2. Inicializa os serviços básicos do agente
		try {
			clock = (VirtualClockHelper)getHelper(VirtualClockService.NAME);
//			router = (RouterHelper)getHelper(RouterService.NAME);
		} catch (ServiceException e) {
//			logger.severe("[" + this.getAgentName() + "] " + "Service not available");
			this.doDelete();
		}
		rcvCmdBehavior = new ReceiveCommand(this);
		this.addBehaviour(tbf.wrap(rcvCmdBehavior));
		
		// 3. Executa o método de configuração do usuário
		configure();

		// 4. Inicializa o agente
		if (start()) {
//			logger.info("[" + this.getAgentName() + "] " + "Initialized");
//			System.out.println("[" + this.getAgentName() + "] " + "Initialized");
		} else {
			doDelete();
		}

	}
	
	/**
	 * Finalization method called by JADE.
	 */
	protected void takeDown() {
		
		// Calls agent's stop method
		stop();
		
		System.out.println("[" + this.getAgentName() + "] " + "Agent terminated");
		
	}
	
	/**
	 * The Class ReceiveCommand.
	 */
	private final class ReceiveCommand extends CyclicBehaviour {

		/** The mt. */
		MessageTemplate mt;
		
		/**
		 * Instantiates a new receive command.
		 *
		 * @param a the a
		 */
		public ReceiveCommand(Agent a) {
			super(a);
			mt = MessageTemplate.MatchConversationId("CommandRouter");
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
//				MusicalAgent.logger.info("[" + getAgentName() + "] " + "Message received from " + msg.getSender().getLocalName() + " (" + msg.getContent() + ")");
				String sender = msg.getSender().getLocalName();
				Command cmd = Command.parse(msg.getContent());
				if (cmd != null) {
					receiveCommand(cmd);
				}
			}
			else {
				block();
			}
		}
	
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#getAddress()
	 */
	@Override
	public final String getAddress() {
		return "/" + Constants.FRAMEWORK_NAME + "/" + getAgentName();
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#sendCommand(ensemble.Command)
	 */
	@Override
	public final void sendCommand(Command cmd) {
		
        // Verifies the destination of the command
        String[] str = cmd.getRecipient().split("/");
        if (str.length < 2) {
            System.err.println("[" + getName() + "] Malformed address: " + cmd.getRecipient());
            return;
        }
        
        // If it's for the same agent, just pass to the processCommand of the recipient
        if (str[1].equals(Constants.FRAMEWORK_NAME) && getAgentName().equals(str[2])) {

        	receiveCommand(cmd);
        	
        } else {
   	    
        	// Fowards the command
	        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	        msg.setSender(getAID());
	        msg.setContent(cmd.toString());
    		msg.setConversationId("CommandRouter");
	        if (str[1].equals(Constants.FRAMEWORK_NAME)) {
	        	cmd.addParameter("sender", getAgentName());
	    		msg.addReceiver(new AID(str[2], AID.ISLOCALNAME));
	        } else {
	    		msg.addReceiver(new AID("Router", AID.ISLOCALNAME));
	        }
	        this.send(msg);
	        
        }
        
	}
	
	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
	}
	
}