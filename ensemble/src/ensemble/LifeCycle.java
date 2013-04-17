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

// TODO: Auto-generated Javadoc
/**
 * The Interface LifeCycle.
 */
public interface LifeCycle {

	/**
	 * User-implemented method that configures the component, setting up user parameters and essential properties.
	 *
	 * @return true, if successful
	 */
	public boolean configure();
	
	/**
	 * Framework-implemented initialization method.
	 *
	 * @return true, if successful
	 */
	public boolean start();
	
	/**
	 * User-implemented initialization method, called by start().
	 *
	 * @return true, if successful
	 */
	public boolean init();
	
	/**
	 * User-implement method called when a parameter has been updated.
	 *
	 * @param name the name
	 * @param newValue the new value
	 * @return true, if successful
	 */
	public boolean parameterUpdate(String name, String newValue);
	
	/**
	 * User-implemented finalization method, called by stop().
	 *
	 * @return true, if successful
	 */
	public boolean finit();
	
	/**
	 * Framework-implemented finalization method.
	 *
	 * @return true, if successful
	 */
	public boolean stop();
	
	/**
	 * Framework-implemented method that sets system and user parameters.
	 *
	 * @param parameters a Parameters object with all user and system Parameters for this object
	 */
	public void setParameters(Parameters parameters);
	
	/**
	 * Returns all configured parameters.
	 * @return a Parameters object with system and user parameters 
	 */
	public Parameters getParameters();
	
}
