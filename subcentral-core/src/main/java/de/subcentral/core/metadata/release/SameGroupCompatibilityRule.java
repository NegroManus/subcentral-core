package de.subcentral.core.metadata.release;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class SameGroupCompatibilityRule implements CompatibilityRule {
	@Override
	public Set<Release> findCompatibles(Release source, Collection<Release> possibleCompatibles) {
		if (source == null || source.getGroup() == null) {
			return ImmutableSet.of();
		}
		Set<Release> compatibles = new HashSet<>(4);
		for (Release possibleCompatible : possibleCompatibles) {
			if (source.getGroup().equals(possibleCompatible.getGroup()) && !source.equals(possibleCompatible)) {
				// Set.add() only adds if does not exist yet. That is what we want.
				// Do not use ImmutableSet.Builder.add() here as it allows the addition of duplicate entries but throws an exception when building
				compatibles.add(possibleCompatible);
			}
		}
		return compatibles;
	}
}