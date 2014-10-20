package de.subcentral.core.parsing;

public class NoMatchException extends ParsingException
{
	private static final long	serialVersionUID	= 5435816357541351825L;

	public NoMatchException(String text, Class<?> entityClass, String message)
	{
		super(text, entityClass, message);
	}
}
