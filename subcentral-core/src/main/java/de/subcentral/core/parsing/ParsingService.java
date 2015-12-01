package de.subcentral.core.parsing;

import java.util.Set;

public interface ParsingService extends Parser<Object>
{
	public String getDomain();

	public Set<Class<?>> getSupportedTargetTypes();

	public <T> T parse(String text, Class<T> targetType) throws ParsingException;

	public Object parse(String text, Set<Class<?>> targetTypes) throws ParsingException;
}
