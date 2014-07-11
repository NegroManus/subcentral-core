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

import de.subcentral.core.util.Settings;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class Season extends AbstractMedia implements AvMediaCollection<Episode>, Comparable<Season>
{
	public static final String						PROP_NAME_SERIES				= "series";
	public static final String						PROP_NAME_NUMBER				= "number";
	public static final String						PROP_NAME_SPECIAL				= "special";

	public static final SimplePropertyDescriptor	PROP_NAME						= new SimplePropertyDescriptor(Season.class, PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_SERIES						= new SimplePropertyDescriptor(Season.class, PROP_NAME_SERIES);
	public static final SimplePropertyDescriptor	PROP_NUMBER						= new SimplePropertyDescriptor(Season.class, PROP_NAME_NUMBER);
	public static final SimplePropertyDescriptor	PROP_TITLE						= new SimplePropertyDescriptor(Season.class, PROP_NAME_TITLE);
	public static final SimplePropertyDescriptor	PROP_SPECIAL					= new SimplePropertyDescriptor(Season.class, PROP_NAME_SPECIAL);
	public static final SimplePropertyDescriptor	PROP_MEDIA_TYPE					= new SimplePropertyDescriptor(Season.class, PROP_NAME_MEDIA_TYPE);
	public static final SimplePropertyDescriptor	PROP_DATE						= new SimplePropertyDescriptor(Season.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_ORIGINAL_LANGUAGE			= new SimplePropertyDescriptor(Season.class,
																							PROP_NAME_ORIGINAL_LANGUAGE);
	public static final SimplePropertyDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropertyDescriptor(Season.class,
																							PROP_NAME_COUNTRIES_OF_ORIGIN);
	public static final SimplePropertyDescriptor	PROP_GENRES						= new SimplePropertyDescriptor(Season.class, PROP_NAME_GENRES);
	public static final SimplePropertyDescriptor	PROP_DESCRIPTION				= new SimplePropertyDescriptor(Season.class,
																							PROP_NAME_DESCRIPTION);
	public static final SimplePropertyDescriptor	PROP_COVER_URL					= new SimplePropertyDescriptor(Season.class, PROP_NAME_COVER_URL);
	public static final SimplePropertyDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropertyDescriptor(Season.class,
																							PROP_NAME_CONTENT_ADVISORY);
	public static final SimplePropertyDescriptor	PROP_FURHTER_INFORMATION_URLS	= new SimplePropertyDescriptor(Season.class,
																							PROP_NAME_FURHTER_INFORMATION_URLS);

	private Series									series;
	private Integer									number;
	private boolean									special;

	public Season()
	{

	}

	public Season(Series series)
	{
		setSeries(series);
	}

	public Season(Series series, int number)
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
		return isNumbered() ? Integer.toString(number) : title;
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
		if (epis.isEmpty())
		{
			return null;
		}
		return epis.get(0).getDate();
	}

	@Override
	public Set<String> getGenres()
	{
		return series == null ? ImmutableSet.of() : series.getGenres();
	}

	@Override
	public String getOriginalLanguage()
	{
		return series == null ? null : series.getOriginalLanguage();
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return series == null ? ImmutableSet.of() : series.getCountriesOfOrigin();
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
		return series == null ? ImmutableList.of() : series.getEpisodes(this);
	}

	public Episode newEpisode()
	{
		return new Episode(series, this);
	}

	public Episode newEpisode(int episodeNumber)
	{
		return new Episode(series, this);
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
		return ComparisonChain.start()
				.compare(series, o.series, Settings.createDefaultOrdering())
				.compare(number, o.number, Settings.createDefaultOrdering())
				.compare(title, o.title, Settings.STRING_ORDERING)
				.result();
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
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInformationUrls", furtherInformationUrls)
				.add("episodes.size", getEpisodes().size())
				.toString();
	}
}
