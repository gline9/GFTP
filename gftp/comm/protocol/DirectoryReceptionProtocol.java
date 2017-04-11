package gftp.comm.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class DirectoryReceptionProtocol extends ServerInstructionHandler{

	@Override
	public void handleCommand(DataOutputStream out, DataInputStream in, File workingDirectory) throws IOException{
		String location = in.readUTF();
		String name = in.readUTF();
		File file = new File(workingDirectory, location + name);
		file.mkdir();
	}

}
