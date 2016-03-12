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

public class Thor {
	static boolean HAS_IP = false;
	static String IP_ADDRESS;
	final static int port = 6789;
	final static String SHARED_FILE_PATH = Thor.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ File.separator + "thor_Shared_Files" + File.separator;

	private static List<String> peerList = new ArrayList<String>();
	private static Peer thor;
	private static String peerFiles = null;
	private static Stack<String> missingFilesList = new Stack<String>();
	private static List<String> myFilesList = new ArrayList<String>();
	private static List<String> peerFileList = new ArrayList<String>();
	
	private static Socket receiverSocket;
	private static Socket senderSocket;
	private static ServerSocket serverSocket;
	
	public static void main(String argv[]) throws Exception {
		
		String peerName = "";
		boolean isSender = false;

		// IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		peerList.add("Yo"); //STUB STUB - testing
		
		while (true) {
			if (!(peerList.isEmpty())) {
				System.out.println("I'm a client");
				IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
				receiverSocket = new Socket(IP_ADDRESS, 6789);
				thor = new Peer(SHARED_FILE_PATH, receiverSocket);
				isSender = false;
				receive();				
				send(serverSocket, isSender);
				receiverSocket.close();
				// Listen till timeout 
			}
			else {				
				serverSocket = new ServerSocket(6789);
				serverSocket.setSoTimeout(10000);
				try {					
					isSender = true;
					send(serverSocket,isSender);
					receive();
					senderSocket.close();
				} catch (java.io.InterruptedIOException e) {
					System.out.println("Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					peerList.add(peerName);
					
				}
				
			}

		
		}

	}
	static void receive() {
		 System.out.println("Request - Thor");
		 peerFiles = thor.requestFileList();
		 peerFileList = convertToList(peerFiles);
		 int peerFileLength = peerFileList.size();
		 myFilesList = getMyFiles();
		 missingFilesList = getMissingFiles(myFilesList, peerFileList);
		 
		 while(true) {
			 if(!missingFilesList.isEmpty()){
				 String fetchFile = missingFilesList.pop();
				 thor.requestFile(fetchFile);
				 if (peerFileLength == myFilesList.size()) {
					 System.out.println("Ending sync");
					 thor.terminateSync();
					 break;
				 }
			 }
		 }
		 
	}


	static void send(ServerSocket welcomeSocket, boolean isSender) throws IOException {
		 System.out.println("Send - Thor");
		while (true) {
			if (isSender == true) {
				senderSocket = welcomeSocket.accept();
				thor = new Peer(SHARED_FILE_PATH, senderSocket);
			}			
			String input = thor.getInputStream().readLine();
			 if (input.equals("L")) {
				 thor.sendFileList();
			 } else if (input.charAt(0) == 'F') {
				 String fileName = input.substring(1);
				 thor.sendFile(fileName);
			 } else if (input.equals("D")) {
				 System.out.println("Terminated sync.");
				 break;
			 }
		}

	}

	
	static List<String> convertToList(String filenames) {
		List<String> list = new ArrayList<String>();
		String fileNames[] = filenames.split(",");
		for (int i = 0; i < fileNames.length - 1; i++) {
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
		for (int i = 0; i < peerFileList.size(); i++) {
			missingFiles.push(peerFileList.get(i));
		}
		System.out.println("Missing files " + missingFiles.size());
		return missingFiles;
	}

}
