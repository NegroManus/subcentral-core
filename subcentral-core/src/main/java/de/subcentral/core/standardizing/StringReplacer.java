package de.subcentral.core.standardizing;

import java.util.Objects;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.MoreObjects;

public class StringReplacer implements UnaryOperator<String>
{
	public static enum Mode
	{
		REPLACE_EACH, REPLACE_WHOLLY
	}

	private final String	searchString;
	private final String	replacement;

	private final Mode		mode;

	public StringReplacer(String searchString, String replacement)
	{
		this(searchString, replacement, Mode.REPLACE_EACH);
	}

	public StringReplacer(String searchString, String replacement, Mode mode)
	{
		this.searchString = searchString;
		this.replacement = replacement;
		this.mode = Objects.requireNonNull(mode, "mode");
	}

	public String getSearchString()
	{
		return searchString;
	}

	public String getReplacement()
	{
		return replacement;
	}

	public Mode getMode()
	{
		return mode;
	}

	@Override
	public String apply(String text)
	{
		switch (mode)
		{
			case REPLACE_EACH:
				return StringUtils.replace(text, searchString, replacement);
			case REPLACE_WHOLLY:
				if (Objects.equals(text, searchString))
				{
					return replacement;
				}
				return text;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(StringReplacer.class)
				.add("searchString", searchString)
				.add("replacement", replacement)
				.add("mode", mode)
				.toString();
	}
}
