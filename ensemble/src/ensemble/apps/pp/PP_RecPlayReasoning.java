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

// TODO: Auto-generated Javadoc
/**
 * The Class PP_RecPlayReasoning.
 */
public class PP_RecPlayReasoning extends Reasoning {

	/** The mouth. */
	Actuator 	mouth;
	
	/** The mouth memory. */
	Memory 		mouthMemory;
	
	/** The ear. */
	Sensor 		ear;
	
	/** The ear memory. */
	Memory 		earMemory;
			
	// número de samples (frame) em um chunk
	/** The chunk_size. */
	int chunk_size;
	
	/** The current chunk. */
	private long currentChunk 	= 0;
	
	/** The initial time. */
	private long initialTime 	= System.currentTimeMillis();

	// Buffer do Agente
	/** The buffer. */
	private byte[] 	buffer;
	
	/** The chunk. */
	private double[] chunk;
	
	/** The backup. */
	private int		backup = 5;

	/** The gain. */
	private double 	gain = 1.0;
	
	// Desempenho
	/** The sent chunks. */
	private long 	sentChunks	= 0;
		
	// Arquivo de áudio
	/** The in. */
	AudioInputFile in;

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

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) {

		// Verifica se tem q executar algum comando
		

		
		


	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	@Override
	public void process() {
	}

}
