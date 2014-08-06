package de.subcentral.core.util;

import java.util.function.UnaryOperator;

public class FunctionUtil
{
	/**
	 * 
	 * @param s
	 *            The string to process. May be null.
	 * @param op
	 *            The operator. May be null.
	 * @return The string after processed by op or <code>null</code> if the String <code>s</code> is null or <code>s</code> if the op <code>r</code>
	 *         is null.
	 */
	public static String applyStringOperator(String s, UnaryOperator<String> op)
	{
		if (s == null)
		{
			return null;
		}
		return op == null ? s : op.apply(s);
	}

	public static UnaryOperator<String> toEmptyOperator()
	{
		return s -> "";
	}

	private FunctionUtil()
	{
		// utilty
	}
}
