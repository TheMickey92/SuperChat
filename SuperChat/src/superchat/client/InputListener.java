package superchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class InputListener extends Thread {
	
	PrintWriter out;
	BufferedReader br;
	
	public InputListener(PrintWriter out, BufferedReader br) {

		this.out = out;
		this.br = br;
	}
	
	public void run() {
		
		String input = "";
		
		do {
			
			try {
				
				input = br.readLine();
				out.println(input);
				
			} catch (IOException e) {
				break;
			}			
			
		} while (true);	
			
	}

}
