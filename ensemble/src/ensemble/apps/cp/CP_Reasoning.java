package ensemble.apps.cp;

import java.util.ArrayList;
import java.util.Collections;

import ensemble.Actuator;
import ensemble.Command;
import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Parameters;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.audio.AudioConstants;
import ensemble.audio.file.AudioInputFile;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;
import ensemble.movement.MovementConstants;
import ensemble.processing.Processor;
import ensemble.processing.ProcessorFactory;
import ensemble.processing.ProcessorFactory.AudioOperation;
import ensemble.world.Vector;


// TODO: Auto-generated Javadoc
/**
 * The Class CP_Reasoning.
 */
public class CP_Reasoning extends Reasoning {

	// Audio
	/** The mouth. */
	Actuator 	mouth;
	
	/** The mouth memory. */
	Memory 		mouthMemory;
	
	/** The ear. */
	Sensor 		ear;
	
	/** The ear memory. */
	Memory 		earMemory;
	
	/** The chunk_size. */
	int 		chunk_size;

	// Movement
	/** The legs. */
	Actuator	legs;
	
	/** The legs memory. */
	Memory 		legsMemory;
	
	/** The eyes. */
	Sensor 		eyes;
	
	/** The eyes memory. */
	Memory 		eyesMemory;
	
	// Reasoning state
	/**
	 * The Enum ReasoningState.
	 */
	enum ReasoningState {
		
		/** The not defined. */
		NOT_DEFINED,
		
		/** The listening. */
		LISTENING,
		
		/** The analysing. */
		ANALYSING,
		
		/** The playing. */
		PLAYING,
		
		/** The error. */
		ERROR
	}
	
	/** The state. */
	ReasoningState state = ReasoningState.NOT_DEFINED;
	
	// Audio file - wavetable
	/** The in. */
	private AudioInputFile 	in;
	
	/** The sample rate. */
	private float 			sampleRate;
	
	/** The wavetable. */
	private double[] 		wavetable;
	
	/** The wavetable_with_gain. */
	private double[] 		wavetable_with_gain;
	
	/** The wavetable_gain. */
	private double 			wavetable_gain = 2.0;
	
	// Audio Processor - Onset Detection
	/** The onsetproc. */
	private Processor 		onsetproc;
	
	// Common Variables
	/** The number_beats. */
	int 	number_beats;
	
	/** The frame_duration. */
	double 	frame_duration;
	
	/** The measure_duration. */
	double 	measure_duration;
	
	/** The beat_duration. */
	double 	beat_duration;
	
	/** The master. */
	boolean master = false;
	
	// Play variables
	/** The start_time. */
	double 		start_time;
	
	/** The start_playing_time. */
	double 		start_playing_time;
	
	/** The pattern. */
	boolean[] 	pattern;
	
	/** The beats. */
	ArrayList<Double> beats = new ArrayList<Double>();
	
	/** The next_sample_play. */
	int 		next_sample_play = 0;
	
	/** The measure_counter. */
	int 		measure_counter = 0;
	
	/** The phase. */
	int 		phase = 0;
	
	/** The slide. */
	int 		slide = 0;
	
	/** The last_phase. */
	int 		last_phase = 0;
	
	/** The actual_phase. */
	int 		actual_phase = 0;

	/** The master_position. */
	Vector 		master_position = new Vector(0,0,0);
	
	/** The start_position. */
	Vector 		start_position = null;
	
	/** The radius. */
	double 		radius;
	
	// Listening Variables;
	/** The detected_beats_time. */
	ArrayList<Double> detected_beats_time = new ArrayList<Double>();
	
	/** The detected_beats_energy. */
	ArrayList<Double> detected_beats_energy = new ArrayList<Double>();
	
