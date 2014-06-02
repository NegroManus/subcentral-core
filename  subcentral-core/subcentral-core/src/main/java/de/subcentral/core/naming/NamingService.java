package de.subcentral.core.naming;

public interface NamingService
{
	public String getDomain();

	public <T> String name(T obj);
}
