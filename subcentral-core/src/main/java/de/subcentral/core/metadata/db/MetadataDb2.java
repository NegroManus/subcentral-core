package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface MetadataDb2
{
	// Metadata
	public String getName();

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
	public default List<? extends Object> search(String query) throws IllegalArgumentException, IOException
	{
		return search(query, Object.class);
	}

	public <T> List<? extends T> search(String query, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Search by object
	public default List<? extends Object> searchByObject(Object queryObj) throws IllegalArgumentException, IOException
	{
		return searchByObject(queryObj, Object.class);
	}

	public <T> List<? extends T> searchByObject(Object queryObj, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Search by external id
	public default List<? extends Object> searchByExternalId(String externalSource, String id) throws IllegalArgumentException, IOException
	{
		return searchByExternalId(externalSource, id, Object.class);
	}

	public <T> List<? extends T> searchByExternalId(String externalSource, String id, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Get record
	public default Object get(String id) throws IllegalArgumentException, IOException
	{
		return get(id, Object.class);
	}

	public <T> T get(String id, Class<T> recordType) throws IllegalArgumentException, IOException;
}
