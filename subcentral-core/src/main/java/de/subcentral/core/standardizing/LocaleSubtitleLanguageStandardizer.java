package de.subcentral.core.standardizing;

import java.util.function.Function;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class LocaleSubtitleLanguageStandardizer extends SinglePropertyStandardizer<Subtitle, String>
{
    public LocaleSubtitleLanguageStandardizer(LocaleLanguageReplacer replacer)
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

    @Override
    public Function<String, String> getReplacer()
    {
	return (LocaleLanguageReplacer) super.getReplacer();
    }

}
