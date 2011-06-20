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

import java.util.ArrayList;

import ensemble.Constants.EA_STATE;
import ensemble.Constants.EH_STATUS;
import ensemble.comm.Comm;
import ensemble.memory.Memory;
import ensemble.world.Vector;


import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public abstract class EventHandler extends MusicalAgentComponent {

	/**
	 * Event Handler's state
	 * 0: not registered, 1: registered
	 */
	protected EH_STATUS	status 			= EH_STATUS.NOT_REGISTERED;

	/**
	 * Event Handler's parameters
	 */
	protected String 		eventType 			= null;
	protected String		eventExchange 		= Constants.EVT_EXC_NOT_DEFINED;
	protected String 		commType 			= "ensemble.comm.direct.CommDirect";
	protected Vector		relative_position 	= new Vector();
	
	protected Comm myComm;
	
	protected Memory myMemory;
	
	// Eventos periódicos - Tempos configurados pelo EventServer
	protected long startTime;
	protected long frameTime;
	protected long period;
	protected long sendDeadline;
	
	protected long workingFrame;
	protected long happeningFrame;
		
	// Lista de componentes interessados no evento
	ArrayList<Reasoning> listeners = new ArrayList<Reasoning>();
	
	@Override
	public boolean start() {
		
		// Define o tipo de evento que o EventHandler irá tratar
		// TODO Colocar um tratamento de erro aqui!
		if (this.eventType == null) {
			this.eventType = parameters.get(Constants.PARAM_EVT_TYPE);
		}
		
		// Initializes the communication channel
		try {
			Class commClass = Class.forName(commType);
			myComm = (Comm)commClass.newInstance();
			Parameters commParam = new Parameters();
			commParam.put(Constants.PARAM_COMM_AGENT, getAgent());
			if (getType().equals(Constants.COMP_SENSOR)) {
				commParam.put(Constants.PARAM_COMM_SENSING, this);
			} 
			else if (getType().equals(Constants.COMP_ACTUATOR)) {
				commParam.put(Constants.PARAM_COMM_ACTING, this);
			}
			commParam.put(Constants.PARAM_COMM_ACCESS_POINT, getComponentName());
			myComm.setParameters(commParam);
			myComm.configure();
			if (!myComm.start()) {
				return false;
			}
		} catch (Exception e) {
//    		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Comm class " + commType + " not found!");
			e.printStackTrace();
			return false;
		}
		
		return true;

	}
	
	@Override
	public boolean stop() {
		
		// Terminates de communication channel
		myComm.stop();
		
		return true;
		
	}
	
	public void setEventType(String eventType) {
		if (getState() == EA_STATE.CREATED) {
			this.eventType = eventType;
		} else {
//    		MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Trying to set eventType after initialization!");
		}
	}

	public String getEventType() {
		return eventType;
	}
	
	public EH_STATUS getStatus() {
		return status;
	}
	
	public void setRelativePosition(Vector relative_position) {
		relative_position.copy(this.relative_position);
	}
	
	// TODO Precisa ser algo mais genérico que Vector3D!!!!
	public Vector getRelativePosition() {
		return relative_position;
	}
	
	/**
	 * Registra o EventHandler no EventServer correspondente 
	 */
	public void register() {

		// Procura o Agente Ambiente responsável pelo tipo de evento e o registra
		// TODO devemos garantir que só existe um agente responsável por tipo de evento
		// TODO o que acontece se não encontrar o Agente Ambiente?!?!
		// TODO pode necessitar de parâmetros, por exemplo, ouvido 'esquerdo' e 'direito'
		boolean es_registered = false;
		try {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType(eventType);
			template.addServices(sd);
			for (int tries = 0; tries < 3; tries++) {
				DFAgentDescription[] result = DFService.search(getAgent(), template);
				if (result.length == 1) {
					es_registered = true;
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception fe) {
			System.err.println("Environment Agent not found...");
			return;
		}
		// If found, sends the register command
		if (es_registered) {
			Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_EVENT_REGISTER );
			cmd.addParameter(Constants.PARAM_COMP_NAME, getComponentName());
			cmd.addParameter(Constants.PARAM_COMP_TYPE, getType());
			cmd.addParameter(Constants.PARAM_EVT_TYPE, getEventType());
			cmd.addParameter(Constants.PARAM_REL_POS, getRelativePosition().toString());
			cmd.addUserParameters(getParameters());
			sendCommand(cmd);
		} else {
//			MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getName() + "] " + "EventServer " + eventType + " not found");
			System.out.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "EventServer " + eventType + " not registered");
		}

	}
	
	public void confirmRegistration(String eventExecution, Parameters serverParameters, Parameters extraParameters) {

		this.eventExchange = eventExecution;

		if (eventExecution.equals(Constants.EVT_EXC_PERIODIC)) {
			// Configuration
			this.startTime 		= Long.valueOf(serverParameters.get(Constants.PARAM_START_TIME));
			this.workingFrame 	= Long.valueOf(serverParameters.get(Constants.PARAM_WORKING_FRAME));
			// TODO happeningFrame também deve ser enviado, para o caso de não ser o (workingFrame - 1) 
			this.happeningFrame = workingFrame - 1;
			// TODO Dar um tempo de sobra para o envio do evento
			this.period 		= Long.valueOf(serverParameters.get(Constants.PARAM_PERIOD));
			this.sendDeadline 	= Long.valueOf(serverParameters.get(Constants.PARAM_RCV_DEADLINE));
			// Armazena os parâmetros no Componente
			addParameter(Constants.PARAM_START_TIME, serverParameters.get(Constants.PARAM_START_TIME));
//			addParameter(Constants.PARAM_WORKING_FRAME, serverParameters.get(Constants.PARAM_WORKING_FRAME));
			addParameter(Constants.PARAM_PERIOD, serverParameters.get(Constants.PARAM_PERIOD));
			addParameter(Constants.PARAM_RCV_DEADLINE, serverParameters.get(Constants.PARAM_RCV_DEADLINE));
//			System.out.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + startTime + " " + workingFrame + " " + period + " " + sendDeadline);
		}
		addParameters(extraParameters);
		
		// Cria a memória relativa a esse EventHandler
		// TODO Pode ser que de problema a criação da memória estar aqui, se o usuário quiser usá-la antes
		Parameters memParameters = new Parameters();
		memParameters.merge(getParameters());
		memParameters.merge(extraParameters);
		myMemory = getAgent().getKB().createMemory(getComponentName(), getParameters());
		if (myMemory == null) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] It was not possible to create a memory! Deregistering...");
			deregister();
			return;
		}
