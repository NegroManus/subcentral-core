package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.Prop;
import de.subcentral.core.util.Settings;
import de.subcentral.core.util.SimplePropDescriptor;

public class Series extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Series>
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(Series.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(Series.class, Prop.TITLE);
	public static final SimplePropDescriptor	PROP_TYPE					= new SimplePropDescriptor(Series.class, Prop.TYPE);
	public static final SimplePropDescriptor	PROP_STATE					= new SimplePropDescriptor(Series.class, Prop.STATE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(Series.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES		= new SimplePropDescriptor(Series.class, Prop.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN	= new SimplePropDescriptor(Series.class, Prop.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(Series.class, Prop.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(Series.class, Prop.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS				= new SimplePropDescriptor(Series.class, Prop.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY		= new SimplePropDescriptor(Series.class, Prop.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(Series.class, Prop.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FINFO_URLS				= new SimplePropDescriptor(Series.class, Prop.FURTHER_INFO_URLS);
	public static final SimplePropDescriptor	PROP_SEASONS				= new SimplePropDescriptor(Series.class, Prop.SEASONS);
	public static final SimplePropDescriptor	PROP_EPISODES				= new SimplePropDescriptor(Series.class, Prop.EPISODES);

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
	private String								type;
	private String								state;
	private List<String>						originalLanguages			= new ArrayList<>(1);
	private Set<String>							countriesOfOrigin			= new HashSet<>(1);
	private int									regularRunningTime;
	private Set<String>							genres						= new HashSet<>(4);
	private List<Season>						seasons						= new ArrayList<>(0);
	private List<Episode>						episodes					= new ArrayList<>(0);

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
	 * @return The air date ({@link Episode#getDate()}) of the first episode of this series, <code>null</code> if this series has no episodes.
	 */
	@Override
	public Temporal getDate()
	{
		if (episodes.isEmpty())
		{
			return null;
		}
		return episodes.get(0).getDate();
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
		this.originalLanguages = originalLanguages;
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(Set<String> countriesOfOrigin)
	{
		Validate.notNull(countriesOfOrigin, "countriesOfOrigin cannot be null");
		this.countriesOfOrigin = countriesOfOrigin;
	}

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
		Validate.notNull(genres, "genres cannot be null");
		this.genres = genres;
	}

	// Convenience
	public String getPrimaryOriginalLanguage()
	{
		return !originalLanguages.isEmpty() ? originalLanguages.get(0) : null;
	}

	/**
	 * @return The air date ({@link Episode#getDate()}) of the last episode of this series, <code>null</code> if this series has no episodes.
	 */
	public Temporal getDateOfLastEpisode()
	{
		if (episodes.isEmpty())
		{
			return null;
		}
		return episodes.get(episodes.size() - 1).getDate();
	}

	// Seasons
	public List<Season> getSeasons()
	{
		return seasons;
	}

	/**
	 * 
	 * @param seasons
	 *            This series' seasons. Only filled if the series is the information root.
	 */
	public void setSeasons(List<Season> seasons)
	{
		Validate.notNull("seasons list cannot be null");
		this.seasons = seasons;
	}

	public Season newSeason()
	{
		return new Season(this);
	}

	public Season newSeason(int seasonNumber)
	{
		return new Season(this, seasonNumber);
	}

	public Season newSeason(String seasonTitle)
	{
		return new Season(this, seasonTitle);
	}

	// Episodes
	@Override
	public List<Episode> getMediaItems()
	{
		return getEpisodes();
	}

	public List<Episode> getEpisodes()
	{
		return episodes;
	}

	/**
	 * 
	 * @param episodes
	 *            This series' episodes. Only filled if the series is the information root.
	 */
	public void setEpisodes(List<Episode> episodes)
	{
		Validate.notNull("episodes list cannot be null");
		this.episodes = episodes;
	}

	public Episode newEpisode()
	{
		return newEpisode(null);
	}

	public Episode newEpisode(Season season)
	{
		return new Episode(this, season);
	}

	public List<Episode> getEpisodes(Season season)
	{
		if (season == null || episodes.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Episode> episInSeason = ImmutableList.builder();
		for (Episode epi : episodes)
		{
			if (season.equals(epi.getSeason()))
			{
				episInSeason.add(epi);
			}
		}
		return episInSeason.build();
	}

	// Object methods
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
		if (Series.class != obj.getClass())
		{
			return false;
		}
		Series o = (Series) obj;
		return Objects.equal(name, o.name);
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(3, 11).append(name).toHashCode();
	}

	@Override
	public int compareTo(Series o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("type", type)
				.add("state", state)
				.add("originalLanguages", originalLanguages)
				.add("countriesOfOrigin", countriesOfOrigin)
				.add("regularRunningTime", regularRunningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrls", coverUrls)
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInfoUrls", furtherInfoUrls)
				.add("seasons.size", seasons.size())
				.add("episodes.size", episodes.size())
				.toString();
	}
}
