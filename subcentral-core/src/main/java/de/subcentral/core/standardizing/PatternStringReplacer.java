package de.subcentral.core.standardizing;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;

public class PatternStringReplacer implements UnaryOperator<String>
{
	private final Pattern	pattern;
	private final String	replacement;

	public PatternStringReplacer(Pattern languagePattern, String replacement)
	{
		this.pattern = languagePattern;
		this.replacement = replacement;
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	public String getReplacement()
	{
		return replacement;
	}

	@Override
	public String apply(String s)
	{
		if (s == null)
		{
			return null;
		}
		return pattern.matcher(s).replaceAll(replacement);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(PatternStringReplacer.class).add("pattern", pattern).add("replacement", replacement).toString();
	}
}
