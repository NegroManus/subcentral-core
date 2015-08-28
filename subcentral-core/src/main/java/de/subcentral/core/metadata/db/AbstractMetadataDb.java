package de.subcentral.core.metadata.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.naming.ConditionalNamingService;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.naming.NoNamerRegisteredException;

public abstract class AbstractMetadataDb<T> implements MetadataDb<T>
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
	public String toString()
	{
		return getName();
	}

	@Override
	public List<T> queryWithObj(Object obj) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		try
		{
			List<T> results = new ArrayList<>();
			int noNamerRegisteredExceptionCount = 0;
			for (NamingService ns : namingServices)
			{
				try
				{
					String name = ns.name(obj, namingParameters);
					results.addAll(query(name));
				}
				catch (NoNamerRegisteredException e)
				{
					noNamerRegisteredExceptionCount++;
				}
			}
			if (noNamerRegisteredExceptionCount == namingServices.size())
			{
				throw new NoNamerRegisteredException(obj, "None of the NamingServices " + namingServices + " had an appropriate namer registered");
			}
			return results;
		}
		catch (MetadataDbUnavailableException ue)
		{
			throw ue;
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, obj, e);
		}
	}
}
