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

public final class Constants {

	public static final String  FRAMEWORK_NAME 			= "ensemble";
	
	// Modos do Clock
	public static final String 	CLOCK_MODE 				= "CLOCK_MODE";
	public static final String 	CLOCK_USER	 			= "CLOCK_USER";
	public static final String 	CLOCK_CPU	 			= "CLOCK_CPU";
	
	// Modos de Processamento
	public static final String 	PROCESS_MODE 			= "PROCESS_MODE";
	public static final String 	MODE_BATCH 				= "BATCH";
	public static final String 	MODE_REAL_TIME			= "REAL_TIME";
	
	public static final String SCHEDULER_THREADS 		= "SCHEDULER_THREADS";

	public static final String 	WAIT_TIME_TURN 			= "WAIT_TIME_TURN";
	public static final String 	WAIT_ALL_AGENTS 		= "WAIT_ALL_AGENTS";
		
	// Tipos de Components
	public static final String 	COMP_SENSOR 			= "SENSOR";
	public static final String 	COMP_ACTUATOR 			= "ACTUATOR";
	public static final String 	COMP_REASONING 			= "REASONING";
	public static final String 	COMP_KB 				= "KB";
	public static final String 	COMP_ANALYZER 			= "ANALYZER";
	public static final String 	COMP_SYNTHESIZER 		= "SYNTHESIZER";
	
	// Tipos de troca de evento
	public static final String	EVT_EXC_NOT_DEFINED		= "NOT_DEFINED";
	public static final String	EVT_EXC_SPORADIC 		= "SPORADIC";
	public static final String	EVT_EXC_HYBRID	 		= "HYBRID";
	public static final String	EVT_EXC_PERIODIC 		= "PERIODIC";

	// Parâmetros genéricos
	public static final String	PARAM_COMP_NAME			= "COMP_NAME";
	public static final String	PARAM_COMP_TYPE			= "COMP_TYPE";
	public static final String	PARAM_EVT_TYPE			= "EVT_TYPE";
	public static final String	PARAM_EVT_EXECUTION		= "EVT_EXECUTION";
	public static final String 	PARAM_ES_EVT_TYPE 		= "ES_EVT_TYPE";
	public static final String	PARAM_COMM_CLASS 		= "COMM_CLASS";
	public static final String	PARAM_COMM_AGENT 		= "COMM_AGENT";
	public static final String	PARAM_COMM_SENSING		= "COMM_SENSING";
	public static final String	PARAM_COMM_ACTING		= "COMM_ACTING";
	public static final String 	PARAM_COMM_ACCESS_POINT = "COMM_ACCESS_POINT";
	public static final String	PARAM_MEMORY_NAME 		= "MEMORY_NAME";
	public static final String	PARAM_MEMORY_CLASS 		= "MEMORY_CLASS";
	public static final String	PARAM_MEMORY_FUTURE 	= "MEMORY_FUTURE";
	public static final String	PARAM_MEMORY_PAST 		= "MEMORY_PAST";
	public static final String	PARAM_TURN 				= "TURN";
	public static final String	PARAM_NUMBER_EVT_SENT 	= "PARAM_NUMBER_EVT_SENT";
	public static final String	PARAM_FACT_NAME			= "FACT_NAME";
	public static final String	PARAM_FACT_VALUE 		= "FACT_VALUE";
	public static final String	PARAM_REASONING_MODE 	= "REASONING_MODE";
	public static final String 	PARAM_REL_POS 			= "RELATIVE_POSITION";
	
	public static final String 	PARAM_POSITION 			= "POSITION";
	
	// Parâmetros da troca de evento periódica
	public static final String	PARAM_START_TIME		= "START_TIME";
	public static final String	PARAM_WORKING_FRAME		= "WORKING_FRAME";
	public static final String	PARAM_PERIOD			= "PERIOD";
	public static final String	PARAM_RCV_DEADLINE		= "RCV_DEADLINE";
	
