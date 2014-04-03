package ddiimmaann.email.controller;

import com.sun.mail.imap.IMAPSSLStore;
import ddiimmaann.email.InvalidDatumException;
import ddiimmaann.email.models.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.mail.*;
import org.apache.log4j.Logger;

public class ControllerMails
{
    public static void sendMail (Mail mail) throws InvalidDatumException, EmailException, IOException
    {
        Pattern patt = Pattern.compile("([\\w\\.]+)@((\\w+\\.)+(com|ru|net|org))");
        Matcher match = patt.matcher(mail.getTo());
        if (!match.matches())
            throw new InvalidDatumException("Invalid e-mail destination.");
        
        EmailAttachment[] attachments = {};
        if (mail.getAttachments() != null)
        {
            String[] names = mail.getAttachments();
            attachments = new EmailAttachment[names.length];
            Pattern pattern = Pattern.compile("[^\\\\/]+\\.[^\\\\/]*");
            Matcher matcher;
            
            for (int i = 0 ; i < attachments.length ; i++)
            {
                attachments[i] = new EmailAttachment();
                attachments[i].setPath(names[i]);
                attachments[i].setDisposition(EmailAttachment.ATTACHMENT);
                matcher = pattern.matcher(names[i]);
                if (matcher.find())
                    attachments[i].setName(matcher.group());
                else
                    throw new InvalidDatumException("File name " + names[i] + " is incorrect.");
            }
        }
        
        MultiPartEmail email = new MultiPartEmail();
        Account account = ControllerAccounts.getAccount(mail.getFrom());
        if (account.getHostNameSMTP() == null)
            throw new InvalidDatumException("Please, set hostNameSMTP.");
        email.setHostName(account.getHostNameSMTP());
        email.setSmtpPort(465);
        Authenticator aut = new DefaultAuthenticator(account.getNickname(), account.getPassword());
        email.setAuthenticator(aut);
        email.setSSLOnConnect(true);
        email.addTo(mail.getTo());
        email.setFrom(mail.getFrom(), account.getFullReallyName());
        email.setSubject(mail.getTitle());
        email.setMsg(mail.getMessage());
        for (int i = 0 ; i < attachments.length ; i++)
            email.attach(attachments[i]);

        email.send();
        
        mail.setDate(new Date ());
        account.getMails().addNewOutMail(mail);
    }
    
    public static LinkedList<String>  refresh () throws MessagingException, IOException,
            InvalidDatumException
    {
        String error = null;
        Accounts accounts = ControllerAccounts.getAllAccs();
        Map<String, Account> accsMap = accounts.getAllAccs();
        Set<Map.Entry<String, Account>> accs = accsMap.entrySet();
        
        for (Map.Entry<String, Account> current : accs)
        {
            Account acc = current.getValue();
            if (acc.getHostNameIMAP() == null)
                throw new InvalidDatumException("Please, set hostNameIMAP for " + 
                        acc.getFullNick());
            
            Properties properties = new Properties();
            properties.put(acc.getHostNameIMAP(), "nice");
            Session session = Session.getInstance(properties, null);
            IMAPSSLStore imap = (IMAPSSLStore) session.getStore("imaps");
            imap.connect(acc.getHostNameIMAP(), 993, acc.getNickname(), acc.getPassword());
            Folder folder = imap.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();
            if (messages.length == 0)
            {
                folder.close(false);
                imap.close();
                LinkedList<String> out = new LinkedList<>();
                out.add("No received mails.");
                return out;
            }
            Date date = messages[messages.length - 1].getReceivedDate();
            Date dateLast;
            if (acc.getMails().getLastInMail() == null)
                dateLast = null;
            else
                dateLast = acc.getMails().getLastInMail().getDate();
            
            try
            {
                if ((dateLast == null)||(date.after(dateLast)))
                    saveMails(acc, messages);
            }
            catch (InvalidDatumException e)
            {
                if (error == null)
                    error = "There are some problem with messages: \n";
                error += e.toString();
            }
            
            folder.close(false);
            imap.close();
        }
        
        LinkedList<String> newMails = new LinkedList<>();
        for (Map.Entry<String, Account> current : accs)
        {
            Account acc = current.getValue();
            TreeMap<Integer, Mail> mails = acc.getMails().getInMails();
            for (int i = mails.lastKey() ; (!mails.get(i).isSeen() && i > 0) ; i--)
                newMails.add(mailToString(mails.get(i), false));
        }
        if (newMails.isEmpty())
            newMails.add("No unread mails.");
        if (error != null)
            newMails.add("\n" + error);

        return newMails;
    }
    
