package ensemble.apps.pp;

import java.util.Hashtable;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.FilterProcessing;
import ensemble.audio.dsp.SmbPitchProcess;
import ensemble.audio.dsp.SmbtPitchProcessing;
import ensemble.audio.dsp.SoundTouchProcessing;
import ensemble.audio.vst.VstProcessReasoning;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.router.MessageConstants;
import ensemble.world.Vector;


// TODO: Auto-generated Javadoc
/**
 * The Class PP_QuadrantReasoning.
 */
public class PP_QuadrantReasoning extends Reasoning{

	// Audio
	/** The mouth. */
	Actuator 	mouth;
	
	/** The mouth memory. */
	Memory 		mouthMemory;
	
	/** The ear. */
	Sensor 		ear;
	
	/** The ear memory. */
	Memory 		earMemory;
	
	/** The chunk_size. */
	int 		chunk_size;
	
	/** The sample rate. */
	float 		sampleRate;
	
	/** The frame_duration. */
	double 		frame_duration;
	
	
	//Time Related
	/** The start_time. */
	double 		start_time;
	
	//Auxiliar Memory
	/** The internal memory. */
	Memory internalMemory;

	
	// Movement
	/** The legs. */
	Actuator	legs;
	
	/** The legs memory. */
	Memory 		legsMemory;
	
	/** The eyes. */
	Sensor 		eyes;
	
	/** The eyes memory. */
	Memory 		eyesMemory;

	//Messages
	/** The antenna. */
	private Sensor 		antenna;
	
	/** The antenna memory. */
	private Memory 		antennaMemory;
	
	/** The LO w_ pas s1. */
	private double LOW_PASS1 = 300;
	
	/** The LO w_ pas s2. */
	private double LOW_PASS2 = 500;
	
	/** The HIG h_ pas s1. */
	private double HIGH_PASS1 = 400;
	
	/** The HIG h_ pas s2. */
	private double HIGH_PASS2 = 800;
	
	//private World 	world;
	/** The actual_pos. */
	private Vector 	actual_pos = null;
	
	// Zones
	/**
	 * The Enum WorldZone.
	 */
	enum WorldZone {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The upper left. */
		UPPER_LEFT,
		
		/** The upper right. */
		UPPER_RIGHT,
		
		/** The lower left. */
		LOWER_LEFT,
		
		/** The lower right. */
		LOWER_RIGHT,
		
		/** The bypass cross. */
		BYPASS_CROSS
	}

	/** The cross_width. */
	private int cross_width = 2;
	
	/** The zone. */
	WorldZone zone = WorldZone.NOT_DEFINED;
	
	//VST definitions
	
	/**
	 * The Enum ZoneMode.
	 */
	enum ZoneMode {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The fixed. */
		FIXED			
	}

	/** The zone mode. */
	ZoneMode zoneMode = ZoneMode.FIXED;

