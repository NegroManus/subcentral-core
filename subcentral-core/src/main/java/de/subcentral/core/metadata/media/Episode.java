package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.PropNames;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.TemporalComparator;

public class Episode extends MediaBase implements Comparable<Episode> {
	private static final long					serialVersionUID		= -5816825595994228543L;

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
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(Episode.class, PropNames.FURTHER_INFO_LINKS);
	public static final SimplePropDescriptor	PROP_IDS				= new SimplePropDescriptor(Episode.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(Episode.class, PropNames.ATTRIBUTES);

	private Series								series;
	private Season								season;
	private Integer								numberInSeries;
	private Integer								numberInSeason;
	private boolean								special;
	private int									runningTime;

	public Episode() {
		// default constructor
	}

	public Episode(Series series) {
		setSeries(series);
	}

	public Episode(Series series, String title) {
		setSeries(series);
		setTitle(title);
	}

	// Mini-series
	public Episode(Series series, Integer numberInSeries) {
		this(series, numberInSeries, null);
	}

	public Episode(Series series, Integer numberInSeries, String title) {
		setSeries(series);
		setNumberInSeries(numberInSeries);
		setTitle(title);
	}

	// Dated
	public Episode(Series series, Temporal date) {
		this(series, date, null);
	}

	public Episode(Series series, Temporal date, String title) {
		setSeries(series);
		setDate(date);
		setTitle(title);
	}

	// Seasoned
	public Episode(Series series, Season season) {
		this(series, season, null, null);
	}

	public Episode(Series series, Season season, int numberInSeason) {
		this(series, season, numberInSeason, null);
	}

	public Episode(Series series, Season season, String title) {
		this(series, season, null, title);
	}

	public Episode(Series series, Season season, Integer numberInSeason, String title) {
		setSeries(series);
		setSeason(season);
		setNumberInSeason(numberInSeason);
		setTitle(title);
	}

	public static Episode createSeasonedEpisode(String seriesName, Integer seasonNumber, Integer episodeNumber) {
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, null);
	}

