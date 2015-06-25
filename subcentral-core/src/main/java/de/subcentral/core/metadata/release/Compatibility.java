package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.Set;

public interface Compatibility
{
    public Set<Release> findCompatibles(Release rls, Collection<Release> existingRlss);
}