	/** The error. */
	double error = 0.05; // porcentagem permitida na diferença entre o onset e o suposto lugar da batida
	
	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {
		
		// Gets and checks arguments from the KB
		String arg_filename = getAgent().getKB().readFact("wavetable");
		if (arg_filename == null) {
			System.err.println("wavetable fact does not exists!");
			return false;
		}
		String arg_role 	= getAgent().getKB().readFact("role");
		if (arg_role == null || !(arg_role.equals("master") || arg_role.equals("slave"))) {
			System.err.println("'role' fact does not exists or is neither 'master', nor 'slave'!");
			return false;
		}
		if (arg_role.equals("master")) {
			master = true;
		}
		
		String arg_bpm 		= getAgent().getKB().readFact("bpm");
		try {
			beat_duration = 60 / Double.parseDouble(arg_bpm);
		} catch (Exception e) {
			System.err.println("'bpm' argument does not exists or is not a valid number! Using 120 as default.");
			beat_duration = 60 / 120;
		}
		String arg_pattern 	= getAgent().getKB().readFact("pattern");
		
		if (arg_role.equals("slave")) {
			String arg_phase 	= getAgent().getKB().readFact("phase");
			try {
				phase = Integer.parseInt(arg_phase);
			} catch (Exception e) {
				System.err.println("'phase' argument is not a valid number!");
			}
			String arg_slide 	= getAgent().getKB().readFact("slide");
			try {
				slide = Integer.parseInt(arg_slide);
			} catch (Exception e) {
				System.err.println("'slide' argument is not a valid number!");
			}
		}
		
		if (start_position == null) {
			start_position = Vector.parse(getAgent().getParameter(Constants.PARAM_POSITION));
			radius = start_position.getDistance(master_position);
		}
		
		// Opens the audio file and imports it as a wavetable
		try {
			in = new AudioInputFile(arg_filename, true);
			long samples = in.getNumberSamples();
			sampleRate = in.getSampleRate();
			// TODO Fazer checagens de tamanho do arquivo
			wavetable = in.readNextChunk((int)samples);
			wavetable_with_gain = in.readNextChunk((int)samples);
			for (int i = 0; i < wavetable.length; i++) {
				wavetable_with_gain[i] = wavetable_gain * wavetable[i];
			}
		} catch (Exception e) {
//			getAgent().logger.severe("[" + getComponentName() + "] " + "Error in opening the file " + arg_filename);
			System.err.println("[" + getComponentName() + "] " + "Error in opening the file " + arg_filename);
			return false;
		}
		
		// Configures the agente after its role
		if (arg_role.equals("master")) {
		
			// If the pattern was not given, creates one randomnly
			if (arg_pattern == null) {
				// From 5 to 16 beats
				number_beats = 5 + (int)(Math.round(Math.random()*11));
				pattern = new boolean[number_beats];
				pattern[0] = true; // Primeira batida sempre deve existir
				String str = "[MASTER] Random pattern = [1";
				for (int i = 1; i < pattern.length; i++) {
					pattern[i] = (int)(Math.round(Math.random())) != 0 ? Boolean.TRUE : Boolean.FALSE;
					str = str + (pattern[i] ? "1" : "0");
				}
				System.out.println(str+"]");
			} else {
				char[] char_pattern = getAgent().getKB().readFact("pattern").toCharArray();
				number_beats = char_pattern.length;
				pattern = new boolean[number_beats];
				for (int i = 0; i < char_pattern.length; i++) {
					if (char_pattern[i] == '1') {
						pattern[i] = true;
					}
				}
			}
			
			// Builds the time array of the beats
			measure_duration = Math.round(beat_duration * number_beats * 10000)/10000.0;
			for (int i = 0; i < pattern.length; i++) {
				if (pattern[i]) {
					beats.add(Math.round(i * beat_duration * 10000)/10000.0);
				}
			}
			
			System.out.println("\twavetable_length = " + wavetable.length);
			System.out.println("\tbeat_duration = " + beat_duration);
			System.out.println("\tmeasure_duration = " + measure_duration);
			System.out.print("\tbeats = [");
			for (int i = 0; i < beats.size(); i++) {
				System.out.print(" " + beats.get(i));
			}
			System.out.println(" ]");

			state = ReasoningState.PLAYING;
			
		} else if (getAgent().getKB().readFact("role").equals("slave")) {

			// Creates de Onset Processor
			Parameters onset_args = new Parameters();
			onset_args.put("frame_size", "512");
			onset_args.put("sample_rate", "44100.0");
			onset_args.put("onset_output", "sample");
			onsetproc = ProcessorFactory.createAudioProcessor(AudioOperation.ONSET_DETECTION, onset_args);

			state = ReasoningState.ANALYSING;
			
		}
		
		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#eventHandlerRegistered(ensemble.EventHandler)
	 */
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		
		// Checar se é um atuador de som e adicionar na lista
		if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			mouth = (Actuator)evtHdl;
			mouth.registerListener(this);
			mouthMemory = getAgent().getKB().getMemory(mouth.getComponentName());
			chunk_size = Integer.parseInt(mouth.getParameter(Constants.PARAM_CHUNK_SIZE, "0"));
			start_time = Integer.parseInt(mouth.getParameter(Constants.PARAM_START_TIME, "0"));
			frame_duration = chunk_size / sampleRate;
			if (getAgent().getKB().readFact("role").equals("master")) {
				start_playing_time = ((double)start_time/1000) + 2*frame_duration + Math.random()*frame_duration;
				System.out.println("[CP] master start_playing_time = " + start_playing_time);
			}
			
		} else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(AudioConstants.EVT_TYPE_AUDIO)) {
			
			ear = (Sensor)evtHdl;
			ear.registerListener(this);
			earMemory = getAgent().getKB().getMemory(ear.getComponentName());
			
		} else if (evtHdl instanceof Actuator && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			
			legs = (Actuator)evtHdl;
			legs.registerListener(this);
			legsMemory = getAgent().getKB().getMemory(legs.getComponentName());
				
		} else if (evtHdl instanceof Sensor && evtHdl.getEventType().equals(MovementConstants.EVT_TYPE_MOVEMENT)) {
			
			eyes = (Sensor)evtHdl;
			eyes.registerListener(this);
			eyesMemory = getAgent().getKB().getMemory(eyes.getComponentName());
				
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#needAction(ensemble.Actuator, double, double)
	 */
	public void needAction(Actuator sourceActuator, double instant, double duration) {

		switch (state) {

		case PLAYING:
			
			if (!(start_playing_time > instant && start_playing_time > instant + duration)) {
				// Calcula os tempos referentes ao ínicio e fim do frame (em ms)
				double ti = (instant - start_playing_time) % measure_duration;
				ti = Math.round(ti * 10000)/10000.0;
				double tf = (ti + frame_duration) % measure_duration;
				tf = Math.round(tf * 10000)/10000.0;
//				System.out.println("instant = " + instant);
//				System.out.println("ti = " + ti);
//				System.out.println("tf = " + tf);

				// Verificar se existe alguma batida entre [ti,ti+frame_duration[
				for (int i = 0; i < beats.size(); i++) {
					double beat_value = beats.get(i);
					if (beat_value >= ti && beat_value < ti+frame_duration) {
						double tm = instant+beat_value-ti;
						tm = Math.round(tm * 10000)/10000.0;
						if (i==0) {
							try {
								mouthMemory.writeMemory(wavetable_with_gain, tm, duration, TimeUnit.SECONDS);
							} catch (MemoryException e1) {
//								MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
							}
						} else {
							try {
								mouthMemory.writeMemory(wavetable, tm, duration, TimeUnit.SECONDS);
							} catch (MemoryException e1) {
//								MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
							}
						}
//						System.out.println("tm = " + tm);
					}
				}
				
				// Mudança de compasso dentro deste frame
				if (tf < ti) {
					// atualiza o contador
					measure_counter++;
//					System.out.println("Tem começo de compasso!!!");
					// Fazer a mudança de fase, caso necessário
					if (!master && slide != 0 && measure_counter % slide == 0) {
						actual_phase = (actual_phase + phase) % number_beats;
						System.out.println("[" + getAgent().getAgentName() + "] slide = " + slide + " measure_counter = " + measure_counter + " actual_phase = " + actual_phase);
						// Reconstruir o array the beats
						int beats_size = beats.size();
						beats.clear();
						for (int i = 0; i < beats_size; i++) {
							int index = (i + actual_phase) % beats_size;
							double beat = detected_beats_time.get(index) - detected_beats_time.get(0);
							beats.add(Math.round(((beat + measure_duration - (actual_phase * beat_duration)) % measure_duration) * 10000)/10000.0);
							Collections.sort(beats);
						}
						System.out.print("\tbeats = [");
						for (int i = 0; i < beats.size(); i++) {
							System.out.print(" " + beats.get(i));
						}
						System.out.println(" ]");
					}
					// Verificar quais batidas pertencem
					for (int i = 0; i < beats.size(); i++) {
						double beat_value = beats.get(i);
						if (beat_value >= 0 && beat_value < tf) {
							// Procura o instante certo de inserir a wavetable neste frame
							double tm = instant+measure_duration+beat_value-ti;
							tm = Math.round(tm * 10000)/10000.0;
							if (i==0) {
								try {
									mouthMemory.writeMemory(wavetable_with_gain, tm, duration, TimeUnit.SECONDS);
								} catch (MemoryException e1) {
//									MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
								}
							} else {
								try {
									mouthMemory.writeMemory(wavetable, tm, duration, TimeUnit.SECONDS);
								} catch (MemoryException e1) {
//									MusicalAgent.logger.warning("[" + getAgent().getAgentName() + ":" + getComponentName() + "] " + "Não foi possível armazenar na memória");
								}
							}
//							System.out.println("tm(2) = " + tm);
						}
					}
				}
				
			}		
			
			// Acts
			mouth.act();
			
			break;
	
		}

//		System.out.println("REAS time = " + (System.currentTimeMillis() - start));
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#newSense(ensemble.Sensor, double, double)
	 */
	@Override
	public void newSense(Sensor sourceSensor, double instant, double duration) {

		if (sourceSensor == ear) {
		
			// Se estiver no modo de escuta, analisa o frame para achar as batidas
			switch (state) {
			
			case ANALYSING:
				
				// Pega o evento sonoro da base
				double[] buf = (double[])earMemory.readMemory(instant, duration, TimeUnit.SECONDS);
	
				// Onset
				Parameters onset_args = new Parameters();
				onset_args.put("start_instant", String.valueOf(instant));
				
				if (onsetproc != null) {
					long now = System.currentTimeMillis();
					Object out = onsetproc.process(onset_args, buf);
//					System.out.println("time = " + (System.currentTimeMillis() - now));
					
					// Beats
					if (out != null && out instanceof double[]) {
						double[] onset = (double[])out;
						if (onset.length > 0) {
							for (int i = 0; i < onset.length; i++) {
								// Add beat
								double beat = onset[i];
								detected_beats_time.add(instant + (beat/sampleRate));
							}
						}
					
						// ------
						// Updates the energy of each onset
						int ti = 0;
						int tf = buf.length;
						// If there is an older onset
						if (detected_beats_energy.size() > 0) {
							ti = 0;
							if (onset.length > 0) {
								tf = (int)onset[0];
							}
							if (ti != tf) {
								double energy = detected_beats_energy.get(detected_beats_energy.size()-1);
								for (int j = ti; j < tf; j++) {
									energy = energy + (buf[j] * buf[j]);
								}
		//						System.out.printf("rms residual = %.3f (ti=%f tf=%f)\n", energy, ti, tf);
								detected_beats_energy.set(detected_beats_energy.size()-1, energy);
							}
						}
						// For each new onset
						for (int i = 0; i < onset.length; i++) {
							ti = (int)onset[i];
							if (i < onset.length-1) {
								tf = (int)onset[i+1];
							} else {
								tf = buf.length;
							}
							double energy = 0;
							for (int j = ti; j < tf; j++) {
								energy = energy + (buf[j] * buf[j]);
							}
		//					System.out.printf("rms novo = %.3f (ti=%f tf=%f)\n", energy, ti, tf);
							detected_beats_energy.add(energy);
						}
					}
				}
				
				String str = "[" + getAgent().getAgentName() + "] time = [ ";
				for (int i = 0; i < detected_beats_time.size(); i++) {
					str = str + String.format("%f ", detected_beats_time.get(i));
				}
				System.out.println(str+"]");
				
				break;
				
			}
		
		}
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.Reasoning#process()
	 */
	public void process() throws Exception {

		switch (state) {

		case ANALYSING:

			// If there's more than one beat, we can start the search!
			if (detected_beats_energy.size() > 1) {
				double first_beat_energy = detected_beats_energy.get(0);
				int size = detected_beats_energy.size();
				for (int i = 1; i < size; i++) {
					// Compares the energy of the first beat with the others
					// If the difference is within an
					double max = Math.max(first_beat_energy, detected_beats_energy.get(i));
					double min = Math.min(first_beat_energy, detected_beats_energy.get(i));
					if ((max-min)/min < error) {
						double pattern_start_time = detected_beats_time.get(0);
						double pattern_repetiton_time = detected_beats_time.get(i);
						System.out.printf("[" + getAgent().getAgentName() + "] Pattern found at beat = %d - t = %f\n", i, pattern_start_time);
						measure_duration = pattern_repetiton_time - pattern_start_time;
						System.out.println("\tmeasure duration = " + measure_duration);
						number_beats = (int)Math.round(measure_duration/beat_duration);
						System.out.println("\tnumber of beats = " + number_beats);
						System.out.print("\tbeats = [");
						for (int j = 0; j < i; j++) {
							double beat = detected_beats_time.get(j) - pattern_start_time;
							beats.add(Math.round(beat * 10000)/10000.0);
							System.out.print(" " + beat);
						}
						System.out.println(" ]");
						// Starts at the third repetition
						start_playing_time = pattern_repetiton_time + measure_duration;
						System.out.println("\tstart playing time = " + start_playing_time);
						// Changes agent's state
						state = ReasoningState.PLAYING;
						
					}
				}
			}
			
			break;
			
		case PLAYING:
			
			// Movimentação
			if (!master && legs != null) {
				if (last_phase != actual_phase) {
					last_phase = actual_phase;
					// Ouve mudança de fase -> movimentar
					Command cmd = new Command(getAddress(), "/"+ Constants.FRAMEWORK_NAME + "/" + getAgent().getAgentName() + "/MovementReasoning", "WALK");
					double angle = actual_phase * 2 * Math.PI / number_beats;
//					System.out.println(number_beats);
//					System.out.println(angle);
//					System.out.println(radius);
					double x = master_position.getValue(0) + (radius * Math.cos(angle));
					double y = master_position.getValue(1) + (radius * Math.sin(angle));
					cmd.addParameter(MovementConstants.PARAM_POS, "("+x+";"+y+";0)");
					cmd.addParameter(MovementConstants.PARAM_TIME, "1");
					sendCommand(cmd);
//					System.out.println("[" + getAgent().getAgentName() + "] new destination =  " + "("+x+";"+y+";0)");
//					String x = actual_phase == 0 || actual_phase == 3 ? "20" : "-20"; 
//					String y = actual_phase == 0 || actual_phase == 1 ? "20" : "-20";
//					String cmd = "TRANSPORT :pos ("+x+";"+y+";0)";
//					legsMemory.writeMemory(cmd);
//					legs.act();
				}
			}
			
			break;

		}

	}
	
}
