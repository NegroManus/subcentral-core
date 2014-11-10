package de.subcentral.core.model.release;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableMap;

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

	public Map<Release, Compatibility> findCompatibles(Release rls, Collection<Release> existingRlss)
	{
		if (rls == null)
		{
			return ImmutableMap.of();
		}

		// Do not use ImmutableMap.Builder here, as it has no putIfAbsent() method
		Map<Release, Compatibility> allCompatibles = new HashMap<>();

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
						Compatibility previousValue = allCompatibles.putIfAbsent(compatible, c);
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
}
