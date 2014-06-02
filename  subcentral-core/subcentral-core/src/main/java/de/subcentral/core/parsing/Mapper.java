package de.subcentral.core.parsing;

import java.util.Map;

public interface Mapper<T>
{
	public Class<T> getType();

	public String[] getKnownAttributeNames();

	public T map(Map<String, String> info);
}
