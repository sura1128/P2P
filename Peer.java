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
		File dir = new File(SHARED_FILE_PATH);
		StringBuffer allFileNames = new StringBuffer();
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				allFileNames.append(fileEntry.getName());
			}			
		}
		try {
			outToClient.writeBytes(allFileNames.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void sendFile(String fileName) {
		
	}
	
	String requestFileList() {
		return "";
	}
	
	void requestFile(String fileName) {
		
	}

	

}
