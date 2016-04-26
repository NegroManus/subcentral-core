package de.subcentral.watcher.settings;

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxUtil;
import javafx.util.StringConverter;

public final class LanguageToTextMapping implements Comparable<LanguageToTextMapping>
{
	public static final StringConverter<LanguageToTextMapping>	STRING_CONVERTER	= initStringConverter();

	private final Locale										language;
	private final String										text;

	public LanguageToTextMapping(Locale language, String text)
	{
		this.language = Objects.requireNonNull(language, "language");
		this.text = Objects.requireNonNull(text, "text");
	}

	private static StringConverter<LanguageToTextMapping> initStringConverter()
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
		if (obj instanceof LanguageToTextMapping)
		{
			LanguageToTextMapping o = (LanguageToTextMapping) obj;
			return language.equals(o.language) && text.equals(text);
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
		return ComparisonChain.start().compare(language, o.language, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR).compare(text, o.text, ObjectUtil.getDefaultStringOrdering()).result();
	}
}