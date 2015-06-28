package de.subcentral.watcher.controller.processing;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.standardizing.StandardizingDefaults;
import de.subcentral.core.standardizing.StandardizingService;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.support.winrar.WinRar.LocateStrategy;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.SettingsUtil;
import de.subcentral.watcher.settings.StandardizerSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class ProcessingController extends AbstractController {
	private static final Logger log = LogManager.getLogger(ProcessingController.class);

	// Controlling properties
	private final MainController mainController;

	// Processing Config
	private final Binding<ProcessingConfig> processingConfig = initProcessingCfgBinding();
	private final NamingService namingService = initNamingService();
	private final NamingService namingServiceForFiltering = initNamingServiceForFiltering();
	private final Map<String, Object> namingParametersForFiltering = initNamingParametersForFiltering();

	private ExecutorService processingExecutor;
	private ProcessingTask processingTask;

	// View properties
	// ProcessingTree
	@FXML
	private TreeTableView<ProcessingItem> processingTreeTable;
	@FXML
	private TreeTableColumn<ProcessingItem, String> nameColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, ObservableList<Path>> filesColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, String> statusColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, Double> progressColumn;
	@FXML
	private TreeTableColumn<ProcessingItem, String> infoColumn;
	// Lower Button bar
	@FXML
	private Button protocolBtn;
	@FXML
	private Button openDirectoryBtn;
	@FXML
	private Button releaseInfoBtn;
	@FXML
	private Button clearBtn;

	public ProcessingController(MainController mainController) {
		this.mainController = Objects.requireNonNull(mainController, "mainController");
	}

	private static Binding<ProcessingConfig> initProcessingCfgBinding() {
		return new ObjectBinding<ProcessingConfig>() {
			{
				super.bind(WatcherSettings.INSTANCE.getProcessingSettings());
			}

			@Override
			protected ProcessingConfig computeValue() {
				final ProcessingConfig cfg = new ProcessingConfig();
				FxUtil.runAndWait(() -> {
					// processingConfig.getValue() has to be executed in JavaFX
					// Application Thread for concurrency reasons
					// (all access to watcher settings has to be in JavaFX
					// Application Thread)
					long start = System.nanoTime();
					log.debug("Rebuilding ProcessingConfig due to changes in the processing settings");
					final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();
					cfg.setFilenamePattern(UserPattern.parseSimplePatterns(settings.getFilenamePatterns()));
					cfg.setFilenameParsingServices(
							SettingsUtil.getValuesOfEnabledSettingEntries(settings.getFilenameParsingServices()));
					cfg.setReleaseDbs(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseDbs()));
					cfg.setReleaseParsingServices(
							SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseParsingServices()));
					cfg.setGuessingEnabled(settings.isGuessingEnabled());
					cfg.setReleaseMetaTags(ImmutableList.copyOf(settings.getReleaseMetaTags()));
					cfg.setStandardReleases(ImmutableList.copyOf(settings.getStandardReleases()));
					cfg.setCompatibilityEnabled(settings.isCompatibilityEnabled());
					cfg.setCompatibilityService(createCompatibilityService(settings));
					cfg.setPreMetadataDbStandardizingService(createPreMetadataDbStandardizingService(settings));
					cfg.setPostMetadataDbStandardizingService(createPostMetadataDbStandardizingService(settings));
					cfg.setNamingParameters(ImmutableMap.copyOf(settings.getNamingParameters()));
					cfg.setTargetDir(settings.getTargetDir());
					cfg.setDeleteSource(settings.isDeleteSource());
					cfg.setPackingEnabled(settings.isPackingEnabled());
					cfg.setWinRarLocateStrategy(settings.getWinRarLocateStrategy());
					cfg.setRarExe(settings.getRarExe());
					cfg.setPackingSourceDeletionMode(settings.getPackingSourceDeletionMode());
					log.debug("Rebuit ProcessingConfig in {} ms", TimeUtil.durationMillis(start));
				});
				return cfg;
			}

			@Override
			protected void onInvalidating() {
				log.debug(
						"Processing settings changed. ProcessingConfig will be rebuilt on next execution of ProcessingTask");
			}
		};
	}

	private static CompatibilityService createCompatibilityService(ProcessingSettings settings) {
		CompatibilityService service = new CompatibilityService();
		service.getCompatibilities().add(new SameGroupCompatibility());
		WatcherFxUtil.bindCompatibilities(service,
				WatcherSettings.INSTANCE.getProcessingSettings().getCompatibilities());
		return service;
	}

	private static TypeStandardizingService createPreMetadataDbStandardizingService(ProcessingSettings settings) {
		TypeStandardizingService service = new TypeStandardizingService("premetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (StandardizerSettingEntry<?, ?> entry : settings.getPreMetadataDbStandardizers()) {
			WatcherFxUtil.registerStandardizer(service, entry);
		}
		// add subtitle language standardizer
		service.registerStandardizer(Subtitle.class,
				settings.getSubtitleLanguageSettings().getSubtitleLanguageStandardizer());
		// add subtitle tags standardizer
		service.registerStandardizer(SubtitleAdjustment.class, SubtitleUtil::standardizeTags);
		return service;
	}

	private static TypeStandardizingService createPostMetadataDbStandardizingService(ProcessingSettings settings) {
		TypeStandardizingService service = new TypeStandardizingService("postmetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (StandardizerSettingEntry<?, ?> entry : settings.getPreMetadataDbStandardizers()) {
			WatcherFxUtil.registerStandardizer(service, entry);
		}
		return service;
	}

	private static NamingService initNamingService() {
		return NamingDefaults.getDefaultNamingService();
	}

	private static NamingService initNamingServiceForFiltering() {
		return NamingDefaults.getDefaultNormalizingNamingService();
	}

	private static Map<String, Object> initNamingParametersForFiltering() {
		return ImmutableMap.of();
	}

	@Override
	public void doInitialize() {
		initProcessingTreeTable();
		initLowerButtonBar();
	}

	private void initProcessingTreeTable() {
		// init root
		processingTreeTable.setRoot(new TreeItem<>());

		// init columns
		nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features
				.getValue().getValue().nameBinding());
		filesColumn.setCellFactory((TreeTableColumn<ProcessingItem, ObservableList<Path>> param) -> {
			return new TreeTableCell<ProcessingItem, ObservableList<Path>>() {
				@Override
				protected void updateItem(ObservableList<Path> item, boolean empty) {
					super.updateItem(item, empty);

					if (empty || item == null) {
						setText("");
					} else {
						StringJoiner joiner = new StringJoiner(", ");
						for (Path path : item) {
							joiner.add(IOUtil.splitIntoFilenameAndExtension(path.getFileName().toString())[1]);
						}
						setText(joiner.toString().replace(".", "").toUpperCase());
					}
				};
			};
		});
		filesColumn.setCellValueFactory(
				(TreeTableColumn.CellDataFeatures<ProcessingItem, ObservableList<Path>> features) -> features.getValue()
						.getValue().getFiles());

		statusColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features
				.getValue().getValue().statusProperty());
		progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
		progressColumn
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, Double> features) -> features
						.getValue().getValue().progressProperty().asObject());
		infoColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features
				.getValue().getValue().infoBinding());

		initProcessingTreeTableDnD();
	}

	/**
	 * Set up handling of Drag & Drop events (drop events on processing tree table).
	 */
	private void initProcessingTreeTableDnD() {
		final Border defaultBorder = processingTreeTable.getBorder();
		final Border dragBorder = new Border(
				new BorderStroke(Color.CORNFLOWERBLUE, BorderStrokeStyle.SOLID, null, null));

		// Handling a DRAG_OVER Event on a Target
		processingTreeTable.setOnDragOver((DragEvent event) -> {
			/* data is dragged over the target */
			/*
			 * accept it only if it is not dragged from the same node and if it
			 * has a files data
			 */
			if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasFiles()) {
				/*
				 * allow for both copying and moving, whatever user chooses
				 */
				event.acceptTransferModes(TransferMode.COPY);
			}

			event.consume();
		});

		// Providing Visual Feedback by a Gesture Target
		processingTreeTable.setOnDragEntered((DragEvent event) -> {
			Region target = (Region) event.getTarget();
			/* the drag-and-drop gesture entered the target */
			/* show to the user that it is an actual gesture target */
			if (event.getGestureSource() != target && event.getDragboard().hasFiles()) {
				target.setBorder(dragBorder);
			}

			event.consume();
		});
		processingTreeTable.setOnDragExited((DragEvent event) -> {
			Region target = (Region) event.getTarget();
			/* mouse moved away, remove the graphical cues */
			target.setBorder(defaultBorder);

			event.consume();
		});

		// Handling a DRAG_DROPPED Event on a Target
		processingTreeTable.setOnDragDropped((DragEvent event) -> {
			/* data dropped */
			/* if there is a string data on dragboard, read it and use it */
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles()) {
				handleFiles(db.getFiles());
				success = true;
			}
			/*
			 * let the source know whether the string was successfully
			 * transferred and used
			 */
			event.setDropCompleted(success);

			event.consume();
		});
	}

	private void initLowerButtonBar() {
		protocolBtn.disableProperty().bind(processingTreeTable.getSelectionModel().selectedItemProperty().isNull());
		protocolBtn.setOnAction(
				evt -> System.out.println(processingTreeTable.getSelectionModel().getSelectedItem().getValue()));

		openDirectoryBtn.disableProperty().bind(new BooleanBinding() {
			{
				super.bind(processingTreeTable.getSelectionModel().selectedItemProperty());
			}

			@Override
			protected boolean computeValue() {
				TreeItem<ProcessingItem> selectedItem = processingTreeTable.getSelectionModel().getSelectedItem();
				return selectedItem == null || selectedItem.getValue().getFiles().isEmpty();
			}
		});
		openDirectoryBtn.setOnAction(evt -> FxUtil.browse(processingTreeTable.getSelectionModel().getSelectedItem()
				.getValue().getFiles().get(0).getParent().toUri().toString(), mainController.getCommonExecutor()));

		releaseInfoBtn.disableProperty().bind(new BooleanBinding() {
			{
				super.bind(processingTreeTable.getSelectionModel().selectedItemProperty());
			}

			@Override
			protected boolean computeValue() {
				TreeItem<ProcessingItem> selectedItem = processingTreeTable.getSelectionModel().getSelectedItem();
				if (selectedItem != null && selectedItem.getValue() instanceof SubtitleTargetProcessingItem) {
					SubtitleTargetProcessingItem subTargetItem = (SubtitleTargetProcessingItem) selectedItem.getValue();
					return subTargetItem.getRelease().getFurtherInfoLinks().isEmpty();
				}
				return true;
			}
		});
		releaseInfoBtn.setOnAction(evt -> {
			SubtitleTargetProcessingItem item = (SubtitleTargetProcessingItem) processingTreeTable.getSelectionModel()
					.getSelectedItem().getValue();
			FxUtil.browse(item.getRelease().getFurtherInfoLinks().get(0), mainController.getCommonExecutor());
		});

		clearBtn.disableProperty().bind(Bindings.size(processingTreeTable.getRoot().getChildren()).isEqualTo(0));
		clearBtn.setOnAction(evt -> {
			cancelAllTasks();
			processingTreeTable.getRoot().getChildren().clear();
		});
	}

	// getter
	public MainController getMainController() {
		return mainController;
	}

	public NamingService getNamingService() {
		return namingService;
	}

	public NamingService getNamingServiceForFiltering() {
		return namingServiceForFiltering;
	}

	public Map<String, Object> getNamingParametersForFiltering() {
		return namingParametersForFiltering;
	}

	public TreeTableView<ProcessingItem> getProcessingTreeTable() {
		return processingTreeTable;
	}

	// other public methods
	public void handleFiles(Path watchDir, Collection<Path> files) {
		log.info("Handling {} file(s) in {}", files.size(), watchDir);
		for (Path file : files) {
			handleFile(watchDir.resolve(file));
		}
	}

	public void handleFiles(Collection<File> files) {
		log.info("Handling {} file(s)", files.size());
		for (File file : files) {
			handleFile(file.toPath());
		}
	}

	public void handleFile(Path file) {
		Platform.runLater(() -> {
			if (processingTask != null && processingTask.isRunning() && processingTask.getSourceFile().equals(file)) {
				log.warn("Rejected {} because that file is already processed at the moment", file);
				return;
			}

			if (processingExecutor == null || processingExecutor.isShutdown()) {
				processingExecutor = createProcessingExecutor();
			}
			processingTask = new ProcessingTask(file, this);
			processingExecutor.execute(processingTask);
		});
	}

	public void cancelAllTasks() {
		if (processingExecutor != null) {
			processingExecutor.shutdownNow();
		}
	}

	@Override
	public synchronized void shutdown() throws InterruptedException {
		if (processingExecutor != null) {
			processingExecutor.shutdown();
			processingExecutor.awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	private ExecutorService createProcessingExecutor() {
		return Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "Watcher-FileProcessor"));
	}

	// package private
	Binding<ProcessingConfig> getProcessingConfig() {
		return processingConfig;
	}

	// package private
	static class ProcessingConfig {
		// parsing
		private Pattern filenamePattern;
		private ImmutableList<ParsingService> filenameParsingServices;
		// release
		private ImmutableList<Tag> releaseMetaTags;
		// release - dbs
		private ImmutableList<MetadataDb<Release>> releaseDbs;
		private ImmutableList<ParsingService> releaseParsingServices;
		// release - guessing
		private boolean guessingEnabled;
		private ImmutableList<StandardRelease> standardReleases;
		// release - compatibility
		private boolean compatibilityEnabled;
		private CompatibilityService compatibilityService;
		// standardizing
		private StandardizingService preMetadataDbStandardizingService;
		private StandardizingService postMetadataDbStandardizingService;
		// naming
		private ImmutableMap<String, Object> namingParameters;
		// File Transformation - General
		private Path targetDir;
		private boolean deleteSource;
		// File Transformation - Packing
		private boolean packingEnabled;
		private Path rarExe;
		private LocateStrategy winRarLocateStrategy;
		private DeletionMode packingSourceDeletionMode;

		// private
		private ProcessingConfig() {

		}

		Pattern getFilenamePattern() {
			return filenamePattern;
		}

		private void setFilenamePattern(Pattern filenamePattern) {
			this.filenamePattern = filenamePattern;
		}

		ImmutableList<ParsingService> getFilenameParsingServices() {
			return filenameParsingServices;
		}

		private void setFilenameParsingServices(ImmutableList<ParsingService> filenameParsingServices) {
			this.filenameParsingServices = filenameParsingServices;
		}

		ImmutableList<MetadataDb<Release>> getReleaseDbs() {
			return releaseDbs;
		}

		private void setReleaseDbs(ImmutableList<MetadataDb<Release>> releaseDbs) {
			this.releaseDbs = releaseDbs;
		}

		ImmutableList<ParsingService> getReleaseParsingServices() {
			return releaseParsingServices;
		}

		private void setReleaseParsingServices(ImmutableList<ParsingService> releaseParsingServices) {
			this.releaseParsingServices = releaseParsingServices;
		}

		boolean isGuessingEnabled() {
			return guessingEnabled;
		}

		private void setGuessingEnabled(boolean guessingEnabled) {
			this.guessingEnabled = guessingEnabled;
		}

		ImmutableList<Tag> getReleaseMetaTags() {
			return releaseMetaTags;
		}

		private void setReleaseMetaTags(ImmutableList<Tag> releaseMetaTags) {
			this.releaseMetaTags = releaseMetaTags;
		}

		ImmutableList<StandardRelease> getStandardReleases() {
			return standardReleases;
		}

		private void setStandardReleases(ImmutableList<StandardRelease> standardReleases) {
			this.standardReleases = standardReleases;
		}

		boolean isCompatibilityEnabled() {
			return compatibilityEnabled;
		}

		private void setCompatibilityEnabled(boolean compatibilityEnabled) {
			this.compatibilityEnabled = compatibilityEnabled;
		}

		CompatibilityService getCompatibilityService() {
			return compatibilityService;
		}

		private void setCompatibilityService(CompatibilityService compatibilityService) {
			this.compatibilityService = compatibilityService;
		}

		StandardizingService getPreMetadataDbStandardizingService() {
			return preMetadataDbStandardizingService;
		}

		private void setPreMetadataDbStandardizingService(StandardizingService preMetadataDbStandardizingService) {
			this.preMetadataDbStandardizingService = preMetadataDbStandardizingService;
		}

		StandardizingService getPostMetadataDbStandardizingService() {
			return postMetadataDbStandardizingService;
		}

		private void setPostMetadataDbStandardizingService(StandardizingService postMetadataDbStandardizingService) {
			this.postMetadataDbStandardizingService = postMetadataDbStandardizingService;
		}

		ImmutableMap<String, Object> getNamingParameters() {
			return namingParameters;
		}

		private void setNamingParameters(ImmutableMap<String, Object> namingParameters) {
			this.namingParameters = namingParameters;
		}

		Path getTargetDir() {
			return targetDir;
		}

		private void setTargetDir(Path targetDir) {
			this.targetDir = targetDir;
		}

		boolean isDeleteSource() {
			return deleteSource;
		}

		private void setDeleteSource(boolean deleteSource) {
			this.deleteSource = deleteSource;
		}

		boolean isPackingEnabled() {
			return packingEnabled;
		}

		private void setPackingEnabled(boolean packingEnabled) {
			this.packingEnabled = packingEnabled;
		}

		Path getRarExe() {
			return rarExe;
		}

		private void setRarExe(Path rarExe) {
			this.rarExe = rarExe;
		}

		LocateStrategy getWinRarLocateStrategy() {
			return winRarLocateStrategy;
		}

		private void setWinRarLocateStrategy(LocateStrategy winRarLocateStrategy) {
			this.winRarLocateStrategy = winRarLocateStrategy;
		}

		DeletionMode getPackingSourceDeletionMode() {
			return packingSourceDeletionMode;
		}

		private void setPackingSourceDeletionMode(DeletionMode packingSourceDeletionMode) {
			this.packingSourceDeletionMode = packingSourceDeletionMode;
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(ProcessingConfig.class).omitNullValues()
					.add("filenamePattern", filenamePattern).add("filenameParsingServices", filenameParsingServices)
					.add("releaseMetaTags", releaseMetaTags).add("releaseDbs", releaseDbs)
					.add("releaseParsingServices", releaseParsingServices).add("guessingEnabled", guessingEnabled)
					.add("standardReleases", standardReleases).add("compatibilityEnabled", compatibilityEnabled)
					.add("compatibilityService", compatibilityService)
					.add("preMetadataDbStandardizingService", preMetadataDbStandardizingService)
					.add("postMetadataDbStandardizingService", postMetadataDbStandardizingService)
					.add("namingParameters", namingParameters).add("targetDir", targetDir)
					.add("deleteSource", deleteSource).add("packingEnabled", packingEnabled).add("rarExe", rarExe)
					.add("winRarLocateStrategy", winRarLocateStrategy)
					.add("packingSourceDeletionMode", packingSourceDeletionMode).toString();
		}
	}
}
