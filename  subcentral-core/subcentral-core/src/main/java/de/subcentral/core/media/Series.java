package de.subcentral.core.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.contribution.Work;
import de.subcentral.core.naming.Nameable;

public class Series implements Nameable, Work, Comparable<Series>
{
	public static final String	TYPE_SEASONED_SERIES	= "SEASONED_SERIES";
	public static final String	TYPE_MINI_SERIES		= "MINI_SERIES";
	public static final String	TYPE_DATED_SERIES		= "DATED_SERIES";

	public static final String	STATE_CONTINUING		= "CONTINUING";
	public static final String	STATE_ENDED				= "ENDED";

	private String				name;
	private String				title;
	private String				type;
	private String				state;
	private String				originalLanguage;
	private Set<String>			countriesOfOrigin		= new HashSet<>(1);
	private int					runningTime;
	private Set<String>			genres;
	private String				description;
	private String				coverUrl;
	private List<Contribution>	contributions			= new ArrayList<>();
	private List<Season>		seasons					= new ArrayList<>();
	private List<Episode>		episodes				= new ArrayList<>();

	public Series()
	{}

	public Series(String name)
	{
		this.name = name;
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

	@Override
	public String computeName()
	{
		return name;
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

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions = contributions;
	}

	public List<Season> getSeasons()
	{
		return Collections.unmodifiableList(seasons);
	}

	public Season getSeason(String name)
	{
		if (name == null)
		{
			return null;
		}
		for (Season s : seasons)
		{
			if (name.equals(s.getNameOrCompute()))
			{
				return s;
			}
		}
		return null;
	}

	public Season addSeason()
	{
		Season s = new Season(this);
		seasons.add(s);
		return s;
	}

	public boolean containsSeason(Season season)
	{
		return seasons.contains(season);
	}

	public boolean removeSeason(Season season)
	{
		return seasons.remove(season);
	}

	public List<Episode> getEpisodes()
	{
		return Collections.unmodifiableList(episodes);
	}

	public Episode getEpisode(String name)
	{
		if (name == null)
		{
			return null;
		}
		for (Episode e : episodes)
		{
			if (name.equals(e.getNameOrCompute()))
			{
				return e;
			}
		}
		return null;
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
		return new EqualsBuilder().append(name, o.name).isEquals();
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
			return 1;
		}
		return new CompareToBuilder().append(name, o.name).toComparison();
	}
}