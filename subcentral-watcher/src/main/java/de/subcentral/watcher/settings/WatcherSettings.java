package de.subcentral.watcher.settings;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
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
import javafx.collections.ObservableMap;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.AssumeExistence;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.standardizing.LocaleLanguageReplacer;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.SeriesNameStandardizer;
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

	/**
	 * Whether the settings changed since initial load
	 */
	private BooleanProperty										changed								= new SimpleBooleanProperty(this,
																											"changed",
																											false);

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

		addListener((Observable o) -> changed.set(true));
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
				for (LanguageUserPattern uiPattern : subtitleLanguageSettings.getCustomLanguagePatterns())
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

	public void load(Path file) throws ConfigurationException
	{
		log.info("Loading settings from file {}", file.toAbsolutePath());

		XMLConfiguration cfg = new XMLConfiguration();
		// cfg.addEventListener(Event.ANY, (Event event) -> {
		// System.out.println(event);
		// });

		FileHandler cfgFileHandler = new FileHandler(cfg);
		cfgFileHandler.load(file.toFile());

		load(cfg);

		changed.set(false);
	}

	public void load(XMLConfiguration cfg)
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
		setFilenameParsingServices(getParsingServices(cfg, "parsing.parsingServices"));
	}

	private void updateReleaseParsingServices(XMLConfiguration cfg)
	{
		setReleaseParsingServices(getParsingServices(cfg, "metadata.release.parsingServices"));
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
		setPackingSourceDeletionMode(DeletionMode.valueOf(cfg.getString("fileTransformation.packing.sourceDeletionMode")));
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

	private static ObservableList<ParsingServiceSettingEntry> getParsingServices(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<ParsingServiceSettingEntry> services = new ArrayList<>(4);
		List<HierarchicalConfiguration<ImmutableNode>> parsingServiceCfgs = cfg.configurationsAt(key + ".parsingService");
		for (HierarchicalConfiguration<ImmutableNode> parsingServiceCfg : parsingServiceCfgs)
		{
			String domain = parsingServiceCfg.getString("");
			boolean enabled = parsingServiceCfg.getBoolean("[@enabled]");
			switch (domain)
			{
				case Addic7edCom.DOMAIN:
					services.add(new ParsingServiceSettingEntry(Addic7edCom.getParsingService(), enabled));
					break;
				case ItalianSubsNet.DOMAIN:
					services.add(new ParsingServiceSettingEntry(ItalianSubsNet.getParsingService(), enabled));
					break;
				case ReleaseScene.DOMAIN:
					services.add(new ParsingServiceSettingEntry(ReleaseScene.getParsingService(), enabled));
					break;
				case SubCentralDe.DOMAIN:
					services.add(new ParsingServiceSettingEntry(SubCentralDe.getParsingService(), enabled));
					break;
				default:
					throw new IllegalArgumentException("Unknown parsing service. domain=" + domain);
			}
		}
		services.trimToSize();
		return FXCollections.observableList(services);
	}

	private static ObservableList<StandardizerSettingEntry<?, ?>> getStandardizers(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<StandardizerSettingEntry<?, ?>> stdzers = new ArrayList<>();
		List<HierarchicalConfiguration<ImmutableNode>> seriesStdzerCfgs = cfg.configurationsAt(key + ".seriesNameStandardizer");
		for (HierarchicalConfiguration<ImmutableNode> seriesStdzerCfg : seriesStdzerCfgs)
		{
			boolean enabled = seriesStdzerCfg.getBoolean("[@enabled]");
			String namePatternStr = seriesStdzerCfg.getString("[@namePattern]");
			Mode namePatternMode = Mode.valueOf(seriesStdzerCfg.getString("[@namePatternMode]"));
			UserPattern nameUiPattern = new UserPattern(namePatternStr, namePatternMode);
			String nameReplacement = seriesStdzerCfg.getString("[@nameReplacement]");
			stdzers.add(new SeriesNameStandardizerSettingEntry(nameUiPattern, nameReplacement, enabled));
		}
		List<HierarchicalConfiguration<ImmutableNode>> rlsTagsStdzerCfgs = cfg.configurationsAt(key + ".releaseTagsStandardizer");
		for (HierarchicalConfiguration<ImmutableNode> rlsTagsStdzerCfg : rlsTagsStdzerCfgs)
		{
			boolean enabled = rlsTagsStdzerCfg.getBoolean("[@enabled]");
			List<Tag> queryTags = Tag.parseList(rlsTagsStdzerCfg.getString("[@queryTags]"));
			List<Tag> replacement = Tag.parseList(rlsTagsStdzerCfg.getString("[@replacement]"));
			QueryMode queryMode = de.subcentral.core.metadata.release.TagUtil.QueryMode.valueOf(rlsTagsStdzerCfg.getString("[@queryMode]"));
			ReplaceMode replaceMode = de.subcentral.core.metadata.release.TagUtil.ReplaceMode.valueOf(rlsTagsStdzerCfg.getString("[@replaceMode]"));
			boolean ignoreOrder = rlsTagsStdzerCfg.getBoolean("[@ignoreOrder]", false);
			ReleaseTagsStandardizer stdzer = new ReleaseTagsStandardizer(new TagsReplacer(queryTags, replacement, queryMode, replaceMode, ignoreOrder));
			stdzers.add(new ReleaseTagsStandardizerSettingEntry(stdzer, enabled));
		}
		stdzers.trimToSize();
		return FXCollections.observableList(stdzers);
	}

	private static ObservableList<MetadataDbSettingEntry<Release>> getReleaseDbs(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		ArrayList<MetadataDbSettingEntry<Release>> dbs = new ArrayList<>(3);
		List<HierarchicalConfiguration<ImmutableNode>> rlsDbCfgs = cfg.configurationsAt(key + ".db");
		for (HierarchicalConfiguration<ImmutableNode> rlsDbCfg : rlsDbCfgs)
		{
			String domain = rlsDbCfg.getString("");
			boolean enabled = rlsDbCfg.getBoolean("[@enabled]");
			switch (domain)
			{
				case OrlyDbComReleaseDb.DOMAIN:
					dbs.add(new MetadataDbSettingEntry<>(new OrlyDbComReleaseDb(), enabled));
					break;
				case PreDbMeReleaseDb.DOMAIN:
					dbs.add(new MetadataDbSettingEntry<>(new PreDbMeReleaseDb(), enabled));
					break;
				case XRelToReleaseDb.DOMAIN:
					dbs.add(new MetadataDbSettingEntry<>(new XRelToReleaseDb(), enabled));
					break;
				default:
					throw new IllegalArgumentException("Unknown metadata database. domain=" + domain);
			}
		}
		dbs.trimToSize();
		return FXCollections.observableList(dbs);
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
			boolean enabled = groupsCompCfg.getBoolean("[@enabled]");
			Group sourceGroup = Group.parse(groupsCompCfg.getString("[@sourceGroup]"));
			Group compatibleGroup = Group.parse(groupsCompCfg.getString("[@compatibleGroup]"));
			boolean symmetric = groupsCompCfg.getBoolean("[@symmetric]", false);
			compatibilities.add(new CompatibilitySettingEntry(new CrossGroupCompatibility(sourceGroup, compatibleGroup, symmetric), enabled));
		}
		return FXCollections.observableArrayList(compatibilities);
	}

	private static ObservableMap<String, Object> getNamingParameters(HierarchicalConfiguration<ImmutableNode> cfg, String key)
	{
		Map<String, Object> params = new LinkedHashMap<>(3);
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
			addPath(cfg, "watch.directories.dir", path);
		}
		cfg.addProperty("watch.initialScan", isInitialScan());

		// FileParsing
		cfg.addProperty("parsing.filenamePatterns", getFilenamePatterns());
		addParsingServices(cfg, "parsing.parsingServices", filenameParsingServices);

		// Metadata
		// Metadata - Release
		addParsingServices(cfg, "metadata.release.parsingServices", releaseParsingServices);
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
			cfg.addProperty("metadata.release.guessing.standardReleases.standardRelease(" + i + ")[@tags]",
					Tag.listToString(stdRls.getRelease().getTags()));
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
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@sourceGroup]",
						cgc.getSourceGroup());
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@compatibleGroup]",
						cgc.getCompatibleGroup());
				cfg.addProperty("metadata.release.compatibility.compatibilities.crossGroupCompatibility(" + i + ")[@symmetric]", cgc.isSymmetric());
			}
			else
			{
				throw new IllegalArgumentException("Unknown compatibility: " + c);
			}
		}

		// Standardizing
		addStandardizer(cfg, "standardizing.preMetadataDb.standardizers", preMetadataDbStandardizers);
		addStandardizer(cfg, "standardizing.postMetadataDb.standardizers", postMetadataStandardizers);

		subtitleLanguageSettings.save(cfg, "standardizing.subtitleLanguage");

		// Naming
		int i = 0;
		for (Map.Entry<String, Object> param : namingParameters.entrySet())
		{
			cfg.addProperty("naming.parameters.param(" + i + ")[@key]", param.getKey());
			cfg.addProperty("naming.parameters.param(" + i + ")[@value]", param.getValue());
			i++;
		}

		// File Transformation - General
		addPath(cfg, "fileTransformation.targetDir", getTargetDir());
		cfg.addProperty("fileTransformation.deleteSource", isDeleteSource());

		// File Transformation - Packing
		cfg.addProperty("fileTransformation.packing[@enabled]", isPackingEnabled());
		cfg.addProperty("fileTransformation.packing.sourceDeletionMode", getPackingSourceDeletionMode());
		cfg.addProperty("fileTransformation.packing.winrar.autoLocate", isAutoLocateWinRar());
		addPath(cfg, "fileTransformation.packing.winrar.rarExe", getRarExe());

		return cfg;
	}

	private static void addParsingServices(XMLConfiguration cfg, String key, List<ParsingServiceSettingEntry> parsingServices)
	{
		for (int i = 0; i < parsingServices.size(); i++)
		{
			ParsingServiceSettingEntry ps = parsingServices.get(i);
			cfg.addProperty(key + ".parsingService(" + i + ")", ps.getValue().getDomain());
			cfg.addProperty(key + ".parsingService(" + i + ")[@enabled]", ps.isEnabled());
		}
	}

	private static void addStandardizer(XMLConfiguration cfg, String key, List<StandardizerSettingEntry<?, ?>> standardizers)
	{
		// one index for each element name
		int seriesNameIndex = 0;
		int releaseTagsIndex = 0;
		for (StandardizerSettingEntry<?, ?> genericEntry : standardizers)
		{
			if (genericEntry instanceof SeriesNameStandardizerSettingEntry)
			{
				SeriesNameStandardizerSettingEntry entry = (SeriesNameStandardizerSettingEntry) genericEntry;
				SeriesNameStandardizer stdzer = entry.getValue();
				UserPattern namePattern = entry.getNameUserPattern();

				cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@enabled]", entry.isEnabled());
				cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@namePattern]", namePattern.getPattern());
				cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@namePatternMode]", namePattern.getMode());
				cfg.addProperty(key + ".seriesNameStandardizer(" + seriesNameIndex + ")[@nameReplacement]", stdzer.getNameReplacement());
				seriesNameIndex++;
			}
			else if (genericEntry instanceof ReleaseTagsStandardizerSettingEntry)
			{
				ReleaseTagsStandardizerSettingEntry entry = (ReleaseTagsStandardizerSettingEntry) genericEntry;
				TagsReplacer replacer = entry.getValue().getReplacer();

				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@enabled]", entry.isEnabled());
				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@queryTags]", Tag.listToString(replacer.getQueryTags()));
				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@replacement]", Tag.listToString(replacer.getReplacement()));
				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@queryMode]", replacer.getQueryMode());
				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@replaceMode]", replacer.getReplaceMode());
				cfg.addProperty(key + ".releaseTagsStandardizer(" + releaseTagsIndex + ")[@ignoreOrder]", replacer.getIgnoreOrder());
				releaseTagsIndex++;
			}
			else
			{
				throw new IllegalArgumentException("Unknown standardizer: " + genericEntry);
			}
		}
	}

	private void addPath(Configuration cfg, String key, Path path)
	{
		// WARNING: Need to use path.toString() because path implements iterable
		// and results in an endless loop when Commons-Configuration tries to print it
		cfg.addProperty(key, path == null ? "" : path.toString());
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
