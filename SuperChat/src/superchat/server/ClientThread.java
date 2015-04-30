package superchat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class ClientThread extends Thread{
	
	private final String QUIT_STRING = "::kickmyass::";
	public static final String FANCY_SERVER_LOGIN_STRING = "-.-.-.-.-.-.-ICHBINEINSERVER!!!-.-.-.-.-.-.-";
	public final static String SERVER_MESSAGE_STRING = "-.-.-.-.-.-DIESISTEINESERVERNACHRICHT-.-.-.-.-.-";
	public static final String PARTIAL_HISTORY_QUERY_STRING = "::GET ";
	
	Socket client;
	Controller controller;
	Scanner in;
	PrintWriter out;
	String username;
	Boolean isForeignServer;
	
	public ClientThread(Socket client, Controller controller, Boolean isServer) throws IOException{
		
		this.client = client;
		this.controller = controller;
		this.isForeignServer = isServer;
		in = new Scanner(client.getInputStream());
		out = new PrintWriter(client.getOutputStream(), true);
		username = FANCY_SERVER_LOGIN_STRING;
		
	}
	
	public void writeMessage(String message) {
		out.println(message);
	}
	

	public void run() { 
		try {
		System.out.println("Client connected...");
		
		String input = QUIT_STRING;
		
		if(!isForeignServer) {
			
				writeMessage("Willkommen im SuperChat!!");
				writeMessage("Bitte geben Sie Ihren Benutzernamen ein: ");			
				username = sanitizeUsername(in.nextLine());
			
			if(username.equals(FANCY_SERVER_LOGIN_STRING)) {
				controller.receiveServerXmlMessage(client);
				System.out.println("SENDING HISTORY...");
				controller.sendServerMessage(client, new String(Files.readAllBytes(Paths
										.get(Controller.XML_PATH))));
				controller.addActiveServer(client);

			} else {
			
				List<String> missedMessages = controller.getMissedMessages(username);
				
				for (String missedMessage : missedMessages) {
					writeMessage(missedMessage);
				}		
			}
			
		}
		
		do {
			input = in.nextLine();
			
			if(input.startsWith(PARTIAL_HISTORY_QUERY_STRING)) {
				controller.getPartialHistory(input, this);				
				continue;
			}
			
			if(input.startsWith(SERVER_MESSAGE_STRING)) {
				input = controller.removeServerString(input, SERVER_MESSAGE_STRING);
				controller.broadcast(input, username, this, true);
			} else {
				controller.broadcast(input, username, this, false);
			}			
			
		} while (!input.equals(QUIT_STRING));
		
		
			client.close();
		} catch (Exception e) {
			System.out.println("Client disconnected...");
		}
	}
	
	private String sanitizeUsername(String dirtyUsername) {
		String sanitizedUsername = "";
		
		for (int i = 0; i < dirtyUsername.length(); i++) {
			int possibleDirtyChar = dirtyUsername.charAt(i);
			if(possibleDirtyChar >= 33 
					&& possibleDirtyChar <= 176 
					&& possibleDirtyChar != 39 
					&& possibleDirtyChar != (int)']' 
					&& possibleDirtyChar != (int)'[' 
					&& possibleDirtyChar != (int)':') {
				sanitizedUsername += (char) possibleDirtyChar;
			}
		}
		
		return sanitizedUsername;
	}

}
