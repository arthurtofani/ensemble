package mms.world.law;

import mms.Constants;
import mms.LifeCycle;
import mms.Parameters;
import mms.world.EntityState;
import mms.world.World;

public abstract class Law implements LifeCycle {
	
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

	@Override
	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Parameters getParameters() {
		return this.parameters;
	}

	@Override
	public boolean start() {
		if (world == null) {
			return false;
		}
		if (!init()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean stop() {
		if (!finit()) {
			return false;
		}
		return true;
	}

    //--------------------------------------------------------------------------------
	// User implemented methods
	//--------------------------------------------------------------------------------

	@Override
	public boolean parameterUpdate(String name, Object newValue) {
		return true;
	}
	
	public abstract void changeState(final State oldState, double instant, State newState);

}
