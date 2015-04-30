/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package superchat.server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author MarcelB
 */
public class HistoryHandler {

    public void Merge(String xmlMain, String xmlFrom) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        ArrayList<Message> messagesMain = readXMLFile(xmlMain);
        ArrayList<Message> messagesFrom = readXMLFile(xmlFrom);
                
        for(Message message:messagesFrom){
            if(!listContains(messagesMain, message.getId()))
                messagesMain.add(message);
        }
        
        Collections.sort(messagesMain);
        
        CreateXML(xmlMain, messagesMain);
    }
    
    private boolean listContains(ArrayList<Message> list, String id)
    {
        for(Message message:list){
            if(message.getId().equals(id))
                return true;
        }
        
        return false;
    }

    public void CreateXML(String path, ArrayList<Message> messages) throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("history");
        doc.appendChild(rootElement);
        
        // Create MessageNode
        for(Message message: messages) {
            createMessageNode(doc, rootElement, message);
        }
        
        // write the content into xml file
        writeXML(doc, path);
    }

    private void writeXML(Document doc, String path) throws TransformerFactoryConfigurationError, TransformerException, TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(path));

        transformer.transform(source, result);
    }

    private void createMessageNode(Document doc, Element rootElement, Message message) throws DOMException {
        // message elements
        Element messageElement = doc.createElement("message");
        rootElement.appendChild(messageElement);

        // set attribute to message element
        Attr attr = doc.createAttribute("id");
        attr.setValue(message.getId());
        messageElement.setAttributeNode(attr);

        // Elements
        Element dateElement = doc.createElement("date");
        dateElement.appendChild(doc.createTextNode(message.getDate()));
        messageElement.appendChild(dateElement);
        
        Element timeElement = doc.createElement("time");
        timeElement.appendChild(doc.createTextNode(message.getTime()));
        messageElement.appendChild(timeElement);
        
        Element userElement = doc.createElement("user");
        userElement.appendChild(doc.createTextNode(message.getUser()));
        messageElement.appendChild(userElement);
        
        Element textElement = doc.createElement("text");
        textElement.appendChild(doc.createTextNode(message.getText()));
        messageElement.appendChild(textElement);
    }
    
    public void AppendToXML(String path, Message message) throws ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException{
        
    	File file = new File(path);
    	if(!file.exists()) {
    		ArrayList<Message> messages = new ArrayList<Message>();
    		messages.add(message);
    		CreateXML(path, messages);
    		return;
    	}
    	
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        
        Document doc = docBuilder.parse(path);
        
        // root elements
        Element rootElement = doc.getDocumentElement();
        
        // Create MessageNode
        createMessageNode(doc, rootElement, message);
        
        // write the content into xml file
        writeXML(doc, path);
    }
    
    private ArrayList<Message> readXMLFile(String path) throws ParserConfigurationException, SAXException, IOException {
        ArrayList<Message> messages = new ArrayList<Message>();

        File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

	doc.getDocumentElement().normalize();

        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        NodeList nList = doc.getElementsByTagName("message");

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                
                Element eElement = (Element) nNode;

                String sDate = eElement.getElementsByTagName("date").item(0).getTextContent();
                String sTime = eElement.getElementsByTagName("time").item(0).getTextContent();
                String user = eElement.getElementsByTagName("user").item(0).getTextContent();
                String text = eElement.getElementsByTagName("text").item(0).getTextContent();
                
                Message message = new Message(sDate, sTime, user, text);
                messages.add(message);
            }
        }

        return messages;
    }
    
    public ArrayList<Message> GetMissedMessages(String xmlPath, String user) throws ParserConfigurationException, SAXException, IOException
    {
        ArrayList<Message> messages = readXMLFile(xmlPath);
        return GetMissedMessages(messages, user);
    }
    
    public ArrayList<Message> GetMissedMessages(ArrayList<Message> allMessages, String user)
    {
        ArrayList<Message> missedMessages = new ArrayList<Message>();
        
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            Message message = allMessages.get(i);
            if (message.getUser().equals(user)) {
                break; // Abbrechen, wenn der Nutzer zuletzt etwas geschrieben hat
            } else {
               missedMessages.add(message);
            }
        }       
        
        return missedMessages;
    }
    
    public List<Message> GetPartitialHistory(String historyPath, String fromDate, String fromTime, String toDate, String toTime) throws ParseException, ParserConfigurationException, SAXException, IOException
    {
        String sFrom = fromTime + " " + fromDate;
        String sTo = toTime + " " + toDate;
        return GetPartialHistory(historyPath, sFrom, sTo);
    }
    
    public List<Message> GetPartialHistory(String historyPath, String sFrom, String sTo) throws ParseException, ParserConfigurationException, SAXException, IOException
    {
        DateFormat format = new SimpleDateFormat("HH:mm yyyy-MM-dd");
        Date from = format.parse(sFrom);
        Date to = format.parse(sTo);
        
        return GetPartialHistory(historyPath, from, to);
    }
    
    public List<Message> GetPartialHistory(String historyPath, Date from, Date to) throws ParserConfigurationException, SAXException, IOException, ParseException
    {
        List<Message> partialMessages = new ArrayList<Message>();
        // Gesamte History auslesen
        List<Message> allMessages = readXMLFile(historyPath);
        
        Collections.sort(allMessages); // Nochmal sortieren, damit Nachrichten auch wirklich in der korrekten Reihenfolge sind
        
        // Nachrichten auslesen, die in den entsprechenden Zeitraum passen.
        for(Message message:allMessages)
        {
            Date date = message.GetDate();
            // Wenn Datum gleich oder zwischen den beiden angegeben Zeiten, dann zu partiellen Liste hinzufügen
            if(from.equals(date) || to.equals(date) || (date.after(from) && date.before(to)))
            {
                partialMessages.add(message);
            }
        }
        
        // Gebe partielle Liste zurück
        return partialMessages;
    }
}
