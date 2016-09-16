package de.subcentral.mig.parse;

import java.util.Objects;
import java.util.regex.Pattern;

public class ConfidencePattern {
	protected final Pattern	pattern;
	protected final int		confidence;

	public ConfidencePattern(Pattern pattern, int confidence) {
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.confidence = confidence;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public int getConfidence() {
		return confidence;
	}
}
