package gftp.fileio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import gcore.counter.BitCounter;

public final class FileTransmission {
	private FileTransmission() {}
	
	public static int sendFile(DataOutputStream out, String fileName) throws IOException {
		return sendFile(out, fileName, new BitCounter());
	}
	
	public static int sendFile(DataOutputStream out, String fileName, BitCounter c) throws IOException {
	    FileInputStream fis = new FileInputStream(fileName);
	    int size = 0;
	    try {
	    	size = (int) fis.getChannel().size();
	        byte[] bytes = new byte[8128];
	        int len;
	        out.writeInt(size);
	        while ((len = fis.read(bytes)) > 0) {
	            out.write(bytes, 0, len);
	            c.incrementBytesBy(len);
	        }
	    } finally {
	        fis.close();
	    }
        return size;
	}
	public static int recvFile(DataInputStream in, String fileName) throws IOException {
		return recvFile(in, fileName, new BitCounter());
	}
	public static int recvFile(DataInputStream in, String fileName, BitCounter c) throws IOException {
	    FileOutputStream fos = new FileOutputStream(fileName);
	    int received = 0;
	    try {
	        int left = in.readInt();
	        byte[] bytes = new byte[8128];
	        int len;
	        while(left > 0 && (len = in.read(bytes, 0, Math.min(left, bytes.length))) > 0) {
	            fos.write(bytes, 0, len);
	            received += len;
	            c.incrementBytesBy(len);
	            left -= len;
	        }
	    } finally {
	        fos.close();
	    }
	    	    
	    return received;
	}
	
}
