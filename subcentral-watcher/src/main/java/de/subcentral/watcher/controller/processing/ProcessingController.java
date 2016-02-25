package de.subcentral.watcher.controller.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.correct.CorrectionDefaults;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.NamingService;
import de.subcentral.core.name.PropToStringService;
import de.subcentral.core.parse.MultiParsingService;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.Controller;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.settings.CompatibilitySettingEntry;
import de.subcentral.watcher.settings.CorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.SettingsUtil;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ProcessingController extends Controller
{
	private static final Logger										log							= LogManager.getLogger(ProcessingController.class);

	// Controlling properties
	private final MainController									mainController;

	// Processing Config
	private final Binding<ProcessingConfig>							processingConfig			= initProcessingCfgBinding();
	private final NamingService										namingService				= initNamingService();
	private final NamingService										namingServiceForFiltering	= initNamingServiceForFiltering();
	private final PropToStringService								propToStringService			= initPropToStringService();

	private ExecutorService											processingExecutor;

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
	private TreeTableColumn<ProcessingItem, ProcessingInfo>			infoColumn;
	// Lower Button bar
	@FXML
	private Button													detailsBtn;
	@FXML
	private Button													openDirectoryBtn;
	@FXML
	private Button													reprocessBtn;
	@FXML
	private Button													cancelBtn;
	@FXML
	private Button													removeBtn;
	@FXML
	private Button													cancelAllBtn;
	@FXML
	private Button													removeAllBtn;

	public ProcessingController(MainController mainController)
	{
		this.mainController = Objects.requireNonNull(mainController, "mainController");
	}

	private static Binding<ProcessingConfig> initProcessingCfgBinding()
	{
		return new ObjectBinding<ProcessingConfig>()
		{
			{
				super.bind(WatcherSettings.INSTANCE.getProcessingSettings());
			}

			@Override
			protected ProcessingConfig computeValue()
			{
				final ProcessingConfig cfg = new ProcessingConfig();
				FxUtil.runAndWait(() ->
				{
					// processingConfig.getValue() has to be executed in JavaFX
					// Application Thread for concurrency reasons
					// (all access to watcher settings has to be in JavaFX
					// Application Thread)
					long start = System.nanoTime();
					log.debug("Rebuilding ProcessingConfig due to changes in the processing settings");
					final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();
					cfg.setFilenamePattern(UserPattern.parseSimplePatterns(settings.getFilenamePatterns()));
					cfg.setFilenameParsingService(new MultiParsingService("filename", SettingsUtil.getValuesOfEnabledSettingEntries(settings.getFilenameParsingServices())));
					cfg.setReleaseDbs(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseDbs()));
					cfg.setReleaseParsingService(new MultiParsingService("release", SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseParsingServices())));
					cfg.setGuessingEnabled(settings.isGuessingEnabled());
					cfg.setReleaseMetaTags(ImmutableList.copyOf(settings.getReleaseMetaTags()));
					cfg.setStandardReleases(ImmutableList.copyOf(settings.getStandardReleases()));
					cfg.setCompatibilityEnabled(settings.isCompatibilityEnabled());
					cfg.setCompatibilityService(createCompatibilityService(settings));
					cfg.setBeforeQueryingStandardizingService(createBeforeQueryingStandardizingService(settings));
					cfg.setAfterQueryingStandardizingService(createAfterQueryingStandardizingService(settings));
					cfg.setNamingParameters(ImmutableMap.copyOf(settings.getNamingParameters()));
					cfg.setTargetDir(settings.getTargetDir());
					cfg.setDeleteSource(settings.isDeleteSource());
					cfg.setPackingEnabled(settings.isPackingEnabled());
					cfg.setWinRarLocateStrategy(settings.getWinRarLocateStrategy());
					cfg.setRarExe(settings.getRarExe());
					cfg.setPackingSourceDeletionMode(settings.getPackingSourceDeletionMode());
					log.debug("Rebuilt ProcessingConfig in {} ms", TimeUtil.durationMillis(start));
				});
				return cfg;
			}

			@Override
			protected void onInvalidating()
			{
				log.debug("Processing settings changed. ProcessingConfig will be rebuilt on next execution of ProcessingTask");
			}
		};
	}

	private static CompatibilityService createCompatibilityService(ProcessingSettings settings)
	{
		CompatibilityService service = new CompatibilityService();
		service.getCompatibilities().add(new SameGroupCompatibility());
		for (CompatibilitySettingEntry entry : WatcherSettings.INSTANCE.getProcessingSettings().getCompatibilities())
		{
			if (entry.isEnabled())
			{
				service.getCompatibilities().add(entry.getValue());
			}
		}
		return service;
	}

	private static TypeBasedCorrectionService createBeforeQueryingStandardizingService(ProcessingSettings settings)
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("premetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (CorrectionRuleSettingEntry<?, ?> entry : settings.getCorrectionRules())
		{
			if (entry.isBeforeQuerying())
			{
				registerCorrector(service, entry);
			}
		}
		// add subtitle language standardizer
		service.registerCorrector(Subtitle.class, settings.getSubtitleLanguageCorrectionSettings().getSubtitleLanguageStandardizer());
		// add subtitle tags standardizer
		service.registerCorrector(SubtitleRelease.class, SubtitleUtil::standardizeTags);
		return service;
	}

	private static TypeBasedCorrectionService createAfterQueryingStandardizingService(ProcessingSettings settings)
	{
		TypeBasedCorrectionService service = new TypeBasedCorrectionService("postmetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		CorrectionDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (CorrectionRuleSettingEntry<?, ?> entry : settings.getCorrectionRules())
		{
			if (entry.isAfterQuerying())
			{
				registerCorrector(service, entry);
			}
		}
		return service;
	}

	private static <T> void registerCorrector(TypeBasedCorrectionService service, CorrectionRuleSettingEntry<T, ?> entry)
	{
		service.registerCorrector(entry.getBeanType(), entry.getValue());
	}

	private static NamingService initNamingService()
	{
		return NamingDefaults.getDefaultNamingService();
	}

	private static NamingService initNamingServiceForFiltering()
	{
		return NamingDefaults.getDefaultNormalizingNamingService();
	}

	private static PropToStringService initPropToStringService()
	{
		return NamingDefaults.getDefaultPropToStringService();
	}

	@Override
	protected void initialize()
	{
		initProcessingTreeTable();
		initLowerButtonBar();
	}

	private void initProcessingTreeTable()
	{
		// init root
		processingTreeTable.setRoot(new TreeItem<>());

		// init columns
		nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features.getValue().getValue().nameProperty());

		filesColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, ObservableList<Path>> features) -> features.getValue().getValue().getFiles());
		filesColumn.setCellFactory((TreeTableColumn<ProcessingItem, ObservableList<Path>> param) ->
		{
			return new TreeTableCell<ProcessingItem, ObservableList<Path>>()
			{
				@Override
				protected void updateItem(ObservableList<Path> item, boolean empty)
				{
					super.updateItem(item, empty);

					if (empty || item == null)
					{
						setText("");
					}
					else
					{
						StringJoiner joiner = new StringJoiner(", ");
						for (Path file : item)
						{
							String ext = IOUtil.splitIntoFilenameAndExtension(file.getFileName().toString())[1];
							ext = StringUtils.stripStart(ext, ".");
							joiner.add(ext);
						}
						setText(joiner.toString().toUpperCase());
					}
				};
			};
		});

		statusColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, String> features) -> features.getValue().getValue().statusProperty());

		progressColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, Double> features) -> features.getValue().getValue().progressProperty().asObject());
		progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());

		infoColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<ProcessingItem, ProcessingInfo> features) -> features.getValue().getValue().infoProperty());
		infoColumn.setCellFactory((TreeTableColumn<ProcessingItem, ProcessingInfo> param) ->
		{
			return new TreeTableCell<ProcessingItem, ProcessingInfo>()
			{

				@Override
				protected void updateItem(ProcessingInfo item, boolean empty)
				{
					super.updateItem(item, empty);

					if (empty || item == null)
					{
						setText("");
						setGraphic(null);
						return;
					}

					TreeItem<ProcessingItem> treeItem = getTreeTableRow().getTreeItem();
					if (treeItem == null)
					{
						setText("");
						setGraphic(null);
						return;
					}

					if (item instanceof ProcessingTaskInfo)
					{
						ProcessingTaskInfo taskInfo = (ProcessingTaskInfo) item;
						if (taskInfo.failed())
						{
							setText(taskInfo.getException().toString());
						}
						else
						{
							setText(taskInfo.getFlags().stream().map(this::flagToString).collect(Collectors.joining()));
						}
						setGraphic(null);
					}
					else if (item instanceof ProcessingResultInfo)
					{
						ProcessingResultInfo resultInfo = (ProcessingResultInfo) item;
						ProcessingResult result = (ProcessingResult) treeItem.getValue();

						HBox hbox = new HBox();
						hbox.setSpacing(5d);
						hbox.setAlignment(Pos.CENTER_LEFT);

						// origin info
						switch (resultInfo.getResultType())
						{
							case LISTED:
							{
								addDatabaseHyperlink(result, hbox);
								break;
							}
							case GUESSED:
							{
								Label guessedLbl = WatcherFxUtil.createGuessedLabel(resultInfo.getStandardRelease(), (Release rls) -> result.getTask().generateDisplayName(rls));
								hbox.getChildren().add(guessedLbl);
								break;
							}
							case LISTED_COMPATIBLE:
							{
								// compatible releases may be listed in a database as well
								addDatabaseHyperlink(result, hbox);

								Label compLbl = WatcherFxUtil.createCompatibilityLabel(resultInfo.getCompatibilityInfo(), (Release rls) -> result.getTask().generateDisplayName(rls), true);
								hbox.getChildren().add(compLbl);
								break;
							}
							case GUESSED_COMPATIBLE:
							{
								Label guessedLbl = WatcherFxUtil.createGuessedLabel(resultInfo.getStandardRelease(), (Release rls) -> result.getTask().generateDisplayName(rls));
								hbox.getChildren().add(guessedLbl);

								Label compLbl = WatcherFxUtil.createCompatibilityLabel(resultInfo.getCompatibilityInfo(), (Release rls) -> result.getTask().generateDisplayName(rls), true);
								hbox.getChildren().add(compLbl);
								break;
							}
							default:
								log.warn("Unknown result type: " + resultInfo.getResultType());
								break;
						}

						// nuke
						hbox.getChildren().addAll(WatcherFxUtil.createNukedLabels(result.getRelease()));

						// meta tags
						Label metaTagsLbl = WatcherFxUtil.createMetaTaggedLabel(result.getRelease(), result.getTask().getConfig().getReleaseMetaTags());
						if (metaTagsLbl != null)
						{
							hbox.getChildren().add(metaTagsLbl);
						}

						setText("");
						setGraphic(hbox);
					}
					else
					{
						setText("");
						setGraphic(null);
					}
				}

				private void addDatabaseHyperlink(ProcessingResult result, HBox hbox)
				{
					Hyperlink database = WatcherFxUtil.createFurtherInfoHyperlink(result.getRelease(), result.getTask().getController().getMainController().getCommonExecutor());
					if (database != null)
					{
						hbox.getChildren().add(database);
					}
				}

				private String flagToString(ProcessingTaskInfo.Flag flag)
				{
					switch (flag)
					{
						case DELETED_SOURCE_FILE:
							return "Deleted source file";
						default:
							return flag.toString();
					}
				}

			};
		});

		initProcessingTreeTableDnD();
	}

	/**
	 * Set up handling of Drag & Drop events (drop events on processing tree table).
	 */
	private void initProcessingTreeTableDnD()
	{
		final Border defaultBorder = processingTreeTable.getBorder();
		final Border dragBorder = new Border(new BorderStroke(Color.CORNFLOWERBLUE, BorderStrokeStyle.SOLID, null, null));

		// Handling a DRAG_OVER Event on a Target
		processingTreeTable.setOnDragOver((DragEvent event) ->
		{
			/* data is dragged over the target */
			/*
			 * accept it only if it is not dragged from the same node and if it has a files data
			 */
			if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasFiles())
			{
				/*
				 * allow for both copying and moving, whatever user chooses
				 */
				event.acceptTransferModes(TransferMode.COPY);
			}

			event.consume();
		});

		// Providing Visual Feedback by a Gesture Target
		processingTreeTable.setOnDragEntered((DragEvent event) ->
		{
			Region target = (Region) event.getTarget();
			/* the drag-and-drop gesture entered the target */
			/* show to the user that it is an actual gesture target */
			if (event.getGestureSource() != target && event.getDragboard().hasFiles())
			{
				target.setBorder(dragBorder);
			}

			event.consume();
		});
		processingTreeTable.setOnDragExited((DragEvent event) ->
		{
			Region target = (Region) event.getTarget();
			/* mouse moved away, remove the graphical cues */
			target.setBorder(defaultBorder);

			event.consume();
		});

		// Handling a DRAG_DROPPED Event on a Target
		processingTreeTable.setOnDragDropped((DragEvent event) ->
		{
			/* data dropped */
			/* if there is a string data on dragboard, read it and use it */
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles())
			{
				handleDroppedFiles(db.getFiles());
				success = true;
			}
			/*
			 * let the source know whether the string was successfully transferred and used
			 */
			event.setDropCompleted(success);

			event.consume();
		});
	}

	private void initLowerButtonBar()
	{
		final BooleanBinding noItemSelectedBinding = processingTreeTable.getSelectionModel().selectedItemProperty().isNull();

		Observable stateOfSelectedItemObservable = FxUtil.observeBean(processingTreeTable.getSelectionModel().selectedItemProperty(), (TreeItem<ProcessingItem> treeItem) ->
		{
			ProcessingTask task = getProcessingTask(treeItem, true);
			if (task == null)
			{
				return new Observable[] {};
			}
			return new Observable[] { task.stateProperty() };
		});

		final BooleanBinding noFinishedTaskSelectedBinding = new BooleanBinding()
		{
			{
				super.bind(stateOfSelectedItemObservable);
			}

			@Override
			protected boolean computeValue()
			{
				ProcessingTask task = getSelectedProcessingTask(true);
				if (task == null)
				{
					return true;
				}
				return State.SUCCEEDED != task.getState() && State.CANCELLED != task.getState() && State.FAILED != task.getState();
			}
		};

		final BooleanBinding noUnfinishedTaskSelectedBinding = new BooleanBinding()
		{
			{
				super.bind(stateOfSelectedItemObservable);
			}

			@Override
			protected boolean computeValue()
			{
				ProcessingTask task = getSelectedProcessingTask(true);
				if (task == null)
				{
					return true;
				}
				return State.SUCCEEDED == task.getState() || State.CANCELLED == task.getState() || State.FAILED == task.getState();
			}
		};

		// Only enabled if a ProcessingTask or ProcessingResult is selected which has finished
		detailsBtn.disableProperty().bind(noFinishedTaskSelectedBinding);
		detailsBtn.setOnAction((ActionEvent evt) ->
		{
			ProcessingTask task = getSelectedProcessingTask(true);
			if (task != null)
			{
				showDetails(task);
			}
		});

		openDirectoryBtn.disableProperty().bind(new BooleanBinding()
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
		openDirectoryBtn.setOnAction((ActionEvent evt) ->
		{
			ProcessingItem item = getSelectedProcessingItem();
			if (item != null)
			{
				openDirectory(item);
			}
		});

		reprocessBtn.disableProperty().bind(noFinishedTaskSelectedBinding);
		reprocessBtn.setOnAction((ActionEvent evt) ->
		{
			ProcessingTask task = getSelectedProcessingTask(true);
			if (task != null)
			{
				reprocess(task);
			}
		});

		cancelBtn.disableProperty().bind(noUnfinishedTaskSelectedBinding);
		cancelBtn.setOnAction((ActionEvent evt) ->
		{
			cancelSelectedTask();
		});

		removeBtn.disableProperty().bind(noItemSelectedBinding);
		removeBtn.setOnAction((ActionEvent evt) ->
		{
			removeSelectedTask();
		});

		BooleanBinding emptyProcessingTreeTable = Bindings.size(processingTreeTable.getRoot().getChildren()).isEqualTo(0);
		cancelAllBtn.disableProperty().bind(emptyProcessingTreeTable);
		cancelAllBtn.setOnAction(evt -> cancelAllTasks());
		removeAllBtn.disableProperty().bind(emptyProcessingTreeTable);
		removeAllBtn.setOnAction(evt -> clearProcessingTreeTable());
	}

	// getter
	public MainController getMainController()
	{
		return mainController;
	}

	public NamingService getNamingService()
	{
		return namingService;
	}

	public NamingService getNamingServicesForFiltering()
	{
		return namingServiceForFiltering;
	}

	public PropToStringService getPropToStringService()
	{
		return propToStringService;
	}

	public TreeTableView<ProcessingItem> getProcessingTreeTable()
	{
		return processingTreeTable;
	}

	private ExecutorService getProcessingExecutor()
	{
		if (processingExecutor == null || processingExecutor.isShutdown())
		{
			processingExecutor = Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "Watcher-File-Processor"));
		}
		return processingExecutor;
	}

	// Controlling methods
	public void handleFilesFromWatchDir(Path watchDir, Collection<Path> files)
	{
		log.debug("Handling {} file(s) from watch directory {}", files.size(), watchDir);
		handleFiles(files.stream().map((Path relativeFile) -> watchDir.resolve(relativeFile)));
	}

	public void handleDroppedFiles(Collection<File> files)
	{
		log.debug("Handling {} file(s) from Drag-And-Drop", files.size());
		handleFiles(files.stream().map((File file) -> file.toPath()));
	}

	private void handleFiles(Stream<Path> files)
	{
		// Filtering based on file attributes is done in the thread which ever called this method (IO can take some time)
		final List<Path> filteredWithFileAttributes = files.filter(ProcessingController::filterByFileAttributes).collect(Collectors.toList());

		// Filtering based on the current settings has to be done in the JavaFX Application Thread
		Platform.runLater(() ->
		{
			filteredWithFileAttributes.stream().filter(this::filterByName).filter(this::filterOutAlreadyProcessedFiles).forEach(this::submitNewTask);
		});
	}

	private static boolean filterByFileAttributes(Path file)
	{
		try
		{
			BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			if (!attrs.isRegularFile())
			{
				log.debug("Rejecting {} because it is no regular file", file);
				return false;
			}
			if (attrs.size() == 0)
			{
				log.debug("Rejecting {} because it is empty", file);
				return false;
			}
			if (IOUtil.isLocked(file))
			{
				log.debug("Rejecting {} because it is currently locked", file);
				return false;
			}
		}
		catch (IOException e)
		{
			log.debug("Rejecting " + file + " because its attributes could not be read: {}", e.toString());
			return false;
		}

		return true;
	}

	private boolean filterByName(Path file)
	{
		Pattern pattern = UserPattern.parseSimplePatterns(WatcherSettings.INSTANCE.getProcessingSettings().getFilenamePatterns());
		if (pattern == null)
		{
			log.debug("Rejecting {} because no pattern is specified", file);
			return false;
		}
		if (!pattern.matcher(file.getFileName().toString()).matches())
		{
			log.debug("Rejecting {} because its name does not match the required pattern {}", file, pattern);
			return false;
		}
		return true;
	}

	private void submitNewTask(Path file)
	{
		TreeItem<ProcessingItem> taskItem = new TreeItem<>();
		ProcessingTask newTask = new ProcessingTask(file, this, taskItem);
		taskItem.setValue(newTask);
		processingTreeTable.getRoot().getChildren().add(taskItem);

		getProcessingExecutor().submit(newTask);
	}

	private boolean filterOutAlreadyProcessedFiles(Path file)
	{
		for (TreeItem<ProcessingItem> sourceTreeItem : processingTreeTable.getRoot().getChildren())
		{
			ProcessingTask task = (ProcessingTask) sourceTreeItem.getValue();
			if (task.getSourceFile().equals(file))
			{
				if (WatcherSettings.INSTANCE.isRejectAlreadyProcessedFiles())
				{
					log.info("Rejecting {} because that file is already present in the processing list and 'rejectAlreadyProcessedFiles' is enabled", file);
					return false;
				}
				if ((task.getState() == State.READY || task.getState() == State.SCHEDULED || task.getState() == State.RUNNING))
				{
					log.info("Rejecting {} because that file is already currently processed", file);
					return false;
				}
			}
		}
		return true;
	}

	public void showDetails(ProcessingTask task)
	{
		try
		{
			DetailsController detailsCtrl = new DetailsController(this, task);

			Parent root = FxUtil.loadFromFxml("DetailsView.fxml", null, null, detailsCtrl);
			Scene scene = new Scene(root);

			Stage owner = mainController.getPrimaryStage();
			Stage stage = new Stage();
			stage.initOwner(owner);
			stage.getIcons().add(FxUtil.loadImg("file_search_16.png"));
			stage.setTitle("Processing details");
			stage.setScene(scene);

			stage.show();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void openDirectory(ProcessingItem item)
	{
		List<Path> files = item.getFiles();
		if (!files.isEmpty())
		{
			FxUtil.browse(files.get(0).getParent().toUri().toString(), mainController.getCommonExecutor());
		}
	}

	public TreeItem<ProcessingItem> getSelectedProcessingTreeItem()
	{
		return processingTreeTable.getSelectionModel().getSelectedItem();
	}

	public TreeItem<ProcessingItem> getSelectedProcessingTaskTreeItem(boolean allowIndirect)
	{
		TreeItem<ProcessingItem> treeItem = processingTreeTable.getSelectionModel().getSelectedItem();
		if (treeItem == null)
		{
			return null;
		}
		if (treeItem.getValue() instanceof ProcessingTask)
		{
			return treeItem;
		}
		else if (allowIndirect && treeItem.getValue() instanceof ProcessingResult)
		{
			return treeItem.getParent();
		}
		return null;
	}

	public ProcessingItem getSelectedProcessingItem()
	{
		TreeItem<ProcessingItem> selectedTreeItem = processingTreeTable.getSelectionModel().getSelectedItem();
		return selectedTreeItem != null ? selectedTreeItem.getValue() : null;
	}

	public ProcessingTask getSelectedProcessingTask(boolean allowIndirect)
	{
		return getProcessingTask(processingTreeTable.getSelectionModel().getSelectedItem(), allowIndirect);
	}

	private static ProcessingTask getProcessingTask(TreeItem<ProcessingItem> treeItem, boolean allowIndirect)
	{
		if (treeItem == null)
		{
			return null;
		}
		if (treeItem.getValue() instanceof ProcessingTask)
		{
			return (ProcessingTask) treeItem.getValue();
		}
		else if (allowIndirect && treeItem.getValue() instanceof ProcessingResult)
		{
			return ((ProcessingResult) treeItem.getValue()).getTask();
		}
		return null;
	}

	public void cancelSelectedTask()
	{
		ProcessingTask task = getSelectedProcessingTask(true);
		task.cancel(true);
	}

	public void removeSelectedTask()
	{
		cancelSelectedTask();
		TreeItem<ProcessingItem> selectedTreeItem = getSelectedProcessingTaskTreeItem(true);
		processingTreeTable.getRoot().getChildren().remove(selectedTreeItem);
		processingTreeTable.getSelectionModel().clearSelection();
	}

	public void clearProcessingTreeTable()
	{
		cancelAllTasks();

		// TODO Workaround to fix the memory leak:
		// processingTreeTable.getRoot().getChildren().clear() clears the children
		// but some skin class instances keep references to the deleted cells and they are never gc'd.
		// So we need to dump the whole TreeTableView and reload it

		// processingTreeTable.getRoot().getChildren().clear();
		// processingTreeTable.setRoot(new TreeItem<>());
		// processingTreeTable.getSelectionModel().clearSelection();
		try
		{
			mainController.reloadProcessingPane();
		}
		catch (IOException e)
		{
			FxUtil.createExceptionAlert(mainController.getPrimaryStage(), "Failed to reload processing pane", "Exception while reloading processing pane", e).show();
		}
	}

	public void cancelAllTasks()
	{
		for (TreeItem<ProcessingItem> sourceTreeItem : processingTreeTable.getRoot().getChildren())
		{
			ProcessingTask task = (ProcessingTask) sourceTreeItem.getValue();
			task.cancel(true);
		}
	}

	@Override
	public void shutdown() throws InterruptedException
	{
		if (processingExecutor != null)
		{
			processingExecutor.shutdownNow();
			processingExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	public void reprocess(ProcessingTask task)
	{
		// Cancel the current task
		task.cancel(true);
		TreeItem<ProcessingItem> treeItem = task.getTaskTreeItem();

		// Delete files on background thread and then create and execute new task
		Task<Void> reprocessingTask = new Task<Void>()
		{
			@Override
			protected Void call() throws IOException
			{
				task.deleteResultFiles();
				return null;
			}

			@Override
			protected void succeeded()
			{
				treeItem.getChildren().clear();
				ProcessingTask newTask = new ProcessingTask(task.getSourceFile(), ProcessingController.this, treeItem);
				treeItem.setValue(newTask);

				// TODO hack so that the new TreeItem value is observed for the state of its ProcessingT
				// -> solution: use service (can be restarted)
				processingTreeTable.getSelectionModel().clearSelection();
				processingTreeTable.getSelectionModel().select(treeItem);

				getProcessingExecutor().execute(newTask);
			}

			@Override
			protected void failed()
			{
				log.error("Deletion of target files of task " + task + " failed", getException());
			}
		};
		getProcessingExecutor().execute(reprocessingTask);
	}

	// package private
	Binding<ProcessingConfig> getProcessingConfig()
	{
		return processingConfig;
	}
}
