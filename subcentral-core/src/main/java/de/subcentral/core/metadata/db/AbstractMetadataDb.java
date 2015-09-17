package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.naming.ConditionalNamingService;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NoNamerRegisteredException;

public abstract class AbstractMetadataDb implements MetadataDb
{
	private final List<NamingService>	namingServices		= initNamingServices();
	private final Map<String, Object>	namingParameters	= initNamingParameters();

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

	protected Map<String, Object> initNamingParameters()
	{
		return ImmutableMap.of();
	}

	@Override
	public Set<String> getSupportedExternalSources()
	{
		return ImmutableSet.of();
	}

	@Override
	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return searchByObjectsName(queryObj, recordType);
	}

	protected <T> List<T> searchByObjectsName(Object queryObj, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		List<T> results = new ArrayList<>();
		int noNamerRegisteredExceptionCount = 0;
		for (NamingService ns : namingServices)
		{
			try
			{
				String name = ns.name(queryObj, namingParameters);
				results.addAll(search(name, recordType));
			}
			catch (NoNamerRegisteredException e)
			{
				noNamerRegisteredExceptionCount++;
			}
		}
		if (noNamerRegisteredExceptionCount == namingServices.size())
		{
			throw new IllegalArgumentException(new NoNamerRegisteredException(queryObj, "None of the NamingServices " + namingServices + " had an appropriate namer registered"));
		}
		return results;
	}

	@Override
	public <T> List<T> searchByExternalId(String externalSource, String id, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(String id, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("name", getName()).add("displayName", getDisplayName()).toString();
	}

	protected IllegalArgumentException createUnsupportedRecordTypeException(Class<?> unsupportedType) throws IllegalArgumentException
	{
		return new IllegalArgumentException("The record type is not supported: " + unsupportedType + " (record types: " + getRecordTypes() + ")");
	}

	protected IllegalArgumentException createRecordTypeNotSearchableException(Class<?> unsupportedType) throws IllegalArgumentException
	{
		return new IllegalArgumentException("The record type is not searchable: " + unsupportedType + " (searchable record types: " + getSearchableRecordTypes() + ")");
	}

	protected IllegalArgumentException createUnsupportedExternalSource(String unsupportedExternalSource) throws IllegalArgumentException
	{
		return new IllegalArgumentException("The external source is not supported: " + unsupportedExternalSource + " (supported external sources: " + getSupportedExternalSources() + ")");
	}
}
