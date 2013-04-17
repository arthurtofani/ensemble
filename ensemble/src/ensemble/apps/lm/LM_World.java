package ensemble.apps.lm;

import java.util.HashMap;

import ensemble.world.World;


// TODO: Auto-generated Javadoc
/**
 * The Class LM_World.
 */
public class LM_World extends World {

	/**
	 * The Enum Direction.
	 */
	public enum Direction {
		
		/** The dir n. */
		DIR_N,
		
		/** The dir nw. */
		DIR_NW,
		
		/** The dir w. */
		DIR_W,
		
		/** The dir sw. */
		DIR_SW,
		
		/** The dir s. */
		DIR_S,
		
		/** The dir se. */
		DIR_SE,
		
		/** The dir e. */
		DIR_E,
		
		/** The dir ne. */
		DIR_NE;
	}
	
	/** The Constant DIR_NONE. */
	public final static int DIR_NONE 	= -1;
	
	/** The Constant DIR_N. */
	public final static int DIR_N	 	= 0;
	
	/** The Constant DIR_NW. */
	public final static int DIR_NW	 	= 1;
	
	/** The Constant DIR_W. */
	public final static int DIR_W 		= 2;
	
	/** The Constant DIR_SW. */
	public final static int DIR_SW 		= 3;
	
	/** The Constant DIR_S. */
	public final static int DIR_S 		= 4;
	
	/** The Constant DIR_SE. */
	public final static int DIR_SE 		= 5;
	
	/** The Constant DIR_E. */
	public final static int DIR_E 		= 6;
	
	/** The Constant DIR_NE. */
	public final static int DIR_NE	 	= 7;
	
	/**
	 * The Class Position.
	 */
	class Position {

		/**
		 * Instantiates a new position.
		 *
		 * @param agentName the agent name
		 */
		public Position(String agentName) {
			this.agentName = agentName;
		}
		
		// Nome do agente
		/** The agent name. */
		String 	agentName = "";

		// Posi��o atual do agent
		/** The pos_y. */
		public int 	pos_x, pos_y;

		// Indica a dire��o que o agente presente est� virado
		// 0 - norte, 1 - leste, 2 - sul, 3 - oeste
		/** The direction. */
		public int 	direction = DIR_NONE;
		
		/** The last sung midi note. */
		public int 	lastSungMidiNote = 0;
		
	}
	
	/**
	 * The Class Sound.
	 */
	class Sound {
		// Indica se existe um som presente
		// qual sua nota (-1 indica que n�o existe), amplitude e dire��o de propaga��o
		/** The note. */
		int 	note		= 0;
		
		/** The amplitude. */
		int 	amplitude 	= 0;
		
		/** The direction. */
		int 	direction 	= DIR_NONE;
		
		/** The propagated. */
		boolean propagated	= false;
	}
	
	/**
	 * The Class Site.
	 */
	class Site {
		
		// Posi��o do site no tabuleiro
		/** The pos_y. */
		private int 	pos_x, pos_y;

		// Informa��es sobre o Agente no local
		/** The agent. */
		Position agent;

		// Informa��es sobre o som no local 
		/** The sound. */
		Sound sound;
		
		/** The food. */
		public float food = 0.0f;
		
		/**
		 * Instantiates a new site.
		 *
		 * @param x the x
		 * @param y the y
		 */
		public Site(int x, int y) {
			pos_x = x;
			pos_y = y;
			sound = new Sound();
		}
		
	}

	// Representa��o do espa�o f�sico (tabuleiro bidimensional)
	/** The square lattice. */
	protected Site[][] squareLattice;
	
	// Lista dos agentes no mundo virtual
//	protected HashMap<String, Agent> agents = new HashMap<String, Agent>();
	
	/* (non-Javadoc)
	 * @see ensemble.world.World#init()
	 */
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
