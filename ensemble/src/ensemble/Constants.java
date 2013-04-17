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

package ensemble;

// TODO: Auto-generated Javadoc
/**
 * The Class Constants.
 */
public final class Constants {

	/** The Constant FRAMEWORK_NAME. */
	public static final String  FRAMEWORK_NAME 			= "ensemble";
	
	// Modos do Clock
	/** The Constant CLOCK_MODE. */
	public static final String 	CLOCK_MODE 				= "CLOCK_MODE";
	
	/** The Constant CLOCK_USER. */
	public static final String 	CLOCK_USER	 			= "CLOCK_USER";
	
	/** The Constant CLOCK_CPU. */
	public static final String 	CLOCK_CPU	 			= "CLOCK_CPU";
	
	// Modos de Processamento
	/** The Constant PROCESS_MODE. */
	public static final String 	PROCESS_MODE 			= "PROCESS_MODE";
	
	/** The Constant MODE_BATCH. */
	public static final String 	MODE_BATCH 				= "BATCH";
	
	/** The Constant MODE_REAL_TIME. */
	public static final String 	MODE_REAL_TIME			= "REAL_TIME";
	
	/** The Constant SCHEDULER_THREADS. */
	public static final String SCHEDULER_THREADS 		= "SCHEDULER_THREADS";

	/** The Constant WAIT_TIME_TURN. */
	public static final String 	WAIT_TIME_TURN 			= "WAIT_TIME_TURN";
	
	/** The Constant WAIT_ALL_AGENTS. */
	public static final String 	WAIT_ALL_AGENTS 		= "WAIT_ALL_AGENTS";
		
	// Tipos de Components
	/** The Constant COMP_SENSOR. */
	public static final String 	COMP_SENSOR 			= "SENSOR";
	
	/** The Constant COMP_ACTUATOR. */
	public static final String 	COMP_ACTUATOR 			= "ACTUATOR";
	
	/** The Constant COMP_REASONING. */
	public static final String 	COMP_REASONING 			= "REASONING";
	
	/** The Constant COMP_KB. */
	public static final String 	COMP_KB 				= "KB";
	
	/** The Constant COMP_ANALYZER. */
	public static final String 	COMP_ANALYZER 			= "ANALYZER";
	
	/** The Constant COMP_SYNTHESIZER. */
	public static final String 	COMP_SYNTHESIZER 		= "SYNTHESIZER";
	
	// Tipos de troca de evento
	/** The Constant EVT_EXC_NOT_DEFINED. */
	public static final String	EVT_EXC_NOT_DEFINED		= "NOT_DEFINED";
	
	/** The Constant EVT_EXC_SPORADIC. */
	public static final String	EVT_EXC_SPORADIC 		= "SPORADIC";
	
	/** The Constant EVT_EXC_HYBRID. */
	public static final String	EVT_EXC_HYBRID	 		= "HYBRID";
	
	/** The Constant EVT_EXC_PERIODIC. */
	public static final String	EVT_EXC_PERIODIC 		= "PERIODIC";

	// Parâmetros genéricos
	/** The Constant PARAM_COMP_NAME. */
	public static final String	PARAM_COMP_NAME			= "COMP_NAME";
	
	/** The Constant PARAM_COMP_TYPE. */
	public static final String	PARAM_COMP_TYPE			= "COMP_TYPE";
	
	/** The Constant PARAM_EVT_TYPE. */
	public static final String	PARAM_EVT_TYPE			= "EVT_TYPE";
	
	/** The Constant PARAM_EVT_EXECUTION. */
	public static final String	PARAM_EVT_EXECUTION		= "EVT_EXECUTION";
	
	/** The Constant PARAM_ES_EVT_TYPE. */
	public static final String 	PARAM_ES_EVT_TYPE 		= "ES_EVT_TYPE";
	
	/** The Constant PARAM_COMM_CLASS. */
	public static final String	PARAM_COMM_CLASS 		= "COMM_CLASS";
	
	/** The Constant PARAM_COMM_AGENT. */
	public static final String	PARAM_COMM_AGENT 		= "COMM_AGENT";
	
	/** The Constant PARAM_COMM_SENSING. */
	public static final String	PARAM_COMM_SENSING		= "COMM_SENSING";
	
	/** The Constant PARAM_COMM_ACTING. */
	public static final String	PARAM_COMM_ACTING		= "COMM_ACTING";
	
	/** The Constant PARAM_COMM_ACCESS_POINT. */
	public static final String 	PARAM_COMM_ACCESS_POINT = "COMM_ACCESS_POINT";
	
	/** The Constant PARAM_MEMORY_NAME. */
	public static final String	PARAM_MEMORY_NAME 		= "MEMORY_NAME";
	
