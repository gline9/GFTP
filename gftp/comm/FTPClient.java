package gftp.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import gencode.encrypt.PublicKeyEncryptionService;
import gftp.fileio.FileTransmission;
import gftp.fileio.FileUtils;

public class FTPClient {
	private DataOutputStream out;
	private DataInputStream in;

	private Socket server = null;
	private String serverIP;
	private short port;

	private Consumer<String> userOut = (String s) -> {};
	private Runnable initializer = () -> {};

	public FTPClient(String serverIP, short port) {
		this.serverIP = serverIP;
		this.port = port;
		server = new Socket();
	}

	public FTPClient(OutputStream out, InputStream in) {
		this.out = new DataOutputStream(out);
		this.in = new DataInputStream(in);
	}

	public void setUserOutput(Consumer<String> userOutput) {
		this.userOut = userOutput;
	}

	public void setInit(Runnable init) {
		this.initializer = init;
	}

	public void connect() throws IOException {
		if (server != null) {
			server = new Socket();
			server.connect(new InetSocketAddress(serverIP, port), 3000);

			// this is a test of the encryption services in a real world
			// situation.
			PublicKeyEncryptionService encrypt = new PublicKeyEncryptionService(server.getInputStream(),
					server.getOutputStream(), 64);

			System.out.println("connecting");

			encrypt.init();

			while (!encrypt.isInitialized()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}

			System.out.println("connection successful");

			out = new DataOutputStream(encrypt.getOutputStream());
			in = new DataInputStream(encrypt.getInputStream());
		}
	}

	public void terminate() throws IOException {
		if (server != null) {
			server.setSoLinger(true, 10000);
			server.close();
		} else {
			in.close();
			out.close();
		}
	}

	public void transmit(File file) throws IOException {
		if (!server.isConnected())
			throw new IOException("Server isn't connected, unable to transmit: " + file.toString());
		initializer.run();
		FileUtils.resetFileWalks();
		String[] walkPlusParent = FileUtils.generateFileWalk(file);
		String[] walk = Arrays.copyOfRange(walkPlusParent, 1, walkPlusParent.length);

		// tell server this is a backup
		out.writeChar('b');
		for (String currentFile : walk) {
			userOut.accept("sending: " + currentFile);
			sendFile(out, new File(currentFile), file);
		}
		out.writeUTF("END");
		out.flush();
		userOut.accept("all files sent");
		userOut.accept("Backup finished last at: " + new Timestamp(new Date().getTime()).toString());
	}

	private void sendFile(DataOutputStream outStream, File file, File root) throws IOException {
		String[] description = generateFileDescription(file, root);
		if (file.isDirectory()) {
			// tell the server the file is a directory
			outStream.writeChar('d');
		} else {
			// tell the server the file is a file
			outStream.writeChar('f');
		}
		for (String descript : description) {
			outStream.writeUTF(descript.replaceAll("\\\\", "/"));
		}
		if (!file.isDirectory()) {
			FileTransmission.sendFile(outStream, file.getAbsolutePath());
		}
	}

	private String[] generateFileDescription(File file, File root) {
		File parent = root.toPath().relativize(file.toPath()).toFile().getParentFile();
		String location = "";
		if (parent != null) {
			location = parent.getPath() + "/";
		}
		String name = file.getName();
		String[] results = { location, name };
		return results;
	}

}
