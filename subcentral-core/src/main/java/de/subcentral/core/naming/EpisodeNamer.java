package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;

public class EpisodeNamer implements Namer<Episode>
{
	private final ImmutableMap<String, Namer<Episode>>	seriesTypeNamers;
	private final Namer<Episode>						defaultNamer;

	public EpisodeNamer(Map<String, Namer<Episode>> seriesTypeNamers, Namer<Episode> defaultNamer)
	{
		this.seriesTypeNamers = ImmutableMap.copyOf(seriesTypeNamers); // includes null check
		this.defaultNamer = Objects.requireNonNull(defaultNamer, "defaultNamer");
	}

	public ImmutableMap<String, ? extends Namer<Episode>> getSeriesTypeNamers()
	{
		return seriesTypeNamers;
	}

	public Namer<Episode> getDefaultNamer()
	{
		return defaultNamer;
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
