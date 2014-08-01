package de.subcentral.core.parsing;

public interface ParsingService
{
	public default Object parse(String name)
	{
		return parse(name, null);
	}

	public Object parse(String name, String domain);

	public default <T> T parseTyped(String name, Class<T> targetType)
	{
		return parseTyped(name, null, targetType);
	}

	public <T> T parseTyped(String name, String domain, Class<T> targetType);
}
