package de.subcentral.core.model.release;

import java.util.Collection;
import java.util.Set;

public interface Compatibility
{
	public Set<Release> findCompatibles(Release rls, Collection<Release> existingRlss);
}
