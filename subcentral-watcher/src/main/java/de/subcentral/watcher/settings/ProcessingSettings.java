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
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
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

    // package protected (should only be instantiated by WatcherSettings)
    ProcessingSettings()
    {
	super.bind(filenamePatterns,
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
	loadPreMetadataDbStandardizingRules(cfg);
	loadPostMetadataDbStandardizingRules(cfg);
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

    private void loadPreMetadataDbStandardizingRules(XMLConfiguration cfg)
    {
	setPreMetadataDbStandardizingRules(ConfigurationHelper.getStandardizers(cfg, "standardizing.preMetadataDb.standardizers"));
    }

    private void loadPostMetadataDbStandardizingRules(XMLConfiguration cfg)
    {
	setPostMetadataDbStandardizingRules(ConfigurationHelper.getStandardizers(cfg, "standardizing.postMetadataDb.standardizers"));
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
	setAutoLocateWinRar(cfg.getBoolean("fileTransformation.packing.winrar.autoLocate"));
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
    }
}
