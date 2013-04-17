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

package ensemble.movement;

import ensemble.world.LawState;
import ensemble.world.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class MovementState.
 */
public class MovementState extends LawState {
	
	/** The position. */
	public Vector position;
	
	/** The velocity. */
	public Vector velocity;
	
	/** The acceleration. */
	public Vector acceleration;
	
	/** The orientation. */
	public Vector orientation;
	
	/** The angular velocity. */
	public Vector angularVelocity;
	
	/**
	 * Instantiates a new movement state.
	 *
	 * @param dimensions the dimensions
	 */
	public MovementState(int dimensions) {
		this.position = new Vector(dimensions);
		this.velocity = new Vector(dimensions);
		this.acceleration = new Vector(dimensions);
		this.orientation = new Vector(dimensions);
		this.angularVelocity = new Vector(dimensions);
	}
	
	/**
	 * Copy.
	 *
	 * @param newState the new state
	 */
	public void copy(MovementState newState) {
		
		position.copy(newState.position);
		velocity.copy(newState.velocity);
		acceleration.copy(newState.acceleration);
		orientation.copy(newState.orientation);
		angularVelocity.copy(newState.angularVelocity);
		
	}

}
