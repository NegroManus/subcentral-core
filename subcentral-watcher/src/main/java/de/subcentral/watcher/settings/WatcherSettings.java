package de.subcentral.watcher.settings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ObservableObject;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class WatcherSettings extends ObservableObject
{
    public static final WatcherSettings	INSTANCE = new WatcherSettings();
    private static final Logger		log	 = LogManager.getLogger(WatcherSettings.class);

    /**
     * Whether the settings changed since initial load
     */
    private BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);

    // Watch
    private final ListProperty<Path> watchDirectories		     = new SimpleListProperty<>(this, "watchDirectories");
    private final BooleanProperty    initialScan		     = new SimpleBooleanProperty(this, "initialScan");
    // Processing
    private final ProcessingSettings processingSettings		     = new ProcessingSettings();
    // UI
    // UI - Warnings
    private final BooleanProperty    warningsEnabled		     = new SimpleBooleanProperty(this, "warningsEnabled");
    private final BooleanProperty    guessingWarningEnabled	     = new SimpleBooleanProperty(this, "guessingWarningEnabled");
    private final BooleanProperty    releaseMetaTaggedWarningEnabled = new SimpleBooleanProperty(this, "releaseMetaTaggedWarningEnabled");
    private final BooleanProperty    releaseNukedWarningEnabled	     = new SimpleBooleanProperty(this, "releaseNukedWarningEnabled");

    private WatcherSettings()
    {
	super.bind(watchDirectories, initialScan, processingSettings);

	addListener((Observable o) -> changed.set(true));
    }

    public void load(Path path) throws ConfigurationException
    {
	try
	{
	    load(path.toUri().toURL());
	}
	catch (MalformedURLException e)
	{
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void load(URL file) throws ConfigurationException
    {
	log.info("Loading settings from {}", file);

	XMLConfiguration cfg = new XMLConfiguration();
	// cfg.addEventListener(Event.ANY, (Event event) -> {
	// System.out.println(event);
	// });

	FileHandler cfgFileHandler = new FileHandler(cfg);
	cfgFileHandler.load(file);

	Platform.runLater(() -> {
	    load(cfg);
	    changed.set(false);
	});
    }

    private void load(XMLConfiguration cfg)
    {
	if (!Platform.isFxApplicationThread())
	{
	    throw new IllegalStateException("The update of the runtime settings has to be executed on the JavaFX Application Thread");
	}
	// Watch
	updateWatchDirs(cfg);
	updateInitialScan(cfg);

	// Processing
	processingSettings.load(cfg);

	// UI
	// UI - Warnings
	updateWarnings(cfg);
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

    private void updateWarnings(XMLConfiguration cfg)
    {
	setWarningsEnabled(cfg.getBoolean("ui.warnings[@enabled]"));
	setGuessingWarningEnabled(cfg.getBoolean("ui.warnings.guessingWarning[@enabled]"));
	setReleaseMetaTaggedWarningEnabled(cfg.getBoolean("ui.warnings.releaseMetaTaggedWarning[@enabled]"));
	setReleaseNukedWarningEnabled(cfg.getBoolean("ui.warnings.releaseNukedWarning[@enabled]"));
    }

    // Write methods
    public void save(Path file) throws ConfigurationException, IOException
    {
	log.info("Saving settings to {}", file.toAbsolutePath());

	XMLConfiguration cfg = new IndentingXMLConfiguration();
	cfg.setRootElementName("watcherConfig");

	FxUtil.runAndWait(() -> save(cfg));

	FileHandler cfgFileHandler = new FileHandler(cfg);
	cfgFileHandler.save(Files.newOutputStream(file), Charset.forName("UTF-8").name());
	changed.set(false);
    }

    private void save(XMLConfiguration cfg)
    {
	if (!Platform.isFxApplicationThread())
	{
	    throw new IllegalStateException("The export of the runtime settings has to be executed on the JavaFX Application Thread");
	}

	// Watch
	for (Path path : watchDirectories)
	{
	    ConfigurationHelper.addPath(cfg, "watch.directories.dir", path);
	}
	cfg.addProperty("watch.initialScan", isInitialScan());

	// Processing
	processingSettings.save(cfg);

	// UI
	// UI - Warnings
	cfg.addProperty("ui.warnings[@enabled]", isWarningsEnabled());
	cfg.addProperty("ui.warnings.guessingWarning[@enabled]", isGuessingWarningEnabled());
	cfg.addProperty("ui.warnings.releaseMetaTaggedWarning[@enabled]", isReleaseMetaTaggedWarningEnabled());
	cfg.addProperty("ui.warnings.releaseNukedWarning[@enabled]", isReleaseNukedWarningEnabled());
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
