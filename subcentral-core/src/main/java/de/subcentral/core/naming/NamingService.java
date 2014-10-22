package de.subcentral.core.naming;

public interface NamingService extends Namer<Object>
{
	public String getDomain();

	public boolean canName(Object candidate);
}
