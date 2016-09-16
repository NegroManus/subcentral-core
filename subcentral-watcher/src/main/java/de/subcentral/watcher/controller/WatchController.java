package de.subcentral.watcher.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.FxIO;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ctrl.SubController;
import de.subcentral.fx.settings.ListSettingsProperty;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.controller.settings.SettingsController;
import de.subcentral.watcher.controller.settings.WatchSettingsController;
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

public class WatchController extends SubController<WatcherMainController> {
	// View
	@FXML
	private Button					startWatchButton;
	@FXML
	private Button					stopWatchButton;
	@FXML
	private Label					watchStatusLabel;
	@FXML
	private HBox					watchDirectoriesHBox;
	private final ImageView			watchImg	= new ImageView(FxIO.loadImg("iris_16.png"));

	// Service
	private DirectoryWatchService	watchService;
	private ExecutorService			watchServiceExecutor;

	public WatchController(WatcherMainController watcherMainController) {
		super(watcherMainController);
	}

	@Override
	protected void initialize() throws Exception {
		initWatchService();

		startWatchButton.disableProperty().bind(new BooleanBinding() {
			{
				super.bind(watchService.runningProperty(), SettingsController.SETTINGS.getWatchDirectories());
			}

			@Override
			protected boolean computeValue() {
				return watchService.isRunning() || SettingsController.SETTINGS.getWatchDirectories().getValue().isEmpty();
			}
		});
		startWatchButton.setOnAction(evt -> {
			watchService.restart();
		});

		stopWatchButton.disableProperty().bind(watchService.runningProperty().not());
		stopWatchButton.setOnAction(evt -> {
			watchService.cancel();
		});

		watchStatusLabel.textProperty().bind(new StringBinding() {
			{
				super.bind(watchService.stateProperty());
			}

			@Override
			protected String computeValue() {
				switch (watchService.getState()) {
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
		updateWatchDirsHBox(SettingsController.SETTINGS.getWatchDirectories().getValue());
		SettingsController.SETTINGS.getWatchDirectories().addListener((Observable o) -> {
			@SuppressWarnings("unchecked")
			ListSettingsProperty<Path> prop = (ListSettingsProperty<Path>) o;
			updateWatchDirsHBox(prop.getValue());
		});

		watchService.setOnFailed((WorkerStateEvent event) -> {
			DirectoryWatchService watchService = (DirectoryWatchService) event.getSource();
			Throwable ex = watchService.getException();
			StringJoiner dirsString = new StringJoiner(", ");
			for (Path dir : watchService.getWatchDirectories()) {
				dirsString.add(dir.toString());
			}
			Alert alert = FxUtil.createExceptionAlert(getPrimaryStage(), "Watch failed", "Failed to watch " + dirsString + ".", ex);
			alert.show();
		});
	}

	private void updateWatchDirsHBox(List<Path> watchDirs) {
		watchDirectoriesHBox.getChildren().retainAll(watchImg);
		if (watchDirs.isEmpty()) {
			ImageView img = new ImageView(FxIO.loadImg("settings_16.png"));
			Hyperlink link = new Hyperlink("Add watch directory", img);
			link.setVisited(true);
			link.setOnAction((ActionEvent evt) -> {
				// Open settings
				parent.selectTab(WatcherMainController.SETTINGS_TAB_INDEX);
				parent.getSettingsController().selectSection(SettingsController.WATCH_SECTION);
				WatchSettingsController watchSettingsCtrl = (WatchSettingsController) parent.getSettingsController().getSections().get(SettingsController.WATCH_SECTION).getController();
				// Open directory chooser
				watchSettingsCtrl.addWatchDirectory();
			});
			watchDirectoriesHBox.getChildren().add(link);
		}
		else {
			for (Path dir : watchDirs) {
				watchDirectoriesHBox.getChildren().add(FxControlBindings.createBrowseHyperlink(dir, getExecutor()));
			}
		}
	}

	private void initWatchService() throws IOException {
		watchServiceExecutor = Executors.newSingleThreadExecutor((Runnable r) -> new Thread(r, "Watcher-WatchService"));

		watchService = new DirectoryWatchService(parent.getProcessingController()::handleFilesFromWatchDir);
		watchService.setExecutor(watchServiceExecutor);
		WatcherFxUtil.bindWatchDirectories(watchService, SettingsController.SETTINGS.getWatchDirectories().property());
		watchService.setInitialScan(SettingsController.SETTINGS.getInitialScan().get());
		SettingsController.SETTINGS.getInitialScan()
				.property()
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> watchService.setInitialScan(newValue));
	}

	@Override
	public void shutdown() throws InterruptedException {
		if (watchService != null) {
			watchService.cancel();
		}
		if (watchServiceExecutor != null) {
			watchServiceExecutor.shutdownNow();
			watchServiceExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}
}
