package mms.apps.lm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import mms.MusicalAgent;
import mms.Parameters;

public class LM_MusicalAgent extends MusicalAgent {

	private void randomizeSoundGenoma() {
		
		// Sorteia um SoundGenoma, de 1 at� 10 notas
		int numberOfNotes = (int)Math.ceil(Math.random() * LM_Constants.MaxSoundGenomeLength);
		String soundGenoma = "";
		for (int i = 0; i < numberOfNotes; i++) {
			soundGenoma = soundGenoma + String.valueOf((int)Math.ceil(Math.random() * 12)) + ":";
		}
		soundGenoma  = soundGenoma .substring(0, soundGenoma.length() - 1);
		
		getKB().registerFact("SoundGenoma", soundGenoma, true);
		
	}
	
	private void randomizeProceduralGenoma() {
		
		// Sorteia um ProceduralGenoma, de 1 at� DeathLength
		int numberOfInstructions = (int)Math.ceil(Math.random() * LM_Constants.DeathLength);
		String proceduralGenoma = "";
		for (int i = 0; i < numberOfInstructions; i++) {
			int instruction = (int)Math.floor(Math.random() * 4);
			switch (instruction) {
			case 0:
				proceduralGenoma = proceduralGenoma + "W" + ":";
				break;
			case 1:
				proceduralGenoma = proceduralGenoma + "T+" + ":";
				break;
			case 2:
				proceduralGenoma = proceduralGenoma + "T-" + ":";
				break;
			case 3:
				proceduralGenoma = proceduralGenoma + "S" + ":";
				break;
			case 4:
				// Turn toward sound instruction
				proceduralGenoma = proceduralGenoma + "Ts" + ":";
				break;
			case 5:
				// LOOP instruction
				NumberFormat nf = new DecimalFormat("00");
				int loopSteps = (int)Math.ceil(Math.random() * LM_Constants.MaxLoopSteps);
				int loopLength = (int)Math.ceil(Math.random() * LM_Constants.MaxLoopLength);
				proceduralGenoma = proceduralGenoma + "L" + nf.format(loopSteps) + nf.format(loopLength) + ":";
				break;
			case 6:
				// IF instruction
				int comparator = (int)Math.ceil(Math.random() * 3);
				switch (comparator) {
				case 1:	
					proceduralGenoma = proceduralGenoma + "<";
					break;
				case 2:	
					proceduralGenoma = proceduralGenoma + "=";
					break;
				case 3:	
					proceduralGenoma = proceduralGenoma + ">";
					break;
				}
				int sensor = (int)Math.ceil(Math.random() * 8);
				switch (sensor) {
				case 1:	
					proceduralGenoma = proceduralGenoma + "a";
					break;
				case 2:	
					proceduralGenoma = proceduralGenoma + "b";
					break;
				case 3:	
					proceduralGenoma = proceduralGenoma + "c";
					break;
				case 4:	
					proceduralGenoma = proceduralGenoma + "d";
					break;
				case 5:	
					proceduralGenoma = proceduralGenoma + "e";
					break;
				case 6:	
					proceduralGenoma = proceduralGenoma + "f";
					break;
				case 7:	
					proceduralGenoma = proceduralGenoma + "g";
					break;
				case 8:	
					proceduralGenoma = proceduralGenoma + "h";
					break;
				}
				NumberFormat nf2 = new DecimalFormat("00");
				int ifSteps = (int)Math.ceil(Math.random() * LM_Constants.MaxIfSteps);
				proceduralGenoma = proceduralGenoma + nf2.format(ifSteps) + ":";
				break;
			}
		}
		proceduralGenoma = proceduralGenoma.substring(0, proceduralGenoma.length() - 1);
		
		getKB().registerFact("ProceduralGenoma", proceduralGenoma, true);
		
	}
	
	// Argumentos: SoundGenoma, ProceduralGenoma, Energy, Position
	public void init(Hashtable<String,String> parameters) {

		String pos_x = null;
		String pos_y = null;

		// Agente rand�mico criado na inicializa��o
		if (parameters == null) {
			
			randomizeSoundGenoma();
			randomizeProceduralGenoma();
			getKB().registerFact("Energy", "15.0", true);
			
		} else {
		
			getKB().registerFact("SoundGenoma", parameters.get("SoundGenoma"), true);
			getKB().registerFact("ProceduralGenoma", parameters.get("ProceduralGenoma"), true);
			getKB().registerFact("Energy", parameters.get("Energy"), true);
			pos_x = parameters.get("pos_x");
			pos_y = parameters.get("pos_y");

		}
		
		getKB().registerFact("Age", "0", true);
		getKB().registerFact("ListeningPleasure", "0.0", true);

		System.out.println(getLocalName() + ": "  + getKB().readFact("SoundGenoma") + "\t" + getKB().readFact("ProceduralGenoma"));
		
		// Adiciona os componentes do Agente Musical
		// TODO Nesse caso, deve ser feito depois caso o componente precise de alguma informa��o do KB
		this.addComponent("Reasoning", "mms.apps.lm.LM_Reasoning", null);

		Parameters footParameters = new Parameters();
		if (pos_x != null && pos_y != null) {
			footParameters.put("pos_x", pos_x);
			footParameters.put("pos_y", pos_y);
		}
		this.addComponent("Feet", "mms.apps.lm.LM_Reasoning", footParameters);
		
		this.addComponent("Mouth", "mms.apps.lm.LM_SoundActuator", null);
		
		this.addComponent("Ear", "mms.apps.lm.LM_SoundSensor", null);

		this.addComponent("Food", "mms.apps.lm.LM_FoodSensor", null);

		//this.addComponent(new LM_FoodActuator("Evacuador", this));
		
		// Tentancles
		Parameters leftParameters = new Parameters();
		leftParameters.put("position", "LEFT");
		this.addComponent("Tentacle_left", "mms.apps.lm.LM_TentacleSensor", leftParameters);
		Parameters rightParameters = new Parameters();
		rightParameters.put("position", "RIGHT");
		this.addComponent("Tentacle_right", "mms.apps.lm.LM_TentacleSensor", rightParameters);
		Parameters frontParameters = new Parameters();
		frontParameters.put("position", "FRONT");
		this.addComponent("Tentacle_front", "mms.apps.lm.LM_TentacleSensor", frontParameters);
		
	}

}
