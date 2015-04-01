package de.subcentral.core.standardizing;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;

public class PatternStringReplacer implements UnaryOperator<String>
{
	private final Pattern	pattern;
	private final String	replacement;

	public PatternStringReplacer(Pattern languagePattern, String languageReplacement)
	{
		this.pattern = languagePattern;
		this.replacement = languageReplacement;
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
	public String apply(String string)
	{
		if (pattern.matcher(string).matches())
		{
			return replacement;
		}
		return string;
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(PatternStringReplacer.class).add("pattern", pattern).add("replacement", replacement).toString();
	}
}
