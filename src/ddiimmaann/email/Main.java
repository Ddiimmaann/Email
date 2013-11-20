package ddiimmaann.email;

import ddiimmaann.email.view.Shell;
import java.io.*;
import java.util.regex.Pattern;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader inBuf = new BufferedReader(in);
        String buf;
        Shell user = Shell.getUser();
        Pattern patt = Pattern.compile("\\s");
        
        while (true)
        {
            buf = inBuf.readLine();
            user.doIt(patt.split(buf));
        }
    }
}