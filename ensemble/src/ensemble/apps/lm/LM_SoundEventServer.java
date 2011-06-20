package ensemble.apps.lm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;


import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;
import javax.sound.midi.MidiDevice.Info;

import ensemble.EnvironmentAgent;
import ensemble.Event;
import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.apps.lm.LM_World.Position;
import ensemble.apps.lm.LM_World.Sound;

public class LM_SoundEventServer extends EventServer {

	// Mundo virtual
	LM_World world;
	Sound[][] newSounds;
	
	// Dados do agente para enviar o eventos
	protected String agentName;
	protected String agentCompName;
	
	// �ltima notada cantada
	private int lastMidiNote = 84;
	
	// Sons cantandos no turno
	ArrayList<Event> events = new ArrayList<Event>();
	
	// MIDI
	Synthesizer	synth;
	MidiChannel	channel;
	Receiver 	rcv;
	
	@Override
	public boolean configure() {
		setEventType("SOUND");
		return true;
	}
	
	public boolean init() {
	
		world = (LM_World)envAgent.getWorld();
		
		// Initializes MIDI
		try {
//			MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
//			for (int i = 0; i < infos.length; i++) {
//				System.out.println(infos[i].getName());
//			}
//			synth = (Synthesizer)MidiSystem.getMidiDevice(infos[3]);
			synth = MidiSystem.getSynthesizer();
			synth.open();
			MidiChannel[] channels = synth.getChannels();
			channel = channels[0];

		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return true;

	}
	
	@Override
	public boolean finit() {
		return true;
	}
	
	private void setSound(int x, int y, int note, int amplitude, int direction) {

		if ((x >= 0 && x < LM_Constants.WorldSize) && (y >= 0 && y < LM_Constants.WorldSize)) {

			Sound sound = newSounds[x][y];

			if (amplitude > sound.amplitude) {
				
				// Em seguida, colocar o som novo
				sound.note			= note;
				sound.amplitude  	= amplitude;
				sound.direction 	= direction;
				sound.propagated 	= true;

			}
		}


	}

	// TODO Problema se dois sons que se encontram tem a mesma amplitude
	private void propagateSound(Sound sound, int i, int j, int note, int amplitude, int direction) {

		if ((i >= 0 && i < LM_Constants.WorldSize) && (j >= 0 && j < LM_Constants.WorldSize)) {

			Sound oldSound = world.squareLattice[i][j].sound;
		
			switch (sound.direction) {
	
			case LM_World.DIR_N:
				if (!((oldSound.direction == LM_World.DIR_S || oldSound.direction == LM_World.DIR_SE || oldSound.direction == LM_World.DIR_SW) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_NE:
				if (!((oldSound.direction == LM_World.DIR_S || oldSound.direction == LM_World.DIR_SW || oldSound.direction == LM_World.DIR_W) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_E:
				if (!((oldSound.direction == LM_World.DIR_SW || oldSound.direction == LM_World.DIR_W || oldSound.direction == LM_World.DIR_NW) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_SE:
				if (!((oldSound.direction == LM_World.DIR_W || oldSound.direction == LM_World.DIR_NW || oldSound.direction == LM_World.DIR_N) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_S:
				if (!((oldSound.direction == LM_World.DIR_NW || oldSound.direction == LM_World.DIR_N || oldSound.direction == LM_World.DIR_NE) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_SW:
				if (!((oldSound.direction == LM_World.DIR_N || oldSound.direction == LM_World.DIR_NE || oldSound.direction == LM_World.DIR_E) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_W:
				if (!((oldSound.direction == LM_World.DIR_NE || oldSound.direction == LM_World.DIR_E || oldSound.direction == LM_World.DIR_SE) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
	
			case LM_World.DIR_NW:
				if (!((oldSound.direction == LM_World.DIR_S || oldSound.direction == LM_World.DIR_SE || oldSound.direction == LM_World.DIR_E) &&
					  (sound.amplitude < oldSound.amplitude || newSounds[i][j].direction != LM_World.DIR_NONE))) {
					setSound(i, j, note, amplitude, direction);
				}
				break;
			}
		}
		
	}

	@Override
	public void process() {
		
		// Cria um novo campo sonoro
		newSounds = new Sound[LM_Constants.WorldSize][LM_Constants.WorldSize]; 		
		for (int i = 0; i < newSounds.length; i++) {
			for (int j = 0; j < newSounds[i].length; j++) {
				newSounds[i][j] = world.new Sound();
			}
		}
		
		// Percorre todas as casas do tabuleiro, propagando o som
		for (int i = 0; i < world.squareLattice.length; i++) {
			for (int j = 0; j < world.squareLattice[i].length; j++) {
				
				Sound sound = world.squareLattice[i][j].sound;

				// S� vai propagar se for maior que o som presente atualmente no local
				// e se as dire��es forem opostas, apagar a ultima posi��o 
				switch (sound.direction) {

				case LM_World.DIR_N:
					propagateSound(sound, i-1, j, sound.note, sound.amplitude - 1, LM_World.DIR_N);
					break;

				case LM_World.DIR_NE:
					propagateSound(sound, i-1, j, sound.note, sound.amplitude - 1, LM_World.DIR_N);
					propagateSound(sound, i-1, j+1, sound.note, sound.amplitude - 1, LM_World.DIR_NE);
					propagateSound(sound, i, j+1, sound.note, sound.amplitude - 1, LM_World.DIR_E);
					break;

				case LM_World.DIR_E:
					propagateSound(sound, i, j+1, sound.note, sound.amplitude - 1, LM_World.DIR_E);
					break;

				case LM_World.DIR_SE:
					propagateSound(sound, i, j+1, sound.note, sound.amplitude - 1, LM_World.DIR_E);
					propagateSound(sound, i+1, j+1, sound.note, sound.amplitude - 1, LM_World.DIR_SE);
					propagateSound(sound, i+1, j, sound.note, sound.amplitude - 1, LM_World.DIR_S);
					break;

				case LM_World.DIR_S:
					propagateSound(sound, i+1, j, sound.note, sound.amplitude - 1, LM_World.DIR_S);
					break;

				case LM_World.DIR_SW:
					propagateSound(sound, i+1, j, sound.note, sound.amplitude - 1, LM_World.DIR_S);
					propagateSound(sound, i+1, j-1, sound.note, sound.amplitude - 1, LM_World.DIR_SW);
					propagateSound(sound, i, j-1, sound.note, sound.amplitude - 1, LM_World.DIR_W);
					break;

				case LM_World.DIR_W:
					propagateSound(sound, i, j-1, sound.note, sound.amplitude - 1, LM_World.DIR_W);
					break;

				case LM_World.DIR_NW:
					propagateSound(sound, i, j-1, sound.note, sound.amplitude - 1, LM_World.DIR_W);
					propagateSound(sound, i-1, j-1, sound.note, sound.amplitude - 1, LM_World.DIR_NW);
					propagateSound(sound, i-1, j, sound.note, sound.amplitude - 1, LM_World.DIR_N);
					break;
				}

			}

		}
		
		// Coloca os sons cantandos pelos agentes
		for (Event evt : events) {
			// Propagar o som
			int note = Integer.parseInt((String)evt.objContent);
			Position pos = (Position)world.getEntityStateAttribute(evt.oriAgentName, "POSITION");
			setSound(pos.pos_x - 1, 	pos.pos_y, 		note, LM_Constants.SoundRadius, LM_World.DIR_N);
			setSound(pos.pos_x - 1, 	pos.pos_y + 1,	note, LM_Constants.SoundRadius, LM_World.DIR_NE);
			setSound(pos.pos_x, 		pos.pos_y + 1, 	note, LM_Constants.SoundRadius, LM_World.DIR_E);
			setSound(pos.pos_x + 1, 	pos.pos_y + 1, 	note, LM_Constants.SoundRadius, LM_World.DIR_SE);
			setSound(pos.pos_x + 1, 	pos.pos_y,	 	note, LM_Constants.SoundRadius, LM_World.DIR_S);
			setSound(pos.pos_x + 1, 	pos.pos_y - 1, 	note, LM_Constants.SoundRadius, LM_World.DIR_SW);
			setSound(pos.pos_x, 		pos.pos_y - 1, 	note, LM_Constants.SoundRadius, LM_World.DIR_W);
			setSound(pos.pos_x - 1, 	pos.pos_y - 1, 	note, LM_Constants.SoundRadius, LM_World.DIR_NW);
		}
		events.clear();
		
		// Copia o novo campo sonoro
		for (int i = 0; i < world.squareLattice.length; i++) {
			for (int j = 0; j < world.squareLattice[i].length; j++) {
				world.squareLattice[i][j].sound = newSounds[i][j];
			}
		}
		
		// Repassa o som para os agentes registrados
		Set<String> set = sensors.keySet();
		for (String sensor : set) {
			String[] str = sensor.split(":");
			Position pos = (Position)world.getEntityStateAttribute(str[0], "POSITION");
			if (world.squareLattice[pos.pos_x][pos.pos_y].sound.direction != LM_World.DIR_NONE) {
				agentName 		= str[0];
				agentCompName 	= str[1];
				act();
			}
		}

	}

	@Override
	public void processSense(Event evt) {
		
		// Armazenar som na lista
		events.add(evt);

		// Tocar a nota MIDI
		float lp = Float.valueOf(envAgent.agentsPublicFacts.get(evt.oriAgentName+":"+"ListeningPleasure"));
		//int velocity = (int)(lp * 100 / 2);
		float L 	= LM_Constants.MaxSoundGenomeLength;
		float L_2 	= L * L;  
		int velocity = Math.round(20f + ((lp + (1/L_2)) * (100f - 80f) / (L - (1/L_2))));
		if (velocity > 100) {
			velocity = 100;
		} else if (velocity < 20) {
			velocity = 20;
		}

		Position pos = (Position)world.getEntityStateAttribute(evt.oriAgentName, "POSITION");

		int midiNote = 84;
		int agentNote = Integer.valueOf((String)evt.objContent);
		if (LM_Constants.AbsoluteNoteMapping) {
			midiNote = 84 + agentNote;
		} else {
			int localLastNote;
			if (LM_Constants.GlobalInterval) {
				localLastNote = lastMidiNote;
			} else {
				localLastNote = pos.lastSungMidiNote;
			}
			// Calcula o intervalo baseado na posi��o do Agent
			if (pos.direction >= 0 && pos.direction <=3) {
				midiNote = localLastNote - agentNote;   
			} else {
				midiNote = localLastNote + agentNote;   
			}
			// Ajusta a nota MIDI no caso de estar abaixo ou acima dos valores configurados
			midiNote = LM_Constants.MinMidiNote + 
						((midiNote - LM_Constants.MinMidiNote) % (LM_Constants.MaxMidiNote + 1 - LM_Constants.MinMidiNote));
		}

		lastMidiNote = midiNote;
		pos.lastSungMidiNote = midiNote;
		
		channel.noteOn(midiNote, velocity);
//		ShortMessage noteOn = new ShortMessage();
//		try {
//			noteOn.setMessage(ShortMessage.NOTE_ON, 0, midiNote, velocity);
//			rcv.send(noteOn, -1);
//		} catch (InvalidMidiDataException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	@Override
	protected Event processAction(Event evt) {
		
		Event event = new Event();
		event.destAgentName = agentName;
		event.destAgentCompName = agentCompName;
		Position pos = (Position)world.getEntityStateAttribute(agentName, "POSITION");
		event.objContent = world.squareLattice[pos.pos_x][pos.pos_y].sound.note + " " + 
						world.squareLattice[pos.pos_x][pos.pos_y].sound.amplitude + " " +
						world.squareLattice[pos.pos_x][pos.pos_y].sound.direction;

		return event;
	}

}
