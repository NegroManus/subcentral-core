package de.subcentral.core.util;

public interface Replacer
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
		return r == null ? s : r.process(s);
	}

	public String process(String s);
}
