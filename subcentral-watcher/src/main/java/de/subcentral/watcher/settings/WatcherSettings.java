package de.subcentral.watcher.settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.model.ObservableObject;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class WatcherSettings extends ObservableObject
{
    public enum FileTransformationMode
    {
	COPY, MOVE
    }

    public static final WatcherSettings	INSTANCE = new WatcherSettings();
    private static final Logger		log	 = LogManager.getLogger(WatcherSettings.class);

    /**
     * Whether the settings changed since initial load
     */
    private BooleanProperty changed = new SimpleBooleanProperty(this, "changed", false);

    // Watch
    private final ListProperty<Path>				watchDirectories	   = new SimpleListProperty<>(this, "watchDirectories");
    private final BooleanProperty				initialScan		   = new SimpleBooleanProperty(this, "initialScan");
    // Parsing
    private final StringProperty				filenamePatterns	   = new SimpleStringProperty(this, "filenamePatterns");
    private final ListProperty<ParsingServiceSettingEntry>	filenameParsingServices	   = new SimpleListProperty<>(this, "filenameParsingServices");
    // Metadata
    // Metadata - Release
    private final ListProperty<ParsingServiceSettingEntry>	releaseParsingServices	   = new SimpleListProperty<>(this, "releaseParsingServices");
    private final ListProperty<Tag>				releaseMetaTags		   = new SimpleListProperty<>(this, "releaseMetaTags");
    // Metadata - Release - Databases
    private final ListProperty<MetadataDbSettingEntry<Release>>	releaseDbs		   = new SimpleListProperty<>(this, "releaseDbs");
    // Metadata - Release - Guessing
    private final BooleanProperty				guessingEnabled		   = new SimpleBooleanProperty(this, "guessingEnabled");
    private final ListProperty<StandardRelease>			standardReleases	   = new SimpleListProperty<>(this, "standardReleases");
    // Metadata - Release - Compatibility
    private final BooleanProperty				compatibilityEnabled	   = new SimpleBooleanProperty(this, "compatibilityEnabled");
    private final ListProperty<CompatibilitySettingEntry>	compatibilities		   = new SimpleListProperty<>(this, "compatibilities");
    // Standardizing
    private final ListProperty<StandardizerSettingEntry<?, ?>>	preMetadataDbStandardizers = new SimpleListProperty<>(this, "preMetadataDbStandardizers");
    private final ListProperty<StandardizerSettingEntry<?, ?>>	postMetadataStandardizers  = new SimpleListProperty<>(this, "postMetadataStandardizers");
    // Standardizing - Subtitle language
    private final LocaleLanguageReplacerSettings		subtitleLanguageSettings   = new LocaleLanguageReplacerSettings();

    // Naming
    private final MapProperty<String, Object> namingParameters = new SimpleMapProperty<>(this, "namingParameters");

    // File Transformation
    // File Transformation - General
    private final Property<Path>	 targetDir		   = new SimpleObjectProperty<>(this, "targetDir", null);
    private final BooleanProperty	 deleteSource		   = new SimpleBooleanProperty(this, "deleteSource");
    // File Transformation - Packing
    private final BooleanProperty	 packingEnabled		   = new SimpleBooleanProperty(this, "packingEnabled");
    private final Property<Path>	 rarExe			   = new SimpleObjectProperty<>(this, "rarExe", null);
    private final BooleanProperty	 autoLocateWinRar	   = new SimpleBooleanProperty(this, "autoLocateWinRar");
    private final Property<DeletionMode> packingSourceDeletionMode = new SimpleObjectProperty<>(this, "packingSourceDeletionMode", DeletionMode.DELETE);

    private WatcherSettings()
    {
	super.bind(watchDirectories,
		initialScan,
		filenamePatterns,
		filenameParsingServices,
		SettingsUtil.observeEnablementOfSettingEntries(filenameParsingServices),
		releaseParsingServices,
		SettingsUtil.observeEnablementOfSettingEntries(releaseParsingServices),
		releaseMetaTags,
		releaseDbs,
		SettingsUtil.observeEnablementOfSettingEntries(releaseDbs),
		guessingEnabled,
		standardReleases,
		compatibilityEnabled,
		compatibilities,
		preMetadataDbStandardizers,
		postMetadataStandardizers,
		subtitleLanguageSettings,
		namingParameters,
		targetDir,
		deleteSource,
		packingEnabled,
		rarExe,
		autoLocateWinRar,
		packingSourceDeletionMode);

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
	log.info("Loading settings from file {}", file);

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

	// FileParsing
	updateFilenamePatterns(cfg);
	updateFilenameParsingServices(cfg);

	// Metadata
	// Metadata - Release
	updateReleaseParsingServices(cfg);
	updateReleaseMetaTags(cfg);
	// Metadata - Release - Databases
	updateReleaseDbs(cfg);
	// Metadata - Release - Guessing
	updateGuessingEnabled(cfg);
	updateStandardReleases(cfg);
	// Metadata - Release - Compatibility
	updateCompatibilityEnabled(cfg);
	updateCompatibilities(cfg);

	// Standardizing
	updatePreMetadataDbStandardizingRules(cfg);
	updatePostMetadataDbStandardizingRules(cfg);
	subtitleLanguageSettings.load(cfg);

	// Naming
	updateNamingParameters(cfg);

	// File Transformation - General
	updateTargetDir(cfg);
	updateDeleteSource(cfg);

	// File Transformation - Packing
	updatePackingEnabled(cfg);
	updateAutoLocateWinRar(cfg);
	updateRarExe(cfg);
	updatePackingSourceDeletionMode(cfg);
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

    private void updateFilenamePatterns(XMLConfiguration cfg)
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

    private void updateFilenameParsingServices(XMLConfiguration cfg)
    {
	setFilenameParsingServices(ConfigurationHelper.getParsingServices(cfg, "parsing.parsingServices"));
    }

    private void updateReleaseParsingServices(XMLConfiguration cfg)
    {
	setReleaseParsingServices(ConfigurationHelper.getParsingServices(cfg, "metadata.release.parsingServices"));
    }

    private void updateReleaseMetaTags(XMLConfiguration cfg)
    {
	setReleaseMetaTags(ConfigurationHelper.getTags(cfg, "metadata.release.metaTags"));
    }

    private void updateReleaseDbs(XMLConfiguration cfg)
    {
	setReleaseDbs(ConfigurationHelper.getReleaseDbs(cfg, "metadata.release.databases"));
    }

    private void updateGuessingEnabled(XMLConfiguration cfg)
    {
	setGuessingEnabled(cfg.getBoolean("metadata.release.guessing[@enabled]", false));
    }

    private void updateStandardReleases(XMLConfiguration cfg)
    {
	setStandardReleases(ConfigurationHelper.getStandardReleases(cfg, "metadata.release.guessing.standardReleases"));
    }

    private void updateCompatibilityEnabled(XMLConfiguration cfg)
    {
	setCompatibilityEnabled(cfg.getBoolean("metadata.release.compatibility[@enabled]", false));
    }

    private void updateCompatibilities(XMLConfiguration cfg)
    {
	setCompatibilities(ConfigurationHelper.getCompatibilities(cfg, "metadata.release.compatibility.compatibilities"));
    }

    private void updatePreMetadataDbStandardizingRules(XMLConfiguration cfg)
    {
	setPreMetadataDbStandardizingRules(ConfigurationHelper.getStandardizers(cfg, "standardizing.preMetadataDb.standardizers"));
    }

    private void updatePostMetadataDbStandardizingRules(XMLConfiguration cfg)
    {
	setPostMetadataDbStandardizingRules(ConfigurationHelper.getStandardizers(cfg, "standardizing.postMetadataDb.standardizers"));
    }

    private void updateNamingParameters(XMLConfiguration cfg)
    {
	setNamingParameters(ConfigurationHelper.getNamingParameters(cfg, "naming.parameters"));
    }

    private void updateTargetDir(XMLConfiguration cfg)
    {
	setTargetDir(ConfigurationHelper.getPath(cfg, "fileTransformation.targetDir"));
    }

    private void updateDeleteSource(XMLConfiguration cfg)
    {
	setDeleteSource(cfg.getBoolean("fileTransformation.deleteSource"));
    }

    private void updatePackingEnabled(XMLConfiguration cfg)
    {
	setPackingEnabled(cfg.getBoolean("fileTransformation.packing[@enabled]"));
    }

    private void updateAutoLocateWinRar(XMLConfiguration cfg)
    {
	setAutoLocateWinRar(cfg.getBoolean("fileTransformation.packing.winrar.autoLocate"));
    }

    private void updateRarExe(XMLConfiguration cfg)
    {
	setRarExe(ConfigurationHelper.getPath(cfg, "fileTransformation.packing.winrar.rarExe"));
    }

    private void updatePackingSourceDeletionMode(XMLConfiguration cfg)
    {
	setPackingSourceDeletionMode(DeletionMode.valueOf(cfg.getString("fileTransformation.packing.sourceDeletionMode")));
    }

    // Write methods
    public void save(Path file) throws ConfigurationException
    {
	log.info("Saving settings to file {}", file.toAbsolutePath());

	XMLConfiguration cfg = snapshot();

	FileHandler cfgFileHandler = new FileHandler(cfg);
	cfgFileHandler.save(file.toFile());
	changed.set(false);
    }

    private XMLConfiguration snapshot()
    {
	XMLConfiguration cfg = new IndentingXMLConfiguration();
	cfg.setRootElementName("watcherConfig");
	// Watch
	for (Path path : watchDirectories)
	{
	    ConfigurationHelper.addPath(cfg, "watch.directories.dir", path);
	}
	cfg.addProperty("watch.initialScan", isInitialScan());

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
	    MetadataDbSettingEntry<Release> db = releaseDbs.get(i);
	    cfg.addProperty("metadata.release.databases.db(" + i + ")", db.getValue().getDomain());
	    cfg.addProperty("metadata.release.databases.db(" + i + ")[@enabled]", db.isEnabled());
	}

	// // Metadata - Release - Guessing
	cfg.addProperty("metadata.release.guessing[@enabled]", isGuessingEnabled());
	for (int i = 0; i < standardReleases.size(); i++)
	{
	    StandardRelease stdRls = standardReleases.get(i);
	    cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@tags]", Tag.listToString(stdRls.getRelease().getTags()));
	    cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@group]", stdRls.getRelease().getGroup());
	    cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@assumeExistence]", stdRls.getAssumeExistence());
	}

	// Metadata - Release - Compatibility
	cfg.addProperty("metadata.release.compatibility[@enabled]", isCompatibilityEnabled());
	for (int i = 0; i < compatibilities.size(); i++)
	{
	    CompatibilitySettingEntry entry = compatibilities.get(i);
	    Compatibility c = entry.getValue();
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

	// Standardizing
	ConfigurationHelper.addStandardizers(cfg, "standardizing.preMetadataDb.standardizers", preMetadataDbStandardizers);
	ConfigurationHelper.addStandardizers(cfg, "standardizing.postMetadataDb.standardizers", postMetadataStandardizers);

	subtitleLanguageSettings.save(cfg);

	// Naming
	int i = 0;
	for (Map.Entry<String, Object> param : namingParameters.entrySet())
	{
	    cfg.addProperty("naming.parameters.param(" + i + ")[@key]", param.getKey());
	    cfg.addProperty("naming.parameters.param(" + i + ")[@value]", param.getValue());
	    i++;
	}

	// File Transformation - General
	ConfigurationHelper.addPath(cfg, "fileTransformation.targetDir", getTargetDir());
	cfg.addProperty("fileTransformation.deleteSource", isDeleteSource());

	// File Transformation - Packing
	cfg.addProperty("fileTransformation.packing[@enabled]", isPackingEnabled());
	cfg.addProperty("fileTransformation.packing.sourceDeletionMode", getPackingSourceDeletionMode());
	cfg.addProperty("fileTransformation.packing.winrar.autoLocate", isAutoLocateWinRar());
	ConfigurationHelper.addPath(cfg, "fileTransformation.packing.winrar.rarExe", getRarExe());

	return cfg;
    }

    // Getter and Setter
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

    public final StringProperty filenamePatternsProperty()
    {
	return this.filenamePatterns;
    }

    public final String getFilenamePatterns()
    {
	return this.filenamePatternsProperty().get();
    }

    public final void setFilenamePatterns(final String filenamePatterns)
    {
	this.filenamePatternsProperty().set(filenamePatterns);
    }

    public final ListProperty<ParsingServiceSettingEntry> filenameParsingServicesProperty()
    {
	return this.filenameParsingServices;
    }

    public final ObservableList<ParsingServiceSettingEntry> getFilenameParsingServices()
    {
	return this.filenameParsingServicesProperty().get();
    }

    public final void setFilenameParsingServices(final ObservableList<ParsingServiceSettingEntry> filenameParsingServices)
    {
	this.filenameParsingServicesProperty().set(filenameParsingServices);
    }

    public final ListProperty<MetadataDbSettingEntry<Release>> releaseDbsProperty()
    {
	return this.releaseDbs;
    }

    public final ObservableList<MetadataDbSettingEntry<Release>> getReleaseDbs()
    {
	return this.releaseDbsProperty().get();
    }

    public final void setReleaseDbs(final ObservableList<MetadataDbSettingEntry<Release>> releaseInfoDbs)
    {
	this.releaseDbsProperty().set(releaseInfoDbs);
    }

    public final ListProperty<ParsingServiceSettingEntry> releaseParsingServicesProperty()
    {
	return this.releaseParsingServices;
    }

    public final ObservableList<ParsingServiceSettingEntry> getReleaseParsingServices()
    {
	return this.releaseParsingServicesProperty().get();
    }

    public final void setReleaseParsingServices(final ObservableList<ParsingServiceSettingEntry> releaseParsingServices)
    {
	this.releaseParsingServicesProperty().set(releaseParsingServices);
    }

    public final BooleanProperty guessingEnabledProperty()
    {
	return this.guessingEnabled;
    }

    public final boolean isGuessingEnabled()
    {
	return this.guessingEnabledProperty().get();
    }

    public final void setGuessingEnabled(final boolean guessingEnabled)
    {
	this.guessingEnabledProperty().set(guessingEnabled);
    }

    public final ListProperty<Tag> releaseMetaTagsProperty()
    {
	return this.releaseMetaTags;
    }

    public final ObservableList<Tag> getReleaseMetaTags()
    {
	return this.releaseMetaTagsProperty().get();
    }

    public final void setReleaseMetaTags(final ObservableList<Tag> releaseMetaTags)
    {
	this.releaseMetaTagsProperty().set(releaseMetaTags);
    }

    public final ListProperty<StandardRelease> standardReleasesProperty()
    {
	return this.standardReleases;
    }

    public final ObservableList<StandardRelease> getStandardReleases()
    {
	return this.standardReleasesProperty().get();
    }

    public final void setStandardReleases(final ObservableList<StandardRelease> standardReleases)
    {
	this.standardReleasesProperty().set(standardReleases);
    }

    public final BooleanProperty compatibilityEnabledProperty()
    {
	return this.compatibilityEnabled;
    }

    public final boolean isCompatibilityEnabled()
    {
	return this.compatibilityEnabledProperty().get();
    }

    public final void setCompatibilityEnabled(final boolean compatibilityEnabled)
    {
	this.compatibilityEnabledProperty().set(compatibilityEnabled);
    }

    public final ListProperty<CompatibilitySettingEntry> compatibilitiesProperty()
    {
	return this.compatibilities;
    }

    public final ObservableList<CompatibilitySettingEntry> getCompatibilities()
    {
	return this.compatibilitiesProperty().get();
    }

    public final void setCompatibilities(final ObservableList<CompatibilitySettingEntry> compatibilities)
    {
	this.compatibilitiesProperty().set(compatibilities);
    }

    public final ListProperty<StandardizerSettingEntry<?, ?>> parsedStandardizersProperty()
    {
	return this.preMetadataDbStandardizers;
    }

    public final ObservableList<StandardizerSettingEntry<?, ?>> getPreMetadataDbStandardizers()
    {
	return this.parsedStandardizersProperty().get();
    }

    public final void setPreMetadataDbStandardizingRules(final ObservableList<StandardizerSettingEntry<?, ?>> parsedStandardizingRules)
    {
	this.parsedStandardizersProperty().set(parsedStandardizingRules);
    }

    public final ListProperty<StandardizerSettingEntry<?, ?>> postMetadataStandardizersProperty()
    {
	return this.postMetadataStandardizers;
    }

    public final ObservableList<StandardizerSettingEntry<?, ?>> getPostMetadataStandardizers()
    {
	return this.postMetadataStandardizersProperty().get();
    }

    public final void setPostMetadataDbStandardizingRules(final ObservableList<StandardizerSettingEntry<?, ?>> postMetadataStandardizingRules)
    {
	this.postMetadataStandardizersProperty().set(postMetadataStandardizingRules);
    }

    public LocaleLanguageReplacerSettings getSubtitleLanguageSettings()
    {
	return subtitleLanguageSettings;
    }

    public final MapProperty<String, Object> namingParametersProperty()
    {
	return this.namingParameters;
    }

    public final javafx.collections.ObservableMap<String, Object> getNamingParameters()
    {
	return this.namingParametersProperty().get();
    }

    public final void setNamingParameters(final javafx.collections.ObservableMap<String, Object> namingParameters)
    {
	this.namingParametersProperty().set(namingParameters);
    }

    public final Property<Path> targetDirProperty()
    {
	return this.targetDir;
    }

    public final java.nio.file.Path getTargetDir()
    {
	return this.targetDirProperty().getValue();
    }

    public final void setTargetDir(final java.nio.file.Path targetDir)
    {
	this.targetDirProperty().setValue(targetDir);
    }

    public final BooleanProperty deleteSourceProperty()
    {
	return this.deleteSource;
    }

    public final boolean isDeleteSource()
    {
	return this.deleteSourceProperty().get();
    }

    public final void setDeleteSource(final boolean deleteSource)
    {
	this.deleteSourceProperty().set(deleteSource);
    }

    public final BooleanProperty packingEnabledProperty()
    {
	return this.packingEnabled;
    }

    public final boolean isPackingEnabled()
    {
	return this.packingEnabledProperty().get();
    }

    public final void setPackingEnabled(final boolean packingEnabled)
    {
	this.packingEnabledProperty().set(packingEnabled);
    }

    public final Property<Path> rarExeProperty()
    {
	return this.rarExe;
    }

    public final Path getRarExe()
    {
	return this.rarExe.getValue();
    }

    public final void setRarExe(final Path rarExe)
    {
	this.rarExe.setValue(rarExe);
    }

    public final BooleanProperty autoLocateWinRarProperty()
    {
	return this.autoLocateWinRar;
    }

    public final boolean isAutoLocateWinRar()
    {
	return this.autoLocateWinRarProperty().get();
    }

    public final void setAutoLocateWinRar(final boolean autoLocateWinRar)
    {
	this.autoLocateWinRarProperty().set(autoLocateWinRar);
    }

    public final Property<DeletionMode> packingSourceDeletionModeProperty()
    {
	return this.packingSourceDeletionMode;
    }

    public final de.subcentral.support.winrar.WinRarPackConfig.DeletionMode getPackingSourceDeletionMode()
    {
	return this.packingSourceDeletionModeProperty().getValue();
    }

    public final void setPackingSourceDeletionMode(final de.subcentral.support.winrar.WinRarPackConfig.DeletionMode packingSourceDeletionMode)
    {
	this.packingSourceDeletionModeProperty().setValue(packingSourceDeletionMode);
    }

    public ReadOnlyBooleanProperty changedProperty()
    {
	return changed;
    }

    public boolean getChanged()
    {
	return changed.get();
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
