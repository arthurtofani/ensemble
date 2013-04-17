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

package ensemble.audio;

import ensemble.world.Law;
import ensemble.world.LawState;

// TODO: Auto-generated Javadoc
/**
 * The Class AudioLaw.
 */
public class AudioLaw extends Law {

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see ensemble.world.Law#changeState(ensemble.world.LawState, double, ensemble.world.LawState)
	 */
	@Override
	public void changeState(LawState oldState, double instant, LawState newState) {
				
	}

}