//		MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Memória de '" + getName() + "' do tipo '" + eventType + "' foi criada");

		// Altera o status
		this.status = EH_STATUS.REGISTERED;
		
		Command cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("STATE", "REGISTERED");
		sendCommand(cmd);

		// Avisa o agente do novo EventHandler registrado
		getAgent().eventHandlerRegistered(getComponentName());
		
		// No caso de ser uma troca de evento frequente, armazena os parÃ¢metros
		if (eventExecution.equals(Constants.EVT_EXC_PERIODIC) && getType().equals(Constants.COMP_ACTUATOR)) {
			Actuator act = (Actuator)this;
			act.setEventFrequency();
		}
		
		if (this instanceof Sensor) {
			Sensor sensor = (Sensor)this;
			for (Event evt : sensor.early_events) {
				sensor.sense(evt);
			}
			sensor.early_events.clear();
		}
		
//		MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Register of '" + getComponentName() + "' confirmed");
		
	}
	
	// Chamado por um raciocínio para registrá-lo como listener dos eventos
	public void registerListener(Reasoning reasoning) {
		listeners.add(reasoning);
	}
	
	/**
	 * Remove o registro do EventHandler do EventServer correspondente
	 */
	public void deregister() {
		
		// Envia mensagem para tirar o EventHandler do registro
		Command cmd = new Command(getAddress(), "/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT, Constants.CMD_EVENT_DEREGISTER );
		cmd.addParameter(Constants.PARAM_COMP_NAME, getComponentName());
		cmd.addParameter(Constants.PARAM_COMP_TYPE, getType());
		cmd.addParameter(Constants.PARAM_EVT_TYPE, getEventType());
		sendCommand(cmd);
		
	}
	
	public void confirmDeregistration() {
		
		this.status = EH_STATUS.NOT_REGISTERED;
		
		Command cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("STATE", "NOT_REGISTERED");
		sendCommand(cmd);
		
		getAgent().eventHandlerDeregistered(getComponentName());

	}
	
}
