package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class TagUtil
{
	public static enum SearchMode
	{
		CONTAIN, EQUAL
	};

	public static enum ReplaceMode
	{
		COMPLETE_LIST, MATCHED_SEQUENCE
	};

	public static List<Tag> getMetaTags(List<Tag> tags, Collection<Tag> metaTags)
	{
		List<Tag> containedMetaTags = new ArrayList<>(4);
		transferMetaTags(tags, containedMetaTags, metaTags);
		return containedMetaTags;
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

	public static boolean replaceRetainingMetaTags(Release rls, List<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		return replaceRetainingMetaTags(rls.getTags(), newTags, metaTagsToRetain);
	}

	public static boolean replaceRetainingMetaTags(List<Tag> tags, List<Tag> newTags, Collection<Tag> metaTagsToRetain)
	{
		// no shortcut || because both operations need to be performed
		return (tags.retainAll(metaTagsToRetain) | tags.addAll(newTags));
	}

	public static boolean replace(List<Tag> tags, List<Tag> queryTags, List<Tag> replacement, SearchMode queryMode, ReplaceMode replaceMode, boolean ignoreOrder)
	{
		switch (queryMode)
		{
			case CONTAIN:
				switch (replaceMode)
				{
					case COMPLETE_LIST:
						if (ignoreOrder)
						{
							if (tags.containsAll(queryTags))
							{
								tags.clear();
								tags.addAll(replacement);
								return true;
							}
						}
						else
						{
							if (containsSequence(tags, queryTags))
							{
								tags.clear();
								tags.addAll(replacement);
								return true;
							}
						}
						break;
					case MATCHED_SEQUENCE:
						return replaceSequences(tags, queryTags, replacement, ignoreOrder, false);
				}
				break;
			case EQUAL:
				if (ignoreOrder)
				{
					if (equalsIgnoreOrder(tags, queryTags))
					{
						tags.clear();
						tags.addAll(replacement);
						return true;
					}
				}
				else
				{
					if (tags.equals(queryTags))
					{
						tags.clear();
						tags.addAll(replacement);
						return true;
					}
				}
		}
		return false;
	}

	public static boolean replaceSequences(List<Tag> tags, List<Tag> tagsToReplace, List<Tag> replacement)
	{
		return replaceSequences(tags, tagsToReplace, replacement, false, false);
	}

	public static boolean replaceSequences(List<Tag> tags, List<Tag> tagSequenceToReplace, List<Tag> replacementSequence, boolean ignoreOrder, boolean onlyReplaceFirst)
	{
		boolean changed = false;
		for (int i = 0; i < tags.size() && i + tagSequenceToReplace.size() <= tags.size(); i++)
		{
			List<Tag> sublist = tags.subList(i, i + tagSequenceToReplace.size());
			if (ignoreOrder ? equalsIgnoreOrder(sublist, tagSequenceToReplace) : sublist.equals(tagSequenceToReplace))
			{
				sublist.clear();
				tags.addAll(i, replacementSequence);
				changed = true;
				if (onlyReplaceFirst)
				{
					return true;
				}
			}
		}
		return changed;
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

	private TagUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
