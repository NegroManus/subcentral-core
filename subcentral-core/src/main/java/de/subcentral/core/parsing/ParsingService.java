package de.subcentral.core.parsing;

public interface ParsingService extends Parser<Object>
{
	public String getDomain();

	public <T> T parseTyped(String text, Class<T> targetType) throws NoMatchException, ParsingException;
}
