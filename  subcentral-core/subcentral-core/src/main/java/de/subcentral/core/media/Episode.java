package de.subcentral.core.media;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.Settings;

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

	@Override
	public String getName()
	{
		return NamingStandards.EPISODE_NAMER.name(this);
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
	public Set<String> getGenres()
	{
		return series.getGenres();
	}

	@Override
	public String getOriginalLanguage()
	{
		return series.getOriginalLanguage();
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return series.getCountriesOfOrigin();
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

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (Episode.class != obj.getClass())
		{
			return false;
		}
		Episode o = (Episode) obj;
		return new EqualsBuilder().append(series, o.series)
				.append(numberInSeries, o.numberInSeries)
				.append(season, o.season)
				.append(numberInSeason, o.numberInSeason)
				.append(date, o.date)
				.append(title, title)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(7, 15).append(series)
				.append(numberInSeries)
				.append(season)
				.append(numberInSeason)
				.append(date)
				.append(title)
				.toHashCode();
	}

	@Override
	public int compareTo(Episode o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(series, o.series)
				.compare(numberInSeries, o.numberInSeries)
				.compare(season, o.season)
				.compare(numberInSeason, o.numberInSeason)
				.compare(date, o.date, Settings.TEMPORAL_ORDERING)
				.compare(title, title, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("series", series)
				.add("numberInSeries", numberInSeries)
				.add("season", season)
				.add("numberInSeason", numberInSeason)
				.add("date", date)
				.add("title", title)
				.add("special", special)
				.add("runningTime", runningTime)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInformationLinks", furtherInformationUrls)
				.toString();
	}

	private void ensurePartOfSeries(Season season) throws IllegalArgumentException
	{
		if (season != null && !series.containsSeason(season))
		{
			throw new IllegalArgumentException("The season is not part of this episode's series: " + season);
		}
	}
}
