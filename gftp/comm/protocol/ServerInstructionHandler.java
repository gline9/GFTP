package gftp.comm.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class ServerInstructionHandler {
	
	private static HashMap<Character, ServerInstructionHandler> handlerRegistry = new HashMap<Character, ServerInstructionHandler>(){
		private static final long serialVersionUID = 1L;

		{
			put('f', new FileReceptionProtocol());
			put('d', new DirectoryReceptionProtocol());
		}
	};
	
	public static void handleCommand(char command, DataOutputStream out, DataInputStream in, File workingDirectory) throws IOException{
		if (handlerRegistry.containsKey(command)){
			handlerRegistry.get(command).handleCommand(out, in, workingDirectory);
		}
	}
	
	
	public abstract void handleCommand(DataOutputStream out, DataInputStream in, File workingDirectory) throws IOException;
	
}
