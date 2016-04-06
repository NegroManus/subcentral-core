package de.subcentral.watcher.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.subcentral.fx.Controller;
import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.settings.SettingsController;
import de.subcentral.watcher.controller.settings.WatchSettingsController;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class WatchController extends Controller
{
	// Controlling properties
	private final MainController	mainController;

	// View
	@FXML
	private Button					startWatchButton;
	@FXML
	private Button					stopWatchButton;
	@FXML
	private Label					watchStatusLabel;
	@FXML
	private HBox					watchDirectoriesHBox;

	private final ImageView			watchImg	= new ImageView(FxUtil.loadImg("iris_16.png"));

	private DirectoryWatchService	watchService;
	private ExecutorService			watchServiceExecutor;

	public WatchController(MainController mainController)
	{
		this.mainController = mainController;
	}

	public MainController getMainController()
	{
		return mainController;
	}

	@Override
	protected void initialize() throws Exception
	{
		initWatchService();

		startWatchButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(watchService.runningProperty(), SettingsController.SETTINGS.watchDirectoriesProperty().emptyProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return watchService.isRunning() || SettingsController.SETTINGS.watchDirectoriesProperty().isEmpty();
			}
		});
		startWatchButton.setOnAction(evt ->
		{
			watchService.restart();
			evt.consume();
		});

		stopWatchButton.disableProperty().bind(watchService.runningProperty().not());
		stopWatchButton.setOnAction(evt ->
		{
			watchService.cancel();
			evt.consume();
		});

		watchStatusLabel.textProperty().bind(new StringBinding()
		{
			{
				super.bind(watchService.stateProperty());
			}

			@Override
			protected String computeValue()
			{
				switch (watchService.getState())
				{
					case READY:
						// fall through
					case SCHEDULED:
						// fall through
					case CANCELLED:
						// fall through
					case SUCCEEDED:
						return "Watch stopped";
					case RUNNING:
						return "Watching";
					case FAILED:
						return "Watch failed";
					default:
						return "";
				}
			}
		});

		watchDirectoriesHBox.getChildren().add(watchImg);
		updateWatchDirsHBox(SettingsController.SETTINGS.getWatchDirectories());
		SettingsController.SETTINGS.watchDirectoriesProperty().addListener(new InvalidationListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void invalidated(Observable o)
			{
				updateWatchDirsHBox((List<Path>) o);
			}
		});

		watchService.setOnFailed((WorkerStateEvent event) ->
		{
			DirectoryWatchService watchService = (DirectoryWatchService) event.getSource();
			Throwable ex = watchService.getException();
			StringJoiner dirsString = new StringJoiner(", ");
			for (Path dir : watchService.getWatchDirectories())
			{
				dirsString.add(dir.toString());
			}
			Alert alert = FxUtil.createExceptionAlert(mainController.getPrimaryStage(), "Watch failed", "Failed to watch " + dirsString + ".", ex);
			alert.show();
		});
	}

	private void updateWatchDirsHBox(List<Path> watchDirs)
	{
		watchDirectoriesHBox.getChildren().retainAll(watchImg);
		if (watchDirs.isEmpty())
		{
			ImageView img = new ImageView(FxUtil.loadImg("settings_16.png"));
			Hyperlink link = new Hyperlink("Add watch directory", img);
			link.setVisited(true);
			link.setOnAction((ActionEvent evt) ->
			{
				// Open settings
				mainController.selectTab(MainController.SETTINGS_TAB_INDEX);
				mainController.getSettingsController().selectSection(SettingsController.WATCH_SECTION);
				WatchSettingsController watchSettingsCtrl = (WatchSettingsController) mainController.getSettingsController().getSections().get(SettingsController.WATCH_SECTION).getController();
				// Open directory chooser
				watchSettingsCtrl.addWatchDirectory();
			});
			watchDirectoriesHBox.getChildren().add(link);
		}
		else
		{
			for (Path dir : watchDirs)
			{
				watchDirectoriesHBox.getChildren().add(FxUtil.createFileHyperlink(dir, mainController.getCommonExecutor()));
			}
		}
	}

	private void initWatchService() throws IOException
	{
		watchServiceExecutor = Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "Watcher-WatchService"));

		watchService = new DirectoryWatchService(this.mainController.getProcessingController()::handleFilesFromWatchDir);
		watchService.setExecutor(watchServiceExecutor);
		WatcherFxUtil.bindWatchDirectories(watchService, SettingsController.SETTINGS.watchDirectoriesProperty());
		watchService.setInitialScan(SettingsController.SETTINGS.isInitialScan());
		SettingsController.SETTINGS.initialScanProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> watchService.setInitialScan(newValue));
	}

	@Override
	public void shutdown() throws InterruptedException
	{
		if (watchService != null)
		{
			watchService.cancel();
		}
		if (watchServiceExecutor != null)
		{
			watchServiceExecutor.shutdownNow();
			watchServiceExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}
}
