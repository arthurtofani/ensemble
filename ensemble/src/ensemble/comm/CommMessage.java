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

package ensemble.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ensemble.Acting;
import ensemble.Event;
import ensemble.EnsembleAgent;
import ensemble.MusicalAgent;
import ensemble.Sensing;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CommMessage extends Comm {

	ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
	
	Behaviour b = null;
	
	@Override
	public final boolean configure() {
		return true;
	}

	@Override
	public final boolean init() {
		
		// Creates a JADE behaviour for message receive 
		b = new ReceiveMessages(myAgent);
		myAgent.addBehaviour(tbf.wrap(b));
		
		return true;
		
	}
	
	@Override
	public final boolean finit() {
		
		myAgent.removeBehaviour(b);
		
		return true;
		
	}
	
	// Recebe eventos
	private class ReceiveMessages extends CyclicBehaviour {

		protected boolean finished = false;
		MessageTemplate mt;
		
		public ReceiveMessages(Agent a) {
			super(a);
			mt = MessageTemplate.MatchConversationId(myAccessPoint);
		}
		
		public void action() {
			// Deve obter apenas mensagens destinadas ao seu Owner!!!
			ACLMessage msg = myAgent.receive(mt);
			if (sensing) {
				if (msg != null) {
//					MusicalAgent.logger.info("[" + ((EnsembleAgent)myAgent).getAgentName() + ":" + myAccessPoint + "] " + "Recebi mensagem JADE de " + msg.getSender());
					Event evt = null;
					try {
						ObjectInputStream in;
						in = new ObjectInputStream(new ByteArrayInputStream(msg.getByteSequenceContent()));
				        evt = (Event)in.readObject();
//						MusicalAgent.logger.info("[" + ((EnsembleAgent)myAgent).getAgentName() + ":" + myAccessPoint + "] " + "Recebi mensagem JADE de " + msg.getSender() + " (" + (System.currentTimeMillis() - evt.timestamp) + ")");
					} catch (Exception e) {
						e.printStackTrace();
					}
					receive(evt);
				}
				else {
					block();
				}
			}
		}
		
	}

	@Override
	public void send(Event evt) {
		
		evt.timestamp = System.currentTimeMillis();

		if (actuating) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			// TODO Mudar aqui pra ficar gen�rico
			msg.addReceiver(new AID(evt.destAgentName, AID.ISLOCALNAME));
			msg.setConversationId(evt.destAgentCompName);
			
			// Serializa o Evento
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(bos);
				oos.writeObject(evt);
			} catch (IOException e) {
				e.printStackTrace();
			}
			msg.setByteSequenceContent(bos.toByteArray());
	
//			MusicalAgent.logger.info("[" + ((EnsembleAgent)myAgent).getAgentName() + ":" + myAccessPoint + "] " + "Enviei mensagem JADE para " + msg.getSender());
			myAgent.send(msg);
		}
			
	}

	@Override
	public void receive(Event evt) {
		
		//MusicalAgent.logger.info("[" + myAgent.getName() + "] " + "Recebi evento");
		// No caso do dono do Comm ser um EnvironmentAgent
		if (mySensor != null) {
			mySensor.sense(evt);
		} else {
//			MusicalAgent.logger.warning("[" + myAgent.getName() + "] ERROR: COMM attached to a component that is not able to sense");
		}
	}
	
	
}
