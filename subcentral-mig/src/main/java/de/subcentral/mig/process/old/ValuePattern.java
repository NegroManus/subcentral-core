package de.subcentral.mig.process.old;

import java.util.regex.Pattern;

public class ValuePattern<V>
{
	private final Pattern	pattern;
	private final V			value;

	public ValuePattern(Pattern pattern, V value)
	{
		this.pattern = pattern;
		this.value = value;
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	public V getValue()
	{
		return value;
	}
}
