package de.subcentral.core.metadata.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.util.SimplePropDescriptor;

public class Series extends AbstractNamedMedia implements Comparable<Series>
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(Series.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(Series.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_TYPE					= new SimplePropDescriptor(Series.class, PropNames.TYPE);
	public static final SimplePropDescriptor	PROP_STATE					= new SimplePropDescriptor(Series.class, PropNames.STATE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(Series.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_FINALE_DATE			= new SimplePropDescriptor(Series.class, PropNames.FINALE_DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES				= new SimplePropDescriptor(Series.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES				= new SimplePropDescriptor(Series.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_REGULAR_RUNNING_TIME	= new SimplePropDescriptor(Series.class, PropNames.REGULAR_RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(Series.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(Series.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_RATINGS				= new SimplePropDescriptor(Episode.class, PropNames.RATINGS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING			= new SimplePropDescriptor(Series.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_IMAGES					= new SimplePropDescriptor(Series.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO			= new SimplePropDescriptor(Series.class, PropNames.FURTHER_INFO);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES				= new SimplePropDescriptor(Series.class, PropNames.ATTRIBUTES);

	/**
	 * A type of series which episodes are organized in seasons. Typically, episodes belong to a season and are numbered in that season. Typical
	 * examples are the TV series "Breaking Bad", "Game of Thrones" and "Psych".
	 */
	public static final String					TYPE_SEASONED				= "SEASONED";

	/**
	 * A type of series which has a limited set of episodes and these episodes are therefore not organized in seasons. A typical example is the TV
	 * mini-series "Band of Brothers".
	 */
	public static final String					TYPE_MINI_SERIES			= "MINI_SERIES";

	/**
	 * A type of series which episodes usually have no numbers. Instead the main identifier is their air date. Typical examples are (daily) shows or
	 * sports events.
	 */
	public static final String					TYPE_DATED					= "DATED";

	private String								type;
	private Temporal							finaleDate;
	private final List<String>					languages					= new ArrayList<>(1);
	private final List<String>					countries					= new ArrayList<>(1);
	private int									regularRunningTime			= 0;
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<String>					genres						= new HashSet<>(4);

	public Series()
	{}

	public Series(String name)
	{
		this(name, null);
	}

	public Series(String name, String title)
	{
		setName(name);
		setTitle(title);
	}

	@Override
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_SERIES;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	/**
	 * Returns the date of the Series' finale. This is the date of the last / final Episode of this Series. The date of the premiere is stored in
	 * {@link #getDate()}.
	 * <p>
	 * <b>Note:</b> The premiere date and finale date may be stored redundantly if this Series contains all its Episodes in {@link #getEpisodes() the
	 * Episodes list}. Then the premiere date and finale date can be retrieved via {@link #getDateOfFirstEpisode()} and
	 * {@link #getDateOfLastEpisode()} respectively. But if this Series object does not contain all its Episodes (because they are unknown), then the
	 * dates can be stored explicitly in the {@link #getDate() date} and {@link #getFinaleDate() finaleDate} properties.
	 * </p>
	 * 
	 * @see #getDate()
	 * @see #getDateOfFirstEpisode()
	 * @see #getDateOfLastEpisode()
	 * 
	 * @return the date of the finale
	 */
	public Temporal getFinaleDate()
	{
		return finaleDate;
	}

	public void setFinaleDate(Temporal finaleDate)
	{
		this.finaleDate = BeanUtil.validateTemporalClass(finaleDate);
	}

	@Override
	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> originalLanguages)
	{
		this.languages.clear();
		this.languages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountries()
	{
		return countries;
	}

	public void setCountries(List<String> countriesOfOrigin)
	{
		this.countries.clear();
		this.countries.addAll(countriesOfOrigin);
	}

	/**
	 * The regular running time in milliseconds of the episodes of this Series.
	 * 
	 * @return the regular running time in milliseconds, <code>0</code> if unknown
	 */
	public int getRegularRunningTime()
	{
		return regularRunningTime;
	}

	public void setRegularRunningTime(int regularRunningTime)
	{
		this.regularRunningTime = regularRunningTime;
	}

	@Override
	public int getRunningTime()
	{
		return 0;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres.clear();
		this.genres.addAll(genres);
	}

	// Convenience
	public String getPrimaryOriginalLanguage()
	{
		return languages.isEmpty() ? null : languages.get(0);
	}

	@Override
	public String getPrimaryCountryOfOrigin()
	{
		return countries.isEmpty() ? null : countries.get(0);
	}

	// == newEpisode() methods ==
	// Generic
	public Episode newEpisode()
	{
		return new Episode(this);
	}

	public Episode newEpisode(String title)
	{
		return new Episode(this, title);
	}

	// Seasoned
	public Episode newEpisode(Season season)
	{
		return new Episode(this, season);
	}

	public Episode newEpisode(Season season, Integer numberInSeason)
	{
		return new Episode(this, season, numberInSeason);
	}

	public Episode newEpisode(Season season, String title)
	{
		return new Episode(this, season, title);
	}

	public Episode newEpisode(Season season, Integer numberInSeason, String title)
	{
		return new Episode(this, season, numberInSeason, title);
	}

	// Mini-series
	public Episode newEpisode(Integer numberInSeries)
	{
		return new Episode(this, numberInSeries);
	}

	public Episode newEpisode(Integer numberInSeries, String title)
	{
		return new Episode(this, numberInSeries, title);
	}

	// Dated
	public Episode newEpisode(Temporal date)
	{
		return new Episode(this, date);
	}

	public Episode newEpisode(Temporal date, String title)
	{
		return new Episode(this, date, title);
	}

	// Seasons
	public Season newSeason()
	{
		return new Season(this);
	}

	public Season newSeason(Integer number)
	{
		return new Season(this, number);
	}

	public Season newSeason(String title)
	{
		return new Season(this, title);
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Series)
		{
			return StringUtils.equalsIgnoreCase(name, ((Series) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(3, 11).append(StringUtils.lowerCase(name, Locale.ENGLISH)).toHashCode();
	}

	@Override
	public int compareTo(Series o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Series.class)
				.omitNullValues()
				.add("name", name)
				.add("aliasNames", BeanUtil.nullIfEmpty(aliasNames))
				.add("title", title)
				.add("type", type)
				.add("date", date)
				.add("finaleDate", finaleDate)
				.add("languages", BeanUtil.nullIfEmpty(languages))
				.add("countries", BeanUtil.nullIfEmpty(countries))
				.add("regularRunningTime", BeanUtil.nullIfZero(regularRunningTime))
				.add("genres", BeanUtil.nullIfEmpty(genres))
				.add("description", description)
				.add("ratings", BeanUtil.nullIfEmpty(ratings))
				.add("contentRating", contentRating)
				.add("images", BeanUtil.nullIfEmpty(images))
				.add("furtherInfo", BeanUtil.nullIfEmpty(furtherInfo))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
