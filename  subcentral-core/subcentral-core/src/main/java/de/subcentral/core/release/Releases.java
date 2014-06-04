package de.subcentral.core.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.subcentral.core.media.Media;

public class Releases
{

	private Releases()
	{
		// utility class
	}

	public static MediaRelease newMediaRelease(Media material, Group group, List<Tag> tags)
	{
		MediaRelease rls = new MediaRelease();
		rls.setMaterial(material);
		rls.setGroup(group);
		rls.setTags(tags);
		return rls;
	}

	public static MediaRelease newMediaRelease(Media material, String group, String... tags)
	{
		MediaRelease rls = new MediaRelease();
		rls.setMaterial(material);
		if (group != null)
		{
			rls.setGroup(new Group(group));
		}
		rls.setTags(tags(tags));
		return rls;
	}

	public static List<Tag> tags(Collection<String> tags)
	{
		if (tags.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<Tag> tagList = new ArrayList<>(tags.size());
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}

	public static List<Tag> tags(String... tags)
	{
		if (tags.length == 0)
		{
			return new ArrayList<>(0);
		}
		List<Tag> tagList = new ArrayList<>(tags.length);
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}
}
