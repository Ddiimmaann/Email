package ddiimmaann.email.models;

public class Account
{
    private String fullNick; //<nickname@hostname>
    private String password;
    private AccountMails mails;
    private String nickname;
    private String hostNameSMTP;
    private String hostNameIMAP;
    private String fullReallyName;
    
    public String getFullNick ()
    {
        return fullNick;
    }
    
    public String getPassword ()
    {
        return password;
    }
    
    public AccountMails getMails ()
    {
        return mails;
    }
    
    public String getNickname ()
    {
        return nickname;
    }
    
    public String getHostNameSMTP ()
    {
        return hostNameSMTP;
    }
    
    public String getHostNameIMAP ()
    {
        return hostNameIMAP;
    }
    
    public String getFullReallyName ()
    {
        return fullReallyName;
    }
    
    public void setFullNick (String fNick)
    {
        fullNick = fNick;
    }
    
    public void setPassword (String pass)
    {
        password = pass;
    }
    
    public void setMails (AccountMails mails)
    {
        this.mails = mails;
    }
    
    public void setNickname (String nickName)
    {
        nickname = nickName;
    }
    
    public void setHostNameSMTP (String hNameSMTP)
    {
        hostNameSMTP = hNameSMTP;
    }
    
    public void setHostNameIMAP (String hNameIMAP)
    {
        hostNameIMAP = hNameIMAP;
    }
    
    public void setFullReallyName (String fullReallyName)
    {
        this.fullReallyName = fullReallyName;
    }
}