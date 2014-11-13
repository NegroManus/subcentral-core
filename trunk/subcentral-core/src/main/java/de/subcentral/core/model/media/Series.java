package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

public class Series extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Series>
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(Series.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(Series.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_TYPE					= new SimplePropDescriptor(Series.class, PropNames.TYPE);
	public static final SimplePropDescriptor	PROP_STATE					= new SimplePropDescriptor(Series.class, PropNames.STATE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(Series.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES		= new SimplePropDescriptor(Series.class, PropNames.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN	= new SimplePropDescriptor(Series.class, PropNames.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(Series.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(Series.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS				= new SimplePropDescriptor(Series.class, PropNames.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY		= new SimplePropDescriptor(Series.class, PropNames.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(Series.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FINFO_URLS				= new SimplePropDescriptor(Series.class, PropNames.FURTHER_INFO_URLS);
	public static final SimplePropDescriptor	PROP_SEASONS				= new SimplePropDescriptor(Series.class, PropNames.SEASONS);
	public static final SimplePropDescriptor	PROP_EPISODES				= new SimplePropDescriptor(Series.class, PropNames.EPISODES);

	/**
	 * A type of series which episodes are organized in seasons. Typically, episodes belong to a season and are numbered in that season. Typical
	 * examples are the TV series "Breaking Bad", "Game of Thrones" and "Psych".
	 */
	public static final String					TYPE_SEASONED				= "SEASONED";

	/**
	 * A type of series which has a limited set of episodes and these episodes are therefore not organized in seasons. A typical example is the TV
	 * mini-series "Band of Brothers".
	 */
	public static final String					TYPE_MINI_SERIES			= "MINI_SERIES";

	/**
	 * A type of series which episodes usually have no numbers. Instead the main identifier is their air date. Typical examples are (daily) shows or
	 * sports events.
	 */
	public static final String					TYPE_DATED					= "DATED";

	/**
	 * If a series is "continuing", then there will be more episodes to come.
	 */
	public static final String					STATE_CONTINUING			= "CONTINUING";

	/**
	 * If a series has "ended", there will be no more episodes to come. Either because the series was cancelled or it simply is complete.
	 */
	public static final String					STATE_ENDED					= "ENDED";

	private String								name;
	private Temporal							finaleDate;
	private String								type;
	private String								state;
	private final List<String>					originalLanguages			= new ArrayList<>(1);
	private final List<String>					countriesOfOrigin			= new ArrayList<>(1);
	private int									regularRunningTime			= 0;
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<String>					genres						= new HashSet<>(4);
	// Normally, the Seasons and Episodes are not stored in the Series
	private final List<Season>					seasons						= new ArrayList<>(0);
	private final List<Episode>					episodes					= new ArrayList<>(0);

	public Series()
	{}

	public Series(String name)
	{
		this(name, null);
	}

	public Series(String name, String title)
	{
		setName(name);
		setTitle(title);
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the date of the Series' finale. This is the date of the last / final Episode of this Series. The date of the premiere is stored in
	 * {@link #getDate()}.
	 * <p>
	 * <b>Note:</b> The premiere date and finale date may be stored redundantly if this Series contains all its Episodes in {@link #getEpisodes() the
	 * Episodes list}. Then the premiere date and finale date can be retrieved via {@link #getDateOfFirstEpisode()} and
	 * {@link #getDateOfLastEpisode()} respectively. But if this Series object does not contain all its Episodes (because they are unknown), then the
	 * dates can be stored explicitly in the {@link #getDate() date} and {@link #getFinaleDate() finaleDate} properties.
	 * </p>
	 * 
	 * @see #getDate()
	 * @see #getDateOfFirstEpisode()
	 * @see #getDateOfLastEpisode()
	 * 
	 * @return the date of the finale
	 */
	public Temporal getFinaleDate()
	{
		return finaleDate;
	}

	public void setFinaleDate(Temporal finaleDate)
	{
		this.finaleDate = Models.validateTemporalClass(finaleDate);
	}

	@Override
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_SERIES;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	@Override
	public List<String> getOriginalLanguages()
	{
		return originalLanguages;
	}

	public void setOriginalLanguages(List<String> originalLanguages)
	{
		this.originalLanguages.clear();
		this.originalLanguages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(List<String> countriesOfOrigin)
	{
		this.countriesOfOrigin.clear();
		this.countriesOfOrigin.addAll(countriesOfOrigin);
	}

	/**
	 * The regular running time in milliseconds of the episodes of this Series.
	 * 
	 * @return the regular running time in milliseconds, <code>0</code> if unknown
	 */
	public int getRegularRunningTime()
	{
		return regularRunningTime;
	}

	public void setRegularRunningTime(int regularRunningTime)
	{
		this.regularRunningTime = regularRunningTime;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres.clear();
		this.genres.addAll(genres);
	}

	// Convenience
	public String getPrimaryOriginalLanguage()
	{
		return originalLanguages.isEmpty() ? null : originalLanguages.get(0);
	}

	@Override
	public String getPrimaryCountryOfOrigin()
	{
		return countriesOfOrigin.isEmpty() ? null : countriesOfOrigin.get(0);
	}

	/**
	 * @return the air date ({@link Episode#getDate()}) of the first episode of this series or <code>null</code> if this series has no episodes
	 */
	public Temporal getDateOfFirstEpisode()
	{
		return episodes.isEmpty() ? null : episodes.get(0).getDate();
	}

	/**
	 * @return the air date ({@link Episode#getDate()}) of the last episode of this series or <code>null</code> if this series has no episodes
	 */
	public Temporal getDateOfLastEpisode()
	{
		return episodes.isEmpty() ? null : episodes.get(episodes.size() - 1).getDate();
	}

	// Episodes
	@Override
	public List<Episode> getMediaItems()
	{
		return getEpisodes();
	}

	/**
	 * 
	 * @return This series' episodes. Only filled if the series is the information root.
	 */
	public List<Episode> getEpisodes()
	{
		return episodes;
	}

	public void setEpisodes(Collection<Episode> episodes)
	{
		this.episodes.clear();
		this.episodes.addAll(episodes);
	}

	public Episode addEpisode()
	{
		Episode epi = new Episode(this);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(String title)
	{
		Episode epi = new Episode(this, title);
		episodes.add(epi);
		return epi;
	}

	// Mini-series
	public Episode addEpisode(Integer numberInSeries)
	{
		Episode epi = new Episode(this, numberInSeries);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(Integer numberInSeries, String title)
	{
		Episode epi = new Episode(this, numberInSeries, title);
		episodes.add(epi);
		return epi;
	}

	// Dated
	public Episode addEpisode(Temporal date)
	{
		Episode epi = new Episode(this, date);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(Temporal date, String title)
	{
		Episode epi = new Episode(this, date, title);
		episodes.add(epi);
		return epi;
	}

	// Seasoned
	public Episode addEpisode(Season season)
	{
		Episode epi = new Episode(this, season);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(Season season, Integer numberInSeason)
	{
		Episode epi = new Episode(this, season, numberInSeason);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(Season season, String title)
	{
		Episode epi = new Episode(this, season, title);
		episodes.add(epi);
		return epi;
	}

	public Episode addEpisode(Season season, Integer numberInSeason, String title)
	{
		Episode epi = new Episode(this, season, numberInSeason, title);
		episodes.add(epi);
		return epi;
	}

	// Seasons
	public List<Season> getSeasons()
	{
		return seasons;
	}

	public void setSeasons(Collection<Season> seasons)
	{
		this.seasons.clear();
		this.seasons.addAll(seasons);
	}

	public Season addSeason()
	{
		Season season = new Season(this);
		seasons.add(season);
		return season;
	}

	public Season addSeason(Integer seasonNumber)
	{
		Season season = new Season(this, seasonNumber);
		seasons.add(season);
		return season;
	}

	public Season addSeason(String seasonTitle)
	{
		Season season = new Season(this, seasonTitle);
		seasons.add(season);
		return season;
	}

	// Seasons and Episode
	public List<Episode> getEpisodesOf(Season season)
	{
		if (episodes.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Episode> episInSeason = ImmutableList.builder();
		for (Episode epi : episodes)
		{
			if (Objects.equals(season, epi.getSeason()))
			{
				episInSeason.add(epi);
			}
		}
		return episInSeason.build();
	}

	/**
	 * Returns an immutable map of all the seasons of this series mapped to their corresponding episodes, plus {@code null} mapped to the episodes
	 * which are not part of a season.<br/>
	 * The map contains a {@code null} key which value is a list of all the episodes with no season, this list may be empty but it is never
	 * {@code null}. The map may contain a mapping between a season and an empty list for seasons without episodes.<br/>
	 * The seasons (the keys) are in the same order as in the {@link #getSeasons() seasons list}. The episodes for each season are in the same order
	 * as in the {@link #getEpisodes() episodes list}.
	 * 
	 * @return an immutable map with the seasons as keys (including the {@code null} season) and their episodes as values
	 * @throws IllegalStateException
	 *             if the season of an episode in the {@link #getEpisodes() episodes list} is not contained in the {@link #getSeasons() seasons list}
	 */
	public Map<Season, List<Episode>> getSeasonsToEpisodesMap() throws IllegalStateException
	{
		// preserve insertion order -> use a LinkedHashMap
		Map<Season, List<Episode>> seasonsAndEpis = new LinkedHashMap<>(seasons.size());
		for (Season s : seasons)
		{
			seasonsAndEpis.put(s, new ArrayList<>());
		}
		// for the Episodes without a Season
		seasonsAndEpis.put(null, new ArrayList<>());
		for (Episode epi : episodes)
		{
			List<Episode> seasonEpiList = seasonsAndEpis.get(epi.getSeason());
			if (seasonEpiList == null)
			{
				throw new IllegalStateException("The seasons list of this series does not contain the season of the following episode: " + epi);
			}
			seasonEpiList.add(epi);
		}
		return Collections.unmodifiableMap(seasonsAndEpis);
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Series)
		{
			return StringUtils.equalsIgnoreCase(name, ((Series) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(3, 11).append(StringUtils.lowerCase(name, Locale.ENGLISH)).toHashCode();
	}

	@Override
	public int compareTo(Series o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Series.class)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("date", date)
				.add("finaleDate", finaleDate)
				.add("type", type)
				.add("state", state)
				.add("originalLanguages", Models.nullIfEmpty(originalLanguages))
				.add("countriesOfOrigin", Models.nullIfEmpty(countriesOfOrigin))
				.add("regularRunningTime", Models.nullIfZero(regularRunningTime))
				.add("genres", Models.nullIfEmpty(genres))
				.add("description", description)
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
				.add("attributes", Models.nullIfEmpty(attributes))
				.add("episodes.size", Models.nullIfZero(episodes.size()))
				.toString();
	}
}
