package de.subcentral.core.parsing;

public class ParsingException extends RuntimeException
{
	private static final long	serialVersionUID	= -1790109092415797792L;

	public ParsingException()
	{}

	public ParsingException(String message)
	{
		super(message);
	}

	public ParsingException(Throwable cause)
	{
		super(cause);
	}

	public ParsingException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
