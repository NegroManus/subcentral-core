package de.subcentral.core.correct;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;

public class PatternStringReplacer implements UnaryOperator<String>
{
	public enum Mode
	{
		REPLACE_ALL, REPLACE_COMPLETE
	}

	private final Pattern	pattern;
	private final String	replacement;
	private final Mode		mode;

	public PatternStringReplacer(Pattern pattern, String replacement)
	{
		this(pattern, replacement, Mode.REPLACE_ALL);
	}

	public PatternStringReplacer(Pattern pattern, String replacement, Mode mode)
	{
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.replacement = replacement;
		this.mode = Objects.requireNonNull(mode, "mode");
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	public String getReplacement()
	{
		return replacement;
	}

	public Mode getMode()
	{
		return mode;
	}

	@Override
	public String apply(String s)
	{
		if (s == null)
		{
			return null;
		}
		switch (mode)
		{
			case REPLACE_ALL:
				return pattern.matcher(s).replaceAll(replacement);
			case REPLACE_COMPLETE:
				if (pattern.matcher(s).matches())
				{
					return replacement;
				}
				return s;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(PatternStringReplacer.class).add("pattern", pattern).add("replacement", replacement).add("mode", mode).toString();
	}
}
