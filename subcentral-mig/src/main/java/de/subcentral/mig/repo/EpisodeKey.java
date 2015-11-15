package de.subcentral.mig.repo;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.naming.NamingDefaults;

public class EpisodeKey
{
	private final SeasonKey	season;
	private final Integer	numberInSeason;
	private final String	title;

	public EpisodeKey(Episode epi)
	{
		this(new SeasonKey(epi.getSeason()), epi.getNumberInSeason(), epi.getTitle());
	}

	public EpisodeKey(SeasonKey season, Integer numberInSeason, String title)
	{
		this.season = season;
		this.numberInSeason = numberInSeason;
		this.title = NamingDefaults.getDefaultNormalizingFormatter().apply(title);
	}

	public SeasonKey getSeason()
	{
		return season;
	}

	public Integer getNumberInSeason()
	{
		return numberInSeason;
	}

	public String getTitle()
	{
		return title;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof EpisodeKey)
		{
			EpisodeKey other = (EpisodeKey) obj;
			return this.season.equals(other.season) && Objects.equal(numberInSeason, other.numberInSeason) && Objects.equal(title, other.title);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 89331).append(season).append(numberInSeason).append(title).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SeriesKey.class).add("season", season).add("numberInSeason", numberInSeason).add("title", title).toString();
	}
}
