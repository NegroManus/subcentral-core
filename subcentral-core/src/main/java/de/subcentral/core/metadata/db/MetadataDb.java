package de.subcentral.core.metadata.db;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface MetadataDb<T>
{
    public String getName();

    public String getDomain();

    public boolean isAvailable();

    public Class<T> getResultType();

    public List<T> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException;

    public default List<T> queryName(Object metadataObj) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	return queryName(metadataObj, ImmutableMap.of());
    }

    public List<T> queryName(Object metadataObj, Map<String, Object> namingParameters) throws MetadataDbUnavailableException, MetadataDbQueryException;

    public default Object get(String id) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	return get(id, null);
    }

    public default <E> E get(String id, Class<E> type) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	throw new UnsupportedOperationException();
    }
}
