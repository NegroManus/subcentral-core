package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import de.subcentral.core.metadata.Site;

public interface MetadataDb
{
	// Metadata
	public Site getSite();

	public Set<Class<?>> getSupportedRecordTypes();

	public default Set<Class<?>> getSearchableRecordTypes()
	{
		return getSupportedRecordTypes();
	}

	public Set<Site> getSupportedExternalSites();

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
	public default List<Object> searchByExternalId(Site externalSite, String externalId) throws UnsupportedOperationException, IOException
	{
		return searchByExternalId(externalSite, externalId, Object.class);
	}

	public <T> List<T> searchByExternalId(Site externalSite, String externalId, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Get record
	public default Object get(String id) throws UnsupportedOperationException, IOException
	{
		return get(id, Object.class);
	}

	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException;
}
