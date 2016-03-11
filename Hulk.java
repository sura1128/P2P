import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Hulk{
	 final static boolean HAS_IP = false;
	 final static String IP_ADDRESS = "";
	 final static int port = 0;
	 
	 
	public static void main(String argv[]) throws Exception {
		 String filePath = Hulk.class.getProtectionDomain().getCodeSource().getLocation().getPath() + 
					File.separator + "Hulk_Shared_Files"+ File.separator;	
		 Peer hulk;
		 String peerFiles = "";
		 
		 Stack<String> missingFilesList = new Stack<String>();
		 List<String> allFilesList = new ArrayList<String>();
		 List<String> peerFileList =  new ArrayList<String>();
		
		 
		 if (HAS_IP == false) {
			 ServerSocket welcomeSocket = new ServerSocket(6789);
			 Socket outgoingSocket = welcomeSocket.accept();
			 hulk = new Peer(filePath, outgoingSocket);
			 
		 }else {
			 //obtain IP somehow
			 Socket hulkSocket = new Socket(IP_ADDRESS, port);
			 hulk = new Peer(filePath, hulkSocket);
			 
		 } 
		 
		 while (true) {
			 if (hulk.getInputStream().ready()) {
				 String input = hulk.getInputStream().readLine();
				 if (input.equals("L")) {
					 hulk.sendFileList();
				 } else if (input.charAt(0) == 'F') {
					 String fileName = input.substring(1);
					 hulk.sendFile(fileName);
				 }
			 }else{
				 if(peerFiles != null){
					 peerFiles = hulk.requestFileList();
					 peerFileList = convertToList(peerFiles);
					 allFilesList = getAllFiles();
					 missingFilesList = getMissingFiles(allFilesList, peerFileList);
				 }else{
					 if(!missingFilesList.isEmpty()){
						 String fetchFile = missingFilesList.pop();
						 hulk.requestFile(fetchFile);
					 }
				 }
			 }
		 }
		 

	}
	
	static List<String> convertToList(String filenames) {
		List<String> list = new ArrayList<String>();
		return list;
	}
	
	static List<String> getAllFiles() {
		List<String> allFilesList = new ArrayList<String>();
		return allFilesList;
	}
	
	static Stack<String> getMissingFiles(List<String> allFiles, List<String> peerFileList) {
		Stack<String> missingFiles = new Stack<String>();
		return missingFiles;
	}
	
	void start(Peer p) {
		
	}
}
