package de.subcentral.core.model.media;

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
import de.subcentral.core.util.SimplePropertyDescriptor;

public class Episode extends AbstractAvMediaItem implements Comparable<Episode>
{
	public static final String						PROP_NAME_SERIES				= "series";
	public static final String						PROP_NAME_NUMBER_IN_SERIES		= "numberInSeries";
	public static final String						PROP_NAME_SEASON				= "season";
	public static final String						PROP_NAME_NUMBER_IN_SEASON		= "numberInSeason";
	public static final String						PROP_NAME_SPECIAL				= "special";

	public static final SimplePropertyDescriptor	PROP_NAME						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_SERIES						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_SERIES);
	public static final SimplePropertyDescriptor	PROP_NUMBER_IN_SERIES			= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_NUMBER_IN_SERIES);
	public static final SimplePropertyDescriptor	PROP_SEASON						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_SEASON);
	public static final SimplePropertyDescriptor	PROP_NUMBER_IN_SEASON			= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_NUMBER_IN_SEASON);
	public static final SimplePropertyDescriptor	PROP_TITLE						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_TITLE);
	public static final SimplePropertyDescriptor	PROP_MEDIA_TYPE					= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_MEDIA_TYPE);
	public static final SimplePropertyDescriptor	PROP_DATE						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_SPECIAL					= new SimplePropertyDescriptor(Episode.class, PROP_NAME_SPECIAL);
	public static final SimplePropertyDescriptor	PROP_ORIGINAL_LANGUAGE			= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_ORIGINAL_LANGUAGE);
	public static final SimplePropertyDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_COUNTRIES_OF_ORIGIN);
	public static final SimplePropertyDescriptor	PROP_GENRES						= new SimplePropertyDescriptor(Episode.class, PROP_NAME_GENRES);
	public static final SimplePropertyDescriptor	PROP_DESCRIPTION				= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_DESCRIPTION);
	public static final SimplePropertyDescriptor	PROP_COVER_URL					= new SimplePropertyDescriptor(Episode.class, PROP_NAME_COVER_URL);
	public static final SimplePropertyDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_CONTENT_ADVISORY);
	public static final SimplePropertyDescriptor	PROP_FURHTER_INFORMATION_URLS	= new SimplePropertyDescriptor(Episode.class,
																							PROP_NAME_FURHTER_INFORMATION_URLS);

	public static Episode createSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber)
	{
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode createSeasonedEpisode(String seriesName, int seasonNumber, int episodeNumber, String episodeTitle)
	{
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seasonTitle, int episodeNumber, String episodeTitle)
	{
		return createSeasonedEpisode(seriesName, null, null, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, String seasonTitle, int episodeNumber,
			String episodeTitle)
	{
		if (seriesName == null)
		{
			throw new IllegalArgumentException("Series name must be set");
		}
		Series series = new Series(seriesName);
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

	public static Episode createMiniSeriesEpisode(String seriesName, String seriesTitle, int episodeNumber, String episodeTitle)
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

	public static Episode createDatedEpisode(String seriesName, ZonedDateTime date)
	{
		return createDatedEpisode(seriesName, null, date, null);
	}

	public static Episode createDatedEpisode(String seriesName, ZonedDateTime date, String episodeTitle)
	{
		return createDatedEpisode(seriesName, null, date, episodeTitle);
	}

	public static Episode createDatedEpisode(String seriesName, String seriesTitle, ZonedDateTime date, String episodeTitle)
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
		return Media.TYPE_VIDEO;
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
		return Objects.toStringHelper(this)
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