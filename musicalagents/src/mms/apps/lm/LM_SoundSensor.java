package mms.apps.lm;

import mms.Event;
import mms.Parameters;
import mms.Sensor;

public class LM_SoundSensor extends Sensor {

	@Override
	protected void configure(Parameters parameters) {
		setEventType("SOUND");
	}
	
	@Override
	protected void process(Event evt) {

		// Extrai os dados do evento
		String content[] = ((String)evt.objContent).split(" ");
		int note 		= Integer.parseInt(content[0]);
		int amplitude	= Integer.parseInt(content[1]);
		int direction 	= Integer.parseInt(content[2]);

		getAgent().getKB().updateFact("LastNoteListened", String.valueOf(note));

		// Recupera o SoundGenoma da Base de Conhecimentos
		String[] soundGenoma = getAgent().getKB().readFact("SoundGenoma").split(":");
		int L = soundGenoma.length;

		// Calcula o novo Listening Pleasure

		float LP = Float.valueOf(getAgent().getKB().readFact("ListeningPleasure"));

		// Conta o n�mero de notas iguais � escutada no Genoma
		for (int i = 0; i < soundGenoma.length; i++) {
			if (Integer.valueOf(soundGenoma[i]) == note) {
				int P = i + 1;
				float aux = ((L - P + 1) / L); 
				LP = LP + (aux * aux);
			}
		}
		
		// Armazena o LP
		getAgent().getKB().updateFact("ListeningPleasure", String.valueOf(LP));

	}

	@Override
	protected boolean init() {
		
		getAgent().getKB().updateFact("LastNoteListened", "0");
		return true;
		
	}

}
