package de.subcentral.core.model.release;

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
}
