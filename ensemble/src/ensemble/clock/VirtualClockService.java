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

package ensemble.clock;

import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Constants;
import ensemble.MusicalAgent;
import ensemble.clock.TimeUnit;


import jade.core.Agent;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class VirtualClockService.
 */
public class VirtualClockService extends BaseService {

	//----------------------------------------------------------
	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// Service name
	/** The Constant NAME. */
	public static final String NAME = "VirtualClock";

	// Indica se o clock funciona em tempo real ou em batch
	/** The user clock mode. */
	private boolean userClockMode = false;

	//----------------------------------------------------------
	// USER MODE
	//----------------------------------------------------------

	// Contador de tempo, no caso de processamento Batch
	/** The user time. */
	private static long userTime = 0;

	// Lock para criar um Mutual exclusion entre os mÈtodos updateClock() e schedule()
	/** The lock. */
	private static Lock lock = new ReentrantLock();

	// Despertadores agendados
	/** The scheduled tasks. */
	private static HashMap<Long,UserModeClockTask> scheduledTasks = new HashMap<Long,UserModeClockTask>();

	//----------------------------------------------------------
	// CPU MODE
	//----------------------------------------------------------
	/** The reference start time. */
	private long referenceStartTime;
	
	/** The reference nano start time. */
	private long referenceNanoStartTime;
	
	/** The scheduler. */
	private static ScheduledThreadPoolExecutor scheduler;
	
	//----------------------------------------------------------

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

		if (p.getParameter(Constants.CLOCK_MODE, Constants.CLOCK_CPU).equals(Constants.CLOCK_USER)) {
			userClockMode = true;
		} else {
			referenceStartTime = System.currentTimeMillis();
			referenceNanoStartTime = System.nanoTime();
//			MusicalAgent.logger.info("[" + NAME + "] " + "Reference Time = " + referenceStartTime);
		}
		
		int num_threads = Integer.valueOf(p.getParameter(Constants.SCHEDULER_THREADS, "5"));
		scheduler = new ScheduledThreadPoolExecutor(num_threads);
		scheduler.prestartAllCoreThreads();
		
        System.out.println("[" + getName() + "] VirtualClock service started");

	}
	
	/* (non-Javadoc)
	 * @see jade.core.BaseService#getHelper(jade.core.Agent)
	 */
	public VirtualClockHelper getHelper(Agent a) {
		return new VirtualClockHelperImp();
	}
	
	/**
	 * The Class VirtualClockHelperImp.
	 */
	public class VirtualClockHelperImp implements VirtualClockHelper {

		/* (non-Javadoc)
		 * @see jade.core.ServiceHelper#init(jade.core.Agent)
		 */
		public void init(Agent a) {
		}
		
		/* (non-Javadoc)
		 * @see ensemble.clock.VirtualClockHelper#getCurrentTime(ensemble.clock.TimeUnit)
		 */
		public double getCurrentTime(TimeUnit unit) {
			double ret = 0.0;
			switch (unit) {
			case TURNS:
				ret = userTime;
				break;
			case SECONDS:
				ret = ((double)(System.currentTimeMillis() - referenceStartTime))/1000;
				break;
			case MILLISECONDS:
				ret = (double)(System.currentTimeMillis() - referenceStartTime);
				break;
			}
			return ret;
		}
		
		/* (non-Javadoc)
		 * @see ensemble.clock.VirtualClockHelper#schedule(jade.core.Agent, java.lang.Runnable, long)
		 */
		public void schedule(Agent a, Runnable b, long wakeupTime) {

			// CLOCK_USER_MODE
			if (userClockMode) {
				
				// No caso do processamento Batch, È importante n„o deixar o schedule() entrar em conflito com o updateClock()
				lock.lock();
				try {
					if (wakeupTime <= userTime) {
						scheduler.execute(b);
					}
					else if (scheduledTasks.containsKey(wakeupTime)) {
						UserModeClockTask existingTask = scheduledTasks.get(wakeupTime);
						while (existingTask.nextTask != null) {
							existingTask = existingTask.nextTask;
						}
						existingTask.nextTask = new UserModeClockTask(a, b);
					} else {
						scheduledTasks.put(wakeupTime, new UserModeClockTask(a, b));
					}
				} finally {
					lock.unlock();
				}
				
			} else {
			
				long wakeupMili = wakeupTime;
				long now = (long)getCurrentTime(TimeUnit.MILLISECONDS);
				if (wakeupMili > now) {
					scheduler.schedule(b, wakeupMili - now, java.util.concurrent.TimeUnit.MILLISECONDS);
				} else {
					//System.out.println("ERRO!");
					scheduler.execute(b);
				}
		//			System.out.println("(" + now + ") " + a.getAgentName() + " vai acordar em \t" + (wakeupMili - now) + "\t(" + System.currentTimeMillis() + ")");
			}
			
		}

		/* (non-Javadoc)
		 * @see ensemble.clock.VirtualClockHelper#execute(jade.core.Agent, java.lang.Runnable)
		 */
		@Override
		public void execute(Agent a, Runnable b) {
			(new Thread(b)).start();
//			scheduler.execute(b);
		}

		/* (non-Javadoc)
		 * @see ensemble.clock.VirtualClockHelper#updateClock(long)
		 */
		@Override
		public void updateClock(long units) {
			if (userClockMode) {
				lock.lock();
				try {
					// Updates the clock
					userTime += units;
					// Verifica os despertadores para o tempo atual
					UserModeClockTask task = scheduledTasks.remove(userTime);
					while (task != null) {
						task.b.run();
						task = task.nextTask;
					}
					
				} finally {
					lock.unlock();
				}
			}
			else {
//				logger.warning("[VirtualClock] " + "updateClock() was called without being in USER mode");
			}
		}

		/* (non-Javadoc)
		 * @see ensemble.clock.VirtualClockHelper#updateClock()
		 */
		@Override
		public void updateClock() {
			this.updateClock(1);
		}

	}
	
	// Internal class that stores user mode clock tasks
	/**
	 * The Class UserModeClockTask.
	 */
	class UserModeClockTask {

		/** The a. */
		protected Agent a;
		
		/** The b. */
		protected Runnable b;
		
		/** The next task. */
		public UserModeClockTask nextTask = null;
		
		/**
		 * Instantiates a new user mode clock task.
		 *
		 * @param a the a
		 * @param b the b
		 */
		public UserModeClockTask(Agent a, Runnable b) {
			this.a = a;
			this.b = b;
		}
		
	}

		
}
