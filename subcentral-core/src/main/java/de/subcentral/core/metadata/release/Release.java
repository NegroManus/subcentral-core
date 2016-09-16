package de.subcentral.core.metadata.release;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.PropNames;
import de.subcentral.core.metadata.NamedMetadataBase;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.name.NamingUtil;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.ValidationUtil;

/**
 * A Release is a publication of one media (movie, TV episode, series, season, song, album, movie, game, software) or a set of media (multiple TV episodes). Every Release has a unique name so that it
 * can be identified. The name is constructed of:
 * <ul>
 * <li>the name(s) of the media</li>
 * <li>the release tags</li>
 * <li>the release group</li>
 * </ul>
 * 
 * So, every Release contains a set of media, has a list of release tags and is released by its release group.
 *
 */
public class Release extends NamedMetadataBase implements Comparable<Release> {
	private static final long					serialVersionUID		= 3021851008940378834L;

	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(Release.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_MEDIA				= new SimplePropDescriptor(Release.class, PropNames.MEDIA);
	public static final SimplePropDescriptor	PROP_TAGS				= new SimplePropDescriptor(Release.class, PropNames.TAGS);
	public static final SimplePropDescriptor	PROP_GROUP				= new SimplePropDescriptor(Release.class, PropNames.GROUP);
	public static final SimplePropDescriptor	PROP_SOURCE				= new SimplePropDescriptor(Release.class, PropNames.SOURCE);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(Release.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_CATEGORY			= new SimplePropDescriptor(Release.class, PropNames.CATEGORY);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(Release.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SIZE				= new SimplePropDescriptor(Release.class, PropNames.SIZE);
	public static final SimplePropDescriptor	PROP_FILE_COUNT			= new SimplePropDescriptor(Release.class, PropNames.FILE_COUNT);
	public static final SimplePropDescriptor	PROP_NUKES				= new SimplePropDescriptor(Release.class, PropNames.NUKES);
	public static final SimplePropDescriptor	PROP_UNNUKES			= new SimplePropDescriptor(Release.class, PropNames.UNNUKES);
	public static final SimplePropDescriptor	PROP_NFO				= new SimplePropDescriptor(Release.class, PropNames.NFO);
	public static final SimplePropDescriptor	PROP_NFO_LINK			= new SimplePropDescriptor(Release.class, PropNames.NFO_LINK);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(Release.class, PropNames.FURTHER_INFO_LINKS);
	public static final SimplePropDescriptor	PROP_IDS				= new SimplePropDescriptor(Release.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(Release.class, PropNames.ATTRIBUTES);

	public static final Comparator<Release>		NAME_COMPARATOR			= (Release r1, Release r2) -> r1 == null ? (r2 == null ? 0 : -1) : r1.compareToByName(r2);

	private String								name;
	// In 99% of the cases, there is only one Media per Release
	private final List<Media>					media					= new ArrayList<>(1);
	// Normally there are 2 to 4 Tags per Release
	private final List<Tag>						tags					= new ArrayList<>(4);
	private Group								group;
	private Site								source;
	private final List<String>					languages				= new ArrayList<>(1);
	private String								category;
	private Temporal							date;
	private long								size					= 0L;
	private int									fileCount				= 0;
	private final List<Nuke>					nukes					= new ArrayList<>(0);
	private String								nfo;
	private String								nfoLink;
	private final List<String>					furtherInfoLinks		= new ArrayList<>(4);

	public Release() {
		// default constructor
	}

	public Release(String name) {
		this.name = name;
	}

	public Release(List<Tag> tags, Group group) {
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(Media media, List<Tag> tags, Group group) {
		this.media.add(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(List<Media> media, List<Tag> tags, Group group) {
		this.media.addAll(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(String name, Media media, List<Tag> tags, Group group) {
		this.name = name;
		this.media.add(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(String name, List<Media> media, List<Tag> tags, Group group) {
		this.name = name;
		this.media.addAll(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(Release rls) {
		this.name = rls.name;
		// just the media references are copied into the new list, no deep copy
		this.media.addAll(rls.media);
		this.tags.addAll(rls.tags);
		this.group = rls.group;
		this.languages.addAll(rls.languages);
		this.category = rls.category;
		this.date = rls.date;
		this.size = rls.size;
		this.fileCount = rls.fileCount;
		this.nukes.addAll(rls.nukes);
		this.nfo = rls.nfo;
		this.nfoLink = rls.nfoLink;
	}

	public static Release create(String group, String... tags) {
		return create(null, null, group, tags);
	}

	public static Release create(Media media, String group, String... tags) {
		return create(null, media, group, tags);
	}

	public static Release create(String name, Media media, String group, String... tags) {
		Release rls = new Release();
		rls.name = name;
		if (media != null) {
			rls.media.add(media);
		}
		if (group != null) {
			rls.group = Group.of(group);
		}
		if (tags.length > 0) {
			// use addAll and do not add separately so that the list is trimmed to the right size
			rls.tags.addAll(Tags.of(tags));
		}
		return rls;
	}

	/**
	 * The unique name of this release. E.g. "Psych.S08E01.HDTV.x264-EXCELLENCE". Consisting of the name of the {@link #getMedia() media} ( "Psych S08E01"), the {@link #getTags() tags} (HDTV, x264)
	 * and the {@link #getGroup() group} ("EXCELLENCE").
	 * 
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The contained media. For the most cases, a Release contains only one media. But, for example, multi-part {@link de.subcentral.core.metadata.media.Episode Episodes} are sometimes packed into one
	 * Release.
	 * 
	 * @return the contained media
	 */

	public List<Media> getMedia() {
		return media;
	}

	public void setMedia(Collection<? extends Media> media) {
		this.media.clear();
		this.media.addAll(media);
	}

	/**
	 * The release tags. Like XviD, WEB-DL, DD5.1, 720p, HDTV, PROPER, REPACK, GERMAN CUSTOM SUBBED, FRENCH, NLSUBBED, etc. May contain language tags.
	 * 
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
	}

	/**
	 * The group which released this release.
	 * 
	 * @return the release group
	 * @see #getSource()
	 */
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	/**
	 * The source of this release. Typically a site that distributed the release. {@code null} if unknown.
	 * 
	 * @return the source (may be {@code null})
	 */
	public Site getSource() {
		return source;
	}

	public void setSource(Site source) {
		this.source = source;
	}

	/**
	 * The (spoken or written) languages of the media of this release. The languages may also appear in the tags.
	 * 
	 * @return the languages.
	 */
	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages.clear();
		this.languages.addAll(languages);
	}

	/**
	 * The release category (section).
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * The release date.
	 * 
	 * @return the date.
	 */
	public Temporal getDate() {
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException {
		this.date = ValidationUtil.validateTemporalClass(date);
	}

	/**
	 * The total file size of the release in bytes.
	 * 
	 * @return the file size
	 */

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * The number of files which form this release. Typically the number of archive files in which the release is split.
	 * 
	 * @return the file count
	 */
	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	/**
	 * The nukes. If a Release violates the rules, it gets nuked. Then, a Nuke instance is associated with it.
	 * 
	 * @return the nukes
	 */
	public List<Nuke> getNukes() {
		return nukes;
	}

	public void setNukes(Collection<Nuke> nukes) {
		this.nukes.clear();
		this.nukes.addAll(nukes);
	}

	/**
	 * The release information (content of the NFO file).
	 * 
	 * @return the NFO
	 */
	public String getNfo() {
		return nfo;
	}

	public void setNfo(String nfo) {
		this.nfo = nfo;
	}

	/**
	 * A link pointing to a file or a HTML page with the NFO of this release.
	 * 
	 * @return the link to the NFO
	 * @see #getNfo()
	 */
	public String getNfoLink() {
		return nfoLink;
	}

	public void setNfoLink(String nfoLink) {
		this.nfoLink = nfoLink;
	}

	public List<String> getFurtherInfoLinks() {
		return furtherInfoLinks;
	}

	public void setFurtherInfoLinks(Collection<String> furtherInfoLinks) {
		this.furtherInfoLinks.clear();
		this.furtherInfoLinks.addAll(furtherInfoLinks);
	}

	// Convenience
	public boolean containsSingleMedia() {
		return media.size() == 1;
	}

	public Media getFirstMedia() {
		return media.isEmpty() ? null : media.get(0);
	}

	public void setSingleMedia(Media media) {
		this.media.clear();
		if (media != null) {
			this.media.add(media);
		}
	}

	public boolean isNuked() {
		return !nukes.isEmpty();
	}

	public void nuke(String nukeReason) {
		nukes.add(Nuke.of(nukeReason));
	}

	public void nuke(String nukeReason, Temporal date) {
		nukes.add(Nuke.of(nukeReason, date));
	}

	public void unnuke(String unnukeReason) {
		nukes.add(Nuke.of(unnukeReason, true));
	}

	public void unnuke(String unnukeReason, Temporal date) {
		nukes.add(Nuke.of(unnukeReason, date, true));
	}

	// Object methods
	/**
	 * Compared by their {@link #getMedia() media}, {@link #getTags() tags} and {@link #getGroup() group}.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Release) {
			Release o = (Release) obj;
			return media.equals(o.media) && Objects.equals(group, o.group) && tags.equals(o.tags);
		}
		return false;
	}

	public boolean equalsByName(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Release) {
			return ObjectUtil.stringEqualIgnoreCase(name, ((Release) obj).name);
		}
		return false;
	}

	/**
	 * Calculated from its {@link #getMedia() media}, {@link #getTags() tags} and {@link #getGroup() group}.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(media, group, tags);
	}

	public int hashCodeByName() {
		return ObjectUtil.stringHashCodeIgnoreCase(name);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Release.class)
				.omitNullValues()
				.add("name", name)
				.add("media", ObjectUtil.nullIfEmpty(media))
				.add("tags", ObjectUtil.nullIfEmpty(tags))
				.add("group", group)
				.add("source", source)
				.add("languages", ObjectUtil.nullIfEmpty(languages))
				.add("date", date)
				.add("category", category)
				.add("size", ObjectUtil.nullIfZero(size))
				.add("fileCount", ObjectUtil.nullIfZero(fileCount))
				.add("nukes", ObjectUtil.nullIfEmpty(nukes))
				.add("nfo", nfo)
				.add("nfoLink", nfoLink)
				.add("furtherInfo", ObjectUtil.nullIfEmpty(furtherInfoLinks))
				.add("ids", ObjectUtil.nullIfEmpty(ids))
				.add("attributes", ObjectUtil.nullIfEmpty(attributes))
				.toString();
	}

	/**
	 * Two releases are compared by their media, then their tags and then by their groups.
	 */
	@Override
	public int compareTo(Release o) {
		if (this == o) {
			return 0;
		}
		// nulls first
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start()
				.compare(media, o.media, NamingUtil.DEFAULT_MEDIA_ITERABLE_NAME_COMPARATOR)
				.compare(tags, o.tags, Tags.COMPARATOR)
				.compare(group, o.group, ObjectUtil.getDefaultOrdering())
				.result();
	}

	public int compareToByName(Release o) {
		if (this == o) {
			return 0;
		}
		// nulls first
		if (o == null) {
			return 1;
		}
		return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
	}
}
