package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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
		 * display name. The whole name. e.g. "Portuguese (Brazil)". See {@link Locale#getDisplayName()}.
		 */
		DISPLAY_NAME,
		/**
		 * display language. Only the language. e.g. "Portuguese". See {@link Locale#getDisplayLanguage()}.
		 */
		DISPLAY_LANGUAGE
	};

	private final ImmutableList<Locale>			possibleInputLanguages;
	private final LanguageFormat				outputLanguageFormat;
	private final Locale						outputLanguage;
	private final ImmutableMap<Pattern, Locale>	customLanguagePatterns;
	private final ImmutableMap<Locale, String>	customLanguageNames;

	public LocaleSubtitleLanguageStandardizer()
	{
		this(ImmutableList.of(Locale.ENGLISH), LanguageFormat.NAME, Locale.ENGLISH, ImmutableMap.of(), ImmutableMap.of());
	}

	public LocaleSubtitleLanguageStandardizer(Collection<Locale> possibleInputLanguages, LanguageFormat outputLanguageFormat, Locale targetLanguage)
	{
		this(possibleInputLanguages, outputLanguageFormat, targetLanguage, ImmutableMap.of(), ImmutableMap.of());
	}

	public LocaleSubtitleLanguageStandardizer(Collection<Locale> possibleInputLanguages, LanguageFormat outputLanguageFormat, Locale targetLanguage,
			Map<Pattern, Locale> customLanguagePatterns, Map<Locale, String> customLanguageNames)
	{
		this.possibleInputLanguages = ImmutableList.copyOf(possibleInputLanguages);
		this.outputLanguageFormat = Objects.requireNonNull(outputLanguageFormat, "outputLanguageFormat");
		this.outputLanguage = Objects.requireNonNull(targetLanguage, "outputLanguage");
		this.customLanguagePatterns = ImmutableMap.copyOf(customLanguagePatterns);
		this.customLanguageNames = ImmutableMap.copyOf(customLanguageNames);
	}

	public ImmutableList<Locale> getPossibleInputLanguages()
	{
		return possibleInputLanguages;
	}

	public LanguageFormat getOutputLanguageFormat()
	{
		return outputLanguageFormat;
	}

	public Locale getOutputLanguage()
	{
		return outputLanguage;
	}

	public ImmutableMap<Pattern, Locale> getCustomLanguagePatterns()
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
		for (Map.Entry<Pattern, Locale> pattern : customLanguagePatterns.entrySet())
		{
			if (pattern.getKey().matcher(lang).matches())
			{
				return pattern.getValue();
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
			for (Locale sourceLang : possibleInputLanguages)
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
				.add("possibleInputLanguages", possibleInputLanguages)
				.add("outputLanguageFormat", outputLanguageFormat)
				.add("outputLanguage", outputLanguage)
				.add("customLanguagePatterns", customLanguagePatterns)
				.add("customLanguageNames", customLanguageNames)
				.toString();
	}
}
