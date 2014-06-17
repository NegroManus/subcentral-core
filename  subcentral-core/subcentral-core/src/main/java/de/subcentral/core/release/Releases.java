package de.subcentral.core.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import de.subcentral.core.media.Media;

public class Releases
{

	private Releases()
	{
		// utility class
	}

	public static final Comparator<List<Tag>>	MEDIA_NAME_COMPARATOR	= new TagsComparator();

	static final class TagsComparator implements Comparator<List<Tag>>
	{
		@Override
		public int compare(List<Tag> o1, List<Tag> o2)
		{
			ComparisonChain chain = ComparisonChain.start();
			for (int i = 0; i < Math.max(o1.size(), o2.size()); i++)
			{
				Tag tag1;
				try
				{
					tag1 = o1.get(i);
				}
				catch (IndexOutOfBoundsException e)
				{
					tag1 = null;
				}
				Tag tag2;
				try
				{
					tag2 = o2.get(i);
				}
				catch (IndexOutOfBoundsException e)
				{
					tag2 = null;
				}
				// if tag1 or tag2 is null, then the corresponding list is shorter
				// and if all values before were equal, the shorter list is considered less.
				// Therefore, "nullsFirst()".
				chain.compare(tag1, tag2, Ordering.natural().nullsFirst());
			}
			return chain.result();
		}
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
