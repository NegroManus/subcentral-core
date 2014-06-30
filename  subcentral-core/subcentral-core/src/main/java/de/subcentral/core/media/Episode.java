package de.subcentral.core.media;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.Settings;

public class Episode extends AbstractAvMediaItem implements Comparable<Episode>
{
	public static Episode newSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber)
	{
		return newSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode newSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber, String episodeTitle)
	{
		return newSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode newSeasonedEpisode(String seriesName, String seasonTitle, int episodeNumber, String episodeTitle)
	{
		return newSeasonedEpisode(seriesName, null, Media.UNNUMBERED, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode newSeasonedEpisode(String seriesName, String seriesTitle, int seasonNumber, String seasonTitle, int episodeNumber,
			String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_SEASONED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		if (seasonNumber != Media.UNNUMBERED || seasonTitle != null)
		{
			Season season = new Season(series);
			season.setNumber(seasonNumber);
			season.setTitle(seasonTitle);
			epi.setSeason(season);
		}
		epi.setNumberInSeason(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public static Episode newMiniSeriesEpisode(String seriesName, int episodeNumber)
	{
		return newMiniSeriesEpisode(seriesName, null, episodeNumber, null);
	}

	public static Episode newMiniSeriesEpisode(String seriesName, int episodeNumber, String episodeTitle)
	{
		return newMiniSeriesEpisode(seriesName, null, episodeNumber, episodeTitle);
	}

	public static Episode newMiniSeriesEpisode(String seriesName, String seriesTitle, int episodeNumber, String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_MINI_SERIES);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		epi.setNumberInSeries(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public static Episode newDatedEpisode(String seriesName, ZonedDateTime date)
	{
		return newDatedEpisode(seriesName, null, date, null);
	}

	public static Episode newDatedEpisode(String seriesName, ZonedDateTime date, String episodeTitle)
	{
		return newDatedEpisode(seriesName, null, date, episodeTitle);
	}

	public static Episode newDatedEpisode(String seriesName, String seriesTitle, ZonedDateTime date, String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series();
		series.setType(Series.TYPE_DATED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		epi.setDate(date);
		epi.setTitle(episodeTitle);
		return epi;
	}

	private Series	series;
	private Season	season;
	private int		numberInSeries	= UNNUMBERED;
	private int		numberInSeason	= UNNUMBERED;
	private boolean	special;

	public Episode()
	{

	}

	public Episode(Series series)
	{
		setSeries(series);
	}

	// Mini-series
	public Episode(Series series, int numberInSeries)
	{
		this(series, numberInSeries, null);
	}

	public Episode(Series series, int numberInSeries, String title)
	{
		setSeries(series);
		setNumberInSeries(numberInSeries);
		setTitle(title);
	}

	// Dated
	public Episode(Series series, Temporal date)
	{
		this(series, date, null);
	}

	public Episode(Series series, Temporal date, String title)
	{
		setSeries(series);
		setDate(date);
		setTitle(title);
	}

	// Seasoned
	public Episode(Series series, Season season)
	{
		this(series, season, UNNUMBERED, null);
	}

	public Episode(Series series, Season season, int numberInSeason)
	{
		this(series, season, numberInSeason, null);
	}

	public Episode(Series series, Season season, String title)
	{
		this(series, season, UNNUMBERED, title);
	}

	public Episode(Series series, Season season, int numberInSeason, String title)
	{
		setSeries(series);
		setSeason(season);
		setNumberInSeason(numberInSeason);
		setTitle(title);
	}

	public Series getSeries()
	{
		return series;
	}

	public void setSeries(Series series)
	{
		this.series = series;
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

	@Override
	public String getName()
	{
		return NamingStandards.EPISODE_NAMER.name(this);
	}

	@Override
	public String getMediaType()
	{
		return Media.TYPE_VIDEO;
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
		return series == null ? ImmutableSet.of() : series.getGenres();
	}

	@Override
	public String getOriginalLanguage()
	{
		return series == null ? null : series.getOriginalLanguage();
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return series == null ? ImmutableSet.of() : series.getCountriesOfOrigin();
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
		if (season != null && !Objects.equal(season.getSeries(), series))
		{
			throw new IllegalArgumentException("The given season is not part of this episode's series: " + season + " is not part of " + series);
		}
	}
}