	public static Episode createSeasonedEpisode(String seriesName, Integer seasonNumber, Integer episodeNumber, String episodeTitle) {
		return createSeasonedEpisode(seriesName, null, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seasonTitle, Integer episodeNumber, String episodeTitle) {
		return createSeasonedEpisode(seriesName, null, null, seasonTitle, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, Integer episodeNumber) {
		return createSeasonedEpisode(seriesName, seriesTitle, seasonNumber, null, episodeNumber, null);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, Integer episodeNumber, String episodeTitle) {
		return createSeasonedEpisode(seriesName, seriesTitle, seasonNumber, null, episodeNumber, episodeTitle);
	}

	public static Episode createSeasonedEpisode(String seriesName, String seriesTitle, Integer seasonNumber, String seasonTitle, Integer episodeNumber, String episodeTitle) {
		Objects.requireNonNull(seriesName, "seriesName");
		Series series = new Series();
		series.setType(Series.TYPE_SEASONED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		if (seasonNumber != null || seasonTitle != null) {
			Season season = new Season(series);
			season.setNumber(seasonNumber);
			season.setTitle(seasonTitle);
			epi.setSeason(season);
		}
		epi.setNumberInSeason(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public static Episode createMiniSeriesEpisode(String seriesName, Integer episodeNumber) {
		return createMiniSeriesEpisode(seriesName, null, episodeNumber, null);
	}

	public static Episode createMiniSeriesEpisode(String seriesName, Integer episodeNumber, String episodeTitle) {
		return createMiniSeriesEpisode(seriesName, null, episodeNumber, episodeTitle);
	}

	public static Episode createMiniSeriesEpisode(String seriesName, String seriesTitle, Integer episodeNumber, String episodeTitle) {
		Objects.requireNonNull(seriesName, "seriesName");
		Series series = new Series();
		series.setType(Series.TYPE_MINI_SERIES);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		epi.setNumberInSeries(episodeNumber);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public static Episode createDatedEpisode(String seriesName, Temporal date) {
		return createDatedEpisode(seriesName, null, date, null);
	}

	public static Episode createDatedEpisode(String seriesName, Temporal date, String episodeTitle) {
		return createDatedEpisode(seriesName, null, date, episodeTitle);
	}

	public static Episode createDatedEpisode(String seriesName, String seriesTitle, Temporal date, String episodeTitle) {
		Objects.requireNonNull(seriesName, "seriesName");
		Series series = new Series();
		series.setType(Series.TYPE_DATED);
		series.setName(seriesName);
		series.setTitle(seriesTitle);
		Episode epi = new Episode(series);
		epi.setDate(date);
		epi.setTitle(episodeTitle);
		return epi;
	}

	public Series getSeries() {
		return series;
	}

	public void setSeries(Series series) {
		this.series = series;
	}

	public Season getSeason() {
		return season;
	}

	public void setSeason(Season season) throws IllegalArgumentException {
		this.season = requireSameSeries(season);
	}

	@Override
	public String getMediaType() {
		return Media.MEDIA_TYPE_EPISODE;
	}

	@Override
	public String getMediaContentType() {
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	public Integer getNumberInSeries() {
		return numberInSeries;
	}

	public void setNumberInSeries(Integer numberInSeries) {
		this.numberInSeries = numberInSeries;
	}

	public Integer getNumberInSeason() {
		return numberInSeason;
	}

	public void setNumberInSeason(Integer numberInSeason) {
		this.numberInSeason = numberInSeason;
	}

	/**
	 * 
	 * @return whether the episode is a special (non-regular episode)
	 */
	public boolean isSpecial() {
		return special;
	}

	public void setSpecial(boolean special) {
		this.special = special;
	}

	@Override
	public int getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

	@Override
	public Set<String> getGenres() {
		return series != null ? series.getGenres() : ImmutableSet.of();
	}

	@Override
	public List<String> getLanguages() {
		return series != null ? series.getLanguages() : ImmutableList.of();
	}

	@Override
	public List<String> getCountries() {
		return series != null ? series.getCountries() : ImmutableList.of();
	}

	// Convenience
	public boolean isPartOfSeason() {
		return season != null;
	}

	public boolean isNumberedInSeries() {
		return numberInSeries != null;
	}

	public boolean isNumberedInSeason() {
		return numberInSeason != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Episode) {
			Episode o = (Episode) obj;
			return Objects.equals(series, o.series) && Objects.equals(season, o.season) && Objects.equals(numberInSeason, o.numberInSeason) && Objects.equals(numberInSeries, o.numberInSeries)
					&& Objects.equals(date, o.date) && Objects.equals(title, o.title);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(series, season, numberInSeason, numberInSeries, date, title);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Episode.class)
				.omitNullValues()
				.add("series.name", series != null ? series.name : null)
				.add("numberInSeries", numberInSeries)
				.add("season.<name>", season != null ? NamingDefaults.getDefaultSeasonNamer().name(season) : null)
				.add("numberInSeason", numberInSeason)
				.add("title", title)
				.add("date", date)
				.add("special", special)
				.add("runningTime", ObjectUtil.nullIfZero(runningTime))
				.add("description", description)
				.add("images", ObjectUtil.nullIfEmpty(images))
				.add("contentRating", contentRating)
				.add("ratings", ObjectUtil.nullIfEmpty(ratings))
				.add("furtherInfoLinks", ObjectUtil.nullIfEmpty(furtherInfoLinks))
				.add("ids", ObjectUtil.nullIfEmpty(ids))
				.add("attributes", ObjectUtil.nullIfEmpty(attributes))
				.toString();
	}

	@Override
	public int compareTo(Episode o) {
		if (this == o) {
			return 0;
		}
		// nulls first
		if (o == null) {
			return 1;
		}
		// first sort criteria (apart from series) is (a) season, numberInSeason instead of (b) numberInSeries
		// because (a) works for seasoned series even if for some episodes the numberInSeries is given and for others it isn't
		// for mini-series or dated series, season and numberInSeason will be null, so they don't affect the ordering
		// but (b) will put the ones with a numberInSeries at the end -> that's not intended
		return ComparisonChain.start()
				.compare(series, o.series, ObjectUtil.getDefaultOrdering())
				.compare(season, o.season, ObjectUtil.getDefaultOrdering())
				.compare(numberInSeason, o.numberInSeason, ObjectUtil.getDefaultOrdering())
				.compare(numberInSeries, o.numberInSeries, ObjectUtil.getDefaultOrdering())
				.compare(date, o.date, TemporalComparator.INSTANCE)
				.compare(title, o.title, ObjectUtil.getDefaultStringOrdering())
				.result();
	}

	private Season requireSameSeries(Season season) throws IllegalArgumentException {
		if (season != null && !Objects.equals(season.getSeries(), series)) {
			throw new IllegalArgumentException("The given season is not part of this episode's series: " + season + " is not part of " + series);
		}
		return season;
	}
}
