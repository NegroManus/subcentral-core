package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class TagUtils
{
	public static boolean replaceTags(Release rls, List<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		return replaceTags(rls.getTags(), newTags, metaTagsToRetain);
	}

	public static boolean replaceTags(List<Tag> tags, List<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		// no shortcut (||) because both operations need to be performed
		return (tags.retainAll(metaTagsToRetain) | tags.addAll(newTags));
	}

	public static void transferMetaTags(List<Tag> sourceTags, List<Tag> targetTags, Collection<Tag> metaTags)
	{
		// iterate the source tags in reverse order so that the order in the target tags is correct
		for (int i = sourceTags.size() - 1; i >= 0; i--)
		{
			Tag sourceTag = sourceTags.get(i);
			if (metaTags.contains(sourceTag))
			{
				targetTags.add(0, sourceTag);
			}
		}
	}

	public static boolean containsAllIgnoreMetaTags(List<Tag> tags1, List<Tag> tags2, Collection<Tag> metaTagsToIgnore)
	{
		return copyAndRemoveMetaTags(tags1, metaTagsToIgnore).containsAll(copyAndRemoveMetaTags(tags2, metaTagsToIgnore));
	}

	public static boolean equalsIgnoreMetaTags(List<Tag> tags1, List<Tag> tags2, Collection<Tag> metaTagsToIgnore)
	{
		return copyAndRemoveMetaTags(tags1, metaTagsToIgnore).equals(copyAndRemoveMetaTags(tags2, metaTagsToIgnore));
	}

	private static List<Tag> copyAndRemoveMetaTags(List<Tag> tags, Collection<Tag> metaTagsToRemove)
	{
		if (tags.isEmpty())
		{
			return ImmutableList.of();
		}
		List<Tag> tagsWithoutMetaTags = new ArrayList<>(tags);
		tagsWithoutMetaTags.removeAll(metaTagsToRemove);
		return tagsWithoutMetaTags;
	}

	public static List<Tag> orderedCopy(List<Tag> tags)
	{
		if (tags.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<Tag> ordered = new ArrayList<>(tags);
		ordered.sort(null);
		return ordered;
	}

	public static boolean equalsIgnoreOrdering(List<Tag> tags1, List<Tag> tags2)
	{
		return orderedCopy(tags1).equals(orderedCopy(tags2));
	}

	public static void replace(List<Tag> tags, List<Tag> target, List<Tag> replacement, boolean ignoreOrdering, boolean onlyReplaceFirst)
	{
		for (int i = 0; i < tags.size() && i + target.size() <= tags.size(); i++)
		{
			List<Tag> sublist = tags.subList(i, i + target.size());
			if (ignoreOrdering ? equalsIgnoreOrdering(sublist, target) : sublist.equals(target))
			{
				sublist.clear();
				tags.addAll(i, replacement);
				if (onlyReplaceFirst)
				{
					break;
				}
			}
		}
	}

	public static void main(String[] args)
	{
		List<Tag> tags = Tag.list("720p", "WEB", "DL", "H", "264", "DD5", "1", "WEB", "DL", "Xx264");

		replace(tags, Tag.list("H", "264"), Tag.list("H.264"), false, false);
		replace(tags, Tag.list("DL", "WEB"), Tag.list("WEB-DL"), true, false);
		replace(tags, Tag.list("DD5", "1"), Tag.list("DD5.1"), false, false);
		replace(tags, Tag.list("xx264"), Tag.list("X264"), false, false);
		System.out.println(tags);
	}

	private TagUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
