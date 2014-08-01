package de.subcentral.core.parsing;

public interface Parser<T>
{
	public String getDomain();

	public Class<T> getTargetClass();

	public T parse(String name) throws ParsingException;
}
