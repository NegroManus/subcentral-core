package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.media.MediaUtil;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.naming.ConditionalNamingService;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingException;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NamingUtil;
import de.subcentral.core.naming.NoNamerRegisteredException;

public abstract class AbstractMetadataDb implements MetadataDb
{
	private final List<NamingService> namingServices = initNamingServices();

	protected List<NamingService> initNamingServices()
	{
		ImmutableList.Builder<NamingService> services = ImmutableList.builder();
		services.add(NamingDefaults.getDefaultNormalizingNamingService());

		// Add a special NamingService which formats the episode numbers different than the default NamingService
		// for ex. S09E23-E24 instead of S09E23E24
		ConditionalNamingService alternateNamingService = new ConditionalNamingService("multiepisode_range_only");
		alternateNamingService.getConditionalNamingEntries().add(ConditionalNamingEntry.of(MultiEpisodeHelper::isMultiEpisode, NamingDefaults.getRangeOnlyMultiEpisodeNamer()));
		NamingService normalizingAlternateNs = NamingDefaults.createNormalizingNamingService(alternateNamingService);
		services.add(normalizingAlternateNs);

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
		List<T> results = new ArrayList<>();
		Set<String> names = NamingUtil.generateNames(queryObj, namingServices, MediaUtil.generateNamingParametersForAllNames(queryObj));
		if (names.isEmpty())
		{
			throw new NoNamerRegisteredException(queryObj, "None of the NamingServices " + namingServices + " had an appropriate namer registered");
		}
		for (String name : names)
		{
			results.addAll(search(name, recordType));
		}
		return ImmutableList.copyOf(results);
	}

	@Override
	public <T> List<T> searchByExternalId(String siteId, String id, Class<T> recordType) throws UnsupportedOperationException, IOException
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

	protected UnsupportedOperationException createUnsupportedRecordTypeException(Class<?> unsupportedType) throws UnsupportedOperationException
	{
		return new UnsupportedOperationException("The record type is not supported: " + unsupportedType + " (record types: " + getRecordTypes() + ")");
	}

	protected UnsupportedOperationException createRecordTypeNotSearchableException(Class<?> unsupportedType) throws UnsupportedOperationException
	{
		return new UnsupportedOperationException("The record type is not searchable: " + unsupportedType + " (searchable record types: " + getSearchableRecordTypes() + ")");
	}

	protected UnsupportedOperationException createUnsupportedExternalSiteException(String unsupportedExternalSite) throws UnsupportedOperationException
	{
		return new UnsupportedOperationException("The external site is not supported: " + unsupportedExternalSite + " (supported external sites: " + getSupportedExternalSites() + ")");
	}

}
