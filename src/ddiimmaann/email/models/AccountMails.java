package ddiimmaann.email.models;

import java.util.TreeMap;

public class AccountMails
{
    private TreeMap<Integer, Mail> outMails = new TreeMap<>();
    private TreeMap<Integer, Mail> inMails = new TreeMap<>();
    
    public TreeMap<Integer, Mail> getOutMails ()
    {
        return outMails;
    }
    
    public TreeMap<Integer, Mail> getInMails ()
    {
        return inMails;
    }
    
    public Mail getLastInMail ()
    {
        if (inMails.isEmpty())
            return null;
        return inMails.pollLastEntry().getValue();
    }
    
    public void setOutMails (TreeMap<Integer, Mail> outMails)
    {
        this.outMails = outMails;
    }
    
    public void setInMails (TreeMap<Integer, Mail> inMails)
    {
        this.inMails = inMails;
    }
    
    public void addNewOutMail (Mail mail)
    {
        if (outMails.isEmpty())
        {
            mail.setNumberMail(1);
            outMails.put(1, mail);
        }
        else
        {
            mail.setNumberMail(outMails.lastKey() + 1);
            outMails.put(outMails.lastKey() + 1, mail);
        }
    }
    
    public void addNewInMail (Mail mail)
    {
        if (inMails.isEmpty())
        {
            mail.setNumberMail(1);
            inMails.put(1, mail);
        }
        else
        {
            mail.setNumberMail(inMails.lastKey() + 1);
            inMails.put(inMails.lastKey() + 1, mail);
        }
    }
}