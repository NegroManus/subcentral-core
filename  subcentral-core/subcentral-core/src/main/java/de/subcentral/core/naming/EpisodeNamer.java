package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import de.subcentral.core.model.media.Episode;

public class EpisodeNamer implements Namer<Episode>
{
	private Map<String, Namer<Episode>>	seriesTypeNamers	= new HashMap<>(4);
	private Namer<Episode>				defaultNamer		= NamingStandards.SEASONED_EPISODE_NAMER;

	public Namer<Episode> registerSeriesTypeNamer(String seriesType, Namer<Episode> namer)
	{
		return seriesTypeNamers.put(seriesType, namer);
	}

	public Namer<Episode> unregisterSeriesTypeNamer(String seriesType)
	{
		return seriesTypeNamers.remove(seriesType);
	}

	public Namer<Episode> getDefaultNamer()
	{
		return defaultNamer;
	}

	public void setDefaultNamer(Namer<Episode> defaultNamer)
	{
		Validate.notNull(defaultNamer, "defaultNamer");
		this.defaultNamer = defaultNamer;
	}

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode epi, Map<String, Object> parameters) throws NamingException
	{
		String type;
		Namer<Episode> namer;
		if (epi.getSeries() != null && (type = epi.getSeries().getType()) != null)
		{
			namer = seriesTypeNamers.getOrDefault(type, defaultNamer);
		}
		else
		{
			namer = defaultNamer;
		}
		return namer.name(epi, parameters);
	}

}
