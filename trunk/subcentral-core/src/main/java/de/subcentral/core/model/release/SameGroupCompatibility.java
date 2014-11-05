package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SameGroupCompatibility implements Compatibility
{
	@Override
	public Set<Release> findCompatibles(Release rls, Collection<Release> existingRlss)
	{
		if (rls == null || rls.getGroup() == null || existingRlss.isEmpty())
		{
			return ImmutableSet.of();
		}
		Set<Release> compatibles = new HashSet<>(4);
		for (Release existingRls : existingRlss)
		{
			if (rls.getGroup().equals(existingRls.getGroup()) && !rls.equals(existingRls))
			{
				// Set.add() only adds the new element if that element is not already contained in the Set.
				// That is what we want.
				compatibles.add(existingRls);
			}
		}
		return compatibles;
	}
}
