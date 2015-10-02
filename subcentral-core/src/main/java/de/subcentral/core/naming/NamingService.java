package de.subcentral.core.naming;

import java.util.Iterator;
import java.util.Map;

public interface NamingService extends Namer<Object>
{
	public String getDomain();

	public String getDefaultSeparator();

	public default String nameAll(Iterable<?> objects, Map<String, Object> parameters)
	{
		StringBuilder name = new StringBuilder();
		Iterator<?> iter = objects.iterator();
		while (iter.hasNext())
		{
			Object obj = iter.next();
			name.append(name(obj, parameters));
			if (iter.hasNext())
			{
				name.append(getDefaultSeparator());
			}
		}
		return name.toString();
	}
}
