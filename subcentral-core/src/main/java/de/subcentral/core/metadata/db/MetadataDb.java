package de.subcentral.core.metadata.db;

import java.util.List;

public interface MetadataDb<T>
{
	public String getName();

	public String getDomain();

	public boolean isAvailable();

	public Class<T> getResultType();

	public List<T> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException;

	public List<T> queryWithName(Object obj) throws MetadataDbUnavailableException, MetadataDbQueryException;
}
