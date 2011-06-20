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

public abstract class Law implements LifeCycle {
	
	protected World 		world;
	protected String 		type;
	protected Parameters 	parameters;
	
	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Parameters getParameters() {
		return this.parameters;
	}

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

	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	public abstract void changeState(final LawState oldState, double instant, LawState newState);

}
