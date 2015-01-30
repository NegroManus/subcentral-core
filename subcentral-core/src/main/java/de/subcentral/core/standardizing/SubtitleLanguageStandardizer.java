package de.subcentral.core.standardizing;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class SubtitleLanguageStandardizer implements Standardizer<Subtitle>
{
	private final Pattern	languagePattern;
	private final String	languageReplacement;

	public SubtitleLanguageStandardizer(Pattern languagePattern, String languageReplacement)
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
	public List<StandardizingChange> standardize(Subtitle sub) throws StandardizingException
	{
		if (sub == null || sub.getLanguage() == null)
		{
			return ImmutableList.of();
		}
		if (languagePattern.matcher(sub.getLanguage()).matches())
		{
			String oldLang = sub.getLanguage();
			sub.setLanguage(languageReplacement);
			return ImmutableList.of(new StandardizingChange(sub, Subtitle.PROP_LANGUAGE.getPropName(), oldLang, sub.getLanguage()));
		}
		return ImmutableList.of();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof SubtitleLanguageStandardizer)
		{
			SubtitleLanguageStandardizer o = (SubtitleLanguageStandardizer) obj;
			return languagePattern.equals(o.languagePattern) && Objects.equals(languageReplacement, o.languageReplacement);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(95, 59).append(languagePattern).append(languageReplacement).toHashCode();
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
