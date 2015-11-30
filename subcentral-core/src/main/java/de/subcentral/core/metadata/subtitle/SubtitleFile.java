package de.subcentral.core.metadata.subtitle;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.Contributor;
import de.subcentral.core.metadata.MetadataBase;
import de.subcentral.core.metadata.Work;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.IterableComparator;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleFile extends MetadataBase implements Work, Comparable<SubtitleFile>
{
	private static final long					serialVersionUID		= 3266903304683246434L;

	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(SubtitleFile.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_SUBTITLES			= new SimplePropDescriptor(SubtitleFile.class, PropNames.SUBTITLES);
	public static final SimplePropDescriptor	PROP_TAGS				= new SimplePropDescriptor(SubtitleFile.class, PropNames.TAGS);
	public static final SimplePropDescriptor	PROP_MATCHING_RELEASES	= new SimplePropDescriptor(SubtitleFile.class, PropNames.MATCHING_RELEASES);
	public static final SimplePropDescriptor	PROP_VERSION			= new SimplePropDescriptor(SubtitleFile.class, PropNames.VERSION);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(SubtitleFile.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SIZE				= new SimplePropDescriptor(SubtitleFile.class, PropNames.SIZE);
	public static final SimplePropDescriptor	PROP_NFO				= new SimplePropDescriptor(SubtitleFile.class, PropNames.NFO);
	public static final SimplePropDescriptor	PROP_NFO_LINK			= new SimplePropDescriptor(SubtitleFile.class, PropNames.NFO_LINK);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS		= new SimplePropDescriptor(SubtitleFile.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_IDS				= new SimplePropDescriptor(SubtitleFile.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(SubtitleFile.class, PropNames.ATTRIBUTES);

	public static final Tag						HEARING_IMPAIRED_TAG	= new Tag("HI", "Hearing Impaired");

	public static enum ForeignParts
	{
		/**
		 * No foreign parts in the media item. Therefore none can be included or excluded. Foreign parts are irrelevant.
		 */
		NONE,

		/**
		 * Foreign parts exist in the media item and are included in the subtitle (typically the case for translated subtitles or VO subtitles where the foreign parts are not hard coded in the media
		 * release).
		 */
		INCLUDED,

		/**
		 * Foreign parts exist in the media item but are not included (typically the case for original subtitles).
		 */
		EXCLUDED,

		/**
		 * Foreign parts exist in the media item and only foreign parts are included (typically the case for special versions of original subtitles for people who only need subtitles for the foreign
		 * parts).
		 */
		ONLY;
	}

	/**
	 * Adjustment of the timings so the subtitle fits on a specific video.
	 */
	public static final String	CONTRIBUTION_TYPE_ADJUSTMENT	= "ADJUSTMENT";

	/**
	 * Textual customization of a subtitle (for example adding/removal of Hearing Impaired parts, foreign language parts, ...).
	 */
	public static final String	CONTRIBUTION_TYPE_CUSTOMIZATION	= "CUSTOMIZATION";

	public static SubtitleFile create(Release matchingRelease, String language, String group)
	{
		SubtitleFile subAdjustment = new SubtitleFile();
		Group grp = null;
		if (group != null)
		{
			grp = new Group(group);
		}
		for (Media media : matchingRelease.getMedia())
		{
			Subtitle sub = new Subtitle();
			sub.setMedia(media);
			sub.setLanguage(language);
			if (grp != null)
			{
				sub.setGroup(grp);
			}
			subAdjustment.getSubtitles().add(sub);
		}
		subAdjustment.getMatchingReleases().add(matchingRelease);
		return subAdjustment;
	}

	private String						name;
	// In 99% of the cases, there is only one subtitle, at most 2
	private final List<Subtitle>		subtitles			= new ArrayList<>(1);
	// Normally there are 0 extra tags
	private final List<Tag>				tags				= new ArrayList<>(0);
	// Most adjustments are compatible to 1 or 2 releases
	// HashMap / HashSet initial capacities should be a power of 2
	private final Set<Release>			matchingReleases	= new HashSet<>(2);
	private String						version;
	private Temporal					date;
	private long						size				= 0L;
	private String						nfo;
	private String						nfoLink;
	// In 99% of the cases, there is only one adjustment contribution
	private final List<Contribution>	contributions		= new ArrayList<>(1);

	public SubtitleFile()
	{

	}

	public SubtitleFile(Subtitle subtitle, Release matchingRelease)
	{
		this(null, subtitle, matchingRelease);
	}

	public SubtitleFile(String name, Subtitle subtitle, Release matchingRelease)
	{
		this.name = name;
		this.subtitles.add(subtitle);
		this.matchingReleases.add(matchingRelease);
	}

	public SubtitleFile(Subtitle subtitle, Collection<Release> matchingReleases)
	{
		this(null, subtitle, matchingReleases);
	}

	public SubtitleFile(String name, Subtitle subtitle, Collection<Release> matchingReleases)
	{
		this.name = name;
		this.subtitles.add(subtitle);
		this.matchingReleases.addAll(matchingReleases);
	}

	public SubtitleFile(List<Subtitle> subtitles, Release matchingRelease)
	{
		this.subtitles.addAll(subtitles);
		this.matchingReleases.add(matchingRelease);
	}

	public SubtitleFile(List<Subtitle> subtitles, Collection<Release> matchingReleases)
	{
		this.subtitles.addAll(subtitles);
		this.matchingReleases.addAll(matchingReleases);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	// Properties
	public List<Subtitle> getSubtitles()
	{
		return subtitles;
	}

	public void setSubtitles(List<Subtitle> subtitles)
	{
		this.subtitles.clear();
		this.subtitles.addAll(subtitles);
	}

	/**
	 * The tags of this subtitle. The tag list must <b>not</b> contain the following tags / information:
	 * <ul>
	 * <li><b>Language tags</b> like "German", "de" (the language is stored separately in {@link #getLanguage()})</li>
	 * <li><b>Foreign parts tags</b> like "FOREIGN PARTS INCLUDED" (the foreign parts information is stored separately in {@link #getForeignParts()})</li>
	 * <li><b>Hearing Impaired tags</b> like "HI" (whether the subtitle contains annotations for the hearing impaired is stored separately in {@link #isHearingImpaired()})</li>
	 * <li><b>Version tags</b> like "V2" (the version is stored separately in {@link #getVersion()})
	 * </ul>
	 * All other important information about this subtitle may be stored in the tag list. For example "COLORED" for colored subs.
	 * 
	 * @return the tags (never {@code null}, may be empty)
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

	public Set<Release> getMatchingReleases()
	{
		return matchingReleases;
	}

	public void setMatchingReleases(Collection<? extends Release> matchingReleases)
	{
		this.matchingReleases.clear();
		this.matchingReleases.addAll(matchingReleases);
	}

	/**
	 * The version string defines the version of this subtitle. The version string should be a simple version number (1, 2, 3, ...) or follow the decimal notation (1.0, 2.0, 2.0.1, ...) and be
	 * incremented whenever this subtitle is changed (improved). But there are no limitations on valid version strings as any source has its own version scheme. For example, for addic7ed.com the
	 * version string can be one of "orig", "c.orig", "c.updated".
	 * <p>
	 * The version string must not contain information about differences from alternate releases (like colored/uncolored, hearing impaired/not hearing impaired, includes foreign parts/does not include
	 * foreign parts, ...).
	 * </p>
	 * <p>
	 * An improved/customized subtitle is always {@link #getBasis() based on} the former version of that subtitle and has the {@link #getProductionType() productionType}
	 * {@value #PRODUCTION_TYPE_MODIFICATION}.
	 * </p>
	 * 
	 * If no version information is available, the version is {@code null}.
	 * 
	 * @return the version string (may be {@code null})
	 */
	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		this.date = BeanUtil.validateTemporalClass(date);
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * Specific information about this adjustment (content of NFO file).
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
	 * A link pointing to a file or a HTML page with the NFO of this adjustment.
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

	// Convenience
	public boolean containsSingleSubtitle()
	{
		return subtitles.size() == 1;
	}

	public Subtitle getFirstSubtitle()
	{
		return subtitles.isEmpty() ? null : subtitles.get(0);
	}

	public void setSingleSubtitle(Subtitle subtitle)
	{
		this.subtitles.clear();
		if (subtitle != null)
		{
			this.subtitles.add(subtitle);
		}
	}

	/**
	 * Whether the subtitle contains transcription for the hearing impaired. Also known as "closed captioning (CC)". The default value is {@code false}.
	 * 
	 * @return whether or not hearing impaired
	 */
	public boolean isHearingImpaired()
	{
		return tags.contains(HEARING_IMPAIRED_TAG);
	}

	public void setHearingImpaired(boolean hearingImpaired)
	{
		if (hearingImpaired)
		{
			if (!tags.contains(HEARING_IMPAIRED_TAG))
			{
				tags.add(HEARING_IMPAIRED_TAG);
			}
		}
		else
		{
			tags.remove(HEARING_IMPAIRED_TAG);
		}
	}

	public boolean matchesSingleRelease()
	{
		return matchingReleases.size() == 1;
	}

	public Release getFirstMatchingRelease()
	{
		return matchingReleases.isEmpty() ? null : matchingReleases.iterator().next();
	}

	public void setSingleMatchingRelease(Release matchingRelease)
	{
		this.matchingReleases.clear();
		if (matchingRelease != null)
		{
			this.matchingReleases.add(matchingRelease);
		}
	}

	public void addAdjuster(Contributor adjuster)
	{
		contributions.add(new Contribution(adjuster, CONTRIBUTION_TYPE_ADJUSTMENT));
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof SubtitleFile)
		{
			SubtitleFile o = (SubtitleFile) obj;
			return subtitles.equals(o.subtitles) && tags.equals(o.tags) && matchingReleases.equals(o.matchingReleases) && Objects.equals(version, o.version);
		}
		return false;
	}

	public boolean equalsByName(SubtitleFile other)
	{
		return other == null ? false : name == null ? false : name.equals(other.name);
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(33, 51).append(subtitles).append(tags).append(matchingReleases).append(version).toHashCode();
	}

	@Override
	public int compareTo(SubtitleFile o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start()
				.compare(subtitles, o.subtitles, IterableComparator.<Subtitle> create())
				.compare(tags, o.tags, Tag.TAGS_COMPARATOR)
				.compare(matchingReleases, matchingReleases, IterableComparator.<Release> create())
				.compare(version, version, Settings.STRING_ORDERING)
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SubtitleFile.class)
				.omitNullValues()
				.add("name", name)
				.add("subtitles", BeanUtil.nullIfEmpty(subtitles))
				.add("tags", BeanUtil.nullIfEmpty(tags))
				.add("matchingReleases", BeanUtil.nullIfEmpty(matchingReleases))
				.add("version", version)
				.add("date", date)
				.add("size", BeanUtil.nullIfZero(size))
				.add("nfo", nfo)
				.add("nfoLink", nfoLink)
				.add("contributions", BeanUtil.nullIfEmpty(contributions))
				.add("ids", BeanUtil.nullIfEmpty(ids))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
