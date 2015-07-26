package de.subcentral.core.standardizing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;

public class TagsReplacer implements UnaryOperator<List<Tag>>
{
	private final ImmutableList<Tag>	queryTags;
	private final ImmutableList<Tag>	replacement;
	private final QueryMode				queryMode;
	private final ReplaceMode			replaceMode;
	private final boolean				ignoreOrder;

	public TagsReplacer(List<Tag> queryTags, List<Tag> replacement)
	{
		this(queryTags, replacement, QueryMode.CONTAIN, ReplaceMode.MATCHED_SEQUENCE, false);
	}

	public TagsReplacer(List<Tag> queryTags, List<Tag> replacement, QueryMode queryMode, ReplaceMode replaceMode, boolean ignoreOrder)
	{
		this.queryTags = ImmutableList.copyOf(queryTags);
		this.replacement = ImmutableList.copyOf(replacement);
		this.queryMode = Objects.requireNonNull(queryMode, "queryMode");
		this.replaceMode = Objects.requireNonNull(replaceMode, "replaceMode");
		this.ignoreOrder = ignoreOrder;
	}

	public ImmutableList<Tag> getQueryTags()
	{
		return queryTags;
	}

	public ImmutableList<Tag> getReplacement()
	{
		return replacement;
	}

	public QueryMode getQueryMode()
	{
		return queryMode;
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
		TagUtil.replace(copy, queryTags, replacement, queryMode, replaceMode, ignoreOrder);
		return copy;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(TagsReplacer.class)
				.add("queryTags", queryTags)
				.add("replacement", replacement)
				.add("queryMode", queryMode)
				.add("replaceMode", replaceMode)
				.add("ignoreOrder", ignoreOrder)
				.toString();
	}
}
