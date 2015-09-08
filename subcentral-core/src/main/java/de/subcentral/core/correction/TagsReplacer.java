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
		List<Tag> copy = new ArrayList<>(tags);
		TagUtil.replace(copy, searchTags, replacement, searchMode, replaceMode, ignoreOrder);
		return copy;
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
