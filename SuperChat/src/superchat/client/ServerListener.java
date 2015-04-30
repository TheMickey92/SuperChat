package superchat.client;

import java.io.IOException;
import java.util.Scanner;

public class ServerListener extends Thread{
	
	Scanner in;
	Client client;
	
	public ServerListener(Scanner in, Client client) throws IOException {
		this.in = in;
		this.client = client;
	}
	
	public void run() {
		
		String networkInput = "";		
		
		do {
			
			try {
				
				networkInput = in.nextLine();
				System.out.println(networkInput);
				
			} catch (Exception e){
				
				client.stopClient(client);

			}
			
		} while (true);
		
	}

}
