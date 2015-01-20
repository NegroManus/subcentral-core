package de.subcentral.core.model.release;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class CompatibilityService
{
	private Set<Compatibility>	compatibilities	= new CopyOnWriteArraySet<>();

	public Set<Compatibility> getCompatibilities()
	{
		return compatibilities;
	}

	public void setCompatibilities(Collection<? extends Compatibility> compatibilities)
	{
		this.compatibilities.clear();
		this.compatibilities.addAll(compatibilities);
	}

	public Map<Release, CompatibilityInfo> findCompatibles(Collection<Release> rlss, Collection<Release> existingRlss)
	{
		if (rlss.isEmpty())
		{
			return ImmutableMap.of();
		}
		Map<Release, CompatibilityInfo> allCompatibles = new HashMap<>(4);
		for (Release rls : rlss)
		{
			Map<Release, CompatibilityInfo> compatiblesForRls = findCompatibles(rls, existingRlss);
			for (Map.Entry<Release, CompatibilityInfo> newCompatibleEntry : compatiblesForRls.entrySet())
			{
				// only add the compatible release if not contained in the original release list
				// and not already in the list of found compatible releases
				Release newCompatibleRls = newCompatibleEntry.getKey();
				if (!rlss.contains(newCompatibleRls))
				{
					allCompatibles.putIfAbsent(newCompatibleRls, newCompatibleEntry.getValue());
					// no need to check the newly found compatible release itself for compatibilities
					// because that was already done in findCompatibles(Release, ...)
				}
			}
		}
		return ImmutableMap.copyOf(allCompatibles);
	}

	public Map<Release, CompatibilityInfo> findCompatibles(Release rls, Collection<Release> existingRlss)
	{
		if (rls == null)
		{
			return ImmutableMap.of();
		}

		// Do not use ImmutableMap.Builder here, as it has no putIfAbsent() method
		Map<Release, CompatibilityInfo> allCompatibles = new HashMap<>(4);

		Queue<Release> rlssToCheck = new ArrayDeque<>(4);
		rlssToCheck.add(rls);
		Release rlsToCheck;
		while ((rlsToCheck = rlssToCheck.poll()) != null)
		{
			for (Compatibility c : compatibilities)
			{
				Set<Release> compatibles = c.findCompatibles(rlsToCheck, existingRlss);
				for (Release compatible : compatibles)
				{
					// Never add the source Release
					if (!rls.equals(compatible))
					{
						// Only add the compatible Release if it was not found before
						CompatibilityInfo previousValue = allCompatibles.putIfAbsent(compatible, new CompatibilityInfo(rlsToCheck, c));
						// If previousValue == null, the compatible is new.
						// Then it should be checked for compatibilities as well
						if (previousValue == null)
						{
							rlssToCheck.add(compatible);
						}
					}
				}
			}
		}
		return ImmutableMap.copyOf(allCompatibles);
	}

	public static class CompatibilityInfo
	{
		private final Release		compatibleTo;
		private final Compatibility	compatibility;

		public CompatibilityInfo(Release compatibleTo, Compatibility compatibility)
		{
			this.compatibleTo = compatibleTo;
			this.compatibility = compatibility;
		}

		public Release getCompatibleTo()
		{
			return compatibleTo;
		}

		public Compatibility getCompatibility()
		{
			return compatibility;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(CompatibilityInfo.class)
					.omitNullValues()
					.add("compatibleTo", compatibleTo)
					.add("compatibility", compatibility)
					.toString();
		}
	}
}
