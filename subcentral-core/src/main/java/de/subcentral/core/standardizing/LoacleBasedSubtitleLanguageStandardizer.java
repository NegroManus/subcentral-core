package de.subcentral.core.standardizing;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LoacleBasedSubtitleLanguageStandardizer implements Standardizer<Subtitle>
{
	public enum LangFormat
	{
		/**
		 * ISO 639-1 two-letter code
		 */
		ISO2,
		/**
		 * ISO 639-2/T three-letter lowercase code
		 */
		ISO3,
		/**
		 * display language
		 */
		DISPLAY_LANGUAGE,
		/**
		 * display name
		 */
		DISPLAY_NAME,

		/**
		 * Java-Name. "pt_BR".
		 */
		NAME,

		/**
		 * Well-formed IETF BCP 47 language tag. e.g. "pt-BR".
		 */
		LANGUAGE_TAG
	};

	private final ImmutableList<Locale>	possibleSourceLanguages;
	private final LangFormat			targetLanguageFormat;
	private final Locale				targetLanguage;

	public LoacleBasedSubtitleLanguageStandardizer(Collection<Locale> possibleSourceLanguages, LangFormat targetLanguageFormat, Locale targetLanguage)
	{
		this.possibleSourceLanguages = ImmutableList.copyOf(possibleSourceLanguages);
		this.targetLanguageFormat = Objects.requireNonNull(targetLanguageFormat, "targetLanguageFormat");
		this.targetLanguage = Objects.requireNonNull(targetLanguage);
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
		Locale[] availableLocales = Locale.getAvailableLocales();
		Locale oldLocale = new Locale(oldLang);
		for (Locale locale : availableLocales)
		{
			if (locale.toString().equalsIgnoreCase(oldLang))
			{
				return calcNewLang(locale);
			}
			if (locale.toLanguageTag().equalsIgnoreCase(oldLang))
			{
				return calcNewLang(locale);
			}
			if (locale.getLanguage().equals(oldLocale.getLanguage()))
			{
				return calcNewLang(locale);
			}
			if (locale.getISO3Language().equalsIgnoreCase(oldLang))
			{
				return calcNewLang(locale);
			}
			for (Locale sourceLang : possibleSourceLanguages)
			{
				if (locale.getDisplayLanguage(sourceLang).equalsIgnoreCase(oldLang))
				{
					return calcNewLang(locale);
				}
			}
		}
		return oldLang;
	}

	private String calcNewLang(Locale l)
	{
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

}
