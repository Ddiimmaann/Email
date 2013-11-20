package ddiimmaann.email.controller;

import ddiimmaann.email.models.Mail;
import java.util.Date;

public class MailBuilder
{
    private Mail mail;
    private MailBuilder() {}
    
    public static MailBuilder setFromTo (String from, String to)
    {
        MailBuilder bilder = new MailBuilder();
        bilder.mail = new Mail(from, to);
        return bilder;
    }
    
    public MailBuilder setTitle (String title)
    {
        mail.setTitle(title);
        return this;
    }
    
    public MailBuilder setMessage (String message)
    {
        mail.setMessage(message);
        return this;
    }
    
    public MailBuilder setAttachments (String ... attach)
    {
        mail.setAttachments(attach);
        return this;
    }
    
    public MailBuilder setDate (Date date)
    {
        mail.setDate(date);
        return this;
    }
    
    public MailBuilder setSeen (boolean b)
    {
        mail.setSeen(b);
        return this;
    }
    
    public Mail build ()
    {
        return mail;
    }
}