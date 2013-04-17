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

package ensemble.world;

import ensemble.Constants;
import ensemble.LifeCycle;
import ensemble.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class Law.
 */
public abstract class Law implements LifeCycle {
	
	/** The world. */
	protected World 		world;
	
	/** The type. */
	protected String 		type;
	
	/** The parameters. */
	protected Parameters 	parameters;
	
	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
	}

	/**
	 * Gets the world.
	 *
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

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
		return this.parameters;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#start()
	 */
	@Override
	public boolean start() {
		if (world == null) {
			return false;
		}
		if (!init()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public boolean stop() {
		if (!finit()) {
			return false;
		}
		return true;
	}

    //--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

	/* (non-Javadoc)
     * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
     */
    @Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	/**
	 * Change state.
	 *
	 * @param oldState the old state
	 * @param instant the instant
	 * @param newState the new state
	 */
	public abstract void changeState(final LawState oldState, double instant, LawState newState);

}
