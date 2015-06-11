package de.subcentral.watcher.settings;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.event.Event;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.AssumeExistence;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.naming.EpisodeNamer;
import de.subcentral.core.naming.ReleaseNamer;
import de.subcentral.core.standardizing.LocaleLanguageReplacer;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.UserPattern.Mode;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.orlydbcom.OrlyDbComReleaseDb;
import de.subcentral.support.predbme.PreDbMeReleaseDb;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.xrelto.XRelToReleaseDb;
import de.subcentral.watcher.model.ObservableBean;

public class WatcherSettings extends ObservableBean
{
	public enum FileTransformationMode
	{
		COPY, MOVE
	}

	public static final WatcherSettings							INSTANCE							= new WatcherSettings();
	private static final Logger									log									= LogManager.getLogger(WatcherSettings.class);

	// Watch
	private final ListProperty<Path>							watchDirectories					= new SimpleListProperty<>(this,
																											"watchDirectories");
	private final BooleanProperty								initialScan							= new SimpleBooleanProperty(this, "initialScan");

	// Parsing
	private final StringProperty								filenamePatterns					= new SimpleStringProperty(this,
																											"filenamePatterns");
	private final ListProperty<ParsingServiceSettingEntry>		filenameParsingServices				= new SimpleListProperty<>(this,
																											"filenameParsingServices");

	// Metadata
	// Metadata - Release
	private final ListProperty<ParsingServiceSettingEntry>		releaseParsingServices				= new SimpleListProperty<>(this,
																											"releaseParsingServices");
	private final ListProperty<Tag>								releaseMetaTags						= new SimpleListProperty<>(this,
																											"releaseMetaTags");
	// Metadata - Release - Databases
	private final ListProperty<MetadataDbSettingEntry<Release>>	releaseDbs							= new SimpleListProperty<>(this, "releaseDbs");
	// Metadata - Release - Guessing
	private final BooleanProperty								guessingEnabled						= new SimpleBooleanProperty(this,
																											"guessingEnabled");

	private final ListProperty<StandardRelease>					standardReleases					= new SimpleListProperty<>(this,
																											"standardReleases");
	// Metadata - Release - Compatibility
	private final BooleanProperty								compatibilityEnabled				= new SimpleBooleanProperty(this,
																											"compatibilityEnabled");
	private final ListProperty<CompatibilitySettingEntry>		compatibilities						= new SimpleListProperty<>(this,
																											"compatibilities");

	// Standardizing
	private final ListProperty<StandardizerSettingEntry<?, ?>>	preMetadataDbStandardizers			= new SimpleListProperty<>(this,
																											"preMetadataDbStandardizers");
	private final ListProperty<StandardizerSettingEntry<?, ?>>	postMetadataStandardizers			= new SimpleListProperty<>(this,
																											"postMetadataStandardizers");
	// Standardizing - Subtitle language
	private final LocaleLanguageReplacerSettings				subtitleLanguageSettings			= new LocaleLanguageReplacerSettings();
	private final Binding<LocaleSubtitleLanguageStandardizer>	subtitleLanguageStandardizerBinding	= initSubtitleLanguageStandardizerBinding();

	// Naming
	private final MapProperty<String, Object>					namingParameters					= new SimpleMapProperty<>(this,
																											"namingParameters");

