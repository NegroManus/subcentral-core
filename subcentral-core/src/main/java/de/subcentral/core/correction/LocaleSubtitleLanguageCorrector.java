package de.subcentral.core.correction;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LocaleSubtitleLanguageCorrector extends SinglePropertyCorrector<Subtitle, String>
{
	public LocaleSubtitleLanguageCorrector(LocaleLanguageReplacer replacer)
	{
		super(replacer);
	}

	@Override
	public Class<Subtitle> getBeanType()
	{
		return Subtitle.class;
	}

	@Override
	public String getPropertyName()
	{
		return Subtitle.PROP_LANGUAGE.getPropName();
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
