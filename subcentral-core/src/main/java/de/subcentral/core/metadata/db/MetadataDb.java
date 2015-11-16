package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface MetadataDb
{
	// Metadata
	public String getSiteId();

	public String getDisplayName();

	public Set<Class<?>> getRecordTypes();

	public default Set<Class<?>> getSearchableRecordTypes()
	{
		return getRecordTypes();
	}

	public Set<String> getSupportedExternalSources();

	// Status
	public boolean isAvailable();

	// Search records
	// Search by Text
	public default List<Object> search(String query) throws UnsupportedOperationException, IOException
	{
		return search(query, Object.class);
	}

	public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Search by object
	public default List<Object> searchByObject(Object queryObj) throws UnsupportedOperationException, IOException
	{
		return searchByObject(queryObj, Object.class);
	}

	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Search by external id
	public default List<Object> searchByExternalId(String externalSource, String id) throws UnsupportedOperationException, IOException
	{
		return searchByExternalId(externalSource, id, Object.class);
	}

	public <T> List<T> searchByExternalId(String externalSource, String id, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Get record
	public default Object get(String id) throws UnsupportedOperationException, IOException
	{
		return get(id, Object.class);
	}

	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException;
}
