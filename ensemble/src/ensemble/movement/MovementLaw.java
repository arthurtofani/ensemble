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

import ensemble.world.Law;
import ensemble.world.LawState;
import ensemble.world.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class MovementLaw.
 */
public class MovementLaw extends Law {

	// Physical constants of the World
	// TODO Pode estar no mundo também
	/** The gravity. */
	private double gravity = 10.0;
	
	/** The friction_coefficient. */
	private double friction_coefficient = 0.0;
	
	// temporary variable
	/** The friction acceleration. */
	private Vector frictionAcceleration;

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		setType(MovementConstants.EVT_TYPE_MOVEMENT);
		if (parameters.containsKey("gravity")) {
			this.gravity = Double.valueOf(parameters.get("gravity"));
		}
		if (parameters.containsKey("friction_coefficient")) {
			this.friction_coefficient = Double.valueOf(parameters.get("friction_coefficient"));
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
		frictionAcceleration = new Vector(world.dimensions);
		warmup();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}
	
	/**
	 * Warmup.
	 */
	public void warmup() {
		
		MovementState prevState = new MovementState(world.dimensions);
		prevState.instant = 0;
		prevState.velocity.setValue(0, 10);
		prevState.angularVelocity.setValue(0, 1);

		MovementState newState = new MovementState(world.dimensions);

		for (int j = 0; j < 20000; j++) {
			changeState(prevState, 10.0, newState);
		}
		
		prevState.velocity.zero();
		prevState.velocity.setValue(0, 10);
		prevState.acceleration.zero();
		prevState.acceleration.setValue(0, 10);

		for (int j = 0; j < 20000; j++) {
			changeState(prevState, 10.0, newState);
		}
		
		prevState.velocity.zero();
		prevState.acceleration.zero();
		prevState.acceleration.setValue(0, 50);
	
		for (int j = 0; j < 20000; j++) {
			changeState(prevState, 10.0, newState);
		}

	}
	
