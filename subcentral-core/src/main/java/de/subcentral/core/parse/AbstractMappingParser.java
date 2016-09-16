package de.subcentral.core.parse;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMappingParser<T> implements Parser<T> {
	protected final MappingMatcher<SimplePropDescriptor> matcher;

	public AbstractMappingParser(MappingMatcher<SimplePropDescriptor> matcher) {
		this.matcher = Objects.requireNonNull(matcher, "matcher");
	}

	public MappingMatcher<SimplePropDescriptor> getMatcher() {
		return matcher;
	}

	@Override
	public T parse(String text) {
		Map<SimplePropDescriptor, String> matchResult = matcher.match(text);
		if (matchResult.isEmpty()) {
			return null;
		}
		return map(matchResult);
	}

	protected abstract T map(Map<SimplePropDescriptor, String> props);
}
