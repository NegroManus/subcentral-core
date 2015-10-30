package de.subcentral.core.metadata.subtitle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.MetadataBase;
import de.subcentral.core.metadata.Work;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.naming.NamingUtil;
import de.subcentral.core.util.SimplePropDescriptor;

public class Subtitle extends MetadataBase implements Work, Comparable<Subtitle>
{
	public static final SimplePropDescriptor	PROP_MEDIA						= new SimplePropDescriptor(Subtitle.class, PropNames.MEDIA);
	public static final SimplePropDescriptor	PROP_LANGUAGE					= new SimplePropDescriptor(Subtitle.class, PropNames.LANGUAGE);
	public static final SimplePropDescriptor	PROP_GROUP						= new SimplePropDescriptor(Subtitle.class, PropNames.GROUP);
	public static final SimplePropDescriptor	PROP_SOURCE						= new SimplePropDescriptor(Subtitle.class, PropNames.SOURCE);
	public static final SimplePropDescriptor	PROP_STATE						= new SimplePropDescriptor(Subtitle.class, PropNames.STATE);
	public static final SimplePropDescriptor	PROP_PRODUCTION_TYPE			= new SimplePropDescriptor(Subtitle.class, PropNames.PRODUCTION_TYPE);
	public static final SimplePropDescriptor	PROP_BASIS						= new SimplePropDescriptor(Subtitle.class, PropNames.BASIS);
	public static final SimplePropDescriptor	PROP_NFO						= new SimplePropDescriptor(Subtitle.class, PropNames.NFO);
	public static final SimplePropDescriptor	PROP_NFO_LINK					= new SimplePropDescriptor(Subtitle.class, PropNames.NFO_LINK);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(Subtitle.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_IDS						= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES					= new SimplePropDescriptor(SubtitleAdjustment.class, PropNames.ATTRIBUTES);

	public static final String					STATE_PLANNED					= "PLANNED";
	public static final String					STATE_TRANSCRIPT				= "TRANSCRIPT";
	public static final String					STATE_TIMINGS					= "TIMINGS";
	public static final String					STATE_TRANSLATION				= "TRANSLATION";
	public static final String					STATE_REVISION					= "REVISION";
	public static final String					STATE_RELEASED					= "RELEASED";

	/**
	 * If a transcript was the source of the subtitle.
	 */
	public static final String					PRODUCTION_TYPE_TRANSCRIPT		= "TRANSCRIPT";

	/**
	 * If the subtitles was created by hearing what is said.
	 */
	public static final String					PRODUCTION_TYPE_LISTENING		= "LISTENING";

	/**
	 * If the subtitle was ripped from a retail source (DVD, BluRay, CD, etc).
	 */
	public static final String					PRODUCTION_TYPE_RETAIL			= "RETAIL";

	/**
	 * If the subtitle is an modification (improvement or customization) of another subtitle.
	 */
	public static final String					PRODUCTION_TYPE_MODIFICATION	= "MODIFICATION";

	/**
	 * If the subtitle is a translation of another subtitle.
	 */
	public static final String					PRODUCTION_TYPE_TRANSLATION		= "TRANSLATION";

	/**
	 * If the subtitle was produced automatically by a machine. For example by speech-to-text or translation software.
	 */
	public static final String					PRODUCTION_TYPE_MACHINE			= "MACHINE";

	public static enum TranslationType
	{
		/**
		 * It is unknown whether the subtitle is an original revision or a translation.
		 */
		UNKNOWN,

		/**
		 * The subtitle's language is the original language. The language of this subtitle is equal to the primary original language of the subtitled media item (in French
		 * <code>"revision originale" (VO)</code>)
		 */
		ORIGINAL,

		/**
		 * The subtitle is a translation. This language of this subtitle differs from the primary original language of the subtitled media item.
		 */
		TRANSLATION
	}

	/**
	 * The making of a transcript.
	 */
	public static final String			CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";

	/**
	 * The syncing of a subtitle.
	 */
	public static final String			CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";

	/**
	 * The actual translation of a subtitle to another language.
	 */
	public static final String			CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";

	/**
	 * Internal revision before the release.
	 */
	public static final String			CONTRIBUTION_TYPE_REVISION		= "REVISION";

	private Media						media;
	private String						language;
	private Group						group;
	private String						source;
	private String						state;
	private String						productionType;
	private Subtitle					basis;
	private String						nfo;
	private String						nfoLink;
	// More than 4 contributions per subtitle is very rare
	private final List<Contribution>	contributions					= new ArrayList<>(4);

	public Subtitle()
	{

	}

	public Subtitle(Media media)
	{
		this.media = media;
	}

	public Subtitle(Media media, String language)
	{
		this.media = media;
		this.language = language;
	}

	public Subtitle(Media media, String language, Group group)
	{
		this.media = media;
		this.language = language;
		this.group = group;
	}

	public Subtitle(Media media, String language, String source)
	{
		this.media = media;
		this.language = language;
		this.source = source;
	}

	/**
	 * {@code null} if unknown.
	 * 
	 * @return the media (may be {@code null})
	 */
	public Media getMedia()
	{
		return media;
	}

	public void setMedia(Media media)
	{
		this.media = media;
	}

	/**
	 * {@code null} if unknown.
	 * 
	 * @return the language (may be {@code null})
	 */
	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * The group which released this subtitle. {@code null} if unknown or not released by a group.
	 * 
	 * @return the release group (may be {@code null})
	 * @see #getSource()
	 */
	public Group getGroup()
	{
		return group;
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	/**
	 * The source of this subtitle. Typically the site which released this Subtitle. {@code null} if unknown.
	 * 
	 * @return the source (may be {@code null})
	 */
	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	/**
	 * How this subtitles was produced. See the <code>PRODUCTION_TYPE_*</code> constants. {@code null} if unknown.
	 * 
	 * @return the production type (may be null)
	 */
	public String getProductionType()
	{
		return productionType;
	}

	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}

	/**
	 * The subtitle on which this subtitle was based on. Only for production types {@link #PRODUCTION_TYPE_IMPROVEMENT} and {@link #PRODUCTION_TYPE_TRANSLATION}.
	 * 
	 * @return the basis
	 */
	public Subtitle getBasis()
	{
		return basis;
	}

	public void setBasis(Subtitle basis) throws IllegalArgumentException
	{
		if (this == basis)
		{
			throw new IllegalArgumentException("cannot be based on itself");
		}
		this.basis = basis;
	}

	/**
	 * The subtitle's release information (content of the NFO file).
	 * 
	 * @return the NFO
	 */
	public String getNfo()
	{
		return nfo;
	}

	public void setNfo(String nfo)
	{
		this.nfo = nfo;
	}

	/**
	 * A link pointing to a file or a HTML page with the NFO of this subtitle.
	 * 
	 * @return the link to the NFO
	 * @see #getNfo()
	 */
	public String getNfoLink()
	{
		return nfoLink;
	}

	public void setNfoLink(String nfoLink)
	{
		this.nfoLink = nfoLink;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		this.contributions.clear();
		this.contributions.addAll(contributions);
	}

	// convenience / complex
	public TranslationType determineTranslationType()
	{
		if (media == null || language == null)
		{
			return TranslationType.UNKNOWN;
		}
		String primaryLangOfMedia = media.getPrimaryOriginalLanguage();
		if (primaryLangOfMedia == null)
		{
			return TranslationType.UNKNOWN;
		}
		return language.equals(primaryLangOfMedia) ? TranslationType.ORIGINAL : TranslationType.TRANSLATION;
	}

	public boolean isBasedOnOther()
	{
		return basis != null;
	}

	public List<Contribution> getAllContributions()
	{
		if (isBasedOnOther())
		{
			ImmutableList.Builder<Contribution> cList = ImmutableList.builder();
			cList.addAll(basis.getAllContributions());
			cList.addAll(contributions);
			return cList.build();
		}
		return contributions;
	}

	public ListMultimap<Subtitle, Contribution> getAllContributionsAsMap()
	{
		if (isBasedOnOther())
		{
			ImmutableListMultimap.Builder<Subtitle, Contribution> cMap = ImmutableListMultimap.builder();
			cMap.putAll(basis.getAllContributionsAsMap());
			cMap.putAll(this, contributions);
			return cMap.build();
		}
		return ImmutableListMultimap.<Subtitle, Contribution> builder().putAll(this, contributions).build();
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Subtitle)
		{
			Subtitle o = (Subtitle) obj;
			return Objects.equals(media, o.media) && StringUtils.equalsIgnoreCase(language, o.language) && Objects.equals(group, o.group) && StringUtils.equalsIgnoreCase(source, o.source);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(media).append(StringUtils.lowerCase(language, Locale.ENGLISH)).append(group).append(StringUtils.lowerCase(source, Locale.ENGLISH)).toHashCode();
	}

	@Override
	public int compareTo(Subtitle o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start()
				.compare(media, o.media, NamingUtil.DEFAULT_MEDIA_NAME_COMPARATOR)
				.compare(language, o.language, Settings.STRING_ORDERING)
				.compare(group, o.group, Settings.createDefaultOrdering())
				.compare(source, o.source, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Subtitle.class)
				.omitNullValues()
				.add("media", media)
				.add("language", language)
				.add("group", group)
				.add("source", source)
				.add("state", state)
				.add("productionType", productionType)
				.add("basis", basis)
				.add("nfo", nfo)
				.add("nfoLink", nfoLink)
				.add("contributions", BeanUtil.nullIfEmpty(contributions))
				.add("ids", BeanUtil.nullIfEmpty(ids))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