	/** The Constant PARAM_MEMORY_CLASS. */
	public static final String	PARAM_MEMORY_CLASS 		= "MEMORY_CLASS";
	
	/** The Constant PARAM_MEMORY_FUTURE. */
	public static final String	PARAM_MEMORY_FUTURE 	= "MEMORY_FUTURE";
	
	/** The Constant PARAM_MEMORY_PAST. */
	public static final String	PARAM_MEMORY_PAST 		= "MEMORY_PAST";
	
	/** The Constant PARAM_TURN. */
	public static final String	PARAM_TURN 				= "TURN";
	
	/** The Constant PARAM_NUMBER_EVT_SENT. */
	public static final String	PARAM_NUMBER_EVT_SENT 	= "PARAM_NUMBER_EVT_SENT";
	
	/** The Constant PARAM_FACT_NAME. */
	public static final String	PARAM_FACT_NAME			= "FACT_NAME";
	
	/** The Constant PARAM_FACT_VALUE. */
	public static final String	PARAM_FACT_VALUE 		= "FACT_VALUE";
	
	/** The Constant PARAM_REASONING_MODE. */
	public static final String	PARAM_REASONING_MODE 	= "REASONING_MODE";
	
	/** The Constant PARAM_REL_POS. */
	public static final String 	PARAM_REL_POS 			= "RELATIVE_POSITION";
	
	/** The Constant PARAM_POSITION. */
	public static final String 	PARAM_POSITION 			= "POSITION";
	
	// Parâmetros da troca de evento periódica
	/** The Constant PARAM_START_TIME. */
	public static final String	PARAM_START_TIME		= "START_TIME";
	
	/** The Constant PARAM_WORKING_FRAME. */
	public static final String	PARAM_WORKING_FRAME		= "WORKING_FRAME";
	
	/** The Constant PARAM_PERIOD. */
	public static final String	PARAM_PERIOD			= "PERIOD";
	
	/** The Constant PARAM_RCV_DEADLINE. */
	public static final String	PARAM_RCV_DEADLINE		= "RCV_DEADLINE";
	
	// Parâmetros de áudio
	/** The Constant PARAM_CHUNK_SIZE. */
	public static final String	PARAM_CHUNK_SIZE		= "CHUNK_SIZE";
	
	/** The Constant PARAM_SAMPLE_RATE. */
	public static final String	PARAM_SAMPLE_RATE		= "SAMPLE_RATE";
	
	/** The Constant PARAM_STEP. */
	public static final String	PARAM_STEP				= "STEP";
	
	/** The Constant PARAM_CHANNELS. */
	public static final String	PARAM_CHANNELS			= "CHANNELS";
	
	// Comandos
	/** The Constant CMD_EVENT_REGISTER. */
	public static final String 	CMD_EVENT_REGISTER 		= "EVENT_REGISTER";
	
	/** The Constant CMD_EVENT_REGISTER_ACK. */
	public static final String 	CMD_EVENT_REGISTER_ACK 	= "EVENT_REGISTER_ACK";
	
	/** The Constant CMD_EVENT_DEREGISTER. */
	public static final String 	CMD_EVENT_DEREGISTER 	= "EVENT_DEREGISTER";
	
	/** The Constant CMD_EVENT_DEREGISTER_ACK. */
	public static final String 	CMD_EVENT_DEREGISTER_ACK= "EVENT_DEREGISTER_ACK";
	
	/** The Constant CMD_PARAMETER. */
	public static final String 	CMD_PARAMETER			= "PARAMETER";
	
	/** The Constant CMD_FACT. */
	public static final String 	CMD_FACT	 			= "FACT";
	
	/** The Constant CMD_AGENT_REGISTER. */
	public static final String 	CMD_AGENT_REGISTER		= "AGENT_REGISTER";
	
	/** The Constant CMD_AGENT_DEREGISTER. */
	public static final String 	CMD_AGENT_DEREGISTER	= "AGENT_DEREGISTER";
	
	/** The Constant CMD_AGENT_READY. */
	public static final String 	CMD_AGENT_READY 		= "AGENT_READY";
	
	/** The Constant CMD_AGENT_READY_ACK. */
	public static final String 	CMD_AGENT_READY_ACK		= "AGENT_READY_ACK";
	
	/** The Constant CMD_BATCH_TURN. */
	public static final String 	CMD_BATCH_TURN			= "BATCH_TURN";
	
	/** The Constant CMD_BATCH_TURN_ACK. */
	public static final String 	CMD_BATCH_TURN_ACK		= "BATCH_TURN_ACK";
	
	/** The Constant CMD_BATCH_EVENT_ACK. */
	public static final String 	CMD_BATCH_EVENT_ACK		= "BATCH_EVENT_ACK";
	
