package de.subcentral.watcher.settings;

import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ObservableObject;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class WatcherSettings extends ObservableObject
{
	public static final WatcherSettings	INSTANCE						= new WatcherSettings();
	private static final Logger			log								= LogManager.getLogger(WatcherSettings.class);

	/**
	 * Whether the settings changed since initial load
	 */
	private BooleanProperty				changed							= new SimpleBooleanProperty(this, "changed", false);

	// Watch
	private final ListProperty<Path>	watchDirectories				= new SimpleListProperty<>(this, "watchDirectories", FXCollections.observableArrayList());
	private final BooleanProperty		initialScan						= new SimpleBooleanProperty(this, "initialScan");
	private final BooleanProperty		rejectAlreadyProcessedFiles		= new SimpleBooleanProperty(this, "rejectAlreadyProcessedFiles");

	// Processing
	private final ProcessingSettings	processingSettings				= new ProcessingSettings();
	// UI
	// UI - Warnings
	private final BooleanProperty		warningsEnabled					= new SimpleBooleanProperty(this, "warningsEnabled");
	private final BooleanProperty		guessingWarningEnabled			= new SimpleBooleanProperty(this, "guessingWarningEnabled");
	private final BooleanProperty		releaseMetaTaggedWarningEnabled	= new SimpleBooleanProperty(this, "releaseMetaTaggedWarningEnabled");
	private final BooleanProperty		releaseNukedWarningEnabled		= new SimpleBooleanProperty(this, "releaseNukedWarningEnabled");
	// UI - System Tray
	private final BooleanProperty		systemTrayEnabled				= new SimpleBooleanProperty(this, "systemTrayEnabled");

	private WatcherSettings()
	{
		super.bind(watchDirectories,
				initialScan,
				rejectAlreadyProcessedFiles,
				processingSettings,
				warningsEnabled,
				guessingWarningEnabled,
				releaseMetaTaggedWarningEnabled,
				releaseNukedWarningEnabled,
				systemTrayEnabled);

		addListener((Observable o) -> changed.set(true));
	}

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(URL file) throws ConfigurationException
	{
		FxUtil.checkJavaFxApplicationThread();

		XMLConfiguration cfg = ConfigurationHelper.load(file);
		loadFromCfg(cfg);
		changed.set(false);

		log.info("Loaded settings from {}", file);
	}

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(Path file) throws ConfigurationException
	{
		FxUtil.checkJavaFxApplicationThread();

		XMLConfiguration cfg = ConfigurationHelper.load(file);
		loadFromCfg(cfg);
		changed.set(false);

		log.info("Loaded settings from {}", file);
	}

	/**
	 * <b>HAS</b> to be called in the FX-Application-Thread.
	 * 
	 * @param cfg
	 *            the cfg from which the settings should be loaded
	 */
	private void loadFromCfg(XMLConfiguration cfg)
	{
		// Watch
		updateWatchDirs(cfg);
		updateInitialScan(cfg);
		updateRejectAlreadyProcessedFiles(cfg);

		// Processing
		processingSettings.load(cfg);

		// UI
		// UI - Warnings
		updateWarnings(cfg);
		updateSystemTray(cfg);
	}

	private void updateWatchDirs(XMLConfiguration cfg)
	{
		Set<Path> dirs = new LinkedHashSet<>();
		for (String watchDirPath : cfg.getList(String.class, "watch.directories.dir", ImmutableList.of()))
		{
			try
			{
				Path watchDir = Paths.get(watchDirPath);
				dirs.add(watchDir);
			}
			catch (InvalidPathException e)
			{
				log.warn("Invalid path for watch directory:" + watchDirPath + ". Ignoring the path", e);
			}
		}
		setWatchDirectories(FXCollections.observableArrayList(dirs));
	}

	private void updateInitialScan(XMLConfiguration cfg)
	{
		setInitialScan(cfg.getBoolean("watch.initialScan", false));
	}

	private void updateRejectAlreadyProcessedFiles(XMLConfiguration cfg)
	{
		setRejectAlreadyProcessedFiles(cfg.getBoolean("rejectAlreadyProcessedFiles", true));
	}

	private void updateWarnings(XMLConfiguration cfg)
	{
		setWarningsEnabled(cfg.getBoolean("ui.warnings[@enabled]"));
		setGuessingWarningEnabled(cfg.getBoolean("ui.warnings.guessingWarning[@enabled]"));
		setReleaseMetaTaggedWarningEnabled(cfg.getBoolean("ui.warnings.releaseMetaTaggedWarning[@enabled]"));
		setReleaseNukedWarningEnabled(cfg.getBoolean("ui.warnings.releaseNukedWarning[@enabled]"));
	}

	private void updateSystemTray(XMLConfiguration cfg)
	{
		setSystemTrayEnabled(cfg.getBoolean("ui.systemTray[@enabled]"));
	}

	// Write methods
	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void save(Path file) throws ConfigurationException
	{
		FxUtil.checkJavaFxApplicationThread();

		XMLConfiguration cfg = saveToCfg();
		ConfigurationHelper.save(cfg, file);
		FxUtil.runAndWait(() -> changed.set(false));

		log.info("Saved settings to {}", file.toAbsolutePath());
	}

	private XMLConfiguration saveToCfg()
	{
		XMLConfiguration cfg = new IndentingXMLConfiguration();
		cfg.setRootElementName("watcherConfig");

		// Watch
		for (Path path : watchDirectories)
		{
			ConfigurationHelper.addPath(cfg, "watch.directories.dir", path);
		}
		cfg.addProperty("watch.initialScan", isInitialScan());
		cfg.addProperty("watch.denyAlreadyProcessedFiles", isRejectAlreadyProcessedFiles());

		// Processing
		processingSettings.save(cfg);

		// UI
		// UI - Warnings
		cfg.addProperty("ui.warnings[@enabled]", isWarningsEnabled());
		cfg.addProperty("ui.warnings.guessingWarning[@enabled]", isGuessingWarningEnabled());
		cfg.addProperty("ui.warnings.releaseMetaTaggedWarning[@enabled]", isReleaseMetaTaggedWarningEnabled());
		cfg.addProperty("ui.warnings.releaseNukedWarning[@enabled]", isReleaseNukedWarningEnabled());
		// UI- System Tray
		cfg.addProperty("ui.systemTray[@enabled]", isSystemTrayEnabled());

		return cfg;
	}

	// Getter and Setter
	public BooleanProperty changedProperty()
	{
		return changed;
	}

	public boolean getChanged()
	{
		return changed.get();
	}

	public final ListProperty<Path> watchDirectoriesProperty()
	{
		return this.watchDirectories;
	}

	public final ObservableList<Path> getWatchDirectories()
	{
		return this.watchDirectoriesProperty().get();
	}

	public final void setWatchDirectories(final ObservableList<Path> watchDirectories)
	{
		this.watchDirectoriesProperty().set(watchDirectories);
	}

	public final BooleanProperty initialScanProperty()
	{
		return this.initialScan;
	}

	public final boolean isInitialScan()
	{
		return this.initialScanProperty().get();
	}

	public final void setInitialScan(final boolean initialScan)
	{
		this.initialScanProperty().set(initialScan);
	}

	public final BooleanProperty rejectAlreadyProcessedFilesProperty()
	{
		return this.rejectAlreadyProcessedFiles;
	}

	public final boolean isRejectAlreadyProcessedFiles()
	{
		return this.rejectAlreadyProcessedFiles.get();
	}

	public final void setRejectAlreadyProcessedFiles(final boolean rejectAlreadyProcessedFiles)
	{
		this.rejectAlreadyProcessedFiles.set(rejectAlreadyProcessedFiles);
	}

	public ProcessingSettings getProcessingSettings()
	{
		return processingSettings;
	}

	public final BooleanProperty warningsEnabledProperty()
	{
		return this.warningsEnabled;
	}

	public final boolean isWarningsEnabled()
	{
		return this.warningsEnabledProperty().get();
	}

	public final void setWarningsEnabled(final boolean warningsEnabled)
	{
		this.warningsEnabledProperty().set(warningsEnabled);
	}

	public final BooleanProperty guessingWarningEnabledProperty()
	{
		return this.guessingWarningEnabled;
	}

	public final boolean isGuessingWarningEnabled()
	{
		return this.guessingWarningEnabledProperty().get();
	}

	public final void setGuessingWarningEnabled(final boolean guessingWarningEnabled)
	{
		this.guessingWarningEnabledProperty().set(guessingWarningEnabled);
	}

	public final BooleanProperty releaseMetaTaggedWarningEnabledProperty()
	{
		return this.releaseMetaTaggedWarningEnabled;
	}

	public final boolean isReleaseMetaTaggedWarningEnabled()
	{
		return this.releaseMetaTaggedWarningEnabledProperty().get();
	}

	public final void setReleaseMetaTaggedWarningEnabled(final boolean releaseMetaTaggedWarningEnabled)
	{
		this.releaseMetaTaggedWarningEnabledProperty().set(releaseMetaTaggedWarningEnabled);
	}

	public final BooleanProperty releaseNukedWarningEnabledProperty()
	{
		return this.releaseNukedWarningEnabled;
	}

	public final boolean isReleaseNukedWarningEnabled()
	{
		return this.releaseNukedWarningEnabledProperty().get();
	}

	public final void setReleaseNukedWarningEnabled(final boolean releaseNukedWarningEnabled)
	{
		this.releaseNukedWarningEnabledProperty().set(releaseNukedWarningEnabled);
	}

	public final BooleanProperty systemTrayEnabledProperty()
	{
		return this.systemTrayEnabled;
	}

	public final boolean isSystemTrayEnabled()
	{
		return this.systemTrayEnabledProperty().get();
	}

	public final void setSystemTrayEnabled(final boolean systemTrayEnabled)
	{
		this.systemTrayEnabledProperty().set(systemTrayEnabled);
	}

	private static class IndentingXMLConfiguration extends XMLConfiguration
	{
		@Override
		protected Transformer createTransformer() throws ConfigurationException
		{
			Transformer transformer = super.createTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			return transformer;
		}
	}
}
