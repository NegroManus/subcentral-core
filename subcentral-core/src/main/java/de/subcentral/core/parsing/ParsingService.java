package de.subcentral.core.parsing;

public interface ParsingService extends Parser<Object>
{
	public String getDomain();

	public <T> T parse(String text, Class<T> targetClass) throws NoMatchException, ParsingException;
}
