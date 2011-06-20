package ensemble.apps.pp;

import ensemble.Actuator;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.MusicalAgent;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.file.AudioInputFile;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;

public class PP_Reasoning extends Reasoning {

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
		
		// Abre o arquivo de áudio para leitura
		String filename = getAgent().getKB().readFact("filename");
		try {
			in = new AudioInputFile(filename, true);
		} catch (Exception e) {
//			getAgent().logger.severe("[" + getComponentName() + "] " + "Error in opening the file " + filename);
			return false;
		}
		
		// Verifica se existe o argumento de ganho
		if (getParameters().containsKey("gain")) {
			gain = Double.valueOf(getParameter("gain"));
		}
		
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

//		System.out.println(System.currentTimeMillis() + " " + getAgent().getAgentName() + " Entrei no needAction() - instant " + instant);

		// Le o fragmento do arquivo e transformas em float
		chunk = in.readNextChunk(chunk_size);

		// Faz qualquer alteração necessária no buffer (aplica o ganho)
		for (int i = 0; i < chunk.length; i++) {
			chunk[i] = chunk[i] * gain;
		}
		
		// Escreve na Base de Conhecimento o evento a ser enviado
		// TODO Ao invés de escrever na KB, fazer diretamente no Atuador
		try {
//			mouthMemory.writeMemoryRelative(chunk, 0, duration, TimeUnit.SECONDS);
			mouthMemory.writeMemory(chunk, instant, duration, TimeUnit.SECONDS);
//			System.out.println("Guardei na memória um evento no instante " + instant + " de duração " + duration);
		} catch (MemoryException e1) {
//			MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
		}

		
		
		mouth.act();


	}

	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		// Reads ear's memory
//		System.out.println("Entrei no newSense()");
		double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);

		// Analisa o evento e modifica as notas escutadas
		// notes = ...
	}

	@Override
	public void process() {
	}

}
