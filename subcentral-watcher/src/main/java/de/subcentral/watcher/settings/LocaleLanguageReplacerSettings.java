package de.subcentral.watcher.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.LocaleLanguageReplacer;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.correct.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.correct.SubtitleLanguageCorrector;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.ConfigurationPropertyHandlers;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.fx.settings.ObjectSettingsProperty;
import de.subcentral.fx.settings.Settings;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocaleLanguageReplacerSettings extends Settings
{
	private static final ConfigurationPropertyHandler<ObservableList<PatternToLanguageMapping>>	PATTERN_TO_LANGUAGE_MAPPING_LIST_HANDLER	= new PatternToLanguageMappingListHandler();
	private static final ConfigurationPropertyHandler<ObservableList<LanguageToTextMapping>>	LANGUAGE_TO_TEXT_MAPPING_LIST_HANDLER		= new LanguageToTextMappingListHandler();

	private final ListSettingsProperty<Locale>													parsingLanguages							= new ListSettingsProperty<>(
			"correction.subtitleLanguage.parsingLanguages", ConfigurationPropertyHandlers.LOCALE_LIST_HANDLER);
	private final ObjectSettingsProperty<LanguageFormat>										outputLanguageFormat						= new ObjectSettingsProperty<>(
			"correction.subtitleLanguage.outputLanguageFormat", ConfigurationPropertyHandlers.LANGUAGE_FORMAT_HANDLER, LanguageFormat.ISO2);
	private final ObjectSettingsProperty<Locale>												outputLanguage								= new ObjectSettingsProperty<>(
			"correction.subtitleLanguage.outputLanguage", ConfigurationPropertyHandlers.LOCALE_HANDLER, Locale.ENGLISH);
	private final ListSettingsProperty<PatternToLanguageMapping>								customLanguagePatterns						= new ListSettingsProperty<>(
			"correction.subtitleLanguage.customLanguagePatterns", PATTERN_TO_LANGUAGE_MAPPING_LIST_HANDLER);
	private final ListSettingsProperty<LanguageToTextMapping>									customLanguageTextMappings					= new ListSettingsProperty<>(
			"correction.subtitleLanguage.customLanguageTextMappings", LANGUAGE_TO_TEXT_MAPPING_LIST_HANDLER);

	private final Binding<SubtitleLanguageCorrector>											subtitleLanguageStandardizerBinding			= initSubtitleLanguageStandardizerBinding();

	// package protected (should only be instantiated by WatcherSettings)
	LocaleLanguageReplacerSettings()
	{
		initSettables(parsingLanguages, outputLanguageFormat, outputLanguage, customLanguagePatterns, customLanguageTextMappings);
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
				List<LanguagePattern> langPatterns = new ArrayList<>(customLanguagePatterns.getCurrent().size());
				for (PatternToLanguageMapping uiPattern : customLanguagePatterns.getCurrent())
				{
					langPatterns.add(uiPattern.toLanguagePattern());
				}
				Map<Locale, String> langTextMappings = new HashMap<>(customLanguageTextMappings.getCurrent().size());
				for (LanguageToTextMapping mapping : customLanguageTextMappings.getCurrent())
				{
					langTextMappings.put(mapping.getLanguage(), mapping.getText());
				}
				return new SubtitleLanguageCorrector(
						new LocaleLanguageReplacer(parsingLanguages.getCurrent(), outputLanguageFormat.getCurrent(), outputLanguage.getCurrent(), langPatterns, langTextMappings));
			}
		};
	}

	public ListSettingsProperty<Locale> getParsingLanguages()
	{
		return parsingLanguages;
	}

	public ObjectSettingsProperty<LanguageFormat> getOutputLanguageFormat()
	{
		return outputLanguageFormat;
	}

	public ObjectSettingsProperty<Locale> getOutputLanguage()
	{
		return outputLanguage;
	}

	public ListSettingsProperty<PatternToLanguageMapping> getCustomLanguagePatterns()
	{
		return customLanguagePatterns;
	}

	public ListSettingsProperty<LanguageToTextMapping> getCustomLanguageTextMappings()
	{
		return customLanguageTextMappings;
	}

	public Binding<SubtitleLanguageCorrector> subtitleLanguageStandardizerBinding()
	{
		return subtitleLanguageStandardizerBinding;
	}

	private static class PatternToLanguageMappingListHandler implements ConfigurationPropertyHandler<ObservableList<PatternToLanguageMapping>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<PatternToLanguageMapping> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<PatternToLanguageMapping> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> patternsCfgs = cfg.configurationsAt(key + ".languagePattern");
			List<PatternToLanguageMapping> patterns = new ArrayList<>(patternsCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> patternsCfg : patternsCfgs)
			{
				UserPattern p = new UserPattern(patternsCfg.getString("[@pattern]"), Mode.valueOf(patternsCfg.getString("[@patternMode]")));
				PatternToLanguageMapping langPattern = new PatternToLanguageMapping(p, Locale.forLanguageTag(patternsCfg.getString("[@languageTag]")));
				patterns.add(langPattern);
			}
			return FXCollections.observableList(patterns);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<PatternToLanguageMapping> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				PatternToLanguageMapping pattern = list.get(i);
				cfg.addProperty(key + ".languagePattern(" + i + ")[@pattern]", pattern.getPattern().getPattern());
				cfg.addProperty(key + ".languagePattern(" + i + ")[@patternMode]", pattern.getPattern().getMode());
				cfg.addProperty(key + ".languagePattern(" + i + ")[@languageTag]", pattern.getLanguage().toLanguageTag());
			}
		}
	}

	private static class LanguageToTextMappingListHandler implements ConfigurationPropertyHandler<ObservableList<LanguageToTextMapping>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<LanguageToTextMapping> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<LanguageToTextMapping> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			List<HierarchicalConfiguration<ImmutableNode>> namesCfgs = cfg.configurationsAt(key + ".languageTextMapping");
			List<LanguageToTextMapping> mappings = new ArrayList<>(namesCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> namesCfg : namesCfgs)
			{
				mappings.add(new LanguageToTextMapping(Locale.forLanguageTag(namesCfg.getString("[@languageTag]")), namesCfg.getString("[@text]")));
			}
			return FXCollections.observableList(mappings);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<LanguageToTextMapping> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				LanguageToTextMapping mapping = list.get(i);
				cfg.addProperty(key + ".languageTextMapping(" + i + ")[@languageTag]", mapping.getLanguage().toLanguageTag());
				cfg.addProperty(key + ".languageTextMapping(" + i + ")[@text]", mapping.getText());
			}
		}
	}
}