	// File Transformation
	// File Transformation - General
	private final Property<Path>								targetDir							= new SimpleObjectProperty<>(this,
																											"targetDir",
																											null);
	private final BooleanProperty								deleteSource						= new SimpleBooleanProperty(this, "deleteSource");
	// File Transformation - Packing
	private final BooleanProperty								packingEnabled						= new SimpleBooleanProperty(this,
																											"packingEnabled");
	private final Property<Path>								rarExe								= new SimpleObjectProperty<>(this, "rarExe", null);
	private final BooleanProperty								autoLocateWinRar					= new SimpleBooleanProperty(this,
																											"autoLocateWinRar");
	private final Property<DeletionMode>						packingSourceDeletionMode			= new SimpleObjectProperty<>(this,
																											"packingSourceDeletionMode",
																											DeletionMode.DELETE);

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
	}

	private Binding<LocaleSubtitleLanguageStandardizer> initSubtitleLanguageStandardizerBinding()
	{
		return new ObjectBinding<LocaleSubtitleLanguageStandardizer>()
		{
			{
				super.bind(subtitleLanguageSettings);
			}

			@Override
			protected LocaleSubtitleLanguageStandardizer computeValue()
			{
				Map<Locale, String> langTextMappings = new HashMap<>(subtitleLanguageSettings.getCustomLanguageTextMappings().size());
				for (LanguageTextMapping mapping : subtitleLanguageSettings.getCustomLanguageTextMappings())
				{
					langTextMappings.put(mapping.getLanguage(), mapping.getText());
				}
				List<LanguagePattern> langPatterns = new ArrayList<>(subtitleLanguageSettings.getCustomLanguagePatterns().size());
				for (LanguageUiPattern uiPattern : subtitleLanguageSettings.getCustomLanguagePatterns())
				{
					langPatterns.add(uiPattern.toLanguagePattern());
				}
				return new LocaleSubtitleLanguageStandardizer(new LocaleLanguageReplacer(subtitleLanguageSettings.getParsingLanguages(),
						subtitleLanguageSettings.getOutputLanguageFormat(),
						subtitleLanguageSettings.getOutputLanguage(),
						langPatterns,
						langTextMappings));
			}
		};
	}

	public void load(URL file) throws ConfigurationException
	{
		log.info("Loading settings from file {}", file);

		XMLConfiguration cfg = new XMLConfiguration();
		cfg.addEventListener(Event.ANY, (Event event) -> {
			System.out.println(event);
		});

		FileHandler cfgFileHandler = new FileHandler(cfg);
		cfgFileHandler.load(file);

		load(cfg);
	}

	private void load(XMLConfiguration cfg)
	{
		// Watch
		updateWatchDirs(cfg);
		updateInitialScan(cfg);
		updateFilenamePatterns(cfg);

		// FileParsing
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
		subtitleLanguageSettings.load(cfg, "standardizing.subtitleLanguage");

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
		setFilenameParsingServices(getSubtitleAdjustmentParsingServices(cfg, "parsing.parsingServices"));
	}

	private void updateReleaseParsingServices(XMLConfiguration cfg)
	{
		setReleaseParsingServices(getReleaseParsingServices(cfg, "metadata.release.parsingServices"));
	}

	private void updateReleaseMetaTags(XMLConfiguration cfg)
	{
		setReleaseMetaTags(getTags(cfg, "metadata.release.metaTags"));
	}

	private void updateReleaseDbs(XMLConfiguration cfg)
	{
		setReleaseDbs(getReleaseDbs(cfg, "metadata.release.databases"));
	}

	private void updateGuessingEnabled(XMLConfiguration cfg)
	{
		setGuessingEnabled(cfg.getBoolean("metadata.release.guessing[@enabled]", false));
	}

	private void updateStandardReleases(XMLConfiguration cfg)
	{
		setStandardReleases(getStandardReleases(cfg, "metadata.release.guessing.standardReleases"));
	}

	private void updateCompatibilityEnabled(XMLConfiguration cfg)
	{
		setCompatibilityEnabled(cfg.getBoolean("metadata.release.compatibility[@enabled]", false));
	}

	private void updateCompatibilities(XMLConfiguration cfg)
	{
		setCompatibilities(getCompatibilities(cfg, "metadata.release.compatibility.compatibilities"));
	}

	private void updatePreMetadataDbStandardizingRules(XMLConfiguration cfg)
	{
		setPreMetadataDbStandardizingRules(getStandardizers(cfg, "standardizing.preMetadataDb.standardizers"));
	}

	private void updatePostMetadataDbStandardizingRules(XMLConfiguration cfg)
	{
		setPostMetadataDbStandardizingRules(getStandardizers(cfg, "standardizing.postMetadataDb.standardizers"));
	}

	private void updateNamingParameters(XMLConfiguration cfg)
	{
		setNamingParameters(getNamingParameters(cfg, "naming.parameters"));
	}

	private void updateTargetDir(XMLConfiguration cfg)
	{
		setTargetDir(getPath(cfg, "fileTransformation.targetDir"));
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
		setRarExe(getPath(cfg, "fileTransformation.packing.winrar.rarExe"));
	}

	private void updatePackingSourceDeletionMode(XMLConfiguration cfg)
	{
		setPackingSourceDeletionMode(DeletionMode.valueOf(cfg.getString("fileTransformation.packing.winrar.sourceDeletionMode")));
	}

	// Static config getter
	private static Path getPath(Configuration cfg, String key)
	{
		String path = cfg.getString(key);
		if (path.isEmpty())
		{
			return null;
		}
		else
		{
			return Paths.get(path);
		}
	}

	private static ObservableList<ParsingServiceSettingEntry> getSubtitleAdjustmentParsingServices(Configuration cfg, String key)
	{
		ArrayList<ParsingServiceSettingEntry> services = new ArrayList<>(4);
		List<String> domains = cfg.getList(String.class, key + ".parsingService");
		boolean addic7edEnabled = domains.contains(Addic7edCom.DOMAIN);
		services.add(new ParsingServiceSettingEntry(Addic7edCom.getParsingService(), addic7edEnabled));
		boolean italianSubsEnabled = domains.contains(ItalianSubsNet.DOMAIN);
		services.add(new ParsingServiceSettingEntry(ItalianSubsNet.getParsingService(), italianSubsEnabled));
		boolean subCentralDeEnabled = domains.contains(SubCentralDe.DOMAIN);
		services.add(new ParsingServiceSettingEntry(SubCentralDe.getParsingService(), subCentralDeEnabled));
		services.trimToSize();
		return FXCollections.observableList(services);
	}

	private static ObservableList<ParsingServiceSettingEntry> getReleaseParsingServices(Configuration cfg, String key)
	{
		ArrayList<ParsingServiceSettingEntry> services = new ArrayList<>(1);
		List<String> domains = cfg.getList(String.class, key + ".parsingService");
		boolean sceneEnabled = domains.contains(ReleaseScene.DOMAIN);
		services.add(new ParsingServiceSettingEntry(ReleaseScene.getParsingService(), sceneEnabled));
		services.trimToSize();
		return FXCollections.observableList(services);
	}

	private static ObservableList<StandardizerSettingEntry<?, ?>> getStandardizers(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<StandardizerSettingEntry<?, ?>> stdzers = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameStandardizer");
		for (HierarchicalConfiguration<ImmutableNode> seriesStdzerCfg : seriesStdzerCfgs)
		{
			String namePatternStr = seriesStdzerCfg.getString("[@namePattern]");
			Mode namePatternMode = Mode.valueOf(seriesStdzerCfg.getString("[@namePatternMode]"));
			UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
			String nameReplacement = seriesStdzerCfg.getString("[@nameReplacement]");
			stdzers.add(new SeriesNameStandardizerSettingEntry(nameUiPattern, nameReplacement, true));
		}
		List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsStandardizer");
		for (HierarchicalConfiguration<ImmutableNode> rlsTagsStdzerCfg : rlsTagsStdzerCfgs)
		{
			List<Tag> tagsToReplace = Tag.parseList(rlsTagsStdzerCfg.getString("[@queryTags]"));
			List<Tag> replacement = Tag.parseList(rlsTagsStdzerCfg.getString("[@replacement]"));
			QueryMode queryMode = de.subcentral.core.metadata.release.TagUtil.QueryMode.valueOf(rlsTagsStdzerCfg.getString("[@queryMode]"));
			ReplaceMode replaceMode = de.subcentral.core.metadata.release.TagUtil.ReplaceMode.valueOf(rlsTagsStdzerCfg.getString("[@replaceMode]"));
			boolean ignoreOrder = rlsTagsStdzerCfg.getBoolean("[@ignoreOrder]", false);
			ReleaseTagsStandardizer stdzer = new ReleaseTagsStandardizer(new TagsReplacer(tagsToReplace,
					replacement,
					queryMode,
					replaceMode,
					ignoreOrder));
			stdzers.add(new ReleaseTagsStandardizerSettingEntry(stdzer, true));
		}
		stdzers.trimToSize();
		return FXCollections.observableList(stdzers);
	}

	private static ObservableList<MetadataDbSettingEntry<Release>> getReleaseDbs(Configuration cfg, String key)
	{
		ArrayList<MetadataDbSettingEntry<Release>> rlsInfoDbs = new ArrayList<>(3);
		List<String> domains = cfg.getList(String.class, key + ".metadataDatabase");
		boolean preDbEnabled = domains.contains(PreDbMeReleaseDb.DOMAIN);
		rlsInfoDbs.add(new MetadataDbSettingEntry<>(new PreDbMeReleaseDb(), preDbEnabled));
		boolean xrelToEnabled = domains.contains(XRelToReleaseDb.DOMAIN);
		rlsInfoDbs.add(new MetadataDbSettingEntry<>(new XRelToReleaseDb(), xrelToEnabled));
		boolean orlyDbEnabled = domains.contains(OrlyDbComReleaseDb.DOMAIN);
		rlsInfoDbs.add(new MetadataDbSettingEntry<>(new OrlyDbComReleaseDb(), orlyDbEnabled));
		rlsInfoDbs.trimToSize();
		return FXCollections.observableList(rlsInfoDbs);
	}

	private static ObservableList<Tag> getTags(Configuration cfg, String key)
	{
		ArrayList<Tag> tags = new ArrayList<>();
		for (String tagName : cfg.getList(String.class, key + ".tag"))
		{
			tags.add(new Tag(tagName));
		}
		tags.trimToSize();
		return FXCollections.observableList(tags);
	}

	private static ObservableList<StandardRelease> getStandardReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<StandardRelease> rlss = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".standardRelease");
		for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
		{
			List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
			Group group = Group.parse(rlsCfg.getString("[@group]"));
			AssumeExistence assumeExistence = AssumeExistence.valueOf(rlsCfg.getString("[@assumeExistence]"));
			rlss.add(new StandardRelease(tags, group, assumeExistence));
		}
		rlss.trimToSize();
		return FXCollections.observableList(rlss);
	}

	private static ObservableList<Release> getReleases(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<Release> rlss = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> rlsCfgs = cfg.configurationsAt(key + ".release");
		for (HierarchicalConfiguration<ImmutableNode> rlsCfg : rlsCfgs)
		{
			List<Tag> tags = Tag.parseList(rlsCfg.getString("[@tags]"));
			Group group = Group.parse(rlsCfg.getString("[@group]"));
			rlss.add(new Release(tags, group));
		}
		rlss.trimToSize();
		return FXCollections.observableList(rlss);
	}

	private static ObservableList<CompatibilitySettingEntry> getCompatibilities(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		Set<CompatibilitySettingEntry> compatibilities = new LinkedHashSet<>();
		// read GroupsCompatibilities
		List<HierarchicalConfiguration<ImmutableNode>> groupsCompCfgs = cfg.configurationsAt(key + ".crossGroupCompatibility");
		for (HierarchicalConfiguration<ImmutableNode> groupsCompCfg : groupsCompCfgs)
		{
			Group sourceGroup = Group.parse(groupsCompCfg.getString("[@sourceGroup]"));
			Group compatibleGroup = Group.parse(groupsCompCfg.getString("[@compatibleGroup]"));
			boolean bidirectional = groupsCompCfg.getBoolean("[@bidirectional]", false);
			compatibilities.add(new CompatibilitySettingEntry(new CrossGroupCompatibility(sourceGroup, compatibleGroup, bidirectional), true));
		}
		return FXCollections.observableArrayList(compatibilities);
	}

	private static ObservableMap<String, Object> getNamingParameters(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		Map<String, Object> params = new HashMap<>(2);
		// add default values
		params.put(EpisodeNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.FALSE);
		params.put(ReleaseNamer.PARAM_PREFER_NAME, Boolean.TRUE);
		// read actual values
		List<HierarchicalConfiguration<ImmutableNode>> paramCfgs = cfg.configurationsAt(key + ".param");
		for (HierarchicalConfiguration<ImmutableNode> paramCfg : paramCfgs)
		{
			String paramKey = paramCfg.getString("[@key]");
			boolean paramValue = paramCfg.getBoolean("[@value]");
			params.put(paramKey, paramValue);
		}
		return FXCollections.observableMap(params);
	}

	// Write methods
	public void writeToFile(Path file) throws ConfigurationException, IOException
	{
		XMLConfiguration cfg = new XMLConfiguration();
		// TODO fill in the cfg
		cfg.write(new FileWriter(file.toFile()));
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

	public Binding<LocaleSubtitleLanguageStandardizer> getSubtitleLanguageStandardizerBinding()
	{
		return subtitleLanguageStandardizerBinding;
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

}
