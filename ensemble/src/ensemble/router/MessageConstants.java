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
	
}