package de.subcentral.core.standardizing;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class PatternSubtitleLanguageStandardizer implements Standardizer<Subtitle>
{
	private final Pattern	languagePattern;
	private final String	languageReplacement;

	public PatternSubtitleLanguageStandardizer(Pattern languagePattern, String languageReplacement)
	{
		this.languagePattern = languagePattern;
		this.languageReplacement = languageReplacement;
	}

	public Pattern getLanguagePattern()
	{
		return languagePattern;
	}

	public String getLanguageReplacement()
	{
		return languageReplacement;
	}

	@Override
	public void standardize(Subtitle sub, List<StandardizingChange> changes)
	{
		if (sub == null || sub.getLanguage() == null)
		{
			return;
		}
		if (languagePattern.matcher(sub.getLanguage()).matches())
		{
			String oldLang = sub.getLanguage();
			sub.setLanguage(languageReplacement);
			changes.add(new StandardizingChange(sub, Subtitle.PROP_LANGUAGE.getPropName(), oldLang, sub.getLanguage()));
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(SeriesNameStandardizer.class)
				.add("languagePattern", languagePattern)
				.add("languageReplacement", languageReplacement)
				.toString();
	}
}
