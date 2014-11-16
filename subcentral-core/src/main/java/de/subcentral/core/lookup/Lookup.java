package de.subcentral.core.lookup;

import java.util.List;

public interface Lookup<R, P>
{
	public String getName();

	public String getDomain();

	public boolean isLookupAvailable();

	public Class<R> getResultClass();

	public Class<P> getParameterBeanClass();

	public List<R> query(String query) throws LookupException;

	public List<R> queryWithParameters(P parameterBean) throws LookupException;

	public List<R> queryWithName(Object obj) throws LookupException;

	public boolean canQueryWithName(Object obj);

}
