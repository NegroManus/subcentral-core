package de.subcentral.core.correct;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class LocaleLanguageReplacer implements UnaryOperator<String>
{
	public enum LanguageFormat
	{
		/**
		 * Java-Name. "pt_BR". See {@link Locale#toString()}.
		 */
		NAME,

		/**
		 * Well-formed IETF BCP 47 language tag. e.g. "pt-BR". See {@link Locale#toLanguageTag()}.
		 */
		LANGUAGE_TAG,

		/**
		 * ISO 639-1 two-letter code. See {@link Locale#getLanguage()}.
		 */
		ISO2,

		/**
		 * ISO 639-2/T three-letter lowercase code. See {@link Locale#getISO3Language()}.
		 */
		ISO3,

		/**
		 * Display name. The whole name. e.g. "Portuguese (Brazil)". See {@link Locale#getDisplayName()}.
		 */
		DISPLAY_NAME,

		/**
		 * Display language. Only the language. e.g. "Portuguese". See {@link Locale#getDisplayLanguage()}.
		 */
		DISPLAY_LANGUAGE
	};

	// Parsing
	private final List<Locale>			parsingLanguages;
	// Formatting
	private final LanguageFormat		outputLanguageFormat;
	private final Locale				outputLanguage;
	// Custom parsing/formatting
	private final List<LanguagePattern>	customLanguagePatterns;
	private final Map<Locale, String>	customLanguageTextMappings;

	public LocaleLanguageReplacer()
	{
		this(ImmutableList.of(Locale.ENGLISH), LanguageFormat.NAME, Locale.ENGLISH, ImmutableList.of(), ImmutableMap.of());
	}

	public LocaleLanguageReplacer(Collection<Locale> parsingLanguages, LanguageFormat outputLanguageFormat, Locale targetLanguage)
	{
		this(parsingLanguages, outputLanguageFormat, targetLanguage, ImmutableList.of(), ImmutableMap.of());
	}

	public LocaleLanguageReplacer(Collection<Locale> parsingLanguages,
			LanguageFormat outputLanguageFormat,
			Locale targetLanguage,
			List<LanguagePattern> customLanguagePatterns,
			Map<Locale, String> customLanguageTextMappings)
	{
		this.parsingLanguages = ImmutableList.copyOf(parsingLanguages);
		this.outputLanguageFormat = Objects.requireNonNull(outputLanguageFormat, "outputLanguageFormat");
		this.outputLanguage = Objects.requireNonNull(targetLanguage, "outputLanguage");
		this.customLanguagePatterns = ImmutableList.copyOf(customLanguagePatterns);
		this.customLanguageTextMappings = ImmutableMap.copyOf(customLanguageTextMappings);
	}

	public List<Locale> getParsingLanguages()
	{
		return parsingLanguages;
	}

	public LanguageFormat getOutputLanguageFormat()
	{
		return outputLanguageFormat;
	}

	public Locale getOutputLanguage()
	{
		return outputLanguage;
	}

	public List<LanguagePattern> getCustomLanguagePatterns()
	{
		return customLanguagePatterns;
	}

	public Map<Locale, String> getCustomLanguageTextMappings()
	{
		return customLanguageTextMappings;
	}

	@Override
	public String apply(String lang)
	{
		if (lang == null)
		{
			return null;
		}
		Locale oldLocale = parseLocale(lang);
		if (oldLocale != null)
		{
			return formatLocale(oldLocale);
		}
		return lang;
	}

	private Locale parseLocale(String lang)
	{
		// 1. try the custom locale patterns
		for (LanguagePattern langPattern : customLanguagePatterns)
		{
			if (langPattern.pattern.matcher(lang).matches())
			{
				return langPattern.language;
			}
		}

		// 2. try "parsing" the locale
		for (Locale locale : Locale.getAvailableLocales())
		{
			// Java language tag
			if (locale.toString().equalsIgnoreCase(lang))
			{
				return locale;
			}
			// IETF language tag
			// cannot use Locale.forLanguageTag() because it accepts any string (not only valid languages)
			if (locale.toLanguageTag().equalsIgnoreCase(lang))
			{
				return locale;
			}
			for (Locale sourceLang : parsingLanguages)
			{
				if (locale.getDisplayName(sourceLang).equalsIgnoreCase(lang))
				{
					return locale;
				}
			}
			// ISO3
			// No need to check for language / display language if it would match.
			// Because in that case toString() / getDisplayName() would have matched, too (if country, script, variant are empty)
			if (locale.getCountry().isEmpty() && locale.getScript().isEmpty() && locale.getVariant().isEmpty() && locale.getISO3Language().equalsIgnoreCase(lang))
			{
				return locale;
			}
		}
		return null;
	}

	private String formatLocale(Locale locale)
	{
		// 1. try the custom locale strings
		String customLocaleString = customLanguageTextMappings.get(locale);
		if (customLocaleString != null)
		{
			return customLocaleString;
		}

		// 2. print the language as specified
		switch (outputLanguageFormat)
		{
			case NAME:
				return locale.toString();
			case LANGUAGE_TAG:
				return locale.toLanguageTag();
			case ISO2:
				return locale.getLanguage();
			case ISO3:
				return locale.getISO3Language();
			case DISPLAY_NAME:
				return locale.getDisplayName(outputLanguage);
			case DISPLAY_LANGUAGE:
				return locale.getDisplayLanguage(outputLanguage);
			default:
				return locale.toString();
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(LocaleLanguageReplacer.class)
				.add("parsingLanguages", parsingLanguages)
				.add("outputLanguageFormat", outputLanguageFormat)
				.add("outputLanguage", outputLanguage)
				.add("customLanguagePatterns", customLanguagePatterns)
				.add("customLanguageTextMappings", customLanguageTextMappings)
				.toString();
	}

	public final static class LanguagePattern
	{
		private final Pattern	pattern;
		private final Locale	language;

		public LanguagePattern(Pattern pattern, Locale lang)
		{
			this.pattern = Objects.requireNonNull(pattern, "pattern");
			this.language = Objects.requireNonNull(lang, "language");
		}

		public Pattern getPattern()
		{
			return pattern;
		}

		public Locale getLanguage()
		{
			return language;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof LanguagePattern)
			{
				return pattern.equals(((LanguagePattern) obj).pattern);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(983, 133).append(pattern).toHashCode();
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(LanguagePattern.class).omitNullValues().add("pattern", pattern).add("language", language).toString();
		}
	}
}
