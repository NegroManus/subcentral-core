package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.contribution.Contribution;
import de.subcentral.core.util.Settings;

public class Season implements AvMedia, AvMediaCollection<Episode>, Comparable<Season>
{
	private final Series	series;
	private int				number	= Media.UNNUMBERED;
	private String			title;
	private boolean			special;
	private String			description;
	private String			coverUrl;

	Season(Series series)
	{
		this.series = series;
	}

	public Series getSeries()
	{
		return series;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String getMediaType()
	{
		return Media.TYPE_COLLECTION;
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
	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String getCoverUrl()
	{
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl)
	{
		this.coverUrl = coverUrl;
	}

	// Convenience / Complex
	public boolean isNumbered()
	{
		return number != Media.UNNUMBERED;
	}

	public boolean isTitled()
	{
		return title != null;
	}

	// Episodes
	@Override
	public List<Episode> getMediaItems()
	{
		return getEpisodes();
	}

	public List<Episode> getEpisodes()
	{
		return series.getEpisodes(this);
	}

	public boolean containsEpisode(Episode episode)
	{
		return getEpisodes().contains(episode);
	}

	public Episode addEpisode()
	{
		return series.addEpisode(this);
	}

	public Episode addEpisode(Episode episode)
	{
		if (episode == null)
		{
			return null;
		}
		episode.setSeason(this);
		return episode;
	}

	public void addEpisodes(Collection<Episode> episodes)
	{
		for (Episode epi : episodes)
		{
			epi.setSeason(this);
		}
	}

	public boolean removeEpisode(Episode episode)
	{
		if (episode == null)
		{
			return false;
		}
		if (this.equals(episode.getSeason()))
		{
			episode.setSeason(null);
			return true;
		}
		return false;
	}

	public void removeAllEpisodes(Collection<Episode> episodes)
	{
		for (Episode epi : episodes)
		{
			if (this.equals(epi.getSeason()))
			{
				epi.setSeason(null);
			}
		}
	}

	public void removeAllEpisodes()
	{
		for (Episode epi : getEpisodes())
		{
			epi.setSeason(null);
		}
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
		if (Season.class != obj.getClass())
		{
			return false;
		}
		Season o = (Season) obj;
		return new EqualsBuilder().append(series, o.series).append(number, o.number).append(title, o.title).isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(5, 13).append(series).append(number).append(title).toHashCode();
	}

	@Override
	public int compareTo(Season o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start().compare(series, o.series).compare(number, o.number).compare(title, o.title, Settings.STRING_ORDERING).result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("series", series)
				.add("number", number)
				.add("title", title)
				.add("special", special)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("episodes.size", getEpisodes().size())
				.toString();
	}

	@Override
	public int getRunningTime()
	{
		int runningTime = 0;
		for (Episode e : getEpisodes())
		{
			runningTime += e.getRunningTime();
		}
		return runningTime;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Temporal getDate()
	{
		if (getEpisodes().isEmpty())
		{
			return null;
		}
		return getEpisodes().get(0).getDate();
	}

	@Override
	public Set<String> getGenres()
	{
		return series.getGenres();
	}

	@Override
	public String getOriginalLanguage()
	{
		return series.getOriginalLanguage();
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return series.getCountriesOfOrigin();
	}

	@Override
	public String getContentAdvisory()
	{
		return series.getContentAdvisory();
	}

	@Override
	public Set<String> getFurtherInformationUrls()
	{
		return series.getFurtherInformationUrls();
	}

	@Override
	public List<Contribution> getContributions()
	{
		return series.getContributions();
	}
}
