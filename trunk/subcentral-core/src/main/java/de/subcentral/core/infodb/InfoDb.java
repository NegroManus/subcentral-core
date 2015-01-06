package de.subcentral.core.infodb;

import java.util.List;

public interface InfoDb<R>
{
	public String getName();

	public String getDomain();

	public boolean isInfoDbAvailable();

	public Class<R> getResultType();

	public List<R> query(String query) throws InfoDbUnavailableException, InfoDbQueryException;

	public List<R> queryWithName(Object obj) throws InfoDbUnavailableException, InfoDbQueryException;
}
