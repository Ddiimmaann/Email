package ddiimmaann.email.DAO;

import ddiimmaann.email.InvalidDatumException;
import ddiimmaann.email.models.Account;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import org.w3c.dom.*;

public class AccountsDAO
{   
    public static Map<String, Account> loadAccounts () throws FileNotFoundException,
            IOException, InvalidDatumException
    {
        HashMap<String, Account> accs = new HashMap<>();
        Document doc = DocumentSaver.loadDocument("accounts.xml");

        if (doc == null || !doc.hasChildNodes() || !doc.getFirstChild().hasChildNodes())
            return accs;

        NodeList nodes = doc.getFirstChild().getChildNodes();
        for (int i = 0 ; i < nodes.getLength() ; i++)
        {
            if (!nodes.item(i).hasChildNodes())
                continue;
            Account acc = loadAccount(nodes.item(i));
            if (acc != null)
                accs.put(acc.getFullNick(), acc);
        }
        return accs;
    }
    
    private static Account loadAccount (Node node)
    {
        Account acc = new Account();
        NodeList childs = node.getChildNodes();
        if (!node.hasAttributes())
            return null;
        NamedNodeMap name = node.getAttributes();
        if (name.getNamedItem("fullName") != null)
            acc.setFullNick(name.getNamedItem("fullName").getNodeValue());
        Node currentNode;
        for (int i = 0 ; i < childs.getLength() ; i++)
        {
            currentNode = childs.item(i);
            switch (currentNode.getNodeName())
            {
                case "password" :
                    acc.setPassword(currentNode.getTextContent());
                    break;
                case "nickname" :
                    acc.setNickname(currentNode.getTextContent());
                    break;
                case "hostNameSMTP" :
                    acc.setHostNameSMTP(currentNode.getTextContent());
                    break;
                case "hostNameIMAP" :
                    acc.setHostNameIMAP(currentNode.getTextContent());
                    break;
                case "fullReallyName" :
                    acc.setFullReallyName(currentNode.getTextContent());
            }
        }
        if ((acc.getPassword() == null)||(acc.getFullNick() == null)||
                (acc.getFullReallyName() == null)||(acc.getNickname() == null))
            return null;
        return acc;
    }
    
    public static void saveNewAcc (Account acc) throws FileNotFoundException,
            IOException, InvalidDatumException
    {
        Document doc = DocumentSaver.loadDocument("accounts.xml");
        if (doc == null || !doc.hasChildNodes())
        {
            doc = DocumentSaver.getNewDocument();
            Element accs = doc.createElement("accounts");
            doc.appendChild(accs);
        }
        
        Element accElem = getAccElement(acc, doc);
        Node accs = doc.getFirstChild();
        accs.appendChild(accElem);
        
        DocumentSaver.writeDocument(doc, "accounts.xml");
    }
    
    public static void resaveAllAccs (Map<String, Account> accs) throws FileNotFoundException,
                                      IOException, InvalidDatumException
    {
        Set<Map.Entry<String, Account>> setAccs = accs.entrySet();
        Document doc = DocumentSaver.getNewDocument();
        Element accsElem = doc.createElement("accounts");
        
        for (Map.Entry<String, Account> acc : setAccs)
                accsElem.appendChild(getAccElement(acc.getValue(), doc));
        
        doc.appendChild(accsElem);
        DocumentSaver.writeDocument(doc, "accounts.xml");
    }
    
    private static Element getAccElement (Account acc, Document doc)
    {
        Element elAcc = doc.createElement("account");
        elAcc.setAttribute("fullName", acc.getFullNick());
        Element child = doc.createElement("password");
        child.setTextContent(acc.getPassword());
        elAcc.appendChild(child);
        child = doc.createElement("nickname");
        child.setTextContent(acc.getNickname());
        elAcc.appendChild(child);
        child = doc.createElement("fullReallyName");
        child.setTextContent(acc.getFullReallyName());
        elAcc.appendChild(child);
        if (acc.getHostNameSMTP() != null)
        {
            child = doc.createElement("hostNameSMTP");
            child.setTextContent(acc.getHostNameSMTP());
            elAcc.appendChild(child);
        }
        if (acc.getHostNameIMAP() != null)
        {
            child = doc.createElement("hostNameIMAP");
            child.setTextContent(acc.getHostNameIMAP());
            elAcc.appendChild(child);
        }
        return elAcc;
    }
}