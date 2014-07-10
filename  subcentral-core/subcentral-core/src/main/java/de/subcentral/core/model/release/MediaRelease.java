package de.subcentral.core.model.release;

import java.util.List;

import com.google.common.base.Objects;

import de.subcentral.core.model.media.Media;

public class MediaRelease extends AbstractRelease<Media>
{
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
