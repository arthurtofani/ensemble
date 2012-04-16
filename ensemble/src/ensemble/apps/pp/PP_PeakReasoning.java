package ensemble.apps.pp;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.audio.dsp.MinimFftProcessing;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.router.MessageConstants;

public class PP_PeakReasoning extends Reasoning{
	
	
	// Audio
	Actuator 	mouth;
	Memory 		mouthMemory;
	int 		chunk_size;
	float 		sampleRate;
	double 		frame_duration;
	
	//Messages
	private Sensor 		antenna;
	private Memory 		antennaMemory;
	
	private Actuator 	messenger;
	private Memory 		messengerMemory;


	private double MIN_PEAK = 0.4;
	private double MAX_PEAK = 0.7;
	
	private boolean checkPeak = false;
	
	@Override
	public boolean init() {

		return true;
		
	}
	
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		
		// Checks if it is a sound Actuator
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			mouthMemory = getAgent().getKB().getMemory( mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			frame_duration = chunk_size / sampleRate;

		}
		else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			antenna = (Sensor)evtHdl;
			antenna.registerListener(this);
			antennaMemory = getAgent().getKB().getMemory(antenna.getComponentName());
		}else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MessageConstants.EVT_TYPE_MESSAGE)) {
			
			messenger = (Actuator)evtHdl;
			messenger.registerListener(this);
			messengerMemory = getAgent().getKB().getMemory(messenger.getComponentName());
		}

	
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {
		
		if(sourceSensor.getEventType().equals(
				MessageConstants.EVT_TYPE_MESSAGE)){
			
			String str = (String) antennaMemory.readMemory(instant,
					TimeUnit.SECONDS);
			Command cmd = Command.parse(str);
					
			
		}
	}
	
	
	public void needAction(Actuator sourceActuator, double instant, double duration) {
		
		try {
			if (checkPeak) {
				double[] dBuffer = new double[chunk_size];

				dBuffer = (double[]) mouthMemory.readMemory(instant - duration,
						duration, TimeUnit.SECONDS);

				double peak = AnalysisProcessing.peakFollower(44100, dBuffer,
						chunk_size);

				/*
				 * double fft = MinimFftProcessing.getBandFft(dBuffer, 44100,
				 * (int) frame_duration, chunk_size);
				 * System.out.println("FFT result: " + fft);
				 */
				// if(peak > 0 && (peak < MIN_PEAK || peak > MAX_PEAK)){
				if (peak > MIN_PEAK) {

					Command cmd = new Command(MessageConstants.CMD_RECEIVE);
					cmd.addParameter(MessageConstants.PARAM_TYPE,
							MessageConstants.DIRECTION_TYPE);
					cmd.addParameter(MessageConstants.PARAM_DOMAIN,
							MessageConstants.INTERNAL_DOMAIN);
					cmd.addParameter(MessageConstants.PARAM_ACTION,
							MessageConstants.DIRECTION_CHANGE);

					if (peak < MAX_PEAK)
						cmd.addParameter(MessageConstants.PARAM_ARGS,
								MessageConstants.DIRECTION_UP);
					else
						cmd.addParameter(MessageConstants.PARAM_ARGS,
								MessageConstants.DIRECTION_DOWN);

					messengerMemory.writeMemory(cmd);
					messenger.act();
				}

				// System.out.println("Instant: " + instant + " Duration: " +
				// duration );

				// System.out.println("Guardei na memória principal um evento no instante "
				// + instant + " de duração " + duration);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			checkPeak = false;
		}

//	System.out.println("REAS time = " + (System.currentTimeMillis() - start));
}
	
	@Override
	public void process() {
		
		checkPeak= true;
	}

}
