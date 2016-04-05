package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.LocaleLanguageReplacer;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.correct.SubtitleLanguageCorrector;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.SubSettings;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocaleLanguageReplacerSettings extends SubSettings
{
	private final ListProperty<Locale>						parsingLanguages					= new SimpleListProperty<>(this, "parsingLanguages", FXCollections.observableArrayList());
	private final Property<LanguageFormat>					outputLanguageFormat				= new SimpleObjectProperty<>(this, "outputLanguageFormat", LanguageFormat.ISO2);
	private final Property<Locale>							outputLanguage						= new SimpleObjectProperty<>(this, "outputLanguage", Locale.ENGLISH);
	private final ListProperty<PatternToLanguageMapping>	customLanguagePatterns				= new SimpleListProperty<>(this, "customLanguagePatterns", FXCollections.observableArrayList());
	private final ListProperty<LanguageToTextMapping>		customLanguageTextMappings			= new SimpleListProperty<>(this, "customLanguageTextMappings", FXCollections.observableArrayList());

	private final Binding<SubtitleLanguageCorrector>		subtitleLanguageStandardizerBinding	= initSubtitleLanguageStandardizerBinding();

	// package protected (should only be instantiated by WatcherSettings)
	LocaleLanguageReplacerSettings()
	{
		super.bind(parsingLanguages, outputLanguageFormat, outputLanguage, customLanguagePatterns, customLanguageTextMappings);
	}

	private Binding<SubtitleLanguageCorrector> initSubtitleLanguageStandardizerBinding()
	{
		return new ObjectBinding<SubtitleLanguageCorrector>()
		{
			{
				super.bind(LocaleLanguageReplacerSettings.this);
			}

			@Override
			protected SubtitleLanguageCorrector computeValue()
			{
				List<LanguagePattern> langPatterns = new ArrayList<>(customLanguagePatterns.size());
				for (PatternToLanguageMapping uiPattern : customLanguagePatterns)
				{
					langPatterns.add(uiPattern.toLanguagePattern());
				}
				Map<Locale, String> langTextMappings = new HashMap<>(customLanguageTextMappings.size());
				for (LanguageToTextMapping mapping : customLanguageTextMappings)
				{
					langTextMappings.put(mapping.getLanguage(), mapping.getText());
				}
				return new SubtitleLanguageCorrector(new LocaleLanguageReplacer(parsingLanguages, outputLanguageFormat.getValue(), outputLanguage.getValue(), langPatterns, langTextMappings));
			}
		};
	}

	@Override
	public String getKey()
	{
		return "correction.subtitleLanguage";
	}

	@Override
	protected void doLoad(XMLConfiguration cfg)
	{
		String key = getKey();

		List<HierarchicalConfiguration<ImmutableNode>> parsingLangsCfgs = cfg.configurationsAt(key + ".parsingLanguages.language");
		List<Locale> parsingLangs = new ArrayList<>(parsingLangsCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> parsingLangCfg : parsingLangsCfgs)
		{
			parsingLangs.add(Locale.forLanguageTag(parsingLangCfg.getString("[@tag]")));
		}
		setParsingLanguages(FXCollections.observableList(parsingLangs));

		LanguageFormat outputFormat = LanguageFormat.valueOf(cfg.getString(key + ".ouputLanguageFormat"));
		setOutputLanguageFormat(outputFormat);
		Locale outputLang = Locale.forLanguageTag(cfg.getString(key + ".ouputLanguage[@tag]"));
		setOutputLanguage(outputLang);

		List<HierarchicalConfiguration<ImmutableNode>> patternsCfgs = cfg.configurationsAt(key + ".customLanguagePatterns.languagePattern");
		List<PatternToLanguageMapping> patterns = new ArrayList<>(patternsCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> patternsCfg : patternsCfgs)
		{
			UserPattern p = new UserPattern(patternsCfg.getString("[@pattern]"), Mode.valueOf(patternsCfg.getString("[@patternMode]")));
			PatternToLanguageMapping langPattern = new PatternToLanguageMapping(p, Locale.forLanguageTag(patternsCfg.getString("[@languageTag]")));
			patterns.add(langPattern);
		}
		setCustomLanguagePatterns(FXCollections.observableList(patterns));

		List<HierarchicalConfiguration<ImmutableNode>> namesCfgs = cfg.configurationsAt(key + ".customLanguageTextMappings.languageTextMapping");
		List<LanguageToTextMapping> langTextMappings = new ArrayList<>(namesCfgs.size());
		for (HierarchicalConfiguration<ImmutableNode> namesCfg : namesCfgs)
		{
			langTextMappings.add(new LanguageToTextMapping(Locale.forLanguageTag(namesCfg.getString("[@languageTag]")), namesCfg.getString("[@text]")));
		}
		setCustomLanguageTextMappings(FXCollections.observableList(langTextMappings));
	}

	@Override
	protected void doSave(XMLConfiguration cfg)
	{
		String key = getKey();

		cfg.clearProperty(key + ".parsingLanguages");
		for (int i = 0; i < parsingLanguages.size(); i++)
		{
			Locale lang = parsingLanguages.get(i);
			cfg.addProperty(key + ".parsingLanguages.language(" + i + ")[@tag]", lang.toLanguageTag());
		}
		cfg.setProperty(key + ".ouputLanguageFormat", getOutputLanguageFormat());
		cfg.setProperty(key + ".ouputLanguage[@tag]", getOutputLanguage().toLanguageTag());
		for (int i = 0; i < customLanguagePatterns.size(); i++)
		{
			PatternToLanguageMapping pattern = customLanguagePatterns.get(i);
			cfg.addProperty(key + ".customLanguagePatterns.languagePattern(" + i + ")[@pattern]", pattern.getPattern().getPattern());
			cfg.addProperty(key + ".customLanguagePatterns.languagePattern(" + i + ")[@patternMode]", pattern.getPattern().getMode());
			cfg.addProperty(key + ".customLanguagePatterns.languagePattern(" + i + ")[@languageTag]", pattern.getLanguage().toLanguageTag());
		}
		for (int i = 0; i < customLanguageTextMappings.size(); i++)
		{
			LanguageToTextMapping mapping = customLanguageTextMappings.get(i);
			cfg.addProperty(key + ".customLanguageTextMappings.languageTextMapping(" + i + ")[@languageTag]", mapping.getLanguage().toLanguageTag());
			cfg.addProperty(key + ".customLanguageTextMappings.languageTextMapping(" + i + ")[@text]", mapping.getText());
		}
	}

	public final ListProperty<Locale> parsingLanguagesProperty()
	{
		return this.parsingLanguages;
	}

	public final ObservableList<java.util.Locale> getParsingLanguages()
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

	public final LocaleLanguageReplacer.LanguageFormat getOutputLanguageFormat()
	{
		return this.outputLanguageFormatProperty().getValue();
	}

	public final void setOutputLanguageFormat(final de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat outputLanguageFormat)
	{
		this.outputLanguageFormatProperty().setValue(outputLanguageFormat);
	}

	public final Property<Locale> outputLanguageProperty()
	{
		return this.outputLanguage;
	}

	public final Locale getOutputLanguage()
	{
		return this.outputLanguageProperty().getValue();
	}

	public final void setOutputLanguage(final java.util.Locale outputLanguage)
	{
		this.outputLanguageProperty().setValue(outputLanguage);
	}

	public final ListProperty<PatternToLanguageMapping> customLanguagePatternsProperty()
	{
		return this.customLanguagePatterns;
	}

	public final ObservableList<PatternToLanguageMapping> getCustomLanguagePatterns()
	{
		return this.customLanguagePatternsProperty().get();
	}

	public final void setCustomLanguagePatterns(final javafx.collections.ObservableList<PatternToLanguageMapping> customLanguagePatterns)
	{
		this.customLanguagePatternsProperty().set(customLanguagePatterns);
	}

	public final ListProperty<LanguageToTextMapping> customLanguageTextMappingsProperty()
	{
		return this.customLanguageTextMappings;
	}

	public final ObservableList<de.subcentral.watcher.settings.LanguageToTextMapping> getCustomLanguageTextMappings()
	{
		return this.customLanguageTextMappingsProperty().get();
	}

	public final void setCustomLanguageTextMappings(final ObservableList<de.subcentral.watcher.settings.LanguageToTextMapping> customLanguageTextMappings)
	{
		this.customLanguageTextMappingsProperty().set(customLanguageTextMappings);
	}

	public Binding<SubtitleLanguageCorrector> subtitleLanguageStandardizerBinding()
	{
		return subtitleLanguageStandardizerBinding;
	}

	public SubtitleLanguageCorrector getSubtitleLanguageStandardizer()
	{
		return subtitleLanguageStandardizerBinding.getValue();
	}
}
