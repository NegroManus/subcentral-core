package de.subcentral.core.util;

import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;

public class CharReplacer implements UnaryOperator<String>
{
	private char[]								allowedChars		= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-".toCharArray();
	private char[]								charsToDelete		= "'´`".toCharArray();
	private String								defaultReplacement	= ".";
	private ImmutableMap<Character, Character>	replacements		= ImmutableMap.of();

	public char[] getAllowedChars()
	{
		return allowedChars.clone();
	}

	public void setAllowedChars(char[] allowedChars)
	{
		this.allowedChars = allowedChars.clone();
	}

	public char[] getCharsToDelete()
	{
		return charsToDelete.clone();
	}

	public void setCharsToDelete(char[] charsToDelete)
	{
		this.charsToDelete = charsToDelete.clone();
	}

	public String getDefaultReplacement()
	{
		return defaultReplacement;
	}

	public void setDefaultReplacement(String defaultReplacement)
	{
		this.defaultReplacement = defaultReplacement;
	}

	public ImmutableMap<Character, Character> getReplacements()
	{
		return replacements;
	}

	public void setReplacements(ImmutableMap<Character, Character> replacements)
	{
		this.replacements = replacements;
	}

	@Override
	public String apply(String s)
	{
		if (s == null)
		{
			return null;
		}
		char[] src = s.toCharArray();
		StringBuilder dest = new StringBuilder(s.length());
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
				continue;
			}
			appendIfNeitherEmptyNorEndsWith(dest, defaultReplacement);
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
