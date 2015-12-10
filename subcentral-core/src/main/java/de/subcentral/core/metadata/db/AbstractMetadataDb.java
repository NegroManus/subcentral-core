package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.media.MediaUtil;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingException;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingUtil;
import de.subcentral.core.naming.NoNamerRegisteredException;

public abstract class AbstractMetadataDb implements MetadataDb
{
	private static final Logger			log				= LogManager.getLogger(AbstractMetadataDb.class);

	private final List<NamingService>	namingServices	= initNamingServices();

	protected List<NamingService> initNamingServices()
	{
		ImmutableList.Builder<NamingService> services = ImmutableList.builder();
		services.add(NamingDefaults.getDefaultNormalizingNamingService());
		services.add(NamingDefaults.getMultiEpisodeRangeNormalizingNamingService());
		return services.build();
	}

	@Override
	public Set<String> getSupportedExternalSites()
	{
		return ImmutableSet.of();
	}

	@Override
	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		return searchByObjectsName(queryObj, recordType);
	}

	protected <T> List<T> searchByObjectsName(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException, NamingException
	{
		if (queryObj == null)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<T> results = ImmutableList.builder();
		Set<String> names = NamingUtil.generateNames(queryObj, namingServices, MediaUtil.generateNamingParametersForAllNames(queryObj));
		if (names.isEmpty())
		{
			throw new NoNamerRegisteredException(queryObj, "None of the NamingServices " + namingServices + " had an appropriate namer registered");
		}
		log.debug("Searching for records of type {} with generated names {} for query object {} of type {}", recordType.getName(), names, queryObj, queryObj.getClass().getName());
		for (String name : names)
		{
			results.addAll(search(name, recordType));
		}
		return results.build();
	}

	@Override
	public <T> List<T> searchByExternalId(String externalSiteId, String id, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("siteId", getSiteId()).toString();
	}

	protected UnsupportedOperationException newUnsupportedRecordTypeException(Class<?> unsupportedType)
	{
		return new UnsupportedOperationException("The record type is not supported: " + unsupportedType + " (supported record types: " + getSupportedRecordTypes() + ")");
	}

	protected UnsupportedOperationException newRecordTypeNotSearchableException(Class<?> unsupportedType)
	{
		return new UnsupportedOperationException("The record type is not searchable: " + unsupportedType + " (searchable record types: " + getSearchableRecordTypes() + ")");
	}

	protected UnsupportedOperationException newUnsupportedExternalSiteException(String unsupportedExternalSite)
	{
		return new UnsupportedOperationException("The external site is not supported: " + unsupportedExternalSite + " (supported external sites: " + getSupportedExternalSites() + ")");
	}

}
