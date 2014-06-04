package de.subcentral.core.lookup;

public interface Lookup<R, Q>
{
	public String getDomain();

	public Class<R> getResultClass();

	public boolean isLookupAvailable();

	public LookupResult<R> lookup(Q query) throws LookupException;

	public default LookupResult<R> lookup(String query) throws LookupException
	{
		return lookup(createQuery(query));
	}

	public Q createQuery(String queryString);
}
