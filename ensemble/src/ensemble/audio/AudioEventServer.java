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

package ensemble.audio;

import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import ensemble.Constants;
import ensemble.Event;
import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.clock.TimeUnit;
import ensemble.memory.AudioMemory;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.movement.MovementLaw;
import ensemble.movement.MovementState;
import ensemble.world.Vector;
import ensemble.world.World;

// TODO: Auto-generated Javadoc
/**
 * The Class AudioEventServer.
 */
public class AudioEventServer extends EventServer {

	// Log
//	public static Logger logger = Logger.getMyLogger(MusicalAgent.class.getName());

    /**
	 * The Enum INTERPOLATION_MODE.
	 */
	private enum INTERPOLATION_MODE {/** The none. */
NONE, /** The linear. */
 LINEAR, /** The polynomial. */
 POLYNOMIAL;
    	
	    /**
	     * From string.
	     *
	     * @param str the str
	     * @return the interpolation mode
	     */
	    public static INTERPOLATION_MODE fromString(String str) {
    		if (str.equals("NONE")) {
				return NONE;
    		} else if (str.equals("LINEAR")) {
				return LINEAR;
    		} else if (str.equals("POLYNOMIAL")) {
				return POLYNOMIAL;
    		} else {
				return LINEAR;
			}
    	}
    };

    //---- WORK VARIABLES ----
	// Newton function's variables 
	/** The f_res. */
    double[] f_res	= new double[2];
	
	/** The f. */
	double[] f		= new double[2];
	
	/** The fl. */
	double[] fl	 	= new double[2];
	
	/** The fh. */
	double[] fh 	= new double[2];
	// States
	/** The rcv_state. */
	MovementState 	rcv_state;
	
	/** The src_state. */
	MovementState 	src_state;
	
	/** The vec_aux. */
	Vector 			vec_aux;
	
	/** The vec_aux_2. */
	Vector 			vec_aux_2;
	
	/** The rcv_comp_pos. */
	Vector 			rcv_comp_pos;
	
	/** The src_comp_pos. */
	Vector 			src_comp_pos;
	
	// Utilizado para comparar o tempo (ajustar de acordo com a precis‹o desejada), em segundos
	/** The epsilon. */
	private final double 	EPSILON 		= 1E-6;
	
	/** The max iterations. */
	private final int 		MAX_ITERATIONS 	= 10;

	// AudioEventServer Parameters
	/** The Constant PARAM_MASTER_GAIN. */
	private static final String PARAM_MASTER_GAIN = "MASTER_GAIN";
	
	/** The Constant PARAM_SPEED_SOUND. */
	private static final String PARAM_SPEED_SOUND = "SPEED_SOUND";
	
	/** The Constant PARAM_REFERENCE_DISTANCE. */
	private static final String PARAM_REFERENCE_DISTANCE = "REFERENCE_DISTANCE";
	
	/** The Constant PARAM_ROLLOFF_FACTOR. */
	private static final String PARAM_ROLLOFF_FACTOR = "ROLLOFF_FACTOR";
	
	/** The Constant PARAM_SAMPLE_RATE. */
	private static final String PARAM_SAMPLE_RATE = "SAMPLE_RATE";
	
	/** The Constant PARAM_LOOP_HEARING. */
	private static final String PARAM_LOOP_HEARING = "LOOP_HEARING";
	
	/** The Constant PARAM_INTERPOLATION_MODE. */
	private static final String PARAM_INTERPOLATION_MODE = "INTERPOLATION_MODE";
	
	/** The Constant PARAM_NUMBER_POINTS. */
	private static final String PARAM_NUMBER_POINTS = "NUMBER_POINTS";
	
	/** The master_gain. */
	private double 			master_gain 		= 1.0;
	
	/** The speed_sound. */
	private double			speed_sound			= 343.3; // speed of sound (m/s)
	
	/** The reference_distance. */
	private double 			reference_distance 	= 1.0;
	
