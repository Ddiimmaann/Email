package ddiimmaann.email.models;

import java.util.Map;

public class Accounts
{
    private Map<String, Account> accounts; //String - fullName

    public Map<String, Account> getAllAccs ()
    {
        return accounts;
    }
    
    public Account getAccount (String nick)
    {
        return accounts.get(nick);
    }
    
    public void setAllAccs (Map<String, Account> accs)
    {
        accounts = accs;
    }
    
    public void setAccount (Account acc)
    {
        accounts.put(acc.getFullNick(), acc);
    }
}