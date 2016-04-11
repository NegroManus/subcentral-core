package de.subcentral.watcher.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.BooleanSettingsProperty;
import de.subcentral.fx.settings.ConfigurationHelper;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.ConfigurationPropertyHandlers;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.fx.settings.MapSettingsProperty;
import de.subcentral.fx.settings.ObjectSettingsProperty;
import de.subcentral.fx.settings.Settings;
import de.subcentral.fx.settings.StringSettingsProperty;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbCom;
import de.subcentral.support.orlydbcom.OrlyDbComMetadataDb;
import de.subcentral.support.predbme.PreDbMe;
import de.subcentral.support.predbme.PreDbMeMetadataDb;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.xrelto.XRelTo;
import de.subcentral.support.xrelto.XRelToMetadataDb;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProcessingSettings extends Settings
{
	public static enum WinRarLocateStrategy
	{
		SPECIFY, AUTO_LOCATE;
	}

	private static final ParsingServiceSettingsItemListHandler					PARSING_SERVICES_HANDLER			= new ParsingServiceSettingsItemListHandler();
	private static final Function<ParsingServiceSettingsItem, Observable[]>		PARSING_SERVICE_PROP_EXTRACTOR		= (ParsingServiceSettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final MetadataDbSettingsItemListHandler						METADATA_DBS_HANDLER				= new MetadataDbSettingsItemListHandler();
	private static final Function<MetadataDbSettingsItem, Observable[]>			METADATA_DB_PROP_EXTRACTOR			= (MetadataDbSettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final Function<CompatibilitySettingsItem, Observable[]>		COMPABILITY_PROP_EXTRACTOR			= (CompatibilitySettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final CorrectorSettingsItemListHandler						CORRECTOR_HANDLER					= new CorrectorSettingsItemListHandler();
	private static final Function<CorrectorSettingsItem<?, ?>, Observable[]>	CORRECTOR_PROP_EXTRACTOR			= (
			CorrectorSettingsItem<?, ?> entry) -> new Observable[] { entry.beforeQueryingProperty(), entry.afterQueryingProperty() };

	// Parsing
	private final StringSettingsProperty										filenamePatterns					= new StringSettingsProperty("parsing.filenamePatterns");
	private final ListSettingsProperty<ParsingServiceSettingsItem>				filenameParsingServices				= new ListSettingsProperty<>("parsing.parsingServices",
			PARSING_SERVICES_HANDLER,
			PARSING_SERVICE_PROP_EXTRACTOR);
	// Metadata
	// Metadata - Release
	private final ListSettingsProperty<ParsingServiceSettingsItem>				releaseParsingServices				= new ListSettingsProperty<>("metadata.release.parsingServices",
			PARSING_SERVICES_HANDLER,
			PARSING_SERVICE_PROP_EXTRACTOR);
	private final ListSettingsProperty<Tag>										releaseMetaTags						= new ListSettingsProperty<>("metadata.release.metaTags",
			ConfigurationPropertyHandlers.TAG_LIST_HANDLER);
	// Metadata - Release - Databases
	private final ListSettingsProperty<MetadataDbSettingsItem>					releaseDbs							= new ListSettingsProperty<>("metadata.release.databases",
			METADATA_DBS_HANDLER,
			METADATA_DB_PROP_EXTRACTOR);
	// Metadata - Release - Guessing
	private final BooleanSettingsProperty										guessingEnabled						= new BooleanSettingsProperty("metadata.release.guessing[@enabled]", true);
	private final ListSettingsProperty<StandardRelease>							standardReleases					= new ListSettingsProperty<>("metadata.release.guessing.standardReleases",
			ConfigurationPropertyHandlers.STANDARD_RELEASE_LIST_HANDLER);
	// Metadata - Release - Compatibility
	private final BooleanSettingsProperty										compatibilityEnabled				= new BooleanSettingsProperty("metadata.release.compatibility[@enabled]", true);
	private final ListSettingsProperty<CompatibilitySettingsItem>				compatibilities						= new ListSettingsProperty<>("metadata.release.compatibility.compatibilities",
			null);
	// Correction - Rules
	private final ListSettingsProperty<CorrectorSettingsItem<?, ?>>				correctionRules						= new ListSettingsProperty<>("correction.rules", CORRECTOR_HANDLER);
	// Correction - Subtitle language
	private final LocaleLanguageReplacerSettings								subtitleLanguageCorrectionSettings	= new LocaleLanguageReplacerSettings();

	// Naming
	private final MapSettingsProperty<String, Object>							namingParameters					= new MapSettingsProperty<>("naming.parameters", null);

	// File Transformation
	// File Transformation - General
	private final ObjectSettingsProperty<Path>									targetDir							= new ObjectSettingsProperty<>("fileTransformation.targetDir",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final BooleanSettingsProperty										deleteSource						= new BooleanSettingsProperty("fileTransformation.deleteSource", false);
	// File Transformation - Packing
	private final BooleanSettingsProperty										packingEnabled						= new BooleanSettingsProperty("fileTransformation.packing[@enabled]", true);
	private final ObjectSettingsProperty<Path>									rarExe								= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.rarExe",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final ObjectSettingsProperty<WinRarLocateStrategy>					winRarLocateStrategy				= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.locateStrategy",
			null,
			WinRarLocateStrategy.AUTO_LOCATE);
	private final ObjectSettingsProperty<DeletionMode>							packingSourceDeletionMode			= new ObjectSettingsProperty<>("fileTransformation.packing.sourceDeletionMode",
			null);

	// package protected (should only be instantiated by WatcherSettings)
	ProcessingSettings()
	{
		initSettables(filenamePatterns,
				filenameParsingServices,
				releaseParsingServices,
				releaseMetaTags,
				releaseDbs,
				guessingEnabled,
				standardReleases,
				compatibilityEnabled,
				compatibilities,
				correctionRules,
				subtitleLanguageCorrectionSettings,
				namingParameters,
				targetDir,
				deleteSource,
				packingEnabled,
				rarExe,
				winRarLocateStrategy,
				packingSourceDeletionMode);
	}

	@Override
	protected void doLoad(XMLConfiguration cfg)
	{
		// FileParsing
		loadFilenamePatterns(cfg);
		loadFilenameParsingServices(cfg);

		// Metadata
		// Metadata - Release
		loadReleaseParsingServices(cfg);
		loadReleaseMetaTags(cfg);
		// Metadata - Release - Databases
		loadReleaseDbs(cfg);
		// Metadata - Release - Guessing
		loadGuessingEnabled(cfg);
		loadStandardReleases(cfg);
		// Metadata - Release - Compatibility
		loadCompatibilityEnabled(cfg);
		loadCompatibilities(cfg);

		// Correction - Rules
		loadCorrectionRules(cfg);
		subtitleLanguageCorrectionSettings.load(cfg);

		// Naming
		loadNamingParameters(cfg);

		// File Transformation - General
		loadTargetDir(cfg);
		loadDeleteSource(cfg);

		// File Transformation - Packing
		loadPackingEnabled(cfg);
		loadAutoLocateWinRar(cfg);
		loadRarExe(cfg);
		loadPackingSourceDeletionMode(cfg);
	}

	private void loadFilenamePatterns(XMLConfiguration cfg)
	{
		String patterns = cfg.getString("parsing.filenamePatterns");
		if (!StringUtils.isBlank(patterns))
		{
			setFilenamePatterns(patterns);
		}
		else
		{
			setFilenamePatterns(null);
		}
	}

	private void loadFilenameParsingServices(XMLConfiguration cfg)
	{
		setFilenameParsingServices(ConfigurationHelper.getParsingServices(cfg, "parsing.parsingServices"));
	}

	private void loadReleaseParsingServices(XMLConfiguration cfg)
	{
		setReleaseParsingServices(ConfigurationHelper.getParsingServices(cfg, "metadata.release.parsingServices"));
	}

	private void loadReleaseMetaTags(XMLConfiguration cfg)
	{
		setReleaseMetaTags(ConfigurationHelper.getTags(cfg, "metadata.release.metaTags"));
	}

	private void loadReleaseDbs(XMLConfiguration cfg)
	{
		setReleaseDbs(ConfigurationHelper.getReleaseDbs(cfg, "metadata.release.databases"));
	}

	private void loadGuessingEnabled(XMLConfiguration cfg)
	{
		setGuessingEnabled(cfg.getBoolean("metadata.release.guessing[@enabled]", false));
	}

	private void loadStandardReleases(XMLConfiguration cfg)
	{
		setStandardReleases(ConfigurationHelper.getStandardReleases(cfg, "metadata.release.guessing.standardReleases"));
	}

	private void loadCompatibilityEnabled(XMLConfiguration cfg)
	{
		setCompatibilityEnabled(cfg.getBoolean("metadata.release.compatibility[@enabled]", false));
	}

	private void loadCompatibilities(XMLConfiguration cfg)
	{
		setCompatibilities(ConfigurationHelper.getCompatibilities(cfg, "metadata.release.compatibility.compatibilities"));
	}

	private void loadCorrectionRules(XMLConfiguration cfg)
	{
		setCorrectionRules(ConfigurationHelper.getCorrectionRules(cfg, "correction.rules"));
	}

	private void loadNamingParameters(XMLConfiguration cfg)
	{
		setNamingParameters(ConfigurationHelper.getNamingParameters(cfg, "naming.parameters"));
	}

	private void loadTargetDir(XMLConfiguration cfg)
	{
		setTargetDir(ConfigurationHelper.getPath(cfg, "fileTransformation.targetDir"));
	}

	private void loadDeleteSource(XMLConfiguration cfg)
	{
		setDeleteSource(cfg.getBoolean("fileTransformation.deleteSource"));
	}

	private void loadPackingEnabled(XMLConfiguration cfg)
	{
		setPackingEnabled(cfg.getBoolean("fileTransformation.packing[@enabled]"));
	}

	private void loadAutoLocateWinRar(XMLConfiguration cfg)
	{
		setWinRarLocateStrategy(WinRarLocateStrategy.valueOf(cfg.getString("fileTransformation.packing.winrar.locateStrategy")));
	}

	private void loadRarExe(XMLConfiguration cfg)
	{
		setRarExe(ConfigurationHelper.getPath(cfg, "fileTransformation.packing.winrar.rarExe"));
	}

	private void loadPackingSourceDeletionMode(XMLConfiguration cfg)
	{
		setPackingSourceDeletionMode(DeletionMode.valueOf(cfg.getString("fileTransformation.packing.sourceDeletionMode")));
	}

	@Override
	protected void doSave(XMLConfiguration cfg)
	{
		// FileParsing
		cfg.addProperty("parsing.filenamePatterns", getFilenamePatterns());
		ConfigurationHelper.addParsingServices(cfg, "parsing.parsingServices", filenameParsingServices);

		// Metadata
		// Metadata - Release
		ConfigurationHelper.addParsingServices(cfg, "metadata.release.parsingServices", releaseParsingServices);
		for (Tag tag : releaseMetaTags)
		{
			cfg.addProperty("metadata.release.metaTags.tag", tag.getName());
		}

		// Metadata - Release - Databases
		for (int i = 0; i < releaseDbs.size(); i++)
		{
			MetadataDbSettingsItem<Release> db = releaseDbs.get(i);
			cfg.addProperty("metadata.release.databases.db(" + i + ")", db.getItem().getSite().getName());
			cfg.addProperty("metadata.release.databases.db(" + i + ")[@enabled]", db.isEnabled());
		}

		// // Metadata - Release - Guessing
		cfg.addProperty("metadata.release.guessing[@enabled]", isGuessingEnabled());
		for (int i = 0; i < standardReleases.size(); i++)
		{
			StandardRelease stdRls = standardReleases.get(i);
			cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@tags]", Tag.formatList(stdRls.getRelease().getTags()));
			cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@group]", stdRls.getRelease().getGroup());
			cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@scope]", stdRls.getScope());
		}

		// Metadata - Release - Compatibility
		cfg.addProperty("metadata.release.compatibility[@enabled]", isCompatibilityEnabled());
		for (int i = 0; i < compatibilities.size(); i++)
		{
			CompatibilitySettingsItem entry = compatibilities.get(i);
			Compatibility c = entry.getItem();
			if (c instanceof CrossGroupCompatibility)
			{
				CrossGroupCompatibility cgc = (CrossGroupCompatibility) c;
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@enabled]", entry.isEnabled());
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@sourceGroup]", cgc.getSourceGroup());
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@compatibleGroup]", cgc.getCompatibleGroup());
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@symmetric]", cgc.isSymmetric());
			}
			else
			{
				throw new IllegalArgumentException("Unknown compatibility: " + c);
			}
		}

		// Correction - Rules
		ConfigurationHelper.addCorrectionRules(cfg, "correction.rules", correctionRules);
		// Correction - SubtitleLanguage
		subtitleLanguageCorrectionSettings.save(cfg);

		// Naming
		int i = 0;
		for (Map.Entry<String, Object> param : namingParameters.entrySet())
		{
			cfg.addProperty("naming.parameters.param(" + i + ")[@key]", param.getKey());
			cfg.addProperty("naming.parameters.param(" + i + ")[@item]", param.getValue());
			i++;
		}

		// File Transformation - General
		ConfigurationHelper.addPath(cfg, "fileTransformation.targetDir", getTargetDir());
		cfg.addProperty("fileTransformation.deleteSource", isDeleteSource());

		// File Transformation - Packing
		cfg.addProperty("fileTransformation.packing[@enabled]", isPackingEnabled());
		cfg.addProperty("fileTransformation.packing.sourceDeletionMode", getPackingSourceDeletionMode());
		cfg.addProperty("fileTransformation.packing.winrar.locateStrategy", getWinRarLocateStrategy());
		ConfigurationHelper.addPath(cfg, "fileTransformation.packing.winrar.rarExe", getRarExe());
	}

	private static class ParsingServiceSettingsItemListHandler implements ConfigurationPropertyHandler<ObservableList<ParsingServiceSettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<ParsingServiceSettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<ParsingServiceSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			ArrayList<ParsingServiceSettingsItem> services = new ArrayList<>(4);
			List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key);
			for (HierarchicalConfiguration<ImmutableNode> parsingServiceCfg : parsingServiceCfgs)
			{
				String domain = parsingServiceCfg.getString("");
				boolean enabled = parsingServiceCfg.getBoolean("[@enabled]");
				if (Addic7edCom.getParsingService().getDomain().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(Addic7edCom.getParsingService(), enabled));
				}
				else if (ItalianSubsNet.getParsingService().getDomain().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(ItalianSubsNet.getParsingService(), enabled));
				}
				else if (ReleaseScene.getParsingService().getDomain().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(ReleaseScene.getParsingService(), enabled));
				}
				else if (SubCentralDe.getParsingService().getDomain().equals(domain))
				{
					services.add(new ParsingServiceSettingsItem(SubCentralDe.getParsingService(), enabled));
				}
				else
				{
					throw new IllegalArgumentException("Unknown parsing service domain: " + domain);
				}
			}
			services.trimToSize();
			return FXCollections.observableList(services);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<ParsingServiceSettingsItem> value)
		{
			for (int i = 0; i < value.size(); i++)
			{
				ParsingServiceSettingsItem ps = value.get(i);
				cfg.addProperty(key + "(" + i + ")", ps.getItem().getDomain());
				cfg.addProperty(key + "(" + i + ")[@enabled]", ps.isEnabled());
			}
		}
	}

	private static class MetadataDbSettingsItemListHandler implements ConfigurationPropertyHandler<ObservableList<MetadataDbSettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<MetadataDbSettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<MetadataDbSettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			ArrayList<MetadataDbSettingsItem> dbs = new ArrayList<>(3);
			List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key);
			for (HierarchicalConfiguration<ImmutableNode> rlsDbCfg : rlsDbCfgs)
			{
				String name = rlsDbCfg.getString("");
				boolean enabled = rlsDbCfg.getBoolean("[@enabled]");
				if (PreDbMe.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new PreDbMeMetadataDb(), enabled));
				}
				else if (XRelTo.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new XRelToMetadataDb(), enabled));
				}
				else if (OrlyDbCom.SITE.getName().equals(name))
				{
					dbs.add(new MetadataDbSettingsItem(new OrlyDbComMetadataDb(), enabled));
				}
				else
				{
					throw new IllegalArgumentException("Unknown metadata database: " + name);
				}
			}
			dbs.trimToSize();
			return FXCollections.observableList(dbs);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<MetadataDbSettingsItem> value)
		{
			for (int i = 0; i < value.size(); i++)
			{
				MetadataDbSettingsItem db = value.get(i);
				cfg.addProperty(key + "(" + i + ")", db.getItem().getSite().getName());
				cfg.addProperty(key + "(" + i + ")[@enabled]", db.isEnabled());
			}
		}
	}

	private static class CorrectorSettingsItemListHandler implements ConfigurationPropertyHandler<ObservableList<CorrectorSettingsItem<?, ?>>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<CorrectorSettingsItem<?, ?>> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<CorrectorSettingsItem<?, ?>> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			ArrayList<CorrectorSettingsItem<?, ?>> stdzers = new ArrayList<>();
			List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule");
			int seriesNameIndex = 0;
			for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : seriesStdzerCfgs)
			{
				String namePatternStr = stdzerCfg.getString("[@namePattern]");
				Mode namePatternMode = Mode.valueOf(stdzerCfg.getString("[@namePatternMode]"));
				UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
				String nameReplacement = stdzerCfg.getString("[@nameReplacement]");
				List<HierarchicalConfiguration<ImmutableNode>> aliasNameCfgs = cfg.configurationsAt(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName");
				List<String> aliasNameReplacements = new ArrayList<>(aliasNameCfgs.size());
				for (HierarchicalConfiguration<ImmutableNode> aliasNameCfg : aliasNameCfgs)
				{
					aliasNameReplacements.add(aliasNameCfg.getString(""));
				}
				boolean enabledPreMetadataDb = stdzerCfg.getBoolean("[@beforeQuerying]");
				boolean enabledPostMetadataDb = stdzerCfg.getBoolean("[@afterQuerying]");
				stdzers.add(new SeriesNameCorrectorSettingsItem(nameUiPattern, nameReplacement, aliasNameReplacements, enabledPreMetadataDb, enabledPostMetadataDb));
				seriesNameIndex++;
			}
			List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsCorrectionRule");
			for (HierarchicalConfiguration<ImmutableNode> stdzerCfg : rlsTagsStdzerCfgs)
			{
				List<Tag> queryTags = Tag.parseList(stdzerCfg.getString("[@searchTags]"));
				List<Tag> replacement = Tag.parseList(stdzerCfg.getString("[@replacement]"));
				SearchMode queryMode = SearchMode.valueOf(stdzerCfg.getString("[@searchMode]"));
				ReplaceMode replaceMode = ReplaceMode.valueOf(stdzerCfg.getString("[@replaceMode]"));
				boolean ignoreOrder = stdzerCfg.getBoolean("[@ignoreOrder]", false);
				boolean beforeQuerying = stdzerCfg.getBoolean("[@beforeQuerying]");
				boolean afterQuerying = stdzerCfg.getBoolean("[@afterQuerying]");
				ReleaseTagsCorrector stdzer = new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
				stdzers.add(new ReleaseTagsCorrectorSettingsItem(stdzer, beforeQuerying, afterQuerying));
			}
			stdzers.trimToSize();
			return FXCollections.observableList(stdzers);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<CorrectorSettingsItem<?, ?>> list)
		{
			// one index for each element name
			int seriesNameIndex = 0;
			int releaseTagsIndex = 0;
			for (CorrectorSettingsItem<?, ?> genericEntry : list)
			{
				if (genericEntry instanceof SeriesNameCorrectorSettingsItem)
				{
					SeriesNameCorrectorSettingsItem entry = (SeriesNameCorrectorSettingsItem) genericEntry;
					SeriesNameCorrector corrector = entry.getItem();
					UserPattern namePattern = entry.getNameUserPattern();

					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@nameReplacement]", corrector.getNameReplacement());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
					cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ")[@afterQuerying]", entry.isAfterQuerying());
					for (String aliasName : corrector.getAliasNamesReplacement())
					{
						cfg.addProperty(key + ".seriesNameCorrectionRule(" + seriesNameIndex + ").aliasNames.aliasName", aliasName);
					}
					seriesNameIndex++;
				}
				else if (genericEntry instanceof ReleaseTagsCorrectorSettingsItem)
				{
					ReleaseTagsCorrectorSettingsItem entry = (ReleaseTagsCorrectorSettingsItem) genericEntry;
					TagsReplacer replacer = (TagsReplacer) entry.getItem().getReplacer();

					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchTags]", Tag.formatList(replacer.getSearchTags()));
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replacement]", Tag.formatList(replacer.getReplacement()));
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@searchMode]", replacer.getSearchMode());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@beforeQuerying]", entry.isBeforeQuerying());
					cfg.addProperty(key + ".releaseTagsCorrectionRule(" + releaseTagsIndex + ")[@afterQuerying]", entry.isAfterQuerying());
					releaseTagsIndex++;
				}
				else
				{
					throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
				}
			}
		}
	}
}