    private static void saveMails (Account acc, Message[] messages) throws
            IOException, MessagingException, InvalidDatumException
    {
        Logger logger = Logger.getLogger(ControllerMails.class);
        String error = null;
        int i;
        Date dateLast;
        if (acc.getMails().getLastInMail() == null)
            dateLast = null;
        else
            dateLast = acc.getMails().getLastInMail().getDate();
        if (dateLast == null)
            i = 0;
        else
        {
            i = messages.length - 1;
            while ((i >= 0)&&(messages[i].getReceivedDate().after(dateLast)))
                i--;
        }
        
        for (; i < messages.length ; i++)
        {
            MailBuilder mail = MailBuilder.setFromTo(messages[i].getFrom()[0].toString(),
                    acc.getFullNick());
            mail.setTitle(messages[i].getSubject());
            mail.setSeen(messages[i].isSet(Flags.Flag.SEEN));
            mail.setDate(messages[i].getReceivedDate());

            if (!messages[i].isSet(Flags.Flag.SEEN))
                messages[i].setFlag(Flags.Flag.SEEN, true);

            Object obj;
            try
            {
                obj = messages[i].getContent();
                if (obj instanceof MimeMultipart)
                {
                    MimeMultipart message = (MimeMultipart)obj;
                    ContentsMessage content = getContent(message);
                    mail.setMessage(content.getMessage());
                    String[] attachments = content.getAttachments().toArray(new String[0]);
                    mail.setAttachments(attachments);
                }
                else
                    mail.setMessage(messages[i].getContent().toString());
            }
            catch (MessagingException e)
            {
                if (error == null)
                    error = "";
                error += "\t" + acc.getFullNick() + " №" + (i+1) + "\n";
                logger.error(acc.getFullNick() + " exception in message № " + (i+1), e);
                continue;
            }

            acc.getMails().addNewInMail(mail.build());
	}
        if (error != null)
            throw new InvalidDatumException(error);
    }
    
    private static ContentsMessage getContent (MimeMultipart message) throws
            MessagingException, IOException
    {
        ContentsMessage content = new ContentsMessage();
        
        for (int i = 0 ; i < message.getCount() ; i++)
        {           
            if (message.getBodyPart(i).isMimeType("text/plain"))
                content.concatMessage(message.getBodyPart(i).getContent().toString());
            if (message.getBodyPart(i).isMimeType("application/*") ||
                    message.getBodyPart(i).isMimeType("image/*"))
                content.addAttachment(message.getBodyPart(i).getFileName());
            if (message.getBodyPart(i).isMimeType("multipart/*"))
            {
                MimeMultipart message1 = (MimeMultipart)message.getBodyPart(i).getContent();
                ContentsMessage cont = getContent(message1);
                content.concatMessage(cont.getMessage());
                for (String current : cont.getAttachments())
                    content.addAttachment(current);
            }
        }
        
        return content;
    }
    
    private static String mailToString (Mail mail, boolean full)
    {
        String out = "";
        out += "Email number: " + mail.getNumberMail() + "\t";
        out += "From: " + mail.getFrom() + "\t";
        out += "To: " + mail.getTo() + "\n";
        out += "Title: " + mail.getTitle() + "\n";
        if (full)
            out += mail.getMessage() + "\n";
        String[] att = mail.getAttachments();
        if (att != null)
            for (int i = 0 ; i < att.length ; i++)
                out += "\n" + att[i];
        out += "\t\t\t" + mail.getDate().toString() + "\n";
        if (!full)
            out += "============================================================";
        return out;
    }
    
