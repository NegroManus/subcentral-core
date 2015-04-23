package de.subcentral.core.standardizing;

import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;

public class PatternStringReplacer implements UnaryOperator<String>
{

	public static enum Mode
	{
		REPLACE_EACH, REPLACE_WHOLLY
	}

	private final Pattern	pattern;
	private final String	replacement;
	private final Mode		mode;

	public PatternStringReplacer(Pattern pattern, String replacement)
	{
		this(pattern, replacement, Mode.REPLACE_EACH);
	}

	public PatternStringReplacer(Pattern pattern, String replacement, Mode mode)
	{
		this.pattern = pattern;
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
			case REPLACE_EACH:
				return pattern.matcher(s).replaceAll(replacement);
			case REPLACE_WHOLLY:
				Matcher m = pattern.matcher(s);
				if (m.matches())
				{
					StringBuffer sb = new StringBuffer();
					m.appendReplacement(sb, replacement);
					return sb.toString();
				}
				return s;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(PatternStringReplacer.class)
				.add("pattern", pattern)
				.add("replacement", replacement)
				.add("mode", mode)
				.toString();
	}
}
