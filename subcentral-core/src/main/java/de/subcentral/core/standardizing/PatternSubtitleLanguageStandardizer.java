package de.subcentral.core.standardizing;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class PatternSubtitleLanguageStandardizer extends SinglePropertyStandardizer<Subtitle, String, PatternStringReplacer>
{
	public PatternSubtitleLanguageStandardizer(PatternStringReplacer replacer)
	{
		super(Subtitle.class, Subtitle.PROP_LANGUAGE.getPropName(), replacer);
	}

	@Override
	protected String getValue(Subtitle bean)
	{
		return bean.getLanguage();
	}

	@Override
	protected void setValue(Subtitle bean, String value)
	{
		bean.setLanguage(value);
	}
}
