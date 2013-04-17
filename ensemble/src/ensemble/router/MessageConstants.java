package ensemble.router;

// TODO: Auto-generated Javadoc
/**
 * The Class MessageConstants.
 */
public class MessageConstants {
	
	/** The Constant EVT_TYPE_MESSAGE. */
	public static final String EVT_TYPE_MESSAGE = "MESSAGE";

	/** The Constant CMD_SEND. */
	public static final String CMD_SEND 		= "SEND";
	
	/** The Constant CMD_RECEIVE. */
	public static final String CMD_RECEIVE 		= "RECEIVE";	
	
	/** The Constant CMD_INFO. */
	public static final String CMD_INFO 		= "INFO";
	
	/** The Constant PARAM_TYPE. */
	public static final String PARAM_TYPE 		= "TYPE";
	
	/** The Constant PARAM_ARGS. */
	public static final String PARAM_ARGS 		= "ARG";
	
	/** The Constant PARAM_DOMAIN. */
	public static final String PARAM_DOMAIN 	= "DOMAIN";
	
	/** The Constant PARAM_ACTION. */
	public static final String PARAM_ACTION 	= "ACTION";

	/** The Constant DEFAULT_TYPE. */
	public static final String DEFAULT_TYPE 	= "DEFAULT_TYPE";
	
	/** The Constant DEFAULT_DOMAIN. */
	public static final String DEFAULT_DOMAIN 	= "DEFAULT_DOMAIN";
	
	/** The Constant DEFAULT_ACTION. */
	public static final String DEFAULT_ACTION 	= "NOTIFY_INITIALIZATION";
	
	/** The Constant EXT_OSC_DOMAIN. */
	public static final String EXT_OSC_DOMAIN 	= "EXT_OSC_DOMAIN";
	
	//SPIN OSC
	/** The Constant SPIN_OSC_IDNUMBER. */
	public static final String SPIN_OSC_IDNUMBER 	= "SPIN_OSC_IDNUMBER";
	
	/** The Constant SPIN_OSC_CMD. */
	public static final String SPIN_OSC_CMD 		= "SPIN_OSC_CMD";	
	
	/** The Constant SPIN_OSC_TYPE. */
	public static final String SPIN_OSC_TYPE 		= "SPIN_OSC_TYPE";
	
	/** The Constant SPIN_OSC_SEARCH. */
	public static final String SPIN_OSC_SEARCH  	= "spin/";
	
	/** The Constant SPIN_OSC_DATA. */
	public static final String SPIN_OSC_DATA 		= "data";	
	
	/** The Constant SPIN_OSC_POSITION. */
	public static final String SPIN_OSC_POSITION  	= "SPIN_POSITION";
	
	//ANDOSC
	/** The Constant ANDOSC_ORI. */
	public static final String ANDOSC_ORI  	= "/ori";
	
	/** The Constant ANDOSC_ACC. */
	public static final String ANDOSC_ACC	= "/acc";
	
	/** The Constant ANDOSC_TOUCH. */
	public static final String ANDOSC_TOUCH			= "/touch";
	
	/** The Constant ANDOSC_TYPE. */
	public static final String ANDOSC_TYPE 			= "ANDOSC_TYPE";
	
	/** The Constant ANDOSC_ORIENTATION. */
	public static final String ANDOSC_ORIENTATION  	= "ANDOSC_ORIENTATION";
	
	/** The Constant ANDOSC_ACCELEROMETER. */
	public static final String ANDOSC_ACCELEROMETER = "ANDOSC_ACCELEROMETER";
	
	/** The Constant ANDOSC_TOUCH_POS. */
	public static final String ANDOSC_TOUCH_POS  	= "ANDOSC_TOUCH_POS";
	
	
	/** The Constant INTERNAL_DOMAIN. */
	public static final String INTERNAL_DOMAIN 	= "INTERNAL_DOMAIN";
	//DIRECTION CHANGE
	/** The Constant DIRECTION_TYPE. */
	public static final String DIRECTION_TYPE 			= "DIRECTION_TYPE";
	
