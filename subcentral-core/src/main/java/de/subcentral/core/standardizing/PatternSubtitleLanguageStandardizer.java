package de.subcentral.core.standardizing;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class PatternSubtitleLanguageStandardizer extends SinglePropertyStandardizer<Subtitle, String>
{
    public PatternSubtitleLanguageStandardizer(PatternStringReplacer replacer)
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
    public PatternStringReplacer getReplacer()
    {
	return (PatternStringReplacer) super.getReplacer();
    }

}
