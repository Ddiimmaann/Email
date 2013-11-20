package ddiimmaann.email.view;

import ddiimmaann.email.*;
import ddiimmaann.email.controller.*;
import java.io.*;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.commons.cli.*;
import org.apache.commons.mail.EmailException;

public class Shell
{
    private Options options = new Options();
    private static Shell user = new Shell();
    private CommandLine cmd;
    private BufferedReader inBuf;
    private boolean hasAnyOption;
    
    private Shell ()
    {
        Option opt = new Option("register", false, "Register new account in this programm.");
        options.addOption(opt);
        opt = new Option("delete", false, "Delete account.");
        options.addOption(opt);
        opt = new Option("view", true, "View mail in program base. "
                + "Arguments - name of account. Params -num -all -dir may exist.");
        opt.setArgs(1);
        opt.setOptionalArg(true);
        options.addOption(opt);
        opt = new Option("num", "number", true, "Number of mail, that you want to see "
                + "and in or out mail you want to see.");
        opt.setArgs(2);
        options.addOption(opt);
        opt = new Option("all", false, "View summary of all mails.");
        options.addOption(opt);
        opt = new Option("dir", "directory", true, "Directory in which you want"
                + " to download attachments.");
        opt.setArgs(1);
        options.addOption(opt);
        opt = new Option("set", true, "Set data of accounts. "
                + "Arguments - name of account. Params -hs -hi -n may exist.");
        opt.setArgs(1);
        options.addOption(opt);
        opt = new Option("hs", "hosts", true, "Name of SMTP server.");
        opt.setArgs(1);
        options.addOption(opt);
        opt = new Option("hi", "hosti", true, "Name of IMAP server.");
        opt.setArgs(1);
        options.addOption(opt);
        opt = new Option("n", "name", true, "Your real name.");
        opt.setArgs(1);
        options.addOption(opt);
        opt = new Option("send", true, "Send email. Arguments - from, to. "
                + "Params -t -a may exist.");
        opt.setArgs(2);
        options.addOption(opt);
        opt = new Option("t", "title", true, "Title of email. No more 10 words.");
        opt.setArgs(10);
        options.addOption(opt);
        opt = new Option("a", "attachments", true, "Attachments of email. "
                + "Max amount of attachments is 10.");
        opt.setArgs(10);
        options.addOption(opt);
        opt = new Option("refresh", false, "Check new emails.");
        options.addOption(opt);
        opt = new Option("help", false, "help");
        options.addOption(opt);
        opt = new Option("exit", false, "exit");
        options.addOption(opt);
        
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader inBuffered = new BufferedReader(in);
        inBuf = inBuffered;
    }
    
    public static Shell getUser ()
    {
        return user;
    }
    