	/** The audio file list. */
	public String[] audioFileList;

	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {

		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		// Checks if it is a sound Actuator
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			internalMemory = getAgent().getKB().getMemory( mouth.getComponentName() + Constants.SUF_AUXILIAR_MEMORY);
			mouthMemory = getAgent().getKB().getMemory( mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			frame_duration = chunk_size / sampleRate;


		
			
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			//Checks if it is a sound sensor
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement Actuator
			
		} else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			// Checks if it is a Movement sensor
			eyes = (Sensor)evtHdl;
			eyes.registerListener(this);
			eyesMemory = getAgent().getKB().getMemory(eyes.getComponentName());
		}else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}
	
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			//System.out.println(" ENtrou em message");
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			
		
			
			Command cmd = Command.parse(str);
			
			if (cmd != null && cmd.getCommand()!= MessageConstants.CMD_INFO) {
			
				if (cmd.getParameter(MessageConstants.PARAM_TYPE)
						.equals(MessageConstants.PP_OSC_TYPE)) {
					if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(
							MessageConstants.CONTROL_OSC_FREQ)) {
						String[] val = cmd.getParameter(
								MessageConstants.PARAM_ARGS).split(" ");
						
						//System.out.println(val[0] + " LOW_PASS1 " + LOW_PASS1 +" LOW_PASS2" + LOW_PASS2 +" HIGH_PASS1" + HIGH_PASS1 +" HIGH_PASS2" + HIGH_PASS2);

						if (val[0].equals("LP1")) {
							LOW_PASS1 = Double.parseDouble(val[1]);
						} else if (val[0].equals("LP2")) {
							LOW_PASS2 = Double.parseDouble(val[1]);
						} else if (val[0].equals("HP1")) {
							HIGH_PASS1 = Double.parseDouble(val[1]);
						} else if (val[0].equals("HP2")) {
							HIGH_PASS2 = Double.parseDouble(val[1]);
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	public void needAction(Actuator sourceActuator, double instant, double duration) {

		
		switch (zoneMode) {
		
		case FIXED:

			try {

				double[] dBuffer = new double[chunk_size];
				double[] dTransBuffer = new double[chunk_size];
				
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				
				FilterProcessing filterProcess = new FilterProcessing();
				//SoundTouchProcessing soundT = new SoundTouchProcessing();
				
				WorldZone currentZone = getZone(10);
				
				switch(currentZone){
				
				case UPPER_LEFT:
					filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, HIGH_PASS1, 44100);
					//System.out.println("Entrei " + chunk_size);
					//SmbPitchProcess.smbPitchShift((float) 1, chunk_size, 1024, 8, 44100, dBuffer, dTransBuffer);
					//soundT.test();
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					break;
				case UPPER_RIGHT:
					filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, HIGH_PASS2, 44100);
					
					//mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				case LOWER_LEFT:
					filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, LOW_PASS1, 44100);
					
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
		
					break;
				case LOWER_RIGHT:
					filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, LOW_PASS2, 44100);
					
					mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
		
					break;
				default:	
					dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);

					mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					break;
				
				}
	

				//System.out.println("Instant: " + instant + " Duration: " + duration );
				
				//System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
				mouth.act();
					
			} catch (MemoryException e) {
				e.printStackTrace();
			
			}

			
			break;

		case NOT_DEFINED:
		
		try {
			
			double[] dBuffer = new double[chunk_size];
			
			dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);

			mouthMemory.writeMemory(dBuffer, instant + duration, duration, TimeUnit.SECONDS);
			
			System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
			
			mouth.act();
				
		} catch (MemoryException e) {
			e.printStackTrace();
		} 
		break;
	}
//		System.out.println("REAS time = " + (System.currentTimeMillis() - start));
	}

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_OFF)) {
			zoneMode = ZoneMode.NOT_DEFINED;
		} else if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_ON)) {
			zoneMode = ZoneMode.FIXED;
		} 
	}
	
	/**
	 * Gets the zone.
	 *
	 * @param size the size
	 * @return the zone
	 */
	public WorldZone getZone(int size){
		
				
		String str = (String)eyesMemory.readMemory(eyesMemory.getLastInstant(), TimeUnit.SECONDS);
		Command cmd = Command.parse(str);
		if (cmd != null) {
			actual_pos = Vector.parse(cmd.getParameter(MovementConstants.PARAM_POS));
		}
		
		int x = 0;
		int y = 1;
		double valX = actual_pos.getValue(x);
		double valY = actual_pos.getValue(y);

		if (valX > (cross_width / 2) && valY > (cross_width / 2)) {

			//System.out.println("UPPER_LEFT");

			return WorldZone.UPPER_LEFT;

		} else if (valX < -(cross_width / 2) && valY > (cross_width / 2)) {

			//System.out.println("LOWER_LEFT");

			return WorldZone.LOWER_LEFT;

		} else if (valX > (cross_width / 2) && valY < -(cross_width / 2)) {
			//System.out.println("UPPER_RIGHT");

			return WorldZone.UPPER_RIGHT;

		} else if (valX < -(cross_width / 2) && valY < -(cross_width / 2)) {

			//System.out.println("LOWER_RIGHT");

			return WorldZone.LOWER_RIGHT;
		} else
			return WorldZone.BYPASS_CROSS;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	public void process() throws Exception {

		
	}
	
	
	
}