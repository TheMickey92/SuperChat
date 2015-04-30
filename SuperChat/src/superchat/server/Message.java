/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package superchat.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author MarcelB
 */
public class Message implements Comparable<Message> {
    private final String id;
    private final String date;
    private final String time;
    private final String user;
    private final String text;
    public Message(String date, String time, String user, String text){
        this.date = date;
        this.time = time;
        this.user = user;
        this.text = text;
        
        id = date + time + user + text.substring(0, Math.min(text.length(), 20));
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getTime() {
        return time;
    }

    public String getUser() {
        return user;
    }

    public int compareTo(Message message) {
        DateFormat format = new SimpleDateFormat("HH:mm yyyy.MM.dd");
        
        
        String string = message.getTime() + " " + message.getDate();
        String string1 = time + " " + date;
        Date date1, date;
        try {
            date = format.parse(string);
            date1 = format.parse(string1);            
            
        if(date1.after(date))
        {
            return 1;
        } else if (date1.equals(date)) {
        	
        	return 0;
        }
        return -1;
        
        } catch (ParseException ex) { }
               
        return 0;
    }
    
    public Date GetDate() throws ParseException
    {
        DateFormat format = new SimpleDateFormat("HH:mm yyyy.MM.dd");
        return format.parse(time + " " + date);
    }
    
    public static Message StringToMessage(String string)
    {        
        // Zunächst bei erstem '[' splitten, um Benutzernamen links davon zu erhalten
        String[] first = string.split("\\[");
        String username = first[0];
        
        // Bei Leerzeichen zwischen Datum und Uhrzeit splitten, um das Datum links zu erhalten
        String[] second = first[1].split(" ");
        String date = second[0];
        
        // Bei "]:" splitten, um Uhrzeit links zu haben. Die Restnachricht liegt jetzt rechts davon
        String secondRest = "";
        for (int i = 1; i < second.length; i++) {
            secondRest += second[i];
        }
        
        String[] third = secondRest.split("\\]:");
        String time = third[0];
        
        // Alle Restteile zur Nachricht hinzufügen (für den Fall, das in der Nachricht ebenfalls "]:" vorkam
        String message = "";
        for (int i = 1; i < third.length; i++) {
            message += third[i];
        }
        
        // Nachrichtenobjekt erstellen und zurückgeben
        Message m = new Message(date, time, username, message);
        return m;
    }
}
