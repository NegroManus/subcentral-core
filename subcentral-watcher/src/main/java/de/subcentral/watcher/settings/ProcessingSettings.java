package de.subcentral.watcher.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.google.common.collect.ComparisonChain;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.settings.BooleanSettingsProperty;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.ConfigurationPropertyHandlers;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.fx.settings.MapSettingsProperty;
import de.subcentral.fx.settings.ObjectSettingsProperty;
import de.subcentral.fx.settings.Settings;
import de.subcentral.fx.settings.StringSettingsProperty;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProcessingSettings extends Settings
{
	public static enum LocateStrategy
	{
		SPECIFY, AUTO_LOCATE;
	}

	public static final Comparator<StandardRelease>							STANDARD_RELEASE_COMPARATOR			= initStandardReleaseComparator();
	private static final StandardReleaseListHandler							STANDARD_RELEASE_LIST_HANDLER		= new StandardReleaseListHandler();
	private static final LocateStrategyHandler								LOCATE_STRATEGY_HANDLER				= new LocateStrategyHandler();
	private static final DeletionModeHandler								DELETION_MODE_HANDLER				= new DeletionModeHandler();

	// Parsing
	private final StringSettingsProperty									filenamePatterns					= new StringSettingsProperty("parsing.filenamePatterns");
	private final ListSettingsProperty<ParsingServiceSettingsItem>			filenameParsers						= new ListSettingsProperty<>("parsing.parsers",
			ParsingServiceSettingsItem.getListConfigurationPropertyHandler(),
			ParsingServiceSettingsItem.createObservableList());
	// Metadata
	// Metadata - Release
	private final ListSettingsProperty<ParsingServiceSettingsItem>			releaseParsers						= new ListSettingsProperty<>("metadata.release.parsers",
			ParsingServiceSettingsItem.getListConfigurationPropertyHandler(),
			ParsingServiceSettingsItem.createObservableList());
	private final ListSettingsProperty<Tag>									releaseMetaTags						= new ListSettingsProperty<>("metadata.release.metaTags.tag",
			ConfigurationPropertyHandlers.TAG_LIST_HANDLER);
	// Metadata - Release - Databases
	private final ListSettingsProperty<MetadataServiceSettingsItem>				releaseDbs							= new ListSettingsProperty<>("metadata.release.databases",
			MetadataServiceSettingsItem.getListConfigurationPropertyHandler(),
			MetadataServiceSettingsItem.createObservableList());
	// Metadata - Release - Guessing
	private final BooleanSettingsProperty									guessingEnabled						= new BooleanSettingsProperty("metadata.release.guessing[@enabled]", true);
	private final ListSettingsProperty<StandardRelease>						standardReleases					= new ListSettingsProperty<>("metadata.release.guessing.standardReleases",
			STANDARD_RELEASE_LIST_HANDLER);
	// Metadata - Release - Compatibility
	private final BooleanSettingsProperty									compatibilityEnabled				= new BooleanSettingsProperty("metadata.release.compatibility[@enabled]", true);
	private final ListSettingsProperty<CrossGroupCompatibilitySettingsItem>	crossGroupCompatibilities			= new ListSettingsProperty<>("metadata.release.compatibility.crossGroupCompatibilities",
			CrossGroupCompatibilitySettingsItem.getListConfigurationPropertyHandler(),
			CrossGroupCompatibilitySettingsItem.createObservableList());
	// Correction - Rules
	private final ListSettingsProperty<CorrectorSettingsItem<?, ?>>			correctionRules						= new ListSettingsProperty<>("correction.rules",
			CorrectorSettingsItem.getListConfigurationPropertyHandler(),
			CorrectorSettingsItem.createObservableList());
	// Correction - Subtitle language
	private final LocaleLanguageReplacerSettings							subtitleLanguageCorrectionSettings	= new LocaleLanguageReplacerSettings();

	// Naming
	private final MapSettingsProperty<String, Object>						namingParameters					= new MapSettingsProperty<>("naming.parameters",
			ConfigurationPropertyHandlers.NAMING_PARAMETER_MAP_HANDLER);

	// File Transformation
	// File Transformation - General
	private final ObjectSettingsProperty<Path>								targetDir							= new ObjectSettingsProperty<>("fileTransformation.targetDir",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final BooleanSettingsProperty									deleteSource						= new BooleanSettingsProperty("fileTransformation.deleteSource", false);
	// File Transformation - Packing
	private final BooleanSettingsProperty									packingEnabled						= new BooleanSettingsProperty("fileTransformation.packing[@enabled]", true);
	private final ObjectSettingsProperty<Path>								rarExe								= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.rarExe",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final ObjectSettingsProperty<LocateStrategy>					winRarLocateStrategy				= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.locateStrategy",
			LOCATE_STRATEGY_HANDLER,
			LocateStrategy.AUTO_LOCATE);
	private final ObjectSettingsProperty<DeletionMode>						packingSourceDeletionMode			= new ObjectSettingsProperty<>("fileTransformation.packing.sourceDeletionMode",
			DELETION_MODE_HANDLER,
			DeletionMode.DELETE);

	// package protected (should only be instantiated by WatcherSettings)
	ProcessingSettings()
	{
		initSettables(filenamePatterns,
				filenameParsers,
				releaseParsers,
				releaseMetaTags,
				releaseDbs,
				guessingEnabled,
				standardReleases,
				compatibilityEnabled,
				crossGroupCompatibilities,
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

	private static Comparator<StandardRelease> initStandardReleaseComparator()
	{
		return (StandardRelease r1, StandardRelease r2) ->
		{
			return ComparisonChain.start()
					.compare(r1.getRelease().getGroup(), r2.getRelease().getGroup(), ObjectUtil.getDefaultOrdering())
					.compare(r1.getRelease().getTags(), r2.getRelease().getTags(), Tag.TAGS_COMPARATOR)
					.result();
		};
	}

	public StringSettingsProperty getFilenamePatterns()
	{
		return filenamePatterns;
	}

	public ListSettingsProperty<ParsingServiceSettingsItem> getFilenameParsers()
	{
		return filenameParsers;
	}

	public ListSettingsProperty<ParsingServiceSettingsItem> getReleaseParsers()
	{
		return releaseParsers;
	}

	public ListSettingsProperty<Tag> getReleaseMetaTags()
	{
		return releaseMetaTags;
	}

	public ListSettingsProperty<MetadataServiceSettingsItem> getReleaseDbs()
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

	public ListSettingsProperty<CrossGroupCompatibilitySettingsItem> getCrossGroupCompatibilities()
	{
		return crossGroupCompatibilities;
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

	private static class StandardReleaseListHandler implements ConfigurationPropertyHandler<ObservableList<StandardRelease>>
	{
		@SuppressWarnings("unchecked")
		@Override
		public ObservableList<StandardRelease> get(ImmutableConfiguration cfg, String key)
		{
			if (cfg instanceof HierarchicalConfiguration<?>)
			{
				return get((HierarchicalConfiguration<ImmutableNode>) cfg, key);
			}
			throw new IllegalArgumentException("Configuration type not supported: " + cfg);
		}

		private static ObservableList<StandardRelease> get(HierarchicalConfiguration<ImmutableNode> cfg, String key)
		{

			List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".standardRelease");
			ArrayList<StandardRelease> list = new ArrayList<>(rlsCfgs.size());
			for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
			{
				List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
				Group group = Group.from(rlsCfg.getString("[@group]"));
				Scope scope = Scope.valueOf(rlsCfg.getString("[@scope]"));
				list.add(new StandardRelease(tags, group, scope));
			}
			// sort the standard releases
			list.sort(STANDARD_RELEASE_COMPARATOR);
			return FXCollections.observableList(list);
		}

		@Override
		public void add(Configuration cfg, String key, ObservableList<StandardRelease> list)
		{
			for (int i = 0; i < list.size(); i++)
			{
				StandardRelease stdRls = list.get(i);
				cfg.addProperty(key + ".standardRelease(" + i + ")[@tags]", Tag.formatList(stdRls.getRelease().getTags()));
				cfg.addProperty(key + ".standardRelease(" + i + ")[@group]", Group.toStringNullSafe(stdRls.getRelease().getGroup()));
				cfg.addProperty(key + ".standardRelease(" + i + ")[@scope]", stdRls.getScope());
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