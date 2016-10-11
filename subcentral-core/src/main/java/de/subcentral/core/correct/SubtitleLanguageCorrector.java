package de.subcentral.core.correct;

import java.util.function.Function;

import de.subcentral.core.metadata.subtitle.Subtitle;

public class SubtitleLanguageCorrector extends SinglePropertyCorrector<Subtitle, String> {
    public SubtitleLanguageCorrector(Function<String, String> replacer) {
        super(replacer);
    }

    @Override
    public String getPropertyName() {
        return Subtitle.PROP_LANGUAGE.getPropName();
    }

    @Override
    protected String getValue(Subtitle bean) {
        return bean.getLanguage();
    }

    @Override
    protected void setValue(Subtitle bean, String value) {
        bean.setLanguage(value);
    }
}
