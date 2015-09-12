package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface MetadataDb2
{
	// Metadata
	public String getDomain();

	public String getDisplayName();

	public Set<Class<?>> getTypes();

	// Status
	public boolean isAvailable();

	// Search
	public default List<Object> search(String query) throws IllegalArgumentException, IOException
	{
		return search(query, Object.class);
	}

	public <T> List<T> search(String query, Class<T> type) throws IllegalArgumentException, IOException;

	public default List<Object> searchObject(Object obj) throws IllegalArgumentException, IOException
	{
		return searchObject(obj, Object.class);
	}

	public <T> List<T> searchObject(Object obj, Class<T> type) throws IllegalArgumentException, IOException;
}
