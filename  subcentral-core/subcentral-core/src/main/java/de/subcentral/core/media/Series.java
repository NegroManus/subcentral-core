package de.subcentral.core.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.util.Settings;

public class Series implements Work, Comparable<Series>
{
	/**
	 * A type of series which episodes are organized in seasons. Typically, episodes belong to a season and are numbered in that season. Typical
	 * examples are the TV series "Breaking Bad", "Game of Thrones" and "Psych".
	 */
	public static final String	TYPE_SEASONED		= "SEASONED";

	/**
	 * A type of series which has a limited set of episodes and these episodes are therefore not organized in seasons. A typical example is the TV
	 * mini-series "Band of Brothers".
	 */
	public static final String	TYPE_MINI_SERIES	= "MINI_SERIES";

	/**
	 * A type of series which episodes usually have no numbers. Instead the main identifier is their air date. Typical examples are (daily) shows or
	 * sports events.
	 */
	public static final String	TYPE_DATED			= "DATED";

	/**
	 * If a series is "continuing", then there will be more episodes to come.
	 */
	public static final String	STATE_CONTINUING	= "CONTINUING";

	/**
	 * If a series has "ended", there will be no more episodes to come. Either because the series was cancelled or it simply is complete.
	 */
	public static final String	STATE_ENDED			= "ENDED";

	private String				name;
	private String				title;
	private String				type;
	private String				state;
	private String				originalLanguage;
	private Set<String>			countriesOfOrigin	= new HashSet<>(1);
	private int					runningTime;
	private Set<String>			genres;
	private String				description;
	private String				coverUrl;
	private String				contentRating;
	private List<Contribution>	contributions		= new ArrayList<>();
	private List<Season>		seasons				= new ArrayList<>();
	private List<Episode>		episodes			= new ArrayList<>();

	public Series()
	{}

	public Series(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
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

	public String getOriginalLanguage()
	{
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage)
	{
		this.originalLanguage = originalLanguage;
	}

	public Set<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(Set<String> countriesOfOrigin)
	{
		this.countriesOfOrigin = countriesOfOrigin;
	}

	public int getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(int runningTime)
	{
		this.runningTime = runningTime;
	}

	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres = genres;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getCoverUrl()
	{
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}

	public String getContentRating()
	{
		return contentRating;
	}

	public void setContentRating(String contentRating)
	{
		this.contentRating = contentRating;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions = contributions;
	}

	// Seasons
	public List<Season> getSeasons()
	{
		return seasons;
	}

	public boolean containsSeason(Season season)
	{
		return seasons.contains(season);
	}

	public Season addSeason()
	{
		Season s = new Season(this);
		seasons.add(s);
		return s;
	}

	public boolean removeSeason(Season season)
	{
		return seasons.remove(season);
	}

	// Episodes
	public List<Episode> getEpisodes()
	{
		return episodes;
	}

	public List<Episode> getEpisodes(Season season)
	{
		if (season == null || episodes.isEmpty())
		{
			return ImmutableList.of();
		}
		List<Episode> episInSeason = new ArrayList<>();
		for (Episode epi : episodes)
		{
			if (season.equals(epi.getSeason()))
			{
				episInSeason.add(epi);
			}
		}
		return episInSeason;
	}

	public boolean containsEpisode(Episode episode)
	{
		return episodes.contains(episode);
	}

	public Episode addEpisode()
	{
		return addEpisode(null);
	}

	public Episode addEpisode(Season season)
	{
		Episode e = new Episode(this, season);
		episodes.add(e);
		return e;
	}

	public boolean removeEpisode(Episode episode)
	{
		return episodes.remove(episode);
	}

	public void removeAllEpisodes(Collection<Episode> episodes)
	{
		this.episodes.removeAll(episodes);
	}

	public void removeAllEpisodes()
	{
		this.episodes.clear();
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
				.add("runningTime", runningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("contentRating", contentRating)
				.add("contributions", contributions)
				.add("seasons.size", seasons.size())
				.add("episodes.size", episodes.size())
				.toString();
	}
}
