package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.SimplePropDescriptor;

public class Season extends AbstractMedia implements Comparable<Season>
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
	private Temporal							finaleDate;
	private boolean								special;

	public Season()
	{

	}

	public Season(Series series)
	{
		this.series = series;
	}

	public Season(Series series, Integer number)
	{
		this.series = series;
		this.number = number;
	}

	public Season(Series series, String title)
	{
		this.series = series;
		this.title = title;
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
		return NamingStandards.getDefaultSeasonNamer().name(this);
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
		Models.validateTemporalClass(finaleDate);
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

	// Episodes
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
		if (obj != null && getClass().equals(obj.getClass()))
		{
			Season o = (Season) obj;
			return new EqualsBuilder().append(series, o.series)
					.append(number, o.number)
					.append(StringUtils.equalsIgnoreCase(title, o.title), true)
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(5, 13).append(series).append(number).append(StringUtils.lowerCase(title)).toHashCode();
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
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
				.add("attributes", Models.nullIfEmpty(attributes))
				.toString();
	}
}
