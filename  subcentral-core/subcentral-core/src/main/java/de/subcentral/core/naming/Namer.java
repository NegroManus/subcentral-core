package de.subcentral.core.naming;

public interface Namer<T>
{
	public Class<T> getType();

	public default String name(T candidate) throws NamingException
	{
		return name(candidate, null);
	}

	public String name(T candidate, NamingService namingService) throws NamingException;
}
