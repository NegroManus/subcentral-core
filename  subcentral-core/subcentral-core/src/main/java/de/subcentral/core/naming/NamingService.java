package de.subcentral.core.naming;

public interface NamingService
{
	public String getDomain();

	public boolean canName(Object candidate);

	public <T> String name(T candidate) throws NamingException;
}
