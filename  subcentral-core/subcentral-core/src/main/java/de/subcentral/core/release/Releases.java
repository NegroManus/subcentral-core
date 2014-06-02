package de.subcentral.core.release;

import java.util.ArrayList;
import java.util.List;

public class Releases
{

	private Releases()
	{
		// utility class
	}

	public static List<Tag> buildTagsFromStringList(List<String> strings)
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
