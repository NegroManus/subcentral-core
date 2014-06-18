package de.subcentral.core.release;

import java.util.List;

import com.google.common.base.Objects;

import de.subcentral.core.media.Media;

public class MediaRelease extends AbstractRelease<Media>
{
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
				.add("nukeReason", nukeReason)
				.add("section", section)
				.add("info", info)
				.add("infoUrl", infoUrl)
				.toString();
	}
}
