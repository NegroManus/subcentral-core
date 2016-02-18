package de.subcentral.watcher.settings;

import java.util.Locale;

import javafx.util.StringConverter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.correct.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.fx.UserPattern;

public class LanguageUserPattern implements Comparable<LanguageUserPattern>
{
	public static final StringConverter<LanguageUserPattern> STRING_CONVERTER = initStringConverter();

	private static StringConverter<LanguageUserPattern> initStringConverter()
	{
		return new StringConverter<LanguageUserPattern>()
		{
			@Override
			public String toString(LanguageUserPattern pattern)
			{
				return pattern.pattern.getPattern() + " (" + pattern.pattern.getMode() + ") -> " + pattern.language.getDisplayName();
			}

			@Override
			public LanguageUserPattern fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	private final UserPattern	pattern;
	private final Locale		language;

	public LanguageUserPattern(UserPattern pattern, Locale language)
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
		if (obj instanceof LanguageUserPattern)
		{
			LanguageUserPattern o = (LanguageUserPattern) obj;
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
	public int compareTo(LanguageUserPattern o)
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
		return MoreObjects.toStringHelper(LanguageUserPattern.class).omitNullValues().add("pattern", pattern).add("language", language).toString();
	}
}
