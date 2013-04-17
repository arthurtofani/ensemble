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

import ensemble.Constants.AC_STATE;
import ensemble.Constants.EA_STATE;
import ensemble.Constants.EH_STATUS;
import ensemble.clock.TimeUnit;
import ensemble.clock.VirtualClockHelper;

import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.WakerBehaviour;

// TODO: Auto-generated Javadoc
/**
 * The Class Actuator.
 */
public class Actuator extends EventHandler implements Acting {

	/** The tbf. */
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();

	/** The clock. */
	VirtualClockHelper clock;
	
	// Usado para referenciar dentro do WakerBehaviour
	/** The me. */
	Actuator me = this;
	
	/** The need action behaviour. */
	WakerBehaviour needActionBehaviour;
	
	/** The actuator state. */
	protected AC_STATE actuatorState;
	
	// Tempos configurados pelo usuário
	/** The send lag. */
	protected long sendLag 			= 0;
	
	/** The need action time. */
	protected long needActionTime 	= 0;
	
	/** The next state change. */
	protected long nextStateChange;
	
	// Define se o action é chamado automaticamente no momento (sendDeadline - sendLag)
	/** The automatic action. */
	protected boolean automaticAction 	= false;
		
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#start()
	 */
	@Override
	public final boolean start() {
	
		// Sets component type
		setType(Constants.COMP_ACTUATOR);
		
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addParameter("TYPE", getComponentType());
		cmd.addParameter("EVT_TYPE", parameters.get("EVT_TYPE"));
		cmd.addUserParameters(parameters);
		sendCommand(cmd);

		if (!super.start()) {
			return false;
		}
		
		clock = getAgent().getClock();
		
		myComm.actuating = true;
		
		// Calls user initialization code
		if (!init()) {
			return false;
		}

		// Sets the agent's state to INITIALIZED
		setState(EA_STATE.INITIALIZED);
		
		cmd = new Command(getAddress(), "/console", "UPDATE");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		cmd.addParameter("STATE", "INITIALIZED");
		sendCommand(cmd);

		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#stop()
	 */
	@Override
	public final boolean stop() {
		
		// Calls user initialization code
		if (!finit()) {
			return false;
		}
		
		// Calls EventHandler stop code
		if (!super.stop()) {
			return false;
		}
		
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", getAgent().getAgentName());
		cmd.addParameter("COMPONENT", getComponentName());
		sendCommand(cmd);
		
		return true; 
		
	}
	
	//--------------------------------------------------------------------------------
	
	// TODO Em que momento esses métodos seriam chamados pelo usuário?!?!
	/**
	 * Sets the need action time.
	 *
	 * @param needActionTime the new need action time
	 */
	protected void setNeedActionTime(long needActionTime) {
		this.needActionTime = needActionTime;
	}
	
	/**
	 * Sets the send lag.
	 *
	 * @param sendLag the new send lag
	 */
	protected void setSendLag(long sendLag) {
		this.sendLag = sendLag;
	}
	
	/**
	 * Sets the automatic action.
	 *
	 * @param automaticAction the new automatic action
	 */
	public void setAutomaticAction(boolean automaticAction) {
		this.automaticAction = automaticAction;
	}
	
	// TODO Se for chamado mais de uma vez, deve matar o Behaviour antigo!!!
	// TODO Ver a opção do raciocínio ser a avisado (needEvent())
	/**
	 * Sets the event frequency.
	 */
	protected void setEventFrequency() {

		actuatorState = AC_STATE.INITIALIZED;
//		getAgent().addBehaviour(tbf.wrap(new ActionScheduler()));
		clock.execute(getAgent(), new ActionScheduler());
//		(new Thread(new ActionScheduler())).start();

		// Cria um WakeBehaviour que chama o action() automaticamente 
			// TODO Certificar que inicia no tempo correto!!!
			// TODO Arrumar para o caso do momento atual em que passar por aqui for maior que o 'deadline'
//			if (automaticAction) {
//				deadline = myStartTime + mySendDeadline - sendLag;
//				getAgent().addBehaviour(tbf.wrap(new WakerBehaviour(getAgent(), new Date(deadline)) {
//					protected void onWake() {
//						reset(new Date(this.getWakeupTime() + period));
//						act();
//					}
//				}));
//			}
			
	}
	
	/**
	 * The Class ActionScheduler.
	 */
	private class ActionScheduler implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			
			switch (actuatorState) {
			
			case INITIALIZED:
				frameTime = startTime + ((workingFrame-1) * period);
//				System.out.println("frameTime = " + frameTime);
				actuatorState = AC_STATE.WAITING_BEGIN;
//				System.out.println(System.currentTimeMillis + "*** estado 1 ***");
				nextStateChange = frameTime;
				break;
			
			// Frame start
			case WAITING_BEGIN:
			case WAITING_RESPONSE:
//				reset(new Date(frameTime + needActionTime));
				if (actuatorState == AC_STATE.WAITING_RESPONSE) {
					workingFrame++;
					happeningFrame++;
				}
				actuatorState = AC_STATE.PROCESSING;
//				System.out.println(System.currentTimeMillis() + "*** estado 2 ***");
//				System.out.println("Novo despertador = " + (frameTime + needActionTime));
				nextStateChange = frameTime + needActionTime;
				break;

			// NeedAction() deadline
			case PROCESSING:
				// chama os needAction()
//				reset(new Date(frameTime + sendDeadline - sendLag));
				actuatorState = AC_STATE.NEED_ACTING;
//				System.out.println(System.currentTimeMillis() + "*** estado 3 ***");
//				System.out.println("Novo despertador = " + (frameTime + sendDeadline - sendLag));
				for (Reasoning reason : listeners) {
					//reason.needAction(me);
					NeedActionBehaviour nab = new NeedActionBehaviour(reason);
					getAgent().addBehaviour(tbf.wrap(nab));
//					nabs.add(nab);
				}
				nextStateChange = frameTime + sendDeadline - sendLag;
				break;

			// sendDeadline
			case NEED_ACTING:
//				reset(new Date(frameTime + period));
				// Interrompe os NeedActions que estiverem rodando
//				for (NeedActionBehaviour nab : nabs) {
//					Thread thread = tbf.getThread(nab);
//					if (thread != null) {
//						thread.interrupt();
//					}
//					if (nab.getExecutionState() == STATE_RUNNING) {
//						System.out.println("INTERROMPI!!!");
//					}
//				}
//				nabs.clear();
				// Chama o act() caso o usu�rio tenho habilitado o modo autom�tico 
				if (automaticAction) {
					act();
				}
				actuatorState = AC_STATE.WAITING_RESPONSE;
				frameTime = frameTime + period;
//				System.out.println(System.currentTimeMillis() + "*** estado 4 ***");
//				System.out.println("Novo despertador = " + (frameTime));
				nextStateChange = frameTime;
				break;

			default:
				break;
			}

//			System.out.println(getAddress() + " - schedule() - " + actuatorState);
			clock.schedule(getAgent(), this, nextStateChange);
			
		}
		
	}
	
	/**
	 * The Class NeedActionBehaviour.
	 */
	private class NeedActionBehaviour extends OneShotBehaviour {

		/** The reasoning. */
		private Reasoning reasoning;
		
		/**
		 * Instantiates a new need action behaviour.
		 *
		 * @param reasoning the reasoning
		 */
		public NeedActionBehaviour(Reasoning reasoning) {
			this.reasoning = reasoning;
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			try {
				double instant = (double)(startTime + (workingFrame * period))/1000;
				double duration = (double)period/1000;
				reasoning.needAction(me, instant, duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Acting#act()
	 */
	public void act() {

		Object content = null;
		
		if (eventExchange.equals(Constants.EVT_EXC_PERIODIC)) { 
			
			// Obter o evento a ser enviado da base de conhecimentos
			double instant = (double)(startTime + (workingFrame * period))/1000;
			double duration = (double)period/1000;
			content = myMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			
//			// Se for uma troca de evento frequente, só envia se for o frame correto
//			if (eventExchange.equals(Constants.EVT_EXC_FREQUENT) && evt.frame != workingFrame) {
//				System.out.println("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Frame n�o corresponde ao atual");
//				MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Frame n�o corresponde ao atual");
//				return;
//			}
			
		} else {
			
			// TODO E se não for em segundos? No caso de turnos!
			double instant = clock.getCurrentTime(TimeUnit.SECONDS);
			content = myMemory.readMemory(instant, TimeUnit.SECONDS);
			
		}

		Event evt = new Event();
		evt.objContent = content;
		
		// Chama o método implementado pelo usuário
		try {
			process(evt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		evt.oriAgentName 		= getAgent().getAgentName();
		evt.oriAgentCompName	= this.getComponentName();
		evt.destAgentName 		= getAgent().environmentAgent;
		evt.destAgentCompName	= eventType;
		evt.eventType 			= eventType;
		evt.timestamp 			= (long)getAgent().getClock().getCurrentTime(TimeUnit.MILLISECONDS);
		if (eventExchange.equals(Constants.EVT_EXC_PERIODIC)) {
			evt.frame = workingFrame;
			evt.instant = (double)(startTime + (workingFrame * period))/1000;
			evt.duration = (double)period/1000;
//			System.out.println(clock.getCurrentTime() + "\t [" + getAgent().getAgentName() + ":" + getName() + "] Atuei - frame = " + workingFrame);
		}
		if (status.equals(EH_STATUS.REGISTERED)) {
//			MusicalAgent.logger.info("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Gerei um evento");
//			System.out.println(clock.getCurrentTime() + " [" + getAgent().getAgentName() + ":" + getName() + "] " + "Gerei um evento");
			
			super.myComm.send(evt);
			
			// Avisa o Agente sobre o envio do evento (importante para o proc. Batch)
			getAgent().eventSent();
		} 
		else {
//			MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getName() + "] " + "Componente n�o registrado em um Ambiente!");
		}
		
	}
	
}
