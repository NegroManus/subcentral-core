package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.Set;

@FunctionalInterface
public interface CompatibilityRule {
    public Set<Release> findCompatibles(Release source, Collection<Release> possibleCompatibles);
}
