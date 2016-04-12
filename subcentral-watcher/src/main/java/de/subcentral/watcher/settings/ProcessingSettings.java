package de.subcentral.watcher.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.SeriesNameCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.fx.settings.BooleanSettingsProperty;
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
	public static enum LocateStrategy
	{
		SPECIFY, AUTO_LOCATE;
	}

	private static final ParsingServiceSettingsItemListHandler					PARSING_SERVICES_HANDLER			= new ParsingServiceSettingsItemListHandler();
	private static final Function<ParsingServiceSettingsItem, Observable[]>		PARSING_SERVICE_PROP_EXTRACTOR		= (ParsingServiceSettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final MetadataDbSettingsItemListHandler						METADATA_DBS_HANDLER				= new MetadataDbSettingsItemListHandler();
	private static final Function<MetadataDbSettingsItem, Observable[]>			METADATA_DB_PROP_EXTRACTOR			= (MetadataDbSettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final CompatibilityHandler									COMPATIBILITY_HANDLER				= new CompatibilityHandler();
	private static final Function<CompatibilitySettingsItem, Observable[]>		COMPABILITY_PROP_EXTRACTOR			= (CompatibilitySettingsItem item) -> new Observable[] { item.enabledProperty() };
	private static final CorrectorSettingsItemListHandler						CORRECTOR_HANDLER					= new CorrectorSettingsItemListHandler();
	private static final Function<CorrectorSettingsItem<?, ?>, Observable[]>	CORRECTOR_PROP_EXTRACTOR			= (
			CorrectorSettingsItem<?, ?> entry) -> new Observable[] { entry.beforeQueryingProperty(), entry.afterQueryingProperty() };
	private static final LocateStrategyHandler									LOCATE_STRATEGY_HANDLER				= new LocateStrategyHandler();
	private static final DeletionModeHandler									DELETION_MODE_HANDLER				= new DeletionModeHandler();

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
	private final ListSettingsProperty<Tag>										releaseMetaTags						= new ListSettingsProperty<>("metadata.release.metaTags.tag",
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
			COMPATIBILITY_HANDLER,
			COMPABILITY_PROP_EXTRACTOR);
	// Correction - Rules
	private final ListSettingsProperty<CorrectorSettingsItem<?, ?>>				correctionRules						= new ListSettingsProperty<>("correction.rules",
			CORRECTOR_HANDLER,
			CORRECTOR_PROP_EXTRACTOR);
	// Correction - Subtitle language
	private final LocaleLanguageReplacerSettings								subtitleLanguageCorrectionSettings	= new LocaleLanguageReplacerSettings();

	// Naming
	private final MapSettingsProperty<String, Object>							namingParameters					= new MapSettingsProperty<>("naming.parameters",
			ConfigurationPropertyHandlers.NAMING_PARAMETER_MAP_HANDLER);

	// File Transformation
	// File Transformation - General
	private final ObjectSettingsProperty<Path>									targetDir							= new ObjectSettingsProperty<>("fileTransformation.targetDir",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final BooleanSettingsProperty										deleteSource						= new BooleanSettingsProperty("fileTransformation.deleteSource", false);
	// File Transformation - Packing
	private final BooleanSettingsProperty										packingEnabled						= new BooleanSettingsProperty("fileTransformation.packing[@enabled]", true);
	private final ObjectSettingsProperty<Path>									rarExe								= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.rarExe",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final ObjectSettingsProperty<LocateStrategy>						winRarLocateStrategy				= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.locateStrategy",
			LOCATE_STRATEGY_HANDLER,
			LocateStrategy.AUTO_LOCATE);
	private final ObjectSettingsProperty<DeletionMode>							packingSourceDeletionMode			= new ObjectSettingsProperty<>("fileTransformation.packing.sourceDeletionMode",
			DELETION_MODE_HANDLER,
			DeletionMode.DELETE);

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

	public static ParsingServiceSettingsItemListHandler getParsingServicesHandler()
	{
		return PARSING_SERVICES_HANDLER;
	}

	public StringSettingsProperty getFilenamePatterns()
	{
		return filenamePatterns;
	}

	public ListSettingsProperty<ParsingServiceSettingsItem> getFilenameParsingServices()
	{
		return filenameParsingServices;
	}

	public ListSettingsProperty<ParsingServiceSettingsItem> getReleaseParsingServices()
	{
		return releaseParsingServices;
	}

	public ListSettingsProperty<Tag> getReleaseMetaTags()
	{
		return releaseMetaTags;
	}

	public ListSettingsProperty<MetadataDbSettingsItem> getReleaseDbs()
	{
		return releaseDbs;
	}

	public BooleanSettingsProperty getGuessingEnabled()
	{
		return guessingEnabled;
	}

	public ListSettingsProperty<StandardRelease> getStandardReleases()
	{
		return standardReleases;
	}

	public BooleanSettingsProperty getCompatibilityEnabled()
	{
		return compatibilityEnabled;
	}

	public ListSettingsProperty<CompatibilitySettingsItem> getCompatibilities()
	{
		return compatibilities;
	}

	public ListSettingsProperty<CorrectorSettingsItem<?, ?>> getCorrectionRules()
	{
		return correctionRules;
	}

	public LocaleLanguageReplacerSettings getSubtitleLanguageCorrectionSettings()
	{
		return subtitleLanguageCorrectionSettings;
	}

	public MapSettingsProperty<String, Object> getNamingParameters()
	{
		return namingParameters;
	}

	public ObjectSettingsProperty<Path> getTargetDir()
	{
		return targetDir;
	}

	public BooleanSettingsProperty getDeleteSource()
	{
		return deleteSource;
	}

	public BooleanSettingsProperty getPackingEnabled()
	{
		return packingEnabled;
	}

	public ObjectSettingsProperty<Path> getRarExe()
	{
		return rarExe;
	}

	public ObjectSettingsProperty<LocateStrategy> getWinRarLocateStrategy()
	{
		return winRarLocateStrategy;
	}

	public ObjectSettingsProperty<DeletionMode> getPackingSourceDeletionMode()
	{
		return packingSourceDeletionMode;
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
			List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key + ".parsingService");
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
				cfg.addProperty(key + ".parsingService(" + i + ")", ps.getItem().getDomain());
				cfg.addProperty(key + ".parsingService(" + i + ")[@enabled]", ps.isEnabled());
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
			List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
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
				cfg.addProperty(key + ".db(" + i + ")", db.getItem().getSite().getName());
				cfg.addProperty(key + ".db(" + i + ")[@enabled]", db.isEnabled());
			}
		}
	}

	private static class CompatibilityHandler implements ConfigurationPropertyHandler<ObservableList<CompatibilitySettingsItem>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<CompatibilitySettingsItem> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<CompatibilitySettingsItem> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{
			// read GroupsCompatibilities
			List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibility");
			List<CompatibilitySettingsItem> compatibilities = new ArrayList<>(groupsCompCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs)
			{
				boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
				Group sourceGroup = Group.from(groupsCompCfg.getString("[@sourceGroup]"));
				Group compatibleGroup = Group.from(groupsCompCfg.getString("[@compatibleGroup]"));
				boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
				compatibilities.add(new CompatibilitySettingsItem(new CrossGroupCompatibility(sourceGroup, compatibleGroup, symmetric), enabled));
			}
			return FXCollections.observableList(compatibilities);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<CompatibilitySettingsItem> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				CompatibilitySettingsItem entry = list.get(i);
				Compatibility c = entry.getItem();
				if (c instanceof CrossGroupCompatibility)
				{
					CrossGroupCompatibility cgc = (CrossGroupCompatibility) c;
					cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@enabled]", entry.isEnabled());
					cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@sourceGroup]", cgc.getSourceGroup());
					cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@compatibleGroup]", cgc.getCompatibleGroup());
					cfg.addProperty(key + ".crossGroupCompatibility(" + i + ")[@symmetric]", cgc.isSymmetric());
				}
				else
				{
					throw new IllegalArgumentException("Unknown compatibility: " + c);
				}
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

	private static class LocateStrategyHandler implements ConfigurationPropertyHandler<LocateStrategy>
	{
		@Override
		public LocateStrategy get(ImmutableConfiguration cfg, String key)
		{
			return LocateStrategy.valueOf(cfg.getString(key));
		}

		@Override
		public void add(Configuration cfg, String key, LocateStrategy value)
		{
			cfg.addProperty(key, value.name());
		}
	}

	private static class DeletionModeHandler implements ConfigurationPropertyHandler<DeletionMode>
	{
		@Override
		public DeletionMode get(ImmutableConfiguration cfg, String key)
		{
			return DeletionMode.valueOf(cfg.getString(key));
		}

		@Override
		public void add(Configuration cfg, String key, DeletionMode value)
		{
			cfg.addProperty(key, value.name());
		}
	}
}
