package de.subcentral.core.parse;

import java.util.Objects;

public abstract class AbstractMapper<T> implements Mapper<T> {
	protected final ParsePropService parsePropService;

	public AbstractMapper() {
		this(ParsingDefaults.getDefaultPropFromStringService());
	}

	public AbstractMapper(ParsePropService parsePropService) {
		this.parsePropService = Objects.requireNonNull(parsePropService, "parsePropService");
	}

	public ParsePropService getPropFromStringService() {
		return parsePropService;
	}

	protected abstract Class<?> getTargetType();
}
