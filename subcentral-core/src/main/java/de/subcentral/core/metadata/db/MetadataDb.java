package de.subcentral.core.metadata.db;

import java.util.List;

public interface MetadataDb<T>
{
	public String getDomain();

	public String getDisplayName();

	public boolean isAvailable();

	public Class<T> getResultType();

	public List<T> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException;

	public List<T> queryWithObj(Object queryObj) throws MetadataDbUnavailableException, MetadataDbQueryException;

	public default Object get(String id) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		return get(id, null);
	}

	public default <E> E get(String id, Class<E> type) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		throw new UnsupportedOperationException();
	}
}
