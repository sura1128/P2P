import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class IronMan {
	
	private static String SHARED_FILE_PATH = IronMan.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ "../src" + File.separator + "Ironman_Shared_Files" + File.separator;;
	private static ServerSocket serverSocket;
	private static Socket receiverSocket;


	public static void main(String argv[]) throws Exception {

		String peerName = "";

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Peer ironman = new Peer(SHARED_FILE_PATH, InetAddress.getLocalHost().getHostAddress(), "ironman");
		initializeClients(ironman);
		
		System.out.println("Type 1 to transfer, type 2 to request.");
		String choice = input.readLine();
		if (choice.equals("2")){
			ironman.getPeerList().add(InetAddress.getLocalHost().getHostAddress());
		}
		
		while (true) {
			if (ironman.getPeerList().isEmpty()) {
				System.out.println("Iron man will now send files.");
				try {
					serverSocket = new ServerSocket(6789);
					serverSocket.setSoTimeout(10000);
					ironman.setServerSocket(serverSocket);
					ironman.send();
					serverSocket.close();
				} catch (java.net.BindException e) {
					System.out.println("The port is not available.");
				} catch (java.io.InterruptedIOException e) {
					serverSocket.close();
					System.out.println(
							"Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					ironman.getPeerList().add(peerName);
				}
			} else {
				String IP = ironman.getPeerList().remove(0);
				
				receiverSocket = new Socket(IP, 6789);				
				ironman.initializeStreams(receiverSocket);
				ironman.handShake();
				receiverSocket.close();
				
				receiverSocket = new Socket(IP, 6789);				
				ironman.initializeStreams(receiverSocket);
				String name = ironman.requestClientName();
				ironman.sendClientName();
				if (ironman.isSecureClient(name)) {
					receiverSocket.close();					
					receiverSocket = new Socket(IP, 6789);
					ironman.initializeStreams(receiverSocket);
					ironman.sendIP();
					receiverSocket.close();
					ironman.receive();
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
		thor.initializeClient("Hulk");
	}

	
}
