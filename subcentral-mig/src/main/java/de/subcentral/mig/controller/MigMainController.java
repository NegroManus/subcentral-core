package de.subcentral.mig.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import de.subcentral.core.util.NamedThreadFactory;
import de.subcentral.fx.FxIO;
import de.subcentral.fx.FxNodes;
import de.subcentral.fx.ctrl.MainController;
import de.subcentral.fx.ctrl.TaskExecutor;
import de.subcentral.mig.process.MigrationAssistance;
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

public class MigMainController extends MainController {
    // Model
    private MigrationAssistance assistance  = new MigrationAssistance();

    // View-Model
    private List<Page>          pages       = new ArrayList<>(3);
    private IntegerProperty     currentPage = new SimpleIntegerProperty(this, "currentPage", -1);

    // View
    // Content
    @FXML
    private Label               pageTitleLbl;
    @FXML
    private BorderPane          rootPane;
    @FXML
    private AnchorPane          pageRootPane;
    // Lower button bar
    @FXML
    private Button              backBtn;
    @FXML
    private Button              nextBtn;
    @FXML
    private Button              cancelBtn;

    public MigMainController(Stage primaryStage) {
        super(primaryStage);
    }

    public MigrationAssistance getAssistance() {
        return assistance;
    }

    @Override
    protected void initialize() throws Exception {
        initPages();
        initLowerButtonBar();
        initExecutor();

        currentPage.set(0);
    }

    private void initPages() {
        Page settingsPage = new Page(() -> new SettingsPageController(MigMainController.this), "SettingsPage.fxml");
        Page configurePage = new Page(() -> new ScopePageController(MigMainController.this), "ConfigurePage.fxml");
        Page migrationPage = new Page(() -> new MigrationPageController(MigMainController.this), "MigrationPage.fxml");
        pages.add(settingsPage);
        pages.add(configurePage);
        pages.add(migrationPage);

        currentPage.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int oldIndex = oldValue.intValue();
                int newIndex = newValue.intValue();
                // At the beginning the index is -1
                Page oldPage = oldIndex < 0 ? null : pages.get(oldIndex);
                Page newPage = pages.get(newIndex);
                if (newPage.isControllerNotLoaded()) {
                    try {
                        FxIO.loadView(newPage.fxmlFilename, newPage.loadController());
                        Pane contentPane = newPage.controller.getRootPane();
                        AnchorPane.setTopAnchor(contentPane, 0.0d);
                        AnchorPane.setRightAnchor(contentPane, 0.0d);
                        AnchorPane.setBottomAnchor(contentPane, 0.0d);
                        AnchorPane.setLeftAnchor(contentPane, 0.0d);
                    }
                    catch (IOException e) {
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
                if (oldPage != null) {
                    oldPage.controller.onExit();
                }
                newPage.controller.onEnter();

                // Set content
                pageRootPane.getChildren().setAll(newPage.controller.getRootPane());
            }
        });
    }

    private void initLowerButtonBar() {
        backBtn.setOnAction((ActionEvent evt) -> pageBack());
        // In order to remove it from the layout when invisible:
        backBtn.managedProperty().bind(backBtn.visibleProperty());

        nextBtn.setOnAction((ActionEvent evt) -> pageNext());
        nextBtn.managedProperty().bind(nextBtn.visibleProperty());

        cancelBtn.setOnAction((ActionEvent evt) -> cancel());
    }

    private void initExecutor() {
        initExecutor(new TaskExecutor(Executors.newSingleThreadExecutor(new NamedThreadFactory("Worker")), primaryStage));
    }

    public void pageBack() {
        int index = currentPage.get();
        if (index > 0) {
            currentPage.set(index - 1);
        }
    }

    public void pageNext() {
        int index = currentPage.get();
        if (index < pages.size() - 1) {
            currentPage.set(index + 1);
        }
    }

    public void cancel() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        FxNodes.fixAlertHeight(alert);
        alert.initOwner(primaryStage);
        alert.setTitle("Cancel migration?");
        alert.setHeaderText("Do you really want to cancel the migration?");
        alert.setContentText("If you cancel the migration, the already migrated data remains migrated but no more data will be migrated.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            Platform.exit();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (Page page : pages) {
            if (page.controller != null) {
                page.controller.shutdown();
            }
        }
        // shutdown executor
        super.shutdown();
    }

    private static final class Page {
        private AbstractPageController                 controller;
        private final Supplier<AbstractPageController> controllerConstructor;
        private final String                           fxmlFilename;

        private Page(Supplier<AbstractPageController> controllerConstructor, String fxmlFilename) {
            this.controllerConstructor = controllerConstructor;
            this.fxmlFilename = fxmlFilename;
        }

        private boolean isControllerNotLoaded() {
            return controller == null;
        }

        private AbstractPageController loadController() {
            return controller = controllerConstructor.get();
        }
    }
}
