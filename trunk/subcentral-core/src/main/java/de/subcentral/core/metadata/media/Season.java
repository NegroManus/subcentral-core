package de.subcentral.core.metadata.media;

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

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.util.SimplePropDescriptor;

public class Season extends AbstractMedia implements Comparable<Season>
{
	public static final SimplePropDescriptor	PROP_SERIES			= new SimplePropDescriptor(Season.class, PropNames.SERIES);
	public static final SimplePropDescriptor	PROP_NUMBER			= new SimplePropDescriptor(Season.class, PropNames.NUMBER);
	public static final SimplePropDescriptor	PROP_TITLE			= new SimplePropDescriptor(Season.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_SPECIAL		= new SimplePropDescriptor(Season.class, PropNames.SPECIAL);
	public static final SimplePropDescriptor	PROP_DATE			= new SimplePropDescriptor(Series.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_FINALE_DATE	= new SimplePropDescriptor(Series.class, PropNames.FINALE_DATE);
	public static final SimplePropDescriptor	PROP_DESCRIPTION	= new SimplePropDescriptor(Season.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_IMAGES			= new SimplePropDescriptor(Season.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_RATINGS		= new SimplePropDescriptor(Episode.class, PropNames.RATINGS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING	= new SimplePropDescriptor(Season.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO	= new SimplePropDescriptor(Season.class, PropNames.FURTHER_INFO);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES		= new SimplePropDescriptor(Season.class, PropNames.ATTRIBUTES);

	private Series								series;
	private Integer								number;
	private boolean								special;
	private Temporal							finaleDate;

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

	@Override
	public List<String> getAliasNames()
	{
		return ImmutableList.of();
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

	public boolean isSpecial()
	{
		return special;
	}

	public void setSpecial(boolean special)
	{
		this.special = special;
	}

	public Temporal getFinaleDate()
	{
		return finaleDate;
	}

	public void setFinaleDate(Temporal finaleDate)
	{
		BeanUtil.validateTemporalClass(finaleDate);
		this.finaleDate = finaleDate;
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
				.add("special", special)
				.add("date", date)
				.add("finaleDate", finaleDate)
				.add("description", description)
				.add("ratings", BeanUtil.nullIfEmpty(ratings))
				.add("contentRating", contentRating)
				.add("images", BeanUtil.nullIfEmpty(images))
				.add("furtherInfo", BeanUtil.nullIfEmpty(furtherInfo))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}