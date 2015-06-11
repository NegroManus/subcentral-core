package de.subcentral.watcher.settings;

import java.util.Locale;

import javafx.util.StringConverter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.fx.UserPattern;

public class LanguageUiPattern implements Comparable<LanguageUiPattern>
{
	public static final StringConverter<LanguageUiPattern>	STRING_CONVERTER	= initStringConverter();

	private static StringConverter<LanguageUiPattern> initStringConverter()
	{
		return new StringConverter<LanguageUiPattern>()
		{
			@Override
			public String toString(LanguageUiPattern pattern)
			{
				return pattern.pattern.getPattern() + " (" + pattern.pattern.getMode() + ") -> " + pattern.language.getDisplayName();
			}

			@Override
			public LanguageUiPattern fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private final UserPattern	pattern;
	private final Locale	language;

	public LanguageUiPattern(UserPattern pattern, Locale language)
	{
		this.pattern = pattern;
		this.language = language;
	}

	public UserPattern getPattern()
	{
		return pattern;
	}

	public Locale getLanguage()
	{
		return language;
	}

	public LanguagePattern toLanguagePattern()
	{
		return new LanguagePattern(pattern.toPattern(), language);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof LanguageUiPattern)
		{
			LanguageUiPattern o = (LanguageUiPattern) obj;
			return pattern.equals(o.pattern);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(73, 113).append(pattern).toHashCode();
	}

	@Override
	public int compareTo(LanguageUiPattern o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return pattern.compareTo(o.pattern);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(LanguageUiPattern.class).omitNullValues().add("pattern", pattern).add("language", language).toString();
	}
}
