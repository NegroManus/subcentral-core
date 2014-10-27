package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import de.subcentral.core.model.media.Episode;

public class EpisodeNamer implements Namer<Episode>
{
	private Map<String, Namer<Episode>>	seriesTypeNamers	= new HashMap<>(3);
	private Namer<Episode>				defaultNamer		= NamingStandards.getDefaultSeasonedEpisodeNamer();

	public EpisodeNamer()
	{

	}

	public Map<String, Namer<Episode>> getSeriesTypeNamers()
	{
		return seriesTypeNamers;
	}

	public void setSeriesTypeNamers(Map<String, Namer<Episode>> seriesTypeNamers)
	{
		this.seriesTypeNamers = seriesTypeNamers;
	}

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

	public Namer<Episode> getNamer(Episode epi)
	{
		if (epi.getSeries() != null && epi.getSeries().getType() != null)
		{
			return seriesTypeNamers.getOrDefault(epi.getSeries().getType(), defaultNamer);
		}
		else
		{
			return defaultNamer;
		}
	}

	@Override
	public String name(Episode epi, Map<String, Object> parameters) throws NamingException
	{
		return getNamer(epi).name(epi, parameters);
	}

}
