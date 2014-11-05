package de.subcentral.core.standardizing;

public class StandardizingException extends RuntimeException
{
	private static final long	serialVersionUID	= -660256862365998918L;

	public StandardizingException(String message)
	{
		super(message);
	}

	public StandardizingException(Throwable cause)
	{
		super(cause);
	}

	public StandardizingException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
