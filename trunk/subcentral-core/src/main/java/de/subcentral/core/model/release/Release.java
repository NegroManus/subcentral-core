package de.subcentral.core.model.release;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Medias;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * * A Release is a publication of one media (movie, TV episode, series, season, song, album, movie, game, software) or a set of media (multiple TV
 * episodes). Every Release has a unique name so that it can be identified. The name is constructed of:
 * <ul>
 * <li>the name(s) of the media</li>
 * <li>the release tags</li>
 * <li>the release group</li>
 * </ul>
 * 
 * So, every Release contains a set of media, has a list of release tags and is released by its release group.
 *
 */
public class Release implements Comparable<Release>
{
	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(Release.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_MEDIA				= new SimplePropDescriptor(Release.class, PropNames.MEDIA);
	public static final SimplePropDescriptor	PROP_TAGS				= new SimplePropDescriptor(Release.class, PropNames.TAGS);
	public static final SimplePropDescriptor	PROP_GROUP				= new SimplePropDescriptor(Release.class, PropNames.GROUP);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(Release.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_SECTION			= new SimplePropDescriptor(Release.class, PropNames.SECTION);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(Release.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SIZE				= new SimplePropDescriptor(Release.class, PropNames.SIZE);
	public static final SimplePropDescriptor	PROP_FILE_COUNT			= new SimplePropDescriptor(Release.class, PropNames.FILE_COUNT);
	public static final SimplePropDescriptor	PROP_NUKES				= new SimplePropDescriptor(Release.class, PropNames.NUKES);
	public static final SimplePropDescriptor	PROP_NFO				= new SimplePropDescriptor(Release.class, PropNames.NFO);
	public static final SimplePropDescriptor	PROP_NFO_URL			= new SimplePropDescriptor(Release.class, PropNames.NFO_URL);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(Release.class, PropNames.FURTHER_INFO_LINKS);

	private String								name;
	// In 99% of the cases, there is only one Media per Release
	private final List<Media>					media					= new ArrayList<>(1);
	// Normally there are 2 to 4 Tags per Release
	private final List<Tag>						tags					= new ArrayList<>(4);
	private Group								group;
	private final List<String>					languages				= new ArrayList<>(1);
	private String								section;
	private Temporal							date;
	private long								size					= 0L;
	private int									fileCount				= 0;
	private final List<Nuke>					nukes					= new ArrayList<>(0);
	private String								nfo;
	private String								nfoLink;
	private List<String>						furtherInfoLinks		= new ArrayList<>(4);

	public static Release create(Media media, String group, String... tags)
	{
		return create(null, media, group, tags);
	}

	public static Release create(String name, Media media, String group, String... tags)
	{
		Release rls = new Release();
		rls.name = name;
		rls.media.add(media);
		if (group != null)
		{
			rls.group = new Group(group);
		}
		if (tags.length > 0)
		{
			// use addAll and do not add separately so that the list is trimmed to the right size
			rls.tags.addAll(Tag.list(tags));
		}
		return rls;
	}

	public Release()
	{

	}

	public Release(String name)
	{
		this.name = name;
	}

	public Release(String name, Media media, List<Tag> tags, Group group)
	{
		this.name = name;
		this.media.add(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(String name, List<Media> media, List<Tag> tags, Group group)
	{
		this.name = name;
		this.media.addAll(media);
		this.tags.addAll(tags);
		this.group = group;
	}

	public Release(Release rls)
	{
		this.name = rls.name;
		// just the media references are copied into the new list, no deep copy
		this.media.addAll(rls.media);
		this.tags.addAll(rls.tags);
		this.group = rls.group;
		this.languages.addAll(rls.languages);
		this.date = rls.date;
		this.section = rls.section;
		this.size = rls.size;
		this.fileCount = rls.fileCount;
		this.nukes.addAll(rls.nukes);
		this.nfo = rls.nfo;
		this.nfoLink = rls.nfoLink;
	}

	/**
	 * The unique name of this release. E.g. "Psych.S08E01.HDTV.x264-EXCELLENCE". Consisting of the name of the {@link #getMedia() media}
	 * ("Psych S08E01"), the {@link #getTags() tags} (HDTV, x264) and the {@link #getGroup() group} ("EXCELLENCE").
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * The contained media. For the most cases, a Release contains only one media. But, for example, multi-part
	 * {@link de.subcentral.core.model.media.Episode Episodes} are sometimes packed into one Release.
	 * 
	 * @return the contained media
	 */

	public List<Media> getMedia()
	{
		return media;
	}

	public void setMedia(List<Media> media)
	{
		this.media.clear();
		this.media.addAll(media);
	}

	/**
	 * The release tags. Like XviD, WEB-DL, DD5.1, 720p, HDTV, PROPER, REPACK, GERMAN CUSTOM SUBBED, FRENCH, NLSUBBED, etc. May contain language tags.
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
	 * The group which released this release.
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
	 * The (spoken or written) languages of the media of this release. The languages may also appear in the tags.
	 * 
	 * @return the languages.
	 */
	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> languages)
	{
		this.languages.clear();
		this.languages.addAll(languages);
	}

	/**
	 * The release section / category.
	 * 
	 * @return the section
	 */
	public String getSection()
	{
		return section;
	}

	public void setSection(String section)
	{
		this.section = section;
	}

	/**
	 * The release date.
	 * 
	 * @return the date.
	 */
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date) throws IllegalArgumentException
	{
		this.date = Models.validateTemporalClass(date);
	}

	/**
	 * The total file size of the release in bytes.
	 * 
	 * @return the file size
	 */

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	/**
	 * The number of files which form this release. Typically the number of archive files in which the release is split.
	 * 
	 * @return the file count
	 */
	public int getFileCount()
	{
		return fileCount;
	}

	public void setFileCount(int fileCount)
	{
		this.fileCount = fileCount;
	}

	/**
	 * The nukes. If a Release violates the rules, it gets nuked. Then, a Nuke instance is associated with it.
	 * 
	 * @return the nukes
	 */
	public List<Nuke> getNukes()
	{
		return nukes;
	}

	public void setNukes(Collection<Nuke> nukes)
	{
		this.nukes.clear();
		this.nukes.addAll(nukes);
	}

	/**
	 * The release information (content of the NFO file).
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
	 * A link pointing to a file or a HTML page with the NFO of this release.
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

	public List<String> getFurtherInfoLinks()
	{
		return furtherInfoLinks;
	}

	public void setFurtherInfoLinks(Collection<String> furtherInfoLinks)
	{
		this.furtherInfoLinks.clear();
		this.furtherInfoLinks.addAll(furtherInfoLinks);
	}

	// Convenience
	public boolean containsSingleMedia()
	{
		return media.size() == 1;
	}

	public Media getFirstMedia()
	{
		return media.isEmpty() ? null : media.get(0);
	}

	public void setSingleMedia(Media media)
	{
		this.media.clear();
		if (media != null)
		{
			this.media.add(media);
		}
	}

	public boolean isNuked()
	{
		return !nukes.isEmpty();
	}

	public void nuke(String nukeReason)
	{
		nukes.add(new Nuke(nukeReason));
	}

	public void nuke(String nukeReason, Temporal date)
	{
		nukes.add(new Nuke(nukeReason, date));
	}

	public void nukeNow(String nukeReason)
	{
		nukes.add(new Nuke(nukeReason, ZonedDateTime.now()));
	}

	// Object methods
	/**
	 * Compared by their {@link #getMedia() media}, {@link #getTags() tags} and {@link #getGroup() group}.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Release)
		{
			Release o = (Release) obj;
			return media.equals(o.media) && tags.equals(o.tags) && Objects.equals(group, o.group);
		}
		return false;
	}

	public boolean equalsByName(Release other)
	{
		return other == null ? false : name == null ? false : name.equalsIgnoreCase(other.name);
	}

	/**
	 * Calculated from its {@link #getMedia() media}, {@link #getTags() tags} and {@link #getGroup() group}.
	 */
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(45, 7).append(media).append(tags).append(group).toHashCode();
	}

	/**
	 * Two releases are compared by their media, then their tags and then by their groups.
	 */
	@Override
	public int compareTo(Release o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start()
				.compare(media, o.media, Medias.MEDIA_ITERABLE_NAME_COMPARATOR)
				.compare(tags, o.tags, Tag.TAGS_COMPARATOR)
				.compare(group, o.group, Settings.createDefaultOrdering())
				.result();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Release.class)
				.omitNullValues()
				.add("name", name)
				.add("media", Models.nullIfEmpty(media))
				.add("tags", Models.nullIfEmpty(tags))
				.add("group", group)
				.add("languages", Models.nullIfEmpty(languages))
				.add("date", date)
				.add("section", section)
				.add("size", Models.nullIfZero(size))
				.add("fileCount", Models.nullIfZero(fileCount))
				.add("nukes", Models.nullIfEmpty(nukes))
				.add("nfo", nfo)
				.add("nfoLink", nfoLink)
				.add("furtherInfoLinks", furtherInfoLinks)
				.toString();
	}
}
