package ensemble.apps.lm;

public class LM_Constants {

	public final static int 	WorldSize 				= 50;
	public final static int 	SoundRadius 			= 10;
	public final static int 	MaxSoundGenomeLength 	= 5;
	public final static int 	MaxActionGenomeLength 	= 12;
	public final static int 	MaxAge				 	= 4000;
	public final static float 	MinLifePoints		 	= 1.0f;
	public final static int 	MaxNumberOfCreatures 	= 100;
	public final static int 	MaxLoopLength 			= 5;
	public final static int 	MaxLoopSteps 			= 5;
	public final static int 	MaxIfSteps 				= 5;
	public final static float 	InitCreatureProb 		= 0.01f;
	public final static float 	InitFoodProb			= 0.05f;
	public final static float 	SoundCrossoverProb		= 0.2f;
	public final static float 	ActionCrossoverProb		= 0.1f;
	public final static float 	MutateSpecies 			= 0.05f;
	public final static float 	MutateRandomCreature 	= 0.02f;
	public final static int 	MateSpread 				= 5;
	public final static int 	MustWalkInterval 		= 5;
	public final static int 	DeathLength 			= 30;
	public final static float	MinEnergy 				= 1.0f;
	public final static float	MinEnergyToMate			= 5.0f;
	public final static int		MinAgeToMate			= 20;
	public final static float	MinLpToMate				= 0.5f;
	
	// Energy
	public final static float 	InitalAgentEnergy		= 0.1f;
	public final static float 	CostOfSinging			= 0.1f;
	public final static float 	CostOfTime				= 0.1f;

	// Sound Mapping
	public final static boolean AbsoluteNoteMapping		= false;
	public final static boolean GlobalInterval			= true;
	
	// MIDI Parameters
	public final static int 	MinMidiNote 			= 48; 	// C2 
	public final static int 	MaxMidiNote 			= 119;	// B8
	
	public final static boolean CircularInstructions 	= true;
	public final static float 	ListeningPleasureDecay 	= 0.01f;
	
}