    public static LinkedList<String> lastFiveMails () throws IOException, FileNotFoundException,
            InvalidDatumException
    {
        LinkedList<String> lastMails = new LinkedList<>();
        TreeSet<Mail> setMails = new TreeSet<>(new ComparatorMails());
        Accounts accounts = ControllerAccounts.getAllAccs();
        Map<String, Account> accsMap = accounts.getAllAccs();
        Set<Map.Entry<String, Account>> accs = accsMap.entrySet();
        
        for (Map.Entry<String, Account> current : accs)
        {
            Account acc = current.getValue();
            TreeMap<Integer, Mail> mails = acc.getMails().getInMails();
            if (!mails.isEmpty())
                for (int i = mails.lastKey() ; (i > mails.lastKey() - 5 && i > 0) ; i--)
                    setMails.add(mails.get(i));
            mails = acc.getMails().getOutMails();
            if (!mails.isEmpty())
                for (int i = mails.lastKey() ; (i > mails.lastKey() - 5 && i > 0) ; i--)
                    setMails.add(mails.get(i));
        }
        
        for (Mail current : setMails)
        {
            lastMails.add(mailToString(current, false));
            if (lastMails.size() == 5)
                break;
        }
        
        return lastMails;
    }
    
    public static LinkedList<String> allMails () throws IOException, FileNotFoundException,
            InvalidDatumException
    {
        LinkedList<String> lastMails = new LinkedList<>();
        TreeSet<Mail> setMails = new TreeSet<>(new ComparatorMails());
        Accounts accounts = ControllerAccounts.getAllAccs();
        Map<String, Account> accsMap = accounts.getAllAccs();
        Set<Map.Entry<String, Account>> accs = accsMap.entrySet();
        
        for (Map.Entry<String, Account> current : accs)
        {
            Account acc = current.getValue();
            TreeMap<Integer, Mail> mails = acc.getMails().getInMails();
            if (!mails.isEmpty())
                for (int i = mails.lastKey() ; i > 0 ; i--)
                    setMails.add(mails.get(i));
            mails = acc.getMails().getOutMails();
            if (!mails.isEmpty())
                for (int i = mails.lastKey() ; (i > mails.lastKey() - 5 && i > 0) ; i--)
                    setMails.add(mails.get(i));
        }
        
        for (Mail current : setMails)
            lastMails.add(mailToString(current, false));
        
        return lastMails;
    }
    
    public static LinkedList<String> lastFiveMailsOfAcc (String name) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        LinkedList<String> lastMails = new LinkedList<>();
        Account acc = ControllerAccounts.getAccount(name);
        TreeSet<Mail> setMails = new TreeSet<>(new ComparatorMails());
        
        TreeMap<Integer, Mail> mails = acc.getMails().getInMails();
        if (!mails.isEmpty())
            for (int i = mails.lastKey() ; (i > mails.lastKey() - 5 && i > 0) ; i--)
                setMails.add(mails.get(i));
        mails = acc.getMails().getOutMails();
        if (!mails.isEmpty())
            for (int i = mails.lastKey() ; (i > mails.lastKey() - 5 && i > 0) ; i--)
                setMails.add(mails.get(i));
        
        for (Mail current : setMails)
        {
            lastMails.add(mailToString(current, false));
            if (lastMails.size() == 5)
                break;
        }
        
