package de.subcentral.watcher;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class WatcherApp extends Application
{
	public static final String	APP_NAME			= "Watcher";
	public static final String	APP_VERSION			= "2.0";
	public static final String	APP_VERSION_DATE	= "2015-09-01 21:30";
	public static final String	APP_INFO			= APP_NAME + " " + APP_VERSION + " (" + APP_VERSION_DATE + ")";

	private static final Logger log = LogManager.getLogger(WatcherApp.class);

	// View
	private Stage		primaryStage;
	private BorderPane	mainView;

	// Control
	private MainController mainController;

	@Override
	public void init() throws Exception
	{
		long start = System.nanoTime();
		log.info("Initializing {} ...", APP_INFO);
		log.info("Operating system: {} {} {}", SystemUtils.OS_NAME, SystemUtils.OS_VERSION, SystemUtils.OS_ARCH);
		log.info("Java version: {}", SystemUtils.JAVA_VERSION);
		log.info("Java runtime: {} {}", SystemUtils.JAVA_RUNTIME_NAME, SystemUtils.JAVA_RUNTIME_VERSION);
		log.info("Java VM: {} ({}) - Vendor: {}, Version: {}", SystemUtils.JAVA_VM_NAME, SystemUtils.JAVA_VM_INFO, SystemUtils.JAVA_VM_VENDOR, SystemUtils.JAVA_VM_VERSION);
		log.info("Java home: {}", SystemUtils.JAVA_HOME);
		log.info("User dir: {}", SystemUtils.USER_DIR);

		log.info("Initialized {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		log.info("Starting {} ...", APP_INFO);
		long start = System.nanoTime();

		this.primaryStage = primaryStage;

		// Order is important
		initPrimaryStage();
		initMainController();
		initSceneAndShow();

		log.info("Started {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
	}

	private void initPrimaryStage()
	{
		primaryStage.setTitle(APP_INFO);
		primaryStage.getIcons().addAll(FxUtil.loadImg("watcher_16.png"), FxUtil.loadImg("watcher_32.png"), FxUtil.loadImg("watcher_64.png"));
	}

	private void initMainController() throws IOException
	{
		this.mainController = new MainController(primaryStage);
		mainView = FxUtil.loadFromFxml("MainView.fxml", "MainView", Locale.ENGLISH, mainController);
	}

	private void initSceneAndShow()
	{
		Scene scene = new Scene(mainView);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public MainController getMainController()
	{
		return mainController;
	}

	/**
	 * Returns the main stage.
	 * 
	 * @return
	 */
	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	@Override
	public void stop() throws Exception
	{
		log.debug("Stopping {} ...", APP_INFO);
		long start = System.nanoTime();

		mainController.shutdown();

		log.info("Stopped {} in {} ms", APP_INFO, TimeUtil.durationMillis(start));
	}

	public static void main(String[] args)
	{
		System.setProperty("javafx.embed.singleThread", "true");

		// Ensure awt toolkit is initialized
		java.awt.Toolkit.getDefaultToolkit();

		launch(args);
	}
}
