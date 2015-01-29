package de.subcentral.core.metadata.db;

import java.util.List;

public interface MetadataDb<R>
{
	public String getName();

	public String getDomain();

	public boolean isAvailable();

	public Class<R> getResultType();

	public List<R> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException;

	public List<R> queryWithName(Object obj) throws MetadataDbUnavailableException, MetadataDbQueryException;
}