	// Parâmetros de áudio
	public static final String	PARAM_CHUNK_SIZE		= "CHUNK_SIZE";
	public static final String	PARAM_SAMPLE_RATE		= "SAMPLE_RATE";
	public static final String	PARAM_STEP				= "STEP";
	public static final String	PARAM_CHANNELS			= "CHANNELS";
	
	// Comandos
	public static final String 	CMD_EVENT_REGISTER 		= "EVENT_REGISTER";
	public static final String 	CMD_EVENT_REGISTER_ACK 	= "EVENT_REGISTER_ACK";
	public static final String 	CMD_EVENT_DEREGISTER 	= "EVENT_DEREGISTER";
	public static final String 	CMD_EVENT_DEREGISTER_ACK= "EVENT_DEREGISTER_ACK";
	public static final String 	CMD_PARAMETER			= "PARAMETER";
	public static final String 	CMD_FACT	 			= "FACT";
	public static final String 	CMD_AGENT_REGISTER		= "AGENT_REGISTER";
	public static final String 	CMD_AGENT_DEREGISTER	= "AGENT_DEREGISTER";
	public static final String 	CMD_AGENT_READY 		= "AGENT_READY";
	public static final String 	CMD_AGENT_READY_ACK		= "AGENT_READY_ACK";
	public static final String 	CMD_BATCH_TURN			= "BATCH_TURN";
	public static final String 	CMD_BATCH_TURN_ACK		= "BATCH_TURN_ACK";
	public static final String 	CMD_BATCH_EVENT_ACK		= "BATCH_EVENT_ACK";
	public static final String 	CMD_PUBLIC_FACT_UPDATE	= "PUBLIC_FACT_UPDATE";
	public static final String 	CMD_KILL_AGENT			= "KILL_AGENT";
	public static final String 	CMD_KILL_AGENT_ACK		= "KILL_AGENT_ACK";

	public static final String 	CMD_START_SIMULATION 	= "START_SIMULATION";
	public static final String 	CMD_STOP_SIMULATION 	= "STOP_SIMULATION";
	public static final String 	CMD_CREATE_AGENT 		= "CREATE_AGENT";
	public static final String 	CMD_ADD_COMPONENT 		= "ADD_COMPONENT";
	public static final String 	CMD_REMOVE_COMPONENT 	= "REMOVE_COMPONENT";
	public static final String 	CMD_DESTROY_AGENT 		= "DESTROY_AGENT";
	public static final String 	CMD_ADD_EVENT_SERVER	= "ADD_EVENT_SERVER";
	public static final String 	CMD_REMOVE_EVENT_SERVER = "REMOVE_EVENT_SERVER";
	
	// 
	public static final String 	ENVIRONMENT_AGENT 		= "Environment";
	public static final String 	WORLD 					= "World";
	public static final String 	KNOWLEDGE_BASE 			= "KnowledgeBase";
	
	// EventHandler Status
	public static enum EH_STATUS {REGISTERED, NOT_REGISTERED};
	// MusicalAgent States
	public static enum EA_STATE {CREATED, INITIALIZED, TERMINATING, FINALIZED};
	// MusicalAgent States
	public static enum MA_STATE {CREATED, INITIALIZED, REGISTERED, TERMINATING, FINALIZED};
	// Actuator States
	public static enum AC_STATE {CREATED, INITIALIZED, WAITING_BEGIN, PROCESSING, NEED_ACTING, WAITING_RESPONSE, TERMINATED}
	// EventServer States
	public static enum ES_STATE {CREATED, CONFIGURED, INITIALIZED, WAITING_BEGIN, WAITING_AGENTS, PROCESSING, SENDING_RESPONSE, TERMINATED}

	//
	public static final String REP_TYPE_INPUT			= "INPUT";
	public static final String REP_TYPE_OUTPUT			= "OUTPUT";
	public static final String REP_TYPE_PARAMETERS  	= "PARAMETERS";
	
	public static final String SUF_AUXILIAR_MEMORY		= "AUX";

}
