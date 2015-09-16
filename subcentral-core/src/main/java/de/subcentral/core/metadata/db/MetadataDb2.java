package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface MetadataDb2
{
	// Metadata
	public String getDomain();

	public String getDisplayName();

	public Set<Class<?>> getRecordTypes();

	public default Set<Class<?>> getSearchableRecordTypes()
	{
		return getRecordTypes();
	}

	// Status
	public boolean isAvailable();

	// Search records
	public default List<Object> search(String query) throws IllegalArgumentException, IOException
	{
		return search(query, Object.class);
	}

	public <T> List<T> search(String query, Class<T> recordType) throws IllegalArgumentException, IOException;

	public default List<Object> searchWithObject(Object obj) throws IllegalArgumentException, IOException
	{
		return searchWithObject(obj, Object.class);
	}

	public <T> List<T> searchWithObject(Object obj, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Get record
	public default Object get(String id) throws IllegalArgumentException, IOException
	{
		return get(id, Object.class);
	}

	public <T> T get(String id, Class<T> recordType) throws IllegalArgumentException, IOException;
}
