package de.subcentral.watcher.controller.processing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CompatibilityService;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.SameGroupCompatibility;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.metadata.subtitle.SubtitleUtil;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.standardizing.StandardizingDefaults;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.core.util.IOUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.controller.processing.ProcessingResult.CompatibleInfo;
import de.subcentral.watcher.controller.processing.ProcessingResult.GuessedInfo;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.image.ImageView;
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

public class ProcessingController extends AbstractController
{
	private static final Logger log = LogManager.getLogger(ProcessingController.class);

	// Controlling properties
	private final MainController mainController;

	// Processing Config
	private final Binding<ProcessingConfig>	processingConfig				= initProcessingCfgBinding();
	private final NamingService				namingService					= initNamingService();
	private final NamingService				namingServiceForFiltering		= initNamingServiceForFiltering();
	private final Map<String, Object>		namingParametersForFiltering	= initNamingParametersForFiltering();

	private ExecutorService processingExecutor;

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
	private Button													reprocessBtn;
	@FXML
	private Button													openDirectoryBtn;
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
					cfg.setFilenameParsingServices(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getFilenameParsingServices()));
					cfg.setReleaseDbs(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseDbs()));
					cfg.setReleaseParsingServices(SettingsUtil.getValuesOfEnabledSettingEntries(settings.getReleaseParsingServices()));
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

	private static TypeStandardizingService createBeforeQueryingStandardizingService(ProcessingSettings settings)
	{
		TypeStandardizingService service = new TypeStandardizingService("premetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (CorrectionRuleSettingEntry<?, ?> entry : settings.getCorrectionRules())
		{
			if (entry.isBeforeQuerying())
			{
				registerStandardizer(service, entry);
			}
		}
		// add subtitle language standardizer
		service.registerStandardizer(Subtitle.class, settings.getSubtitleLanguageCorrectionSettings().getSubtitleLanguageStandardizer());
		// add subtitle tags standardizer
		service.registerStandardizer(SubtitleAdjustment.class, SubtitleUtil::standardizeTags);
		return service;
	}

	private static TypeStandardizingService createAfterQueryingStandardizingService(ProcessingSettings settings)
	{
		TypeStandardizingService service = new TypeStandardizingService("postmetadatadb");
		// Register default nested beans retrievers but not default
		// standardizers
		StandardizingDefaults.registerAllDefaultNestedBeansRetrievers(service);
		for (CorrectionRuleSettingEntry<?, ?> entry : settings.getCorrectionRules())
		{
			if (entry.isAfterQuerying())
			{
				registerStandardizer(service, entry);
			}
		}
		return service;
	}

	private static <T> void registerStandardizer(TypeStandardizingService service, CorrectionRuleSettingEntry<T, ?> entry)
	{
		service.registerStandardizer(entry.getBeanType(), entry.getValue());
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

	@Override
	public void doInitialize()
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
							joiner.add(IOUtil.splitIntoFilenameAndExtension(file.getFileName().toString())[1]);
						}
						setText(joiner.toString().replace(".", "").toUpperCase());
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
					}
					else
					{
						if (item instanceof ProcessingTaskInfo)
						{
							ProcessingTaskInfo taskInfo = (ProcessingTaskInfo) item;
							setText(taskInfo.getInfo());
							setGraphic(null);
						}
						else if (item instanceof ProcessingResultInfo)
						{
							ProcessingResultInfo resultInfo = (ProcessingResultInfo) item;
							HBox hbox = new HBox();
							hbox.setSpacing(5d);
							hbox.setAlignment(Pos.CENTER_LEFT);

							// origin info
							switch (resultInfo.getOriginInfo().getOrigin())
							{
							case DATABASE:
							{
								addDatabaseHyperlink(resultInfo, hbox);
								break;
							}
							case GUESSED:
							{
								GuessedInfo gi = (GuessedInfo) resultInfo.getOriginInfo();
								StringBuilder sb = new StringBuilder();
								sb.append("Guessed");
								if (gi.getStandardRelease() != null)
								{
									sb.append(" (using standard release: ");
									sb.append(resultInfo.getProcessingResult().getTask().name(gi.getStandardRelease().getRelease()));
									sb.append(')');
								}
								ImageView guessingImg = new ImageView(FxUtil.loadImg("idea_16.png"));
								Label guessing = new Label("", guessingImg);
								guessing.setTooltip(new Tooltip(sb.toString()));
								hbox.getChildren().add(guessing);
								break;
							}
							case COMPATIBLE:
							{
								// compatible releases may be listed in a database as well
								addDatabaseHyperlink(resultInfo, hbox);

								CompatibleInfo ci = (CompatibleInfo) resultInfo.getOriginInfo();
								StringBuilder txt = new StringBuilder();
								Compatibility c = ci.getCompatibilityInfo().getCompatibility();
								if (c instanceof SameGroupCompatibility)
								{
									txt.append("Same group");
								}
								else if (c instanceof CrossGroupCompatibility)
								{
									txt.append(((CrossGroupCompatibility) c).toShortString());
								}

								ImageView compImg = new ImageView(FxUtil.loadImg("couple_16.png"));
								Label comp = new Label(txt.toString(), compImg);

								StringBuilder tooltip = new StringBuilder();
								tooltip.append("Compatible to ");
								tooltip.append(resultInfo.getProcessingResult().getTask().name(ci.getCompatibilityInfo().getSource()));

								comp.setTooltip(new Tooltip(tooltip.toString()));
								hbox.getChildren().add(comp);
								break;
							}
							default:
								log.warn("Unknown method info type: " + resultInfo.getOriginInfo());
								break;
							}

							// nuke
							Label nukedLbl = WatcherFxUtil.createNukedLabel(resultInfo.getProcessingResult().getRelease());
							if (nukedLbl != null)
							{
								hbox.getChildren().add(nukedLbl);
							}

							// meta tags
							Label metaTagsLbl = WatcherFxUtil.createMetaTaggedLabel(resultInfo.getProcessingResult().getRelease(),
									resultInfo.getProcessingResult().getTask().getConfig().getReleaseMetaTags());
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
				}

				private void addDatabaseHyperlink(ProcessingResultInfo resultInfo, HBox hbox)
				{
					Hyperlink database = WatcherFxUtil.createFurtherInfoHyperlink(resultInfo.getProcessingResult().getRelease(),
							resultInfo.getProcessingResult().getTask().getController().getMainController().getCommonExecutor());
					if (database != null)
					{
						database.setTooltip(new Tooltip("Found in release database"));
						hbox.getChildren().add(database);
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
				handleFiles(db.getFiles());
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
		final BooleanBinding finishedTaskSelected = new BooleanBinding()
		{
			{
				Observable stateObsv = FxUtil.observeBean(processingTreeTable.getSelectionModel().selectedItemProperty(), (TreeItem<ProcessingItem> treeItem) ->
				{
					ProcessingTask task = getProcessingTask(treeItem, true);
					if (task == null)
					{
						return new Observable[] {};
					}
					return new Observable[]
					{ task.stateProperty() };
				});
				super.bind(stateObsv);
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

		// Only enabled if a ProcessingTask or ProcessingResult is selected which has finished
		detailsBtn.disableProperty().bind(finishedTaskSelected);
		detailsBtn.setOnAction((ActionEvent evt) ->
		{
			ProcessingTask task = getSelectedProcessingTask(true);
			if (task != null)
			{
				showDetails(task);
			}
		});

		reprocessBtn.disableProperty().bind(finishedTaskSelected);
		reprocessBtn.setOnAction((ActionEvent evt) ->
		{
			ProcessingTask task = getSelectedProcessingTask(true);
			if (task != null)
			{
				reprocess(task);
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

	private ExecutorService getProcessingExecutor()
	{
		if (processingExecutor == null || processingExecutor.isShutdown())
		{
			processingExecutor = Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "Watcher-FileProcessor"));
		}
		return processingExecutor;
	}

	// Controlling methods
	public void handleFiles(Path watchDir, Collection<Path> files)
	{
		log.info("Handling {} file(s) in {}", files.size(), watchDir);
		for (Path file : files)
		{
			handleFile(watchDir.resolve(file));
		}
	}

	public void handleFiles(Collection<File> files)
	{
		log.info("Handling {} file(s)", files.size());
		for (File file : files)
		{
			handleFile(file.toPath());
		}
	}

	public void handleFile(Path file)
	{
		Platform.runLater(() ->
		{
			if (!filter(file))
			{
				return;
			}
			if (alreadyInProcess(file))
			{
				log.info("Rejecting {} because that file is already in processing", file);
				return;
			}

			TreeItem<ProcessingItem> taskItem = new TreeItem<>();
			ProcessingTask newTask = new ProcessingTask(file, this, taskItem);
			taskItem.setValue(newTask);
			processingTreeTable.getRoot().getChildren().add(taskItem);

			getProcessingExecutor().execute(newTask);
		});
	}

	private boolean filter(Path file)
	{
		if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
		{
			log.debug("Filtered out {} because it is no regular file", file);
			return false;
		}

		Pattern pattern = UserPattern.parseSimplePatterns(WatcherSettings.INSTANCE.getProcessingSettings().getFilenamePatterns());
		if (pattern == null)
		{
			log.debug("Filtered out {} because no pattern is specified", file);
			return false;
		}
		if (!pattern.matcher(file.getFileName().toString()).matches())
		{
			log.debug("Filtered out {} because its name does not match the required pattern {}", file, pattern);
			return false;
		}

		return true;
	}

	/**
	 * @return <code>true</code> if file is already queued for or in process. <code>false</code> otherwise
	 */
	private boolean alreadyInProcess(Path file)
	{
		for (TreeItem<ProcessingItem> sourceTreeItem : processingTreeTable.getRoot().getChildren())
		{
			ProcessingTask task = (ProcessingTask) sourceTreeItem.getValue();
			if ((task.getState() == State.READY || task.getState() == State.SCHEDULED || task.getState() == State.RUNNING) && task.getSourceFile().equals(file))
			{
				return true;
			}
		}
		return false;
	}

	public void showDetails(ProcessingTask task)
	{
		try
		{
			DetailsController protocolCtrl = new DetailsController(this, task);

			Parent root = FxUtil.loadFromFxml("DetailsView.fxml", null, null, protocolCtrl);
			Scene scene = new Scene(root);

			Stage owner = mainController.getWatcherApp().getPrimaryStage();
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

	public void clearProcessingTreeTable()
	{
		cancelAllTasks();
		processingTreeTable.getRoot().getChildren().clear();
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
			processingExecutor.awaitTermination(30, TimeUnit.SECONDS);
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
