package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

public class GroupsCompatibility implements Compatibility
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

	public GroupsCompatibility(Group sourceGroup, Group compatibleGroup, Scope scope, boolean bidirectional)
	{
		this(sourceGroup, null, compatibleGroup, null, scope, bidirectional);
	}

	public GroupsCompatibility(Group sourceGroup, List<Tag> sourceTags, Group compatibleGroup, List<Tag> compatibleTags, Scope scope,
			boolean bidirectional)
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
		switch (getScope())
		{
			case IF_EXISTS:
				Set<Release> compatibles = new HashSet<>(4);
				for (Release existingRls : existingRlss)
				{
					if (matchesCompatible(existingRls, md) && !rls.equals(existingRls))
					{
						compatibles.add(existingRls);
					}
				}
				return compatibles;
			case ALWAYS:
				Release compatibleRls = buildCompatible(rls, md);
				if (!rls.equals(compatibleRls))
				{
					return ImmutableSet.of(compatibleRls);
				}
				break;
		}
		return ImmutableSet.of();
	}

	private MatchDirection match(Release rls)
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

	private Release buildCompatible(Release sourceRls, MatchDirection md)
	{
		Release compatibleRls = new Release();
		compatibleRls.setMedia(sourceRls.getMedia());
		switch (md)
		{
			case FORWARD:
				compatibleRls.setGroup(compatibleGroup == null ? sourceRls.getGroup() : compatibleGroup);
				compatibleRls.setTags(compatibleTags == null ? sourceRls.getTags() : compatibleTags);
				break;
			case BACKWARD:
				compatibleRls.setGroup(sourceGroup == null ? sourceRls.getGroup() : sourceGroup);
				compatibleRls.setTags(sourceTags == null ? sourceRls.getTags() : sourceTags);
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
		if (obj != null && getClass().equals(obj.getClass()))
		{
			GroupsCompatibility o = (GroupsCompatibility) obj;
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
		return MoreObjects.toStringHelper(GroupsCompatibility.class)
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
