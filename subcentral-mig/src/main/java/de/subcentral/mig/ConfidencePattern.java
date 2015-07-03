package de.subcentral.mig;

import java.util.Objects;
import java.util.regex.Pattern;

public class ConfidencePattern
{
    private final Pattern pattern;
    private final int	  confidence;

    public ConfidencePattern(Pattern pattern, int confidence)
    {
	this.pattern = Objects.requireNonNull(pattern, "pattern");
	this.confidence = confidence;
    }

    public Pattern getPattern()
    {
	return pattern;
    }

    public int getConfidence()
    {
	return confidence;
    }
}
