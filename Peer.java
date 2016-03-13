import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class Peer {

	private String SHARED_FILE_PATH;
	
	private Socket mySocket;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	
	Peer(String sharedFilePath, Socket incomingSocket) throws IOException {
		SHARED_FILE_PATH = sharedFilePath;
		this.mySocket = incomingSocket;
		inFromClient = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		outToClient = new DataOutputStream(mySocket.getOutputStream());
		
	}
	
	BufferedReader getInputStream() {
		return inFromClient;
	}
	
	DataOutputStream getDataOutputStream() {
		return outToClient;
	}

	void sendFileList(){
		System.out.println("Sending file list...");
		File dir = new File(SHARED_FILE_PATH);
		StringBuffer allFileNames = new StringBuffer();
		if (dir.exists()) {			
			for (final File fileEntry : dir.listFiles()) {
				allFileNames.append(fileEntry.getName()+",");
			}			
		}
		try {
			outToClient.writeBytes(allFileNames.toString()+"\n");
			System.out.println("Sent file names " + allFileNames);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	void sendFile(String fileName) {
		InputStream in = null;
		OutputStream out = null;
		File file = new File("\\" + fileName);
		// Get the size of the file
		long length = file.length();
		byte[] bytes = new byte[16 * 1024];
		int count;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			System.out.println("File not found. ");
		}
		try {
			out = mySocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	String requestFileList() {
		String fileList = "";
		try {
			outToClient.writeBytes("L\n"); //no problem?
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileList = inFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileList;
	}
	
	void requestFile(String fileName) {
		OutputStream out = null;
		InputStream in = null;
		try {
			outToClient.writeBytes("F" + fileName+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out = new FileOutputStream("\\" + fileName);
		} catch (FileNotFoundException ex) {
			System.out.println("File not found. ");
		}
		try {
			in = mySocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = new byte[100*1024];
		int count;
		try {
			while ((count = in.read(bytes)) > 0) {
			out.write(bytes, 0, count);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	void terminateSync() {
		try {
			outToClient.writeBytes("D\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
