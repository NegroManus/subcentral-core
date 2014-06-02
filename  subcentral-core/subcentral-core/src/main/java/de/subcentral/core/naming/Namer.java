package de.subcentral.core.naming;

public interface Namer<T>
{
	public Class<T> getType();

	public default String name(T obj)
	{
		return name(obj, null);
	}

	public String name(T obj, NamingService namingService);
}
