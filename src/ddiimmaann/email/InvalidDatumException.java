package ddiimmaann.email;

public class InvalidDatumException extends Exception
{
    private Exception exc;
    private String err;
    
    public InvalidDatumException(String review)
    {
        err = review;
    }
    
    public InvalidDatumException(String review, Exception e)
    {
        err = review;
        exc = e;
    }

    @Override
    public Exception getCause ()
    {
        return exc;
    }
    
    @Override
    public String toString ()
    {
        return err;
    }
}