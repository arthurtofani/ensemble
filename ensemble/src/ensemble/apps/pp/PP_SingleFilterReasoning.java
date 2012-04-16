package ensemble.apps.pp;


import java.util.Hashtable;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.audio.dsp.FilterProcessing;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.router.MessageConstants;


public class PP_SingleFilterReasoning extends Reasoning{

	// Audio
	Actuator 	mouth;
	Memory 		mouthMemory;
	Sensor 		ear;
	Memory 		earMemory;
	int 		chunk_size;
	float 		sampleRate;
	double 		frame_duration;
	private Sensor 		antenna;
	private Memory 		antennaMemory;
	
	//Time Related
	double 		start_time;
	
	//Auxiliar Memory
	Memory internalMemory;

	
	// Movement
	Actuator	legs;
	Memory 		legsMemory;
	Sensor 		eyes;
	Memory 		eyesMemory;

	
	//private World 	world;
	//private Vector 	actual_pos = null;
	
	
	
	enum ZoneMode {
		NOT_DEFINED,
		FIXED			
	}

	ZoneMode zoneMode = ZoneMode.FIXED;


	static int currentFreq = 300;
	static double rez = 1;

	// Reasoning state
	enum FilterType {
		LOWPASS,
		HIGHPASS,
		BYPASS
		}
	
	static FilterType filterType= FilterType.BYPASS;
	

	
	@Override
	public boolean init() {

		return true;
		
	}
	
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
		}
		else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}

	
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
			
			
			if (cmd != null && cmd.getCommand()!=MessageConstants.CMD_INFO) {
				
				if (cmd.getParameter(MessageConstants.PARAM_TYPE).equals(MessageConstants.ANDOSC_TYPE)) {
					
					
					if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.ANDOSC_ORIENTATION)) {
						
						/*System.out.println("Recebeu mensagem "
								+ cmd.getParameter(MessageConstants.PARAM_TYPE));
						*/	
						String[] val = cmd.getParameter(MessageConstants.PARAM_ARGS).split(" ");
						if(val.length ==3)
						{
						double valX = Double.parseDouble(val[0]);
						double valY = Double.parseDouble(val[1]);
						double valZ = Double.parseDouble(val[2]);
						
						currentFreq = getFreq(valX);
						rez = geRez(valY);
						}
					}else if (cmd.getParameter(MessageConstants.PARAM_ACTION).equals(MessageConstants.ANDOSC_TOUCH_POS)) {
						if(filterType == FilterType.BYPASS){
							filterType = FilterType.LOWPASS;
						}else if(filterType == FilterType.LOWPASS){
							filterType = FilterType.HIGHPASS;
						}else filterType = FilterType.BYPASS;
					}
				}
			}
		}
	}

	private double geRez(double valY) {
		//O valor vira entre -180 e 180
		double aux =  (valY + 180);
		double val = ( (Math.sqrt(2)-0.1) *aux /360) + 0.1;
		
		return val;
	}

	private int getFreq(double valX) {
		//Consideramos a frequencia de 100 a 1000 Hz
		//O valor vira entre 0 e 360
		int val = (int) ((900*valX /360) + 100);
		return val;
	}

	public void needAction(Actuator sourceActuator, double instant, double duration) {
		
			try {

				double[] dBuffer = new double[chunk_size];
				double[] dTransBuffer = new double[chunk_size];
				dBuffer = (double[])internalMemory.readMemory(instant - duration, duration, TimeUnit.SECONDS);
				
				
				FilterProcessing filterProcess = new FilterProcessing();
				
				//filterProcess.FourPolesHighPass(dBuffer, dTransBuffer, chunk_size, 600);
				//filterProcess.ProcessLPF(dBuffer, dTransBuffer, chunk_size, 600, sampleRate);
				
				//filterProcess.FourPolesLowPass(dBuffer, dTransBuffer, chunk_size, 1700);
				//filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, 400, 44100);
				//filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, 800, 44100);
				
				//Teste peaks
				//double peak = AnalysisProcessing.peakFollower(44100, dBuffer, chunk_size);	
				//System.out.println("Peak: " + peak);
				
				switch(filterType){
				case BYPASS:
					dTransBuffer = dBuffer;
					//System.out.println("BYPASS - freq: " + currentFreq);
					break;
				case LOWPASS:	
					filterProcess.lowPass(dBuffer, dTransBuffer, chunk_size, currentFreq, 44100);
					//System.out.println("LOWPASS - freq: " + currentFreq);
					break;
				case HIGHPASS:	
					filterProcess.highPass(dBuffer, dTransBuffer, chunk_size, currentFreq, 44100);
					//System.out.println("HIGHPASS - freq: " + currentFreq);
					break;	
				}
				
				mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);
					
					
					//mouthMemory.writeMemory(dTransBuffer, instant + duration, duration, TimeUnit.SECONDS);

					//System.out.println("Instant: " + instant + " Duration: " + duration );
				
				//System.out.println("Guardei na memória principal um evento no instante " + instant + " de duração " + duration);
				mouth.act();
					
			} catch (MemoryException e) {
				e.printStackTrace();}

			
//		System.out.println("REAS time = " + (System.currentTimeMillis() - start));
	}

	@Override
	public void processCommand(Command cmd) {
		if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_OFF)) {
			zoneMode = ZoneMode.NOT_DEFINED;
		} else if (cmd.getCommand().equals(AudioConstants.CMD_ZONE_ON)) {
			zoneMode = ZoneMode.FIXED;
		} 
	}
	
	
	public void process() throws Exception {

		
	}
	
	
	
}