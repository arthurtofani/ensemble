package ensemble.apps.lm;

import java.util.HashMap;

import ensemble.world.World;


public class LM_World extends World {

	public enum Direction {
		DIR_N,
		DIR_NW,
		DIR_W,
		DIR_SW,
		DIR_S,
		DIR_SE,
		DIR_E,
		DIR_NE;
	}
	
	public final static int DIR_NONE 	= -1;
	public final static int DIR_N	 	= 0;
	public final static int DIR_NW	 	= 1;
	public final static int DIR_W 		= 2;
	public final static int DIR_SW 		= 3;
	public final static int DIR_S 		= 4;
	public final static int DIR_SE 		= 5;
	public final static int DIR_E 		= 6;
	public final static int DIR_NE	 	= 7;
	
	class Position {

		public Position(String agentName) {
			this.agentName = agentName;
		}
		
		// Nome do agente
		String 	agentName = "";

		// Posi��o atual do agent
		public int 	pos_x, pos_y;

		// Indica a dire��o que o agente presente est� virado
		// 0 - norte, 1 - leste, 2 - sul, 3 - oeste
		public int 	direction = DIR_NONE;
		
		public int 	lastSungMidiNote = 0;
		
	}
	
	class Sound {
		// Indica se existe um som presente
		// qual sua nota (-1 indica que n�o existe), amplitude e dire��o de propaga��o
		int 	note		= 0;
		int 	amplitude 	= 0;
		int 	direction 	= DIR_NONE;
		boolean propagated	= false;
	}
	
	class Site {
		
		// Posi��o do site no tabuleiro
		private int 	pos_x, pos_y;

		// Informa��es sobre o Agente no local
		Position agent;

		// Informa��es sobre o som no local 
		Sound sound;
		
		public float food = 0.0f;
		
		public Site(int x, int y) {
			pos_x = x;
			pos_y = y;
			sound = new Sound();
		}
		
	}

	// Representa��o do espa�o f�sico (tabuleiro bidimensional)
	protected Site[][] squareLattice;
	
	// Lista dos agentes no mundo virtual
//	protected HashMap<String, Agent> agents = new HashMap<String, Agent>();
	
	@Override
	public boolean init() {
		// Inicializa��o das posi��es
		squareLattice = new Site[LM_Constants.WorldSize][LM_Constants.WorldSize];
		
		for (int i = 0; i < squareLattice.length; i++) {
			for (int j = 0; j < squareLattice[i].length; j++) {
				squareLattice[i][j] = new Site(i, j);
			}
		}
	
		setWorldGUI(new LM_BoardGUI(squareLattice));
		
		return true;
	}
	
}
