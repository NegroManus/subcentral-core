package de.subcentral.fx;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;

public class UserPattern implements Comparable<UserPattern>
{
	public enum Mode
	{
		LITERAL, SIMPLE, REGEX
	}

	private final String	pattern;
	private final Mode		mode;

	public UserPattern(String pattern, Mode mode) throws PatternSyntaxException
	{
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.mode = Objects.requireNonNull(mode, "mode");
	}

	public String getPattern()
	{
		return pattern;
	}

	public Mode getMode()
	{
		return mode;
	}

	public Pattern toPattern() throws PatternSyntaxException
	{
		return compilePattern();
	}

	private Pattern compilePattern() throws PatternSyntaxException
	{
		switch (mode)
		{
			case LITERAL:
				return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL);
			case SIMPLE:
				return parseSimplePattern(pattern);
			case REGEX:
				return Pattern.compile(pattern);
			default:
				throw new IllegalStateException();
		}
	}

	public static Pattern parseSimplePatterns(String simplePatternsString)
	{
		if (StringUtils.isBlank(simplePatternsString))
		{
			return null;
		}
		List<String> simplePatterns = Splitter.on(Pattern.compile("\\s*,\\s*")).omitEmptyStrings().splitToList(simplePatternsString);
		String[] convertedPatterns = new String[simplePatterns.size()];
		for (int i = 0; i < simplePatterns.size(); i++)
		{
			convertedPatterns[i] = convertToPattern(simplePatterns.get(i));
		}
		if (convertedPatterns.length == 1)
		{
			return Pattern.compile(convertedPatterns[0], Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
		else
		{
			StringJoiner strJoiner = new StringJoiner("|", "(", ")");
			for (String p : convertedPatterns)
			{
				strJoiner.add(p);
			}
			return Pattern.compile(strJoiner.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		}
	}

	public static Pattern parseSimplePattern(String simplePattern)
	{
		return Pattern.compile(convertToPattern(simplePattern), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	private static String convertToPattern(String simplePattern)
	{
		StringBuilder convertedPattern = new StringBuilder();
		Matcher mAsterisk = Pattern.compile("\\*").matcher(simplePattern);
		int index = 0;
		while (mAsterisk.find())
		{
			String head = simplePattern.substring(index, mAsterisk.start());
			if (!head.isEmpty())
			{
				convertedPattern.append(Pattern.quote(head));
			}
			convertedPattern.append(".*");
			index = mAsterisk.end();
		}
		String tail = simplePattern.substring(index);
		if (!tail.isEmpty())
		{
			convertedPattern.append(Pattern.quote(tail));
		}
		return convertedPattern.toString();
	}

	@Override
	public int compareTo(UserPattern o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return pattern.compareToIgnoreCase(o.pattern);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof UserPattern)
		{
			UserPattern o = (UserPattern) obj;
			return pattern.equals(o.pattern) && mode.equals(o.mode);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(731, 15).append(pattern).append(mode).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(UserPattern.class).omitNullValues().add("pattern", pattern).add("mode", mode).toString();
	}
}