/******************************************************************************

Copyright 2011 

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

package ensemble.audio.file;

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
import ensemble.movement.MovementConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class AudioFileInputToMemoryReasoning.
 */
public class AudioFileInputToMemoryReasoning  extends Reasoning {

	/** The mouth. */
	Actuator 	mouth;
	
	/** The mouth memory. */
	Memory 		mouthMemory;
//	Sensor 		ear;
//	Memory 		earMemory;
			
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
		
		// Abre o arquivo de áudio para leitura
		String filename = getAgent().getKB().readFact("filename");
		try {
			in = new AudioInputFile(filename, false);
		} catch (Exception e) {
//			getAgent().logger.severe("[" + getComponentName() + "] " + "Error in opening the file " + filename);
			System.out.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Error in opening the file " + filename);
			return false;
		}
		
		// Verifica se existe o argumento de ganho
		if (getParameters().containsKey("gain")) {
			gain = Double.valueOf(getParameter("gain"));
		}
		
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
			mouthMemory = getAgent().getKB().getMemory(mouth.getComponentName() + Constants.SUF_AUXILIAR_MEMORY);
			
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
		}

		// Checar se é um atuador de som e adicionar na lista
//		if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
//			ear = (Sensor)evtHdl;
//			ear.registerListener(this);
//			earMemory = getAgent().getKB().getMemory(ear.getComponentName());
//		}

	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	@Override
	public void needAction(Actuator sourceActuator, double instant, double duration) {

//		System.out.println(System.currentTimeMillis() + " " + getAgent().getAgentName() + " Entrei no needAction() - instant " + instant);

		// Le o fragmento do arquivo e transformas em float
		chunk = in.readNextChunk(chunk_size);
		
		//notifica fim de arquivo
		if(in.hasEnded && getAgent().getKB().getParameter("playState")!="STOP"){
			getParameters().put("playState", "STOP");
			getAgent().getKB().setParameters(getParameters());
			
		}
		
		// Faz qualquer alteração necessária no buffer (aplica o ganho)
		for (int i = 0; i < chunk.length; i++) {
			chunk[i] = chunk[i] * gain;
		}
		
		// Escreve na Base de Conhecimento o evento a ser enviado
		// TODO Ao invés de escrever na KB, fazer diretamente no Atuador
		try {
			mouthMemory.writeMemory(chunk, instant, duration, TimeUnit.SECONDS);
			//System.out.println("Guardei na memória auxiliar um evento no instante " + instant + " de duração " + duration);
		} catch (MemoryException e1) {
		}
		//mouth.act();

//			System.out.println(System.currentTimeMillis() + " MusicalAgent: enviei chunk de tamanho " + chunk.length);
//		System.out.println(System.currentTimeMillis() + " " + getAgent().getAgentName() + " Sai do needAction() - " + num);

	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		// Reads ear's memory
//		System.out.println("Entrei no newSense()");
//		double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);

		// Analisa o evento e modifica as notas escutadas
		// notes = ...
	}

	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	@Override
	public void process() {
	}
	
	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		//System.out.println("FILE = " +cmd.getCommand());
		if (cmd.getCommand().equals(AudioConstants.CMD_PLAY)) {
			
			// Abre o arquivo de áudio para leitura
			String filename = cmd.getParameter("filename");
			System.out.println("FILE = " +filename);
			try {
				in = new AudioInputFile(filename, false);
				
			} catch (Exception e) {
//				getAgent().logger.severe("[" + getComponentName() + "] " + "Error in opening the file " + filename);
				System.out.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Error in opening the file " + filename);
			}
		}

		if (cmd.getCommand().equals(AudioConstants.CMD_STOP)) {
			
			
		}
	}

}
