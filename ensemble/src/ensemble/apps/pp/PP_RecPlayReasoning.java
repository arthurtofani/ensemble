package ensemble.apps.pp;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.file.AudioInputFile;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;

public class PP_RecPlayReasoning extends Reasoning {

	Actuator 	mouth;
	Memory 		mouthMemory;
	Sensor 		ear;
	Memory 		earMemory;
			
	// número de samples (frame) em um chunk
	int chunk_size;
	
	private long currentChunk 	= 0;
	private long initialTime 	= System.currentTimeMillis();

	// Buffer do Agente
	private byte[] 	buffer;
	private double[] chunk;
	private int		backup = 5;

	private double 	gain = 1.0;
	
	// Desempenho
	private long 	sentChunks	= 0;
		
	// Arquivo de áudio
	AudioInputFile in;

	@Override
	public boolean init() {
		
		
		
		return true;
		
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		// Checar se é um atuador de som e adicionar na lista
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			mouthMemory = getAgent().getKB().getMemory(mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
		}

		// Checar se é um atuador de som e adicionar na lista
		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			ear = (Sensor)evtHdl;
			ear.registerListener(this);
			earMemory = getAgent().getKB().getMemory(ear.getComponentName());
		}

	}

	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) {

		// Verifica se tem q executar algum comando
		

		
		


	}

	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		
	}

	@Override
	public void process() {
	}

}