	/** The Constant CMD_PUBLIC_FACT_UPDATE. */
	public static final String 	CMD_PUBLIC_FACT_UPDATE	= "PUBLIC_FACT_UPDATE";
	
	/** The Constant CMD_KILL_AGENT. */
	public static final String 	CMD_KILL_AGENT			= "KILL_AGENT";
	
	/** The Constant CMD_KILL_AGENT_ACK. */
	public static final String 	CMD_KILL_AGENT_ACK		= "KILL_AGENT_ACK";

	/** The Constant CMD_START_SIMULATION. */
	public static final String 	CMD_START_SIMULATION 	= "START_SIMULATION";
	
	/** The Constant CMD_STOP_SIMULATION. */
	public static final String 	CMD_STOP_SIMULATION 	= "STOP_SIMULATION";
	
	/** The Constant CMD_CREATE_AGENT. */
	public static final String 	CMD_CREATE_AGENT 		= "CREATE_AGENT";
	
	/** The Constant CMD_ADD_COMPONENT. */
	public static final String 	CMD_ADD_COMPONENT 		= "ADD_COMPONENT";
	
	/** The Constant CMD_REMOVE_COMPONENT. */
	public static final String 	CMD_REMOVE_COMPONENT 	= "REMOVE_COMPONENT";
	
	/** The Constant CMD_DESTROY_AGENT. */
	public static final String 	CMD_DESTROY_AGENT 		= "DESTROY_AGENT";
	
	/** The Constant CMD_ADD_EVENT_SERVER. */
	public static final String 	CMD_ADD_EVENT_SERVER	= "ADD_EVENT_SERVER";
	
	/** The Constant CMD_REMOVE_EVENT_SERVER. */
	public static final String 	CMD_REMOVE_EVENT_SERVER = "REMOVE_EVENT_SERVER";
	
	// 
	/** The Constant ENVIRONMENT_AGENT. */
	public static final String 	ENVIRONMENT_AGENT 		= "Environment";
	
	/** The Constant WORLD. */
	public static final String 	WORLD 					= "World";
	
	/** The Constant KNOWLEDGE_BASE. */
	public static final String 	KNOWLEDGE_BASE 			= "KnowledgeBase";
	
	// EventHandler Status
	/**
	 * The Enum EH_STATUS.
	 */
	public static enum EH_STATUS {
/** The registered. */
REGISTERED, 
 /** The not registered. */
 NOT_REGISTERED};
	// MusicalAgent States
	/**
	 * The Enum EA_STATE.
	 */
	public static enum EA_STATE {
/** The created. */
CREATED, 
 /** The initialized. */
 INITIALIZED, 
 /** The terminating. */
 TERMINATING, 
 /** The finalized. */
 FINALIZED};
	// MusicalAgent States
	/**
	 * The Enum MA_STATE.
	 */
	public static enum MA_STATE {
/** The created. */
CREATED, 
 /** The initialized. */
 INITIALIZED, 
 /** The registered. */
 REGISTERED, 
 /** The terminating. */
 TERMINATING, 
 /** The finalized. */
 FINALIZED};
	// Actuator States
	/**
	 * The Enum AC_STATE.
	 */
	public static enum AC_STATE {
/** The created. */
CREATED, 
 /** The initialized. */
 INITIALIZED, 
 /** The waiting begin. */
 WAITING_BEGIN, 
 /** The processing. */
 PROCESSING, 
 /** The need acting. */
 NEED_ACTING, 
 /** The waiting response. */
 WAITING_RESPONSE, 
 /** The terminated. */
 TERMINATED}
	// EventServer States
	/**
	 * The Enum ES_STATE.
	 */
	public static enum ES_STATE {
/** The created. */
CREATED, 
 /** The configured. */
 CONFIGURED, 
 /** The initialized. */
 INITIALIZED, 
 /** The waiting begin. */
 WAITING_BEGIN, 
 /** The waiting agents. */
 WAITING_AGENTS, 
 /** The processing. */
 PROCESSING, 
 /** The sending response. */
 SENDING_RESPONSE, 
 /** The terminated. */
 TERMINATED}

	//
	/** The Constant REP_TYPE_INPUT. */
	public static final String REP_TYPE_INPUT			= "INPUT";
	
	/** The Constant REP_TYPE_OUTPUT. */
	public static final String REP_TYPE_OUTPUT			= "OUTPUT";
	
	/** The Constant REP_TYPE_PARAMETERS. */
	public static final String REP_TYPE_PARAMETERS  	= "PARAMETERS";
	
	/** The Constant SUF_AUXILIAR_MEMORY. */
	public static final String SUF_AUXILIAR_MEMORY		= "AUX";

}
