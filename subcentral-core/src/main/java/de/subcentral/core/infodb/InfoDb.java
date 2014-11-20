package de.subcentral.core.infodb;

import java.util.List;

public interface InfoDb<R, P>
{
	public String getName();

	public String getDomain();

	public boolean isInfoDbAvailable();

	public Class<R> getResultType();

	public Class<P> getQueryParametersType();

	public List<R> query(String query) throws InfoDbQueryException;

	public List<R> queryWithParameters(P parameterBean) throws InfoDbQueryException;

	public List<R> queryWithName(Object obj) throws InfoDbQueryException;

	public boolean canQueryWithName(Object obj);

}
