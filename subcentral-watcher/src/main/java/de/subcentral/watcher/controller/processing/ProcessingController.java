package de.subcentral.watcher.controller.processing;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTreeTableCell;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.StandardizingDefaults;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.settings.SettingsUtil;
import de.subcentral.watcher.settings.WatcherSettings;

public class ProcessingController extends AbstractController
{
	private static final Logger										log									= LogManager.getLogger(ProcessingController.class);

	// Controlling properties
	private final MainController									mainController;

	// Processing Config
	private final Binding<ProcessingConfig>							processingConfig					= initProcessingCfg();
	private final TypeStandardizingService							preMetadataDbStandardizingService	= initPreMetadataDbStandardizingService();
	private final CompatibilityService								compatibilityService				= initCompatibilityService();
	private final TypeStandardizingService							postMetadataStandardizingService	= initPostMetadataStandardizingService();
	private final NamingService										namingService						= initNamingService();
	private final NamingService										namingServiceForFiltering			= initNamingServiceForFiltering();
	private final Map<String, Object>								namingParametersForFiltering		= initNamingParametersForFiltering();

	private ExecutorService											processingExecutor;
	private ProcessingTask											processingTask;

	// View properties
	// ProcessingTree
	@FXML
	private TreeTableView<ProcessingItem>							processingTreeTable;
	@FXML
	private TreeTableColumn<ProcessingItem, String>					nameColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, ObservableList<Path>>	filesColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, String>					statusColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, Double>					progressColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, String>					infoColumn;
	// Row Button bar
	@FXML
	private Button													openFolderBtn;
	@FXML
	private Button													openNfoBtn;
	@FXML
	private Button													clearBtn;

	public ProcessingController(MainController mainController)
	{
		this.mainController = Objects.requireNonNull(mainController, "mainController");
	}

