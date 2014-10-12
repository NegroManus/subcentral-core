package de.subcentral.core.model.release;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.model.media.Media;

public class Compatibility
{
	public static enum Scope
	{
		ALWAYS, IF_EXISTS;
	}

	public static enum MatchDirection
	{
		NONE, FORWARD, BACKWARD;
	}

	private final Predicate<List<Media>>	mediaFilter;
	private final Group						sourceGroup;
	private final List<Tag>					sourceTags;
	private final Group						compatibleGroup;
	private final List<Tag>					compatibleTags;
	private final Scope						scope;
	private final boolean					bidirectional;

	public Compatibility(Predicate<List<Media>> mediaFilter, Group sourceGroup, List<Tag> sourceTags, Group compatibleGroup,
			List<Tag> compatibleTags, Scope scope, boolean bidirectional)
	{
		if (sourceGroup == null && sourceTags == null)
		{
			throw new IllegalArgumentException("Either sourceGroup or sourceTags must not be null");
		}
		if (compatibleGroup == null && compatibleTags == null)
		{
			throw new IllegalArgumentException("Either compatibleGroup or compatibleTags must not be null");
		}
		this.mediaFilter = mediaFilter;
		this.sourceGroup = sourceGroup;
		this.sourceTags = sourceTags;
		this.compatibleGroup = compatibleGroup;
		this.compatibleTags = compatibleTags;
		this.scope = Objects.requireNonNull(scope, "scope");
		this.bidirectional = bidirectional;
	}

	public Predicate<List<Media>> getMediaFilter()
	{
		return mediaFilter;
	}

	public Group getSourceGroup()
	{
		return sourceGroup;
	}

	public List<Tag> getSourceTags()
	{
		return sourceTags;
	}

	public Group getCompatibleGroup()
	{
		return compatibleGroup;
	}

	public List<Tag> getCompatibleTags()
	{
		return compatibleTags;
	}

	public Scope getScope()
	{
		return scope;
	}

	public boolean isBidirectional()
	{
		return bidirectional;
	}

	public MatchDirection match(Release rls)
	{
		if (rls == null)
		{
			return MatchDirection.NONE;
		}
		if (mediaFilter == null || mediaFilter.test(rls.getMedia()))
		{
			if ((sourceGroup == null || sourceGroup.equals(rls.getGroup())) && (sourceTags == null || sourceTags.equals(rls.getTags())))
			{
				return MatchDirection.FORWARD;
			}
			if (bidirectional && (compatibleGroup == null || compatibleGroup.equals(rls.getGroup()))
					&& (compatibleTags == null || compatibleTags.equals(rls.getTags())))
			{
				return MatchDirection.BACKWARD;
			}
		}
		return MatchDirection.NONE;
	}

	public boolean matchesCompatible(Release compatibleRls, MatchDirection applicability)
	{
		if (compatibleRls == null)
		{
			return false;
		}
		switch (applicability)
		{
			case FORWARD:
				return (compatibleGroup == null || compatibleGroup.equals(compatibleRls.getGroup()))
						&& (compatibleTags == null || compatibleTags.equals(compatibleRls.getTags()));
			case BACKWARD:
				return (sourceGroup == null || sourceGroup.equals(compatibleRls.getGroup()))
						&& (sourceTags == null || sourceTags.equals(compatibleRls.getTags()));
			case NONE:
				return false;
			default:
				return false;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass()))
		{
			Compatibility o = (Compatibility) obj;
			return new EqualsBuilder().append(mediaFilter, o.mediaFilter)
					.append(sourceGroup, o.sourceGroup)
					.append(sourceTags, o.sourceTags)
					.append(compatibleGroup, o.compatibleGroup)
					.append(compatibleTags, o.compatibleTags)
					.append(scope, o.scope)
					.append(bidirectional, o.bidirectional)
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(13, 67).append(mediaFilter)
				.append(sourceGroup)
				.append(sourceTags)
				.append(compatibleGroup)
				.append(compatibleTags)
				.append(scope)
				.append(bidirectional)
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Compatibility.class)
				.omitNullValues()
				.add("mediaFilter", mediaFilter)
				.add("sourceGroup", sourceGroup)
				.add("sourceTags", sourceTags)
				.add("compatibleGroup", compatibleGroup)
				.add("compatibleTags", compatibleTags)
				.add("scope", scope)
				.add("bidirectional", bidirectional)
				.toString();
	}
}
