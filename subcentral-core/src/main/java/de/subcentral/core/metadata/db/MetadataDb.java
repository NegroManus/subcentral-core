package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import de.subcentral.core.metadata.Site;

public interface MetadataDb
{
	public enum State
	{
		AVAILABLE, AVAILABLE_LIMITED, NOT_AVAILABLE
	}

	// Metadata
	public Site getSite();

	public Set<Class<?>> getSupportedRecordTypes();

	public default Set<Class<?>> getSearchableRecordTypes()
	{
		return getSupportedRecordTypes();
	}

	public Set<Site> getSupportedExternalSites();

	// State
	public State checkState();

	// Search records
	// Search by Text
	public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Search by object
	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Search by external id
	public <T> List<T> searchByExternalId(Site externalSite, String externalId, Class<T> recordType) throws UnsupportedOperationException, IOException;

	// Get record
	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException;
}
