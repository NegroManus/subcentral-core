package de.subcentral.core.parsing;

public interface ParsingService
{
	public default Object parse(String name) throws ParsingException
	{
		return parse(name, null);
	}

	public Object parse(String name, String domain) throws ParsingException;

	public default <T> T parseTyped(String name, Class<T> targetType) throws ParsingException
	{
		return parseTyped(name, null, targetType);
	}

	public <T> T parseTyped(String name, String domain, Class<T> targetType) throws ParsingException;
}
