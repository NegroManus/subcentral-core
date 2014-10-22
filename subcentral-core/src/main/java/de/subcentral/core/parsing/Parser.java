package de.subcentral.core.parsing;

import java.util.function.Function;

public interface Parser<T> extends Function<String, T>
{
	public T parse(String text) throws NoMatchException, ParsingException;

	@Override
	public default T apply(String text)
	{
		return parse(text);
	}
}
