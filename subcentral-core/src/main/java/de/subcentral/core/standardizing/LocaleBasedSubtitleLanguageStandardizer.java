package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LocaleBasedSubtitleLanguageStandardizer implements Standardizer<Subtitle>
{
	public enum LangFormat
	{
		/**
		 * ISO 639-1 two-letter code. See {@link Locale#getLanguage()}.
		 */
		ISO2,
		/**
		 * ISO 639-2/T three-letter lowercase code. See {@link Locale#getISO3Language()}.
		 */
		ISO3,
		/**
		 * display language. Only the language. e.g. "Portuguese". See {@link Locale#getDisplayLanguage()}.
		 */
		DISPLAY_LANGUAGE,
		/**
		 * display name. The whole name. e.g. "Portuguese (Brazil)". See {@link Locale#getDisplayName()}.
		 */
		DISPLAY_NAME,

		/**
		 * Java-Name. "pt_BR". See {@link Locale#toString()}.
		 */
		NAME,

		/**
		 * Well-formed IETF BCP 47 language tag. e.g. "pt-BR". See {@link Locale#toLanguageTag()}.
		 */
		LANGUAGE_TAG
	};

	private final ImmutableList<Function<String, Locale>>	customToLocaleConverters;
	private final ImmutableList<Locale>						possibleSourceLanguages;
	private final LangFormat								targetLanguageFormat;
	private final Locale									targetLanguage;
	private final ImmutableList<Function<Locale, String>>	customFromLocaleConverters;

	public LocaleBasedSubtitleLanguageStandardizer(Collection<Locale> possibleSourceLanguages, LangFormat targetLanguageFormat, Locale targetLanguage)
	{
		this(possibleSourceLanguages, targetLanguageFormat, targetLanguage, ImmutableList.of(), ImmutableList.of());
	}

	public LocaleBasedSubtitleLanguageStandardizer(Collection<Locale> possibleSourceLanguages, LangFormat targetLanguageFormat,
			Locale targetLanguage, Collection<Function<Locale, String>> customFromLocaleConverters,
			Collection<Function<String, Locale>> customToLocaleConverters)
	{
		this.possibleSourceLanguages = ImmutableList.copyOf(possibleSourceLanguages);
		this.targetLanguageFormat = Objects.requireNonNull(targetLanguageFormat, "targetLanguageFormat");
		this.targetLanguage = Objects.requireNonNull(targetLanguage, "targetLanguage");
		this.customFromLocaleConverters = ImmutableList.copyOf(customFromLocaleConverters);
		this.customToLocaleConverters = ImmutableList.copyOf(customToLocaleConverters);
	}

	public ImmutableList<Locale> getPossibleSourceLanguages()
	{
		return possibleSourceLanguages;
	}

	public LangFormat getTargetLanguageFormat()
	{
		return targetLanguageFormat;
	}

	public Locale getTargetLanguage()
	{
		return targetLanguage;
	}

	public ImmutableList<Function<String, Locale>> getCustomToLocaleConverters()
	{
		return customToLocaleConverters;
	}

	public ImmutableList<Function<Locale, String>> getCustomFromLocaleConverters()
	{
		return customFromLocaleConverters;
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
		// 1. try custom to-locale converters
		for (Function<String, Locale> converter : customToLocaleConverters)
		{
			Locale locale = converter.apply(s);
			if (locale != null)
			{
				return locale;
			}
		}

		// 2. try "parsing" the locale
		Locale[] availableLocales = Locale.getAvailableLocales();
		Locale localeFromString = new Locale(s);
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
			if (locale.getLanguage().equals(localeFromString.getLanguage()))
			{
				return locale;
			}
			if (locale.getISO3Language().equalsIgnoreCase(s))
			{
				return locale;
			}
			for (Locale sourceLang : possibleSourceLanguages)
			{
				if (locale.getDisplayLanguage(sourceLang).equalsIgnoreCase(s))
				{
					return locale;
				}
				if (locale.getDisplayName(sourceLang).equalsIgnoreCase(s))
				{
					return locale;
				}
			}
		}
		return null;
	}

	private String localeToString(Locale l)
	{
		// 1. try the custom from-locale converters
		for (Function<Locale, String> converter : customFromLocaleConverters)
		{
			String string = converter.apply(l);
			if (string != null)
			{
				return string;
			}
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

	public static class ToLocaleConversionEntry
	{
		private final Pattern	pattern;
		private final Locale	locale;

		public ToLocaleConversionEntry(Pattern pattern, Locale locale)
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

	public static class FromLocaleConversionEntry
	{
		private final Locale	locale;
		private final String	string;

		private FromLocaleConversionEntry(Locale locale, String string)
		{
			this.locale = Objects.requireNonNull(locale, "locale");
			this.string = Objects.requireNonNull(string, "string");
		}

		public Locale getLocale()
		{
			return locale;
		}

		public String getString()
		{
			return string;
		}
	}
}
