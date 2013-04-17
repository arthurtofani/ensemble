package ensemble.apps.lm;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_Constants.
 */
public class LM_Constants {

	/** The Constant WorldSize. */
	public final static int 	WorldSize 				= 50;
	
	/** The Constant SoundRadius. */
	public final static int 	SoundRadius 			= 10;
	
	/** The Constant MaxSoundGenomeLength. */
	public final static int 	MaxSoundGenomeLength 	= 5;
	
	/** The Constant MaxActionGenomeLength. */
	public final static int 	MaxActionGenomeLength 	= 12;
	
	/** The Constant MaxAge. */
	public final static int 	MaxAge				 	= 4000;
	
	/** The Constant MinLifePoints. */
	public final static float 	MinLifePoints		 	= 1.0f;
	
	/** The Constant MaxNumberOfCreatures. */
	public final static int 	MaxNumberOfCreatures 	= 100;
	
	/** The Constant MaxLoopLength. */
	public final static int 	MaxLoopLength 			= 5;
	
	/** The Constant MaxLoopSteps. */
	public final static int 	MaxLoopSteps 			= 5;
	
	/** The Constant MaxIfSteps. */
	public final static int 	MaxIfSteps 				= 5;
	
	/** The Constant InitCreatureProb. */
	public final static float 	InitCreatureProb 		= 0.01f;
	
	/** The Constant InitFoodProb. */
	public final static float 	InitFoodProb			= 0.05f;
	
	/** The Constant SoundCrossoverProb. */
	public final static float 	SoundCrossoverProb		= 0.2f;
	
	/** The Constant ActionCrossoverProb. */
	public final static float 	ActionCrossoverProb		= 0.1f;
	
	/** The Constant MutateSpecies. */
	public final static float 	MutateSpecies 			= 0.05f;
	
	/** The Constant MutateRandomCreature. */
	public final static float 	MutateRandomCreature 	= 0.02f;
	
	/** The Constant MateSpread. */
	public final static int 	MateSpread 				= 5;
	
	/** The Constant MustWalkInterval. */
	public final static int 	MustWalkInterval 		= 5;
	
	/** The Constant DeathLength. */
	public final static int 	DeathLength 			= 30;
	
	/** The Constant MinEnergy. */
	public final static float	MinEnergy 				= 1.0f;
	
	/** The Constant MinEnergyToMate. */
	public final static float	MinEnergyToMate			= 5.0f;
	
	/** The Constant MinAgeToMate. */
	public final static int		MinAgeToMate			= 20;
	
	/** The Constant MinLpToMate. */
	public final static float	MinLpToMate				= 0.5f;
	
	// Energy
	/** The Constant InitalAgentEnergy. */
	public final static float 	InitalAgentEnergy		= 0.1f;
	
	/** The Constant CostOfSinging. */
	public final static float 	CostOfSinging			= 0.1f;
	
	/** The Constant CostOfTime. */
	public final static float 	CostOfTime				= 0.1f;

	// Sound Mapping
	/** The Constant AbsoluteNoteMapping. */
	public final static boolean AbsoluteNoteMapping		= false;
	
	/** The Constant GlobalInterval. */
	public final static boolean GlobalInterval			= true;
	
	// MIDI Parameters
	/** The Constant MinMidiNote. */
	public final static int 	MinMidiNote 			= 48; 	// C2 
	
	/** The Constant MaxMidiNote. */
	public final static int 	MaxMidiNote 			= 119;	// B8
	
	/** The Constant CircularInstructions. */
	public final static boolean CircularInstructions 	= true;
	
	/** The Constant ListeningPleasureDecay. */
	public final static float 	ListeningPleasureDecay 	= 0.01f;
	
}
