package ensemble.apps.lm;

import java.util.Set;

import ensemble.Event;
import ensemble.EventServer;
import ensemble.Parameters;
import ensemble.apps.lm.LM_World.Position;


public class LM_EnergyEventServer extends EventServer {

	LM_World world;
	
	// Dados para o evento
	String 	agentName;
	String 	agentComponentName;
	float 	food;
	
	@Override
	public boolean configure() {
		setEventType("ENERGY");
		return true;
	}

	@Override
	public boolean init() {
		
		// obt�m o ambiente virtual
		world = (LM_World)envAgent.getWorld();
		// TODO reservar a comida para os agentes iniciais
		
		// Distribui comida aleatoriamente
		for (int i = 0; i < world.squareLattice.length; i++) {
			for (int j = 0; j < world.squareLattice[i].length; j++) {
				if (Math.random() <= LM_Constants.InitFoodProb) {
					// TODO O valor deve ser configur�vel
					world.squareLattice[i][j].food = 3.0f;
				}
			}
		}

		return true;
	}

	@Override
	public boolean finit() {
		return true;
	}

	@Override
	public void process() {
		
		// Verifica o tabuleiro para ver se existe agente em alguma posi��o com comida
		Set<String> set = sensors.keySet();
		for (String sensor : set) {

			String[] str = sensor.split(":");
			Position pos = (Position)world.getEntityStateAttribute(str[0], "POSITION");
			food = world.squareLattice[pos.pos_x][pos.pos_y].food;
			if (food > 0.0f) {

				// Envia o evento para o agente
				agentName 			= str[0];
				agentComponentName 	= str[1];
				act();
				
				// Apaga a comida usada do tabuleiro
				world.squareLattice[pos.pos_x][pos.pos_y].food = 0.0f;
				
			}

		}
			
	}

	@Override
	protected Event processAction(Event evt) {
		
		Event event 			= new Event();
		event.destAgentName 	= agentName;
		event.destAgentCompName = agentComponentName;
		event.objContent 		= String.valueOf(food);

		return event;
		
	}

}