	/** The rolloff_factor. */
	private double 			rolloff_factor 		= 1.0;
    
    /** The sample_rate. */
    private int 			sample_rate 		= 44100;
    
    /** The interpolation_mode. */
    private INTERPOLATION_MODE 	interpolation_mode 	= INTERPOLATION_MODE.LINEAR;
    
    /** The number_points. */
    private int 			number_points		= 3;
    
    /** The loop_hearing. */
    private boolean 		loop_hearing 		= false;

    // Working variables
    /** The step. */
    private double 			step 				= 1 / sample_rate;
    
    /** The chunk_size. */
    private int 			chunk_size 			= 4410;

    // When a parameter has changed, only applies it in the next cycle 
    /** The param_changed. */
    private boolean 		param_changed 		= false;

    // Table that stores the last calculated delta of each pair
//    double[] deltas, deltas_1, deltas_2, deltas_3;
    /** The deltas. */
    double[] deltas;
    
    /** The last_deltas. */
    private HashMap<String, Double> last_deltas = new HashMap<String, Double>();
    
    // Table that stores sent audio chunks
    /** The memories. */
    private HashMap<String, Memory> memories = new HashMap<String, Memory>();

	// Descrição do mundo
	/** The world. */
	private World world;

	/** The movement present. */
	private boolean 	movementPresent = false;
	
	/** The mov law. */
	private MovementLaw movLaw;
	
//	// Performance
//	int number_of_frames;
//	long proc_time_1, proc_time_2, proc_time_3;
//	PrintWriter file_perf, file_perf_1, file_perf_2, file_perf_3;
	
