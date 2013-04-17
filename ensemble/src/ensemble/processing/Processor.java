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

package ensemble.processing;

import ensemble.LifeCycle;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class Processor.
 */
public abstract class Processor implements LifeCycle {

	/** The parameters. */
	protected Parameters parameters;
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#setParameters(ensemble.Parameters)
	 */
	@Override
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#getParameters()
	 */
	@Override
	public Parameters getParameters() {
		return parameters;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#start()
	 */
	@Override
	public boolean start() {
		
		// Check for parameters
		// TODO Deveriamos ter uma lista dos argumentos necessário (e opcionais) para cada process, e checar a existência deles aqui!
		if (parameters == null) {
			System.err.println("ERROR: No parameters was passed to the Process class!");
			return false;
		}
		
		// Call the user initialization method
		init();
		
		// Everything is ok!
		return true;
		
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public boolean stop() {
		
		finit();
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	/**
	 * Process method.
	 *
	 * @param parameters the parameters
	 * @param in the in
	 * @return the object
	 */
	public abstract Object process(Parameters parameters, Object in);


}
