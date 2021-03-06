import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Hulk {
	
	private static String SHARED_FILE_PATH = Hulk.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ "../src" + File.separator + "Hulk_Shared_Files" + File.separator;;
	private static ServerSocket serverSocket;
	private static Socket receiverSocket;

	public static void main(String argv[]) throws Exception {

		String peerName = "";

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Peer hulk = new Peer(SHARED_FILE_PATH, InetAddress.getLocalHost().getHostAddress(), "Hulk");
		initializeClients(hulk);
		
		System.out.println("Type 1 to transfer, type 2 to request.");
		String choice = input.readLine();
		if (choice.equals("2")){
			hulk.getPeerList().add(InetAddress.getLocalHost().getHostAddress());
		}
		
		while (true) {
			if (hulk.getPeerList().isEmpty()) {
				System.out.println("Hulk will now send files.");
				try {
					serverSocket = new ServerSocket(6789);
					serverSocket.setSoTimeout(10000);
					hulk.setServerSocket(serverSocket);
					hulk.send();
					serverSocket.close();
				} catch (java.net.BindException e) {
					System.out.println("The port is not available.");
				} catch (java.io.InterruptedIOException e) {
					serverSocket.close();
					System.out.println(
							"Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					hulk.getPeerList().add(peerName);
				}
			} else {
				String IP = hulk.getPeerList().remove(0);
				
				receiverSocket = new Socket(IP, 6789);				
				hulk.initializeStreams(receiverSocket);
				hulk.handShake();
				receiverSocket.close();
				
				receiverSocket = new Socket(IP, 6789);				
				hulk.initializeStreams(receiverSocket);
				String name = hulk.requestClientName();
				hulk.sendClientName();
				if (hulk.isSecureClient(name)) {
					receiverSocket.close();					
					receiverSocket = new Socket(IP, 6789);
					hulk.initializeStreams(receiverSocket);
					hulk.sendIP();
					receiverSocket.close();
					hulk.receive();
				} else {
					receiverSocket.close();
					System.out.println("Insecure Client Login. Abort file transfer.");
					break;
				}

			}

		}

	}
	
	static void initializeClients(Peer thor) {
		thor.initializeClient("Thor");
		thor.initializeClient("IronMan");
	}

	
}
