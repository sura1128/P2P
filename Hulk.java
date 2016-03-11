import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class Hulk {

	private static String IP_ADDRESS = "1234567612621";
	private final static String SHARED_FILE_PATH = Paths.get(".").toAbsolutePath().normalize().toString()
			+ File.separator + "src" + File.separator + "Hulk_Shared_Files";

	public static void connectToClient() throws Exception {

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
				
				outToClient.writeBytes("SEND FILE NAMES"); //ASK FOR FILE NAMES
				fileNames = inFromClient.readLine();
				
				if (haveSameFiles(fileNames) == false) { //COMPARE AND SYNC
					missingFiles = synchronize(fileNames);
					for (int i = 0; i < missingFiles.size(); i++) {
						byte[] bytes = new byte[16 * 1024];
						InputStream in = new FileInputStream(missingFiles.get(i));
						int ctr;
						while ((ctr = in.read(bytes)) > 0) {
							outToClient.write(bytes, 0, ctr);
						}
						in.close();
					}
				}
			} else {
				if (command == "STOP SYNC") { //RECEIVE STOP
					welcomeSocket.close();
					break;
				}
			}

		}
	}
	// Helper Files
	private static boolean haveSameFiles(String filenames) { //Check if client and server have same files
		return false;
	}

	private static List<File> synchronize(String fileNames) { //Send back a list of files to send to client
		String filePath = new File(".").getAbsolutePath();
		System.out.println(filePath);

		List<File> missingFiles = new ArrayList<File>(); // THIS IS A STUB THIS IS A STUB!!!!
		File test = new File(filePath + "test.txt"); // SO IS THIS!!

		return missingFiles;
	}

	private static void connectToServer(String IP) throws UnknownHostException, IOException {
		Socket clientSocket = new Socket(IP, 6789);
		StringBuffer fileNames = new StringBuffer();

		String command = "";

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

		outToServer.writeBytes("START SYNC" + '\n'); // SEND START COMMAND
		command = inFromServer.readLine();
		
		if (command.equals("SEND FILE NAMES")) { //GETTING FILE NAMES
			File dir = new File("Hulk_Shared_Files");
			if (dir.exists()) {
				for (final File fileEntry : dir.listFiles()) {
			        if (fileEntry.isDirectory()) {
			        	fileNames.append(fileEntry.getName()+",");
			        }
			    }
			}
			String temp = fileNames.toString(); // SENDING FILES TO SERVER
			outToServer.writeBytes(temp);
		}
	}

	public static void main(String argv[]) throws Exception {
				
		
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
