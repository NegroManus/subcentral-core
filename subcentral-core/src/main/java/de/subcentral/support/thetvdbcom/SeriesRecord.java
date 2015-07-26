package de.subcentral.support.thetvdbcom;

import java.util.Objects;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;

public class SeriesRecord
{
	private final Series					series;
	private final ImmutableList<Season>		seasons;
	private final ImmutableList<Episode>	episodes;

	// package visibility
	SeriesRecord(Series series, Iterable<Season> seasons, Iterable<Episode> episodes)
	{
		this.series = Objects.requireNonNull(series, "series");
		this.seasons = ImmutableList.copyOf(seasons);
		this.episodes = ImmutableList.copyOf(episodes);
	}

	public Series getSeries()
	{
		return series;
	}

	public ImmutableList<Season> getSeasons()
	{
		return seasons;
	}

	public ImmutableList<Episode> getEpisodes()
	{
		return episodes;
	}
}
