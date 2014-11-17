package de.subcentral.core.util;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

/**
 * @implSpec #immutable #thread-safe
 */
public class PatternReplacer implements UnaryOperator<String>
{
	private final ImmutableMap<Pattern, String>	replacements;

	public PatternReplacer(Map<Pattern, String> replacements)
	{
		this.replacements = ImmutableMap.copyOf(replacements); // null check included
	}

	public ImmutableMap<Pattern, String> getReplacements()
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
		for (Map.Entry<Pattern, String> entry : replacements.entrySet())
		{
			s = entry.getKey().matcher(s).replaceAll(entry.getValue());
		}
		return s;
	}
}
