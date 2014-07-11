package de.subcentral.core.model.release;

import java.util.List;

import com.google.common.base.Objects;

import de.subcentral.core.model.media.Media;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class MediaRelease extends AbstractRelease<Media>
{
	public static final SimplePropertyDescriptor	PROP_NAME			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_MATERIALS		= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_MATERIALS);
	public static final SimplePropertyDescriptor	PROP_GROUP			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_GROUP);
	public static final SimplePropertyDescriptor	PROP_TAGS			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_TAGS);
	public static final SimplePropertyDescriptor	PROP_DATE			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_SECTION		= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_SECTION);
	public static final SimplePropertyDescriptor	PROP_SIZE			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_SIZE);
	public static final SimplePropertyDescriptor	PROP_NUKE_REASON	= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_NUKE_REASON);
	public static final SimplePropertyDescriptor	PROP_INFO			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_INFO);
	public static final SimplePropertyDescriptor	PROP_INFO_URL		= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_INFO_URL);
	public static final SimplePropertyDescriptor	PROP_SOURCE			= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_SOURCE);
	public static final SimplePropertyDescriptor	PROP_SOURCE_URL		= new SimplePropertyDescriptor(MediaRelease.class, PROP_NAME_SOURCE_URL);

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

	public MediaRelease(String name, Media material, Group group, List<Tag> tags)
	{
		this.name = name;
		setMaterial(material);
		this.group = group;
		setTags(tags);
	}

	public MediaRelease(String name, List<Media> materials, Group group, List<Tag> tags)
	{
		this.name = name;
		setMaterials(materials);
		this.group = group;
		setTags(tags);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("materials", materials)
				.add("group", group)
				.add("tags", tags)
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
