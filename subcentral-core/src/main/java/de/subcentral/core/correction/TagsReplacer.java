package de.subcentral.core.correction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;

public class TagsReplacer implements UnaryOperator<List<Tag>>
{
	private final ImmutableList<Tag>	searchTags;
	private final ImmutableList<Tag>	replacement;
	private final SearchMode			searchMode;
	private final ReplaceMode			replaceMode;
	private final boolean				ignoreOrder;

	public TagsReplacer(List<Tag> searchTags, List<Tag> replacement)
	{
		this(searchTags, replacement, SearchMode.CONTAIN, ReplaceMode.MATCHED_SEQUENCE, false);
	}

	public TagsReplacer(List<Tag> searchTags, List<Tag> replacement, SearchMode searchMode, ReplaceMode replaceMode, boolean ignoreOrder)
	{
		this.searchTags = ImmutableList.copyOf(searchTags);
		this.replacement = ImmutableList.copyOf(replacement);
		this.searchMode = Objects.requireNonNull(searchMode, "searchMode");
		this.replaceMode = Objects.requireNonNull(replaceMode, "replaceMode");
		this.ignoreOrder = ignoreOrder;
	}

	public ImmutableList<Tag> getSearchTags()
	{
		return searchTags;
	}

	public ImmutableList<Tag> getReplacement()
	{
		return replacement;
	}

	public SearchMode getSearchMode()
	{
		return searchMode;
	}

	public ReplaceMode getReplaceMode()
	{
		return replaceMode;
	}

	public boolean getIgnoreOrder()
	{
		return ignoreOrder;
	}

	@Override
	public List<Tag> apply(List<Tag> tags)
	{
		switch (searchMode)
		{
			case CONTAIN:
				switch (replaceMode)
				{
					case COMPLETE_LIST:
						if (ignoreOrder)
						{
							if (tags.containsAll(searchTags))
							{
								return replacement;
							}
						}
						else
						{
							if (TagUtil.containsSequence(tags, searchTags))
							{
								return replacement;
							}
						}
						break;
					case MATCHED_SEQUENCE:
						return replaceSequences(tags);
				}
				break;
			case EQUAL:
				if (ignoreOrder)
				{
					if (TagUtil.equalsIgnoreOrder(tags, searchTags))
					{
						return replacement;
					}
				}
				else
				{
					if (tags.equals(searchTags))
					{
						return replacement;
					}
				}
		}
		return tags;
	}

	public List<Tag> replaceSequences(List<Tag> tags)
	{
		List<Tag> result = tags;
		for (int i = 0; i < result.size() && i + searchTags.size() <= result.size(); i++)
		{
			List<Tag> sublist = result.subList(i, i + searchTags.size());
			if (ignoreOrder ? TagUtil.equalsIgnoreOrder(sublist, searchTags) : sublist.equals(searchTags))
			{
				if (result == tags)
				{
					result = new ArrayList<>(tags);
					result.subList(i, i + searchTags.size()).clear();
				}
				else
				{
					sublist.clear();
				}
				result.addAll(i, replacement);
			}
		}
		return result;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(TagsReplacer.class)
				.add("searchTags", searchTags)
				.add("replacement", replacement)
				.add("searchMode", searchMode)
				.add("replaceMode", replaceMode)
				.add("ignoreOrder", ignoreOrder)
				.toString();
	}
}
