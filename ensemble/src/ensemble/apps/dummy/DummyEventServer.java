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

package ensemble.apps.dummy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Enumeration;

import ensemble.Command;
import ensemble.Constants;
import ensemble.Event;
import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.clock.TimeUnit;
import ensemble.memory.EventMemory;
import ensemble.memory.Memory;
import ensemble.world.World;


// TODO: Auto-generated Javadoc
/**
 * The Class DummyEventServer.
 */
public class DummyEventServer extends EventServer {
	
	/** The world. */
	private World world;
	
//	PrintWriter file_perf;
	
	/** The number frames. */
private int numberFrames;
	
	/** The events received. */
	private int eventsReceived = 0;
	
	/** The events received in frame. */
	private int eventsReceivedInFrame = 0;
	
	/** The number agents. */
	private int numberAgents = 0;
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("DUMMY");
		if (parameters.containsKey(Constants.PARAM_PERIOD)) {
			String[] str = (parameters.get(Constants.PARAM_PERIOD)).split(" ");
			setEventExchange(Integer.valueOf(str[0]), Integer.valueOf(str[1]), Integer.valueOf(str[2]), Integer.valueOf(str[3]));
		} else {
			setEventExchange(500, 200, 400, 1000);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#init()
	 */
	@Override
	public boolean init() {
		world = envAgent.getWorld();
//		try {
//			file_perf = new PrintWriter(new FileOutputStream("out_Environment.txt"), false);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		File dir = new File("./tests");
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("out_");
			}
		});
		for (File file : files) {
			file.delete();
		}
		numberFrames = 5;
//		numberFrames = (int)(1000 / period);
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#finit()
	 */
	@Override
	public boolean finit() {
		
		// Fazer as contas!!!
		int agentsEventsReceived = 0;
		// abrir todos os arquivos dos agentes
		File dir = new File("./tests");
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.startsWith("out_");
			}
		});
		for (File file : files) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (br.readLine() != null) {
					agentsEventsReceived++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Number of Agents = " + numberAgents);
		System.out.println("Events received ES = " + eventsReceived);
		System.out.println("Events received MAs = " + agentsEventsReceived);
		double expected = numberAgents * numberFrames;
		double ratio = (agentsEventsReceived / expected);
		System.out.printf("%.4f\n", ratio);
		
//		// Cair fora
		System.exit(0);
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#actuatorRegistered(java.lang.String, java.lang.String, ensemble.Parameters)
	 */
	@Override
	protected Parameters actuatorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {

		numberAgents++;
		
		// Parâmetos
		Parameters userParameters = new Parameters();
//		userParameters.put(Constants.PARAM_CHUNK_SIZE, String.valueOf());
		
		// Cria uma memória para o atuador
		if (world.getEntityStateAttribute(agentName, "DUMMY") == null) {
	    	world.addEntityStateAttribute(agentName, "DUMMY", createMemory("DUMMY", userParameters));
		}
    	
		return userParameters;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#sensorRegistered(java.lang.String, java.lang.String, ensemble.Parameters)
	 */
	@Override
	protected Parameters sensorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		// Parâmetos
		Parameters userParameters = new Parameters();

		// Cria uma memória para o atuador
		if (world.getEntityStateAttribute(agentName, "DUMMY") == null) {
	    	world.addEntityStateAttribute(agentName, "DUMMY", createMemory("DUMMY", userParameters));
		}

		return userParameters;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#processSense(ensemble.Event)
	 */
	@Override
	protected void processSense(Event evt) throws Exception {
 
		if (evt.frame == workingFrame) {
			eventsReceivedInFrame++;
			// Recebe o evento e armazena na memória
			Memory memory = (Memory)world.getEntityStateAttribute(evt.oriAgentName, "DUMMY");
			memory.writeMemory(evt);
		} else {
			System.out.println("OPS!!!");
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#process()
	 */
	@Override
	protected void process() throws Exception {
		
		if (workingFrame > numberFrames) {

			Command cmd = new Command(getAddress(), "/"+Constants.FRAMEWORK_NAME+"/"+Constants.ENVIRONMENT_AGENT, "REMOVE_EVENT_SERVER");
			cmd.addParameter("NAME", getEventType());
//			Command cmd = new Command(getAddress(), "/"+Constants.FRAMEWORK_NAME+"/"+Constants.ENVIRONMENT_AGENT, "STOP_SIMULATION");
			sendCommand(cmd);
			
		} else {
		
			double instant = startTime + (workingFrame * period);
			
			eventsReceived += eventsReceivedInFrame;
//			file_perf.printf("%d - %d\n", workingFrame, eventsReceived);
//			file_perf.flush();
	 		System.out.println(envAgent.getClock().getCurrentTime(TimeUnit.MILLISECONDS) + " - process() - wf = " + workingFrame + "(" +  eventsReceivedInFrame + ")");
			eventsReceivedInFrame = 0;

			for (Enumeration<String> s = sensors.keys(); s.hasMoreElements();) {
	
				String s_key = s.nextElement();
				String[] sensor = s_key.split(":");
	
//				System.out.println(instant + " " + evt1.frame);
				Memory memory = (Memory)world.getEntityStateAttribute(sensor[0], "DUMMY");
				Event evt1 = (Event)memory.readMemory(instant, TimeUnit.MILLISECONDS);
				if (evt1 != null && evt1.frame == workingFrame) {
					// Cria o evento a ser enviado para o sensor
					Event evt = new Event();
					evt.destAgentName = sensor[0];
					evt.destAgentCompName = sensor[1];
					double[] buf = new double[1024];
					evt.objContent = buf;
		//			evt.instant = instant;
		//			evt.duration = (double)(CHUNK_SIZE * STEP);
					
					// Puts the newly created event in the output queue
					sendEvent(evt);
				}
				
			}
		
			
		}
		
	}

}
