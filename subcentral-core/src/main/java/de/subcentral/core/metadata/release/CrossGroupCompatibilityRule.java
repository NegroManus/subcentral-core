package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;

public class CrossGroupCompatibilityRule implements CompatibilityRule, Comparable<CrossGroupCompatibilityRule>
{
	private enum MatchDirection
	{
		NONE, FORWARD, BACKWARD;
	}

	private final Group		sourceGroup;
	private final Group		compatibleGroup;
	private final boolean	symmetric;

	public CrossGroupCompatibilityRule(Group sourceGroup, Group compatibleGroup, boolean symmetric)
	{
		this.sourceGroup = Objects.requireNonNull(sourceGroup, "sourceGroup");
		this.compatibleGroup = Objects.requireNonNull(compatibleGroup, "compatibleGroup");
		this.symmetric = symmetric;
	}

	public Group getSourceGroup()
	{
		return sourceGroup;
	}

	public Group getCompatibleGroup()
	{
		return compatibleGroup;
	}

	public boolean isSymmetric()
	{
		return symmetric;
	}

	@Override
	public Set<Release> findCompatibles(Release source, Collection<Release> possibleCompatibles)
	{
		MatchDirection md = matchSourceRelease(source);
		if (MatchDirection.NONE == md)
		{
			return ImmutableSet.of();
		}
		Set<Release> compatibles = new HashSet<>(4);
		for (Release possibleCompatible : possibleCompatibles)
		{
			if (matchesCompatibleRelease(possibleCompatible, md) && !source.equals(possibleCompatible))
			{
				// Set.add() only adds if does not exist yet. That is what we want.
				// Do not use ImmutableSet.Builder.add here as it allows the addition of duplicate entries but throws an exception at build time.
				compatibles.add(possibleCompatible);
			}
		}
		return compatibles;
	}

	private MatchDirection matchSourceRelease(Release source)
	{
		if (source == null)
		{
			return MatchDirection.NONE;
		}
		if (sourceGroup.equals(source.getGroup()))
		{
			return MatchDirection.FORWARD;
		}
		if (symmetric && compatibleGroup.equals(source.getGroup()))
		{
			return MatchDirection.BACKWARD;
		}
		return MatchDirection.NONE;
	}

	private boolean matchesCompatibleRelease(Release possibleCompatible, MatchDirection matchDirection)
	{
		switch (matchDirection)
		{
			case FORWARD:
				return compatibleGroup.equals(possibleCompatible.getGroup());
			case BACKWARD:
				return sourceGroup.equals(possibleCompatible.getGroup());
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
		if (obj instanceof CrossGroupCompatibilityRule)
		{
			CrossGroupCompatibilityRule o = (CrossGroupCompatibilityRule) obj;
			return sourceGroup.equals(o.sourceGroup) && compatibleGroup.equals(o.compatibleGroup) && symmetric == o.symmetric;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(sourceGroup, compatibleGroup, symmetric);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(CrossGroupCompatibilityRule.class).add("sourceGroup", sourceGroup).add("compatibleGroup", compatibleGroup).add("symmetric", symmetric).toString();
	}

	public String toShortString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(sourceGroup);
		if (symmetric)
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

	@Override
	public int compareTo(CrossGroupCompatibilityRule o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start().compare(sourceGroup, o.sourceGroup).compare(compatibleGroup, o.compatibleGroup).compareFalseFirst(symmetric, o.symmetric).result();
	}
}
