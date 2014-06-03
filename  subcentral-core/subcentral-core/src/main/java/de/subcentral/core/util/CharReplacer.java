package de.subcentral.core.util;

import org.apache.commons.lang3.ArrayUtils;

public class CharReplacer implements Replacer
{
	private char[]	allowedChars	= new char[] {};
	private String	replacement		= "";
	private char[]	charsToDelete	= new char[] {};

	public char[] getAllowedChars()
	{
		return allowedChars;
	}

	public void setAllowedChars(char[] allowedChars)
	{
		this.allowedChars = allowedChars;
	}

	public String getReplacement()
	{
		return replacement;
	}

	public void setReplacement(String replacement)
	{
		this.replacement = replacement;
	}

	public char[] getCharsToDelete()
	{
		return charsToDelete;
	}

	public void setCharsToDelete(char[] charsToDelete)
	{
		this.charsToDelete = charsToDelete;
	}

	@Override
	public String process(String s)
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
			}
			else
			{
				// do not append replacement at the beginning
				if (dest.length() > 0)
				{
					// do not append replacement at the end
					// if StringBuilder already ends with replacement
					StringUtil.append(dest, replacement);
				}
			}
		}
		// strip replacement at the end
		StringUtil.deleteTrailing(dest, replacement);
		return dest.toString();
	}
}
