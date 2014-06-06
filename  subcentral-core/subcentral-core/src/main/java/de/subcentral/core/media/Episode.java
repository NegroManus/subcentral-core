package de.subcentral.core.media;

import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.naming.NamingStandards;

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
		return computeName();
	}

	@Override
	public String computeName()
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
				.append(season, o.season)
				.append(numberInSeries, o.numberInSeries)
				.append(numberInSeason, o.numberInSeason)
				.append(date, o.date)
				.append(title, o.title)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(7, 15).append(series)
				.append(season)
				.append(numberInSeries)
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
			return 1;
		}
		return new CompareToBuilder().append(series, o.series)
				.append(numberInSeries, o.numberInSeries)
				.append(season, o.season)
				.append(numberInSeason, o.numberInSeason)
				.append(date, o.date)
				.append(title, o.title)
				.toComparison();
	}

	private void ensurePartOfSeries(Season season) throws IllegalArgumentException
	{
		if (season != null && !series.containsSeason(season))
		{
			throw new IllegalArgumentException("The season is not part of this episodes series: " + season);
		}
	}
}
