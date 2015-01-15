package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.Settings;
import de.subcentral.core.model.ModelUtils;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.util.SimplePropDescriptor;

public class Season extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Season>
{
	public static final SimplePropDescriptor	PROP_SERIES				= new SimplePropDescriptor(Season.class, PropNames.SERIES);
	public static final SimplePropDescriptor	PROP_NUMBER				= new SimplePropDescriptor(Season.class, PropNames.NUMBER);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(Season.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_SPECIAL			= new SimplePropDescriptor(Season.class, PropNames.SPECIAL);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(Season.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_LINKS		= new SimplePropDescriptor(Season.class, PropNames.COVER_LINKS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(Season.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS		= new SimplePropDescriptor(Season.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(Season.class, PropNames.FURTHER_INFO_LINKS);

	private Series								series;
	private Integer								number;
	private Temporal							finaleDate;
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

	@Override
	public String getName()
	{
		return NamingDefaults.getDefaultSeasonNamer().name(this);
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
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_SEASON;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	public Integer getNumber()
	{
		return number;
	}

	public void setNumber(Integer number)
	{
		this.number = number;
	}

	public Temporal getFinaleDate()
	{
		return finaleDate;
	}

	public void setFinaleDate(Temporal finaleDate)
	{
		ModelUtils.validateTemporalClass(finaleDate);
		this.finaleDate = finaleDate;
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
	public boolean isPartOfSeries()
	{
		return series != null;
	}

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
		return series != null ? series.getEpisodesOf(this) : ImmutableList.of();
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

	public Episode addEpisode()
	{
		return series.addEpisode(this);
	}

	public Episode addEpisode(Integer numberInSeason)
	{
		return series.addEpisode(this, numberInSeason);
	}

	public Episode addEpisode(Integer numberInSeason, String title)
	{
		return series.addEpisode(this, numberInSeason, title);
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Season)
		{
			Season o = (Season) obj;
			return Objects.equals(series, o.series) && Objects.equals(number, o.number) && StringUtils.equalsIgnoreCase(title, o.title);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(5, 13).append(series).append(number).append(StringUtils.lowerCase(title, Locale.ENGLISH)).toHashCode();
	}

	@Override
	public int compareTo(Season o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
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
		return MoreObjects.toStringHelper(Season.class)
				.omitNullValues()
				.add("series", series)
				.add("number", number)
				.add("title", title)
				.add("date", date)
				.add("finaleDate", finaleDate)
				.add("special", special)
				.add("description", description)
				.add("coverLinks", ModelUtils.nullIfEmpty(coverLinks))
				.add("contentRating", contentRating)
				.add("contributions", ModelUtils.nullIfEmpty(contributions))
				.add("furtherInfoLinks", ModelUtils.nullIfEmpty(furtherInfoLinks))
				.add("attributes", ModelUtils.nullIfEmpty(attributes))
				.toString();
	}
}
