package de.subcentral.core.model.subtitle;

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

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.AvMedia;
import de.subcentral.core.model.media.Contribution;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.media.Work;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class Subtitle implements Work, Comparable<Subtitle>
{
	public static final SimplePropDescriptor	PROP_MEDIA						= new SimplePropDescriptor(Subtitle.class, PropNames.MEDIA);
	public static final SimplePropDescriptor	PROP_LANGUAGE					= new SimplePropDescriptor(Subtitle.class, PropNames.LANGUAGE);
	public static final SimplePropDescriptor	PROP_HEARING_IMPAIRED			= new SimplePropDescriptor(Subtitle.class, PropNames.HEARING_IMPAIRED);
	public static final SimplePropDescriptor	PROP_FOREIGN_PARTS				= new SimplePropDescriptor(Subtitle.class, PropNames.FOREIGN_PARTS);
	public static final SimplePropDescriptor	PROP_TAGS						= new SimplePropDescriptor(Subtitle.class, PropNames.TAGS);
	public static final SimplePropDescriptor	PROP_VERSION					= new SimplePropDescriptor(Subtitle.class, PropNames.VERSION);
	public static final SimplePropDescriptor	PROP_GROUP						= new SimplePropDescriptor(Subtitle.class, PropNames.GROUP);
	public static final SimplePropDescriptor	PROP_SOURCE						= new SimplePropDescriptor(Subtitle.class, PropNames.SOURCE);
	public static final SimplePropDescriptor	PROP_PRODUCTION_TYPE			= new SimplePropDescriptor(Subtitle.class, PropNames.PRODUCTION_TYPE);
	public static final SimplePropDescriptor	PROP_BASIS						= new SimplePropDescriptor(Subtitle.class, PropNames.BASIS);
	public static final SimplePropDescriptor	PROP_INFO						= new SimplePropDescriptor(Subtitle.class, PropNames.INFO);
	public static final SimplePropDescriptor	PROP_INFO_URL					= new SimplePropDescriptor(Subtitle.class, PropNames.INFO_URL);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(Subtitle.class, PropNames.CONTRIBUTIONS);

	public static final Tag						TAG_HEARING_IMPAIRED			= new Tag("HI", "Hearing Impaired");

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
		 * It is unknown whether the subtitle is an original version or a translation.
		 */
		UNKNOWN,

		/**
		 * The subtitle's language is the original language. The language of this subtitle is equal to the primary original language of the subtitled
		 * media item (in French <code>"version originale" (VO)</code>)
		 */
		ORIGINAL,

		/**
		 * The subtitle is a translation. This language of this subtitle differs from the primary original language of the subtitled media item.
		 */
		TRANSLATION
	}

	public static enum ForeignParts
	{
		/**
		 * No foreign parts in the media item. Therefore none can be included or excluded. Foreign parts are irrelevant.
		 */
		NONE,

		/**
		 * Foreign parts exist in the media item and are included in the subtitle (typically the case for translated subtitles or VO subtitles where
		 * the foreign parts are not hard coded in the media release).
		 */
		INCLUDED,

		/**
		 * Foreign parts exist in the media item but are not included (typically the case for original subtitles).
		 */
		EXCLUDED,

		/**
		 * Foreign parts exist in the media item and only foreign parts are included (typically the case for special versions of original subtitles
		 * for people who only need subtitles for the foreign parts).
		 */
		ONLY;
	}

	public static final String			CONTRIBUTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";
	public static final String			CONTRIBUTION_TYPE_TIMINGS		= "TIMINGS";
	public static final String			CONTRIBUTION_TYPE_TRANSLATION	= "TRANSLATION";
	public static final String			CONTRIBUTION_TYPE_REVISION		= "REVISION";
	public static final String			CONTRIBUTION_TYPE_IMPROVEMENT	= "IMPROVEMENT";

	private AvMedia						media;
	private String						language;
	private boolean						hearingImpaired					= false;
	private ForeignParts				foreignParts					= ForeignParts.NONE;
	// Normally there are 0 extra tags per Subtitle
	private final List<Tag>				tags							= new ArrayList<>(0);
	private String						version;
	private Group						group;
	private String						source;
	private String						productionType;
	private Subtitle					basis;
	private String						info;
	private String						infoLink;
	// More than 4 contributions per subtitle is very rare
	private final List<Contribution>	contributions					= new ArrayList<>(4);

	public Subtitle()
	{

	}

	public Subtitle(AvMedia media)
	{
		this.media = media;
	}

	public Subtitle(AvMedia media, String language)
	{
		this.media = media;
		this.language = language;
	}

	public Subtitle(AvMedia media, String language, List<Tag> tags)
	{
		this.media = media;
		this.language = language;
		this.tags.addAll(tags);
	}

	public Subtitle(AvMedia media, String language, List<Tag> tags, Group group)
	{
		this.media = media;
		this.language = language;
		this.tags.addAll(tags);
		this.group = group;
	}

	public AvMedia getMedia()
	{
		return media;
	}

	public void setMedia(AvMedia media)
	{
		this.media = media;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * Whether the subtitle contains annotations for the hearing impaired.
	 * 
	 * @return whether or not hearing impaired
	 */
	public boolean isHearingImpaired()
	{
		return hearingImpaired;
	}

	public void setHearingImpaired(boolean hearingImpaired)
	{
		this.hearingImpaired = hearingImpaired;
	}

	public ForeignParts getForeignParts()
	{
		return foreignParts;
	}

	public void setForeignParts(ForeignParts foreignParts)
	{
		this.foreignParts = Objects.requireNonNull(foreignParts);
	}

	/**
	 * The tags of this subtitle. The tag list must <b>not</b> contain the following tags / information:
	 * <ul>
	 * <li><b>Language tags</b> like "German", "de" (the language is stored separately in {@link #getLanguage()})</li>
	 * <li><b>Foreign parts tags</b> like "FOREIGN PARTS INCLUDED" (the foreign parts information is stored separately in {@link #getForeignParts()})</li>
	 * <li><b>Hearing Impaired tags</b> like "HI" (whether the subtitle contains annotations for the hearing impaired is stored separately in
	 * {@link #isHearingImpaired()})</li>
	 * <li><b>Version tags</b> like "V2" (the version is stored separately in {@link #getVersion()})
	 * </ul>
	 * All other important information about this subtitle may be stored in the tag list. For example "COLORED" for colored subs.
	 * 
	 * @return the tags
	 */
	public List<Tag> getTags()
	{
		return tags;
	}

	public void setTags(List<Tag> tags)
	{
		this.tags.clear();
		this.tags.addAll(tags);
	}

	/**
	 * The version number of this subtitle. The version should follow the decimal notation (1.0) and get incremented whenever this subtitle is changed
	 * (improved) by its contributors. <br/>
	 * If someone other than its original contributors improves a subtitle, this leads to a new Subtitle which is {@link #getBasis() based on} the old
	 * one. <br/>
	 * If no version information is available, the version is {@code null}.
	 * <p>
	 * The version is not for storing information about alternate releases (like colored/uncolored, hearing impaired/not hearing impaired, ...). It's
	 * more like a revision.
	 * </p>
	 * 
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * The group which released this subtitle.
	 * 
	 * @return the release group
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
	 * The source of this subtitle. Typically the site which released this Subtitle.
	 * 
	 * @return the source
	 */
	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	/**
	 * How this subtitles was produced. See the <code>PRODUCTION_TYPE_*</code> constants.
	 * 
	 * @return the production type
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
	 * The subtitle on which this subtitle was based on. Only for production types {@link #PRODUCTION_TYPE_IMPROVEMENT} and
	 * {@link #PRODUCTION_TYPE_TRANSLATION}.
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
	 * The information (description) about this subtitle. Like release notes.
	 * 
	 * @return the information
	 */
	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * An URL pointing to a file or a website providing the information about this subtitle.
	 * 
	 * @return the information URL
	 */
	public String getInfoLink()
	{
		return infoLink;
	}

	public void setInfoLink(String infoLink)
	{
		this.infoLink = infoLink;
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
			return Objects.equals(media, o.media) && StringUtils.equalsIgnoreCase(language, o.language) && hearingImpaired == o.hearingImpaired
					&& foreignParts.equals(o.foreignParts) && tags.equals(o.tags) && Objects.equals(version, o.version)
					&& Objects.equals(group, o.group) && StringUtils.equalsIgnoreCase(source, o.source);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(media)
				.append(StringUtils.lowerCase(language, Locale.ENGLISH))
				.append(hearingImpaired)
				.append(foreignParts)
				.append(tags)
				.append(version)
				.append(group)
				.append(StringUtils.lowerCase(source, Locale.ENGLISH))
				.toHashCode();
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
				.compare(media, o.media, Medias.MEDIA_NAME_COMPARATOR)
				.compare(language, o.language, Settings.STRING_ORDERING)
				.compare(hearingImpaired, o.hearingImpaired)
				.compare(foreignParts, o.foreignParts)
				.compare(tags, o.tags, Tag.TAGS_COMPARATOR)
				.compare(version, version)
				.compare(group, o.group)
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
				.add("hearingImpaired", hearingImpaired)
				.add("foreignParts", foreignParts)
				.add("tags", Models.nullIfEmpty(tags))
				.add("version", version)
				.add("group", group)
				.add("source", source)
				.add("productionType", productionType)
				.add("basis", basis)
				.add("info", info)
				.add("infoLink", infoLink)
				.add("contributions", Models.nullIfEmpty(contributions))
				.toString();
	}
}
