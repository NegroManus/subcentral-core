package de.subcentral.fx;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.StringUtil;

public class UserPattern implements Comparable<UserPattern>
{
	public enum Mode
	{
		LITERAL, SIMPLE, REGEX
	}

	private static final int	LITERAL_FLAGS	= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.LITERAL;
	private static final int	SIMPLE_FLAGS	= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

	private final String		pattern;
	private final Mode			mode;

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
				return Pattern.compile(pattern, LITERAL_FLAGS);
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
		List<String> simplePatterns = StringUtil.COMMA_SPLITTER.splitToList(simplePatternsString);
		String[] convertedPatterns = new String[simplePatterns.size()];
		for (int i = 0; i < simplePatterns.size(); i++)
		{
			convertedPatterns[i] = convertToPattern(simplePatterns.get(i));
		}
		if (convertedPatterns.length == 1)
		{
			return Pattern.compile(convertedPatterns[0], SIMPLE_FLAGS);
		}
		else
		{
			StringJoiner strJoiner = new StringJoiner("|", "(", ")");
			for (String p : convertedPatterns)
			{
				strJoiner.add(p);
			}
			return Pattern.compile(strJoiner.toString(), SIMPLE_FLAGS);
		}
	}

	public static Pattern parseSimplePattern(String simplePattern)
	{
		return Pattern.compile(convertToPattern(simplePattern), SIMPLE_FLAGS);
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
		return Objects.hash(pattern, mode);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(UserPattern.class).add("pattern", pattern).add("mode", mode).toString();
	}

	@Override
	public int compareTo(UserPattern o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ComparisonChain.start().compare(pattern, o.pattern, ObjectUtil.getDefaultStringOrdering()).compare(mode, o.mode).result();
	}
}