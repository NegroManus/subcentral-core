package de.subcentral.watcher.settings;

import java.nio.file.Path;

import de.subcentral.fx.settings.BooleanSettingsProperty;
import de.subcentral.fx.settings.ConfigurationPropertyHandlers;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.fx.settings.Settings;

public class WatcherSettings extends Settings
{
	// Watch
	private final ListSettingsProperty<Path>	watchDirectories				= new ListSettingsProperty<>("watch.directories.dir", ConfigurationPropertyHandlers.PATH_SORTED_LIST_HANDLER);
	private final BooleanSettingsProperty		initialScan						= new BooleanSettingsProperty("watch.initialScan", true);
	private final BooleanSettingsProperty		rejectAlreadyProcessedFiles		= new BooleanSettingsProperty("watch.rejectAlreadyProcessedFiles", true);

	// Processing
	private final ProcessingSettings			processingSettings				= new ProcessingSettings();
	// UI
	// UI - Warnings
	private final BooleanSettingsProperty		warningsEnabled					= new BooleanSettingsProperty("ui.warnings[@enabled]", true);
	private final BooleanSettingsProperty		guessingWarningEnabled			= new BooleanSettingsProperty("ui.warnings.guessingWarning[@enabled]", true);
	private final BooleanSettingsProperty		releaseMetaTaggedWarningEnabled	= new BooleanSettingsProperty("ui.warnings.releaseMetaTaggedWarning[@enabled]", true);
	private final BooleanSettingsProperty		releaseNukedWarningEnabled		= new BooleanSettingsProperty("ui.warnings.releaseNukedWarning[@enabled]", true);
	// UI - System Tray
	private final BooleanSettingsProperty		systemTrayEnabled				= new BooleanSettingsProperty("ui.systemTray[@enabled]", true);

	public WatcherSettings()
	{
		initSettables(watchDirectories,
				initialScan,
				rejectAlreadyProcessedFiles,
				processingSettings,
				warningsEnabled,
				guessingWarningEnabled,
				releaseMetaTaggedWarningEnabled,
				releaseNukedWarningEnabled,
				systemTrayEnabled);
	}

	public ListSettingsProperty<Path> getWatchDirectories()
	{
		return watchDirectories;
	}

	public BooleanSettingsProperty getInitialScan()
	{
		return initialScan;
	}

	public BooleanSettingsProperty getRejectAlreadyProcessedFiles()
	{
		return rejectAlreadyProcessedFiles;
	}

	public ProcessingSettings getProcessingSettings()
	{
		return processingSettings;
	}

	public BooleanSettingsProperty getWarningsEnabled()
	{
		return warningsEnabled;
	}

	public BooleanSettingsProperty getGuessingWarningEnabled()
	{
		return guessingWarningEnabled;
	}

	public BooleanSettingsProperty getReleaseMetaTaggedWarningEnabled()
	{
		return releaseMetaTaggedWarningEnabled;
	}

	public BooleanSettingsProperty getReleaseNukedWarningEnabled()
	{
		return releaseNukedWarningEnabled;
	}

	public BooleanSettingsProperty getSystemTrayEnabled()
	{
		return systemTrayEnabled;
	}
}
