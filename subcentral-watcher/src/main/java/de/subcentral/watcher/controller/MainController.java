package de.subcentral.watcher.controller;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherApp;
import de.subcentral.watcher.controller.processing.ProcessingController;
import de.subcentral.watcher.controller.settings.SettingsController;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainController extends AbstractController
{
    private static final Logger log = LogManager.getLogger(MainController.class);

    public static final int PROCESSING_TAB_INDEX = 0;
    public static final int SETTINGS_TAB_INDEX	 = 1;

    // View
    // UI components are automatically injected before initialize()
    private final WatcherApp watcherApp;
    @FXML
    private BorderPane	     rootPane;
    @FXML
    private TabPane	     tabPane;
    @FXML
    private AnchorPane	     processingRootPane;
    @FXML
    private AnchorPane	     settingsRootPane;

    // Controller
    private WatchController	     watchController;
    private ProcessingController     processingController;
    private SettingsController	     settingsController;
    private ScheduledExecutorService commonExecutor;

    public MainController(WatcherApp watcherApp)
    {
	this.watcherApp = watcherApp;
    }

    @Override
    public void doInitialize() throws Exception
    {
	commonExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2, new NamedThreadFactory("Watcher-CommonExecutor-Thread", false));

	// initializing order important
	initSettingsController();
	initProcessingController();
	initWatchController();
    }

    public WatcherApp getWatcherApp()
    {
	return watcherApp;
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

    public void selectTab(int index)
    {
	tabPane.getSelectionModel().select(index);
    }

    private void initSettingsController() throws IOException
    {
	settingsController = new SettingsController(this);
	BorderPane settingsPane = FxUtil.loadFromFxml("SettingsView.fxml", "SettingsView", Locale.ENGLISH, settingsController);
	AnchorPane.setTopAnchor(settingsPane, 0.0d);
	AnchorPane.setRightAnchor(settingsPane, 0.0d);
	AnchorPane.setBottomAnchor(settingsPane, 0.0d);
	AnchorPane.setLeftAnchor(settingsPane, 0.0d);
	settingsRootPane.getChildren().add(settingsPane);
    }

    private void initProcessingController() throws IOException
    {
	processingController = new ProcessingController(this);
	loadProcessingPane();
    }

    public void loadProcessingPane() throws IOException
    {
	BorderPane processingPane = FxUtil.loadFromFxml("ProcessingView.fxml", "ProcessingView", Locale.ENGLISH, processingController);
	AnchorPane.setTopAnchor(processingPane, 0.0d);
	AnchorPane.setRightAnchor(processingPane, 0.0d);
	AnchorPane.setBottomAnchor(processingPane, 0.0d);
	AnchorPane.setLeftAnchor(processingPane, 0.0d);
	processingRootPane.getChildren().setAll(processingPane);
    }

    private void initWatchController() throws IOException
    {
	watchController = new WatchController(this);
	HBox watchPane = FxUtil.loadFromFxml("WatchPane.fxml", null, Locale.ENGLISH, watchController);
	rootPane.setTop(watchPane);
    }

    @Override
    public void shutdown() throws Exception
    {
	// shutdown in reverse order
	processingController.shutdown();
	watchController.shutdown();
	settingsController.shutdown();
	if (commonExecutor != null)
	{
	    commonExecutor.shutdown();
	    commonExecutor.awaitTermination(10, TimeUnit.SECONDS);
	}
    }
}
