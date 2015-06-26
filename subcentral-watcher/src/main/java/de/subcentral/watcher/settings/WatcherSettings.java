package de.subcentral.watcher.settings;

import java.net.MalformedURLException;
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
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.watcher.model.ObservableObject;
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
    private final ListProperty<Path> watchDirectories	= new SimpleListProperty<>(this, "watchDirectories");
    private final BooleanProperty    initialScan	= new SimpleBooleanProperty(this, "initialScan");
    // Processing
    private final ProcessingSettings processingSettings	= new ProcessingSettings();

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

	load(cfg);

	changed.set(false);
    }

    private void load(XMLConfiguration cfg)
    {
	// Watch
	updateWatchDirs(cfg);
	updateInitialScan(cfg);

	// Processing
	processingSettings.load(cfg);
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

    // Write methods
    public void save(Path file) throws ConfigurationException
    {
	log.info("Saving settings to file {}", file.toAbsolutePath());

	XMLConfiguration cfg = new IndentingXMLConfiguration();
	cfg.setRootElementName("watcherConfig");

	save(cfg);

	FileHandler cfgFileHandler = new FileHandler(cfg);
	cfgFileHandler.save(file.toFile());
	changed.set(false);
    }

    private void save(XMLConfiguration cfg)
    {
	// Watch
	for (Path path : watchDirectories)
	{
	    ConfigurationHelper.addPath(cfg, "watch.directories.dir", path);
	}
	cfg.addProperty("watch.initialScan", isInitialScan());

	// Processing
	processingSettings.save(cfg);
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
