package de.subcentral.watcher.controller;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.Controller;
import de.subcentral.fx.FxUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.watcher.controller.processing.ProcessingController;
import de.subcentral.watcher.controller.settings.SettingsController;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainController extends Controller
{
	private static final Logger		log						= LogManager.getLogger(MainController.class);

	public static final int			PROCESSING_TAB_INDEX	= 0;
	public static final int			SETTINGS_TAB_INDEX		= 1;

	private final Stage				primaryStage;

	// View
	// UI components are automatically injected before initialize()
	@FXML
	private BorderPane				rootPane;
	@FXML
	private TabPane					tabPane;
	@FXML
	private AnchorPane				processingRootPane;
	@FXML
	private AnchorPane				settingsRootPane;

	// SystemTray handling
	private SystemTray				systemTray;
	private TrayIcon				systemTrayIcon;
	private MenuItem				systemTrayShowHideMenuItem;

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
	protected void initialize() throws Exception
	{
		initCommonExecutor();
		initCloseHandling();

		// initializing order important
		initSettingsController();
		initProcessingController();
		initWatchController();

		initSystemTray();
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
		primaryStage.setOnCloseRequest((WindowEvent) -> exit());
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
		reloadProcessingPane();
	}

	private void initWatchController() throws IOException
	{
		watchController = new WatchController(this);
		HBox watchPane = FxUtil.loadFromFxml("WatchPane.fxml", null, Locale.ENGLISH, watchController);
		rootPane.setTop(watchPane);
	}

	private void initSystemTray()
	{
		if (WatcherSettings.INSTANCE.isSystemTrayEnabled())
		{
			SwingUtilities.invokeLater(() ->
			{
				// Check the SystemTray is supported
				if (!SystemTray.isSupported())
				{
					log.warn("System tray is not supported");
					return;
				}
				try
				{
					systemTray = SystemTray.getSystemTray();
					ActionListener showHideListener = (ActionEvent e) -> Platform.runLater(this::toggleShowHide);

					java.awt.Image trayImg = FxUtil.loadAwtImg("watcher_16.png");
					systemTrayIcon = new TrayIcon(trayImg);
					// Show/Hide on double-click
					systemTrayIcon.addActionListener(showHideListener);

					PopupMenu popup = new PopupMenu();

					MenuItem appItem = new MenuItem("Watcher");
					appItem.addActionListener((ActionEvent e) -> Platform.runLater(() -> primaryStage.toFront()));
					appItem.setFont(java.awt.Font.decode(null).deriveFont(java.awt.Font.BOLD));

					systemTrayShowHideMenuItem = new MenuItem("Hide");
					systemTrayShowHideMenuItem.addActionListener(showHideListener);

					MenuItem exitItem = new MenuItem("Exit");
					exitItem.addActionListener((ActionEvent e) -> Platform.runLater(this::exit));

					// Add components to pop-up menu
					popup.add(appItem);
					popup.addSeparator();
					popup.add(systemTrayShowHideMenuItem);
					popup.addSeparator();
					popup.add(exitItem);

					systemTrayIcon.setPopupMenu(popup);

					systemTray.add(systemTrayIcon);
				}
				catch (Exception e)
				{
					log.warn("System tray icon could not be added", e);

					// Clean up just in case. So that no uninitialized SystemTrayIcon is displayed
					removeSystemTrayIcon();
				}
			});
		}
		else
		{
			log.debug("Not adding system tray icon because setting systemTrayEnabled is false");
		}
	}

	// Public API
	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public ExecutorService getCommonExecutor()
	{
		return commonExecutor;
	}

	public WinRar getWinRar() throws UnsupportedOperationException
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

	public void show()
	{
		if (!primaryStage.isShowing())
		{
			showImpl();
		}
	}

	public void hide()
	{
		if (primaryStage.isShowing())
		{
			hideImpl();
		}
	}

	public void toggleShowHide()
	{
		if (primaryStage.isShowing())
		{
			hideImpl();
		}
		else
		{
			showImpl();
		}
	}

	private void showImpl()
	{
		primaryStage.show();
		SwingUtilities.invokeLater(() -> systemTrayShowHideMenuItem.setLabel("Hide"));
	}

	private void hideImpl()
	{
		primaryStage.hide();
		SwingUtilities.invokeLater(() -> systemTrayShowHideMenuItem.setLabel("Open"));
	}

	public void reloadProcessingPane() throws IOException
	{
		// log.debug("Reloading the processing pane");
		BorderPane processingPane = FxUtil.loadFromFxml("ProcessingView.fxml", "ProcessingView", Locale.ENGLISH, processingController);
		AnchorPane.setTopAnchor(processingPane, 0.0d);
		AnchorPane.setRightAnchor(processingPane, 0.0d);
		AnchorPane.setBottomAnchor(processingPane, 0.0d);
		AnchorPane.setLeftAnchor(processingPane, 0.0d);
		processingRootPane.getChildren().setAll(processingPane);
	}

	public void selectTab(int index)
	{
		tabPane.getSelectionModel().select(index);
	}

	public boolean isSystemTrayAvailable()
	{
		// if systemTrayIcon is not null, then systemTray and systemTrayIcon were successfully initialized
		return systemTrayIcon != null;
	}

	public void displaySystemTrayNotification(String caption, String text, MessageType messageType)
	{
		if (isSystemTrayAvailable())
		{
			javax.swing.SwingUtilities.invokeLater(() -> systemTrayIcon.displayMessage(caption, text, messageType));
		}
	}

	public void exit()
	{
		// Explicit exit: Causes Application.stop()
		Platform.exit();
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

		removeSystemTrayIcon();
	}

	/**
	 * Removes the TrayIcon. Must be done on application shutdown because the SystemTrayIcon causes the AWT Event Dispatch Thread to keep running.
	 */
	private void removeSystemTrayIcon()
	{
		if (systemTray != null)
		{
			if (SwingUtilities.isEventDispatchThread())
			{
				systemTray.remove(systemTrayIcon);
			}
			else
			{
				try
				{
					SwingUtilities.invokeAndWait(() -> systemTray.remove(systemTrayIcon));
				}
				catch (InvocationTargetException | InterruptedException e)
				{
					log.warn("Exception while removing the system tray icon from the system tray", e);
				}
			}
		}
	}
}
