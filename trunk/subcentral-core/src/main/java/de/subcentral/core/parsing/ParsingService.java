package de.subcentral.core.parsing;

import java.util.Set;

public interface ParsingService extends Parser<Object>
{
	public String getDomain();

	public Set<Class<?>> getTargetTypes();

	public <T> T parse(String text, Class<T> targetClass) throws NoMatchException, ParsingException;
}
