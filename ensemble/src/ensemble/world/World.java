/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.world;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ensemble.Command;
import ensemble.Constants;
import ensemble.EnvironmentAgent;
import ensemble.LifeCycle;
import ensemble.Parameters;
import ensemble.Constants.EA_STATE;
import ensemble.clock.VirtualClockHelper;
import ensemble.router.RouterClient;


// TODO: Auto-generated Javadoc
/**
 * Represents the actual state of the world, with all its entities.
 */
//TODO Criar métodos genéricos para obter o estado do agente no mundo, posição etc...
public class World implements LifeCycle, RouterClient {
	
	/** The lock. */
	protected Lock lock = new ReentrantLock();

	/** The parameters. */
	Parameters parameters = null;

	/** The env agent. */
	protected EnvironmentAgent envAgent;
	
	/** The dimensions. */
	public int 		dimensions;
	
	/** The structure. */
	public String 	structure;
	
	/** The form_type. */
	public String 	form_type;
	
	/** The form_size. */
	public double 	form_size;
	
	/** The form_size_half. */
	public double 	form_size_half;
	
	/** The form_loop. */
	public boolean 	form_loop;
	
	/** The laws. */
	private HashMap<String,Law> laws = new HashMap<String,Law>();
	
	// TODO Na hora da criação, fine tune no tamanho e no load factor
    /** The entities. */
	protected HashMap<String, EntityState> entities = new HashMap<String, EntityState>();

    /** The gui. */
    protected WorldGUI gui;
    
    /** The clock. */
    protected VirtualClockHelper clock;
    
    // Performance
	/** The calls. */
    public int calls = 0;
	
	/** The time_1. */
	public long time_1 = 0;
	
	/** The time_2. */
	public long time_2 = 0;

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#setParameters(ensemble.Parameters)
	 */
	public final void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#getParameters()
	 */
	public final Parameters getParameters() {
		return this.parameters;
	}

	/**
	 * Sets the env agent.
	 *
	 * @param envAgent the new env agent
	 */
	public final void setEnvAgent(EnvironmentAgent envAgent) {
		this.envAgent = envAgent;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#start()
	 */
	@Override
    public final boolean start() {

    	// Parameters
    	if (parameters == null || envAgent == null) {
			System.err.println("[World] Parameters not set! World not initialized!");
			return false;
		}
    	
    	this.clock = envAgent.getClock();

    	// Basic world attributes
		this.dimensions = Integer.valueOf(parameters.get("dimensions", "3"));
		this.structure	= parameters.get("structure", "continuous");
    	String form = getParameters().get("form");
    	if (form != null) {
    		String[] str = form.split(":");
    		if (str[0].equals("cube") && str.length == 3) {
        		this.form_type 		= str[0];
    			this.form_size 		= Double.valueOf(str[1]); 
    			this.form_size_half = form_size / 2;
    			this.form_loop 		= str[2].equals("loop") ? true : false;
    		}
    	} else {
        		this.form_type 		= "infinite";
    			this.form_size 		= Double.MAX_VALUE; 
    			this.form_size_half = Double.MAX_VALUE / 2;
    			this.form_loop 		= false;
    	}
    	
		if (!init()) {
			return false;
		}
		
		// Starts world laws
		for (String lawName : laws.keySet()) {
			Law law = (Law)(laws.get(lawName));
			law.start();
		}
		
//		System.out.println("[WORLD] " + "Initialized");
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":WORLD] " + "Initialized");
		
		Command cmd = new Command(getAddress(), "/console", "CREATE");
		cmd.addParameter("AGENT", envAgent.getAgentName());
		cmd.addParameter("WORLD", Constants.WORLD);
		cmd.addParameter("CLASS", this.getClass().toString());
		cmd.addUserParameters(parameters);
		sendCommand(cmd);
		
		return true;
	
    }
    
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#stop()
	 */
	@Override
	public final boolean stop() {

		// Terminates world laws
		for (String lawName : laws.keySet()) {
			Law law = (Law)(laws.get(lawName));
			law.stop();
		}
		
		Command cmd = new Command(getAddress(), "/console", "DESTROY");
		cmd.addParameter("AGENT", Constants.ENVIRONMENT_AGENT);
		cmd.addParameter("WORLD", Constants.WORLD);
		sendCommand(cmd);

		return true;
	}
	