	private static Binding<ProcessingConfig> initProcessingCfg()
	{
		return new ObjectBinding<ProcessingConfig>()
		{
			{
				super.bind(WatcherSettings.INSTANCE);
			}

			@Override
			protected ProcessingConfig computeValue()
			{
				final ProcessingConfig cfg = new ProcessingConfig();
				FxUtil.runAndWait(() -> {
					// processingConfig.getValue() has to be executed in JavaFX Application Thread for concurrency reasons
					// (all access to watcher settings has to be in JavaFX Application Thread)
					log.debug("Rebuilding ProcessingConfig due to changes in WatcherSettings");
					WatcherSettings settings = WatcherSettings.INSTANCE;
					cfg.setFilenamePattern(UserPattern.parseSimplePatterns(settings.getFilenamePatterns()));
					cfg.setFilenameParsingServices(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getFilenameParsingServices()));
					cfg.setReleaseDbs(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseDbs()));
					cfg.setReleaseParsingServices(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseParsingServices()));
					cfg.setGuessingEnabled(settings.isGuessingEnabled());
					cfg.setReleaseMetaTags(ImmutableList.copyOf(settings.getReleaseMetaTags()));
					cfg.setStandardReleases(ImmutableList.copyOf(settings.getStandardReleases()));
					cfg.setCompatibilityEnabled(settings.isCompatibilityEnabled());
					cfg.setNamingParameters(ImmutableMap.copyOf(settings.getNamingParameters()));
					cfg.setTargetDir(settings.getTargetDir());
					cfg.setDeleteSource(settings.isDeleteSource());
					cfg.setPackingEnabled(settings.isPackingEnabled());
					cfg.setAutoLocateWinRar(settings.isAutoLocateWinRar());
					cfg.setRarExe(settings.getRarExe());
					cfg.setPackingSourceDeletionMode(settings.getPackingSourceDeletionMode());
				});
				return cfg;
			}

			@Override
			protected void onInvalidating()
			{
				log.debug("WatcherSettings changed. ProcessingConfig will be rebuilt on next execution of ProcessingTask");
			}
		};
	}

	private static TypeStandardizingService initPreMetadataDbStandardizingService()
	{
		TypeStandardizingService service = new TypeStandardizingService("parsed");
		// Register default nested beans retrievers but not default standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		WatcherFxUtil.bindStandardizers(service, WatcherSettings.INSTANCE.getPreMetadataDbStandardizers());
		return service;
	}

	private static CompatibilityService initCompatibilityService()
	{
		CompatibilityService service = new CompatibilityService();
		service.getCompatibilities().add(new SameGroupCompatibility());
		WatcherFxUtil.bindCompatibilities(service, WatcherSettings.INSTANCE.getCompatibilities());
		return service;
	}

	private static TypeStandardizingService initPostMetadataStandardizingService()
	{
		final TypeStandardizingService service = new TypeStandardizingService("custom");
		// Register default nested beans retrievers but not default standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);

		// Bind SubtitleLanguageStandardizer
		Binding<LocaleSubtitleLanguageStandardizer> langStdzerBinding = WatcherSettings.INSTANCE.getSubtitleLanguageStandardizerBinding();
		service.registerStandardizer(Subtitle.class, langStdzerBinding.getValue());
		langStdzerBinding.addListener((ObservableValue<? extends LocaleSubtitleLanguageStandardizer> observable,
				LocaleSubtitleLanguageStandardizer oldValue, LocaleSubtitleLanguageStandardizer newValue) -> {
			if (oldValue != null)
			{
				service.unregisterStandardizer(oldValue);
			}

			if (newValue != null)
			{
				service.registerStandardizer(Subtitle.class, newValue);
			}
		});

		// Bind all other Standardizers
		WatcherFxUtil.bindStandardizers(service, WatcherSettings.INSTANCE.getPostMetadataStandardizers());
		return service;
	}

	private static NamingService initNamingService()
	{
		return NamingDefaults.getDefaultNamingService();
	}

	private static NamingService initNamingServiceForFiltering()
	{
		return NamingDefaults.getDefaultNormalizingNamingService();
	}

	private static Map<String, Object> initNamingParametersForFiltering()
	{
		return ImmutableMap.of();
	}

	public void doInitialize()
	{
		initProcessingTreeTable();

		openFolderBtn.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(processingTreeTable.getSelectionModel().selectedItemProperty());
			}

			@Override
			protected boolean computeValue()
			{
				TreeItem<ProcessingItem> selectedItem = processingTreeTable.getSelectionModel().getSelectedItem();
				return selectedItem == null || selectedItem.getValue().getFiles().isEmpty();
			}
		});
		openFolderBtn.setOnAction(evt -> FxUtil.browse(processingTreeTable.getSelectionModel()
				.getSelectedItem()
				.getValue()
				.getFiles()
				.get(0)
				.getParent()
				.toUri()
				.toString(), mainController.getCommonExecutor()));

		openNfoBtn.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(processingTreeTable.getSelectionModel().selectedItemProperty());
			}

			@Override
			protected boolean computeValue()
			{
				TreeItem<ProcessingItem> selectedItem = processingTreeTable.getSelectionModel().getSelectedItem();
				if (selectedItem != null && selectedItem.getValue() instanceof SubtitleTargetProcessingItem)
				{
					SubtitleTargetProcessingItem subTargetItem = (SubtitleTargetProcessingItem) selectedItem.getValue();
					return subTargetItem.getRelease().getFurtherInfoLinks().isEmpty();
				}
				return true;
			}
		});
		openNfoBtn.setOnAction(evt -> {
			SubtitleTargetProcessingItem item = (SubtitleTargetProcessingItem) processingTreeTable.getSelectionModel().getSelectedItem().getValue();
			FxUtil.browse(item.getRelease().getFurtherInfoLinks().get(0), mainController.getCommonExecutor());
		});

		clearBtn.setOnAction(evt -> {
			processingTreeTable.getRoot().getChildren().clear();
			processingTreeTable.setRoot(new TreeItem<>());
			cancelAllTasks();
		});
	}

	private void initProcessingTreeTable()
	{
		// init root
		processingTreeTable.setRoot(new TreeItem<>());

		// init columns
		nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features.getValue()
				.getValue()
				.nameBinding());
		filesColumn.setCellFactory((TreeTableColumn<ProcessingItem, ObservableList<Path>> param) -> {
			return new TreeTableCell<ProcessingItem, ObservableList<Path>>()
			{
				@Override
				protected void updateItem(ObservableList<Path> item, boolean empty)
				{
					// calling super here is very important - don't skip this!
					super.updateItem(item, empty);

					if (empty || item == null)
					{
						setText("");
					}
					else
					{
						StringJoiner joiner = new StringJoiner(", ");
						for (Path path : item)
						{
							joiner.add(IOUtil.splitIntoFilenameAndExtension(path.getFileName().toString())[1]);
						}
						setText(joiner.toString().replace(".", "").toUpperCase());
					}
				};
			};
		});
		filesColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, ObservableList<Path>> features) -> {
			return new SimpleListProperty<>(features.getValue().getValue().getFiles());
		});

		statusColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features.getValue()
				.getValue()
				.statusProperty());
		progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
		progressColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, Double> features) -> features.getValue()
				.getValue()
				.progressProperty()
				.asObject());
		infoColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features.getValue()
				.getValue()
				.infoBinding());
	}

	// getter
	public MainController getMainController()
	{
		return mainController;
	}

	public TypeStandardizingService getPreMetadataDbStandardizingService()
	{
		return preMetadataDbStandardizingService;
	}

	public CompatibilityService getCompatibilityService()
	{
		return compatibilityService;
	}

	public TypeStandardizingService getPostMetadataStandardizingService()
	{
		return postMetadataStandardizingService;
	}

	public NamingService getNamingService()
	{
		return namingService;
	}

	public NamingService getNamingServiceForFiltering()
	{
		return namingServiceForFiltering;
	}

	public Map<String, Object> getNamingParametersForFiltering()
	{
		return namingParametersForFiltering;
	}

	public TreeTableView<ProcessingItem> getProcessingTreeTable()
	{
		return processingTreeTable;
	}

	// other public methods
	public synchronized void handleFiles(Path watchDir, Collection<Path> files)
	{
		log.info("Handling {} file(s) in {}", files.size(), watchDir);
		for (Path file : files)
		{
			handleFile(watchDir.resolve(file));
		}
	}

	public synchronized void handleFile(Path file)
	{
		if (processingTask != null && processingTask.getSourceFile().equals(file))
		{
			log.warn("Rejected file {} because that file is already processed at the moment");
			return;
		}

		if (processingExecutor == null || processingExecutor.isShutdown())
		{
			processingExecutor = createProcessingExecutor();
		}
		processingTask = new ProcessingTask(file, this);
		processingExecutor.execute(processingTask);
	}

	public synchronized void cancelAllTasks()
	{
		if (processingExecutor != null)
		{
			processingExecutor.shutdownNow();
		}
	}

	@Override
	public synchronized void shutdown() throws InterruptedException
	{
		if (processingExecutor != null)
		{
			processingExecutor.shutdown();
			processingExecutor.awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	private ExecutorService createProcessingExecutor()
	{
		return Executors.newSingleThreadExecutor(new NamedThreadFactory("Watcher-FileProcessor", false));
	}

	// package private
	Binding<ProcessingConfig> getProcessingConfig()
	{
		return processingConfig;
	}

	// package private
	static class ProcessingConfig
	{
		// parsing
		private Pattern								filenamePattern;
		private ImmutableList<ParsingService>		filenameParsingServices;
		// release
		private ImmutableList<Tag>					releaseMetaTags;
		// release - dbs
		private ImmutableList<MetadataDb<Release>>	releaseDbs;
		private ImmutableList<ParsingService>		releaseParsingServices;
		// release - guessing
		private boolean								guessingEnabled;
		private ImmutableList<StandardRelease>		standardReleases;
		// release - compatibility
		private boolean								compatibilityEnabled;
		// naming
		private ImmutableMap<String, Object>		namingParameters;
		// File Transformation - General
		private Path								targetDir;
		private boolean								deleteSource;
		// File Transformation - Packing
		private boolean								packingEnabled;
		private Path								rarExe;
		private boolean								autoLocateWinRar;
		private DeletionMode						packingSourceDeletionMode;

		// private
		private ProcessingConfig()
		{

		}

		Pattern getFilenamePattern()
		{
			return filenamePattern;
		}

		private void setFilenamePattern(Pattern filenamePattern)
		{
			this.filenamePattern = filenamePattern;
		}

		ImmutableList<ParsingService> getFilenameParsingServices()
		{
			return filenameParsingServices;
		}

		private void setFilenameParsingServices(ImmutableList<ParsingService> filenameParsingServices)
		{
			this.filenameParsingServices = filenameParsingServices;
		}

		ImmutableList<MetadataDb<Release>> getReleaseDbs()
		{
			return releaseDbs;
		}

		private void setReleaseDbs(ImmutableList<MetadataDb<Release>> releaseDbs)
		{
			this.releaseDbs = releaseDbs;
		}

		ImmutableList<ParsingService> getReleaseParsingServices()
		{
			return releaseParsingServices;
		}

		private void setReleaseParsingServices(ImmutableList<ParsingService> releaseParsingServices)
		{
			this.releaseParsingServices = releaseParsingServices;
		}

		boolean isGuessingEnabled()
		{
			return guessingEnabled;
		}

		private void setGuessingEnabled(boolean guessingEnabled)
		{
			this.guessingEnabled = guessingEnabled;
		}

		ImmutableList<Tag> getReleaseMetaTags()
		{
			return releaseMetaTags;
		}

		private void setReleaseMetaTags(ImmutableList<Tag> releaseMetaTags)
		{
			this.releaseMetaTags = releaseMetaTags;
		}

		ImmutableList<StandardRelease> getStandardReleases()
		{
			return standardReleases;
		}

		private void setStandardReleases(ImmutableList<StandardRelease> standardReleases)
		{
			this.standardReleases = standardReleases;
		}

		boolean isCompatibilityEnabled()
		{
			return compatibilityEnabled;
		}

		private void setCompatibilityEnabled(boolean compatibilityEnabled)
		{
			this.compatibilityEnabled = compatibilityEnabled;
		}

		ImmutableMap<String, Object> getNamingParameters()
		{
			return namingParameters;
		}

		private void setNamingParameters(ImmutableMap<String, Object> namingParameters)
		{
			this.namingParameters = namingParameters;
		}

		Path getTargetDir()
		{
			return targetDir;
		}

		private void setTargetDir(Path targetDir)
		{
			this.targetDir = targetDir;
		}

		boolean isDeleteSource()
		{
			return deleteSource;
		}

		private void setDeleteSource(boolean deleteSource)
		{
			this.deleteSource = deleteSource;
		}

		boolean isPackingEnabled()
		{
			return packingEnabled;
		}

		private void setPackingEnabled(boolean packingEnabled)
		{
			this.packingEnabled = packingEnabled;
		}

		Path getRarExe()
		{
			return rarExe;
		}

		private void setRarExe(Path rarExe)
		{
			this.rarExe = rarExe;
		}

		boolean isAutoLocateWinRar()
		{
			return autoLocateWinRar;
		}

		public void setAutoLocateWinRar(boolean autoLocateWinRar)
		{
			this.autoLocateWinRar = autoLocateWinRar;
		}

		public DeletionMode getPackingSourceDeletionMode()
		{
			return packingSourceDeletionMode;
		}

		public void setPackingSourceDeletionMode(DeletionMode packingSourceDeletionMode)
		{
			this.packingSourceDeletionMode = packingSourceDeletionMode;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(ProcessingConfig.class)
					.omitNullValues()
					.add("filenamePattern", filenamePattern)
					.add("filenameParsingServices", filenameParsingServices)
					.add("releaseMetaTags", releaseMetaTags)
					.add("releaseDbs", releaseDbs)
					.add("releaseParsingServices", releaseParsingServices)
					.add("guessingEnabled", guessingEnabled)
					.add("standardReleases", standardReleases)
					.add("compatibilityEnabled", compatibilityEnabled)
					.add("namingParameters", namingParameters)
					.add("targetDir", targetDir)
					.add("deleteSource", deleteSource)
					.add("packingEnabled", packingEnabled)
					.add("rarExe", rarExe)
					.add("autoLocateWinRar", autoLocateWinRar)
					.add("packingSourceDeletionMode", packingSourceDeletionMode)
					.toString();
		}
	}
}
