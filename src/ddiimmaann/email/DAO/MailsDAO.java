package ddiimmaann.email.DAO;

import ddiimmaann.email.InvalidDatumException;
import ddiimmaann.email.models.AccountMails;
import ddiimmaann.email.models.Mail;
import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.*;

public class MailsDAO
{
    public static AccountMails loadData (String nick) throws IOException,
            InvalidDatumException, NumberFormatException
    {
        AccountMails mails = new AccountMails();
        Document doc = DocumentSaver.loadDocument(nick + ".xml");
        
        if (doc == null || !doc.hasChildNodes() || !doc.getFirstChild().hasChildNodes())
            return mails;
        
        NodeList inOutNodes = doc.getFirstChild().getChildNodes();
        for (int i = 0 ; i < inOutNodes.getLength() ; i++)
        {
            if (inOutNodes.item(i).getNodeName().equals("inMails"))
                if (inOutNodes.item(i).hasChildNodes())
                    setMapOfMails(mails.getInMails(), inOutNodes.item(i).getChildNodes());
            if (inOutNodes.item(i).getNodeName().equals("outMails"))
                if (inOutNodes.item(i).hasChildNodes())
                    setMapOfMails(mails.getOutMails(), inOutNodes.item(i).getChildNodes());
        }
        
        return mails;
    }
    
    private static void setMapOfMails (Map mails, NodeList mailNodes)
    {
        Node node;
        Mail mail;
        for (int i = 0 ; i < mailNodes.getLength() ; i++)
        {
            node = mailNodes.item(i);
            if (!node.hasAttributes() || !node.hasChildNodes())
                continue;
            NodeList content = node.getChildNodes();
            mail = getMail(content);
            if (mail == null)
                continue;
            NamedNodeMap attr = node.getAttributes();
            if (attr.getNamedItem("number") == null)
                continue;
            int number = Integer.parseInt(attr.getNamedItem("number").getNodeValue());
            mail.setNumberMail(number);
            mails.put(number, mail);
        }
    }
    
    private static Mail getMail (NodeList content)
    {
        Node node;
        String from = null, to = null, title = null, message = null, attachments = null;
        Date date = null;
        boolean isSeen = true;
        
        for (int i = 0 ; i < content.getLength() ; i++)
        {
            node = content.item(i);
            switch (node.getNodeName())
            {
                case "from" :
                    from = node.getTextContent();
                    break;
                case "to" :
                    to = node.getTextContent();
                    break;
                case "title" :
                    title = node.getTextContent();
                    break;
                case "message" :
                    message = node.getTextContent();
                    break;
                case "date" :
                    Long dateLong = Long.parseLong(node.getTextContent());
                    date = new Date(dateLong);
                    break;
                case "attachments" :
                    attachments = node.getTextContent();
            }
        }
        if (from == null || to == null)
            return null;
        if (title == null)
            title = "No theme";
        if (message == null)
            message = "";
        
        Mail mail = new Mail(from, to);
        mail.setTitle(title);
        mail.setMessage(message);
        mail.setDate(date);
        mail.setSeen(isSeen);
        if (attachments != null && attachments.length() != 0)
            mail.setAttachments(attachments.split("\\s"));
        return mail;
    }
    
    public static void resaveDataBase (AccountMails mails, String nick) throws
            InvalidDatumException, IOException
    {
        Document doc = DocumentSaver.getNewDocument();
        Set<Map.Entry<Integer, Mail>> setMails;
        Element root = doc.createElement("mails");
        doc.appendChild(root);
        if (!mails.getInMails().isEmpty())
        {
            Element inMails = doc.createElement("inMails");
            setMails = mails.getInMails().entrySet();
            for (Map.Entry<Integer, Mail> mail : setMails)
                inMails.appendChild(getMailElem(mail.getValue(), doc));
            root.appendChild(inMails);
        }
        if (!mails.getOutMails().isEmpty())
        {
            Element outMails = doc.createElement("outMails");
            setMails = mails.getOutMails().entrySet();
            for (Map.Entry<Integer, Mail> mail : setMails)
                outMails.appendChild(getMailElem(mail.getValue(), doc));
            root.appendChild(outMails);
        }
        DocumentSaver.writeDocument(doc, nick + ".xml");
    }
    
    private static Element getMailElem (Mail mail, Document doc)
    {
        Element mailElem = doc.createElement("mail");
        String numberMail = Integer.toString(mail.getNumberMail());
        mailElem.setAttribute("number", numberMail);
        Element child = doc.createElement("from");
        child.setTextContent(mail.getFrom());
        mailElem.appendChild(child);
        child = doc.createElement("to");
        child.setTextContent(mail.getTo());
        mailElem.appendChild(child);
        child = doc.createElement("title");
        child.setTextContent(mail.getTitle());
        mailElem.appendChild(child);
        child = doc.createElement("message");
        child.setTextContent(mail.getMessage());
        mailElem.appendChild(child);
        if (mail.getDate() != null)
        {
            child = doc.createElement("date");
            child.setTextContent(Long.toString(mail.getDate().getTime()));
            mailElem.appendChild(child);
        }
        if (mail.getAttachments() != null)
        {
            child = doc.createElement("attachments");
            String[] att = mail.getAttachments();
            String attachments = "";
            for (int i = 0 ; i < att.length ; i++)
                attachments += att[i] + " ";
            child.setTextContent(attachments);
            mailElem.appendChild(child);
        }
        return mailElem;
    }
    
    public static void removeAccData (String nick)
    {
        File file = new File("data" + File.separator + nick + ".xml");
        if (file.exists())
            file.delete();
    }
}