    /**
     * Add an entity to the virtual World (agent, obstacle etc.)
     *
     * @param entityName the entity name
     * @param parameters the parameters
     * @return true, if successful
     */
    public final boolean addEntity(String entityName, Parameters parameters) {

    	boolean result = false;

    	lock.lock();
    	try {
    		if (!entities.containsKey(entityName)) {
    			
    			// Creates a new EntityState for the Entity
    			EntityState state = new EntityState();
		    	entities.put(entityName, state);

		    	// Checks for defaults attributes for an entity
		    	// TODO ISSO NÃO VALE PARA O LM!!!
		    	Vector position = new Vector(dimensions);
		    	if (parameters.containsKey(Constants.PARAM_POSITION)) {
		    		String str_pos = parameters.get(Constants.PARAM_POSITION);
					if (str_pos.equals("random")) {
						for (int i = 0; i < dimensions; i++) {
							position.setValue(i, Math.random() * form_size - form_size_half);
						}
					} else {
			    		Vector position_1 = Vector.parse(str_pos);
			    		if (position_1 != null) {
				    		for (int i = 0; i < position_1.dimensions && i < position.dimensions; i++) {
								position.setValue(i, position_1.getValue(i));
							}
			    		}
					}
		    	}
	    		state.attributes.put(Constants.PARAM_POSITION, position);
	    		
	    		// Calls user implemented method
	    		entityAdded(entityName);
	    		
		    	result = true;
		    	
    		}			
    	} finally {
    		lock.unlock();
    	}
		
		return result;
    	
    }
    
    /**
     * Remove an entity from the Virtual World.
     *
     * @param entityName the entity name
     */
    public final void removeEntity(String entityName) {
    	
		// Calls user implemented method
		entityRemoved(entityName);

		// Removes entity from the world
		entities.remove(entityName);
    	
    }
    
    /**
     * Gets the entity list.
     *
     * @return the entity list
     */
    public final Set<String> getEntityList() {
    	
    	return entities.keySet();
    	
    }
    
    //--------------------------------------------------------------------------------
	// Laws
	//--------------------------------------------------------------------------------

