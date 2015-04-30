package superchat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;


public class Controller {
	
	public static final String XML_PATH = "history.xml";
	
	private Vector<ClientThread> clientThreads = new Vector<ClientThread>();
	public Map<Socket, PrintWriter> servers = new HashMap<Socket, PrintWriter>();
	
	HistoryHandler hh = new HistoryHandler();
	Message message = null;
	Date dNow = null;
	
	public void addThread(ClientThread thread) {
		clientThreads.add(thread);		
	}
	
	public void broadcast(String messageText, String username, ClientThread sendingThread, boolean isServerMessage) {
		
				
		if(!isServerMessage) {
			
			dNow = new Date();
			SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd");
			SimpleDateFormat time =  new SimpleDateFormat("HH:mm");
			
			String sDate = date.format(dNow);
			String sTime = time.format(dNow);		
			
			message = new Message(sDate, sTime, username, messageText);		
			try {
				hh.AppendToXML(XML_PATH, message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String assembledMessage = username + "[" + sDate + " " + sTime + "]: " + messageText;	
			
			//Send message to servers			
			for (Socket server : servers.keySet()) {
				servers.get(server).println(ClientThread.SERVER_MESSAGE_STRING + assembledMessage);
			}
			
			//Send message to clients
			for (ClientThread thread : clientThreads) {
				if(!(thread==sendingThread) && !thread.username.equals(ClientThread.FANCY_SERVER_LOGIN_STRING))
				thread.writeMessage(assembledMessage);
			}
			
		} else {
			
			message = Message.StringToMessage(messageText);	
			
			try {
				hh.AppendToXML(XML_PATH, message);
			} catch (Exception e) {
				e.printStackTrace();
			}						
			
			//Send message to clients
			for (ClientThread thread : clientThreads) {
				if(!(thread==sendingThread))
				thread.writeMessage(messageText);
			}
		}
		
	}
	
	public List<String> getMissedMessages(String username) {
	
		ArrayList<Message> missedMessages = null;
		List<String> smissedMessages = new ArrayList<String>();
		
		try {
			missedMessages = hh.GetMissedMessages(XML_PATH, username);
			
			Collections.reverse(missedMessages);
			
			for (Message message : missedMessages) {
				String smessage = message.getUser() + "[" + message.getDate() + " " + message.getTime() + "]: " + message.getText();
				smissedMessages.add(smessage);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return smissedMessages;
	}
	
	
	public void readStartup(Socket socket) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		br.readLine();
		br.readLine();
	}
	
	public void receiveServerXmlMessage(Socket socket) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		final String FOREIGNXML = "foreignHistory.xml";
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String xml = br.readLine();

		File foreignHistory = new File(FOREIGNXML);
		if(foreignHistory.exists()) {
			foreignHistory.delete();
		}
		foreignHistory.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(foreignHistory));
		bw.write(xml);
		bw.close();
		hh.Merge(XML_PATH, FOREIGNXML);		
	}

	public void sendServerMessage(Socket socket,
			String fancyServerLoginString) throws IOException {
		PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		printWriter.println(fancyServerLoginString);
		printWriter.flush();
	}

	public void addActiveServer(Socket socket) {
		try {
			servers.put(socket, new PrintWriter(socket.getOutputStream(), true));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public String removeServerString(String string, String serverString)
	 {
	        // ServerString weghauen und weitersplitten
	        String[] splitted = string.split(serverString);        
	        return splitted[1];
	 }

	public void getPartialHistory(String input, ClientThread clientThread) {
		String datequery = input.split(ClientThread.PARTIAL_HISTORY_QUERY_STRING)[1];
		String[] queryData = datequery.split(" ");
		List<Message> dateMessages = new ArrayList<Message>();
		
		try {			
			dateMessages = hh.GetPartitialHistory(XML_PATH, queryData[0], queryData[1], queryData[2], queryData[3]);			
		} catch (Exception e) {
		}
		
		for (Message message : dateMessages) {
			clientThread.writeMessage(message.getUser() + "[" + message.getDate() + " " + message.getTime() + "]: " + message.getText());
		}
		
	}

}
