package mms.commands;

import jade.core.ServiceHelper;

import java.util.HashMap;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

public interface RouterHelper extends ServiceHelper {

	public void sendCommand(CommandClientInterface cmdInterface, Command cmd);
	
	public RouterHelper connect(CommandClientInterface cmdInterface);
	
	public boolean disconnect(CommandClientInterface cmdInterface);
	
}
