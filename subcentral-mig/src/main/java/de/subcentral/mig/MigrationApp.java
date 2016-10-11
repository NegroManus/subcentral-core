package de.subcentral.mig;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.FxIO;
import de.subcentral.mig.controller.MigMainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MigrationApp extends Application {
    private static final Logger log              = LogManager.getLogger(MigrationApp.class);

    public static final String  APP_NAME         = "SubCentral Migration Tool";
    public static final String  APP_VERSION      = "1.0";
    public static final String  APP_VERSION_DATE = "2015-11-13 00:00";
    public static final String  APP_INFO         = APP_NAME + " " + APP_VERSION + " (" + APP_VERSION_DATE + ")";

    // View
    private Stage               primaryStage;
    private BorderPane          mainView;

    // Control
    private MigMainController   migMainController;

    @Override
    public void init() throws Exception {
        log.info("Initializing {} ...", APP_INFO);
        long start = System.nanoTime();

        log.info("Operating system: {} {} {}", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH);
        log.info("Java version: {}", SystemUtils.JAVA_VERSION);
        log.info("Java runtime: {} {}", SystemUtils.JAVA_RUNTIME_NAME, SystemUtils.JAVA_RUNTIME_VERSION);
        log.info("Java VM: {} ({}) - Vendor: {}, Version: {}", SystemUtils.JAVA_VM_NAME, SystemUtils.JAVA_VM_INFO, SystemUtils.JAVA_VM_VENDOR, SystemUtils.JAVA_VM_VERSION);
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
    }

    private void initMainController() throws IOException {
        this.migMainController = new MigMainController(primaryStage);
        mainView = FxIO.loadView("MainView.fxml", migMainController);
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

        migMainController.shutdown();

        log.info("Stopped {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));

        // Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
