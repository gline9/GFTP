package gftp.comm.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import gftp.fileio.FileTransmission;

public class FileReceptionProtocol extends ServerInstructionHandler{

	@Override
	public void handleCommand(DataOutputStream out, DataInputStream in, File workingDirectory) throws IOException{
		String location = in.readUTF();
		String name = in.readUTF();
		FileTransmission.recvFile(in, new File(workingDirectory, location + name).toString());
	}

}
