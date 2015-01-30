package de.subcentral.core.parsing;

import java.util.Set;

public class NoMatchException extends ParsingException
{
	private static final long	serialVersionUID	= 5435816357541351825L;

	public NoMatchException(String text, Set<Class<?>> targetTypes, String message)
	{
		super(text, targetTypes, message);
	}
}
