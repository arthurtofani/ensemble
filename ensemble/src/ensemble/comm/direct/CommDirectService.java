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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import ensemble.Event;
import ensemble.MusicalAgent;
import ensemble.comm.Comm;

import jade.core.Agent;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class CommDirectService.
 */
public class CommDirectService extends BaseService {

	//----------------------------------------------------------
	// Log
//	private static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());
	
	//----------------------------------------------------------
	// Nome do servi�o
	/** The Constant NAME. */
	public static final String NAME = "CommDirect";

	//----------------------------------------------------------
	/** The comms. */
	private ConcurrentHashMap<String, Comm> comms = new ConcurrentHashMap<String, Comm>();
	
	/** The executor. */
	private static Executor executor;

	/* (non-Javadoc)
	 * @see jade.core.Service#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see jade.core.BaseService#boot(jade.core.Profile)
	 */
	public void boot(jade.core.Profile p) throws ServiceException {
		
		super.boot(p);
		
		executor = Executors.newCachedThreadPool();
//		executor = Executors.newFixedThreadPool(50);
		
        System.out.println("[" + getName() + "] CommDirect service started");

	}
	
	/* (non-Javadoc)
	 * @see jade.core.BaseService#getHelper(jade.core.Agent)
	 */
	public CommDirectHelper getHelper(Agent a) {
		
		return new CommDirectHelperImp();
	
	}
	
	/**
	 * The Class CommDirectTask.
	 */
	public class CommDirectTask implements Runnable {

		/** The comm. */
		Comm comm;
		
		/** The evt. */
		Event evt;
		
		/**
		 * Instantiates a new comm direct task.
		 *
		 * @param comm the comm
		 * @param evt the evt
		 */
		public CommDirectTask(Comm comm, Event evt) {
			this.comm 	= comm;
			this.evt 	= evt;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (comm != null) {
				comm.receive(evt);
			} else {
				System.err.println("[ERROR] Comm object does not exist!");
			}
		}
		
	}

	/**
	 * The Class CommDirectHelperImp.
	 */
	public class CommDirectHelperImp implements CommDirectHelper {

		/* (non-Javadoc)
		 * @see jade.core.ServiceHelper#init(jade.core.Agent)
		 */
		@Override
		public void init(Agent a) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see ensemble.comm.direct.CommDirectHelper#register(java.lang.String, java.lang.String, ensemble.comm.Comm)
		 */
		@Override
		public void register(String agentName, String accessPoint, Comm comm) {

			comms.put(agentName + ":" + accessPoint, comm);
			
		}

		/* (non-Javadoc)
		 * @see ensemble.comm.direct.CommDirectHelper#deregister(java.lang.String, java.lang.String)
		 */
		@Override
		public void deregister(String agentName, String accessPoint) {

			comms.remove(agentName + ":" + accessPoint);
			
		}

		/* (non-Javadoc)
		 * @see ensemble.comm.direct.CommDirectHelper#send(ensemble.Event)
		 */
		@Override
		public void send(Event evt) {

			CommDirect comm = (CommDirect)comms.get(evt.destAgentName + ":" + evt.destAgentCompName);
			// TODO talvez utilizar uma pool de threads?!
//			logger.info("[CommDirect] " + " Vou chamar a Thread!");
			CommDirectTask task = new CommDirectTask(comm, evt);
			executor.execute(task);
//			(new Thread(task)).start();
//			comm.receive(evt);
			
		}
	}
	
}
