package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.ModelUtils;

public class GroupsCompatibility implements Compatibility
{
	public static enum Condition
	{
		IF_EXISTS, IF_EXISTS_OR_NO_INFO, ALWAYS;
	}

	private static enum MatchDirection
	{
		NONE, FORWARD, BACKWARD;
	}

	private final ImmutableList<Tag>	sourceTags;
	private final Group					sourceGroup;
	private final ImmutableList<Tag>	compatibleTags;
	private final Group					compatibleGroup;
	private final Condition				condition;
	private final boolean				bidirectional;

	public GroupsCompatibility(Group sourceGroup, Group compatibleGroup, Condition condition, boolean bidirectional)
	{
		this(ImmutableList.of(), sourceGroup, ImmutableList.of(), compatibleGroup, condition, bidirectional);
	}

	public GroupsCompatibility(Collection<Tag> sourceTags, Group sourceGroup, Collection<Tag> compatibleTags, Group compatibleGroup,
			Condition condition, boolean bidirectional)
	{
		Objects.requireNonNull(sourceTags, "sourceTags");
		Objects.requireNonNull(compatibleTags, "compatibleTags");
		if (sourceGroup == null && sourceTags.isEmpty())
		{
			throw new IllegalArgumentException("either sourceGroup or sourceTags must be non-null / non-empty");
		}
		if (compatibleGroup == null && compatibleTags.isEmpty())
		{
			throw new IllegalArgumentException("either compatibleGroup or compatibleTags must be non-null / non-empty");
		}
		this.sourceTags = ImmutableList.copyOf(sourceTags);
		this.sourceGroup = sourceGroup;
		this.compatibleTags = ImmutableList.copyOf(compatibleTags);
		this.compatibleGroup = compatibleGroup;
		this.condition = Objects.requireNonNull(condition, "condition");
		this.bidirectional = bidirectional;
	}

	public ImmutableList<Tag> getSourceTags()
	{
		return sourceTags;
	}

	public Group getSourceGroup()
	{
		return sourceGroup;
	}

	public ImmutableList<Tag> getCompatibleTags()
	{
		return compatibleTags;
	}

	public Group getCompatibleGroup()
	{
		return compatibleGroup;
	}

	public Condition getCondition()
	{
		return condition;
	}

	public boolean isBidirectional()
	{
		return bidirectional;
	}

	@Override
	public Set<Release> findCompatibles(Release rls, Collection<Release> existingRlss)
	{
		if (rls == null)
		{
			return ImmutableSet.of();
		}
		MatchDirection md = match(rls);
		if (MatchDirection.NONE == md)
		{
			return ImmutableSet.of();
		}
		if (condition == Condition.ALWAYS || (condition == Condition.IF_EXISTS_OR_NO_INFO && existingRlss.isEmpty()))
		{
			Release compatibleRls = buildCompatible(rls, md);
			if (!rls.equals(compatibleRls))
			{
				return ImmutableSet.of(compatibleRls);
			}
		}
		else if (condition == Condition.IF_EXISTS || condition == Condition.IF_EXISTS_OR_NO_INFO)
		{
			Set<Release> compatibles = new HashSet<>(4);
			for (Release existingRls : existingRlss)
			{
				if (matchesCompatible(existingRls, md) && !rls.equals(existingRls))
				{
					// Set.add() only adds if does not exist yet. That is what we want.
					// Do not use ImmutableSet.Builder.add here as it allows the addition of duplicate entries but throws an exception at build time.
					compatibles.add(existingRls);
				}
			}
			return compatibles;
		}
		return ImmutableSet.of();
	}

	private MatchDirection match(Release rls)
	{
		if ((sourceTags.isEmpty() || sourceTags.equals(rls.getTags())) && (sourceGroup == null || sourceGroup.equals(rls.getGroup())))
		{
			return MatchDirection.FORWARD;
		}
		if (bidirectional && (compatibleTags.isEmpty() || compatibleTags.equals(rls.getTags()))
				&& (compatibleGroup == null || compatibleGroup.equals(rls.getGroup())))
		{
			return MatchDirection.BACKWARD;
		}
		return MatchDirection.NONE;
	}

	private boolean matchesCompatible(Release compatibleRls, MatchDirection matchDirection)
	{
		if (compatibleRls == null)
		{
			return false;
		}
		switch (matchDirection)
		{
			case FORWARD:
				return (compatibleTags.isEmpty() || compatibleTags.equals(compatibleRls.getTags()))
						&& (compatibleGroup == null || compatibleGroup.equals(compatibleRls.getGroup()));
			case BACKWARD:
				return (sourceTags.isEmpty() || sourceTags.equals(compatibleRls.getTags()))
						&& (sourceGroup == null || sourceGroup.equals(compatibleRls.getGroup()));
			case NONE:
				return false;
			default:
				return false;
		}
	}

	private Release buildCompatible(Release sourceRls, MatchDirection md)
	{
		Release compatibleRls = new Release();
		compatibleRls.setMedia(sourceRls.getMedia());
		switch (md)
		{
			case FORWARD:
				compatibleRls.setTags(compatibleTags.isEmpty() ? sourceRls.getTags() : compatibleTags);
				compatibleRls.setGroup(compatibleGroup == null ? sourceRls.getGroup() : compatibleGroup);
				break;
			case BACKWARD:
				compatibleRls.setTags(sourceTags.isEmpty() ? sourceRls.getTags() : sourceTags);
				compatibleRls.setGroup(sourceGroup == null ? sourceRls.getGroup() : sourceGroup);
				break;
			case NONE:
				break;
		}
		return compatibleRls;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof GroupsCompatibility)
		{
			GroupsCompatibility o = (GroupsCompatibility) obj;
			return sourceTags.equals(o.sourceTags) && Objects.equals(sourceGroup, o.sourceGroup) && compatibleTags.equals(o.compatibleTags)
					&& Objects.equals(compatibleGroup, o.compatibleGroup) && condition.equals(o.condition) && bidirectional == o.bidirectional;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(13, 67).append(sourceTags)
				.append(sourceGroup)
				.append(compatibleTags)
				.append(compatibleGroup)
				.append(condition)
				.append(bidirectional)
				.toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(GroupsCompatibility.class)
				.omitNullValues()
				.add("sourceTags", ModelUtils.nullIfEmpty(sourceTags))
				.add("sourceGroup", sourceGroup)
				.add("compatibleTags", ModelUtils.nullIfEmpty(compatibleTags))
				.add("compatibleGroup", compatibleGroup)
				.add("condition", condition)
				.add("bidirectional", bidirectional)
				.toString();
	}

	public String toShortString()
	{
		StringBuilder sb = new StringBuilder();
		if (!sourceTags.isEmpty())
		{
			Joiner.on('.').appendTo(sb, sourceTags);
		}
		if (sourceGroup != null)
		{
			if (!sourceTags.isEmpty())
			{
				sb.append('.');
			}
			sb.append(sourceGroup);
		}
		if (bidirectional)
		{
			sb.append(" <-> ");
		}
		else
		{
			sb.append(" -> ");
		}
		if (!compatibleTags.isEmpty())
		{
			Joiner.on('.').appendTo(sb, compatibleTags);
		}
		if (compatibleGroup != null)
		{
			if (!compatibleTags.isEmpty())
			{
				sb.append('.');
			}
			sb.append(compatibleGroup);
		}
		sb.append(" [");
		sb.append(condition);
		sb.append(']');
		return sb.toString();
	}
}
