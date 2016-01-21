package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SameGroupCompatibility implements Compatibility
{
	@Override
	public Set<Release> findCompatibles(Release rls, Collection<Release> existingRlss)
	{
		if (rls == null || rls.getGroup() == null)
		{
			return ImmutableSet.of();
		}
		Set<Release> compatibles = new HashSet<>(4);
		for (Release existingRls : existingRlss)
		{
			if (rls.getGroup().equals(existingRls.getGroup()) && !rls.equals(existingRls))
			{
				// Set.add() only adds if does not exist yet. That is what we want.
				// Do not use ImmutableSet.Builder.add() here as it allows the addition of duplicate entries but throws an exception when building
				compatibles.add(existingRls);
			}
		}
		return compatibles;
	}
}