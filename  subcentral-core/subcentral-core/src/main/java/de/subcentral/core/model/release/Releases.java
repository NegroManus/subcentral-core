package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import de.subcentral.core.util.ListComparator;

public class Releases
{
	public static final Comparator<List<Tag>>	TAGS_COMPARATOR	= ListComparator.create();

	private Releases()
	{
		// utility class
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

	public static void nuke(Release rls, String nukeReason)
	{
		rls.getNukes().add(new Nuke(nukeReason));
	}
}
