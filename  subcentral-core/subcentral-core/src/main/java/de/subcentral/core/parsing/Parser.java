package de.subcentral.core.parsing;


public interface Parser<T>
{
	public String getDomain();

	public Class<T> getTargetType();

	public T parse(String name) throws NoMatchException, ParsingException;
}
