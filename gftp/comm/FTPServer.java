package gftp.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gencode.encrypt.PublicKeyEncryptionService;
import gftp.comm.protocol.ConnectionType;
import gftp.comm.protocol.ServerInstructionHandler;

public class FTPServer {

	private final ServerSocket server;

	private File root;

	private Listener listener = new Listener();

	private int maxNumOfClients = 10;

	private int serverThreadPoolSize = maxNumOfClients + 1;

	ExecutorService threads;

	/**
	 * initialize the server by giving it the port that should be connected to.
	 * Sets the maximum number of clients to the default of 10
	 * 
	 * @param port
	 *            port for server to bind to
	 * @throws IOException
	 */
	public FTPServer(short port) throws IOException {
		// initialize the server socket
		server = new ServerSocket(port);
		
	}

	/**
	 * initialize the server by giving it the port that should be connected to
	 * and the maximum number of clients that can connect at any given time.
	 * 
	 * @param port
	 *            port for server to bind to
	 * @param maxClients
	 *            maximum number of clients that can connect at any given time.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             if the number of clients passed is less than 1
	 */
	public FTPServer(short port, int maxClients) throws IOException {
		// if there are too few clients given throw an IllegalArgumentException
		if (maxClients < 1)
			throw new IllegalArgumentException("Invalid number of clients for server: " + maxClients);

		// initialize the server socket
		server = new ServerSocket(port);

		// set the maximum number of clients to maxClients.
		maxNumOfClients = maxClients;

		// set the maximum thread pool size to the maximum number of clients + 1
		// for the listener thread.
		serverThreadPoolSize = maxNumOfClients + 1;
	}

	/**
	 * set the root directory the server works out of.
	 * 
	 * @param file
	 *            directory to work out of
	 * @throws IllegalArgumentException
	 *             if the file given is not a directory.
	 */
	public void setRootDirectory(File file) {
		// make sure the file is a directory
		if (!file.isDirectory())
			throw new IllegalArgumentException("File needs to be a directory for server to back up to it");

		// set the root of the server to the file.
		root = file;
	}

	public void setMaximumClientNumber(int maxClients) {
		serverThreadPoolSize = maxClients + 1;
		if (serverThreadPoolSize < 2)
			throw new IllegalArgumentException("Invalid number of clients for server: " + maxClients);
	}

	public void start() {
		System.out.println("size " + serverThreadPoolSize);
		threads = Executors.newFixedThreadPool(serverThreadPoolSize);
		threads.execute(listener);
	}

	/**
	 * execute if you want clients to finish what they are doing and then shut
	 * down the server.
	 * 
	 * @throws IOException
	 */
	public void gentleTerminate() throws IOException {
		// stop the listener from accepting new clients
		listener.stopAccepting();

		// start the shutdown process of the executor
		threads.shutdown();

		// once the executor is shut down close the server socket
		while (!threads.isTerminated()) {

			// wait a second after each is terminated check
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}

		// close the server socket
		server.close();
	}

	/**
	 * execute if you want to shut down the server immediately and kick all
	 * clients off with errors.
	 * 
	 * @throws IOException
	 */
	public void forceTerminate() throws IOException {
		// terminate the executor
		threads.shutdownNow();

		// close the server socket
		server.close();
	}

	private class Listener implements Runnable {
		private volatile boolean isRunning = true;

		@Override
		public void run() {
			try {
				server.setSoTimeout(3000);
				while (isRunning) {
					try {
						Socket socket = server.accept();

						PublicKeyEncryptionService encrypt = new PublicKeyEncryptionService(socket.getInputStream(),
								socket.getOutputStream(), 64);

						encrypt.init();

						while (!encrypt.isInitialized()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {}
						}

						DataInputStream in = new DataInputStream(encrypt.getInputStream());
						DataOutputStream out = new DataOutputStream(encrypt.getOutputStream());

						// execute the new executor at the specified file with
						// the socket.
						threads.execute(new Executor(socket, in, out));
					} catch (SocketTimeoutException e) {}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void stopAccepting() {
			isRunning = false;
		}

	}

	private class Executor implements Runnable {
		private final Socket socket;
		private DataOutputStream out = null;
		private DataInputStream in = null;
		private File parent;
		
		public Executor(Socket s, DataInputStream in, DataOutputStream out) {
			this.socket = s;
			this.in = in;
			this.out = out;
		}

		@Override
		public void run() {
			if (socket.isConnected()) {
				// get the type of ftp from the client
				char type = ' ';
				try {
					type = in.readChar();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				switch (type) {
				case 'b':
					// if it is a backup generate the file from the
					// backup enum in connection type
					parent = new File(root, ConnectionType.Backup.getSubDirectory());
					break;
				}
				parent.mkdirs();
				try {
					while (!socket.isClosed()) {
						char command = in.readChar();
						ServerInstructionHandler.handleCommand(command, out, in, parent);
					}
				} catch (IOException e) {}
			}
		}

	}
}
