package mms.audio.portaudio;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import jade.util.Logger;


import mms.Constants;
import mms.EventHandler;
import mms.MusicalAgent;
import mms.Reasoning;
import mms.Sensor;
import mms.clock.TimeUnit;
import mms.memory.Memory;
import mms.tools.AudioTools;
import mmsportaudio.PaCallback;
import mmsportaudio.PaDeviceInfo;
import mmsportaudio.PaStreamInfo;
import mmsportaudio.PaStreamParameters;
import mmsportaudio.portaudio;

public class PAOutputReasoning extends Reasoning {

	// Log
	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

	// PortAudio
	portaudio 					pa;
	HashMap<Long, StreamInfo> 	streamInfos = new HashMap<Long, StreamInfo>(2);
	HashMap<Long, String> 		streams_sensors = new HashMap<Long, String>(2);
	HashMap<String, Long> 		sensors_streams = new HashMap<String, Long>(2);
	double 						callbackStartTime, period;
	double 						step = 1/44100.0;
	
	// Parameters
	HashMap<String,Integer> devices = new HashMap<String, Integer>();
	HashMap<String,Integer> channels = new HashMap<String, Integer>();
	
	// Sensor
	HashMap<String,Memory> 	earMemories = new HashMap<String, Memory>(2);

	@Override
	public boolean init() {
		
		// It must be in the format "sensor:device,channel;sensor2:device,channel..."
		String[] str = getParameter("mapping", "").split(";");
		
		if (str.length == 0) {
			System.err.println("[" + getComponentName() + "] No channels configured... Aborting PA");
			return false;
		}
		
		for (int i = 0; i < str.length; i++) {
			String[] str2 = str[i].split(":");
			String[] str3 = str2[1].split(",");
			devices.put(str2[0], Integer.valueOf(str3[0]));
			channels.put(str2[0], Integer.valueOf(str3[1]));
		}
		
		// Initializes PortAudio
		pa = portaudio.getInstance();

		return true;
		
	}

	@Override
	public boolean finit() {

		portaudio.deleteInstance();
		
		return true;
		
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(Constants.EVT_AUDIO)) {
			Sensor ear = (Sensor)evtHdl;
			String sensorName = evtHdl.getComponentName();
			ear.registerListener(this);
			period = Double.valueOf(ear.getParameter("PERIOD"))/1000.0;
			earMemories.put(sensorName, getAgent().getKB().getMemory(ear.getComponentName()));
			// Creates a portaudio stream
			long stream = 0;
			if (devices.containsKey(sensorName)) {
//				System.out.println("[PORTAUDIO] Opening stream...");
				// Gets DeviceInfo
				int device = devices.get(sensorName);
				PaDeviceInfo info = pa.Pa_GetDeviceInfo(device);
				if (info == null) {
					System.err.println("[PORTAUDIO] Audio device '" + device + "' not available...");
					return;
				}
				double sr = info.getDefaultSampleRate();
				int channelCount = info.getMaxOutputChannels();
				if (channelCount == 0) {
					System.err.println("[PORTAUDIO] There are no output channels in audio device '" + device + "'");
					return;
				}
				// Sets Parameters
				PaStreamParameters outputParameters = new PaStreamParameters();
				outputParameters.setDevice(device);
				outputParameters.setChannelCount(channelCount);
				outputParameters.setHostApiSpecificStreamInfo(null);
				outputParameters.setSampleFormat(portaudio.SIGNED_INTEGER_16);
				outputParameters.setSuggestedLatency(info.getDefaultLowOutputLatency());
				// Opens the stream
				stream = pa.Pa_OpenStream(null, outputParameters, sr, 256, 0, new Callback());
				if (stream == 0) {
					System.err.println("[PORTAUDIO] Stream not available for audio device '" + device + "'");
					return;
				}
				// Stores Stream parameters
				StreamInfo streamInfo = new StreamInfo();
				streamInfo.stream = stream;
				streamInfo.evtHdlName = sensorName;
				streamInfo.device = device;
				streamInfo.channel = channels.get(sensorName);
				streamInfo.channelCount = channelCount;
				streamInfo.latency = pa.Pa_GetStreamInfo(stream).getOutputLatency();
//				System.out.println("[PORTAUDIO] Output latency = " + streamInfo.latency);
				streamInfos.put(stream, streamInfo);
				streams_sensors.put(stream, sensorName);
				sensors_streams.put(sensorName, stream);
				pa.Pa_StartStream(stream);
			}
		}
		
	}

	@Override
	protected void eventHandlerDeregistered(EventHandler evtHdl) throws Exception {
		String sensorName = evtHdl.getComponentName();
		if (sensors_streams.containsKey(sensorName)) {
			long stream = sensors_streams.get(sensorName);
//			System.out.println("[PORTAUDIO] Stoping stream " + stream);
			pa.Pa_StopStream(stream);
//			System.out.println("[PORTAUDIO]Closing stream " + stream);
			pa.Pa_CloseStream(stream);
			streams_sensors.remove(stream);
			sensors_streams.remove(sensorName);
			streamInfos.remove(sensorName);
		}
	}
	
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

//		System.out.println(System.currentTimeMillis() + " Recebi evento de " + instant + " até " + (instant+duration));

	}

	class StreamInfo {
		long 	stream;
		String 	evtHdlName;
		int 	device;
		int 	channel;
		int 	channelCount;
		double 	latency;
		boolean firstCall = true;
		double 	instant = 0.0;
	}
	
	class Callback extends PaCallback {
		
		@Override
		public int callback(long stream, ByteBuffer input, ByteBuffer output,
				long frameCount, double inputBufferAdcTime,
				double currentTime, double outputBufferDacTime) {

			StreamInfo info = streamInfos.get(stream);
			
			// If it's the first call, sets the startTime based in the mms's clock
			if (info.firstCall) {
				info.instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) - info.latency;
				info.firstCall = false;
			}

//			System.out.printf(System.currentTimeMillis() + " callback = %f %f %f\n", info.instant, outputBufferDacTime, portaudio.Pa_GetStreamTime(stream));
			
			double duration = (double)(frameCount) * step;
//			System.out.println("vou ler de instant = " + instant + " até " + (instant+duration));
			Memory earMemory = earMemories.get(info.evtHdlName);
			double[] buf = (double[])earMemory.readMemory(info.instant, duration, TimeUnit.SECONDS);
			byte[] buffer = AudioTools.convertDoubleByte(buf, 0, buf.length);
			int ptr = 0;
			while (output.remaining() > 0) {
				for (int i = 0; i < info.channelCount; i++) {
					// If it is the chosen channel
					if (i == info.channel) {
						output.put(buffer[ptr++]);
						output.put(buffer[ptr++]);
					}
					// Else, silence
					else {
						output.put((byte)(0 & 0xFF));
						output.put((byte)((0 >> 8) & 0xFF));
					}
				}
			}
			info.instant = info.instant + duration;
			return paContinue;
			
		}
		
		@Override
		public void hook(long stream) {
		}
		
	};

	
}
