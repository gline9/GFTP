package gftp.test;

import java.io.File;
import java.io.IOException;

import gftp.comm.FTPClient;
import gftp.comm.FTPServer;

public class Tester {
	public static void main(String[] args) {
		try {
			//start a new server
			FTPServer server = new FTPServer((short)5375);
			server.start();
			server.setRootDirectory(new File("I:/FTPTest/Server/"));
			
			//start a client that will connect to the server
			FTPClient client = new FTPClient("2607:ea00:107:1001:7950:b2cb:7826:3761", (short)5375);
			//FTPClient client = new FTPClient("192.168.1.44", (short)5375);
			client.connect();
			
			client.setUserOutput((String m) -> System.out.println(m));
			
			//transfer a file from the client to the server
			client.transmit(new File("I:/"));
			
			//shut down the client
			client.terminate();
			
			System.out.println("shutting down the server");
			
			//shut down the server.
			//server.gentleTerminate();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
