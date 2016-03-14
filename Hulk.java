import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Hulk {
	
	private String SHARED_FILE_PATH = Hulk.class.getProtectionDomain().getCodeSource().getLocation().getPath()
			+ "../src" + File.separator + "Hulk_Shared_Files" + File.separator;;
	private ServerSocket serverSocket;
	private Socket receiverSocket;

	public void synchronize(String argv[]) throws Exception {

		String peerName = "";

		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		Peer hulk = new Peer(SHARED_FILE_PATH, InetAddress.getLocalHost().getHostAddress());
		//hulk.getPeerList().add(InetAddress.getLocalHost().getHostAddress());
		
		while (true) {
			if (hulk.getPeerList().isEmpty()) {
				System.out.println("I'm a server");
				try {
					serverSocket = new ServerSocket(6789);
					serverSocket.setSoTimeout(10000);
					hulk.setServerSocket(serverSocket);
					hulk.send();
					serverSocket.close();
				} catch (java.net.BindException e) {
					System.out.println("The port is not available.");
				} catch (java.io.InterruptedIOException e) {
					System.out.println(
							"Time Out 10 Sec. No Peer found, please enter the IP address of the peer you want to connect to. ");
					peerName = input.readLine();
					hulk.getPeerList().add(peerName);
				}
			} else {
				String IP = hulk.getPeerList().remove(0);
				receiverSocket = new Socket(IP, 6789);
				hulk.initializeStreams(receiverSocket);
				hulk.sendIP();
				receiverSocket.close();
				hulk.receive();

			}

		}

	}

	
}
