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

package ensemble.comm.direct;

import ensemble.Event;
import ensemble.MusicalAgent;
import ensemble.comm.Comm;
import jade.core.ServiceException;
import jade.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class CommDirect.
 */
public class CommDirect extends Comm {
	
	//----------------------------------------------------------
	// Log
//	private static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	//----------------------------------------------------------
	// Serviço de CommDirect
	/** The comm direct. */
	protected CommDirectHelper commDirect;
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public final boolean configure() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public final boolean init() {
		
		try {
//			System.out.println("COMM = " + myAgent.getName());
			commDirect = (CommDirectHelper)myAgent.getHelper(CommDirectService.NAME);
		} catch (ServiceException e) {
//			logger.severe("[" + myAgent.getAgentName() + "] " + "CommDirect service not available");
			System.err.println("[" + myAgent.getAgentName() + "] " + "CommDirect service not available");
			return false;
		}

		// registrar Agente/EventHandler no Service CommDirect
		commDirect.register(myAgent.getAgentName(), myAccessPoint, this);
		
		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public final boolean finit() {
		
		commDirect.deregister(myAgent.getAgentName(), myAccessPoint);
		
		return true;
		
	}

	/* (non-Javadoc)
	 * @see ensemble.comm.Comm#receive(ensemble.Event)
	 */
	@Override
	public void receive(Event evt) {
//		MusicalAgent.logger.info("[" + myAgent.getAID().getAgentName() + ":" + myAccessPoint + "] " + "Enviei evento via CommDirect");
		//eventQueue.add(evt);
		if (mySensor != null) {
			mySensor.sense(evt);
		} else {
//			MusicalAgent.logger.warning("[" + myAgent.getName() + "] " + "ERRO: não pertenço um sensor!");
		}
 	}

	/* (non-Javadoc)
	 * @see ensemble.comm.Comm#send(ensemble.Event)
	 */
	@Override
	public void send(Event evt) {
//		MusicalAgent.logger.info("[" + myAgent.getAID().getAgentName() + ":" + myAccessPoint + "] " + "Enviei evento via CommDirect");
		commDirect.send(evt);
	}

}
