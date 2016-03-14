import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Thor {

	private static String SHARED_FILE_PATH = Thor.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ "../src" + File.separator + "Thor_Shared_Files" + File.separator;;
	private static ServerSocket serverSocket;
	private static Socket receiverSocket;

	public static void main(String argv[]) throws Exception {

		String peerName = "";

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Peer thor = new Peer(SHARED_FILE_PATH, InetAddress.getLocalHost().getHostAddress(), "Thor");
		initializeClients(thor);

		System.out.println("Type 1 to transfer, type 2 to request.");
		String choice = input.readLine();
		if (choice.equals("2")) {
			thor.getPeerList().add(InetAddress.getLocalHost().getHostAddress());
		}

		while (true) {
			if (thor.getPeerList().isEmpty()) {
				System.out.println("Thor will now send files.");
				try {
					serverSocket = new ServerSocket(6789);
					serverSocket.setSoTimeout(10000);
					thor.setServerSocket(serverSocket);
					thor.send();
					serverSocket.close();
				} catch (java.net.BindException e) {
					System.out.println("The port is not available.");
				} catch (java.io.InterruptedIOException e) {
					System.out.println(
							"Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					thor.getPeerList().add(peerName);
				}
			} else {
				String IP = thor.getPeerList().remove(0);

				receiverSocket = new Socket(IP, 6789);
				thor.initializeStreams(receiverSocket);
				thor.handShake();
				receiverSocket.close();

				receiverSocket = new Socket(IP, 6789);
				thor.initializeStreams(receiverSocket);
				String name = thor.requestClientName();
				thor.sendClientName();
				if (thor.isSecureClient(name)) {
					receiverSocket.close();
					receiverSocket = new Socket(IP, 6789);
					thor.initializeStreams(receiverSocket);
					thor.sendIP();
					receiverSocket.close();
					thor.receive();
				} else {
					receiverSocket.close();
					System.out.println("Insecure Client Login. Abort file transfer.");
					break;
				}

			}

		}

	}

	static void initializeClients(Peer thor) {
		thor.initializeClient("Hulk");
		thor.initializeClient("IronMan");
	}

}
