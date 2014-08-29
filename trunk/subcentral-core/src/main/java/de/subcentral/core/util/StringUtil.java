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

	public static void appendIfNotEndsWith(StringBuilder sb, char c)
	{
		if (endsWith(sb, c))
		{
			sb.append(c);
		}
	}

	public static void appendIfNotEndsWith(StringBuilder sb, CharSequence cs)
	{
		if (!StringUtils.endsWith(sb, cs))
		{
			sb.append(cs);
		}
	}

	public static void appendSpaceIfNotEndsWith(StringBuilder sb)
	{
		appendIfNotEndsWith(sb, ' ');
	}

	public static void stripStart(StringBuilder sb, char c)
	{
		if (startsWith(sb, c))
		{
			sb.deleteCharAt(0);
		}
	}

	public static void stripStart(StringBuilder sb, CharSequence cs)
	{
		while (startsWith(sb, cs))
		{
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	public static void stripStart(StringBuilder sb)
	{
		stripStart(sb, ' ');
	}

	public static void stripEnd(StringBuilder sb, char c)
	{
		while (endsWith(sb, c))
		{
			sb.deleteCharAt(sb.length() - 1);
		}
	}

	public static void stripEnd(StringBuilder sb, CharSequence cs)
	{
		while (endsWith(sb, cs))
		{
			sb.delete(sb.length() - cs.length(), sb.length());
		}
	}

	public static void stripEnd(StringBuilder sb)
	{
		stripEnd(sb, ' ');
	}

	public static void trim(StringBuilder sb)
	{
		stripStart(sb);
		stripEnd(sb);
	}

}