	/* (non-Javadoc)
	 * @see ensemble.world.Law#changeState(ensemble.world.LawState, double, ensemble.world.LawState)
	 */
	@Override
	public void changeState(final LawState prevState, double instant, LawState newState) {
		
		// If not the right kind of State, returns
		if (!(prevState instanceof MovementState) && !(newState instanceof MovementState)) {
			System.err.println("[MovementLaw] Not the right kind of state!");
			return;
		}
		
		MovementState movPrevState = (MovementState)prevState;
		MovementState movNewState = (MovementState)newState;

		// Copies the prevState into newState
		movPrevState.copy(movNewState);
		movNewState.instant = instant;

		// Does any necessary calculation
		double interval = instant - movPrevState.instant;
//		System.out.printf("interval = %f\n", interval);
		if (interval > 0) {
			
			double acc = movPrevState.acceleration.magnitude;
			double vel = movPrevState.velocity.magnitude;
			// Se o corpo não está em movimento e não está sendo acelerado
			if (acc == 0 && vel == 0) {
				return;
			}
			// Se o corpo está em movimento, mas sem acelerar
			else if (acc == 0 && vel > 0) {
				// Calcular a aceleração devido ao atrito
				movNewState.velocity.copy(frictionAcceleration);
				frictionAcceleration.normalizeVectorInverse();
				frictionAcceleration.product(gravity * friction_coefficient);

				// Se velocidade chegar em zero durante esse intervalo, calcular 
				double t_vel_zero = movPrevState.instant + (movPrevState.velocity.magnitude / frictionAcceleration.magnitude);
	    		if (t_vel_zero < instant) {
	    			double new_interval = t_vel_zero - movPrevState.instant;
	    			// S = S0 + V0t + 1/2atˆ2
	    			movNewState.position.add(movPrevState.velocity, new_interval);
	    			movNewState.position.add(frictionAcceleration, (0.5 * new_interval * new_interval));
					movNewState.velocity.update(0, 0, 0);
	    		} else {
	    			// S = S0 + V0t + 1/2atˆ2
	    			movNewState.position.add(movPrevState.velocity, interval);
	    			movNewState.position.add(frictionAcceleration, (0.5 * interval * interval));
	    			// V = V0 + at
	    			movNewState.velocity.add(frictionAcceleration, interval);
	    		}
			}
			// Se o corpo foi acelerado mas ainda não está em movimento
			else if (acc > 0 && vel == 0) {
				// Verificar que tem aceleração suficiente para começar a andar
				movPrevState.acceleration.copy(frictionAcceleration);
				frictionAcceleration.normalizeVectorInverse();
				frictionAcceleration.product(gravity * friction_coefficient);
				if (acc > frictionAcceleration.magnitude) {
					frictionAcceleration.add(movNewState.acceleration);
	    			// S = S0 + V0t + 1/2atˆ2
	    			movNewState.position.add(movPrevState.velocity, interval);
	    			movNewState.position.add(frictionAcceleration, (0.5 * interval * interval));
	    			// V = V0 + at
	    			movNewState.velocity.add(frictionAcceleration, interval);

				}
			}
			// Se o corpo está em movimento e está sendo acelerado
			else {
				movPrevState.acceleration.copy(frictionAcceleration);
				frictionAcceleration.normalizeVectorInverse();
				frictionAcceleration.product(gravity * friction_coefficient);
				frictionAcceleration.add(movNewState.acceleration);
    			// S = S0 + V0t + 1/2atˆ2
    			movNewState.position.add(movPrevState.velocity, interval);
    			movNewState.position.add(frictionAcceleration, (0.5 * interval * interval));
    			// V = V0 + at
    			movNewState.velocity.add(frictionAcceleration, interval);
			}
			
			// Se o loop estiver habilitado, modifica a posição
			if (world != null && world.form_loop) {
				for (int i = 0; i < movNewState.position.dimensions; i++) {
					double value = movNewState.position.getValue(i);
					if (Math.abs(value) > world.form_size_half) {
						value = value > 0 ? value - world.form_size : value + world.form_size;
					}
					movNewState.position.setValue(i, value);
				}
				movNewState.position.updateMagnitude();
			}
			
			// Atualizar a orientação
			if (movPrevState.angularVelocity.magnitude != 0) {
				movNewState.orientation.add(movPrevState.angularVelocity, interval);
				// TODO E se tiver duas dimensões?!
				for (int i = 0; i < movNewState.orientation.dimensions; i++) {
					movNewState.orientation.setValue(i, movNewState.orientation.getValue(i) % (2*Math.PI));
				}
				movNewState.orientation.updateMagnitude();
			}
			
		}
		
		return;
		
	}
	
//	public static void main(String[] args) {
//	
//		MovementLaw law = new MovementLaw();
//		law.configure();
//		
//		MovementState prevState = new MovementState(3);
//		prevState.instant = 0;
//		prevState.velocity.update(10,0,0);
//
//		MovementState newState = new MovementState(3);
//
//		for (int j = 0; j < 10; j++) {
//			long start_time = System.nanoTime();
//			// Simulando um chunk_size de 250 ms
//			for (int i = 0; i < 44000; i++) {
//				law.changeState(prevState, 10.0, newState);
//			}
//			long elapsed_time = System.nanoTime() - start_time;
//			System.out.println("elapsed time = " + elapsed_time);
////			System.out.println("time_1 = " + law.time_1);
////			System.out.println("time_2 = " + law.time_2);
////			System.out.println("time_3 = " + law.time_3);
////			System.out.println("time_4 = " + law.time_4);
////			System.out.println("time_5 = " + law.time_5);
//			elapsed_time = 0;
//			law.time_1 = 0;
//			law.time_2 = 0;
//			law.time_3 = 0;
//			law.time_4 = 0;
//			law.time_5 = 0;
//		}
//		
////		System.out.println("ins = " + newState.instant);
////		System.out.println("pos = " + newState.position);
////		System.out.println("vel = " + newState.velocity);
////		System.out.println("acc = " + newState.acceleration);
////		System.out.println("angVel = " + newState.angularVelocity);
////		System.out.println("ori = " + newState.orientation);
//		
//	}

}
