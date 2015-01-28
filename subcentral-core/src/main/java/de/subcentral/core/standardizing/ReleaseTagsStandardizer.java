package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtils;

public class ReleaseTagsStandardizer implements Standardizer<Release>
{
	public static enum QueryMode
	{
		CONTAINS, MATCHES
	};

	public static enum ReplaceWith
	{
		COMPLETE_LIST, MATCHED_SEQUENCE
	};

	private final ImmutableList<Tag>	queryTags;
	private final ImmutableList<Tag>	replacement;
	private final QueryMode				queryMode;
	private final ReplaceWith			replaceWith;
	private final boolean				ignoreOrder;

	public ReleaseTagsStandardizer(List<Tag> queryTags, List<Tag> replacement, QueryMode queryMode, ReplaceWith replaceWith, boolean ignoreOrder)
	{
		this.queryTags = ImmutableList.copyOf(queryTags);
		this.replacement = ImmutableList.copyOf(replacement);
		this.queryMode = Objects.requireNonNull(queryMode, "queryMode");
		this.replaceWith = Objects.requireNonNull(replaceWith, "replaceWith");
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

	public ReplaceWith getReplaceWith()
	{
		return replaceWith;
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
		switch (queryMode)
		{
			case CONTAINS:
				switch (replaceWith)
				{
					case COMPLETE_LIST:
						if (ignoreOrder)
						{
							if (tags.containsAll(queryTags))
							{
								rls.setTags(replacement);
							}
						}
						else
						{
							if (TagUtils.containsSequence(tags, queryTags))
							{
								rls.setTags(replacement);
							}
						}
						break;
					case MATCHED_SEQUENCE:
						TagUtils.replace(tags, queryTags, replacement, ignoreOrder, false);
						break;
				}
				break;
			case MATCHES:
				if (ignoreOrder)
				{
					if (TagUtils.equalsIgnoreOrder(tags, queryTags))
					{
						rls.setTags(replacement);
					}
				}
				else
				{
					if (tags.equals(queryTags))
					{
						rls.setTags(replacement);
					}
				}
		}
		if (oldTags.equals(tags))
		{
			return ImmutableList.of();
		}
		return ImmutableList.of(new StandardizingChange(rls, Release.PROP_TAGS.getPropName(), oldTags, tags));
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
					&& replaceWith.equals(o.replaceWith);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(41, 93).append(queryTags).append(replacement).append(queryMode).append(replaceWith).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(ReleaseTagsStandardizer.class).add("queryTags", queryTags).add("replacement", replacement).toString();
	}
}
