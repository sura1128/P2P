import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Thor {
	
	private String SHARED_FILE_PATH = Thor.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ "../src" + File.separator + "Thor_Shared_Files" + File.separator;;
	private ServerSocket serverSocket;
	private Socket receiverSocket;

	public void synchronize(String argv[]) throws Exception {

		String peerName = "";

		// IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Peer thor = new Peer(SHARED_FILE_PATH, InetAddress.getLocalHost().getHostAddress());
		thor.getPeerList().add(InetAddress.getLocalHost().getHostAddress());
		
		while (true) {
			if (thor.getPeerList().isEmpty()) {
				System.out.println("I'm a server");
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
				thor.sendIP();
				receiverSocket.close();
				thor.receive();

			}

		}

	}

	
}
