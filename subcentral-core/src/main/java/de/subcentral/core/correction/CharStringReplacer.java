package de.subcentral.core.correction;

import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.StringUtil;

/**
 * @implSpec #immutable #thread-safe
 */
public class CharStringReplacer implements UnaryOperator<String>
{
	private final char[]								allowedChars;
	private final char[]								charsToDelete;
	private final char									defaultReplacement;
	private final ImmutableMap<Character, Character>	replacements;

	public CharStringReplacer(String allowedChars, String charsToDelete, char defaultReplacement)
	{
		this(allowedChars, charsToDelete, defaultReplacement, ImmutableMap.of());
	}

	public CharStringReplacer(String allowedChars, String charsToDelete, char defaultReplacement, Map<Character, Character> replacements)
	{
		this.allowedChars = Objects.requireNonNull(allowedChars, "allowedChars").toCharArray();
		this.charsToDelete = Objects.requireNonNull(charsToDelete, "charsToDelete").toCharArray();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.copyOf(replacements); // null check included
	}

	public CharStringReplacer(char[] allowedChars, char[] charsToDelete, char defaultReplacement)
	{
		this(allowedChars, charsToDelete, defaultReplacement, ImmutableMap.of());
	}

	public CharStringReplacer(char[] allowedChars, char[] charsToDelete, char defaultReplacement, Map<Character, Character> replacements)
	{
		this.allowedChars = Objects.requireNonNull(allowedChars, "allowedChars").clone();
		this.charsToDelete = Objects.requireNonNull(charsToDelete, "charsToDelete").clone();
		this.defaultReplacement = defaultReplacement;
		this.replacements = ImmutableMap.copyOf(replacements); // null check included
	}

	public char[] getAllowedChars()
	{
		return allowedChars.clone();
	}

	public char[] getCharsToDelete()
	{
		return charsToDelete.clone();
	}

	public char getDefaultReplacement()
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
			if (c == defaultReplacement)
			{
				appendIfNeitherEmptyNorEndsWith(dest, defaultReplacement);
			}
			else if (ArrayUtils.contains(allowedChars, c))
			{
				dest.append(c);
			}
			else if (!ArrayUtils.contains(charsToDelete, c))
			{
				Character replacingChar = replacements.get(c);
				if (replacingChar != null)
				{
					dest.append(replacingChar.charValue());
				}
				else
				{
					appendIfNeitherEmptyNorEndsWith(dest, defaultReplacement);
				}
			}
		}
		// strip replacement at the end
		StringUtil.stripEnd(dest, defaultReplacement);
		return dest.toString();
	}

	private static final void appendIfNeitherEmptyNorEndsWith(StringBuilder sb, char s)
	{
		if (sb.length() > 0)
		{
			StringUtil.appendIfNotEndsWith(sb, s);
		}
	}
}
