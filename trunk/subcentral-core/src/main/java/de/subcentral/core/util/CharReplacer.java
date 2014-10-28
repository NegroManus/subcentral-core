package de.subcentral.core.util;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;

/**
 * #Immutable #Thread-safe
 */
public class CharReplacer implements UnaryOperator<String>
{
	private final char[]								allowedChars;
	private final char[]								charsToDelete;
	private final String								defaultReplacement;
	private final ImmutableMap<Character, Character>	replacements;

	public CharReplacer(String allowedChars, String charsToDelete, String defaultReplacement)
	{
		this.allowedChars = allowedChars.toCharArray();
		this.charsToDelete = charsToDelete.toCharArray();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.of();
	}

	public CharReplacer(String allowedChars, String charsToDelete, String defaultReplacement, Map<Character, Character> replacements)
	{
		this.allowedChars = allowedChars.toCharArray();
		this.charsToDelete = charsToDelete.toCharArray();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.copyOf(replacements);
	}

	public CharReplacer(char[] allowedChars, char[] charsToDelete, String defaultReplacement)
	{
		this.allowedChars = allowedChars.clone();
		this.charsToDelete = charsToDelete.clone();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.of();
	}

	public CharReplacer(char[] allowedChars, char[] charsToDelete, String defaultReplacement, Map<Character, Character> replacements)
	{
		this.allowedChars = allowedChars.clone();
		this.charsToDelete = charsToDelete.clone();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.copyOf(replacements);
	}

	public char[] getAllowedChars()
	{
		return allowedChars.clone();
	}

	public char[] getCharsToDelete()
	{
		return charsToDelete.clone();
	}

	public String getDefaultReplacement()
	{
		return defaultReplacement;
	}

	public ImmutableMap<Character, Character> getReplacements()
	{
		return replacements;
	}

	@Override
	public String apply(String s)
	{
		if (s == null)
		{
			return null;
		}
		char[] src = s.toCharArray();
		StringBuilder dest = new StringBuilder(src.length);
		for (char c : src)
		{
			if (ArrayUtils.contains(charsToDelete, c))
			{
				continue;
			}
			if (ArrayUtils.contains(allowedChars, c))
			{
				dest.append(c);
				continue;
			}
			Character replacingChar = replacements.get(c);
			if (replacingChar != null)
			{
				appendIfNeitherEmptyNorEndsWith(dest, replacingChar.toString());
			}
			else
			{
				appendIfNeitherEmptyNorEndsWith(dest, defaultReplacement);
			}
		}
		// strip replacement at the end
		StringUtil.stripEnd(dest, defaultReplacement);
		return dest.toString();
	}

	private static final void appendIfNeitherEmptyNorEndsWith(StringBuilder sb, String s)
	{
		if (sb.length() > 0)
		{
			StringUtil.appendIfNotEndsWith(sb, s);
		}
	}
}
