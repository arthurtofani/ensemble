/******************************************************************************

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.router;

import ensemble.world.Law;
import ensemble.world.LawState;
import ensemble.world.Vector;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageLaw.
 */
public class MessageLaw extends Law {

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
		setType(MessageConstants.EVT_TYPE_MESSAGE);
		if (parameters.containsKey("gravity")) {
			this.gravity = Double.valueOf(parameters.get("gravity"));
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
		
		MessageState prevState = new MessageState();
		prevState.instant = 0;
//		prevState.velocity.setValue(0, 10);
//		prevState.angularVelocity.setValue(0, 1);

		MessageState newState = new MessageState();

		for (int j = 0; j < 20000; j++) {
			changeState(prevState, 10.0, newState);
		}
		
//		prevState.velocity.zero();
//		prevState.velocity.setValue(0, 10);
//		prevState.acceleration.zero();
//		prevState.acceleration.setValue(0, 10);

		for (int j = 0; j < 20000; j++) {
			changeState(prevState, 10.0, newState);
		}
		
//		prevState.velocity.zero();
//		prevState.acceleration.zero();
//		prevState.acceleration.setValue(0, 50);
//	
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
		if (!(prevState instanceof MessageState) && !(newState instanceof MessageState)) {
			System.err.println("[MovementLaw] Not the right kind of state!");
			return;
		}
		
		MessageState movPrevState = (MessageState)prevState;
		MessageState movNewState = (MessageState)newState;

		// Copies the prevState into newState
		//movPrevState.copy(movNewState);
		movNewState.instant = instant;

		// Does any necessary calculation
		double interval = instant - movPrevState.instant;
//		System.out.printf("interval = %f\n", interval);
		if (interval > 0) {
			
									
			
			
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
