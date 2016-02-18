package de.subcentral.core.correct;

import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableMap;

/**
 * @implSpec #immutable #thread-safe
 */
public class CharStringReplacer implements UnaryOperator<String>
{
	private final char[]					allowedChars;
	private final char[]					charsToDelete;
	private final char						defaultReplacement;
	private final Map<Character, Character>	replacements;

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

	public Map<Character, Character> getReplacements()
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

		StringBuilder dest = new StringBuilder(s.length());
		dest.append(s);
		for (int i = 0; i < dest.length(); i++)
		{
			char c = dest.charAt(i);
			if (c == defaultReplacement && (i == 0 || dest.charAt(i - 1) == defaultReplacement || i == dest.length() - 1))
			{
				dest.deleteCharAt(i);
				i--;
			}
			else if (contains(allowedChars, c))
			{
				// keep the char
			}
			else if (contains(charsToDelete, c))
			{
				dest.deleteCharAt(i);
				i--;
			}
			else
			{
				Character replacingChar = replacements.get(c);
				if (replacingChar != null)
				{
					dest.setCharAt(i, replacingChar);
				}
				else
				{
					if (i == 0 || dest.charAt(i - 1) == defaultReplacement || i == dest.length() - 1)
					{
						dest.deleteCharAt(i);
						i--;
					}
					else
					{
						dest.setCharAt(i, defaultReplacement);
					}
				}
			}
		}
		return dest.toString();
	}

	private static boolean contains(char[] array, char charToFind)
	{
		for (char c : array)
		{
			if (c == charToFind)
			{
				return true;
			}
		}
		return false;
	}
}
