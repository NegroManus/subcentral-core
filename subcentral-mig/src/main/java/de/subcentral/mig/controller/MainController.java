package de.subcentral.mig.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxUtil;
import de.subcentral.mig.MigrationConfig;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainController extends AbstractController
{
	// Model
	private MigrationConfig	config		= new MigrationConfig();

	// View-Model
	private List<Page>		pages		= new ArrayList<>(3);
	private IntegerProperty	currentPage	= new SimpleIntegerProperty(this, "currentPage", -1);

	// View
	private final Stage		primaryStage;
	// Content
	@FXML
	private Label			pageTitleLbl;
	@FXML
	private BorderPane		rootPane;
	@FXML
	private AnchorPane		pageRootPane;
	// Lower button bar
	@FXML
	private Button			backBtn;
	@FXML
	private Button			nextBtn;
	@FXML
	private Button			cancelBtn;

	// Controller
	private ExecutorService	commonExecutor;

	public MainController(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
	}

	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public ExecutorService getCommonExecutor()
	{
		return commonExecutor;
	}

	public MigrationConfig getConfig()
	{
		return config;
	}

	@Override
	public void initialize() throws Exception
	{
		initPages();
		initLowerButtonBar();
		initCommonExecutor();

		currentPage.set(0);
	}

	private void initPages()
	{
		Page settingsPage = new Page(() -> new SettingsPageController(MainController.this), "SettingsPage.fxml");
		Page configurePage = new Page(() -> new ConfigurePageController(MainController.this), "ConfigurePage.fxml");
		Page migrationPage = new Page(() -> new MigrationPageController(MainController.this), "MigrationPage.fxml");
		pages.add(settingsPage);
		pages.add(configurePage);
		pages.add(migrationPage);

		currentPage.addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				int oldIndex = oldValue.intValue();
				int newIndex = newValue.intValue();
				// At the beginning the index is -1
				Page oldPage = oldIndex < 0 ? null : pages.get(oldIndex);
				Page newPage = pages.get(newIndex);
				if (newPage.isControllerNotLoaded())
				{
					try
					{
						FxUtil.loadFromFxml(newPage.fxmlFilename, newPage.loadController());
						Pane contentPane = newPage.controller.getRootPane();
						AnchorPane.setTopAnchor(contentPane, 0.0d);
						AnchorPane.setRightAnchor(contentPane, 0.0d);
						AnchorPane.setBottomAnchor(contentPane, 0.0d);
						AnchorPane.setLeftAnchor(contentPane, 0.0d);
					}
					catch (IOException e)
					{
						throw new RuntimeException("Error while loading controller", e);
					}
				}

				// Set page title
				pageTitleLbl.setText("Step " + (newIndex + 1) + "/" + pages.size() + ": " + newPage.controller.getTitle());

				// Set visibilities & bind enablement
				backBtn.setVisible(newIndex > 0);
				nextBtn.setVisible(newIndex < pages.size() - 1);
				nextBtn.disableProperty().unbind();
				nextBtn.disableProperty().bind(newPage.controller.nextButtonDisableBinding());

				// Apply operations on page change
				if (oldPage != null)
				{
					oldPage.controller.onExiting();
				}
				newPage.controller.onEntering();
				// Set content
				pageRootPane.getChildren().setAll(newPage.controller.getRootPane());
			}
		});
	}

	private void initLowerButtonBar()
	{
		backBtn.setOnAction((ActionEvent evt) -> pageBack());
		// In order to remove it from the layout when invisible:
		backBtn.managedProperty().bind(backBtn.visibleProperty());

		nextBtn.setOnAction((ActionEvent evt) -> pageNext());
		nextBtn.managedProperty().bind(nextBtn.visibleProperty());

		cancelBtn.setOnAction((ActionEvent evt) -> cancel());
	}

	private void initCommonExecutor()
	{
		commonExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Worker"));
	}

	public void pageBack()
	{
		int index = currentPage.get();
		if (index > 0)
		{
			currentPage.set(index - 1);
		}
	}

	public void pageNext()
	{
		int index = currentPage.get();
		if (index < pages.size() - 1)
		{
			currentPage.set(index + 1);
		}
	}

	public void cancel()
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(primaryStage);
		alert.setTitle("Cancel migration?");
		alert.setHeaderText("Do you really want to cancel the migration?");
		alert.setContentText("If you cancel the migration, the already migrated data remains migrated but no more data will be migrated.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.YES)
		{
			Platform.exit();
		}
	}

	@Override
	public void shutdown() throws Exception
	{
		for (Page page : pages)
		{
			if (page.controller != null)
			{
				page.controller.shutdown();
			}
		}
		if (commonExecutor != null)
		{
			commonExecutor.shutdownNow();
			commonExecutor.awaitTermination(10, TimeUnit.SECONDS);
		}
	}

	private static final class Page
	{
		private AbstractPageController					controller;
		private final Supplier<AbstractPageController>	controllerConstructor;
		private final String							fxmlFilename;

		private Page(Supplier<AbstractPageController> controllerConstructor, String fxmlFilename)
		{
			this.controllerConstructor = controllerConstructor;
			this.fxmlFilename = fxmlFilename;
		}

		private boolean isControllerNotLoaded()
		{
			return controller == null;
		}

		private AbstractPageController loadController()
		{
			return controller = controllerConstructor.get();
		}
	}
}
