package de.subcentral.core.model.subtitle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Contribution;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.Prop;
import de.subcentral.core.model.Work;
import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class Subtitle implements Work, Comparable<Subtitle>
{
	public static final SimplePropDescriptor	PROP_MEDIA_ITEM				= new SimplePropDescriptor(Subtitle.class, Prop.MEDIA_ITEM);
	public static final SimplePropDescriptor	PROP_LANGUAGE				= new SimplePropDescriptor(Subtitle.class, Prop.LANGUAGE);
	public static final SimplePropDescriptor	PROP_HEARING_IMPAIRED		= new SimplePropDescriptor(Subtitle.class, Prop.HEARING_IMPAIRED);
	public static final SimplePropDescriptor	PROP_FOREIGN_PARTS			= new SimplePropDescriptor(Subtitle.class, Prop.FOREIGN_PARTS);
	public static final SimplePropDescriptor	PROP_TAGS					= new SimplePropDescriptor(Subtitle.class, Prop.TAGS);
	public static final SimplePropDescriptor	PROP_GROUP					= new SimplePropDescriptor(Subtitle.class, Prop.GROUP);
	public static final SimplePropDescriptor	PROP_VERSION				= new SimplePropDescriptor(Subtitle.class, Prop.VERSION);
	public static final SimplePropDescriptor	PROP_PRODUCTION_TYPE		= new SimplePropDescriptor(Subtitle.class, Prop.PRODUCTION_TYPE);
	public static final SimplePropDescriptor	PROP_BASIS					= new SimplePropDescriptor(Subtitle.class, Prop.BASIS);
	public static final SimplePropDescriptor	PROP_INFO					= new SimplePropDescriptor(Subtitle.class, Prop.INFO);
	public static final SimplePropDescriptor	PROP_INFO_URL				= new SimplePropDescriptor(Subtitle.class, Prop.INFO_URL);
	public static final SimplePropDescriptor	PROP_SOURCE					= new SimplePropDescriptor(Subtitle.class, Prop.SOURCE);
	public static final SimplePropDescriptor	PROP_SOURCE_URL				= new SimplePropDescriptor(Subtitle.class, Prop.SOURCE_URL);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(Subtitle.class, Prop.CONTRIBUTIONS);

	public static final Tag						TAG_HEARING_IMPAIRED		= new Tag("HI", "Hearing Impaired", Tag.CATEGORY_FORMAT);

	/**
	 * If a transcript was the source of the subtitle.
	 */
	public static final String					PRODUCTION_TYPE_TRANSCRIPT	= "TRANSCRIPT";

	/**
	 * If the subtitles was created by hearing what is said.
	 */
	public static final String					PRODUCTION_TYPE_LISTENING	= "LISTENING";

	/**
	 * If the subtitle was ripped from a retail source (DVD, BluRay, CD, etc).
	 */
	public static final String					PRODUCTION_TYPE_RETAIL		= "RETAIL";

	/**
	 * If the subtitle is an improvement or modification / customization of another subtitle.
	 */
	public static final String					PRODUCTION_TYPE_IMPROVEMENT	= "IMPROVEMENT";

	/**
	 * If the subtitle is a translation of another subtitle.
	 */
	public static final String					PRODUCTION_TYPE_TRANSLATION	= "TRANSLATION";

	/**
	 * If the subtitle was produced automatically by a machine.
	 */
	public static final String					PRODUCTION_TYPE_MACHINE		= "MACHINE";

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

	private AvMediaItem					mediaItem;
	private String						language;
	private boolean						hearingImpaired					= false;
	private ForeignParts				foreignParts					= ForeignParts.NONE;
	// Normally there are 0 extra tags per Subtitle
	private final List<Tag>				tags							= new ArrayList<>(0);
	private int							version							= 1;
	private Group						group;
	private String						productionType;
	private String						info;
	private String						infoUrl;
	private String						source;
	private String						sourceUrl;
	// More than 5 contributions per subtitle is very rare
	private final List<Contribution>	contributions					= new ArrayList<>(5);
	private Subtitle					basis;

	public Subtitle()
	{

	}

	public Subtitle(AvMediaItem mediaItem)
	{
		this(mediaItem, null);
	}

	public Subtitle(AvMediaItem mediaItem, String language)
	{
		this.mediaItem = mediaItem;
		this.language = language;
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

	/**
	 * 
	 * @return Whether the subtitle contains annotations for the hearing impaired.
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
		this.foreignParts = foreignParts;
	}

	/**
	 * The tag list must not contain the following tags / information:
	 * <ul>
	 * <li><b>Language tags</b> like "German", "de" (the language is stored separately in {@link #getLanguage()})</li>
	 * <li><b>Foreign parts tags</b> like "FOREIGN PARTS INCLUDED" (the foreign parts information is stored separately in {@link #getForeignParts()})</li>
	 * <li><b>Hearing Impaired tags</b> like "HI" (whether the subtitle contains annotations for the hearing impaired is stored separately in
	 * {@link #isHearingImpaired()})</li>
	 * <li><b>Version tags</b> like "V2" (the version is stored separately in {@link #getVersion()})
	 * </ul>
	 * All other important information about this subtitle may be stored in the tag list.
	 * 
	 * @return The tags of this subtitle.
	 */
	public List<Tag> getTags()
	{
		return tags;
	}

	public void setTags(List<Tag> tags)
	{
		Validate.notNull(tags, "tags cannot be null");
		this.tags.clear();
		this.tags.addAll(tags);
	}

	/**
	 * The version gets incremented whenever this subtitle is changed by its contributors. If someone other than its original contributors improves a
	 * subtitle, this leads to a new Subtitle which is {@link #getBasis() based on} the old one.
	 * 
	 * @return The version of this subtitle. Initially <code>1</code>.
	 */
	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
	 * 
	 * @return The group which released this subtitle.
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
	 * See the <code>PRODUCTION_TYPE_*</code> constants.
	 * 
	 * @return The production type. How this subtitles was produced.
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
	 * 
	 * @return The information (description) about this subtitle. Like release notes.
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
	 * 
	 * @return An URL pointing to a file or a website providing the information about this subtitle.
	 */
	public String getInfoUrl()
	{
		return infoUrl;
	}

	public void setInfoUrl(String infoUrl)
	{
		this.infoUrl = infoUrl;
	}

	/**
	 * 
	 * @return The source of this subtitle. Typically the site which released this Subtitle.
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
	 * 
	 * @return An URL pointing to the website of the source of this subtitle. Typically the site which released this Subtitle.
	 */
	public String getSourceUrl()
	{
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
	}

	@Override
	public List<Contribution> getContributions()
	{
		return contributions;
	}

	public void setContributions(List<Contribution> contributions)
	{
		Validate.notNull(contributions, "contributions cannot be null");
		this.contributions.clear();
		this.contributions.addAll(contributions);
	}

	/**
	 * 
	 * @return The subtitle on which this subtitle was based on (only for production types {@link #PRODUCTION_TYPE_IMPROVEMENT} and
	 *         {@link #PRODUCTION_TYPE_TRANSLATION}.
	 */
	public Subtitle getBasis()
	{
		return basis;
	}

	public void setBasis(Subtitle basis)
	{
		this.basis = basis;
	}

	// convenience / complex
	public TranslationType determineTranslationType()
	{
		if (mediaItem == null || language == null)
		{
			return TranslationType.UNKNOWN;
		}
		String primaryLangOfMedia = mediaItem.getPrimaryOriginalLanguage();
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
		if (obj != null && Subtitle.class.equals(obj.getClass()))
		{
			Subtitle o = (Subtitle) obj;
			return new EqualsBuilder().append(mediaItem, o.mediaItem)
					.append(language, o.language)
					.append(hearingImpaired, o.hearingImpaired)
					.append(foreignParts, o.foreignParts)
					.append(tags, o.tags)
					.append(group, o.group)
					.append(version, o.version)
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(37, 99).append(mediaItem)
				.append(language)
				.append(hearingImpaired)
				.append(foreignParts)
				.append(tags)
				.append(group)
				.append(version)
				.toHashCode();
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
				.compare(hearingImpaired, o.hearingImpaired)
				.compare(foreignParts, o.foreignParts)
				.compare(tags, o.tags, Releases.TAGS_COMPARATOR)
				.compare(group, o.group)
				.compare(version, version)
				.result();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Subtitle.class)
				.omitNullValues()
				.add("mediaItem", mediaItem)
				.add("language", language)
				.add("hearingImpaired", hearingImpaired)
				.add("foreignParts", foreignParts)
				.add("tags", Models.nullIfEmpty(tags))
				.add("group", group)
				.add("version", version)
				.add("productionType", productionType)
				.add("basis", basis)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.add("source", source)
				.add("sourceUrl", sourceUrl)
				.add("contributions", Models.nullIfEmpty(contributions))
				.toString();
	}
}
