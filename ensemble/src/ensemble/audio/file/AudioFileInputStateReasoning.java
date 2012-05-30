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

import java.util.Date;
import java.util.Random;

import FIPA.DateTime;

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

public class AudioFileInputStateReasoning extends Reasoning {

	Actuator mouth;
	Memory mouthMemory;
	// Sensor ear;
	// Memory earMemory;

	// número de samples (frame) em um chunk
	int chunk_size;

	private long currentChunk = 0;
	private long initialTime = System.currentTimeMillis();

	// Buffer do Agente
	private byte[] buffer;
	private double[] chunk;
	private int backup = 5;

	private double gain = 1.0;

	// Desempenho
	private long sentChunks = 0;
	
	private boolean active = true;
	long rest;
	// Arquivo de áudio
	AudioInputFile in;

	@Override
	public boolean init() {

		// Abre o arquivo de áudio para leitura
		// String filename = getAgent().getKB().readFact("filename");
		/*
		 * try { in = new AudioInputFile(filename, false); } catch (Exception e)
		 * { // getAgent().logger.severe("[" + getComponentName() + "] " + //
		 * "Error in opening the file " + filename); System.out.println("[" +
		 * getAgent().getAgentName() + ":" + getComponentName() + "] " +
		 * "Error in opening the file " + filename); return false; }
		 */
		// Verifica se existe o argumento de ganho
		if (getParameters().containsKey("gain")) {
			gain = Double.valueOf(getParameter("gain"));
		}

		return true;

	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {

		// Checar se é um atuador de som e adicionar na lista
		if (evtHdl instanceof Actuator
				&& evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			mouth = (Actuator) evtHdl;
			mouth.registerListener(this);
			mouthMemory = getAgent().getKB()
					.getMemory(mouth.getComponentName());

			chunk_size = Integer.parseInt(mouth.getParameter(
					Constants.PARAM_CHUNK_SIZE, "0"));
		}

		// Checar se é um atuador de som e adicionar na lista
		// if (evtHdl instanceof Sensor &&
		// evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
		// ear = (Sensor)evtHdl;
		// ear.registerListener(this);
		// earMemory = getAgent().getKB().getMemory(ear.getComponentName());
		// }

	}

	@Override
	public void needAction(Actuator sourceActuator, double instant,
			double duration) {

		boolean hasFile = in != null;
		boolean changed = getParameter("changed")== "true";
		Random rnd = new Random();
		
		// System.out.println(System.currentTimeMillis() + " " +
		// getAgent().getAgentName() + " Entrei no needAction() - instant " +
		// instant);
		if (hasFile) {
			// Le o fragmento do arquivo e transformas em float
			chunk = in.readNextChunk(chunk_size);
			// System.out.println("Ended =" + in.hasEnded + " hasfile=" +
			// hasFile);
		} else
			chunk = null;
		// System.out.println("files: " + getParameter("files") );

		// notifica fim de arquivo
		if (!hasFile || in.hasEnded || changed) {
			
			if(rest ==0){
				//Calcula o descanso
				rest = (new Date()).getTime() + rnd.nextInt(2500);
				
			}
			
			// && getAgent().getKB().getParameter("playState") != "STOP")) {
			if(changed)getParameters().put("changed", "false");
			
			double d = 0;

			
			String str = getAgent().getKB().getParameter("files", null);

			if (str != null) {
				String[] fls = str.split(";");

				int next = Math.max(rnd.nextInt(fls.length) - 1, 0);
				
				
				if (new Date().getTime() >= rest)
				{
					rest=0;
					try {
						in = new AudioInputFile(fls[next], false);

					} catch (Exception e) {
						// getAgent().logger.severe("[" + getComponentName() +
						// "] "
						// + "Error in opening the file " + filename);
						System.out.println("[" + getAgent().getAgentName()
								+ ":" + getComponentName() + "] "
								+ "Error in opening the file " + fls[next]);
					}
				}
				/*
				 * else { getParameters().put("playState", "STOP");
				 * getAgent().getKB().setParameters(getParameters()); }
				 */
			} else {
				getParameters().put("playState", "STOP");
				getAgent().getKB().setParameters(getParameters());
			}
		}

		if (chunk != null) {
			// Faz qualquer alteração necessária no buffer (aplica o ganho)
			for (int i = 0; i < chunk.length; i++) {
				chunk[i] = chunk[i] * gain;
			}

			// Escreve na Base de Conhecimento o evento a ser enviado
			// TODO Ao invés de escrever na KB, fazer diretamente no Atuador
			try {
				mouthMemory.writeMemory(chunk, instant, duration,
						TimeUnit.SECONDS);
				// System.out.println("Guardei na memória auxiliar um evento no instante "
				// + instant + " de duração " + duration);
			} catch (MemoryException e1) {
			}
			mouth.act();
		} else {

			String files = getParameter("files", "");
			if (files.length() > 0) {
				String[] repertoire = files.split(";");
				if (repertoire.length > 0) {
					int next = Math.max(rnd.nextInt(repertoire.length) - 1, 0);
					String filename = repertoire[next];

					try {
						in = new AudioInputFile(filename, false);

					} catch (Exception e) {
						// getAgent().logger.severe("[" + getComponentName() +
						// "] " +
						// "Error in opening the file " + filename);
						System.out.println("[" + getAgent().getAgentName()
								+ ":" + getComponentName() + "] "
								+ "Error in opening the file " + filename);
					}
				}
			}
		}
		// System.out.println(System.currentTimeMillis() +
		// " MusicalAgent: enviei chunk de tamanho " + chunk.length);
		// System.out.println(System.currentTimeMillis() + " " +
		// getAgent().getAgentName() + " Sai do needAction() - " + num);

	}

	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		// Reads ear's memory
		// System.out.println("Entrei no newSense()");
		// double[] buf = (double[])earMemory.readMemory(instant, duration,
		// TimeUnit.SECONDS);

		// Analisa o evento e modifica as notas escutadas
		// notes = ...
	}

	@Override
	public void process() {
	}

	@Override
	public void processCommand(Command cmd) {
		System.out.println("FILE = " + cmd.getCommand());
		if (cmd.getCommand().equals(AudioConstants.CMD_PLAY)) {

			// Abre o arquivo de áudio para leitura
			// String filename = cmd.getParameter("filename");
			String files = cmd.getParameter("files");

			if (cmd.getParameter("files") != getParameter("files")) {
				getParameters().put("files", files);
				getParameters().put("changed", "true");
			}
			// System.out.println("FILE = " + filename);
			/*
			 * try { in = new AudioInputFile(filename, false);
			 * 
			 * } catch (Exception e) { // getAgent().logger.severe("[" +
			 * getComponentName() + "] " + // "Error in opening the file " +
			 * filename); System.out.println("[" + getAgent().getAgentName() +
			 * ":" + getComponentName() + "] " + "Error in opening the file " +
			 * filename); }
			 */}

		if (cmd.getCommand().equals(AudioConstants.CMD_STOP)) {
			
		}
	}

}
