package de.subcentral.core.util;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class StringUtil
{

	public static final Joiner		COMMA_JOINER		= Joiner.on(',').skipNulls();
	public static final Splitter	COMMA_SPLITTER		= Splitter.on(',');

	public static final Splitter	WHITESPACE_SPLITTER	= Splitter.on(Pattern.compile("\\s+"));

	private StringUtil()
	{
		// static utility class
	}

	public static boolean startsWith(StringBuilder sb, char c)
	{
		int len = sb.length();
		return len > 0 && sb.charAt(0) == c;
	}

	public static boolean endsWith(StringBuilder sb, char c)
	{
		int len = sb.length();
		return len > 0 && sb.charAt(len - 1) == c;
	}

	public static void append(StringBuilder sb, char c)
	{
		if (endsWith(sb, c))
		{
			sb.append(c);
		}
	}

	public static void append(StringBuilder sb, CharSequence cs)
	{
		if (!StringUtils.endsWith(sb, cs))
		{
			sb.append(cs);
		}
	}

	public static void append(StringBuilder sb)
	{
		append(sb, ' ');
	}

	public static void deleteLeading(StringBuilder sb, char c)
	{
		if (startsWith(sb, c))
		{
			sb.deleteCharAt(0);
		}
	}

	public static void deleteLeading(StringBuilder sb, CharSequence cs)
	{
		if (StringUtils.startsWith(sb, cs))
		{
			sb.delete(0, cs.length());
		}
	}

	public static void deleteTrailing(StringBuilder sb, char c)
	{
		int len = sb.length();
		while (len > 0 && sb.charAt(len - 1) == c)
		{
			sb.deleteCharAt(len - 1);
			len = sb.length();
		}
	}

	public static void deleteTrailing(StringBuilder sb, CharSequence cs)
	{
		while (sb.length() > 0 && StringUtils.endsWith(sb, cs))
		{
			sb.delete(sb.length() - cs.length(), sb.length());
		}
	}

	public static void deleteTrailing(StringBuilder sb)
	{
		deleteTrailing(sb, ' ');
	}
}