	/** The Constant DIRECTION_CHANGE. */
	public static final String DIRECTION_CHANGE 			= "DIRECTION_CHANGE";
	
	/** The Constant DIRECTION_RIGHT. */
	public static final String DIRECTION_RIGHT 			= "1";
	
	/** The Constant DIRECTION_LEFT. */
	public static final String DIRECTION_LEFT 			= "2";
	
	/** The Constant DIRECTION_UP. */
	public static final String DIRECTION_UP 			= "3";
	
	/** The Constant DIRECTION_DOWN. */
	public static final String DIRECTION_DOWN 			= "4";

	//ISO - Interactive Swarm Orchestra
	/** The Constant ISO_SWARM. */
	public static final String ISO_SWARM 			= "/swarm";
	
	/** The Constant ISO_TYPE. */
	public static final String ISO_TYPE 			= "ISO_TYPE";
	
	/** The Constant ISO_POSITION. */
	public static final String ISO_POSITION  		= "ISO_POSITION";
	
	/** The Constant SWARM_MOVEMENT_TYPE. */
	public static final String SWARM_MOVEMENT_TYPE  = "SWARM_MOVEMENT_TYPE";
	
	/** The Constant SWARM_DEFAULT_MVT. */
	public static final String SWARM_DEFAULT_MVT  	= "SWARM_DEFAULT_MVT";
	
	/** The Constant SWARM_CIRCULAR_MVT. */
	public static final String SWARM_CIRCULAR_MVT  	= "SWARM_CIRCULAR_MVT";
	
	/** The Constant SWARM_FAST_MVT. */
	public static final String SWARM_FAST_MVT  		= "SWARM_FAST_MVT";
	
	/** The Constant SWARM_NUMBER. */
	public static final String SWARM_NUMBER			= "SWARM_NUMBER";
	
	/** The Constant AGENT_NUMBER. */
	public static final String AGENT_NUMBER			= "AGENT_NUMBER";
	
	//CONTROL OSC
	/** The Constant CONTROL_OSC_TYPE. */
	public static final String CONTROL_OSC_TYPE 		= "CONTROL_OSC_TYPE";
	
	/** The Constant CONTROL_OSC_POSITION. */
	public static final String CONTROL_OSC_POSITION  	= "CONTROL_OSC_POSITION";
	
	/** The Constant CONTROL_OSC_DELAY. */
	public static final String CONTROL_OSC_DELAY  		= "CONTROL_OSC_DELAY";
	
	/** The Constant CONTROL_OSC_VOLUME. */
	public static final String CONTROL_OSC_VOLUME  		= "CONTROL_OSC_VOLUME";
	
	/** The Constant CONTROL_OSC_MVT_TYPE. */
	public static final String CONTROL_OSC_MVT_TYPE  	= "CONTROL_OSC_MVT_TYPE";
	
	/** The Constant CONTROL_OSC_FREQ. */
	public static final String CONTROL_OSC_FREQ  		= "CONTROL_OSC_FREQ";
	
	/** The Constant CONTROL_SLIDER1. */
	public static final String CONTROL_SLIDER1 			= "/slider1";
	
	/** The Constant CONTROL_SLIDER2. */
	public static final String CONTROL_SLIDER2 			= "/slider2";
	
	/** The Constant CONTROL_MONO. */
	public static final String CONTROL_MONO 			= "/mlr";
	
	//INTERFACE PIANO PREPARADO
	/** The Constant PP_OSC_TYPE. */
	public static final String PP_OSC_TYPE 		= "PP_OSC_TYPE";
	
	/** The Constant PP_SWITCH_TYPE. */
	public static final String PP_SWITCH_TYPE 	= "PP_SWITCH_TYPE";
	
	/** The Constant PP_OSC_SWITCH. */
	public static final String PP_OSC_SWITCH 	= "/M";
	
	/** The Constant PP_OSC_ISO. */
	public static final String PP_OSC_ISO 		= "/ISO";
	
	/** The Constant PP_OSC_LP. */
	public static final String PP_OSC_LP 		= "/LP";
	
	/** The Constant PP_OSC_HP. */
	public static final String PP_OSC_HP 		= "/HP";
	
	
}