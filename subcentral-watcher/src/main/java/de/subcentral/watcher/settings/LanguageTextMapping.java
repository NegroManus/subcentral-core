package de.subcentral.watcher.settings;

import java.util.Locale;
import java.util.Objects;

import javafx.util.StringConverter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.fx.FXUtil;

public final class LanguageTextMapping implements Comparable<LanguageTextMapping>
{
	public static final StringConverter<LanguageTextMapping>	STRING_CONVERTER	= initStringConverter();

	private static StringConverter<LanguageTextMapping> initStringConverter()
	{
		return new StringConverter<LanguageTextMapping>()
		{
			@Override
			public String toString(LanguageTextMapping mapping)
			{
				return mapping.language.getDisplayName() + " -> " + mapping.text;
			}

			@Override
			public LanguageTextMapping fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	final Locale	language;
	final String	text;

	public LanguageTextMapping(Locale language, String text)
	{
		this.language = Objects.requireNonNull(language, "language");
		this.text = Objects.requireNonNull(text, "text");
	}

	public Locale getLanguage()
	{
		return language;
	}

	public String getText()
	{
		return text;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof LanguageTextMapping)
		{
			LanguageTextMapping o = (LanguageTextMapping) obj;
			return language.equals(o.language);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(983, 133).append(language).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(LanguageTextMapping.class).omitNullValues().add("language", language).add("text", text).toString();
	}

	@Override
	public int compareTo(LanguageTextMapping o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR.compare(this.language, o.language);
	}
}