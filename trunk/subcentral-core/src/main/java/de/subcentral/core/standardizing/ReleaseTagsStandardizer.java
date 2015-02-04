package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtils;
import de.subcentral.core.metadata.release.TagUtils.QueryMode;
import de.subcentral.core.metadata.release.TagUtils.ReplaceMode;

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
	public List<StandardizingChange> standardize(Release rls) throws StandardizingException
	{
		if (rls == null || rls.getTags().isEmpty())
		{
			return ImmutableList.of();
		}
		List<Tag> tags = rls.getTags();
		ImmutableList<Tag> oldTags = ImmutableList.copyOf(tags);
		boolean changed = TagUtils.replace(tags, queryTags, replacement, queryMode, replaceMode, ignoreOrder);
		if (changed)
		{
			return ImmutableList.of(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, Tag.immutableCopy(tags)));
		}
		return ImmutableList.of();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof ReleaseTagsStandardizer)
		{
			ReleaseTagsStandardizer o = (ReleaseTagsStandardizer) obj;
			return queryTags.equals(o.queryTags) && replacement.equals(o.replacement) && queryMode.equals(o.queryMode)
					&& replaceMode.equals(o.replaceMode) && ignoreOrder == o.ignoreOrder;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(15, 93).append(queryTags)
				.append(replacement)
				.append(queryMode)
				.append(replaceMode)
				.append(ignoreOrder)
				.toHashCode();
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
