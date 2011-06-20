package ensemble.apps.lm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import ensemble.MusicalAgent;
import ensemble.Parameters;


public class LM_MusicalAgent extends MusicalAgent {

	private String randomizeSoundGenoma() {
		
		// Sorteia um SoundGenoma, de 1 at� 10 notas
		int numberOfNotes = (int)Math.ceil(Math.random() * LM_Constants.MaxSoundGenomeLength);
		String soundGenoma = "";
		for (int i = 0; i < numberOfNotes; i++) {
			soundGenoma = soundGenoma + String.valueOf((int)Math.ceil(Math.random() * 12)) + ":";
		}
		soundGenoma  = soundGenoma .substring(0, soundGenoma.length() - 1);

		return soundGenoma;
		
	}
	
	private String randomizeProceduralGenoma() {
		
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
		
		return proceduralGenoma;
		
	}
	
	@Override
	public boolean configure() {
		
		String pos_x = parameters.get("pos_x");
		String pos_y = parameters.get("pos_y");

		// Adiciona os componentes do Agente Musical
		// TODO Nesse caso, deve ser feito depois caso o componente precise de alguma informa��o do KB
		this.addComponent("Reasoning", "ensemble.apps.lm.LM_Reasoning", null);

		Parameters footParameters = new Parameters();
		if (pos_x != null && pos_y != null) {
			footParameters.put("pos_x", pos_x);
			footParameters.put("pos_y", pos_y);
		}
		this.addComponent("Feet", "ensemble.apps.lm.LM_MovementActuator", footParameters);
		
		this.addComponent("Mouth", "ensemble.apps.lm.LM_SoundActuator", new Parameters());
		
		this.addComponent("Ear", "ensemble.apps.lm.LM_SoundSensor", new Parameters());

//		this.addComponent("Food", "ensemble.apps.lm.LM_FoodSensor", new Parameters());

//		//this.addComponent(new LM_FoodActuator("Evacuador", this));
		
		// Tentacles
		Parameters leftParameters = new Parameters();
		leftParameters.put("position", "LEFT");
		this.addComponent("Tentacle_left", "ensemble.apps.lm.LM_TentacleSensor", leftParameters);
		Parameters rightParameters = new Parameters();
		rightParameters.put("position", "RIGHT");
		this.addComponent("Tentacle_right", "ensemble.apps.lm.LM_TentacleSensor", rightParameters);
		Parameters frontParameters = new Parameters();
		frontParameters.put("position", "FRONT");
		this.addComponent("Tentacle_front", "ensemble.apps.lm.LM_TentacleSensor", frontParameters);
		
		return true;
	}
	
	@Override
	public boolean init() {
		
		// Agente rand�mico criado na inicializa��o
		getKB().registerFact("SoundGenoma", parameters.get("SoundGenoma", randomizeSoundGenoma()), true);
		getKB().registerFact("ProceduralGenoma", parameters.get("ProceduralGenoma", randomizeProceduralGenoma()), true);
		getKB().registerFact("Energy", parameters.get("Energy", "15.0"), true);		
		getKB().registerFact("Age", "0", true);
		getKB().registerFact("ListeningPleasure", "0.0", true);

		System.out.println(getAgentName() + ": "  + getKB().readFact("SoundGenoma") + "\t" + getKB().readFact("ProceduralGenoma"));
		
		return true;

	}

}
