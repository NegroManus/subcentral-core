package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;

import de.subcentral.core.PropNames;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.ValidationUtil;

public class Series extends NamedMediaBase implements Comparable<Series>
{
	private static final long					serialVersionUID			= -3817853387927606602L;

	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(Series.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_ALIAS_NAMES			= new SimplePropDescriptor(Series.class, PropNames.ALIAS_NAMES);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(Series.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_TYPE					= new SimplePropDescriptor(Series.class, PropNames.TYPE);
	public static final SimplePropDescriptor	PROP_STATE					= new SimplePropDescriptor(Series.class, PropNames.STATE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(Series.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_FINALE_DATE			= new SimplePropDescriptor(Series.class, PropNames.FINALE_DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES				= new SimplePropDescriptor(Series.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES				= new SimplePropDescriptor(Series.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_REGULAR_RUNNING_TIME	= new SimplePropDescriptor(Series.class, PropNames.REGULAR_RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(Series.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_NETWORKS				= new SimplePropDescriptor(Series.class, PropNames.NETWORKS);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(Series.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_RATINGS				= new SimplePropDescriptor(Series.class, PropNames.RATINGS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING			= new SimplePropDescriptor(Series.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_IMAGES					= new SimplePropDescriptor(Series.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS		= new SimplePropDescriptor(Series.class, PropNames.FURTHER_INFO_LINKS);
	public static final SimplePropDescriptor	PROP_IDS					= new SimplePropDescriptor(Series.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES				= new SimplePropDescriptor(Series.class, PropNames.ATTRIBUTES);
	public static final SimplePropDescriptor	PROP_EPISODES				= new SimplePropDescriptor(Series.class, PropNames.EPISODES);
	public static final SimplePropDescriptor	PROP_SEASONS				= new SimplePropDescriptor(Series.class, PropNames.SEASONS);

	/**
	 * A type of series which episodes are organized in seasons. Typically, episodes belong to a season and are numbered in that season. Typical examples are the TV series "Breaking Bad",
	 * "Game of Thrones" and "Psych".
	 */
	public static final String					TYPE_SEASONED				= "SEASONED";

	/**
	 * A type of series which has a limited set of episodes and these episodes are therefore not organized in seasons. A typical example is the TV mini-series "Band of Brothers".
	 */
	public static final String					TYPE_MINI_SERIES			= "MINI_SERIES";

	/**
	 * A type of series which episodes usually have no numbers. Instead the main identifier is their air date. Typical examples are (daily) shows or sports events.
	 */
	public static final String					TYPE_DATED					= "DATED";

	private String								type;
	private Temporal							finaleDate;
	private final List<String>					languages					= new ArrayList<>(1);
	private final List<String>					countries					= new ArrayList<>(1);
	private int									regularRunningTime			= 0;
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<String>					genres						= new HashSet<>(4);
	private final List<Network>					networks					= new ArrayList<>(1);
	// Episodes/Seasons
	private final List<Episode>					episodes					= new ArrayList<>(0);
	private final List<Season>					seasons						= new ArrayList<>(0);

	public Series()
	{
		// default constructor
	}

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

	/**
	 * Returns the date of the Series' finale. This is the date of the last / final Episode of this Series. The date of the premiere is stored in {@link #getDate()}.
	 * <p>
	 * <b>Note:</b> The premiere date and finale date may be stored redundantly if this Series contains all its Episodes in {@link #getEpisodes() the Episodes list}. Then the premiere date and finale
	 * date can be retrieved via {@link #getDateOfFirstEpisode()} and {@link #getDateOfLastEpisode()} respectively. But if this Series object does not contain all its Episodes (because they are
	 * unknown), then the dates can be stored explicitly in the {@link #getDate() date} and {@link #getFinaleDate() finaleDate} properties.
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
		this.finaleDate = ValidationUtil.validateTemporalClass(finaleDate);
	}

	@Override
	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> originalLanguages)
	{
		this.languages.clear();
		this.languages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountries()
	{
		return countries;
	}

	public void setCountries(List<String> countriesOfOrigin)
	{
		this.countries.clear();
		this.countries.addAll(countriesOfOrigin);
	}

	/**
	 * The regular running time of the episodes of this Series in minutes.
	 * 
	 * @return the regular running time in minutes, <code>0</code> if unknown
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
	public int getRunningTime()
	{
		return 0;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Collection<? extends String> genres)
	{
		this.genres.clear();
		this.genres.addAll(genres);
	}

	public List<Network> getNetworks()
	{
		return networks;
	}

	public void setNetworks(Collection<? extends Network> networks)
	{
		this.networks.clear();
		this.networks.addAll(networks);
	}

	// Convenience
	@Override
	public String getPrimaryOriginalLanguage()
	{
		return languages.isEmpty() ? null : languages.get(0);
	}

	@Override
	public String getPrimaryCountryOfOrigin()
	{
		return countries.isEmpty() ? null : countries.get(0);
	}

	// == newEpisode() methods ==
	// Generic
	public Episode newEpisode()
	{
		return new Episode(this);
	}

	public Episode newEpisode(String title)
	{
		return new Episode(this, title);
	}

	// Seasoned
	public Episode newEpisode(Season season)
	{
		return new Episode(this, season);
	}

	public Episode newEpisode(Season season, Integer numberInSeason)
	{
		return new Episode(this, season, numberInSeason);
	}

	public Episode newEpisode(Season season, String title)
	{
		return new Episode(this, season, title);
	}

	public Episode newEpisode(Season season, Integer numberInSeason, String title)
	{
		return new Episode(this, season, numberInSeason, title);
	}

	// Mini-series
	public Episode newEpisode(Integer numberInSeries)
	{
		return new Episode(this, numberInSeries);
	}

	public Episode newEpisode(Integer numberInSeries, String title)
	{
		return new Episode(this, numberInSeries, title);
	}

	// Dated
	public Episode newEpisode(Temporal date)
	{
		return new Episode(this, date);
	}

	public Episode newEpisode(Temporal date, String title)
	{
		return new Episode(this, date, title);
	}

	// Seasons
	public Season newSeason()
	{
		return new Season(this);
	}

	public Season newSeason(Integer number)
	{
		return new Season(this, number);
	}

	public Season newSeason(String title)
	{
		return new Season(this, title);
	}

	/*
	 * Episodes, Seasons
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

	public List<Season> getSeasons()
	{
		return seasons;
	}

	public void setSeasons(Collection<Season> seasons)
	{
		this.seasons.clear();
		this.seasons.addAll(seasons);
	}

	// Convenience
	public Episode addEpisode()
	{
		Episode epi = new Episode(this);
		episodes.add(epi);
		return epi;
	}

	public void addEpisode(Episode epi)
	{
		episodes.add(epi);
		epi.setSeries(this);
	}

	public Season addSeason()
	{
		Season season = new Season(this);
		seasons.add(season);
		return season;
	}

	public void addSeason(Season season)
	{
		seasons.add(season);
		season.setSeries(this);
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
			return Objects.equals(name, ((Series) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(Series.class, name);
	}

	@Override
	public int compareTo(Series o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Series.class)
				.omitNullValues()
				.add("name", name)
				.add("aliasNames", ObjectUtil.nullIfEmpty(aliasNames))
				.add("title", title)
				.add("type", type)
				.add("date", date)
				.add("finaleDate", finaleDate)
				.add("languages", ObjectUtil.nullIfEmpty(languages))
				.add("countries", ObjectUtil.nullIfEmpty(countries))
				.add("regularRunningTime", ObjectUtil.nullIfZero(regularRunningTime))
				.add("genres", ObjectUtil.nullIfEmpty(genres))
				.add("networks", ObjectUtil.nullIfEmpty(networks))
				.add("description", description)
				.add("ratings", ObjectUtil.nullIfEmpty(ratings))
				.add("contentRating", contentRating)
				.add("images", ObjectUtil.nullIfEmpty(images))
				.add("furtherInfoLinks", ObjectUtil.nullIfEmpty(furtherInfoLinks))
				.add("ids", ObjectUtil.nullIfEmpty(ids))
				.add("attributes", ObjectUtil.nullIfEmpty(attributes))
				.add("episodes.size()", ObjectUtil.nullIfZero(episodes.size()))
				.add("seasons.size()", ObjectUtil.nullIfZero(seasons.size()))
				.toString();
	}
}