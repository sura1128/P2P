import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Peer {

	private String SHARED_FILE_PATH;
	private String IP_ADDRESS;
	private String PEER_NAME;

	private String peerFiles = null;
	private static List<String> peerList = new ArrayList<String>();
	private Stack<String> missingFilesList = new Stack<String>();
	private List<String> myFilesList = new ArrayList<String>();
	private List<String> peerFileList = new ArrayList<String>();
	private static List<String> PEERNAMES = new ArrayList<String>();

	private Socket receiverSocket;
	private Socket senderSocket;
	private ServerSocket serverSocket;

	private Socket mySocket;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	Peer(String SHARED_FILE_PATH, String IP_ADDRESS, String PEER_NAME) {
		this.SHARED_FILE_PATH = SHARED_FILE_PATH;
		this.IP_ADDRESS = IP_ADDRESS;
		this.PEER_NAME = PEER_NAME;
	}

	void initializeStreams(Socket incomingSocket) throws IOException {
		this.mySocket = incomingSocket;
		inFromClient = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		outToClient = new DataOutputStream(mySocket.getOutputStream());
	}

	List<String> getPeerList() {
		return peerList;
	}

	void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	void handShake() {
		try {
			outToClient.writeBytes("Hello\n");
			outToClient.flush();
		} catch (Exception e) {			
			System.out.println("Handled Socket Writing errors in Handshake.");
		}
	}
	
	String requestClientName() {
		String name="";
		try {
			outToClient.writeBytes("NAME\n");
			name = inFromClient.readLine();
			outToClient.flush();
		} catch (IOException e) {
			System.out.println("Handled Socket Writing errors in Client name Request.");
		}
		return name;
		
	}
	
	void sendClientName() {
		try {
			outToClient.writeBytes(PEER_NAME+"\n");
		} catch (IOException e) {
			System.out.println("Handled Socket Writing errors in Sending client name.");
		}
	}

	void sendIP() {
		try {
			outToClient.writeBytes("I" + InetAddress.getLocalHost().getHostAddress() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void sendFileList() {

		File dir = new File(SHARED_FILE_PATH);
		StringBuffer allFileNames = new StringBuffer();
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				allFileNames.append(fileEntry.getName() + ",");
			}
		}
		try {
			outToClient.writeBytes(allFileNames.toString() + "\n");
			System.out.println("Sent file names " + allFileNames);
			outToClient.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void sendFile(String fileName) {
		InputStream in = null;
		OutputStream out = null;
		File file = new File(SHARED_FILE_PATH + fileName);
		// Get the size of the file
		byte[] bytes = new byte[16 * 1024];
		int count;
		try {
			in = new FileInputStream(file);
			out = mySocket.getOutputStream();
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
			outToClient.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String requestFileList() {
		String fileList = "";

		try {
			outToClient.writeBytes("REQLIST\n");
			fileList = inFromClient.readLine();
			outToClient.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileList;
	}

	void requestFile(String fileName) {
		OutputStream out = null;
		InputStream in = null;

		try {
			outToClient.writeBytes("F" + fileName + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out = new FileOutputStream(SHARED_FILE_PATH + fileName);
		} catch (FileNotFoundException ex) {
			System.out.println("File not found. ");
		}
		try {
			in = mySocket.getInputStream();
			byte[] bytes = new byte[100 * 1024];
			int count;
			while ((count = in.read(bytes)) > 0) {
				out.write(bytes, 0, count);
			}
			outToClient.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	void terminateSync() {
		try {
			outToClient.writeBytes("END\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void receive() throws UnknownHostException, IOException {

		receiverSocket = new Socket(IP_ADDRESS, 6789);
		initializeStreams(receiverSocket);

		peerFiles = requestFileList();

		receiverSocket.close();

		peerFileList = convertToList(peerFiles);
		myFilesList = getMyFiles();
		missingFilesList = getMissingFiles(myFilesList, peerFileList);

		while (true) {
			if (!missingFilesList.isEmpty()) {
				String fetchFile = missingFilesList.pop();

				receiverSocket = new Socket(IP_ADDRESS, 6789);
				initializeStreams(receiverSocket);

				requestFile(fetchFile);

				receiverSocket.close();

			} else {
				receiverSocket = new Socket(IP_ADDRESS, 6789);
				initializeStreams(receiverSocket);
				terminateSync();
				receiverSocket.close();
				break;
			}
		}
	}

	void send() throws IOException {
		while (true) {

			senderSocket = serverSocket.accept();
			initializeStreams(senderSocket);
			System.out.println("Established connection");

			String input = inFromClient.readLine(); // getting stuck

			System.out.println(input);// here again

			if (input.equals("Hello")) {
				handShake();
			} else if (input.equals("NAME")) {
				sendClientName();
				String name = requestClientName();
				if (!isSecureClient(name)) {
					senderSocket.close();
					System.out.println("Insecure client connection. Abort file transfer.");
					break;
				}
				senderSocket.close();
			}
			else if (input.charAt(0) == 'I') {
				peerList.add(input.substring(1));
			} else if (input.equals("REQLIST")) {
				sendFileList();
				senderSocket.close();
			} else if (input.charAt(0) == 'F') {
				String fileName = input.substring(1);
				sendFile(fileName);
				senderSocket.close();
			} else if (input.equals("END")) {
				System.out.println("Terminated sync.");
				senderSocket.close();
				break;
			}
		}

	}
	
	void initializeClient(String name) {
		PEERNAMES.add(name);
	}
	boolean isSecureClient(String name) {
		System.out.println(name);
		if (PEERNAMES.contains(name)){
			return true;
		} else return false;
	}

	List<String> convertToList(String filenames) {
		List<String> list = new ArrayList<String>();
		String fileNames[] = filenames.split(",");
		for (int i = 0; i < fileNames.length; i++) {
			list.add(fileNames[i]);
		}
		return list;
	}

	List<String> getMyFiles() {
		List<String> allFilesList = new ArrayList<String>();
		File dir = new File(SHARED_FILE_PATH);
		if (dir.exists()) {
			for (final File fileEntry : dir.listFiles()) {
				allFilesList.add(fileEntry.getName());
			}
		}
		return allFilesList;
	}

	Stack<String> getMissingFiles(List<String> myFilesList, List<String> peerFileList) {
		peerFileList.removeAll(myFilesList);
		Stack<String> missingFiles = new Stack<String>();
		for (int i = 0; i < peerFileList.size(); i++) {
			System.out.println(peerFileList.get(i));
			missingFiles.push(peerFileList.get(i));
		}
		return missingFiles;
	}

}
