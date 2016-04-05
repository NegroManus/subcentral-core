package de.subcentral.watcher.settings;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.fx.FxUtil;
import javafx.util.StringConverter;

public final class LanguageToTextMapping implements Map.Entry<Locale, String>, Comparable<LanguageToTextMapping>
{
	final Locale	language;
	final String	text;

	public LanguageToTextMapping(Locale language, String text)
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

	// Map.Entry implementation
	@Override
	public Locale getKey()
	{
		return language;
	}

	@Override
	public String getValue()
	{
		return text;
	}

	@Override
	public String setValue(String value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof LanguageToTextMapping)
		{
			LanguageToTextMapping o = (LanguageToTextMapping) obj;
			return language.equals(o.language) && text.equals(o.text);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(983, 133).append(language).append(text).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(LanguageToTextMapping.class).omitNullValues().add("language", language).add("text", text).toString();
	}

	@Override
	public int compareTo(LanguageToTextMapping o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR.compare(this.language, o.language);
	}

	public static StringConverter<LanguageToTextMapping> createStringConverter()
	{
		return new StringConverter<LanguageToTextMapping>()
		{
			@Override
			public String toString(LanguageToTextMapping mapping)
			{
				return mapping.language.getDisplayName() + " -> " + mapping.text;
			}

			@Override
			public LanguageToTextMapping fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}