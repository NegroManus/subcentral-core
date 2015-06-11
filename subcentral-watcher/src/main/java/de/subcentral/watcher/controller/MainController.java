package de.subcentral.watcher.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.apache.commons.configuration2.ex.ConfigurationException;

import com.google.common.io.Resources;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.processing.ProcessingController;
import de.subcentral.watcher.controller.settings.SettingsController;
import de.subcentral.watcher.settings.WatcherSettings;

public class MainController extends AbstractController
{
	// View
	// UI components are automatically injected before initialize()
	private final Stage					primaryStage;
	@FXML
	private BorderPane					rootBorderPane;
	@FXML
	private AnchorPane					processingRootPane;
	@FXML
	private AnchorPane					settingsRootPane;

	// Controller
	private WatchController				watchController;
	private ProcessingController		processingController;
	private SettingsController			settingsController;
	private ScheduledExecutorService	commonExecutor;

	public MainController(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
	}

	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public ScheduledExecutorService getCommonExecutor()
	{
		return commonExecutor;
	}

	public WatchController getWatchController()
	{
		return watchController;
	}

	public ProcessingController getProcessingController()
	{
		return processingController;
	}

	public SettingsController getSettingsController()
	{
		return settingsController;
	}

	public void doInitialize() throws IOException, ConfigurationException
	{
		commonExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2,
				new NamedThreadFactory("Watcher-CommonExecutor-Thread", false));

		loadSettings();

		initProcessingController();
		initWatchController();
		initSettingsController();
	}

	private void loadSettings() throws ConfigurationException
	{
		WatcherSettings.INSTANCE.load(Resources.getResource("watcher-config-default.xml"));
	}

	private void initWatchController() throws IOException
	{
		watchController = new WatchController(this);
		HBox watchPane = FxUtil.loadFromFxml("WatchPane.fxml", null, Locale.ENGLISH, watchController);
		rootBorderPane.setTop(watchPane);
	}

	private void initProcessingController() throws IOException
	{
		processingController = new ProcessingController(this);
		BorderPane processingPane = FxUtil.loadFromFxml("ProcessingView.fxml", "ProcessingView", Locale.ENGLISH, processingController);
		AnchorPane.setTopAnchor(processingPane, 0.0d);
		AnchorPane.setRightAnchor(processingPane, 0.0d);
		AnchorPane.setBottomAnchor(processingPane, 0.0d);
		AnchorPane.setLeftAnchor(processingPane, 0.0d);
		processingRootPane.getChildren().add(processingPane);
	}

	private void initSettingsController() throws IOException
	{
		settingsController = new SettingsController(this);
		AnchorPane settingsPane = FxUtil.loadFromFxml("SettingsView.fxml", "SettingsView", Locale.ENGLISH, settingsController);
		AnchorPane.setTopAnchor(settingsPane, 0.0d);
		AnchorPane.setRightAnchor(settingsPane, 0.0d);
		AnchorPane.setBottomAnchor(settingsPane, 0.0d);
		AnchorPane.setLeftAnchor(settingsPane, 0.0d);
		settingsRootPane.getChildren().add(settingsPane);
	}

	@Override
	public void shutdown() throws Exception
	{
		watchController.shutdown();
		processingController.shutdown();
		settingsController.shutdown();
		if (commonExecutor != null)
		{
			commonExecutor.shutdown();
			commonExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}
}
