import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Hulk{
	 static boolean HAS_IP = false;
	 static String IP_ADDRESS;
	 final static int port = 6789;
	 final static String SHARED_FILE_PATH = Hulk.class.getProtectionDomain().getCodeSource().getLocation().getPath() + 
				File.separator + "Hulk_Shared_Files"+ File.separator;
	 
	 
	public static void main(String argv[]) throws Exception {
		
		
		 String peerFiles = null;
		 
		 Stack<String> missingFilesList = new Stack<String>();
		 List<String> myFilesList = new ArrayList<String>();
		 List<String> peerFileList =  new ArrayList<String>();
		 int peerFileLength = 0;
		 
		 //IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
		 
		 BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		 System.out.println("Which Peer do you want to connect to? ");
		 String peer = input.readLine();


		 System.out.println("Wait or connect? ");
		 String ans = input.readLine();

		 if (ans.equalsIgnoreCase("wait")) {
			ServerSocket welcomeSocket = new ServerSocket(6789);
		 	send(welcomeSocket);
		 	receiveAfter();
		 } else {
		 	IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
		 	Socket mySocket = new Socket(IP_ADDRESS,6789);
		 	Peer hulk = new Peer(SHARED_FILE_PATH, mySocket);
		 	receive();
		 	sendAfter();
		 }


		
	}

	static void send(ServerSocket welcomeSocket) throws IOException {
		while(true) {
			Socket connectSocket = welcomeSocket.accept();
		 	Peer hulk = new Peer(SHARED_FILE_PATH, connectSocket);
		}
		
	}


	static void sendAfter() {

	}

	static void receiveAfter() {

	}

	static void receive() {

	}

	static List<String> convertToList(String filenames) {
		List<String> list = new ArrayList<String>();
		String fileNames[] = filenames.split(",");
		for(int i=0; i<fileNames.length-1; i++) {
			list.add(fileNames[i]);
		}
		return list;
	}
	
	static List<String> getMyFiles() {
		List<String> allFilesList = new ArrayList<String>();		
		File dir = new File(SHARED_FILE_PATH);
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				allFilesList.add(fileEntry.getName());
			}			
		}
		return allFilesList;
	}
	
	static Stack<String> getMissingFiles(List<String> myFilesList, List<String> peerFileList) {		
		peerFileList.removeAll(myFilesList);
		Stack<String> missingFiles = new Stack<String>();
		for(int i=0; i<peerFileList.size(); i++) {
			missingFiles.push(peerFileList.get(i));
		}
		System.out.println("Missing files " + missingFiles.size());
		return missingFiles;
	}

}
