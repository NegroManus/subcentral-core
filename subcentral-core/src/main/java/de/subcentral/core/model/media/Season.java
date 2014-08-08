package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.SimplePropDescriptor;

public class Season extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Season>
{
	public static final SimplePropDescriptor	PROP_SERIES				= new SimplePropDescriptor(Season.class, PropNames.SERIES);
	public static final SimplePropDescriptor	PROP_NUMBER				= new SimplePropDescriptor(Season.class, PropNames.NUMBER);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(Season.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_SPECIAL			= new SimplePropDescriptor(Season.class, PropNames.SPECIAL);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(Season.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS			= new SimplePropDescriptor(Season.class, PropNames.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY	= new SimplePropDescriptor(Season.class, PropNames.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS		= new SimplePropDescriptor(Season.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_URLS	= new SimplePropDescriptor(Season.class, PropNames.FURTHER_INFO_URLS);

	private Series								series;
	private Integer								number;
	private boolean								special;

	public Season()
	{

	}

	public Season(Series series)
	{
		setSeries(series);
	}

	public Season(Series series, Integer number)
	{
		setSeries(series);
		setNumber(number);
	}

	public Season(Series series, String title)
	{
		setSeries(series);
		setTitle(title);
	}

	public Series getSeries()
	{
		return series;
	}

	public void setSeries(Series series)
	{
		this.series = series;
	}

	@Override
	public String getName()
	{
		return NamingStandards.SEASON_NAMER.name(this);
	}

	@Override
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_SEASON;
	}

	public Integer getNumber()
	{
		return number;
	}

	public void setNumber(Integer number)
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
		return epis.isEmpty() ? null : epis.get(0).getDate();
	}

	@Override
	public Set<String> getGenres()
	{
		return series != null ? series.getGenres() : ImmutableSet.of();
	}

	@Override
	public List<String> getOriginalLanguages()
	{
		return series != null ? series.getOriginalLanguages() : ImmutableList.of();
	}

	@Override
	public List<String> getCountriesOfOrigin()
	{
		return series != null ? series.getCountriesOfOrigin() : ImmutableList.of();
	}

	// Convenience / Complex
	public boolean isNumbered()
	{
		return number != null;
	}

	/**
	 * @return The air date ({@link Episode#getDate()}) of the last episode of this series, <code>null</code> if this series has no episodes.
	 */
	public Temporal getDateOfLastEpisode()
	{
		List<Episode> epis = getEpisodes();
		return epis.isEmpty() ? null : epis.get(epis.size() - 1).getDate();
	}

	// Episodes
	@Override
	public List<Episode> getMediaItems()
	{
		return getEpisodes();
	}

	public List<Episode> getEpisodes()
	{
		return series != null ? series.getEpisodes(this) : ImmutableList.of();
	}

	public Episode newEpisode()
	{
		return new Episode(series, this);
	}

	public Episode newEpisode(Integer numberInSeason)
	{
		return new Episode(series, this, numberInSeason);
	}

	public Episode newEpisode(Integer numberInSeason, String title)
	{
		return new Episode(series, this, numberInSeason, title);
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && Season.class.equals(obj.getClass()))
		{
			Season o = (Season) obj;
			return new EqualsBuilder().append(series, o.series).append(number, o.number).append(title, o.title).isEquals();
		}
		return false;
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
		return ComparisonChain.start()
				.compare(series, o.series, Settings.createDefaultOrdering())
				.compare(number, o.number, Settings.createDefaultOrdering())
				.compare(title, o.title, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Season.class)
				.omitNullValues()
				.add("series", series)
				.add("number", number)
				.add("title", title)
				.add("date", date)
				.add("special", special)
				.add("description", description)
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
				.add("episodes.size", getEpisodes().size())
				.toString();
	}
}
