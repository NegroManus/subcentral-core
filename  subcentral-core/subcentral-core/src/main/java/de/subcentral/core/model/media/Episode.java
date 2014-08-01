package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.SimplePropDescriptor;

public class Episode extends AbstractAvMediaItem implements Comparable<Episode>
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(Episode.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_SERIES					= new SimplePropDescriptor(Episode.class, PropNames.SERIES);
	public static final SimplePropDescriptor	PROP_NUMBER_IN_SERIES		= new SimplePropDescriptor(Episode.class, PropNames.NUMBER_IN_SERIES);
	public static final SimplePropDescriptor	PROP_SEASON					= new SimplePropDescriptor(Episode.class, PropNames.SEASON);
	public static final SimplePropDescriptor	PROP_NUMBER_IN_SEASON		= new SimplePropDescriptor(Episode.class, PropNames.NUMBER_IN_SEASON);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(Episode.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE				= new SimplePropDescriptor(Episode.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(Episode.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SPECIAL				= new SimplePropDescriptor(Episode.class, PropNames.SPECIAL);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES		= new SimplePropDescriptor(Episode.class, PropNames.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN	= new SimplePropDescriptor(Episode.class, PropNames.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(Episode.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(Episode.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS				= new SimplePropDescriptor(Episode.class, PropNames.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY		= new SimplePropDescriptor(Episode.class, PropNames.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(Episode.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_URLS		= new SimplePropDescriptor(Episode.class, PropNames.FURTHER_INFO_URLS);

	public static Episode createSeasonedEpisode(String seriesName, Integer seasonNumber, Integer episodeNumber)
	{
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode createSeasonedEpisode(String seriesName, Integer seasonNumber, Integer episodeNumber, String episodeTitle)
	{
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seasonTitle, Integer episodeNumber, String episodeTitle)
	{
		return createSeasonedEpisode(seriesName, null, null, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, Integer episodeNumber,
			String episodeTitle)
	{
		return createSeasonedEpisode(seriesName, seriesTitle, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, String seasonTitle,
			Integer episodeNumber, String episodeTitle)
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
		if (seasonNumber != null || seasonTitle != null)
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

	public static Episode createMiniSeriesEpisode(String seriesName, Integer episodeNumber)
	{
		return createMiniSeriesEpisode(seriesName, null, episodeNumber, null);
	}

	public static Episode createMiniSeriesEpisode(String seriesName, Integer episodeNumber, String episodeTitle)
	{
		return createMiniSeriesEpisode(seriesName, null, episodeNumber, episodeTitle);
	}

	public static Episode createMiniSeriesEpisode(String seriesName, String seriesTitle, Integer episodeNumber, String episodeTitle)
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

	public static Episode createDatedEpisode(String seriesName, Temporal date)
	{
		return createDatedEpisode(seriesName, null, date, null);
	}

	public static Episode createDatedEpisode(String seriesName, Temporal date, String episodeTitle)
	{
		return createDatedEpisode(seriesName, null, date, episodeTitle);
	}

	public static Episode createDatedEpisode(String seriesName, String seriesTitle, Temporal date, String episodeTitle)
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
	private Integer	numberInSeries;
	private Integer	numberInSeason;
	private boolean	special;

	public Episode()
	{

	}

	public Episode(Series series)
	{
		setSeries(series);
	}

	// Mini-series
	public Episode(Series series, Integer numberInSeries)
	{
		this(series, numberInSeries, null);
	}

	public Episode(Series series, Integer numberInSeries, String title)
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
		this(series, season, null, null);
	}

	public Episode(Series series, Season season, int numberInSeason)
	{
		this(series, season, numberInSeason, null);
	}

	public Episode(Series series, Season season, String title)
	{
		this(series, season, null, title);
	}

	public Episode(Series series, Season season, Integer numberInSeason, String title)
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
		return NamingStandards.SEASONED_EPISODE_NAMER.name(this);
	}

	@Override
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_EPISODE;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	public Integer getNumberInSeries()
	{
		return numberInSeries;
	}

	public void setNumberInSeries(Integer numberInSeries)
	{
		this.numberInSeries = numberInSeries;
	}

	public Integer getNumberInSeason()
	{
		return numberInSeason;
	}

	public void setNumberInSeason(Integer numberInSeason)
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
		return series != null ? series.getGenres() : ImmutableSet.of();
	}

	@Override
	public List<String> getOriginalLanguages()
	{
		return series != null ? series.getOriginalLanguages() : ImmutableList.of();
	}

	@Override
	public List<String> getCountriesOfOrigin()
	{
		return series != null ? series.getCountriesOfOrigin() : ImmutableList.of();
	}

	// Convenience
	public boolean isPartOfSeason()
	{
		return season != null;
	}

	public boolean isNumberedInSeries()
	{
		return numberInSeries != null;
	}

	public boolean isNumberedInSeason()
	{
		return numberInSeason != null;
	}

	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && Episode.class.equals(obj.getClass()))
		{
			Episode o = (Episode) obj;
			return new EqualsBuilder().append(series, o.series)
					.append(numberInSeries, o.numberInSeries)
					.append(season, o.season)
					.append(numberInSeason, o.numberInSeason)
					.append(date, o.date)
					.append(title, title)
					.isEquals();
		}
		return false;
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
				.compare(series, o.series, Settings.createDefaultOrdering())
				.compare(numberInSeries, o.numberInSeries, Settings.createDefaultOrdering())
				.compare(season, o.season, Settings.createDefaultOrdering())
				.compare(numberInSeason, o.numberInSeason, Settings.createDefaultOrdering())
				.compare(date, o.date, Settings.TEMPORAL_ORDERING)
				.compare(title, title, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Episode.class)
				.omitNullValues()
				.add("series", series)
				.add("numberInSeries", numberInSeries)
				.add("season", season)
				.add("numberInSeason", numberInSeason)
				.add("title", title)
				.add("date", date)
				.add("special", special)
				.add("runningTime", runningTime)
				.add("description", description)
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
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
