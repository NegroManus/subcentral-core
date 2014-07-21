package de.subcentral.core.model.release;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Models;
import de.subcentral.core.model.Prop;
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
	public static final SimplePropDescriptor	PROP_NAME			= new SimplePropDescriptor(Release.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_MEDIA			= new SimplePropDescriptor(Release.class, Prop.MEDIA);
	public static final SimplePropDescriptor	PROP_TAGS			= new SimplePropDescriptor(Release.class, Prop.TAGS);
	public static final SimplePropDescriptor	PROP_GROUP			= new SimplePropDescriptor(Release.class, Prop.GROUP);
	public static final SimplePropDescriptor	PROP_SECTION		= new SimplePropDescriptor(Release.class, Prop.SECTION);
	public static final SimplePropDescriptor	PROP_DATE			= new SimplePropDescriptor(Release.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_SIZE			= new SimplePropDescriptor(Release.class, Prop.SIZE);
	public static final SimplePropDescriptor	PROP_NUKE_REASON	= new SimplePropDescriptor(Release.class, Prop.NUKE_REASON);
	public static final SimplePropDescriptor	PROP_INFO			= new SimplePropDescriptor(Release.class, Prop.INFO);
	public static final SimplePropDescriptor	PROP_INFO_URL		= new SimplePropDescriptor(Release.class, Prop.INFO_URL);
	public static final SimplePropDescriptor	PROP_SOURCE			= new SimplePropDescriptor(Release.class, Prop.SOURCE);
	public static final SimplePropDescriptor	PROP_SOURCE_URL		= new SimplePropDescriptor(Release.class, Prop.SOURCE_URL);

	public static final String					UNKNOWN_NUKE_REASON	= "";

	private String								name;
	// In 99,9% of the cases, there is only one Media per Release
	private List<Media>							media				= new ArrayList<>(1);
	// Normally there are 1 to 5 Tags per Release
	private List<Tag>							tags				= new ArrayList<>(5);
	private Group								group;
	private String								section;
	private Temporal							date;
	private long								size;
	private String								nukeReason;
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
		rls.setSingleMedia(media);
		if (group != null)
		{
			rls.setGroup(new Group(group));
		}
		rls.setTags(Releases.tags(tags));
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
		setSingleMedia(media);
		setTags(tags);
		this.group = group;
	}

	public Release(String name, List<Media> media, List<Tag> tags, Group group)
	{
		this.name = name;
		setMedia(media);
		setTags(tags);
		this.group = group;
	}

	/**
	 * 
	 * @return The unique name of this release (e.g. "Psych.S08E01.HDTV.x264-EXCELLENCE").
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
	 * 
	 * @return The contained media. For the most cases, a Release contains only one media. But, for example, multiple
	 *         {@link de.subcentral.core.model.media.Episode Episodes} are sometimes packed into one Release.
	 */

	public List<Media> getMedia()
	{
		return media;
	}

	public void setMedia(List<Media> media)
	{
		Validate.notNull(media, "media cannot be null");
		this.media = media;
	}

	/**
	 * @return The release tags (XviD, WEB-DL, DD5.1, 720p, ...).
	 */
	public List<Tag> getTags()
	{
		return tags;
	}

	public void setTags(List<Tag> tags)
	{
		Validate.notNull(tags, "tags cannot be null");
		this.tags = tags;
	}

	/**
	 * 
	 * @return The release group.
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
	 * 
	 * @return The release section.
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
	 * 
	 * @return The release date.
	 */

	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		Models.validateDateClass(date);
		this.date = date;
	}

	/**
	 * 
	 * @return The file size in bytes.
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
	 * 
	 * @return The nuke reason. If <code>null</code>, the release is not nuked. Can be an empty String (<code>""</code>) if the release is nuked, but
	 *         the reason is unknown.
	 */

	public String getNukeReason()
	{
		return nukeReason;
	}

	public void setNukeReason(String nukeReason)
	{
		this.nukeReason = nukeReason;
	}

	/**
	 * 
	 * @return Information about this release (typically, the text of the nfo file).
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
	 * @return The URL of the nfo file.
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
	 * @return The name of the source of the information about this release.
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
	 * @return The URL of the source of the information about this release.
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
		this.media = new ArrayList<>(1);
		if (media != null)
		{
			this.media.add(media);
		}
	}

	public boolean isNuked()
	{
		return nukeReason != null;
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
			Release o = (Release) obj;
			return Objects.equal(getName(), o.getName());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(45, 3).append(getName()).toHashCode();
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
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("media", media)
				.add("tags", tags)
				.add("group", group)
				.add("date", date)
				.add("section", section)
				.add("size", size)
				.add("nukeReason", nukeReason)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.add("source", source)
				.add("sourceUrl", sourceUrl)
				.toString();
	}
}
