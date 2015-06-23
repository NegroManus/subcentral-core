package de.subcentral.watcher.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.DirectoryWatchService;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.settings.WatcherSettings;

public class WatchController extends AbstractController
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
	protected void doInitialize() throws Exception
	{
		initWatchService();

		startWatchButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(watchService.runningProperty(), WatcherSettings.INSTANCE.watchDirectoriesProperty().emptyProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return watchService.isRunning() || WatcherSettings.INSTANCE.watchDirectoriesProperty().isEmpty();
			}
		});
		startWatchButton.setOnAction(evt -> {
			watchService.restart();
			evt.consume();
		});

		stopWatchButton.disableProperty().bind(watchService.runningProperty().not());
		stopWatchButton.setOnAction(evt -> {
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

		updateWatchDirsHBox(WatcherSettings.INSTANCE.getWatchDirectories());
		WatcherSettings.INSTANCE.watchDirectoriesProperty().addListener(new InvalidationListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void invalidated(Observable o)
			{
				updateWatchDirsHBox((List<Path>) o);
			}
		});

		watchService.setOnFailed((WorkerStateEvent event) -> {
			DirectoryWatchService watchService = (DirectoryWatchService) event.getSource();
			Throwable ex = watchService.getException();
			StringJoiner dirsString = new StringJoiner(", ");
			for (Path dir : watchService.getWatchDirectories())
			{
				dirsString.add(dir.toString());
			}
			Alert alert = FxUtil.createExceptionAlert("Exception while watching", "An exception occured while watching " + dirsString + ".", ex);
			alert.show();
		});
	}

	private void updateWatchDirsHBox(List<Path> watchDirs)
	{
		watchDirectoriesHBox.getChildren().clear();
		if (watchDirs.isEmpty())
		{
			addToHBoxWithMaxHeight(watchDirectoriesHBox, new Label("<no watch directories>"));
		}
		else
		{
			Iterator<Path> iter = watchDirs.iterator();
			while (iter.hasNext())
			{
				addToHBoxWithMaxHeight(watchDirectoriesHBox, FxUtil.createPathHyperlink(iter.next(), mainController.getCommonExecutor()));
				if (iter.hasNext())
				{
					addToHBoxWithMaxHeight(watchDirectoriesHBox, new Label("·"));
				}
			}
		}
	}

	private void addToHBoxWithMaxHeight(HBox hbox, Control ctrl)
	{
		// maximal max height is important so that labels take all the height and are centered correctly
		ctrl.setMaxHeight(Double.MAX_VALUE);
		hbox.getChildren().add(ctrl);
	}

	private void initWatchService() throws IOException
	{
		watchServiceExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Watcher-WatchService", false));

		watchService = new DirectoryWatchService(this.mainController.getProcessingController()::handleFiles);
		watchService.setExecutor(watchServiceExecutor);
		WatcherFxUtil.bindWatchDirectories(watchService, WatcherSettings.INSTANCE.getWatchDirectories());
		watchService.setInitialScan(WatcherSettings.INSTANCE.isInitialScan());
		WatcherSettings.INSTANCE.initialScanProperty()
				.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
					watchService.setInitialScan(newValue);
				});
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
			watchServiceExecutor.shutdown();
			watchServiceExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}
}
