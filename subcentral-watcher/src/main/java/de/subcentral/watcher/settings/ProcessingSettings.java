package de.subcentral.watcher.settings;

import java.nio.file.Path;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;

import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.settings.BooleanSettingsProperty;
import de.subcentral.fx.settings.ConfigurationPropertyHandler;
import de.subcentral.fx.settings.ConfigurationPropertyHandlers;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.fx.settings.MapSettingsProperty;
import de.subcentral.fx.settings.ObjectSettingsProperty;
import de.subcentral.fx.settings.Settings;
import de.subcentral.fx.settings.StringSettingsProperty;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;

public class ProcessingSettings extends Settings
{
	public static enum LocateStrategy
	{
		SPECIFY, AUTO_LOCATE;
	}

	private static final LocateStrategyHandler						LOCATE_STRATEGY_HANDLER				= new LocateStrategyHandler();
	private static final DeletionModeHandler						DELETION_MODE_HANDLER				= new DeletionModeHandler();

	// Parsing
	private final StringSettingsProperty							filenamePatterns					= new StringSettingsProperty("parsing.filenamePatterns");
	private final ListSettingsProperty<ParsingServiceSettingsItem>	filenameParsingServices				= new ListSettingsProperty<>("parsing.parsingServices",
			ParsingServiceSettingsItem.getListConfigurationPropertyHandler(),
			ParsingServiceSettingsItem.createObservableList());
	// Metadata
	// Metadata - Release
	private final ListSettingsProperty<ParsingServiceSettingsItem>	releaseParsingServices				= new ListSettingsProperty<>("metadata.release.parsingServices",
			ParsingServiceSettingsItem.getListConfigurationPropertyHandler(),
			ParsingServiceSettingsItem.createObservableList());
	private final ListSettingsProperty<Tag>							releaseMetaTags						= new ListSettingsProperty<>("metadata.release.metaTags.tag",
			ConfigurationPropertyHandlers.TAG_LIST_HANDLER);
	// Metadata - Release - Databases
	private final ListSettingsProperty<MetadataDbSettingsItem>		releaseDbs							= new ListSettingsProperty<>("metadata.release.databases",
			MetadataDbSettingsItem.getListConfigurationPropertyHandler(),
			MetadataDbSettingsItem.createObservableList());
	// Metadata - Release - Guessing
	private final BooleanSettingsProperty							guessingEnabled						= new BooleanSettingsProperty("metadata.release.guessing[@enabled]", true);
	private final ListSettingsProperty<StandardRelease>				standardReleases					= new ListSettingsProperty<>("metadata.release.guessing.standardReleases",
			ConfigurationPropertyHandlers.STANDARD_RELEASE_LIST_HANDLER);
	// Metadata - Release - Compatibility
	private final BooleanSettingsProperty							compatibilityEnabled				= new BooleanSettingsProperty("metadata.release.compatibility[@enabled]", true);
	private final ListSettingsProperty<CompatibilitySettingsItem>	compatibilities						= new ListSettingsProperty<>("metadata.release.compatibility.compatibilities",
			CompatibilitySettingsItem.getListConfigurationPropertyHandler(),
			CompatibilitySettingsItem.createObservableList());
	// Correction - Rules
	private final ListSettingsProperty<CorrectorSettingsItem<?, ?>>	correctionRules						= new ListSettingsProperty<>("correction.rules",
			CorrectorSettingsItem.getListConfigurationPropertyHandler(),
			CorrectorSettingsItem.createObservableList());
	// Correction - Subtitle language
	private final LocaleLanguageReplacerSettings					subtitleLanguageCorrectionSettings	= new LocaleLanguageReplacerSettings();

	// Naming
	private final MapSettingsProperty<String, Object>				namingParameters					= new MapSettingsProperty<>("naming.parameters",
			ConfigurationPropertyHandlers.NAMING_PARAMETER_MAP_HANDLER);

	// File Transformation
	// File Transformation - General
	private final ObjectSettingsProperty<Path>						targetDir							= new ObjectSettingsProperty<>("fileTransformation.targetDir",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final BooleanSettingsProperty							deleteSource						= new BooleanSettingsProperty("fileTransformation.deleteSource", false);
	// File Transformation - Packing
	private final BooleanSettingsProperty							packingEnabled						= new BooleanSettingsProperty("fileTransformation.packing[@enabled]", true);
	private final ObjectSettingsProperty<Path>						rarExe								= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.rarExe",
			ConfigurationPropertyHandlers.PATH_HANDLER);
	private final ObjectSettingsProperty<LocateStrategy>			winRarLocateStrategy				= new ObjectSettingsProperty<>("fileTransformation.packing.winrar.locateStrategy",
			LOCATE_STRATEGY_HANDLER,
			LocateStrategy.AUTO_LOCATE);
	private final ObjectSettingsProperty<DeletionMode>				packingSourceDeletionMode			= new ObjectSettingsProperty<>("fileTransformation.packing.sourceDeletionMode",
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