        return lastMails;
    }
    
    public static LinkedList<String> allMailsOfAcc (String name) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        LinkedList<String> lastMails = new LinkedList<>();
        Account acc = ControllerAccounts.getAccount(name);
        TreeSet<Mail> setMails = new TreeSet<>(new ComparatorMails());
        
        TreeMap<Integer, Mail> mails = acc.getMails().getInMails();
        if (!mails.isEmpty())
            for (int i = mails.lastKey() ; i > 0 ; i--)
                setMails.add(mails.get(i));
        mails = acc.getMails().getOutMails();
        if (!mails.isEmpty())
            for (int i = mails.lastKey() ; i > 0 ; i--)
                setMails.add(mails.get(i));
        
        for (Mail current : setMails)
            lastMails.add(mailToString(current, false));
        
        return lastMails;
    }
    
    public static LinkedList<String> getMailOfAcc (String name, Integer num, boolean isIn, String dir)
            throws IOException, FileNotFoundException, InvalidDatumException, MessagingException
    {
        LinkedList<String> mailWithNum = new LinkedList<>();
        Account acc = ControllerAccounts.getAccount(name);
        
        if (!isIn)
        {
            if (!acc.getMails().getOutMails().containsKey(num))
                throw new InvalidDatumException("Mail with number " + num + " isn't exist.");
            String mailNum = mailToString(acc.getMails().getOutMails().get(num), true);
            mailWithNum.add(mailNum);
            return mailWithNum;
        }
        
        if (!acc.getMails().getInMails().containsKey(num))
            throw new InvalidDatumException("Mail with number " + num + " isn't exist.");
        Date dateMailNum = acc.getMails().getInMails().get(num).getDate();
        mailWithNum.add(mailToString(acc.getMails().getInMails().get(num), true));
        if (acc.getMails().getInMails().get(num).getAttachments() == null ||
                acc.getMails().getInMails().get(num).getAttachments().length == 0)
            return mailWithNum;
     
        if (dir == null)
            dir = "C:";
        dir += File.separator;
        
        Properties properties = new Properties();
        properties.put(acc.getHostNameIMAP(), "nice");
        Session session = Session.getInstance(properties, null);
        IMAPSSLStore imap = (IMAPSSLStore) session.getStore("imaps");
        imap.connect(acc.getHostNameIMAP(), 993, acc.getNickname(), acc.getPassword());
        Folder folder = imap.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        
        Message[] messages = folder.getMessages();
        if (messages.length == 0)
        {
            folder.close(false);
            imap.close();
            LinkedList<String> out = new LinkedList<>();
            out.add("Mail has been deleted.");
            return out;
        }
        Message mess = null;
        for (int i = messages.length - 1 ; i >= 0 ; i--)
            if (messages[i].getReceivedDate().compareTo(dateMailNum) == 0)
            {
                mess = messages[i];
                break;
            }
        if (mess == null)
        {
            folder.close(false);
            imap.close();
            LinkedList<String> out = new LinkedList<>();
            out.add("Mail has been deleted.");
            return out;
        }
        MimeMultipart message = (MimeMultipart)mess.getContent();
        for (int i = 0 ; i < message.getCount() ; i++)
        {
            if (message.getBodyPart(i).isMimeType("application/*") || 
                    message.getBodyPart(i).isMimeType("image/*"))
            {
                BufferedInputStream in = new BufferedInputStream(
                            message.getBodyPart(i).getInputStream());
                try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
                            dir + message.getBodyPart(i).getFileName()));)
                {
                    byte[] buf = new byte[1024];
                    int bytesRead;
                    while (true)
                    {
                        bytesRead = in.read(buf);
                        if (bytesRead == -1)
                            break;
                        out.write(buf, 0, bytesRead);
                    }
                }
                catch (FileNotFoundException e)
                {
                    mailWithNum.add("Problem with file " + message.getBodyPart(i).getFileName()
                            + " " + e.getMessage());
                }
                in.close();
            }
        }
            
        folder.close(false);
        imap.close();
        
        mailWithNum.add("Attachments have been saved in " + dir + " directory.\n");
        return mailWithNum;
    }
}

class ContentsMessage
{
    private String message = "";
    private LinkedList<String> attachments = new LinkedList<>();
    
    String getMessage ()
    {
        return message;
    }
    
    LinkedList<String> getAttachments ()
    {
        return attachments;
    }
    
    void concatMessage (String mes)
    {
        message = message + mes;
    }
    
    void addAttachment (String att)
    {
        attachments.add(att);
    }
}

class ComparatorMails implements Comparator<Mail>
{
    @Override
    public int compare (Mail m1, Mail m2)
    {
        if (m1.getDate().after(m2.getDate()))
            return -1;
        if (m1.getDate().before(m2.getDate()))
            return 1;
        return 0;
    }
}