package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;

public class ReleaseTagsStandardizer implements Standardizer<Release>
{
	private final ImmutableList<Tag>	queryTags;
	private final ImmutableList<Tag>	replacement;
	private final QueryMode				queryMode;
	private final ReplaceMode			replaceMode;
	private final boolean				ignoreOrder;

	public ReleaseTagsStandardizer(List<Tag> queryTags, List<Tag> replacement)
	{
		this(queryTags, replacement, QueryMode.CONTAINS, ReplaceMode.MATCHED_SEQUENCE, false);
	}

	public ReleaseTagsStandardizer(List<Tag> queryTags, List<Tag> replacement, QueryMode queryMode, ReplaceMode replaceMode, boolean ignoreOrder)
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
	public void standardize(Release rls, List<StandardizingChange> changes) throws StandardizingException
	{
		if (rls == null || rls.getTags().isEmpty())
		{
			return;
		}
		List<Tag> tags = rls.getTags();
		ImmutableList<Tag> oldTags = ImmutableList.copyOf(tags);
		boolean changed = TagUtil.replace(tags, queryTags, replacement, queryMode, replaceMode, ignoreOrder);
		if (changed)
		{
			changes.add(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, Tag.immutableCopy(tags)));
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(ReleaseTagsStandardizer.class)
				.add("queryTags", queryTags)
				.add("replacement", replacement)
				.add("queryMode", queryMode)
				.add("replaceMode", replaceMode)
				.add("ignoreOrder", ignoreOrder)
				.toString();
	}
}
