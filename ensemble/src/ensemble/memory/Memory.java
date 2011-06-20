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

package ensemble.memory;

import ensemble.Constants;
import ensemble.LifeCycle;
import ensemble.EnsembleAgent;
import ensemble.Parameters;
import ensemble.clock.TimeUnit;
import ensemble.clock.VirtualClockHelper;
import jade.util.Logger;

// usar TimeFormat(); para os instantes e amostras

// TODO se ele pedir em tempo, devolver o vetor interpolado, se pedir em amostras, devolver os valores do buffer
// TODO Reconversão entre taxas de amostragem (raciocionio trabalhando em outra taxa que o sensor)
// TODO Se não está acontecendo nada no ambiente, não enviar os chunks (ou então os chunks com zero são descartadas)
// TODO Raciocinio cutucar o outro (oscilidores/mixer)
// Reunião - terça
// TODO Não funciona para Batch (considerar turnos também) -> Já tem o tipo em TimeUnit
public abstract class Memory implements LifeCycle {

	// Log
//	public static Logger logger = Logger.getMyLogger(EnsembleAgent.class.getName());
	
	protected  Parameters 	parameters;

	protected VirtualClockHelper clock;

	protected EnsembleAgent 	myAgent;
	protected String 	name;
	protected double	past;
	protected double	future;

	@Override
	public Parameters getParameters() {
		return parameters;
	}
	
	@Override
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	public void setAgent(EnsembleAgent myAgent) {
		this.myAgent = myAgent;
	}
	
	@Override
	public boolean start() {

		this.name = parameters.get(Constants.PARAM_MEMORY_NAME);
		this.past = Double.valueOf(parameters.get(Constants.PARAM_MEMORY_PAST, "1.0"));
		this.future = Double.valueOf(parameters.get(Constants.PARAM_MEMORY_FUTURE, "1.0"));
		
		clock = myAgent.getClock();

		if (!init()) {
			return false;
		}
		
//		System.out.println(myAgent.getAgentName() + " New memory " + this.toString() + " called " + name + " - " + past + " - " + future);
		
		return true;
	}
	
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return false;
	}
	
	@Override
	public boolean stop() {

		if (!finit()) {
			return false;
		}

		return true;
		
	}
	
	//--------------------------------------------------------------------------------
	// User implemented method
	//--------------------------------------------------------------------------------

	@Override
	public boolean configure() {
		return true;
	}
	
	@Override
	public boolean init() {
		return true;
	}
	
	@Override
	public boolean finit() {
		return true;
	}

	/**
	 * Retorna o nome do componente ao qual está memória está associada
	 * @return
	 */
	public final String getName() {
		return name;
	}
	
	public final double getPast() {
		return past;
	}
	
	public final double getFuture() {
		return future;
	}
	
	/**
	 * Retorna o instante mais antigo em que existe informação na memória
	 * @return
	 */
	public abstract double getFirstInstant();

	/**
	 * Retorna o último instante em que existe informação na memória
	 * @return
	 */
	public abstract double getLastInstant();
	
	/**
	 * Obtém a memória em um instante do tempo
	 * @param instant
	 * @return
	 */
	public abstract Object readMemory(double instant, TimeUnit unit);
	
	/**
	 * Obtém a memória em um dado intervalo de tempo
	 * @param initialInstant
	 * @param duration
	 * @return
	 */
	public abstract Object readMemory(double instant, double duration, TimeUnit unit);
	
	/**
	 * Limpa a memória
	 */
	public abstract void resetMemory();
	
	/**
	 * Escreve na memória a partir de um instante absoluto
	 * @param object
	 * @param instant
	 */
	public abstract void writeMemory(Object object, double instant, double duration, TimeUnit unit) throws MemoryException;

	/**
	 *
	 * @param object
	 * @throws MemoryException
	 */
	public abstract void writeMemory(Object object, double instant, TimeUnit unit) throws MemoryException;

	/**
	 *
	 * @param object
	 * @throws MemoryException
	 */
	public abstract void writeMemory(Object object) throws MemoryException;

}
