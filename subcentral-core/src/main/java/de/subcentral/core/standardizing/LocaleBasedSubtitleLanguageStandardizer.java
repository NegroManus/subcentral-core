package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LocaleBasedSubtitleLanguageStandardizer implements Standardizer<Subtitle>
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
		 * display name. The whole name. e.g. "Portuguese (Brazil)". See {@link Locale#getDisplayName()}.
		 */
		DISPLAY_NAME,
		/**
		 * display language. Only the language. e.g. "Portuguese". See {@link Locale#getDisplayLanguage()}.
		 */
		DISPLAY_LANGUAGE
	};

	private final ImmutableMap<Pattern, Locale>	customLocalePatterns;
	private final ImmutableList<Locale>			possibleSourceLanguages;
	private final LanguageFormat				targetLanguageFormat;
	private final Locale						targetLanguage;
	private final ImmutableMap<Locale, String>	customLocaleStrings;

	public LocaleBasedSubtitleLanguageStandardizer()
	{
		this(ImmutableList.of(Locale.ENGLISH), LanguageFormat.NAME, Locale.ENGLISH, ImmutableMap.of(), ImmutableMap.of());
	}

	public LocaleBasedSubtitleLanguageStandardizer(Collection<Locale> possibleSourceLanguages, LanguageFormat targetLanguageFormat,
			Locale targetLanguage)
	{
		this(possibleSourceLanguages, targetLanguageFormat, targetLanguage, ImmutableMap.of(), ImmutableMap.of());
	}

	public LocaleBasedSubtitleLanguageStandardizer(Collection<Locale> possibleSourceLanguages, LanguageFormat targetLanguageFormat,
			Locale targetLanguage, Map<Pattern, Locale> customLocalePatterns, Map<Locale, String> customLocaleStrings)
	{
		this.possibleSourceLanguages = ImmutableList.copyOf(possibleSourceLanguages);
		this.targetLanguageFormat = Objects.requireNonNull(targetLanguageFormat, "targetLanguageFormat");
		this.targetLanguage = Objects.requireNonNull(targetLanguage, "targetLanguage");
		this.customLocalePatterns = ImmutableMap.copyOf(customLocalePatterns);
		this.customLocaleStrings = ImmutableMap.copyOf(customLocaleStrings);
	}

	public ImmutableList<Locale> getPossibleSourceLanguages()
	{
		return possibleSourceLanguages;
	}

	public LanguageFormat getTargetLanguageFormat()
	{
		return targetLanguageFormat;
	}

	public Locale getTargetLanguage()
	{
		return targetLanguage;
	}

	public ImmutableMap<Pattern, Locale> getCustomLocalePatterns()
	{
		return customLocalePatterns;
	}

	public ImmutableMap<Locale, String> getCustomLocaleStrings()
	{
		return customLocaleStrings;
	}

	@Override
	public List<StandardizingChange> standardize(Subtitle sub)
	{
		if (sub == null || sub.getLanguage() == null)
		{
			return null;
		}
		String oldLang = sub.getLanguage();
		String newLang = standardizeLang(oldLang);
		if (oldLang.equals(newLang))
		{
			return ImmutableList.of();
		}
		sub.setLanguage(newLang);
		return ImmutableList.of(new StandardizingChange(sub, Subtitle.PROP_LANGUAGE.getPropName(), oldLang, newLang));
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

	private Locale parseLocale(String s)
	{
		// 1. try the custom locale patterns
		for (Map.Entry<Pattern, Locale> pattern : customLocalePatterns.entrySet())
		{
			if (pattern.getKey().matcher(s).matches())
			{
				return pattern.getValue();
			}
		}

		// 2. try "parsing" the locale
		Locale[] availableLocales = Locale.getAvailableLocales();

		// first check: full qualified locale names
		for (Locale locale : availableLocales)
		{
			if (locale.toString().equalsIgnoreCase(s))
			{
				return locale;
			}
			if (locale.toLanguageTag().equalsIgnoreCase(s))
			{
				return locale;
			}
			for (Locale sourceLang : possibleSourceLanguages)
			{
				if (locale.getDisplayName(sourceLang).equalsIgnoreCase(s))
				{
					return locale;
				}
			}
			if (locale.getCountry().isEmpty() && locale.getScript().isEmpty() && locale.getVariant().isEmpty())
			{
				// no need to check for language / display language because if it would match.
				// Because in that case toString() / getDisplayName() would have matched, too (if country, script, variant are empty)
				if (locale.getISO3Language().equalsIgnoreCase(s))
				{
					return locale;
				}
			}
		}
		return null;
	}

	private String localeToString(Locale l)
	{
		// 1. try the custom locale strings
		String customLocaleString = customLocaleStrings.get(l);
		if (customLocaleString != null)
		{
			return customLocaleString;
		}

		// 2. print the language as specified
		switch (targetLanguageFormat)
		{
			case ISO2:
				return l.getLanguage();
			case ISO3:
				return l.getISO3Language();
			case DISPLAY_LANGUAGE:
				return l.getDisplayLanguage(targetLanguage);
			case DISPLAY_NAME:
				return l.getDisplayName(targetLanguage);
			case NAME:
				return l.toString();
			case LANGUAGE_TAG:
				return l.toLanguageTag();
			default:
				return l.getLanguage();
		}
	}

	public static class ToLocaleConverter
	{
		private final Pattern	pattern;
		private final Locale	locale;

		public ToLocaleConverter(Pattern pattern, Locale locale)
		{
			this.pattern = Objects.requireNonNull(pattern, "pattern");
			this.locale = Objects.requireNonNull(locale, "locale");
		}

		public Pattern getPattern()
		{
			return pattern;
		}

		public Locale getLocale()
		{
			return locale;
		}
	}
}
