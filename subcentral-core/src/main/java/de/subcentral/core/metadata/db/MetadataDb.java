package de.subcentral.core.metadata.db;

import java.util.List;

import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;

public interface MetadataDb<T>
{
    public String getName();

    public String getDomain();

    public boolean isAvailable();

    public Class<T> getResultType();

    public List<T> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException;

    public default List<T> queryName(Object metadataObj) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	return queryName(metadataObj, NamingDefaults.getDefaultNormalizingNamingService());
    }

    public default List<T> queryName(Object metadataObj, NamingService namingService) throws MetadataDbUnavailableException, MetadataDbQueryException
    {
	try
	{
	    return query(namingService.name(metadataObj));
	}
	catch (MetadataDbUnavailableException ue)
	{
	    throw ue;
	}
	catch (Exception e)
	{
	    throw new MetadataDbQueryException(this, metadataObj, e);
	}
    }

    public default T get(String id) throws MetadataDbQueryException
    {
	throw new UnsupportedOperationException();
    }
}
