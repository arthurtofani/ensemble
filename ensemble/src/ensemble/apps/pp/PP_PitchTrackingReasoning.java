
package ensemble.apps.pp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.dsp.AnalysisProcessing;
import ensemble.audio.dsp.TarsosProcessing;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.router.MessageConstants;

public class PP_PitchTrackingReasoning extends Reasoning{
	
	
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


	private int MIN_PITCH = 150;
	private int MAX_PITCH = 3500;
	private boolean checkPitch = false; 

	private TarsosProcessing tarsos = new TarsosProcessing();
	
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
	
	public static String now() {
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
	    return sdf.format(cal.getTime());

	  }
	
	public void needAction(Actuator sourceActuator, double instant, double duration) {

		try {

			if (checkPitch) {
				double[] dBuffer = new double[chunk_size];

				dBuffer = (double[]) mouthMemory.readMemory(instant - duration,
						duration, TimeUnit.SECONDS);
				float pitch = tarsos.pitchTrack(dBuffer, chunk_size, 44100);
				// int pitch = AnalysisProcessing.pitchFollower(44100, dBuffer,
				// chunk_size);

				System.out.println("Pitch " + pitch + "Hz Time:" + now());
				if (pitch > 0) {
					if (pitch < MIN_PITCH || pitch > MAX_PITCH) {

						Command cmd = new Command(MessageConstants.CMD_RECEIVE);
						cmd.addParameter(MessageConstants.PARAM_TYPE,
								MessageConstants.DIRECTION_TYPE);
						cmd.addParameter(MessageConstants.PARAM_DOMAIN,
								MessageConstants.INTERNAL_DOMAIN);
						cmd.addParameter(MessageConstants.PARAM_ACTION,
								MessageConstants.DIRECTION_CHANGE);

						if (pitch < MAX_PITCH)
							cmd.addParameter(MessageConstants.PARAM_ARGS,
									MessageConstants.DIRECTION_LEFT);
						else
							cmd.addParameter(MessageConstants.PARAM_ARGS,
									MessageConstants.DIRECTION_RIGHT);

						messengerMemory.writeMemory(cmd);
						messenger.act();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
			checkPitch= false;
		}
		
//	System.out.println("REAS time = " + (System.currentTimeMillis() - start));
}
	
	
	@Override
	public void process() {
		
		checkPitch= true;
	}

}
