package de.subcentral.core.model.release;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

public class Compatibility
{
	public static enum Scope
	{
		IF_EXISTS, ALWAYS;
	}

	public static enum MatchDirection
	{
		NONE, FORWARD, BACKWARD;
	}

	private final Group		sourceGroup;
	private final List<Tag>	sourceTags;
	private final Group		compatibleGroup;
	private final List<Tag>	compatibleTags;
	private final Scope		scope;
	private final boolean	bidirectional;

	public Compatibility(Group sourceGroup, Group compatibleGroup, Scope scope, boolean bidirectional)
	{
		this(sourceGroup, null, compatibleGroup, null, scope, bidirectional);
	}

	public Compatibility(Group sourceGroup, List<Tag> sourceTags, Group compatibleGroup, List<Tag> compatibleTags, Scope scope, boolean bidirectional)
	{
		if (sourceGroup == null && sourceTags == null)
		{
			throw new IllegalArgumentException("either sourceGroup or sourceTags must be non-null");
		}
		if (compatibleGroup == null && compatibleTags == null)
		{
			throw new IllegalArgumentException("either compatibleGroup or compatibleTags must be non-null");
		}
		this.sourceGroup = sourceGroup;
		this.sourceTags = sourceTags;
		this.compatibleGroup = compatibleGroup;
		this.compatibleTags = compatibleTags;
		this.scope = Objects.requireNonNull(scope, "scope");
		this.bidirectional = bidirectional;
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
		if ((sourceGroup == null || sourceGroup.equals(rls.getGroup())) && (sourceTags == null || sourceTags.equals(rls.getTags())))
		{
			return MatchDirection.FORWARD;
		}
		if (bidirectional && (compatibleGroup == null || compatibleGroup.equals(rls.getGroup()))
				&& (compatibleTags == null || compatibleTags.equals(rls.getTags())))
		{
			return MatchDirection.BACKWARD;
		}
		return MatchDirection.NONE;
	}

	public boolean matchesCompatible(Release compatibleRls, MatchDirection matchDirection)
	{
		if (compatibleRls == null)
		{
			return false;
		}
		switch (matchDirection)
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
			return new EqualsBuilder().append(sourceGroup, o.sourceGroup)
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
		return new HashCodeBuilder(13, 67).append(sourceGroup)
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
				.add("sourceGroup", sourceGroup)
				.add("sourceTags", sourceTags)
				.add("compatibleGroup", compatibleGroup)
				.add("compatibleTags", compatibleTags)
				.add("scope", scope)
				.add("bidirectional", bidirectional)
				.toString();
	}
}
