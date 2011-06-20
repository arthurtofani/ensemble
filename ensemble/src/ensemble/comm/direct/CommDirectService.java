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

public class CommDirectService extends BaseService {

	//----------------------------------------------------------
	// Log
//	private static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());
	
	//----------------------------------------------------------
	// Nome do servi�o
	public static final String NAME = "CommDirect";

	//----------------------------------------------------------
	private ConcurrentHashMap<String, Comm> comms = new ConcurrentHashMap<String, Comm>();
	
	private static Executor executor;

	@Override
	public String getName() {
		return NAME;
	}

	public void boot(jade.core.Profile p) throws ServiceException {
		
		super.boot(p);
		
		executor = Executors.newCachedThreadPool();
//		executor = Executors.newFixedThreadPool(50);
		
        System.out.println("[" + getName() + "] CommDirect service started");

	}
	
	public CommDirectHelper getHelper(Agent a) {
		
		return new CommDirectHelperImp();
	
	}
	
	public class CommDirectTask implements Runnable {

		Comm comm;
		Event evt;
		
		public CommDirectTask(Comm comm, Event evt) {
			this.comm 	= comm;
			this.evt 	= evt;
		}
		
		@Override
		public void run() {
			if (comm != null) {
				comm.receive(evt);
			} else {
				System.err.println("[ERROR] Comm object does not exist!");
			}
		}
		
	}

	public class CommDirectHelperImp implements CommDirectHelper {

		@Override
		public void init(Agent a) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void register(String agentName, String accessPoint, Comm comm) {

			comms.put(agentName + ":" + accessPoint, comm);
			
		}

		@Override
		public void deregister(String agentName, String accessPoint) {

			comms.remove(agentName + ":" + accessPoint);
			
		}

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
