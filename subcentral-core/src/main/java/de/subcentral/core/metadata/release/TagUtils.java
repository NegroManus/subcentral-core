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
		// no shortcut || because both operations need to be performed
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

	public static boolean equalsIgnoreOrder(List<Tag> tags1, List<Tag> tags2)
	{
		if (tags1.size() != tags2.size())
		{
			return false;
		}
		return tags1.containsAll(tags2) && tags2.containsAll(tags1);
	}

	public static boolean replace(List<Tag> tags, List<Tag> tagsToReplace, List<Tag> replacement)
	{
		return replace(tags, tagsToReplace, replacement, false, false);
	}

	public static boolean containsSequence(List<Tag> tags, List<Tag> tagSequenceToFind)
	{
		for (int i = 0; i < tags.size() && i + tagSequenceToFind.size() <= tags.size(); i++)
		{
			List<Tag> sublist = tags.subList(i, i + tagSequenceToFind.size());
			if (equalsIgnoreOrder(sublist, tagSequenceToFind))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean replace(List<Tag> tags, List<Tag> tagsToReplace, List<Tag> replacement, boolean ignoreOrder, boolean onlyReplaceFirst)
	{
		boolean changed = false;
		for (int i = 0; i < tags.size() && i + tagsToReplace.size() <= tags.size(); i++)
		{
			List<Tag> sublist = tags.subList(i, i + tagsToReplace.size());
			if (ignoreOrder ? equalsIgnoreOrder(sublist, tagsToReplace) : sublist.equals(tagsToReplace))
			{
				sublist.clear();
				tags.addAll(i, replacement);
				changed = true;
				if (onlyReplaceFirst)
				{
					return true;
				}
			}
		}
		return changed;
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
