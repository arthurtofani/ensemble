package mms.world.law;

import mms.Parameters;
import mms.world.EntityState;
import mms.world.World;

public abstract class Law {
	
	protected World 		world;
	protected String 		type;
	protected Parameters 	parameters;
	
	public void setWorld(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	public Parameters getParameters() {
		return this.parameters;
	}

	public boolean configure() {
		return true;
	}

    //--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

	public abstract void changeState(final State oldState, double instant, State newState);

}
