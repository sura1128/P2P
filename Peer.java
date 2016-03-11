import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class Peer {

	private String SHARED_FILE_PATH;
	
	Peer(String sharedFilePath) {
		SHARED_FILE_PATH = sharedFilePath;
		
	}

	void connectToClient() throws Exception {

		String fileNames = "";
		String command = "";
		List<File> missingFiles = new ArrayList<File>();

		ServerSocket welcomeSocket = new ServerSocket(6789);
		Socket connectionSocket = welcomeSocket.accept();
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

		while (true) {
			command = inFromClient.readLine();
			
			if (command == "START SYNC") { //RECEIVE START FROM CLIENT		
					missingFiles = getFiles(); //SEND FILES OVER
					for (int i = 0; i < missingFiles.size(); i++) {
						byte[] bytes = new byte[16 * 1024];
						InputStream in = new FileInputStream(missingFiles.get(i));
						int ctr;
						while ((ctr = in.read(bytes)) > 0) {
							outToClient.write(bytes, 0, ctr);
						}
						in.close();
					}
			} else {
				if (command == "STOP SYNC") { //RECEIVE STOP
					welcomeSocket.close();
					break;
				}
			}

		}
	}
	
	List<File> getFiles() {
		File dir = new File(SHARED_FILE_PATH);
		List<File> missingFiles = new ArrayList<File>();
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				missingFiles.add(fileEntry);
			}			
		}
		return missingFiles;
	}


	void connectToServer(String IP) throws UnknownHostException, IOException {
		Socket clientSocket = new Socket(IP, 6789);
		String command = "";

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		outToServer.writeBytes("START SYNC" + '\n'); // SEND START COMMAND
		command = inFromServer.readLine();
		
		//Read files from server
		compareFiles();
	}
	
	void compareFiles() {
		
	}


}
