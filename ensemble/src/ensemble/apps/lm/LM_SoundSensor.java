package ensemble.apps.lm;

import ensemble.Event;
import ensemble.Parameters;
import ensemble.Sensor;

// TODO: Auto-generated Javadoc
/**
 * The Class LM_SoundSensor.
 */
public class LM_SoundSensor extends Sensor {

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("SOUND");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventHandler#process(ensemble.Event)
	 */
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

	/* (non-Javadoc)
	 * @see ensemble.MusicalAgentComponent#init()
	 */
	@Override
	public boolean init() {
		
		getAgent().getKB().updateFact("LastNoteListened", "0");
		return true;
		
	}

}
