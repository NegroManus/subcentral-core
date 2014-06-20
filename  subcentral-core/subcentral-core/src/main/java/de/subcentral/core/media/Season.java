package de.subcentral.core.media;

import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.Settings;

public class Season extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Season>
{
	private final Series	series;
	private int				number	= Media.UNNUMBERED;
	private boolean			special;

	Season(Series series)
	{
		this.series = series;
	}

	public Series getSeries()
	{
		return series;
	}

	@Override
	public String getName()
	{
		return isNumbered() ? Integer.toString(number) : title;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public boolean isSpecial()
	{
		return special;
	}

	public void setSpecial(boolean special)
	{
		this.special = special;
	}

	/**
	 * @return The air date ({@link Episode#getDate()}) of the first episode of this season, <code>null</code> if this season has no episodes.
	 */
	@Override
	public Temporal getDate()
	{
		List<Episode> epis = getEpisodes();
		if (epis.isEmpty())
		{
			return null;
		}
		return epis.get(0).getDate();
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

	// Convenience / Complex
	public boolean isNumbered()
	{
		return number != Media.UNNUMBERED;
	}

	public boolean isTitled()
	{
		return title != null;
	}

	/**
	 * @return The air date ({@link Episode#getDate()}) of the last episode of this series, <code>null</code> if this series has no episodes.
	 */
	public Temporal getDateOfLastEpisode()
	{
		List<Episode> epis = getEpisodes();
		if (epis.isEmpty())
		{
			return null;
		}
		return epis.get(epis.size() - 1).getDate();
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
}
