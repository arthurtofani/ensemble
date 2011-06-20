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

package ensemble;

public interface LifeCycle {

	/**
	 * User-implemented method that configures the component, setting up user parameters and essential properties
	 */
	public boolean configure();
	
	/**
	 * Framework-implemented initialization method
	 * @return 
	 */
	public boolean start();
	
	/**
	 * User-implemented initialization method, called by start()
	 */
	public boolean init();
	
	/**
	 * User-implement method called when a parameter has been updated
	 */
	public boolean parameterUpdate(String name, String newValue);
	
	/**
	 * User-implemented finalization method, called by stop()
	 * @return 
	 */
	public boolean finit();
	
	/** Framework-implemented finalization method
	 * @return 
	 */
	public boolean stop();
	
	/**
	 * Framework-implemented method that sets system and user parameters
	 * @param parameters a Parameters object with all user and system Parameters for this object
	 */
	public void setParameters(Parameters parameters);
	
	/**
	 * Returns all configured parameters.
	 * @return a Parameters object with system and user parameters 
	 */
	public Parameters getParameters();
	
}
