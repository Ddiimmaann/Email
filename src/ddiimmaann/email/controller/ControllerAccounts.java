package ddiimmaann.email.controller;

import ddiimmaann.email.DAO.*;
import ddiimmaann.email.*;
import ddiimmaann.email.models.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControllerAccounts
{
    private static Accounts accounts;
            
    public static Account getAccount (String nick) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        if (nick == null)
            throw new InvalidDatumException("Invalid account.");
        
        if (accounts == null)
            createAccounts ();
        
        nick = nick.toLowerCase();
        if (!accounts.getAllAccs().containsKey(nick))
            throw new InvalidDatumException("Account " + nick + " not found. Register it.");

        return accounts.getAccount(nick);
    }
    
    public static Accounts newAccount (String nick, String pass) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        if ((nick == null)||(pass == null))
            throw new InvalidDatumException("Invalid account or password.");
        
        if (accounts == null)
            createAccounts ();
        
        nick = nick.toLowerCase();
        if (accounts.getAllAccs().containsKey(nick))
            throw new InvalidDatumException("Account " + nick + " exist.");
        
        Pattern patt = Pattern.compile("([\\w\\.]+)@((\\w+\\.)+(com|ru|net|org))");
        Matcher match = patt.matcher(nick);
        if (!match.matches())
            throw new InvalidDatumException("Account " + nick + " isn't correct.");
        
        Account acc = new Account();
        acc.setFullNick(nick);
        acc.setPassword(pass);
        acc.setFullReallyName(nick);
        setNickAndHostName(acc);
        acc.setMails(new AccountMails());
        AccountsDAO.saveNewAcc(acc);
        accounts.getAllAccs().put(nick, acc);
        
        return accounts;
    }
    
    public static Account deleteAccount (String nick, String pass) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        if ((nick == null)||(pass == null))
            throw new InvalidDatumException("Invalid account or password.");
        
        if (accounts == null)
            createAccounts ();
        
        nick = nick.toLowerCase();
        if (!accounts.getAllAccs().containsKey(nick))
            throw new InvalidDatumException("Account " + nick + " not found.");
        
        if (accounts.getAllAccs().get(nick).getPassword().compareTo(pass) != 0)
            throw new InvalidDatumException("Invalid password. Try again.");
        
        Account acc = accounts.getAllAccs().get(nick);
        accounts.getAllAccs().remove(nick);
        AccountsDAO.resaveAllAccs(accounts.getAllAccs());
        MailsDAO.removeAccData(nick);
        
        return acc;
    }
    
    private static void createAccounts () throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        accounts = new Accounts ();
        accounts.setAllAccs(AccountsDAO.loadAccounts());

        Map<String, Account> accsMap = accounts.getAllAccs();
        Set<Entry<String, Account>> accs = accsMap.entrySet();
        for (Entry<String, Account> current : accs)
        {
            String fullNick = current.getKey();
            accounts.getAccount(fullNick).setMails(MailsDAO.loadData(fullNick));
        }
    }
    
    private static void setNickAndHostName (Account acc)
    {
        Pattern patt = Pattern.compile("@");
        String fullNick = acc.getFullNick();
        String[] nameAndHost = patt.split(fullNick);
        acc.setNickname(nameAndHost[0]);
            
        switch (nameAndHost[1])
        {
            case "yandex.ru": case "ya.ru": case "yandex.com":
                acc.setHostNameSMTP("smtp.yandex.ru");
                acc.setHostNameIMAP("imap.yandex.ru");
                break;
            case "gmail.com":
                acc.setHostNameSMTP("smtp.gmail.com");
                acc.setHostNameIMAP("imap.gmail.com");
                break;
            case "mail.ru":
                acc.setHostNameSMTP("smtp.mail.ru");
                acc.setHostNameIMAP("imap.mail.ru");
        }
    }
    
    public static void setHostNameSMTP (String name, String hostNameSMTP) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        getAccount(name).setHostNameSMTP(hostNameSMTP);
        String pass = getAccount(name).getPassword();
        AccountsDAO.saveNewAcc(deleteAccount(name, pass));
    }
    
    public static void setHostNameIMAP (String name, String hostNameIMAP) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        getAccount(name).setHostNameIMAP(hostNameIMAP);
        String pass = getAccount(name).getPassword();
        AccountsDAO.saveNewAcc(deleteAccount(name, pass));
    }
    
    public static void setRealName (String name, String realName) throws IOException,
            FileNotFoundException, InvalidDatumException
    {
        getAccount(name).setFullReallyName(realName);
        String pass = getAccount(name).getPassword();
        AccountsDAO.saveNewAcc(deleteAccount(name, pass));
    }
    
    public static Accounts getAllAccs () throws IOException, FileNotFoundException,
            InvalidDatumException
    {
        if (accounts == null)
            createAccounts();
        return accounts;
    }
    
    public static void saveDataBase () throws InvalidDatumException, IOException
    {
        if (accounts == null)
            return;
        Set<Entry<String, Account>> accs = accounts.getAllAccs().entrySet();
        AccountMails mails;
        for (Entry<String, Account> acc : accs)
            if ((mails = acc.getValue().getMails()) != null)
                MailsDAO.resaveDataBase(mails, acc.getKey());
    }
}