	/* (non-Javadoc)
 * @see ensemble.LifeCycle#configure()
 */
@Override
	public boolean configure() {
		setEventType(AudioConstants.EVT_TYPE_AUDIO);
		if (parameters.containsKey(Constants.PARAM_COMM_CLASS)) {
			setCommType(parameters.get(Constants.PARAM_COMM_CLASS));
		} else {
			setCommType("ensemble.comm.direct.CommDirect");
		}
		if (parameters.containsKey(Constants.PARAM_PERIOD)) {
			String[] str = (parameters.get(Constants.PARAM_PERIOD)).split(" ");
			setEventExchange(Integer.valueOf(str[0]), Integer.valueOf(str[1]), Integer.valueOf(str[2]), Integer.valueOf(str[3]));
		} else {
			setEventExchange(500, 200, 400, 1000);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#init()
	 */
	@Override
	public boolean init() {

	    // Inicialização dos parâmetros
		this.master_gain		= Double.valueOf(parameters.get(PARAM_MASTER_GAIN, "1.0"));
		this.speed_sound		= Double.valueOf(parameters.get(PARAM_SPEED_SOUND, "343.3"));
		this.reference_distance = Double.valueOf(parameters.get(PARAM_REFERENCE_DISTANCE, "1.0"));
		this.rolloff_factor 	= Double.valueOf(parameters.get(PARAM_ROLLOFF_FACTOR, "1.0"));
		this.number_points 		= Integer.valueOf(parameters.get(PARAM_NUMBER_POINTS, "3"));
		this.interpolation_mode = INTERPOLATION_MODE.fromString(parameters.get(PARAM_INTERPOLATION_MODE, "LINEAR"));
		this.loop_hearing 		= Boolean.valueOf(parameters.get(PARAM_LOOP_HEARING, "FALSE"));
		this.sample_rate 		= Integer.valueOf(parameters.get(PARAM_SAMPLE_RATE, "44100"));
		
		// Chunk size deve ser baseado na freqüência
		// TODO Cuidado com aproximações aqui!
		this.step 				= 1 / (double)sample_rate;
		this.chunk_size 		= (int)Math.round(sample_rate * ((double)period / 1000));
		this.deltas 			= new double[chunk_size];
//		System.out.printf("%d %f %d\n", SAMPLE_RATE, STEP, CHUNK_SIZE);
		
		this.world = envAgent.getWorld();

		// Verifies if there is a MovementEventServer and a MovementLaw
		this.movLaw = (MovementLaw)world.getLaw(MovementConstants.EVT_TYPE_MOVEMENT);
		if (envAgent.getEventServer(MovementConstants.EVT_TYPE_MOVEMENT) != null && movLaw != null) {
			movementPresent = true;
		} else {
			movementPresent = false;
		}
		rcv_state = new MovementState(world.dimensions);
		src_state = new MovementState(world.dimensions);
		vec_aux = new Vector(world.dimensions);
		vec_aux_2 = new Vector(world.dimensions);
		
//		try {
//			out = new PrintWriter(new BufferedWriter(new FileWriter("foo.out")));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		buffer = new StringBuilder(50);
//		try {
//			file_perf = new PrintWriter(new FileOutputStream("out_perf.txt"), false);
//			file_perf_1 = new PrintWriter(new FileOutputStream("out_perf_1.txt"), false);
//			file_perf_2 = new PrintWriter(new FileOutputStream("out_perf_2.txt"), false);
//			file_perf_3 = new PrintWriter(new FileOutputStream("out_perf_3.txt"), false);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		
		return true;

	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#actuatorRegistered(java.lang.String, java.lang.String, ensemble.Parameters)
	 */
	@Override
	public Parameters actuatorRegistered(String agentName, String eventHandlerName, Parameters userParam) {
		
		Parameters retParameters = new Parameters();
		retParameters.put(Constants.PARAM_CHUNK_SIZE, String.valueOf(chunk_size));
		retParameters.put(Constants.PARAM_SAMPLE_RATE, String.valueOf(sample_rate));
		retParameters.put(Constants.PARAM_STEP, String.valueOf(step));
		retParameters.put(Constants.PARAM_PERIOD, String.valueOf(period));
		retParameters.put(Constants.PARAM_START_TIME, String.valueOf(startTime));
		
		// Cria uma memória para o atuador
		Memory memory;
		try {
			// Criar a instância do componente
			Class esClass = Class.forName("ensemble.memory.AudioMemory");
			memory = (Memory)esClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		Parameters memParameters = new Parameters();
		String memoryName = agentName+":"+eventHandlerName;
		memParameters.put(Constants.PARAM_MEMORY_NAME, memoryName);
		memParameters.put(Constants.PARAM_MEMORY_PAST, "1.0");
		memParameters.put(Constants.PARAM_MEMORY_FUTURE, "1.0");
		memParameters.put(Constants.PARAM_PERIOD, String.valueOf(period));
		memParameters.put(Constants.PARAM_START_TIME, String.valueOf(startTime));
		memParameters.put(Constants.PARAM_STEP, String.valueOf(step));
		memory.setParameters(memParameters);
		memory.setAgent(envAgent);
		memory.configure();
		memory.start();
		
		memories.put(memoryName, memory);

		return retParameters;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#sensorRegistered(java.lang.String, java.lang.String, ensemble.Parameters)
	 */
	@Override
	public Parameters sensorRegistered(String agentName, String eventHandlerName, Parameters userParam) throws Exception {
		
		Parameters userParameters = new Parameters();
		userParameters.put(Constants.PARAM_CHUNK_SIZE, String.valueOf(chunk_size));
		userParameters.put(Constants.PARAM_SAMPLE_RATE, String.valueOf(sample_rate));
		userParameters.put(Constants.PARAM_STEP, String.valueOf(step));
		userParameters.put(Constants.PARAM_PERIOD, String.valueOf(period));
		userParameters.put(Constants.PARAM_START_TIME, String.valueOf(startTime));

		// Cria uma memória para o atuador
		Memory memory;
		try {
			// Criar a instância do componente
			Class esClass = Class.forName("ensemble.memory.AudioMemory");
			memory = (Memory)esClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		Parameters memParameters = new Parameters();
		String memoryName = agentName+":"+eventHandlerName;
		memParameters.put(Constants.PARAM_MEMORY_NAME, memoryName);
		memParameters.put(Constants.PARAM_MEMORY_PAST, "1.0");
		memParameters.put(Constants.PARAM_MEMORY_FUTURE, "1.0");
		memParameters.put(Constants.PARAM_PERIOD, String.valueOf(period));
		memParameters.put(Constants.PARAM_START_TIME, String.valueOf(startTime));
		memParameters.put(Constants.PARAM_STEP, String.valueOf(step));
		memory.setParameters(memParameters);
		memory.setAgent(envAgent);
		memory.configure();
		memory.start();
		memories.put(memoryName, memory);
		
		return userParameters;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		if (name.equals(PARAM_MASTER_GAIN)) {
			this.master_gain 		= Double.valueOf(newValue);
		}
		else if (name.equals(PARAM_SAMPLE_RATE)) {
			// TODO Verifies if it's a valid sample rate
			this.sample_rate 		= Integer.valueOf(newValue); 
			this.step 				= 1 / (double)sample_rate;
			this.chunk_size 		= (int)Math.round(sample_rate * ((double)period / 1000));
			this.deltas 			= new double[chunk_size];
//			System.out.printf("%d %f %d\n", SAMPLE_RATE, STEP, CHUNK_SIZE);
		} 
		else if (name.equals(PARAM_SPEED_SOUND)) {
			this.speed_sound		= Double.valueOf(newValue);
		} 
		else if (name.equals(PARAM_REFERENCE_DISTANCE)) {
			this.reference_distance = Double.valueOf(newValue);
		} 
		else if (name.equals(PARAM_ROLLOFF_FACTOR)) {
			this.rolloff_factor 	= Double.valueOf(newValue);
			System.out.println("ATUALIZEI: " + rolloff_factor);
		} 
		else if (name.equals(PARAM_LOOP_HEARING)) {
			this.loop_hearing 		= Boolean.valueOf(newValue);
		} 
		else if (name.equals(PARAM_INTERPOLATION_MODE)) {
			this.interpolation_mode = INTERPOLATION_MODE.fromString(newValue);
		} 
		else if (name.equals(PARAM_NUMBER_POINTS)) {
			this.number_points 	= Integer.valueOf(newValue);
		} 
		else {
			return false;
		}
		param_changed = true;
		return true;
	}
	
	// TODO Verificar problemas de concorrência!
	/* (non-Javadoc)
	 * @see ensemble.EventServer#processSense(ensemble.Event)
	 */
	@Override
	public void processSense(Event evt) {

		// TODO Tratar depois o que acontece quando muda o tamanho do chunk
//		System.out.println("Inseri na tabela - frame = " + workingFrame + " - pos = " + state.position);
		Memory mem = memories.get(evt.oriAgentName+":"+evt.oriAgentCompName);
		try {
			mem.writeMemory(evt.objContent, evt.instant, evt.duration, TimeUnit.SECONDS);
//			System.out.println("Recebi um evento " + evt.instant + " " + evt.duration);
		} catch (MemoryException e) {
			e.printStackTrace();
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#process()
	 */
	@Override
	public void process() {
		
//		long time_process = System.nanoTime();

		if (param_changed) {
			// TODO What is the best way to implement that?
			param_changed = false;
		}
		
		// TODO Ver se vamos trabalhar com milisegundos ou segundos
		double instant = (double)(startTime + workingFrame * period) / 1000;

//		System.out.println("SENSORS = " + sensors.size() + " - ACTUATORS = " + actuators.size());
		for (Enumeration<String> s = sensors.keys(); s.hasMoreElements();) {
			
			String s_key = s.nextElement();
			String[] sensor = s_key.split(":");
			rcv_comp_pos = Vector.parse(sensors.get(s_key).get(Constants.PARAM_REL_POS, "(0;0;0)"));

			// Cria o evento a ser enviado para o sensor
			Event evt = new Event();
			evt.destAgentName = sensor[0];
			evt.destAgentCompName = sensor[1];
			double[] buf = new double[chunk_size];
			evt.objContent = buf;
			evt.instant = instant;
			evt.duration = (double)(chunk_size * step);
			
			// Calculates the contribution of each sound source
			for (Enumeration<String> a = actuators.keys(); a.hasMoreElements();) {
				
				String a_key = a.nextElement();
				String pair = s_key + "<>" + a_key;
				src_comp_pos = Vector.parse(actuators.get(a_key).get(Constants.PARAM_REL_POS, "(0;0;0)"));
				
				AudioMemory mem = (AudioMemory)memories.get(a_key);

				String[] actuator = a_key.split(":");
				
				// If it's the same agent, 
				if (actuator[0].equals(sensor[0]) && loop_hearing) {
					double[] buf_act = (double[])mem.readMemory(instant, (double)(chunk_size * step), TimeUnit.SECONDS);
					for (int i = 0; i < buf.length; i++) {
						buf[i] =+ master_gain * buf_act[i];
					}
				}
				// Else, simulates the propagation of sound
				else {
					
					double t;
					
					if (movementPresent) {
					
						// Gets the movement memory
						Memory mem_mov_src = (Memory)world.getEntityStateAttribute(actuator[0], MovementConstants.EVT_TYPE_MOVEMENT);
						Memory mem_mov_rcv = (Memory)world.getEntityStateAttribute(sensor[0], MovementConstants.EVT_TYPE_MOVEMENT);

						// TODO Must use the POSITION attribute instead of giving up the pair 
						if (mem_mov_rcv != null && mem_mov_src != null) {
						
							// Guess
							double guess;
							if (last_deltas.containsKey(pair)) {
								guess = last_deltas.get(pair);
							} else {
								// Only runs the first time
								MovementState src_state_old = (MovementState)mem_mov_src.readMemory(instant, TimeUnit.SECONDS);
								movLaw.changeState(src_state_old, instant, src_state);
		//				    	System.out.println("src_state = " + src_state.position);
								
								MovementState rcv_state_old = (MovementState)mem_mov_rcv.readMemory(instant, TimeUnit.SECONDS);
								movLaw.changeState(rcv_state_old, instant, rcv_state);
		//				    	System.out.println("rcv_state = " + rcv_state.position);
		
								// Adjusts the position according to the component relative position to the center of the agent
								src_state.position.add(src_comp_pos);
								rcv_state.position.add(rcv_comp_pos);
								
								double distance = src_state.position.getDistance(rcv_state.position);
								guess = distance / speed_sound;
		//						System.out.println("initial guess for " + pair + " = " + guess);
							}
		
							// Finds the deltas for all the samples in the chunk, according to the chosen process mode
							double delta = 0.0, delta_i = 0.0, delta_f = 0.0;
		
							switch (interpolation_mode) {
							case NONE:
		//						long start = System.nanoTime();
								// For each sample...
								for (int j = 0; j < chunk_size; j++) {
									t = instant + (j * step);
									delta = newton_raphson(mem_mov_src, mem_mov_rcv, t, guess, 0.0, mem_mov_src.getPast());
									if (delta < 0.0) {
										System.err.println("[ERROR] delta = " + delta);
										delta = 0.0;
									}
									deltas[j] = delta;
									guess = delta;
								}
		//						proc_time_1 = System.nanoTime() - start;
								break;
							case POLYNOMIAL:
		//						start = System.nanoTime();
								double[] xa = new double[number_points]; 
								double[] ya = new double[number_points]; 
		
								// calculates points for the polynomial interpolation
								xa[0] = instant;
								ya[0] = newton_raphson(mem_mov_src, mem_mov_rcv, xa[0], guess, 0.0, mem_mov_src.getPast());
								int samples_jump = chunk_size / number_points;
								for (int i = 1; i < number_points-1; i++) {							
									xa[i] = instant + (i * samples_jump * step);
									ya[i] = newton_raphson(mem_mov_src, mem_mov_rcv, xa[i], ya[i-1], 0.0, mem_mov_src.getPast());
								}
								xa[number_points-1] = instant + ((chunk_size-1) * step);
								ya[number_points-1] = newton_raphson(mem_mov_src, mem_mov_rcv, xa[number_points-1], ya[number_points-2], 0.0, mem_mov_src.getPast());
		
								// For each sample in this division...
								for (int i = 0; i < chunk_size; i++) {
									t = instant + (i * step);
									delta = polint(xa, ya, t);
									deltas[i] = delta;
								}
		//						proc_time_2 = System.nanoTime() - start;
								break;
							case LINEAR:
		//						start = System.nanoTime();
								// first delta of the chunk
								delta_i = newton_raphson(mem_mov_src, mem_mov_rcv, instant, guess, 0.0, mem_mov_src.getPast());
								delta_f = newton_raphson(mem_mov_src, mem_mov_rcv, (instant + ((chunk_size-1) * step)), delta_i, 0.0, mem_mov_src.getPast());
								// For each sample in this division...
								for (int i = 0; i < chunk_size; i++) {
									t = instant + (i * step);
									delta = delta_i + ((t-instant)/((chunk_size-1) * step))*(delta_f-delta_i);
									deltas[i] = delta;
								}
		//						proc_time_3 = System.nanoTime() - start;
								break;
							default:
								break;
							}
		
		//					file_perf.printf("proc_time %f %f %f\n", ((double)proc_time_1/1000000), ((double)proc_time_2/1000000), ((double)proc_time_3/1000000));
							
							// Fills the buffer
							for (int i = 0; i < chunk_size; i++) {
								t = instant + (i * step);
								double gain = Math.min(1.0, reference_distance / (reference_distance + rolloff_factor * ((deltas[i] * speed_sound) - reference_distance)));
								double value = 0.0;
								value = (Double)mem.readMemory(t-deltas[i], TimeUnit.SECONDS);
								buf[i] += (value * gain * master_gain);
//								for (int j = 0; j < 4; j++) {
//									System.out.printf("%.3f ", buf[j]);
//								}
//								System.out.println();
								// Performance
	//							MovementState rcv_state_old = (MovementState)mem_mov_src.readMemory(t-deltas_1[i], TimeUnit.SECONDS);
	//							movLaw.changeState(rcv_state_old, instant, rcv_state);
	//							file_perf.printf("%d %f %.10f %.10f %.10f\n", i, t, deltas_1[i], deltas_2[i], deltas_3[i]);
							}
							
							// Stores the last delta for the next computation
							last_deltas.put(pair, deltas[deltas.length-1]);
							
	//						// Performance
	//						file_perf.flush();
							
						}

					}
					// There is no Movement Event Server or Law present
					else {
						// Gets static positions from the world and
						// adjusts the position according to the component relative position to the center of the agent
						((Vector)world.getEntityStateAttribute(actuator[0], "POSITION")).copy(vec_aux);
						vec_aux.add(src_comp_pos);
						((Vector)world.getEntityStateAttribute(sensor[0], "POSITION")).copy(vec_aux_2);
						vec_aux_2.add(rcv_comp_pos);
						// Calculates the delay between them
						double delta = (vec_aux.getDistance(vec_aux_2)) / speed_sound;
						double gain = Math.min(1.0, reference_distance / (reference_distance + rolloff_factor * ((delta * speed_sound) - reference_distance)));
						for (int i = 0; i < chunk_size; i++) {
							t = instant + (i * step);
							double value = (Double)mem.readMemory(t-delta, TimeUnit.SECONDS);
							buf[i] += (value * gain * master_gain);
						}
					}
				
				}
			}
			
			// Puts the newly created event in the output queue
//			addOutputEvent(evt.destAgentName, evt.destAgentCompName, evt);
			sendEvent(evt);
			
		}
		
//		System.out.printf("AS time = %.3f \t(t = %.3f)\n", ((double)(System.nanoTime()-time_process)/1000000), instant);
//		System.out.printf("%.5f\n", ((double)(System.nanoTime()-time_process)/1000000));		

	}

    /**
     * Function.
     *
     * @param src_state the src_state
     * @param rcv_state the rcv_state
     * @param t the t
     * @param delta the delta
     */
    private void function(MovementState src_state, MovementState rcv_state, double t, double delta) {
    	
    	if (src_state == null || rcv_state == null) {
			// Se é null, é porque o agente não existia nesse momento
    		System.err.println("WARNING: Tentou buscar amostra no futuro ou antes do início da simulação (" + (t - delta) + ")");
    		f_res[0] = 0; f_res[1] = 0;
    	}
    	
    	Vector q = rcv_state.position;
    	Vector p = src_state.position;
    	Vector v = src_state.velocity;
    	
    	f_res[0] 	= (q.magnitude * q.magnitude) - 2 * q.dotProduct(p) + (p.magnitude * p.magnitude) - (delta * delta * speed_sound * speed_sound);
    	p.copy(vec_aux);
    	vec_aux.subtract(q);
    	f_res[1] 	= 2 * v.dotProduct(vec_aux) - (2 * delta * speed_sound * speed_sound);

    }
    
    /**
     * Newton_raphson.
     *
     * @param mem_src the mem_src
     * @param mem_rcv the mem_rcv
     * @param t the t
     * @param initial_guess the initial_guess
     * @param x1 the x1
     * @param x2 the x2
     * @return the double
     */
    private double newton_raphson(Memory mem_src, Memory mem_rcv, double t, double initial_guess, double x1, double x2) {
    	    	
    	double dx, dx_old, rts, xl, xh, temp;
    	MovementState rcv_state_old, src_state_old;

//    	System.out.println("newton() - t = " + t + " - guess = " + initial_guess);
    	
		rcv_state_old = (MovementState)mem_rcv.readMemory(t, TimeUnit.SECONDS);
		movLaw.changeState(rcv_state_old, t, rcv_state);
		rcv_state.position.add(rcv_comp_pos); // Component relative position

		src_state_old = (MovementState)mem_src.readMemory(t-x1, TimeUnit.SECONDS);
		movLaw.changeState(src_state_old, t-x1, src_state);
		src_state.position.add(src_comp_pos); // Component relative position
    	function(src_state, rcv_state, t, x1);
    	fl[0] = f_res[0]; fl[1] = f_res[1];
    	
		src_state_old = (MovementState)mem_src.readMemory(t-x2, TimeUnit.SECONDS);
		movLaw.changeState(src_state_old, t-x2, src_state);
		src_state.position.add(src_comp_pos); // Component relative position
		function(src_state, rcv_state, t, x2);
    	fh[0] = f_res[0]; fh[1] = f_res[1];
    	
    	if ((fl[0] > 0.0 && fh[0] > 0.0) || (fl[0] < 0.0 && fh[0] < 0.0)) {		
//    		System.err.println("Root must be bracketed in rtsafe");
    		return -1;
    	}
		if (fl[0] == 0.0) {
			return x1;
		}
		if (fh[0] == 0.0) { 
			return x2;
		}
		// Orient the search so that f(xl) < 0.0
		if (fl[0] < 0.0) {
			xl = x1;
			xh = x2;
		} else {
			xh = x1;
			xl = x2;
		}
		// Initialize the guess for root, the stepsize before last, and the last step
    	rts = initial_guess;
		dx_old = Math.abs(x2-x1);
		dx = dx_old;
		src_state_old = (MovementState)mem_src.readMemory(t-rts, TimeUnit.SECONDS);
//		System.out.printf("fui buscar o instante %f e retornou %f\n", t-rts, src_state_old.instant);
		movLaw.changeState(src_state_old, t-rts, src_state);
//		System.out.printf("src_state(%f)=%f\n", t-rts, src_state.position.getValue(0));
		src_state.position.add(src_comp_pos); // Component relative position
		function(src_state, rcv_state, t, rts);
    	f[0] = f_res[0]; f[1] = f_res[1];
    	if (f == null) {
    		System.err.println("WARNING: newton tried to search a sample before the begining of the simulation or in the future (" + (t - rts) + ")");
    		return 0.0;
    	}
    	// Loop over allowed iterations
		boolean found = false;
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			// Bisect if Newton out of range, or not decreasing fast enough
			if ((((rts-xh)*f[1]-f[0])*((rts-xl)*f[1]-f[0]) >= 0.0)
				|| (Math.abs(2.0*f[0]) > Math.abs(dx_old*f[1]))) {
				dx_old = dx;
				dx = 0.5 * (xh - xl);
				rts = xl + dx;
				// Change in root is negligible
				if (xl == rts) {
					found = true;
					break;
				}
			// Newton step acceptable. Take it.
			} else {
				dx_old = dx;
	        	dx = f[0] / f[1];
	        	temp = rts;
	        	rts -= dx;
	        	if (temp == rts) {
					found = true;
					break;

	        	}
			}
			// Convergence criterion
        	if (Math.abs(dx) < EPSILON) {
				found = true;
				break;
        	}
        	// The one new function evaluation per iteration
    		src_state_old = (MovementState)mem_src.readMemory(t-rts, TimeUnit.SECONDS);
//    		System.out.printf("fui buscar o instante %f e retornou %f\n", t-rts, src_state_old.instant);
    		movLaw.changeState(src_state_old, t-rts, src_state);
//    		System.out.printf("src_state(%f)=%f\n", t-rts, src_state.position.getValue(0));
    		src_state.position.add(src_comp_pos); // Component relative position
    		function(src_state, rcv_state, t, rts);
        	f[0] = f_res[0]; f[1] = f_res[1];
    		// Maintain the bracket on the root
    		if (f[0] < 0.0) {
    			xl = rts;
    		} else {
    			xh = rts;
    		}
    	}

		if (!found) {
 			// TODO Está chegando nesse ponto em alguns casos!!!
//			System.err.println("WARNING: Maximum number of iterations exceeded in rtsafe (t = " + t + ") - delta = " + rts);
		}
    	
		return rts;
    }

    /**
     * Polint.
     *
     * @param xa the xa
     * @param ya the ya
     * @param x the x
     * @return the double
     */
    private double polint(double[] xa, double[] ya, double x) {
    	
    	double y, dy;
    	int ns = 0;
    	double den, dif, dift, ho, hp, w;
    	double[] c, d;
    	
    	dif = Math.abs(x-xa[0]);
    	// TODO Otimizar e criar antes
    	int n = xa.length;
    	c = new double[n];
    	d = new double[n];
    	
    	// Here we find the index ns of the closest table entry 1 = true
    	for (int i = 0; i < n; i++) {
    		dift = Math.abs(x-xa[i]);
    		if (dift < dif) {
				ns = i;
				dif = dift;
			}
			// Initializes the tableau of c's and d's
			c[i] = ya[i];
			d[i] = ya[i];
		}
    	y = ya[ns--];
    	for (int m = 1; m < n; m++) {
			for (int i = 0; i < n-m; i++) {
				ho = xa[i] - x;
				hp = xa[i+m] - x;
				w = c[i+1] - d[i];
				if ((den=ho-hp) == 0.0) {
					System.err.println("Error in routine polint!");
				}
				den = w / den;
				d[i] = hp * den;
				c[i] = ho * den;
			}
			y += (dy = (2 *(ns+1) < (n-m) ? c[ns+1] : d[ns--]));
		}
    	
    	return y;
    	
    }

}
