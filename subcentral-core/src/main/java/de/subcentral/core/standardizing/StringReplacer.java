package de.subcentral.core.standardizing;

import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;

public class StringReplacer implements UnaryOperator<String>
{
	private final String	searchString;
	private final String	replacement;

	public StringReplacer(String searchString, String replacement)
	{
		this.searchString = searchString;
		this.replacement = replacement;
	}

	@Override
	public String apply(String text)
	{
		return StringUtils.replace(text, searchString, replacement);
	}
}
