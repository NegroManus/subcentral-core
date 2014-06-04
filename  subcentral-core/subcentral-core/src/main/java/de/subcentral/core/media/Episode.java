package de.subcentral.core.media;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.naming.Nameables;

public class Episode extends AbstractAvMedia implements Comparable<Episode>
{
	private final Series	series;
	private Season			season;
	private int				numberInSeries	= UNNUMBERED;
	private int				numberInSeason	= UNNUMBERED;
	private boolean			special;

	Episode(Series series, Season season)
	{
		this.series = series;
		setSeason(season);
	}

	public Series getSeries()
	{
		return series;
	}

	public Season getSeason()
	{
		return season;
	}

	public void setSeason(Season season)
	{
		ensurePartOfSeries(season);
		this.season = season;
	}

	public int getNumberInSeries()
	{
		return numberInSeries;
	}

	public void setNumberInSeries(int numberInSeries)
	{
		this.numberInSeries = numberInSeries;
	}

	public int getNumberInSeason()
	{
		return numberInSeason;
	}

	public void setNumberInSeason(int numberInSeason)
	{
		this.numberInSeason = numberInSeason;
	}

	public boolean isSpecial()
	{
		return special;
	}

	public void setSpecial(boolean special)
	{
		this.special = special;
	}

	@Override
	public String getOriginalLanguage()
	{
		return series != null ? series.getOriginalLanguage() : null;
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return series != null ? Collections.unmodifiableSet(series.getCountriesOfOrigin()) : ImmutableSet.<String> of();
	}

	// Convenience
	public boolean isPartOfSeason()
	{
		return season != null;
	}

	public boolean isNumberedInSeries()
	{
		return numberInSeries != Media.UNNUMBERED;
	}

	public boolean isNumberedInSeason()
	{
		return numberInSeason != Media.UNNUMBERED;
	}

	public boolean isTitled()
	{
		return title != null;
	}

	@Override
	public int compareTo(Episode o)
	{
		return Nameables.NAME_COMPARATOR.compare(this, o);
	}

	private void ensurePartOfSeries(Season season) throws IllegalArgumentException
	{
		if (season != null && !series.containsSeason(season))
		{
			throw new IllegalArgumentException("The season is not part of this episodes series: " + season);
		}
	}
}
