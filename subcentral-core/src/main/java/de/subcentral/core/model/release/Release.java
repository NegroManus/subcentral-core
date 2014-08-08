package de.subcentral.core.model.release;

import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.model.media.Media;
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
	public static final SimplePropDescriptor	PROP_NAME		= new SimplePropDescriptor(Release.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_MEDIA		= new SimplePropDescriptor(Release.class, PropNames.MEDIA);
	public static final SimplePropDescriptor	PROP_TAGS		= new SimplePropDescriptor(Release.class, PropNames.TAGS);
	public static final SimplePropDescriptor	PROP_LANGUAGES	= new SimplePropDescriptor(Release.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_GROUP		= new SimplePropDescriptor(Release.class, PropNames.GROUP);
	public static final SimplePropDescriptor	PROP_SECTION	= new SimplePropDescriptor(Release.class, PropNames.SECTION);
	public static final SimplePropDescriptor	PROP_DATE		= new SimplePropDescriptor(Release.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_SIZE		= new SimplePropDescriptor(Release.class, PropNames.SIZE);
	public static final SimplePropDescriptor	PROP_FILE_COUNT	= new SimplePropDescriptor(Release.class, PropNames.FILE_COUNT);
	public static final SimplePropDescriptor	PROP_NUKES		= new SimplePropDescriptor(Release.class, PropNames.NUKES);
	public static final SimplePropDescriptor	PROP_INFO		= new SimplePropDescriptor(Release.class, PropNames.INFO);
	public static final SimplePropDescriptor	PROP_INFO_URL	= new SimplePropDescriptor(Release.class, PropNames.INFO_URL);
	public static final SimplePropDescriptor	PROP_SOURCE		= new SimplePropDescriptor(Release.class, PropNames.SOURCE);
	public static final SimplePropDescriptor	PROP_SOURCE_URL	= new SimplePropDescriptor(Release.class, PropNames.SOURCE_URL);

	private String								name;
	// In 99,9% of the cases, there is only one Media per Release
	private List<Media>							media			= new ArrayList<>(1);
	// Normally there are 2 to 4 Tags per Release
	private List<Tag>							tags			= new ArrayList<>(4);
	private List<String>						languages		= new ArrayList<>(1);
	private Group								group;
	private String								section;
	private Temporal							date;
	private long								size;
	private int									fileCount;
	private List<Nuke>							nukes			= new ArrayList<>(0);
	private String								info;
	private String								infoUrl;
	private String								source;
	private String								sourceUrl;

	public static Release create(Media media, String group, String... tags)
	{
		return create(null, media, group, tags);
	}

	public static Release create(String name, Media media, String group, String... tags)
	{
		Release rls = new Release();
		rls.setName(name);
		rls.getMedia().add(media);
		if (group != null)
		{
			rls.setGroup(new Group(group));
		}
		rls.getTags().addAll(Tag.tags(tags));
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
		Validate.notNull(media, "media cannot be null");
		this.media.clear();
		this.media.addAll(media);
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
		this.languages = languages;
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
		Validate.notNull(tags, "tags cannot be null");
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
		Models.validateTemporalClass(date);
		this.date = date;
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

	public void setNukes(List<Nuke> nukes)
	{
		Validate.notNull(nukes, "nukes cannot be null");
		this.nukes.clear();
		this.nukes.addAll(nukes);
	}

	/**
	 * Information about this release / the release notes (typically, the text of the nfo file).
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
	 * An URL pointing to a file or a website providing the information about this release.
	 * 
	 * @return the information URL
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
	 * The name of the source of this release. Typically the site which released this release.
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
	 * An URL pointing to the source of this release. Typically the site which released this release.
	 * 
	 * @return the source URL
	 */
	public String getSourceUrl()
	{
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl)
	{
		this.sourceUrl = sourceUrl;
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
		Validate.notNull(media, "media cannot be null");
		this.media.clear();
		this.media.add(media);
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
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && Release.class.equals(obj.getClass()))
		{
			return Objects.equal(name, ((Release) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(45, 3).append(name).toHashCode();
	}

	@Override
	public int compareTo(Release o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(Release.class)
				.omitNullValues()
				.add("name", name)
				.add("media", Models.nullIfEmpty(media))
				.add("languages", Models.nullIfEmpty(languages))
				.add("tags", Models.nullIfEmpty(tags))
				.add("group", group)
				.add("date", date)
				.add("section", section)
				.add("size", size)
				.add("fileCount", fileCount)
				.add("nukes", Models.nullIfEmpty(nukes))
				.add("info", info)
				.add("infoUrl", infoUrl)
				.add("source", source)
				.add("sourceUrl", sourceUrl)
				.toString();
	}
}
