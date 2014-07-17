package de.subcentral.core.model.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.util.Settings;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class Subtitle implements Work, Comparable<Subtitle>
{
	public static final String						PROP_NAME_MEDIA_ITEM			= "mediaItem";
	public static final String						PROP_NAME_LANGUAGE				= "language";
	public static final String						PROP_NAME_CONTRIBUTIONS			= "contributions";
	public static final String						PROP_NAME_VERSION				= "version";
	public static final String						PROP_NAME_PRODUCTION_TYPE		= "productionType";
	public static final String						PROP_NAME_DATE					= "date";
	public static final String						PROP_NAME_DESCRIPTION			= "description";

	public static final SimplePropertyDescriptor	PROP_MEDIA_ITEM					= new SimplePropertyDescriptor(Subtitle.class,
																							PROP_NAME_MEDIA_ITEM);
	public static final SimplePropertyDescriptor	PROP_LANGUAGE					= new SimplePropertyDescriptor(Subtitle.class, PROP_NAME_LANGUAGE);
	public static final SimplePropertyDescriptor	PROP_CONTRIBUTIONS				= new SimplePropertyDescriptor(Subtitle.class,
																							PROP_NAME_CONTRIBUTIONS);
	public static final SimplePropertyDescriptor	PROP_VERSION					= new SimplePropertyDescriptor(Subtitle.class, PROP_NAME_VERSION);
	public static final SimplePropertyDescriptor	PROP_PRODUCTION_TYPE			= new SimplePropertyDescriptor(Subtitle.class,
																							PROP_NAME_PRODUCTION_TYPE);
	public static final SimplePropertyDescriptor	PROP_DATE						= new SimplePropertyDescriptor(Subtitle.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_DESCRIPTION				= new SimplePropertyDescriptor(Subtitle.class,
																							PROP_NAME_DESCRIPTION);

	public static final String						CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";
	public static final String						CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";
	public static final String						CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";
	public static final String						CONTRIBUTION_TYPE_REVISION		= "REVISION";

	/**
	 * If a transcript was the source of the subtitle.
	 */
	public static final String						PRODUCTION_TYPE_TRANSCRIPT		= "TRANSCRIPT";

	/**
	 * If the subtitles was created by hearing what is said.
	 */
	public static final String						PRODUCTION_TYPE_LISTENING		= "LISTENING";

	/**
	 * If the subtitle was ripped from a retail source (DVD, BluRay, CD, etc).
	 */
	public static final String						PRODUCTION_TYPE_RETAIL			= "RETAIL";

	/**
	 * If the subtitle is an improvement of another subtitle.
	 */
	public static final String						PRODUCTION_TYPE_IMPROVEMENT		= "IMPROVEMENT";

	/**
	 * If the subtitle is a translation of another subtitle.
	 */
	public static final String						PRODUCTION_TYPE_TRANSLATION		= "TRANSLATION";

	/**
	 * If the subtitle was produced automatically by a machine.
	 */
	public static final String						PRODUCTION_TYPE_MACHINE			= "MACHINE";

	private AvMediaItem								mediaItem;
	private String									language;
	private List<Contribution>						contributions					= new ArrayList<>();
	private int										version							= 1;
	private String									productionType;
	private Temporal								date;
	private String									description;
	private Subtitle								basedOn;

	public Subtitle()
	{

	}

	public Subtitle(AvMediaItem mediaItem)
	{
		this(mediaItem, null);
	}

	public Subtitle(AvMediaItem mediaItem, String language)
	{
		setMediaItem(mediaItem);
		setLanguage(language);
	}

	public AvMediaItem getMediaItem()
	{
		return mediaItem;
	}

	public void setMediaItem(AvMediaItem mediaItem)
	{
		this.mediaItem = mediaItem;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		Validate.notNull(contributions, "contributions cannot be null");
		this.contributions = contributions;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getProductionType()
	{
		return productionType;
	}

	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * 
	 * @return The subtitle on which this subtitle was based on (only for production types {@link #PRODUCTION_TYPE_IMPROVEMENT} and
	 *         {@link #PRODUCTION_TYPE_TRANSLATION}.
	 */
	public Subtitle getBasedOn()
	{
		return basedOn;
	}

	public void setBasedOn(Subtitle basedOn)
	{
		this.basedOn = basedOn;
	}

	// convenience / complex
	public boolean isTranslation()
	{
		if (mediaItem == null || language == null)
		{
			return false;
		}
		return !language.equals(mediaItem.getOriginalLanguage());
	}

	public boolean isBasedOn()
	{
		return basedOn != null;
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
		if (Subtitle.class != obj.getClass())
		{
			return false;
		}
		Subtitle o = (Subtitle) obj;
		return new EqualsBuilder().append(mediaItem, o.mediaItem)
				.append(language, o.language)
				.append(contributions, o.contributions)
				.append(version, o.version)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(mediaItem).append(language).append(contributions).append(version).toHashCode();
	}

	@Override
	public int compareTo(Subtitle o)
	{
		if (o == null)
		{
			return -1;
		}
		return ComparisonChain.start()
				.compare(mediaItem, o.mediaItem, Medias.MEDIA_NAME_COMPARATOR)
				.compare(language, o.language, Settings.STRING_ORDERING)
				.compare(date, o.date, Settings.TEMPORAL_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("mediaItem", mediaItem)
				.add("language", language)
				.add("date", date)
				.add("productionType", productionType)
				.add("description", description)
				.add("contributions", contributions)
				.toString();
	}
}
