package superchat.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
	
	boolean mainloop = true;
	boolean connected = true;
	ServerListener serverListener = null;
	InputListener systemInputListener = null;

	public static void main(String[] args) {		
		
		Client client = new Client();
		
		
		File ipAdresses = new File("ipadresses.txt");
		List<String> serverIPs = new ArrayList<String>();		
		
		try {					
		BufferedReader br = new BufferedReader(new FileReader(ipAdresses));			
		while(br.ready()) {
			serverIPs.add(br.readLine());
		}
		br.close();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Socket socket = null;
		
		while(client.mainloop) {
		
			client.startClient(serverIPs, socket);
			
		}
		

	}
	
	public void stopClient(Client client) {
		client.connected = false;
	}

	@SuppressWarnings("deprecation")
	private void startClient(List<String> serverIPs, Socket socket) {
		
		String username = "";
		this.connected = true;
		socket = null;
		
		socket = connectToServer(serverIPs);		
		
		InputStream in = null;
		PrintWriter networkOut = null;	
		InputStreamReader isr = null;
		BufferedReader br = null;
		Scanner networkIn = null;
		
		try {
			in = System.in;
			isr = new InputStreamReader(in);
			br = new BufferedReader(isr);						
			networkOut = new PrintWriter(socket.getOutputStream(), true);
			networkIn = new Scanner(socket.getInputStream());
			serverListener = new ServerListener(networkIn, this);
			systemInputListener = new InputListener(networkOut, br);
		} catch (IOException e) {
			e.printStackTrace();
		}			
		
		serverListener.start();
		
		//save Username
		try {
			username = br.readLine();
		} catch (IOException e1) {
			System.out.println("Error while reading Username!");
		}
		networkOut.println(username);
		
		systemInputListener.start();
		
		//Loop to check client connection
		while(true) {
			
			if(!connected) {
				
				// If not connected anymore, reconnect to another server
				// Deprecated Methods used to avoid blocking System.in-Reading in Thread
				serverListener.stop();
				systemInputListener.stop();
				socket = connectToServer(serverIPs);
				
				try {
					networkOut = new PrintWriter(socket.getOutputStream(), true);
					networkIn = new Scanner(socket.getInputStream());
					serverListener = new ServerListener(networkIn, this);
					systemInputListener = new InputListener(networkOut, br);
				
					String  rubbish = networkIn.nextLine();
					while(!rubbish.equals("Bitte geben Sie Ihren Benutzernamen ein: ")) {
					rubbish = networkIn.nextLine();
					}
					networkOut.println(username);
					networkOut.println();
					
				} catch (IOException e) {
					e.printStackTrace();
				}				
				
				serverListener.start();
				systemInputListener.start();
				connected=true;
			}
			
		}
		
	}

	private Socket connectToServer(List<String> serverIPs) {
		Socket socket = null;
		for (String ip : serverIPs) {	
			
			try {
				socket = new Socket(ip, 4321);
				System.out.println("Verbindung zum Server mit der IP-Adresse " + ip + " wurde aufgebaut.");
				break;
			} catch (Exception e1) {
				System.out.println("Verbindung zum Server mit der IP-Adresse " + ip + " konnte nicht aufgebaut werden.");
			}
		}
		
		if(socket == null) {
			System.out.println("Alles Scheiﬂe, keine Server zu erreichen!!");
			System.exit(0);
		}
		
		return socket;
	}

}
