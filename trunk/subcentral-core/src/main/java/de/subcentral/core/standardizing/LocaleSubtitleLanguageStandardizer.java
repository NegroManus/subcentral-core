package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LocaleSubtitleLanguageStandardizer implements Standardizer<Subtitle>
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
	private final ImmutableList<Locale>				parsingLanguages;
	// Formatting
	private final LanguageFormat					outputLanguageFormat;
	private final Locale							outputLanguage;
	// Custom parsing/formatting
	private final ImmutableList<LanguagePattern>	customLanguagePatterns;
	private final ImmutableMap<Locale, String>		customLanguageNames;

	public LocaleSubtitleLanguageStandardizer()
	{
		this(ImmutableList.of(Locale.ENGLISH), LanguageFormat.NAME, Locale.ENGLISH, ImmutableList.of(), ImmutableMap.of());
	}

	public LocaleSubtitleLanguageStandardizer(Collection<Locale> parsingLanguages, LanguageFormat outputLanguageFormat, Locale targetLanguage)
	{
		this(parsingLanguages, outputLanguageFormat, targetLanguage, ImmutableList.of(), ImmutableMap.of());
	}

	public LocaleSubtitleLanguageStandardizer(Collection<Locale> parsingLanguages, LanguageFormat outputLanguageFormat, Locale targetLanguage,
			List<LanguagePattern> customLanguagePatterns, Map<Locale, String> customLanguageNames)
	{
		this.parsingLanguages = ImmutableList.copyOf(parsingLanguages);
		this.outputLanguageFormat = Objects.requireNonNull(outputLanguageFormat, "outputLanguageFormat");
		this.outputLanguage = Objects.requireNonNull(targetLanguage, "outputLanguage");
		this.customLanguagePatterns = ImmutableList.copyOf(customLanguagePatterns);
		this.customLanguageNames = ImmutableMap.copyOf(customLanguageNames);
	}

	public ImmutableList<Locale> getParsingLanguages()
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

	public ImmutableList<LanguagePattern> getCustomLanguagePatterns()
	{
		return customLanguagePatterns;
	}

	public ImmutableMap<Locale, String> getCustomLanguageNames()
	{
		return customLanguageNames;
	}

	@Override
	public void standardize(Subtitle sub, List<StandardizingChange> changes)
	{
		if (sub == null || sub.getLanguage() == null)
		{
			return;
		}
		String oldLang = sub.getLanguage();
		String newLang = standardizeLang(oldLang);
		if (oldLang.equals(newLang))
		{
			return;
		}
		sub.setLanguage(newLang);
		changes.add(new StandardizingChange(sub, Subtitle.PROP_LANGUAGE.getPropName(), oldLang, newLang));
	}

	private String standardizeLang(String oldLang)
	{
		Locale locale = parseLocale(oldLang);
		if (locale != null)
		{
			return localeToString(locale);
		}
		return oldLang;
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
			if (locale.toString().equalsIgnoreCase(lang))
			{
				return locale;
			}
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
			if (locale.getCountry().isEmpty() && locale.getScript().isEmpty() && locale.getVariant().isEmpty())
			{
				// no need to check for language / display language because if it would match.
				// Because in that case toString() / getDisplayName() would have matched, too (if country, script, variant are empty)
				if (locale.getISO3Language().equalsIgnoreCase(lang))
				{
					return locale;
				}
			}
		}
		return null;
	}

	private String localeToString(Locale locale)
	{
		// 1. try the custom locale strings
		String customLocaleString = customLanguageNames.get(locale);
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
		return MoreObjects.toStringHelper(LocaleSubtitleLanguageStandardizer.class)
				.add("parsingLanguages", parsingLanguages)
				.add("outputLanguageFormat", outputLanguageFormat)
				.add("outputLanguage", outputLanguage)
				.add("customLanguagePatterns", customLanguagePatterns)
				.add("customLanguageNames", customLanguageNames)
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
				LanguagePattern o = (LanguagePattern) obj;
				return pattern.equals(o.pattern);
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
