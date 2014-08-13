package de.subcentral.core.parsing;

public class MappingException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7524198648124655458L;

	public MappingException(String message)
	{
		super(message);
	}

	public MappingException(Throwable cause)
	{
		super(cause);
	}

	public MappingException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
