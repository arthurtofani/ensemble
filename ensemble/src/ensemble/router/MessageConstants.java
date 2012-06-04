package ensemble.router;

public class MessageConstants {
	
	public static final String EVT_TYPE_MESSAGE = "MESSAGE";

	public static final String CMD_SEND 		= "SEND";
	public static final String CMD_RECEIVE 		= "RECEIVE";	
	public static final String CMD_INFO 		= "INFO";
	
	public static final String PARAM_TYPE 		= "TYPE";
	public static final String PARAM_ARGS 		= "ARG";
	public static final String PARAM_DOMAIN 	= "DOMAIN";
	public static final String PARAM_ACTION 	= "ACTION";

	public static final String DEFAULT_TYPE 	= "DEFAULT_TYPE";
	public static final String DEFAULT_DOMAIN 	= "DEFAULT_DOMAIN";
	public static final String DEFAULT_ACTION 	= "NOTIFY_INITIALIZATION";
	
	public static final String EXT_OSC_DOMAIN 	= "EXT_OSC_DOMAIN";
	
	//SPIN OSC
	public static final String SPIN_OSC_IDNUMBER 	= "SPIN_OSC_IDNUMBER";
	public static final String SPIN_OSC_CMD 		= "SPIN_OSC_CMD";	
	public static final String SPIN_OSC_TYPE 		= "SPIN_OSC_TYPE";
	public static final String SPIN_OSC_SEARCH  	= "spin/";
	public static final String SPIN_OSC_DATA 		= "data";	
	
	public static final String SPIN_OSC_POSITION  	= "SPIN_POSITION";
	
	//ANDOSC
	public static final String ANDOSC_ORI  	= "/ori";
	public static final String ANDOSC_ACC	= "/acc";
	public static final String ANDOSC_TOUCH			= "/touch";
	
	public static final String ANDOSC_TYPE 			= "ANDOSC_TYPE";
	
	public static final String ANDOSC_ORIENTATION  	= "ANDOSC_ORIENTATION";
	public static final String ANDOSC_ACCELEROMETER = "ANDOSC_ACCELEROMETER";
	public static final String ANDOSC_TOUCH_POS  	= "ANDOSC_TOUCH_POS";
	
	
	public static final String INTERNAL_DOMAIN 	= "INTERNAL_DOMAIN";
	//DIRECTION CHANGE
	public static final String DIRECTION_TYPE 			= "DIRECTION_TYPE";
	public static final String DIRECTION_CHANGE 			= "DIRECTION_CHANGE";
	public static final String DIRECTION_RIGHT 			= "1";
	public static final String DIRECTION_LEFT 			= "2";
	public static final String DIRECTION_UP 			= "3";
	public static final String DIRECTION_DOWN 			= "4";

	//ISO - Interactive Swarm Orchestra
	public static final String ISO_SWARM 			= "/swarm";
	
	public static final String ISO_TYPE 			= "ISO_TYPE";
	public static final String ISO_POSITION  		= "ISO_POSITION";
	public static final String SWARM_MOVEMENT_TYPE  = "SWARM_MOVEMENT_TYPE";
	public static final String SWARM_DEFAULT_MVT  	= "SWARM_DEFAULT_MVT";
	public static final String SWARM_CIRCULAR_MVT  	= "SWARM_CIRCULAR_MVT";
	public static final String SWARM_FAST_MVT  		= "SWARM_FAST_MVT";
	public static final String SWARM_NUMBER			= "SWARM_NUMBER";
	public static final String AGENT_NUMBER			= "AGENT_NUMBER";
	
	//CONTROL OSC
	public static final String CONTROL_OSC_TYPE 		= "CONTROL_OSC_TYPE";
	public static final String CONTROL_OSC_POSITION  	= "CONTROL_OSC_POSITION";
	public static final String CONTROL_OSC_DELAY  		= "CONTROL_OSC_DELAY";
	public static final String CONTROL_OSC_VOLUME  		= "CONTROL_OSC_VOLUME";
	public static final String CONTROL_OSC_MVT_TYPE  	= "CONTROL_OSC_MVT_TYPE";
	public static final String CONTROL_OSC_FREQ  		= "CONTROL_OSC_FREQ";
	public static final String CONTROL_SLIDER1 			= "/slider1";
	public static final String CONTROL_SLIDER2 			= "/slider2";
	public static final String CONTROL_MONO 			= "/mlr";
}