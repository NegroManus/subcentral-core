package de.subcentral.watcher.controller;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.watcher.controller.processing.ProcessingController;
import de.subcentral.watcher.controller.settings.SettingsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainController extends AbstractController
{
	private static final Logger log = LogManager.getLogger(MainController.class);

	public static final int	PROCESSING_TAB_INDEX	= 0;
	public static final int	SETTINGS_TAB_INDEX		= 1;

	private final Stage primaryStage;

	// View
	// UI components are automatically injected before initialize()
	@FXML
	private BorderPane	rootPane;
	@FXML
	private TabPane		tabPane;
	@FXML
	private AnchorPane	processingRootPane;
	@FXML
	private AnchorPane	settingsRootPane;

	// SystemTray handling
	private SystemTray	systemTray;
	private TrayIcon	trayIcon;

	// Controller
	private WatchController			watchController;
	private ProcessingController	processingController;
	private SettingsController		settingsController;
	private ExecutorService			commonExecutor;

	public MainController(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
	}

	@Override
	public void doInitialize() throws Exception
	{
		initCommonExecutor();
		initCloseHandling();
		// initializing order important
		initSettingsController();
		initProcessingController();
		initWatchController();

		// System Tray
		initSystemTrayIcon();
	}

	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public ExecutorService getCommonExecutor()
	{
		return commonExecutor;
	}

	public WinRar getWinRar()
	{
		WinRar winRar = WinRar.getInstance();
		winRar.setProcessExecutor(commonExecutor);
		return winRar;
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

	private void initCommonExecutor()
	{
		int cpus = Runtime.getRuntime().availableProcessors();
		int coreSize = 1 * cpus;
		int maxSize = 8 * cpus;
		long idleTimeout = 60;
		BlockingQueue<Runnable> workQueue = new SynchronousQueue<Runnable>();
		ThreadFactory threadFactory = new NamedThreadFactory("Watcher-Worker", false);
		RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(coreSize, maxSize, idleTimeout, TimeUnit.SECONDS, workQueue, threadFactory, handler);
		commonExecutor = pool;
	}

	private void initCloseHandling()
	{
		// Explicit exit because application can be hidden and shown again
		Platform.setImplicitExit(false);
		primaryStage.setOnCloseRequest((WindowEvent) -> Platform.exit());
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

	private void initSystemTrayIcon()
	{
		javax.swing.SwingUtilities.invokeLater(() ->
		{
			// Check the SystemTray is supported
			if (!SystemTray.isSupported())
			{
				log.warn("SystemTray is not supported");
				return;
			}

			try
			{
				// Convention for tray icons: set the default icon for opening the application stage in a bold font
				java.awt.Font defaultFont = java.awt.Font.decode(null);
				java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);

				systemTray = SystemTray.getSystemTray();
				java.awt.Image trayImg = FxUtil.loadAwtImg("watcher_16.png");
				trayIcon = new TrayIcon(trayImg);

				PopupMenu popup = new PopupMenu();
				MenuItem showHideItem = new MenuItem("Hide");
				ActionListener showHideListener = (ActionEvent e) -> Platform.runLater(() ->
				{
					if (primaryStage.isShowing())
					{
						primaryStage.hide();
						showHideItem.setLabel("Show");
						showHideItem.setFont(boldFont);
					}
					else
					{
						primaryStage.show();
						showHideItem.setLabel("Hide");
						showHideItem.setFont(defaultFont);
					}
				});
				showHideItem.addActionListener(showHideListener);

				MenuItem exitItem = new MenuItem("Exit");
				exitItem.addActionListener((ActionEvent e) -> Platform.runLater(() -> primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST))));

				// Add components to pop-up menu
				popup.add(showHideItem);
				popup.addSeparator();
				popup.add(exitItem);

				trayIcon.setPopupMenu(popup);
				// Show/Hide on double-click
				trayIcon.addActionListener(showHideListener);

				systemTray.add(trayIcon);
				log.info("Added SystemTray icon");
			}
			catch (IOException | AWTException e)
			{
				log.warn("SystemTray icon could not be added", e);
			}
		});
	}

	public boolean isSystemTrayAvailable()
	{
		return trayIcon != null;
	}

	public void displaySystemTrayNotification(String caption, String text, MessageType messageType)
	{
		if (isSystemTrayAvailable())
		{
			javax.swing.SwingUtilities.invokeLater(() -> trayIcon.displayMessage(caption, text, messageType));
		}
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

		// Remove the SystemTray icon to shutdown AWT EventQueue
		javax.swing.SwingUtilities.invokeLater(() -> systemTray.remove(trayIcon));
	}
}
