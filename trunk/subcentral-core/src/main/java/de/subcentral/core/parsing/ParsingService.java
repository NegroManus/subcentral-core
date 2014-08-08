package de.subcentral.core.parsing;

import java.util.function.Function;

public interface ParsingService extends Function<String, Object>
{
	public default Object parse(String text) throws ParsingException
	{
		return parse(text, null);
	}

	public Object parse(String text, String domain) throws NoMatchException, ParsingException;

	public default <T> T parseTyped(String text, Class<T> targetType) throws NoMatchException, ParsingException
	{
		return parseTyped(text, null, targetType);
	}

	public <T> T parseTyped(String text, String domain, Class<T> targetType) throws NoMatchException, ParsingException;

	@Override
	public default Object apply(String text)
	{
		return parse(text);
	}
}
