package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.watcher.model.ObservableBean;

public class LocaleLanguageReplacerSettings extends ObservableBean
{
	private final ListProperty<Locale>				parsingLanguages			= new SimpleListProperty<>(this, "parsingLanguages");
	private final Property<LanguageFormat>			outputLanguageFormat		= new SimpleObjectProperty<>(this,
																						"outputLanguageFormat",
																						LanguageFormat.NAME);
	private final Property<Locale>					outputLanguage				= new SimpleObjectProperty<>(this, "outputLanguage", Locale.ENGLISH);
	private final ListProperty<LanguageUiPattern>	customLanguagePatterns		= new SimpleListProperty<>(this, "customLanguagePatterns");
	private final ListProperty<LanguageTextMapping>	customLanguageTextMappings	= new SimpleListProperty<>(this, "customLanguageTextMappings");

	// package protected
	LocaleLanguageReplacerSettings()
	{
		super.bind(parsingLanguages, outputLanguageFormat, outputLanguage, customLanguagePatterns, customLanguageTextMappings);
	}

	public void load(XMLConfiguration cfg, String key)
	{
		List<HierarchicalConfiguration<ImmutableNode>> parsingLangsCfgs = cfg.configurationsAt(key + ".parsingLanguages.language");
		List<Locale> parsingLangs = new ArrayList<>(parsingLangsCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> parsingLangCfg : parsingLangsCfgs)
		{
			parsingLangs.add(Locale.forLanguageTag(parsingLangCfg.getString("[@tag]")));
		}
		parsingLangs.sort(FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR);
		setParsingLanguages(FXCollections.observableList(parsingLangs));

		LanguageFormat outputFormat = LanguageFormat.valueOf(cfg.getString(key + ".ouputLanguageFormat[@format]"));
		setOutputLanguageFormat(outputFormat);
		Locale outputLang = Locale.forLanguageTag(cfg.getString(key + ".ouputLanguage[@tag]"));
		setOutputLanguage(outputLang);

		List<HierarchicalConfiguration<ImmutableNode>> patternsCfgs = cfg.configurationsAt(key + ".customLanguagePatterns.languagePattern");
		List<LanguageUiPattern> patterns = new ArrayList<>(patternsCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> patternsCfg : patternsCfgs)
		{
			UserPattern p = new UserPattern(patternsCfg.getString("[@pattern]"), Mode.valueOf(patternsCfg.getString("[@patternMode]")));
			LanguageUiPattern langPattern = new LanguageUiPattern(p, Locale.forLanguageTag(patternsCfg.getString("[@languageTag]")));
			patterns.add(langPattern);
		}
		setCustomLanguagePatterns(FXCollections.observableList(patterns));

		List<HierarchicalConfiguration<ImmutableNode>> namesCfgs = cfg.configurationsAt(key + ".customLanguageNames.languageName");
		List<LanguageTextMapping> langTextMappings = new ArrayList<>(namesCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> namesCfg : namesCfgs)
		{
			langTextMappings.add(new LanguageTextMapping(Locale.forLanguageTag(namesCfg.getString("[@tag]")), namesCfg.getString("[@name]")));
		}
		setCustomLanguageTextMappings(FXCollections.observableList(langTextMappings));
	}

	public final ListProperty<Locale> parsingLanguagesProperty()
	{
		return this.parsingLanguages;
	}

	public final javafx.collections.ObservableList<java.util.Locale> getParsingLanguages()
	{
		return this.parsingLanguagesProperty().get();
	}

	public final void setParsingLanguages(final javafx.collections.ObservableList<java.util.Locale> parsingLanguages)
	{
		this.parsingLanguagesProperty().set(parsingLanguages);
	}

	public final Property<LanguageFormat> outputLanguageFormatProperty()
	{
		return this.outputLanguageFormat;
	}

	public final de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat getOutputLanguageFormat()
	{
		return this.outputLanguageFormatProperty().getValue();
	}

	public final void setOutputLanguageFormat(final de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat outputLanguageFormat)
	{
		this.outputLanguageFormatProperty().setValue(outputLanguageFormat);
	}

	public final Property<Locale> outputLanguageProperty()
	{
		return this.outputLanguage;
	}

	public final java.util.Locale getOutputLanguage()
	{
		return this.outputLanguageProperty().getValue();
	}

	public final void setOutputLanguage(final java.util.Locale outputLanguage)
	{
		this.outputLanguageProperty().setValue(outputLanguage);
	}

	public final ListProperty<LanguageUiPattern> customLanguagePatternsProperty()
	{
		return this.customLanguagePatterns;
	}

	public final javafx.collections.ObservableList<LanguageUiPattern> getCustomLanguagePatterns()
	{
		return this.customLanguagePatternsProperty().get();
	}

	public final void setCustomLanguagePatterns(final javafx.collections.ObservableList<LanguageUiPattern> customLanguagePatterns)
	{
		this.customLanguagePatternsProperty().set(customLanguagePatterns);
	}

	public final ListProperty<LanguageTextMapping> customLanguageTextMappingsProperty()
	{
		return this.customLanguageTextMappings;
	}

	public final javafx.collections.ObservableList<de.subcentral.watcher.settings.LanguageTextMapping> getCustomLanguageTextMappings()
	{
		return this.customLanguageTextMappingsProperty().get();
	}

	public final void setCustomLanguageTextMappings(
			final javafx.collections.ObservableList<de.subcentral.watcher.settings.LanguageTextMapping> customLanguageTextMappings)
	{
		this.customLanguageTextMappingsProperty().set(customLanguageTextMappings);
	}

}
