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

package ensemble.audio.portaudio;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.MusicalAgent;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.tools.AudioTools;

import jade.util.Logger;
import jportaudio.PaCallback;
import jportaudio.PaDeviceInfo;
import jportaudio.PaStreamInfo;
import jportaudio.PaStreamParameters;
import jportaudio.portaudio;


public class PAInputReasoning extends Reasoning {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

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
	HashMap<String,Memory> 	mouthMemories = new HashMap<String, Memory>(2);

	@Override
	public boolean init() {
		
		// It must be in the format "sensor:device,channel;sensor2:device,channel..."
		String[] str = getParameter("mapping", "").split(";");
		
		if (str.length == 0) {
			System.err.println("[" + getComponentName() + "] No channels configured... Aborting PA");
			return false;
		}
		
		System.out.println(getParameter("mapping", ""));
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
		
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			Actuator mouth = (Actuator)evtHdl;
			String actuatorName = evtHdl.getComponentName();
			mouth.registerListener(this);
			period = Double.valueOf(mouth.getParameter("PERIOD"))/1000.0;
			mouthMemories.put(actuatorName, getAgent().getKB().getMemory(mouth.getComponentName()));
			// Creates a portaudio stream
			long stream = 0;
			if (devices.containsKey(actuatorName)) {
//				System.out.println("[PORTAUDIO] Opening stream...");
				// Gets DeviceInfo
				int device = devices.get(actuatorName);
				PaDeviceInfo info = pa.Pa_GetDeviceInfo(device);
				if (info == null) {
					System.err.println("[PORTAUDIO] Audio device '" + device + "' not available...");
					return;
				}
				double sr = info.getDefaultSampleRate();
				int channelCount = info.getMaxInputChannels();
				if (channelCount == 0) {
					System.err.println("[PORTAUDIO] There are no input channels in audio device '" + device + "'");
					return;
				}
				// Sets Parameters
				PaStreamParameters inputParameters = new PaStreamParameters();
				inputParameters.setDevice(device);
				inputParameters.setChannelCount(channelCount);
				inputParameters.setHostApiSpecificStreamInfo(null);
				inputParameters.setSampleFormat(portaudio.SIGNED_INTEGER_16);
				inputParameters.setSuggestedLatency(info.getDefaultLowInputLatency());
				// Opens the stream
				stream = pa.Pa_OpenStream(inputParameters, null, sr, 256, 0, new Callback());
				if (stream == 0) {
					System.err.println("[PORTAUDIO] Stream not available for audio device '" + device + "'");
					return;
				}
				// Stores Stream parameters
				StreamInfo streamInfo = new StreamInfo();
				streamInfo.stream = stream;
				streamInfo.evtHdlName = actuatorName;
				streamInfo.device = device;
				streamInfo.channel = channels.get(actuatorName);
				streamInfo.channelCount = channelCount;
				streamInfo.latency = pa.Pa_GetStreamInfo(stream).getInputLatency();
//				System.out.println("[PORTAUDIO] Input latency = " + streamInfo.latency);
				streamInfos.put(stream, streamInfo);
				streams_sensors.put(stream, actuatorName);
				sensors_streams.put(actuatorName, stream);
				pa.Pa_StartStream(stream);
			}
		}
		
	}

	@Override
	protected void eventHandlerDeregistered(EventHandler evtHdl)
			throws Exception {
		String sensorName = evtHdl.getComponentName();
		if (sensors_streams.containsKey(sensorName)) {
			long stream = sensors_streams.get(sensorName);
//			System.out.println("[PORTAUDIO] Stoping stream " + stream);
			pa.Pa_StopStream(stream);
//			System.out.println("[PORTAUDIO] Closing stream " + stream);
			pa.Pa_CloseStream(stream);
			streams_sensors.remove(stream);
			sensors_streams.remove(sensorName);
			streamInfos.remove(sensorName);
		}
	}

	@Override
	public void needAction(Actuator sourceActuator, double instant,
			double duration) throws Exception {
		// Teoricamente, já vai estar escrito na memória o que deve ser enviado,
		// pois foi preenchido pelo callback do PA
		sourceActuator.act();
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
			
			// If it's the first call, sets the startTime based in the ensemble's clock
			if (info.firstCall) {
				info.instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) - info.latency;
				info.firstCall = false;
			}

//			System.out.printf(System.currentTimeMillis() + " callback = %f %f %f\n", instant, outputBufferDacTime, portaudio.Pa_GetStreamTime(stream));
			
			int ptr = 0;
			byte[] buffer = new byte[input.capacity()/info.channelCount];
			while (input.remaining() > 0) {
				for (int i = 0; i < info.channelCount; i++) {
					// Se foi o canal escolhido, guarda o sample
					if (i == info.channel) {
						buffer[ptr++] = input.get();
						buffer[ptr++] = input.get();
					}
					// Descarta os outros canais
					else {
						input.get();
						input.get();
					}
				}
			}

			double[] d_buf = AudioTools.convertByteDouble(buffer, 0, buffer.length);
			double duration = d_buf.length * step;
			try {
				Memory mouthMemory = mouthMemories.get(info.evtHdlName);
				double instant = info.instant + (2 * period) + info.latency;
				mouthMemory.writeMemory(d_buf, instant, duration, TimeUnit.SECONDS);
//				System.out.println(now + " " + getAgent().getClock().getCurrentTime() + " Escrevi do instante " + (instant+period) + " até " + (instant+period+duration));
			} catch (MemoryException e) {
				e.printStackTrace();
			}

			info.instant = info.instant + duration;
			return paContinue;
			
		}
		
		@Override
		public void hook(long stream) {
		}
		
	};

	
}
