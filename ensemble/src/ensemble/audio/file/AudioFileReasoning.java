package ensemble.audio.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.tools.AudioTools;

public class AudioFileReasoning extends Reasoning {

	// Memories
	private HashMap<String,Memory> sensorMemories = new HashMap<String, Memory>(2);
	private HashMap<String,Memory> actuatorMemories = new HashMap<String, Memory>(2);
	
	// Active Mappings
	private HashMap<String, Mapping> mappings = new HashMap<String, Mapping>(2);
	
	int chunk_size;
	
	@Override
	public boolean init() {
		
		return true;
		
	}
	
	@Override
	public boolean finit() {
		// Closes all open files
		Set<String> keys = mappings.keySet();
		for (String key : keys) {
			Mapping mapping = mappings.get(key);
			if (mapping.playing) {
				try {
					((ByteArrayOutputStream)mapping.stream).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					((ByteArrayInputStream)mapping.stream).close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) throws Exception {
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			// Stores sensor's memory
			Sensor sensor = (Sensor)evtHdl;
			sensor.registerListener(this);
			sensorMemories.put(sensor.getComponentName(), getAgent().getKB().getMemory(sensor.getComponentName()));
		}
		else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			// Stores actuator's memory
			Actuator actuator = (Actuator)evtHdl;
			actuator.registerListener(this);
			actuatorMemories.put(actuator.getComponentName(), getAgent().getKB().getMemory(actuator.getComponentName()));
			chunk_size = Integer.parseInt(actuator.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
		}
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) throws Exception {

		// Checks if there's a mapping for this sensor
		if (mappings.containsKey(sourceSensor.getComponentName())) {
		
			System.out.println("Entrei no newSense()");
			
			Mapping map = mappings.get(sourceSensor.getComponentName());
			double[] buf = (double[])map.ehMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			byte[] b = AudioTools.convertDoubleByte(buf, 0, buf.length);
			((FileOutputStream)map.stream).write(b);
		
		}
		
	}
	
	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) throws Exception {

		// Checks if there's a mapping for this actuator
		if (mappings.containsKey(sourceActuator.getComponentName())) {
			
			Mapping map = mappings.get(sourceActuator.getComponentName());
			byte[] b = new byte[2*chunk_size];
			int res = ((FileInputStream)map.stream).read(b);
			try {
				map.ehMemory.writeMemory(AudioTools.convertByteDouble(b, 0, b.length), instant, duration, TimeUnit.SECONDS);
			} catch (MemoryException e1) {
				e1.printStackTrace();
			}
			
			sourceActuator.act();
			
			if (res == -1) {
				System.out.println("Fim do arquivo!");
				mappings.remove(sourceActuator.getComponentName());
			}

		}

	}
	
	@Override
	public void processCommand(Command cmd) {
		
		if (cmd.getCommand().equals("RECORD")) {
			
			System.out.println("Command RECORD");
			
			String sensorName = cmd.getParameter("SENSOR");
			String file = cmd.getParameter("FILE");
			
			if (!mappings.containsKey(sensorName)) {
				Mapping map = new Mapping();
				map.playing = false;
				map.ehMemory = sensorMemories.get(sensorName);
				if (map.ehMemory == null) {
					return;
				}
				try {
					map.stream = new FileOutputStream(new File(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				mappings.put(sensorName, map);

			}
			
		}
		else if (cmd.getCommand().equals("PLAY")) {
			
			System.out.println("Command PLAY");

			String actuatorName = cmd.getParameter("ACTUATOR");
			String file = cmd.getParameter("FILE");

			if (!mappings.containsKey(actuatorName)) {
				Mapping map = new Mapping();
				map.playing = false;
				map.ehMemory = actuatorMemories.get(actuatorName);
				if (map.ehMemory == null) {
					return;
				}
				try {
					map.stream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				mappings.put(actuatorName, map);
				
			}
			
		}
		else if (cmd.getCommand().equals("STOP")) {
			
			String mapping = cmd.getParameter("EH");
			if (mappings.containsKey(mapping)) {
				Mapping map = mappings.remove(mapping);
				if (map.playing) {
					try {
						((FileOutputStream)map.stream).close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						((FileInputStream)map.stream).close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
	}
	
	class Mapping {
		boolean playing; 
		Memory 	ehMemory;
		Object 	stream;
	}
	
}
