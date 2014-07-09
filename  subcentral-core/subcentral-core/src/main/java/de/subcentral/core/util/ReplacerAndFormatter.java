package de.subcentral.core.util;

import java.util.function.UnaryOperator;

public class ReplacerAndFormatter implements UnaryOperator<String>
{
	private final Replacer	replacer;
	private final String	format;

	public ReplacerAndFormatter(Replacer replacer, String format)
	{
		this.replacer = replacer;
		this.format = format;
	}

	@Override
	public String apply(String s)
	{
		if (s == null)
		{
			return null;
		}
		if (format == null)
		{
			return Replacer.replace(s, replacer);
		}
		return String.format(format, Replacer.replace(s, replacer));
	}
}
