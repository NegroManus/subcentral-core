package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.TemporalComparator;

public class Episode extends AbstractMedia implements Comparable<Episode>
{
	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(Episode.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_SERIES				= new SimplePropDescriptor(Episode.class, PropNames.SERIES);
	public static final SimplePropDescriptor	PROP_NUMBER_IN_SERIES	= new SimplePropDescriptor(Episode.class, PropNames.NUMBER_IN_SERIES);
	public static final SimplePropDescriptor	PROP_SEASON				= new SimplePropDescriptor(Episode.class, PropNames.SEASON);
	public static final SimplePropDescriptor	PROP_NUMBER_IN_SEASON	= new SimplePropDescriptor(Episode.class, PropNames.NUMBER_IN_SEASON);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(Episode.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE			= new SimplePropDescriptor(Episode.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(Episode.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SPECIAL			= new SimplePropDescriptor(Episode.class, PropNames.SPECIAL);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(Episode.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES			= new SimplePropDescriptor(Episode.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_GENRES				= new SimplePropDescriptor(Episode.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(Episode.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_RATINGS			= new SimplePropDescriptor(Episode.class, PropNames.RATINGS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(Episode.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_IMAGES				= new SimplePropDescriptor(Episode.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO		= new SimplePropDescriptor(Episode.class, PropNames.FURTHER_INFO);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(Episode.class, PropNames.ATTRIBUTES);

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

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, Integer episodeNumber)
	{
		return createSeasonedEpisode(seriesName, seriesTitle, seasonNumber, null, episodeNumber, null);
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
			throw new IllegalArgumentException("Series' name must be set");
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
			throw new IllegalArgumentException("Series' name must be set");
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
			throw new IllegalArgumentException("Series' name must be set");
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
	private int		runningTime;

	public Episode()
	{

	}

	public Episode(Series series)
	{
		setSeries(series);
	}

	public Episode(Series series, String title)
	{
		setSeries(series);
		setTitle(title);
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

	@Override
	public String getName()
	{
		return NamingDefaults.getDefaultEpisodeNamer().name(this);
	}

	@Override
	public List<String> getAliasNames()
	{
		return ImmutableList.of();
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

	public void setSeason(Season season) throws IllegalArgumentException
	{
		this.season = requireSameSeries(season);
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

	/**
	 * 
	 * @return whether the episode is a special (non-regular episode)
	 */
	public boolean isSpecial()
	{
		return special;
	}

	public void setSpecial(boolean special)
	{
		this.special = special;
	}

	@Override
	public int getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(int runningTime)
	{
		this.runningTime = runningTime;
	}

	@Override
	public Set<String> getGenres()
	{
		return series != null ? series.getGenres() : ImmutableSet.of();
	}

	@Override
	public List<String> getLanguages()
	{
		return series != null ? series.getLanguages() : ImmutableList.of();
	}

	@Override
	public List<String> getCountries()
	{
		return series != null ? series.getCountries() : ImmutableList.of();
	}

	// Convenience
	public boolean isPartOfSeries()
	{
		return series != null;
	}

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

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Episode)
		{
			Episode o = (Episode) obj;
			return Objects.equals(series, o.series) && Objects.equals(season, o.season) && Objects.equals(numberInSeason, o.numberInSeason)
					&& Objects.equals(numberInSeries, o.numberInSeries) && Objects.equals(date, o.date)
					&& StringUtils.equalsIgnoreCase(title, o.title);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(7, 15).append(series)
				.append(season)
				.append(numberInSeason)
				.append(numberInSeries)
				.append(date)
				.append(StringUtils.lowerCase(title, Locale.ENGLISH))
				.toHashCode();
	}

	@Override
	public int compareTo(Episode o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		// first sort criteria (apart from series) is (a) season, numberInSeason instead of (b) numberInSeries
		// because (a) works for seasoned series even if for some episodes the numberInSeries is given and for others it isn't
		// for mini-series or dated series, season and numberInSeason will be null, so they don't affect the ordering
		// but (b) will put the ones with a numberInSeries at the end -> that's not intended
		return ComparisonChain.start()
				.compare(series, o.series, Settings.createDefaultOrdering())
				.compare(season, o.season, Settings.createDefaultOrdering())
				.compare(numberInSeason, o.numberInSeason, Settings.createDefaultOrdering())
				.compare(numberInSeries, o.numberInSeries, Settings.createDefaultOrdering())
				.compare(date, o.date, TemporalComparator.INSTANCE)
				.compare(title, title, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Episode.class)
				.omitNullValues()
				.add("series", series)
				.add("numberInSeries", numberInSeries)
				.add("season", season)
				.add("numberInSeason", numberInSeason)
				.add("title", title)
				.add("date", date)
				.add("special", special)
				.add("runningTime", BeanUtil.nullIfZero(runningTime))
				.add("description", description)
				.add("images", BeanUtil.nullIfEmpty(images))
				.add("contentRating", contentRating)
				.add("ratings", BeanUtil.nullIfEmpty(ratings))
				.add("furtherInfo", BeanUtil.nullIfEmpty(furtherInfo))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}

	private Season requireSameSeries(Season season) throws IllegalArgumentException
	{
		if (season != null && !Objects.equals(season.getSeries(), series))
		{
			throw new IllegalArgumentException("The given season is not part of this episode's series: " + season + " is not part of " + series);
		}
		return season;
	}
}
