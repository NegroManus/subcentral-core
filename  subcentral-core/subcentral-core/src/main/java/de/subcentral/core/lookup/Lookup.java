package de.subcentral.core.lookup;

public interface Lookup<R, Q>
{
	public String getDomain();

	public boolean isLookupAvailable();

	public LookupResult<R> lookup(String query) throws Exception;

	public LookupResult<R> lookup(Q query) throws Exception;
}
