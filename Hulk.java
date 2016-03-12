import java.io.File;
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
	 final static boolean HAS_IP = false;
	 static String IP_ADDRESS;
	 final static int port = 0;
	 final static String SHARED_FILE_PATH = Hulk.class.getProtectionDomain().getCodeSource().getLocation().getPath() + 
				File.separator + "Hulk_Shared_Files"+ File.separator;
	 
	 
	public static void main(String argv[]) throws Exception {
		
		 Peer hulk;
		 String peerFiles = "";
		 
		 Stack<String> missingFilesList = new Stack<String>();
		 List<String> myFilesList = new ArrayList<String>();
		 List<String> peerFileList =  new ArrayList<String>();
		 
		 //IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
		
		 
		 if (HAS_IP == false) {
			 ServerSocket welcomeSocket = new ServerSocket(6789);
			 Socket outgoingSocket = welcomeSocket.accept();
			 hulk = new Peer(SHARED_FILE_PATH, outgoingSocket);
			 System.out.println("Established connection.");			 
		 }else {
			 //obtain IP somehow
			 Socket hulkSocket = new Socket(IP_ADDRESS, port);
			 hulk = new Peer(SHARED_FILE_PATH, hulkSocket);
			 
		 } 
		 
		 while (true) {
			 if (hulk.getInputStream().ready()) { //Being server
				 String input = hulk.getInputStream().readLine();
				 if (input.equals("L")) {
					 hulk.sendFileList();
				 } else if (input.charAt(0) == 'F') {
					 String fileName = input.substring(1);
					 hulk.sendFile(fileName);
				 }
			 }else{
				 if(peerFiles == null){ //Being client
					 peerFiles = hulk.requestFileList();
					 peerFileList = convertToList(peerFiles);
					 myFilesList = getMyFiles();
					 missingFilesList = getMissingFiles(myFilesList, peerFileList);
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
		return missingFiles;
	}

}
