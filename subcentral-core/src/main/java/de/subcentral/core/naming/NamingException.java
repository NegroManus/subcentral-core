package de.subcentral.core.naming;

public class NamingException extends RuntimeException
{
    private static final long serialVersionUID = -2678379604002150514L;

    public NamingException(Object candidate)
    {
	this(candidate, null, null);
    }

    public NamingException(Object candidate, String message)
    {
	this(candidate, message, null);
    }

    public NamingException(Object candidate, Throwable cause)
    {
	this(candidate, null, cause);
    }

    public NamingException(Object candidate, String message, Throwable cause)
    {
	super(generateMessage(candidate, message), cause);
    }

    public NamingException(Object candidate, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
	super(generateMessage(candidate, message), cause, enableSuppression, writableStackTrace);
    }

    private static final String generateMessage(Object candidate, String additionalMsg)
    {
	StringBuilder sb = new StringBuilder();
	sb.append("Exception while naming ");
	sb.append(candidate);
	if (candidate != null)
	{
	    sb.append(" of ");
	    sb.append(candidate.getClass());
	    sb.append("");
	}
	if (additionalMsg != null)
	{
	    sb.append(": ");
	    sb.append(additionalMsg);
	}
	return sb.toString();
    }
}
