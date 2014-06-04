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

	public static List<Tag> tagsOf(Collection<String> strings)
	{
		if (strings.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<Tag> tags = new ArrayList<>(strings.size());
		for (String s : strings)
		{
			tags.add(new Tag(s));
		}
		return tags;
	}
}