    public void doIt (String[] buf) throws IOException
    {
        try
        {
            CommandLineParser parser = new PosixParser();
            cmd = parser.parse(options, buf);
        }
        catch (ParseException err)
        {
            System.out.println("Parse failed. " + err.getMessage());
            return;
        }
        
        hasAnyOption = false;
        if (cmd.hasOption("register"))
            register ();
        
        if (cmd.hasOption("delete"))
            delete ();

        if (cmd.hasOption("send"))
            send ();
        
        if (cmd.hasOption("view"))
            view ();
        
        if (cmd.hasOption("set"))
            set ();
        
        if (cmd.hasOption("refresh"))
            refresh ();

        if (cmd.hasOption("help"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Available command: ", options);
            return;
        }
        if (cmd.hasOption("exit"))
            exit();
        
        if (!hasAnyOption)
            System.out.println("Invalid command.");
    }
    
    void exit ()
    {
        try
        {
            ControllerAccounts.saveDataBase();
            inBuf.close();
        }
        catch (InvalidDatumException | IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        finally
        {
            System.exit(0);
        }
    }
    
    void register () throws IOException
    {
        try
        {
            System.out.print("Please enter login: ");
            String login = inBuf.readLine();
            System.out.print("Please enter password: ");
            String pass = inBuf.readLine();
            ControllerAccounts.newAccount(login, pass);
            System.out.println("You are registered.");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Accounts file may be lost. " + e);
        }
        catch (InvalidDatumException e)
        {
            System.out.println(e);
        }        
        hasAnyOption = true;
    }
    
    void delete ()
    {
        try
        {
            System.out.print("Please enter login, you want delete: ");
            String login = inBuf.readLine();
            System.out.print("Please enter password: ");
            String pass = inBuf.readLine();
            ControllerAccounts.deleteAccount(login, pass);
            System.out.println("Account " + login + " has been deleted.");
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Accounts file may be lost. Register new accounts. " + e);
        }
        catch (IOException | InvalidDatumException e)
        {
            System.out.println(e);
        }        
        hasAnyOption = true;
    }
    
    void send () throws IOException
    {
        String [] fromTo = cmd.getOptionValues("send");
        if (fromTo.length < 2)
        {
            System.out.println("Argument is missing.");
            hasAnyOption = true;
            return;
        }
        MailBuilder mailBuild = MailBuilder.setFromTo(fromTo[0], fromTo[1]);
        
        if (cmd.hasOption("t"))
        {
            String title = "";
            String [] array = cmd.getOptionValues("t");
            for (int i = 0 ; i < array.length ; i++)
                title += array[i] + " ";
            mailBuild = mailBuild.setTitle(title);
        }  
        if (cmd.hasOption("a"))
            mailBuild = mailBuild.setAttachments(cmd.getOptionValues("a"));
        
        System.out.println("Enter your message. End of message - double \"enter\".");
        String buf, mess = "";

        do
        {
            buf = inBuf.readLine();
            if (buf == null)
                break;
            mess = mess + buf + "\n";
        }
        while (buf.length() != 0);
            
        mailBuild = mailBuild.setMessage(mess);
        
        try
        {
            System.out.println("Email is sending.");
            ControllerMails.sendMail(mailBuild.build());
            System.out.println("Email has been sent.");
        }
        catch (InvalidDatumException e)
        {
            System.out.println(e);
            if (e.getMessage() != null)
                System.out.println(e.getMessage());
        }
        catch (EmailException e)
        {
            System.out.println(e.getMessage());
        }
        hasAnyOption = true;
    }
    
    void set () throws IOException
    {
        String name = cmd.getOptionValue("set");
        
        try
        {
            if (cmd.hasOption("hs"))
                ControllerAccounts.setHostNameSMTP(name, cmd.getOptionValue("hs"));
            if (cmd.hasOption("hi"))
                ControllerAccounts.setHostNameIMAP(name, cmd.getOptionValue("hi"));
            if (cmd.hasOption("n"))
                ControllerAccounts.setRealName(name, cmd.getOptionValue("n"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Accounts file may be lost. " + e);
        }
        catch (InvalidDatumException e)
        {
            System.out.println(e);
        }
        hasAnyOption = true;
    }
    
    void view ()
    {
        String name = cmd.getOptionValue("view");
        List<String> mails;
        
        try
        {
            if (name == null)
                if (cmd.hasOption("all"))
                    mails = ControllerMails.allMails();
                else
                    mails = ControllerMails.lastFiveMails();
            else
            {
                if (cmd.hasOption("all"))
                    mails = ControllerMails.allMailsOfAcc(name);
                else
                    if (cmd.hasOption("num"))
                    {
                        String dir = null;
                        boolean isIn = false;
                        String[] args = cmd.getOptionValues("num");
                        if (args.length < 2)
                        {
                            System.out.println("Argument is missing.");
                            hasAnyOption = true;
                            return;
                        }
                        if (cmd.hasOption("dir"))
                            dir = cmd.getOptionValue("dir");
                        if (args[1].equals("in"))
                            isIn = true;
                        mails = ControllerMails.getMailOfAcc(name,
                                    Integer.parseInt(args[0]), isIn, dir);
                    }
                    else
                        mails = ControllerMails.lastFiveMailsOfAcc(name);
            }
        }
        catch (InvalidDatumException | IOException e)
        {
            System.out.println(e);
            hasAnyOption = true;
            return;
        }
        catch (MessagingException e1)
        {
            System.out.println("Message can't be read " + e1.getMessage());
            hasAnyOption = true;
            return;
        }
        
        for (String mail : mails)
                System.out.println(mail);
        hasAnyOption = true;
    }
    
    void refresh ()
    {
        try
        {
            System.out.println("Start refreshing.");
            List<String> mails = ControllerMails.refresh();
            for (String mail : mails)
                System.out.println(mail);
        }
        catch (InvalidDatumException | IOException e1)
        {
            System.out.println("Refresh failed. " + e1);
        }
        catch (MessagingException e2)
        {
            System.out.println("Refresh failed. Messages can't be read. " + e2.getMessage());
        }
        
        hasAnyOption = true;
    }
}