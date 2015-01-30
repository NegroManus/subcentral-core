package de.subcentral.core.util;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class StringUtil
{
	public static final Joiner		COMMA_JOINER		= Joiner.on(',').skipNulls();
	public static final Splitter	COMMA_SPLITTER		= Splitter.on(',');

	public static final Splitter	WHITESPACE_SPLITTER	= Splitter.on(Pattern.compile("\\s+"));

	public static boolean startsWith(StringBuilder sb, char c)
	{
		return sb.length() > 0 && sb.charAt(0) == c;
	}

	public static boolean startsWith(StringBuilder sb, CharSequence cs)
	{
		return cs != null && sb.length() >= cs.length() && sb.substring(0, cs.length()).equals(cs);
	}

	public static boolean endsWith(StringBuilder sb, char c)
	{
		return sb.length() > 0 && sb.charAt(sb.length() - 1) == c;
	}

	public static boolean endsWith(StringBuilder sb, CharSequence cs)
	{
		return cs != null && sb.length() >= cs.length() && sb.substring(sb.length() - cs.length()).equals(cs);
	}

	public static StringBuilder appendIfNotEndsWith(StringBuilder sb, char c)
	{
		if (!endsWith(sb, c))
		{
			sb.append(c);
		}
		return sb;
	}

	public static StringBuilder appendIfNotEndsWith(StringBuilder sb, CharSequence cs)
	{
		if (!StringUtils.endsWith(sb, cs))
		{
			sb.append(cs);
		}
		return sb;
	}

	public static StringBuilder appendSpaceIfNotEndsWith(StringBuilder sb)
	{
		return appendIfNotEndsWith(sb, ' ');
	}

	public static StringBuilder stripStart(StringBuilder sb, char c)
	{
		if (startsWith(sb, c))
		{
			sb.deleteCharAt(0);
		}
		return sb;
	}

	public static StringBuilder stripStart(StringBuilder sb, CharSequence cs)
	{
		while (startsWith(sb, cs))
		{
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb;
	}

	public static StringBuilder stripStart(StringBuilder sb)
	{
		return stripStart(sb, ' ');
	}

	public static StringBuilder stripEnd(StringBuilder sb, char c)
	{
		while (endsWith(sb, c))
		{
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb;
	}

	public static StringBuilder stripEnd(StringBuilder sb, CharSequence cs)
	{
		while (endsWith(sb, cs))
		{
			sb.delete(sb.length() - cs.length(), sb.length());
		}
		return sb;
	}

	public static StringBuilder stripEnd(StringBuilder sb)
	{
		return stripEnd(sb, ' ');
	}

	public static StringBuilder trim(StringBuilder sb)
	{
		stripStart(sb);
		return stripEnd(sb);
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
			return Pattern.compile(convertedPatterns[0], Pattern.CASE_INSENSITIVE);
		}
		else
		{
			StringJoiner strJoiner = new StringJoiner("|", "(", ")");
			for (String p : convertedPatterns)
			{
				strJoiner.add(p);
			}
			return Pattern.compile(strJoiner.toString(), Pattern.CASE_INSENSITIVE);
		}
	}

	public static Pattern parseSimplePattern(String simplePattern)
	{
		return Pattern.compile(convertToPattern(simplePattern), Pattern.CASE_INSENSITIVE);
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

	public static String quoteString(String s)
	{
		if (s == null)
		{
			return null;
		}
		return '"' + s + '"';
	}

	private StringUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
