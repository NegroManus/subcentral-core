package de.subcentral.watcher.settings;

import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FxUtil;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class ProcessingSettings extends AbstractSubSettings
{
    // Parsing
    private final StringProperty				filenamePatterns	 = new SimpleStringProperty(this, "filenamePatterns");
    private final ListProperty<ParsingServiceSettingEntry>	filenameParsingServices	 = new SimpleListProperty<>(this, "filenameParsingServices");
    // Metadata
    // Metadata - Release
    private final ListProperty<ParsingServiceSettingEntry>	releaseParsingServices	 = new SimpleListProperty<>(this, "releaseParsingServices");
    private final ListProperty<Tag>				releaseMetaTags		 = new SimpleListProperty<>(this, "releaseMetaTags");
    // Metadata - Release - Databases
    private final ListProperty<MetadataDbSettingEntry<Release>>	releaseDbs		 = new SimpleListProperty<>(this, "releaseDbs");
    // Metadata - Release - Guessing
    private final BooleanProperty				guessingEnabled		 = new SimpleBooleanProperty(this, "guessingEnabled");
    private final ListProperty<StandardRelease>			standardReleases	 = new SimpleListProperty<>(this, "standardReleases");
    // Metadata - Release - Compatibility
    private final BooleanProperty				compatibilityEnabled	 = new SimpleBooleanProperty(this, "compatibilityEnabled");
    private final ListProperty<CompatibilitySettingEntry>	compatibilities		 = new SimpleListProperty<>(this, "compatibilities");
    // Standardizing
    private final ListProperty<StandardizerSettingEntry<?, ?>>	standardizers		 = new SimpleListProperty<>(this, "standardizers");
    // Standardizing - Subtitle language
    private final LocaleLanguageReplacerSettings		subtitleLanguageSettings = new LocaleLanguageReplacerSettings();

    // Naming
    private final MapProperty<String, Object> namingParameters = new SimpleMapProperty<>(this, "namingParameters");

    // File Transformation
    // File Transformation - General
    private final Property<Path>	   targetDir		     = new SimpleObjectProperty<>(this, "targetDir", null);
    private final BooleanProperty	   deleteSource		     = new SimpleBooleanProperty(this, "deleteSource");
    // File Transformation - Packing
    private final BooleanProperty	   packingEnabled	     = new SimpleBooleanProperty(this, "packingEnabled");
    private final Property<Path>	   rarExe		     = new SimpleObjectProperty<>(this, "rarExe", null);
    private final Property<LocateStrategy> winRarLocateStrategy	     = new SimpleObjectProperty<>(this, "winRarLocateStrategy");
    private final Property<DeletionMode>   packingSourceDeletionMode = new SimpleObjectProperty<>(this, "packingSourceDeletionMode", DeletionMode.DELETE);

    // package protected (should only be instantiated by WatcherSettings)
    ProcessingSettings()
    {
	super.bind(filenamePatterns,
		FxUtil.observeBeans(filenameParsingServices, (ParsingServiceSettingEntry entry) -> new Observable[] { entry.enabledProperty() }),
		FxUtil.observeBeans(releaseParsingServices, (ParsingServiceSettingEntry entry) -> new Observable[] { entry.enabledProperty() }),
		releaseMetaTags,
		FxUtil.observeBeans(releaseDbs, (MetadataDbSettingEntry<Release> entry) -> new Observable[] { entry.enabledProperty() }),
		guessingEnabled,
		standardReleases,
		compatibilityEnabled,
		FxUtil.observeBeans(compatibilities, (CompatibilitySettingEntry entry) -> new Observable[] { entry.enabledProperty() }),
		FxUtil.observeBeans(standardizers, (StandardizerSettingEntry<?, ?> entry) -> new Observable[] { entry.beforeQueryingProperty(), entry.afterQueryingProperty() }),
		subtitleLanguageSettings,
		namingParameters,
		targetDir,
		deleteSource,
		packingEnabled,
		rarExe,
		winRarLocateStrategy,
		packingSourceDeletionMode);
    }

    @Override
    public String getKey()
    {
	return "processing";
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

    public final ListProperty<StandardizerSettingEntry<?, ?>> standardizersProperty()
    {
	return this.standardizers;
    }

    public final javafx.collections.ObservableList<de.subcentral.watcher.settings.StandardizerSettingEntry<?, ?>> getStandardizers()
    {
	return this.standardizersProperty().get();
    }

    public final void setStandardizers(final javafx.collections.ObservableList<de.subcentral.watcher.settings.StandardizerSettingEntry<?, ?>> standardizers)
    {
	this.standardizersProperty().set(standardizers);
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

    public final Property<LocateStrategy> winRarLocateStrategyProperty()
    {
	return this.winRarLocateStrategy;
    }

    public final de.subcentral.support.winrar.WinRar.LocateStrategy getWinRarLocateStrategy()
    {
	return this.winRarLocateStrategyProperty().getValue();
    }

    public final void setWinRarLocateStrategy(final de.subcentral.support.winrar.WinRar.LocateStrategy winRarLocateStrategy)
    {
	this.winRarLocateStrategyProperty().setValue(winRarLocateStrategy);
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

	// Standardizing
	loadStandardizers(cfg);
	subtitleLanguageSettings.load(cfg);

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

    private void loadStandardizers(XMLConfiguration cfg)
    {
	setStandardizers(ConfigurationHelper.getStandardizers(cfg, "standardizing.standardizers"));
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
	setWinRarLocateStrategy(LocateStrategy.valueOf(cfg.getString("fileTransformation.packing.winrar.locateStrategy")));
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
	    cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@scope]", stdRls.getScope());
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
	ConfigurationHelper.addStandardizers(cfg, "standardizing.standardizers", standardizers);

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
	cfg.addProperty("fileTransformation.packing.winrar.locateStrategy", getWinRarLocateStrategy());
	ConfigurationHelper.addPath(cfg, "fileTransformation.packing.winrar.rarExe", getRarExe());
    }
}
