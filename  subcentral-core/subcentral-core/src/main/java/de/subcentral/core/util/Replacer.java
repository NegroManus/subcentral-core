package de.subcentral.core.util;

import java.util.function.UnaryOperator;

public interface Replacer extends UnaryOperator<String>
{
	/**
	 * 
	 * @param s
	 *            The string to process. May be null.
	 * @param r
	 *            The Replacer. May be null.
	 * @return The string after replacement by the replacer or <code>null</code> if the String <code>s</code> is null or <code>s</code> if the
	 *         Replacer <code>r</code> is null.
	 */
	public static String replace(String s, Replacer r)
	{
		if (s == null)
		{
			return null;
		}
		return r == null ? s : r.replace(s);
	}

	public static Replacer toEmptyReplacer()
	{
		return s -> "";
	}

	public default String apply(String s)
	{
		return replace(s);
	}

	public String replace(String s);
}
