package de.subcentral.core.parsing;

public interface ParsingService
{
	public Object parse(String name, String domain);

	public <T> T parse(String name, String domain, Class<T> targetClass);
}
