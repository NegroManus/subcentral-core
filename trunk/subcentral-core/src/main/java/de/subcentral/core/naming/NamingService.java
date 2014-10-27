package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.StringUtil;

public interface NamingService extends Namer<Object>
{
	public String getDomain();

	public String getDefaultSeparator();

	public boolean canName(Object candidate);

	public default String nameAll(Iterable<?> candidates, Map<String, Object> parameters)
	{
		return nameAll(candidates, getDefaultSeparator(), parameters);
	}

	public default String nameAll(Iterable<?> candidates, String separator, Map<String, Object> parameters)
	{
		StringBuilder name = new StringBuilder();
		for (Object candidate : candidates)
		{
			name.append(name(candidate, parameters));
			name.append(separator);
		}
		return StringUtil.stripEnd(name, separator).toString();
	}

	public default List<String> nameEach(Iterable<?> candidates, Map<String, Object> parameters)
	{
		ImmutableList.Builder<String> names = ImmutableList.builder();
		for (Object o : candidates)
		{
			names.add(name(o, parameters));
		}
		return names.build();
	}
}
