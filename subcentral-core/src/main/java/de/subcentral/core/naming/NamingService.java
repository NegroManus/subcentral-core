package de.subcentral.core.naming;

import java.util.Map;

public interface NamingService extends Namer<Object>
{
    public String getDomain();

    public String getDefaultSeparator();

    public default String nameAll(Iterable<?> candidates, Map<String, Object> parameters)
    {
	return nameAll(candidates, getDefaultSeparator(), parameters);
    }
}
