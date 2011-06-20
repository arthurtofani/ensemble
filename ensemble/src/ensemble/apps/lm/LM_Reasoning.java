package ensemble.apps.lm;

import ensemble.Actuator;
import ensemble.Event;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.memory.Memory;
import ensemble.memory.MemoryException;

public class LM_Reasoning extends Reasoning {

	private Actuator foot;
	private Memory footMemory;
	
	private Actuator mouth;
	private Memory mouthMemory;

	private Actuator evacuator;	
	private Memory evacuatorMemory;

	private String[]	proceduralGenoma;
	private int 		instrPointer = 0;

	private int 		loopSteps 			= 0;
	private int 		loopStepsCounter 	= 0;
	private int 		loopLenght 			= 0;
	
	private int 		walked 				= 0;
	
	@Override
	public boolean init() {
		proceduralGenoma = getAgent().getKB().readFact("ProceduralGenoma").split(":");
		return true;
	}

	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) {
		if (evtHdl instanceof LM_MovementActuator) {
			foot = (LM_MovementActuator)evtHdl;
			footMemory = getAgent().getKB().getMemory(foot.getComponentName());
		}
		else if (evtHdl instanceof LM_SoundActuator) {
			mouth = (LM_SoundActuator)evtHdl;
			mouthMemory = getAgent().getKB().getMemory(mouth.getComponentName());
		}
//		else if (evtHdl instanceof LM_SoundActuator) {
//			evacuator = (LM_FoodActuator)evtHdl;
//			evacuatorMemory = getAgent().getKB().getMemory(evacuator.getName());
//		}
	}

	@Override
	public void process() {

		float energyWaste = 0.0f;
		
		// 1. Obt�m pr�xima instru��o no KB e atualiza o ponteiro
		String instr = proceduralGenoma[instrPointer];

		// Instru��es de controle (LOOP e IF)
		if (instr.startsWith("L")) {
			
			loopLenght = Integer.parseInt(instr.substring(1, 3));
			loopSteps  = Integer.parseInt(instr.substring(3, 5));
			loopStepsCounter = loopSteps;
			instrPointer++;
			instr = proceduralGenoma[instrPointer];
			
		} else if (instr.startsWith("<") || instr.startsWith("=") || instr.startsWith(">")) {
			
			boolean result = false;
			int constant = 0;
			int ifSteps = 0;
			
			char sensor1 = instr.substring(1,2).toCharArray()[0];
			// Descobrir se est� comparando com um outro sensor ou com uma constante
			char sensor2 = instr.substring(2,3).toCharArray()[0];
			//System.out.println((int)sensor2);
			if (((int)sensor2) >= 97 && ((int)sensor2) <= 104) {
				ifSteps = Integer.parseInt(instr.substring(3,5));
			} else {
				constant = Integer.parseInt(instr.substring(2,4));
				ifSteps = Integer.parseInt(instr.substring(4,6));
			}
			
//			// Obter valor dos sensores
//			long value1 = -1;
//			if (sensor1 == 'a') {
//				getAgent().getKB().readFact("");
//			} else if (sensor1 == 'b') {
//				getAgent().getKB().readFact("ListeningPleasure"));
//			} else if (sensor1 == 'c') {
//				getAgent().getKB().readFact("ListeningPleasure"));
//			} else if (sensor1 == 'd') {
//				value1 = Float.valueOf(getAgent().getKB().readFact("ListeningPleasure"));
//			} else if (sensor1 == 'e') {
//				getAgent().getKB().readFact("ListeningPleasure"));
//			} else if (sensor1 == 'f') {
//				getAgent().getKB().readFact("ListeningPleasure"));
//			} else if (sensor1 == 'g') {
//				value1 = Long.valueOf(getAgent().getKB().readFact("Age"));
//			}
			
			// Comparar, se verdadeiro, executar a pr�xima a��o, caso contr�rio, executar a ifSteps a��o
			if (!result) {
				instrPointer = instrPointer + ifSteps;
			}
			instr = proceduralGenoma[instrPointer];
			
		}

		// Verifica se est� dentro de um loop
		if (loopLenght > 0) {
			if (loopStepsCounter > 0) {
				loopStepsCounter--;
				if (loopStepsCounter == 0 && loopLenght > 1) {
					loopLenght--;
					loopStepsCounter = loopSteps;
					instrPointer = instrPointer - loopSteps;
				}
			}
		}
		
		// 2. Executa a a��o
		// TODO N�o trata Loops e IFs
		if (instr.equals("R")) {
		
			String cmd = getAgent().getAgentName() + " R";
			try {
				footMemory.writeMemory(cmd);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			foot.act();
			walked = 0;
		
		} else if (instr.equals("W")) {
			
			String cmd = getAgent().getAgentName() + " W";
			try {
				footMemory.writeMemory(cmd);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			foot.act();
			walked++;

		} else if (instr.equals("S")) {
			
			// Coloca a nota a ser cantada
			try {
				String note = (getAgent().getKB().readFact("SoundGenoma")).substring(0,1);
				mouthMemory.writeMemory(note);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			mouth.act();
			energyWaste = energyWaste + LM_Constants.CostOfSinging;
			walked = 0;
			
		} else if (instr.equals("T-")) {
			
			String cmd = getAgent().getAgentName() + " T-";
			try {
				footMemory.writeMemory(cmd);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			foot.act();
			walked = 0;
				
		} else if (instr.equals("T+")) {
				
			String cmd = getAgent().getAgentName() + " T+";
			try {
				footMemory.writeMemory(cmd);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			foot.act();
			walked = 0;
				
		} else if (instr.equals("Ts")) {
			
			// Verifica o ListeningPleasure, se for maior que 1, vira para o som
			
			// Coloca a instru��o no KB para criar o evento
			String cmd = getAgent().getAgentName() + " Ts";
			try {
				footMemory.writeMemory(cmd);
			} catch (MemoryException e) {
				e.printStackTrace();
			}
			foot.act();
			walked = 0;
			
		} 
		
		// Atualiza a idade do Agente
		int age = Integer.valueOf(getAgent().getKB().readFact("Age"));
		age = age + 1;
		getAgent().getKB().updateFact("Age", String.valueOf(age));
		
		// Faz o decaimento do Listening Pleasure
		float lp = Float.valueOf(getAgent().getKB().readFact("ListeningPleasure"));
		lp = lp * (1 - LM_Constants.ListeningPleasureDecay);
		getAgent().getKB().updateFact("ListeningPleasure", String.valueOf(lp));
		
		// Atualiza a energia
		//System.out.println("Reasoning...");
		energyWaste = energyWaste + LM_Constants.CostOfTime;
		float energy = Float.valueOf(getAgent().getKB().readFact("Energy"));
		getAgent().getKB().updateFact("Energy", String.valueOf(energy - energyWaste));
		//evacuator.action();
			
		// Atualiza o ponteiro de instru��es
		instrPointer++;
		if (instrPointer == proceduralGenoma.length) {
			instrPointer = 0;
		}
		
	}

}
