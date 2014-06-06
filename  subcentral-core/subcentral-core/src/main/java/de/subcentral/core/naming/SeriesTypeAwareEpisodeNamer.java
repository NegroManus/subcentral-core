package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Series;

public class SeriesTypeAwareEpisodeNamer implements Namer<Episode>
{
	private String						assumedSeriesType	= null;
	private Namer<Episode>				standardNamer		= NamingStandards.SEASONED_EPISODE_NAMER;
	private Map<String, Namer<Episode>>	typeSpecificNamers	= new HashMap<>(3);

	public SeriesTypeAwareEpisodeNamer()
	{
		typeSpecificNamers.put(Series.TYPE_SEASONED, NamingStandards.SEASONED_EPISODE_NAMER);
		typeSpecificNamers.put(Series.TYPE_MINI_SERIES, NamingStandards.MINI_SERIES_EPISODE_NAMER);
		typeSpecificNamers.put(Series.TYPE_DATED, NamingStandards.DATED_EPISODE_NAMER);
	}

	public String getAssumedSeriesType()
	{
		return assumedSeriesType;
	}

	public void setAssumedSeriesType(String assumedSeriesType)
	{
		this.assumedSeriesType = assumedSeriesType;
	}

	public Namer<Episode> getStandardNamer()
	{
		return standardNamer;
	}

	public void setStandardNamer(Namer<Episode> standardNamer)
	{
		this.standardNamer = standardNamer;
	}

	public Map<String, Namer<Episode>> getTypeSpecificNamers()
	{
		return typeSpecificNamers;
	}

	public void setTypeSpecificNamers(Map<String, Namer<Episode>> typeSpecificNamers)
	{
		this.typeSpecificNamers = typeSpecificNamers;
	}

	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode epi, NamingService namingService)
	{
		String type = assumedSeriesType != null ? assumedSeriesType : epi.getSeries().getType();
		Namer<Episode> namer = typeSpecificNamers.get(type);
		if (namer != null)
		{
			return namer.name(epi);
		}
		return standardNamer.name(epi);
	}
}
