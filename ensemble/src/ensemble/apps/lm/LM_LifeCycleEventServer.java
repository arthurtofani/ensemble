package ensemble.apps.lm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.apps.lm.LM_World.Direction;
import ensemble.apps.lm.LM_World.Position;


// TODO: Auto-generated Javadoc
/**
 * The Class LM_LifeCycleEventServer.
 */
public class LM_LifeCycleEventServer extends EventServer {

	// Mundo virtual
	/** The world. */
	LM_World world;

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
		setEventType("LIFE");
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#init()
	 */
	@Override
	public boolean init() {
		world = (LM_World)envAgent.getWorld();
		return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.EventServer#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}

	/**
	 * Check agent presence.
	 *
	 * @param x the x
	 * @param y the y
	 * @param dir the dir
	 * @return the position
	 */
	private Position checkAgentPresence(int x, int y, Direction dir) {

		Position pos = null;
		
		switch (dir) {
			case DIR_NW:
				x = x - 1;
				y = y - 1;
				break;
			case DIR_N:
				x = x - 1;
				break;
			case DIR_NE:
				x = x - 1;
				y = y + 1;
				break;
			case DIR_E:
				y = y + 1;
				break;
			case DIR_SE:
				x = x + 1;
				y = y + 1;
				break;
			case DIR_S:
				x = x + 1;
				break;
			case DIR_SW:
				x = x + 1;
				y = y - 1;
				break;
			case DIR_W:
				y = y - 1;
				break;
		}

		if ((x >= 0 && x < LM_Constants.WorldSize) && (y >= 0 && y < LM_Constants.WorldSize)) {
			pos = world.squareLattice[x][y].agent;
		}

		return pos;
		
	}
	
	/**
	 * Crossover.
	 *
	 * @param genoma1 the genoma1
	 * @param genoma2 the genoma2
	 * @param crossoverProbability the crossover probability
	 * @return the string
	 */
	private String crossover(String genoma1, String genoma2, float crossoverProbability) {
		
		String[] gen1 = genoma1.split(":");
		String[] gen2 = genoma2.split(":");
		String newGenoma = "";
		
		int i = 0, j = 0;
		while (i < gen1.length && j < gen2.length) {
			newGenoma = newGenoma + gen1[i] + ":";
			if (Math.random() <= crossoverProbability) {
				newGenoma = newGenoma + gen2[j] + ":";
			}
			i++; j++;
		}
		newGenoma = newGenoma.substring(0, newGenoma.length()-1);
		
		return newGenoma;
		
	}
	
	/* (non-Javadoc)
	 * @see ensemble.EventServer#process()
	 */
	@Override
	public void process() {
		
		// --------------------------------------------------------------------------------------
		// Morte de um Agente
		
		Set<String> e = world.getEntityList();
		for (String agent : e) {
			int age = Integer.valueOf(envAgent.agentsPublicFacts.get(agent + ":Age"));
			float energy = Float.valueOf(envAgent.agentsPublicFacts.get(agent + ":Energy"));
			String proceduralGenoma = envAgent.agentsPublicFacts.get(agent + ":ProceduralGenoma");
			String[] instr = proceduralGenoma.split(":");
			if (age > LM_Constants.MaxAge || energy < LM_Constants.MinEnergy || instr.length > LM_Constants.DeathLength) {
				System.out.println("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "Agent '" + agent + "' has died");
				envAgent.destroyAgent(agent);
			}
		}

		// --------------------------------------------------------------------------------------
		// Reprodu��o dos Agentes
		
		// tabela de agentes que j� receberam par
		Set<String> alreadyPaired = new HashSet<String>();
		ArrayList<Position[]> pairs = new ArrayList<Position[]>();
		
		// Checar todos os pares de Agentes
		Set<String> agents = world.getEntityList();
		for (String agent : agents) {
			Position pos = (Position)world.getEntityStateAttribute(agent, "POSITION");
			// verificar se existe algum agente pr�ximo
			for (Direction dir: Direction.values()) {
				Position mate = checkAgentPresence(pos.pos_x, pos.pos_y, dir);
				if (mate != null) {
					// S� emparelha se os dois n�o tiverem para inda
					if (!alreadyPaired.contains(pos.agentName) && !alreadyPaired.contains(mate.agentName)) {
						alreadyPaired.add(pos.agentName);
						alreadyPaired.add(mate.agentName);
						Position[] pair = {pos, mate};
						pairs.add(pair);
					}
				}
			}
		}
		// Para cada par, checar se eles satisfazem
		for (Position[] pair : pairs) {
			
			Position mate1 = pair[0];
			Position mate2 = pair[1];
			
			String 	name1 	= mate1.agentName;
			int 	age1 	= Integer.valueOf(envAgent.agentsPublicFacts.get(name1+":Age"));
			float 	energy1 = Float.valueOf(envAgent.agentsPublicFacts.get(name1+":Energy"));
			float 	lp1 	= Float.valueOf(envAgent.agentsPublicFacts.get(name1+":ListeningPleasure"));
			//int 	walk1 	= Float.valueOf(envAgent.agentsPhenotype.get(name1+":Walk"));
			String 	proceduralGenoma1 = envAgent.agentsPublicFacts.get(name1+":ProceduralGenoma");
			String 	soundGenoma1 = envAgent.agentsPublicFacts.get(name1+":SoundGenoma");

			String name2 	= mate2.agentName;
			int 	age2 	= Integer.valueOf(envAgent.agentsPublicFacts.get(name2+":Age"));
			float 	energy2 = Float.valueOf(envAgent.agentsPublicFacts.get(name2+":Energy"));
			float 	lp2 	= Float.valueOf(envAgent.agentsPublicFacts.get(name2+":ListeningPleasure"));
			//int 	walk2 	= Float.valueOf(envAgent.agentsPhenotype.get(name2+":Walk"));
			String 	proceduralGenoma2 = envAgent.agentsPublicFacts.get(name2+":ProceduralGenoma");
			String 	soundGenoma2 = envAgent.agentsPublicFacts.get(name2+":SoundGenoma");

			// Para reproduzir, deve ter cumprir os seguintes requisitos
			if (age1 > LM_Constants.MinAgeToMate && age2 > LM_Constants.MinAgeToMate &&
				energy1 > LM_Constants.MinEnergyToMate && energy2 > LM_Constants.MinEnergyToMate &&
				lp1 > LM_Constants.MinLpToMate && lp2 > LM_Constants.MinLpToMate) {
				
//			if (age1 > LM_Constants.MinAgeToMate && age2 > LM_Constants.MinAgeToMate) {
				Parameters parameters = new Parameters();
				// Faz o crossover para os genomas do novo agente
				// TODO Mutata��o nos genes!
				parameters.put("SoundGenoma", crossover(soundGenoma1, soundGenoma2, LM_Constants.SoundCrossoverProb));
				parameters.put("ProceduralGenoma", crossover(proceduralGenoma1, proceduralGenoma2, LM_Constants.ActionCrossoverProb));
				parameters.put("Energy", "15.0");
				// Escolher uma posi��o aleat�ria baseado na posi��o dos pais
				// Move um dos pais
				boolean positioned = false;  
				while (!positioned) {
					int spread_x = (int)Math.floor(Math.random() * ((LM_Constants.MateSpread * 2) + 1));
					int spread_y = (int)Math.floor(Math.random() * ((LM_Constants.MateSpread * 2) + 1));
					int pos_x = (mate2.pos_x - LM_Constants.MateSpread) + spread_x;
					if (pos_x < 0) {
						pos_x = pos_x + LM_Constants.WorldSize;
					} 
					else if (pos_x >= LM_Constants.WorldSize) {
						pos_x = pos_x - LM_Constants.WorldSize;
					}
					int pos_y = (mate2.pos_y - LM_Constants.MateSpread) + spread_y;
					if (pos_y < 0) {
						pos_y = pos_y + LM_Constants.WorldSize;
					} 
					else if (pos_y >= LM_Constants.WorldSize) {
						pos_y = pos_y - LM_Constants.WorldSize;
					}
					if (world.squareLattice[pos_x][pos_y].agent == null) {
						parameters.put("pos_x", String.valueOf(pos_x));
						parameters.put("pos_y", String.valueOf(pos_y));
						positioned = true;
					}
				}
				
				// Cria o novo agente
				// TODO Como posicionar no local adequadao o novo agente?? PArametro!?
				String newAgent = envAgent.createMusicalAgent(null, "lm.LM_MusicalAgent", parameters);
				System.out.println("Nasceu o agente " + newAgent);
				
				// Move um dos pais
				positioned = false;  
				while (!positioned) {
					int spread_x = (int)Math.floor(Math.random() * ((LM_Constants.MateSpread * 2) + 1));
					int spread_y = (int)Math.floor(Math.random() * ((LM_Constants.MateSpread * 2) + 1));
					int pos_x = (mate1.pos_x - LM_Constants.MateSpread) + spread_x;
					if (pos_x < 0) {
						pos_x = pos_x + LM_Constants.WorldSize;
					} 
					else if (pos_x >= LM_Constants.WorldSize) {
						pos_x = pos_x - LM_Constants.WorldSize;
					}
					int pos_y = (mate1.pos_y - LM_Constants.MateSpread) + spread_y;
					if (pos_y < 0) {
						pos_y = pos_y + LM_Constants.WorldSize;
					} 
					else if (pos_y >= LM_Constants.WorldSize) {
						pos_y = pos_y - LM_Constants.WorldSize;
					}
					if (world.squareLattice[pos_x][pos_y].agent == null) {
						world.squareLattice[mate1.pos_x][mate1.pos_y].agent = null;
						world.squareLattice[pos_x][pos_y].agent = mate1;
						mate1.pos_x = pos_x;
						mate1.pos_y = pos_y;
						positioned = true;
					}
				}
				
			}
		}
		alreadyPaired.clear();
		pairs.clear();
	}

}
