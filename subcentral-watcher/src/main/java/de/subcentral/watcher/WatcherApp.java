package de.subcentral.watcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.LocalConfig;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.FxIO;
import de.subcentral.watcher.controller.WatcherMainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class WatcherApp extends Application {
    public static final String    APP_NAME         = "Watcher";
    public static final String    APP_VERSION      = "2.2.1";
    public static final String    APP_VERSION_DATE = "2016-04-29";
    public static final String    APP_INFO         = APP_NAME + " " + APP_VERSION + " (" + APP_VERSION_DATE + ")";

    public static final String    SYS_PROP_LOGDIR  = "watcher.logdir";

    private static Logger         log;

    // View
    private Stage                 primaryStage;
    private BorderPane            mainView;

    // Control
    private WatcherMainController watcherMainController;

    @Override
    public void init() throws Exception {
        log.info("Initializing {} ...", APP_INFO);
        long start = System.nanoTime();

        log.info("Operating system: {} (version: {}, arch: {})", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH);
        log.info("Java version: {} (vendor: {})", SystemUtils.JAVA_VERSION, SystemUtils.JAVA_VENDOR);
        log.info("Java runtime: {} (version: {})", SystemUtils.JAVA_RUNTIME_NAME, SystemUtils.JAVA_RUNTIME_VERSION);
        log.info("Java VM: {} {} (vendor: {}, version: {})", SystemUtils.JAVA_VM_NAME, SystemUtils.JAVA_VM_INFO, SystemUtils.JAVA_VM_VENDOR, SystemUtils.JAVA_VM_VERSION);
        log.info("Java home: {}", SystemUtils.JAVA_HOME);
        log.info("User dir: {}", SystemUtils.USER_DIR);

        log.info("Initialized {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.debug("Starting {} ...", APP_INFO);
        long start = System.nanoTime();

        this.primaryStage = primaryStage;

        // Order is important
        initPrimaryStage();
        initMainController();
        initSceneAndShow();

        log.info("Started {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
    }

    private void initPrimaryStage() {
        primaryStage.setTitle(APP_INFO);
        primaryStage.getIcons().addAll(FxIO.loadImg("watcher_16.png"), FxIO.loadImg("watcher_32.png"), FxIO.loadImg("watcher_64.png"));
    }

    private void initMainController() throws IOException {
        this.watcherMainController = new WatcherMainController(primaryStage);
        mainView = FxIO.loadView("MainView.fxml", watcherMainController, "MainView", Locale.ENGLISH);
    }

    private void initSceneAndShow() {
        Scene scene = new Scene(mainView);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        log.debug("Stopping {} ...", APP_INFO);
        long start = System.nanoTime();

        watcherMainController.shutdown();

        log.info("Stopped {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
    }

    public static Path getLocalConfigDirectory() {
        return LocalConfig.getLocalConfigDirectorySave().resolve(APP_NAME);
    }

    public static void main(String[] args) {
        // Set logDir system property if not already set
        String logDir = System.getProperty(SYS_PROP_LOGDIR);
        if (logDir == null) {
            System.setProperty(SYS_PROP_LOGDIR, getLocalConfigDirectory().toString());
        }
        log = LogManager.getLogger(WatcherApp.class);

        launch(args);
    }
}
