package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.util.StringUtil;

public interface NamingService extends Namer<Object>
{
	public String getDomain();

	public boolean canName(Object candidate);

	public default String nameMulti(Iterable<?> candidates, String separator, Map<String, Object> parameters)
	{
		try
		{
			return name(candidates, parameters);
		}
		catch (NoNamerRegisteredException e)
		{
			StringBuilder name = new StringBuilder();
			for (Object candidate : candidates)
			{
				name.append(name(candidate, parameters));
				name.append(separator);
			}
			return StringUtil.stripEnd(name, separator).toString();
		}
	}
}
