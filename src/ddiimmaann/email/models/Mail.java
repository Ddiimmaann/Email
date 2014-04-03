package ddiimmaann.email.models;

import java.util.Date;

public class Mail
{
    private String from;
    private String to;
    private String title = "No theme";
    private String message = "";
    private String[] attachments = null;
    private Date date;
    private boolean isSeen = false;
    private int numberOfMessage;
    
    public Mail (String from, String to)
    {
        this.from = from;
        this.to = to;
    }
    
    public String getFrom ()
    {
        return from;
    }
    
    public String getTo ()
    {
        return to;
    }
    
    public String getTitle ()
    {
        return title;
    }
    
    public String getMessage ()
    {
        return message;
    }
    
    public String[] getAttachments ()
    {
        return attachments;
    }
    
    public Date getDate ()
    {
        return date;
    }
    
    public boolean isSeen ()
    {
        return isSeen;
    }
    
    public int getNumberMail ()
    {
        return numberOfMessage;
    }
    
    public void setTitle (String tit)
    {
        title = tit;
    }
    
    public void setMessage (String mess)
    {
        message = mess;
    }
    
    public void setAttachments (String ... attach)
    {
        attachments = attach;
    }
    
    public void setDate (Date date)
    {
        this.date = date;
    }
    
    public void setSeen (boolean b)
    {
        isSeen = b;
    }
    
    public void setNumberMail (int num)
    {
        numberOfMessage = num;
    }
}