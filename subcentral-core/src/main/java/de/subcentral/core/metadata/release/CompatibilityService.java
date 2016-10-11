package de.subcentral.core.metadata.release;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableSet;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class CompatibilityService {
    private Set<CompatibilityRule> rules = new CopyOnWriteArraySet<>();

    public Set<CompatibilityRule> getRules() {
        return rules;
    }

    public void setCompatibilities(Collection<? extends CompatibilityRule> rules) {
        this.rules.clear();
        this.rules.addAll(rules);
    }

    public Set<Compatibility> findCompatibilities(Collection<Release> sources, Collection<Release> possibleCompatibles) {
        // LinkedHashMap to maintain insertion order
        Map<Release, Compatibility> allCompatibilities = new LinkedHashMap<>(4);
        for (Release source : sources) {
            Set<Compatibility> compatibilities = findCompatibilities(source, possibleCompatibles);
            for (Compatibility compatibility : compatibilities) {
                // only add the compatible release if not contained in the original release list
                // and not already in the list of found compatible releases
                Release compatible = compatibility.getCompatible();
                if (!sources.contains(compatible)) {
                    allCompatibilities.putIfAbsent(compatible, compatibility);
                    // no need to check the newly found compatible release itself for rules
                    // because that was already done in findCompatibles(Release, Collection<Release>)
                }
            }
        }
        return ImmutableSet.copyOf(allCompatibilities.values());
    }

    public Set<Compatibility> findCompatibilities(Release source, Collection<Release> possibleCompatibles) {
        if (source == null) {
            return ImmutableSet.of();
        }

        // Do not use ImmutableMap.Builder here, as it has no putIfAbsent() method
        // LinkedHashMap to maintain insertion order
        Map<Release, Compatibility> allCompatibilities = new LinkedHashMap<>(4);

        Queue<Release> sources = new ArrayDeque<>(4);
        sources.add(source);
        Release currentSource;
        while ((currentSource = sources.poll()) != null) {
            for (CompatibilityRule c : rules) {
                Set<Release> compatibles = c.findCompatibles(currentSource, possibleCompatibles);
                for (Release compatible : compatibles) {
                    // Never add the source Release
                    if (!source.equals(compatible)) {
                        // Only add the compatible Release if it was not found before
                        Compatibility previousValue = allCompatibilities.putIfAbsent(compatible, new Compatibility(currentSource, compatible, c));
                        // If previousValue == null, the compatible is new.
                        // Then it should be checked for rules as well.
                        if (previousValue == null) {
                            sources.add(compatible);
                        }
                    }
                }
            }
        }
        return ImmutableSet.copyOf(allCompatibilities.values());
    }
}
