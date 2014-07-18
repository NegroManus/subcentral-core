package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Objects;

import de.subcentral.core.model.Prop;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.util.SimplePropDescriptor;

public class MediaRelease extends AbstractRelease<Media>
{
	public static final SimplePropDescriptor	PROP_NAME			= new SimplePropDescriptor(MediaRelease.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_MATERIALS		= new SimplePropDescriptor(MediaRelease.class, Prop.MATERIALS);
	public static final SimplePropDescriptor	PROP_GROUP			= new SimplePropDescriptor(MediaRelease.class, Prop.GROUP);
	public static final SimplePropDescriptor	PROP_TAGS			= new SimplePropDescriptor(MediaRelease.class, Prop.TAGS);
	public static final SimplePropDescriptor	PROP_DATE			= new SimplePropDescriptor(MediaRelease.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_SECTION		= new SimplePropDescriptor(MediaRelease.class, Prop.SECTION);
	public static final SimplePropDescriptor	PROP_SIZE			= new SimplePropDescriptor(MediaRelease.class, Prop.SIZE);
	public static final SimplePropDescriptor	PROP_NUKE_REASON	= new SimplePropDescriptor(MediaRelease.class, Prop.NUKE_REASON);
	public static final SimplePropDescriptor	PROP_INFO			= new SimplePropDescriptor(MediaRelease.class, Prop.INFO);
	public static final SimplePropDescriptor	PROP_INFO_URL		= new SimplePropDescriptor(MediaRelease.class, Prop.INFO_URL);
	public static final SimplePropDescriptor	PROP_SOURCE			= new SimplePropDescriptor(MediaRelease.class, Prop.SOURCE);
	public static final SimplePropDescriptor	PROP_SOURCE_URL		= new SimplePropDescriptor(MediaRelease.class, Prop.SOURCE_URL);

	public static final String					UNKNOWN_NUKE_REASON	= "";

	private String								name;
	private List<Tag>							tags				= new ArrayList<>(5);
	private Group								group;
	private String								section;
	private String								info;
	private String								infoUrl;
	private String								source;
	private String								sourceUrl;

	public static MediaRelease create(Media media, String group, String... tags)
	{
		return create(null, media, group, tags);
	}

	public static MediaRelease create(String name, Media media, String group, String... tags)
	{
		MediaRelease rls = new MediaRelease();
		rls.setName(name);
		rls.setMaterial(media);
		if (group != null)
		{
			rls.setGroup(new Group(group));
		}
		rls.setTags(Releases.tags(tags));
		return rls;
	}

	public MediaRelease()
	{

	}

	public MediaRelease(String name)
	{
		this.name = name;
	}

	public MediaRelease(String name, Media material, List<Tag> tags, Group group)
	{
		this.name = name;
		setMaterial(material);
		setTags(tags);
		this.group = group;
	}

	public MediaRelease(String name, List<Media> materials, List<Tag> tags, Group group)
	{
		this.name = name;
		setMaterials(materials);
		setTags(tags);
		this.group = group;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("materials", materials)
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
