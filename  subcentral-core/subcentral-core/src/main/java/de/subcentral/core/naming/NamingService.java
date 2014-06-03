package de.subcentral.core.naming;

public interface NamingService
{
	public String getDomain();

	public boolean canName(Object obj);

	public <T> String name(T obj);
}
