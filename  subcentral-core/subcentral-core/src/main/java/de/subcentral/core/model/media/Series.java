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

import de.subcentral.core.util.Settings;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class Series extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Series>
{
	public static final String						PROP_NAME_TYPE					= "type";
	public static final String						PROP_NAME_STATE					= "state";
	public static final String						PROP_NAME_SEASONS				= "seasons";
	public static final String						PROP_NAME_EPISODES				= "episodes";

	public static final SimplePropertyDescriptor	PROP_NAME						= new SimplePropertyDescriptor(Series.class, PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_TITLE						= new SimplePropertyDescriptor(Series.class, PROP_NAME_TITLE);
	public static final SimplePropertyDescriptor	PROP_MEDIA_TYPE					= new SimplePropertyDescriptor(Series.class, PROP_NAME_MEDIA_TYPE);
	public static final SimplePropertyDescriptor	PROP_TYPE						= new SimplePropertyDescriptor(Series.class, PROP_NAME_TYPE);
	public static final SimplePropertyDescriptor	PROP_STATE						= new SimplePropertyDescriptor(Series.class, PROP_NAME_STATE);
	public static final SimplePropertyDescriptor	PROP_DATE						= new SimplePropertyDescriptor(Series.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_ORIGINAL_LANGUAGE			= new SimplePropertyDescriptor(Series.class,
																							PROP_NAME_ORIGINAL_LANGUAGE);
	public static final SimplePropertyDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropertyDescriptor(Series.class,
																							PROP_NAME_COUNTRIES_OF_ORIGIN);
	public static final SimplePropertyDescriptor	PROP_GENRES						= new SimplePropertyDescriptor(Series.class, PROP_NAME_GENRES);
	public static final SimplePropertyDescriptor	PROP_DESCRIPTION				= new SimplePropertyDescriptor(Series.class,
																							PROP_NAME_DESCRIPTION);
	public static final SimplePropertyDescriptor	PROP_COVER_URL					= new SimplePropertyDescriptor(Series.class, PROP_NAME_COVER_URL);
	public static final SimplePropertyDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropertyDescriptor(Series.class,
																							PROP_NAME_CONTENT_ADVISORY);
	public static final SimplePropertyDescriptor	PROP_FURHTER_INFORMATION_URLS	= new SimplePropertyDescriptor(Series.class,
																							PROP_NAME_FURHTER_INFORMATION_URLS);
	public static final SimplePropertyDescriptor	PROP_SEASONS					= new SimplePropertyDescriptor(Series.class, PROP_NAME_SEASONS);
	public static final SimplePropertyDescriptor	PROP_EPISODES					= new SimplePropertyDescriptor(Series.class, PROP_NAME_EPISODES);

	/**
	 * A type of series which episodes are organized in seasons. Typically, episodes belong to a season and are numbered in that season. Typical
	 * examples are the TV series "Breaking Bad", "Game of Thrones" and "Psych".
	 */
	public static final String						TYPE_SEASONED					= "SEASONED";

	/**
	 * A type of series which has a limited set of episodes and these episodes are therefore not organized in seasons. A typical example is the TV
	 * mini-series "Band of Brothers".
	 */
	public static final String						TYPE_MINI_SERIES				= "MINI_SERIES";

	/**
	 * A type of series which episodes usually have no numbers. Instead the main identifier is their air date. Typical examples are (daily) shows or
	 * sports events.
	 */
	public static final String						TYPE_DATED						= "DATED";

	/**
	 * If a series is "continuing", then there will be more episodes to come.
	 */
	public static final String						STATE_CONTINUING				= "CONTINUING";

	/**
	 * If a series has "ended", there will be no more episodes to come. Either because the series was cancelled or it simply is complete.
	 */
	public static final String						STATE_ENDED						= "ENDED";

	private String									name;
	private String									type;
	private String									state;
	private String									originalLanguage;
	private Set<String>								countriesOfOrigin				= new HashSet<>(1);
	private int										regularRunningTime;
	private Set<String>								genres							= new HashSet<>(4);
	private List<Season>							seasons							= new ArrayList<>();
	private List<Episode>							episodes						= new ArrayList<>();

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
	public String getOriginalLanguage()
	{
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage)
	{
		this.originalLanguage = originalLanguage;
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
				.add("originalLanguage", originalLanguage)
				.add("countriesOfOrigin", countriesOfOrigin)
				.add("regularRunningTime", regularRunningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInformationUrls", furtherInformationUrls)
				.add("seasons.size", seasons.size())
				.add("episodes.size", episodes.size())
				.toString();
	}
}
