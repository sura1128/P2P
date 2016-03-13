import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
			+ "../src" + File.separator + "Thor_Shared_Files" + File.separator;

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
		peerList.add("Yo"); // STUB STUB - testing

		while (true) {
			if (!(peerList.isEmpty())) {
				System.out.println("I'm a client");
				IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
				receiverSocket = new Socket(IP_ADDRESS, 6789);
				thor = new Peer(SHARED_FILE_PATH, receiverSocket);
				isSender = false;
				receive();
				send(isSender);
				receiverSocket.close();
				// Listen till timeout
			} else {
				serverSocket = new ServerSocket(6789);
				serverSocket.setSoTimeout(10000);
				try {
					isSender = true;
					send(isSender);
					receive();
					senderSocket.close();
					serverSocket.close();
				} catch (java.io.InterruptedIOException e) {
					System.out.println(
							"Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					peerList.add(peerName);

				}

			}

		}

	}

	static void receive() throws UnknownHostException, IOException {
		System.out.println("Request - Thor");
		peerFiles = thor.requestFileList();
		peerFileList = convertToList(peerFiles);
		int peerFileLength = peerFileList.size();
		myFilesList = getMyFiles();
		missingFilesList = getMissingFiles(myFilesList, peerFileList);
		while (true) {
			if (!missingFilesList.isEmpty()) {
				String fetchFile = missingFilesList.pop();
				
				receiverSocket = new Socket(IP_ADDRESS, 6789);
				thor = new Peer(SHARED_FILE_PATH, receiverSocket);
				
				thor.requestFile(fetchFile);
				System.out.println("File requested ..." + fetchFile);
				if (peerFileLength == myFilesList.size()) {
					System.out.println("Ending sync");
					thor.terminateSync();
					break;
				}
			}
		}

	}

	static void send(boolean isSender) throws IOException {
		System.out.println("Send - Thor");
		while (true) {
			if (isSender == true) {
				senderSocket = serverSocket.accept();
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
		System.out.println("Hulk files");
		for (int i = 0; i < fileNames.length; i++) {
			System.out.println(fileNames[i]);
			list.add(fileNames[i]);
		}
		return list;
	}

	static List<String> getMyFiles() {
		List<String> allFilesList = new ArrayList<String>();
		File dir = new File(SHARED_FILE_PATH);
		System.out.println("Thor files " + SHARED_FILE_PATH);
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				System.out.println(fileEntry.getName());
				allFilesList.add(fileEntry.getName());
			}
		}
		return allFilesList;
	}

	static Stack<String> getMissingFiles(List<String> myFilesList, List<String> peerFileList) {
		peerFileList.removeAll(myFilesList);
		Stack<String> missingFiles = new Stack<String>();
		for (int i = 0; i < peerFileList.size(); i++) {
			// System.out.println(peerFileList.get(i));
			missingFiles.push(peerFileList.get(i));
		}
		System.out.println("Missing files " + missingFiles.size());
		return missingFiles;
	}

}
