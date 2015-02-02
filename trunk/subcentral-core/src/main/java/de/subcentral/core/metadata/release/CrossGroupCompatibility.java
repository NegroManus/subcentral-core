package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

public class CrossGroupCompatibility implements Compatibility
{
	private static enum MatchDirection
	{
		NONE, FORWARD, BACKWARD;
	}

	private final Group		sourceGroup;
	private final Group		compatibleGroup;
	private final boolean	bidirectional;

	public CrossGroupCompatibility(Group sourceGroup, Group compatibleGroup, boolean bidirectional)
	{
		this.sourceGroup = Objects.requireNonNull(sourceGroup, "sourceGroup");
		this.compatibleGroup = Objects.requireNonNull(compatibleGroup, "compatibleGroup");
		this.bidirectional = bidirectional;
	}

	public Group getSourceGroup()
	{
		return sourceGroup;
	}

	public Group getCompatibleGroup()
	{
		return compatibleGroup;
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
		MatchDirection md = matchSourceRelease(rls);
		if (MatchDirection.NONE == md)
		{
			return ImmutableSet.of();
		}
		Set<Release> compatibles = new HashSet<>(4);
		for (Release existingRls : existingRlss)
		{
			if (matchesCompatibleRelease(existingRls, md) && !rls.equals(existingRls))
			{
				// Set.add() only adds if does not exist yet. That is what we want.
				// Do not use ImmutableSet.Builder.add here as it allows the addition of duplicate entries but throws an exception at build time.
				compatibles.add(existingRls);
			}
		}
		return ImmutableSet.copyOf(compatibles);
	}

	private MatchDirection matchSourceRelease(Release rls)
	{
		if (sourceGroup.equals(rls.getGroup()))
		{
			return MatchDirection.FORWARD;
		}
		if (bidirectional && compatibleGroup.equals(rls.getGroup()))
		{
			return MatchDirection.BACKWARD;
		}
		return MatchDirection.NONE;
	}

	private boolean matchesCompatibleRelease(Release compatibleRls, MatchDirection matchDirection)
	{
		if (compatibleRls == null)
		{
			return false;
		}
		switch (matchDirection)
		{
			case FORWARD:
				return compatibleGroup.equals(compatibleRls.getGroup());
			case BACKWARD:
				return sourceGroup.equals(compatibleRls.getGroup());
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
		if (obj instanceof CrossGroupCompatibility)
		{
			CrossGroupCompatibility o = (CrossGroupCompatibility) obj;
			return sourceGroup.equals(o.sourceGroup) && compatibleGroup.equals(o.compatibleGroup) && bidirectional == o.bidirectional;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(13, 67).append(sourceGroup).append(compatibleGroup).append(bidirectional).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(CrossGroupCompatibility.class)
				.add("sourceGroup", sourceGroup)
				.add("compatibleGroup", compatibleGroup)
				.add("bidirectional", bidirectional)
				.toString();
	}

	public String toShortString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(sourceGroup);
		if (bidirectional)
		{
			sb.append(" <-> ");
		}
		else
		{
			sb.append(" -> ");
		}
		sb.append(compatibleGroup);
		return sb.toString();
	}
}
