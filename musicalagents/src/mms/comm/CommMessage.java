package mms.comm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import mms.Acting;
import mms.Event;
import mms.MusicalAgent;
import mms.Sensing;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CommMessage extends Comm {

	Behaviour b = null;
	
	@Override
	public final boolean start() {
		
		// Creates a JADE behaviour for message receive 
		b = new ReceiveMessages(myAgent);
		myAgent.addBehaviour(b);
		
		return true;
		
	}
	
	@Override
	public boolean end() {
		
		myAgent.removeBehaviour(b);
		
		return false;
		
	}
	
	// Recebe eventos
	private class ReceiveMessages extends CyclicBehaviour {

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
					MusicalAgent.logger.info("[" + myAgent.getAID().getLocalName() + ":" + myAccessPoint + "] " + "Recebi mensagem JADE de " + msg.getSender());
					Event evt = null;
					try {
						ObjectInputStream in;
						in = new ObjectInputStream(new ByteArrayInputStream(msg.getByteSequenceContent()));
				        evt = (Event)in.readObject();
						MusicalAgent.logger.info("[" + myAgent.getAID().getLocalName() + ":" + myAccessPoint + "] " + "Recebi mensagem JADE de " + msg.getSender() + " (" + (System.currentTimeMillis() - evt.timestamp) + ")");
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
	
			MusicalAgent.logger.info("[" + myAgent.getAID().getLocalName() + ":" + myAccessPoint + "] " + "Enviei mensagem JADE para " + msg.getSender());
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
			MusicalAgent.logger.warning("[" + myAgent.getName() + "] ERROR: COMM attached to a component that is not able to sense");
		}
	}
	
	
}
