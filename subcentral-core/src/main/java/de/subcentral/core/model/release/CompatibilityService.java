package de.subcentral.core.model.release;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public class CompatibilityService
{
	private Set<Compatibility>	compatibilities	= new HashSet<>();

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
						// only add the compatible Release, if it was not found before
						Compatibility previousValue = allCompatibles.putIfAbsent(compatible, c);
						// if previousValue == null, the compatible is new
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
