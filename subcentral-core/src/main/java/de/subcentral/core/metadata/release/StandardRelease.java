package de.subcentral.core.metadata.release;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

public class StandardRelease implements Comparable<StandardRelease> {
	public enum Scope {
		ALWAYS, IF_GUESSING
	};

	// private final Predicate<List<Media>> mediaFilter;
	private final Release	release;
	private final Scope		scope;

	public StandardRelease(Release release, Scope scope) {
		this(release.getTags(), release.getGroup(), scope);
	}

	public StandardRelease(List<Tag> tags, Group group, Scope scope) {
		this.release = new Release(tags, group);
		this.scope = Objects.requireNonNull(scope, "scope");
	}

	/**
	 * Only stores tags and group.
	 * 
	 * @return the standard release
	 */
	public Release getRelease() {
		return release;
	}

	public Scope getScope() {
		return scope;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof StandardRelease) {
			StandardRelease o = (StandardRelease) obj;
			return release.equals(o.release) && scope == o.scope;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(release, scope);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(StandardRelease.class).add("release", release).add("scope", scope).toString();
	}

	@Override
	public int compareTo(StandardRelease o) {
		if (this == o) {
			return 0;
		}
		// nulls first
		if (o == null) {
			return 1;
		}
		return ComparisonChain.start().compare(release, o.release).compare(scope, o.scope).result();
	}
}
