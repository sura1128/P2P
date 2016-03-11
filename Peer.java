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



	void sendFileList() {
		
	}
	
	void sendFile(String fileName) {
		
	}
	
	String requestFileList() {
		return "";
	}
	
	void requestFile(String fileName) {
		
	}

	

}