    /**
     * Adds the law.
     *
     * @param className the class name
     * @param parameters the parameters
     */
    public final void addLaw(String className, Parameters parameters) {
		try {
			// Creates a Law instance
			Class lawClass = Class.forName(className);
			Law law = (Law)lawClass.newInstance();
			// Configures this Law
			law.setWorld(this);
			law.setParameters(parameters);
			law.configure();
			// Adicionar na tabela
			if (laws.containsKey(law.getType())) {
				System.err.println("ERROR: There is already a Law with type " + law.getType());
				return;
			} else {
				laws.put(law.getType(), law);
			}
			if (envAgent.getEAState() == EA_STATE.INITIALIZED) {
				law.start();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println("ERROR: Not possible to create an instance of " + className);
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.err.println("ERROR: Not possible to create an instance of " + className);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.err.println("ERROR: Not possible to create an instance of " + className);
		}
	}
		
    /**
     * Removes the law.
     *
     * @param type the type
     */
    public final void removeLaw(String type) {
    	if (laws.containsKey(type)) {
			Law law = laws.remove(type);
			law.stop();
		} else {
			System.err.println("["+envAgent.getAgentName()+"] Law " + type + " does not exist.");
		}
    }
    
    /**
     * Changes a state based in a law.
     *
     * @param type the type
     * @return the law
     */
    public final Law getLaw(String type) {
    	
    	return laws.get(type);
    	
    }

    //--------------------------------------------------------------------------------
	// GUI
	//--------------------------------------------------------------------------------

    /**
     * Gets the world gui.
     *
     * @return the world gui
     */
    public final WorldGUI getWorldGUI() {
    	
    	return gui;
    	
    }
    
    /**
     * Sets the world gui.
     *
     * @param gui the new world gui
     */
    public final void setWorldGUI(WorldGUI gui) {
    	
    	this.gui = gui;
    	
    }
    
    //--------------------------------------------------------------------------------
	// State Management methods
	//--------------------------------------------------------------------------------

    /**
     * Retorna o estado atual de uma entidade.
     *
     * @param entityName the entity name
     * @param attribute the attribute
     * @return variável do estado de uma entidade
     */
    public final Object getEntityStateAttribute(String entityName, String attribute) {

    	if (entities.containsKey(entityName)) {
    		return (entities.get(entityName)).getEntityStateAttribute(attribute);
    	} else {
    		return null;
    	}
    	
    }

    /**
     * Adds the entity state attribute.
     *
     * @param entityName the entity name
     * @param attribute the attribute
     * @param value the value
     */
    public final void addEntityStateAttribute(String entityName, String attribute, Object value) {

    	EntityState entity = entities.get(entityName);
    	if (entity != null) {
        	entity.attributes.put(attribute, value);
    	} else {
    		System.err.println("[ERROR] EntityState does not exist!");
    	}
    
    }
    
    /**
     * Removes the entity state attribute.
     *
     * @param entityName the entity name
     * @param attribute the attribute
     * @return the object
     */
    public final Object removeEntityStateAttribute(String entityName, String attribute) {

    	return entities.get(entityName).attributes.remove(attribute);
    
    }
    
	//--------------------------------------------------------------------------------
	// Command Interface
	//--------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#getAddress()
	 */
	@Override
	public final String getAddress() {
		return "/" + Constants.FRAMEWORK_NAME + "/" + envAgent.getAgentName() + "/" + Constants.WORLD;
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public final void receiveCommand(Command cmd) {
//        System.out.println("[" + getAddress() +"] Command received: " + cmd);
		if (cmd.getCommand().equals(Constants.CMD_PARAMETER)) {
			String param = cmd.getParameter("NAME");
			String value = cmd.getParameter("VALUE");
			if (param != null && value != null && parameters.containsKey(param)) {
				// TODO Alguns parâmetros não podem ser mudados!
				// Calls user method
				if (!parameterUpdate(param, value)) {
					return;
				}
				parameters.put(param, value);
				// Let the console knows about the updated parameter
				cmd = new Command(getAddress(), "/console", "UPDATE");
				cmd.addParameter("AGENT", Constants.ENVIRONMENT_AGENT);
				cmd.addParameter("WORLD", Constants.WORLD);
				cmd.addParameter("NAME", param);
				cmd.addParameter("VALUE", value);
				sendCommand(cmd);
			}
		} else {
		    processCommand(cmd);
		}
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#sendCommand(ensemble.Command)
	 */
	@Override
	public final void sendCommand(Command cmd) {
		envAgent.sendCommand(cmd);
	}
	
	//--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------
    
    /* (non-Javadoc)
	 * @see ensemble.LifeCycle#configure()
	 */
	@Override
	public boolean configure() {
    	return true;
	}

	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#init()
	 */
	@Override
	public boolean init() {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "init()");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#parameterUpdate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean parameterUpdate(String name, String newValue) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see ensemble.LifeCycle#finit()
	 */
	@Override
	public boolean finit() {
		return true;
	}

	/**
	 * Called when an entity is added from the World. Must be overrided by the user.
	 *
	 * @param entityName the entity name
	 */
	protected void entityAdded(String entityName) {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "entityAdded()");
	}

	/**
	 * Called when an entity is removed from the World. Must be overrided by the user.
	 *
	 * @param entityName the entity name
	 */
	protected void entityRemoved(String entityName) {
//		MusicalAgent.logger.info("[" + envAgent.getAgentName() + ":" + getEventType() + "] " + "entityRemoved()");
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
		
	}
	
}