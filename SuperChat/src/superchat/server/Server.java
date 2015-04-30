package superchat.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class Server {

	public static void main(String[] args) {
		
		Boolean runServer = true;
		Controller controller = new Controller();
		
		try {
			initializeServer(controller);
		} catch (IOException e2) {
			System.out.println("Couldn't initialize Server");
			e2.printStackTrace();			
		} 


		ServerSocket server = null;
		try {
			server = new ServerSocket(4321);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// Listen to Connection-Requests
		while (runServer) {
			Socket client = null;

			try {	
				// Wait for connection and accept it
				client = server.accept();

				// Start new Thread that listens to connected server
				ClientThread thread = new ClientThread(client, controller, false);
				controller.addThread(thread);
				thread.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			} 

		}
		
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void initializeServer(Controller controller) throws IOException {
		
		// Load IP-Adresses
		File ipAdresses = new File("ipadresses.txt");
		BufferedReader br = new BufferedReader(new FileReader(ipAdresses));
		List<String> serverIPs = new ArrayList<String>();
		while(br.ready()) {
			serverIPs.add(br.readLine());
		}
		br.close();
		
		List<String> localIPs = getLocalIpAdresses();
		
		// Iterate over every stored IP-Adress
		for (String ip : serverIPs) {			
			
			// If it's the own IP skip to next
			if(localIPs.contains(ip)) {
				continue;
			}
			
			try {
				// Try to open Socket-Connection
				Socket socket = new Socket(ip, 4321);
				
				// Store the Connection to Server in Controller
				controller.addActiveServer(socket);	
				
				// Do SuperChat-Protocol specific stuff/identify as server
				controller.readStartup(socket);
				controller.sendServerMessage(socket, ClientThread.FANCY_SERVER_LOGIN_STRING);
				
				// Load local Chat-Hisotry if there is any and send it to remote server
				File file = new File("history.xml");
				if (file.exists()) { 
					controller.sendServerMessage(socket, new String(Files.readAllBytes(Paths.get(Controller.XML_PATH))));
				} else {
					controller.sendServerMessage(socket, " ");
				}
				
				// Receive history of remote server and merge with own
				controller.receiveServerXmlMessage(socket);
				
				System.out.println("Verbindung zum Server mit der IP-Adresse " + ip + " wurde aufgebaut.");
				
				// Start new Thread that listens for connected server
				ClientThread thread = new ClientThread(socket, controller, true);
				thread.start();

			} catch (Exception e1) {
				System.out.println("Verbindung zum Server mit der IP-Adresse " + ip + " konnte nicht aufgebaut werden.");
			}
		}
	}

	private static List<String> getLocalIpAdresses() {
		List<String> localIps = new ArrayList<String>();
		Enumeration<NetworkInterface> e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
			
			while(e.hasMoreElements())
			{
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    Enumeration<InetAddress> ee = n.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) ee.nextElement();
			        localIps.add(i.getHostAddress());
			    }
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		return localIps;
	}

}
