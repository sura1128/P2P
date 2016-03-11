import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

class Peer {

	private String IP_ADDRESS = "1234567612621";
	private String SHARED_FILE_PATH = Peer.class.getProtectionDomain().getCodeSource().getLocation().getPath() + 
			File.separator + "Hulk_Shared_Files"+ File.separator;

	public void connectToClient() throws Exception {

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
	
	private List<File> getFiles() {
		File dir = new File(SHARED_FILE_PATH);
		List<File> missingFiles = new ArrayList<File>();
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				missingFiles.add(fileEntry);
			}
			
		}
		return missingFiles;
	}


	private void connectToServer(String IP) throws UnknownHostException, IOException {
		Socket clientSocket = new Socket(IP, 6789);
		String command = "";

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		outToServer.writeBytes("START SYNC" + '\n'); // SEND START COMMAND
		command = inFromServer.readLine();
		
		//Read files from server
		compareFiles();
	}
	
	private void compareFiles() {
		
	}

	public void main(String argv[]) throws Exception {
				
		
		 BufferedReader inFromUser = new BufferedReader(new
		 InputStreamReader(System.in));
		 int command = 0;
		 System.out.println("Choose one of the given options: ");
		 System.out.println("1. SYNC TO CLIENT");
		 System.out.println("2. SYNC FROM SERVER");
		 System.out.println("3. EXIT");
		
		 while (true) {
		 command = Integer.parseInt(inFromUser.readLine());
		 if (command == 3) {
		 break;
		 }
		 switch (command) {
		 case 1: //Allow clients to sync
		 System.out.println("Connecting to other peers ...");
		 connectToClient();
		 break;
		
		 case 2: //Sync from other clients
		 System.out.println("ENTER IP ADDRESS: ");
		 String IP = inFromUser.readLine();
		 connectToServer(IP);
		 break;
		 }
		
		 }

	}
}
