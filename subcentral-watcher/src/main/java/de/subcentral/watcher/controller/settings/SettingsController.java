package de.subcentral.watcher.controller.settings;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.io.Resources;

import de.subcentral.fx.FxIO;
import de.subcentral.fx.FxNodes;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.ctrl.SubController;
import de.subcentral.watcher.WatcherApp;
import de.subcentral.watcher.controller.WatcherMainController;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class SettingsController extends SubController<WatcherMainController> {
    private static final Logger                log                                  = LogManager.getLogger(SettingsController.class);

    public static final WatcherSettings        SETTINGS                             = new WatcherSettings();

    private static final String                CUSTOM_SETTINGS_FILENAME             = "watcher-settings.xml";
    private static final String                DEFAULT_SETTINGS_FILENAME            = "watcher-settings-default.xml";

    public static final String                 WATCH_SECTION                        = "watch";
    public static final String                 FILENAME_PARSING_SECTION             = "filenameparsing";
    public static final String                 RELEASE_SECTION                      = "release";
    public static final String                 RELEASE_DBS_SECTION                  = "release.dbs";
    public static final String                 RELEASE_GUESSING_SECTION             = "release.guessing";
    public static final String                 RELEASE_COMPATIBILITY_SECTION        = "release.compatibility";
    public static final String                 CORRECTION_SECTION                   = "correction";
    public static final String                 CORRECTION_SUBTITLE_LANGUAGE_SECTION = "correction.subtitleLanguage";
    public static final String                 NAMING_SECTION                       = "naming";
    public static final String                 FILE_TRANSFORMATION_SECTION          = "filetransformation";
    public static final String                 UI_SECTION                           = "ui";

    // View
    @FXML
    private TreeView<SettingsSection>          sectionSelectionTreeView;
    @FXML
    private AnchorPane                         sectionRootPane;
    @FXML
    private Button                             saveBtn;
    @FXML
    private Button                             restoreLastSavedBtn;
    @FXML
    private Button                             restoreDefaultsBtn;

    private BooleanProperty                    defaultSettingsLoaded                = new SimpleBooleanProperty();
    private BooleanProperty                    customSettingsExist                  = new SimpleBooleanProperty();

    // Controllers
    private final Map<String, SettingsSection> sections;

    public SettingsController(WatcherMainController watcherMainController) {
        super(watcherMainController);
        sections = initSections();
    }

    private Map<String, SettingsSection> initSections() {
        Map<String, SettingsSection> ctrls = new HashMap<>();

        SettingsSection watchSection = new SettingsSection(WATCH_SECTION);
        watchSection.setLabel("Watch");
        watchSection.setImage("iris_16.png");
        watchSection.setControllerConstructor(() -> new WatchSettingsController(this));
        watchSection.setFxml("WatchSettingsView.fxml");
        ctrls.put(watchSection.getName(), watchSection);

        SettingsSection filenameParsingSection = new SettingsSection(FILENAME_PARSING_SECTION);
        filenameParsingSection.setLabel("Filename parsing");
        filenameParsingSection.setImage("search_text_16.png");
        filenameParsingSection.setControllerConstructor(() -> new FilenameParsingSettingsController(this));
        filenameParsingSection.setFxml("FilenameParsingSettingsView.fxml");
        ctrls.put(filenameParsingSection.getName(), filenameParsingSection);

        SettingsSection releaseSection = new SettingsSection(RELEASE_SECTION);
        releaseSection.setLabel("Release");
        releaseSection.setImage("release_16.png");
        releaseSection.setControllerConstructor(() -> new ReleaseSettingsController(this));
        releaseSection.setFxml("ReleaseSettingsView.fxml");
        ctrls.put(releaseSection.getName(), releaseSection);

        SettingsSection releaseDbsSection = new SettingsSection(RELEASE_DBS_SECTION);
        releaseDbsSection.setLabel("Databases");
        releaseDbsSection.setImage("database_16.png");
        releaseDbsSection.setControllerConstructor(() -> new ReleaseDbsSettingsController(this));
        releaseDbsSection.setFxml("ReleaseDbsSettingsView.fxml");
        ctrls.put(releaseDbsSection.getName(), releaseDbsSection);

        SettingsSection releaseGuessingSection = new SettingsSection(RELEASE_GUESSING_SECTION);
        releaseGuessingSection.setLabel("Guessing");
        releaseGuessingSection.setImage("idea_16.png");
        releaseGuessingSection.setControllerConstructor(() -> new ReleaseGuessingSettingsController(this));
        releaseGuessingSection.setFxml("ReleaseGuessingSettingsView.fxml");
        ctrls.put(releaseGuessingSection.getName(), releaseGuessingSection);

        SettingsSection releaseCompatibilitySection = new SettingsSection(RELEASE_COMPATIBILITY_SECTION);
        releaseCompatibilitySection.setLabel("Compatibility");
        releaseCompatibilitySection.setImage("couple_16.png");
        releaseCompatibilitySection.setControllerConstructor(() -> new ReleaseCompatibilitySettingsController(this));
        releaseCompatibilitySection.setFxml("ReleaseCompatibilitySettingsView.fxml");
        ctrls.put(releaseCompatibilitySection.getName(), releaseCompatibilitySection);

        SettingsSection correctionSection = new SettingsSection(CORRECTION_SECTION);
        correctionSection.setLabel("Correction");
        correctionSection.setImage("edit_text_16.png");
        correctionSection.setControllerConstructor(() -> new CorrectionSettingsController(this));
        correctionSection.setFxml("CorrectionSettingsView.fxml");
        ctrls.put(correctionSection.getName(), correctionSection);

        SettingsSection correctionSubtitleLanguageSection = new SettingsSection(CORRECTION_SUBTITLE_LANGUAGE_SECTION);
        correctionSubtitleLanguageSection.setLabel("Subtitle language");
        correctionSubtitleLanguageSection.setImage("usa_flag_16.png");
        correctionSubtitleLanguageSection.setControllerConstructor(() -> new SubtitleLanguageCorrectionSettingsController(this));
        correctionSubtitleLanguageSection.setFxml("SubtitleLanguageCorrectionSettingsView.fxml");
        ctrls.put(correctionSubtitleLanguageSection.getName(), correctionSubtitleLanguageSection);

        SettingsSection namingSection = new SettingsSection(NAMING_SECTION);
        namingSection.setLabel("Naming");
        namingSection.setImage("font_16.png");
        namingSection.setControllerConstructor(() -> new NamingSettingsController(this));
        namingSection.setFxml("NamingSettingsView.fxml");
        ctrls.put(namingSection.getName(), namingSection);

        SettingsSection fileTransformationSection = new SettingsSection(FILE_TRANSFORMATION_SECTION);
        fileTransformationSection.setLabel("File transformation");
        fileTransformationSection.setImage("copy_file_16.png");
        fileTransformationSection.setControllerConstructor(() -> new FileTransformationSettingsController(this));
        fileTransformationSection.setFxml("FileTransformationSettingsView.fxml");
        ctrls.put(fileTransformationSection.getName(), fileTransformationSection);

        SettingsSection uiSection = new SettingsSection(UI_SECTION);
        uiSection.setLabel("User interface");
        uiSection.setImage("ui_16.png");
        uiSection.setControllerConstructor(() -> new UserInterfaceSettingsController(this));
        uiSection.setFxml("UserInterfaceSettingsView.fxml");
        ctrls.put(uiSection.getName(), uiSection);

        return ctrls;
    }

    @Override
    protected void initialize() throws Exception {
        loadSettings();
        initSettingsTree();
        initBottomButtonPane();
    }

    private void initSettingsTree() {
        final TreeItem<SettingsSection> root = new TreeItem<>();
        sectionSelectionTreeView.setRoot(root);
        sectionSelectionTreeView.setCellFactory((TreeView<SettingsSection> param) -> new SettingsSectionTreeCell());

        TreeItem<SettingsSection> watchTreeItem = new TreeItem<>(sections.get(WATCH_SECTION));

        TreeItem<SettingsSection> parsingTreeItem = new TreeItem<>(sections.get(FILENAME_PARSING_SECTION));

        TreeItem<SettingsSection> releaseTreeItem = new TreeItem<>(sections.get(RELEASE_SECTION));
        TreeItem<SettingsSection> releaseDatabasesTreeItem = new TreeItem<>(sections.get(RELEASE_DBS_SECTION));
        TreeItem<SettingsSection> releaseGuessingTreeItem = new TreeItem<>(sections.get(RELEASE_GUESSING_SECTION));
        TreeItem<SettingsSection> releaseCompatibilityTreeItem = new TreeItem<>(sections.get(RELEASE_COMPATIBILITY_SECTION));

        TreeItem<SettingsSection> correctionTreeItem = new TreeItem<>(sections.get(CORRECTION_SECTION));
        TreeItem<SettingsSection> correctionSubtitleLanguageTreeItem = new TreeItem<>(sections.get(CORRECTION_SUBTITLE_LANGUAGE_SECTION));

        TreeItem<SettingsSection> namingTreeItem = new TreeItem<>(sections.get(NAMING_SECTION));

        TreeItem<SettingsSection> filetransformationTreeItem = new TreeItem<>(sections.get(FILE_TRANSFORMATION_SECTION));

        TreeItem<SettingsSection> uiTreeItem = new TreeItem<>(sections.get(UI_SECTION));

        // Watch
        root.getChildren().add(watchTreeItem);
        // Parsing
        root.getChildren().add(parsingTreeItem);
        // Release
        root.getChildren().add(releaseTreeItem);
        releaseTreeItem.setExpanded(true);
        releaseTreeItem.getChildren().add(releaseDatabasesTreeItem);
        releaseTreeItem.getChildren().add(releaseGuessingTreeItem);
        releaseTreeItem.getChildren().add(releaseCompatibilityTreeItem);
        // Correction
        root.getChildren().add(correctionTreeItem);
        correctionTreeItem.setExpanded(true);
        correctionTreeItem.getChildren().add(correctionSubtitleLanguageTreeItem);
        // Naming
        root.getChildren().add(namingTreeItem);
        // File transformation
        root.getChildren().add(filetransformationTreeItem);
        // UI
        root.getChildren().add(uiTreeItem);

        sectionSelectionTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends TreeItem<SettingsSection>> observable, TreeItem<SettingsSection> oldValue, TreeItem<SettingsSection> newValue) -> {
                    if (newValue != null) {
                        showSection(newValue.getValue().getName());
                    }
                    else {
                        // no section was selected -> clear
                        sectionRootPane.getChildren().clear();
                    }
                });

        // Select the first section
        sectionSelectionTreeView.getSelectionModel().selectFirst();
    }

    private void initBottomButtonPane() {
        saveBtn.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(SettingsController.SETTINGS.changedProperty(), customSettingsExist, defaultSettingsLoaded);
            }

            @Override
            protected boolean computeValue() {
                // disable if nothing has changed and there exist custom settings and the default settings were not loaded
                return !SettingsController.SETTINGS.changed() && customSettingsExist.get() && !defaultSettingsLoaded.get();
            }
        });
        saveBtn.setOnAction((ActionEvent e) -> confirmSaveSettings());

        restoreLastSavedBtn.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(SettingsController.SETTINGS.changedProperty(), customSettingsExist, defaultSettingsLoaded);
            }

            @Override
            protected boolean computeValue() {
                // disable if nothing has changed or no custom settings exist to restore
                return !SettingsController.SETTINGS.changed() && !defaultSettingsLoaded.get() || !customSettingsExist.get();
            }
        });
        restoreLastSavedBtn.setOnAction((ActionEvent e) -> confirmRestoreLastSavedSettings());

        restoreDefaultsBtn.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(SettingsController.SETTINGS.changedProperty(), defaultSettingsLoaded);
            }

            @Override
            protected boolean computeValue() {
                // disable if nothing has changed and the default settings were not loaded
                return !SettingsController.SETTINGS.changed() && defaultSettingsLoaded.get();
            }
        });
        restoreDefaultsBtn.setOnAction((ActionEvent e) -> confirmRestoreDefaultSettings());
    }

    private void showSection(String sectionName) {
        SettingsSection section = sections.get(sectionName);
        if (section != null && section.hasController()) {
            if (section.isControllerLoaded()) {
                sectionRootPane.getChildren().setAll(section.getController().getContentPane());
            }
            else {
                sectionRootPane.getChildren().setAll(createLoadingIndicator());
                execute(createLoadSectionControllerTask(section));
            }
        }
        else {
            // no matching section or section has no controller (empty section)
            // -> clear
            sectionRootPane.getChildren().clear();
        }
    }

    private StackPane createLoadingIndicator() {
        StackPane loadingPane = new StackPane();
        AnchorPane.setTopAnchor(loadingPane, 0.0d);
        AnchorPane.setRightAnchor(loadingPane, 0.0d);
        AnchorPane.setBottomAnchor(loadingPane, 0.0d);
        AnchorPane.setLeftAnchor(loadingPane, 0.0d);
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxHeight(50d);
        loadingIndicator.setMaxWidth(50d);
        // "Loading ..."
        StackPane.setAlignment(loadingIndicator, Pos.CENTER);
        loadingPane.getChildren().add(loadingIndicator);
        return loadingPane;
    }

    private Task<AbstractSettingsSectionController> createLoadSectionControllerTask(final SettingsSection settingsSection) {
        return new Task<AbstractSettingsSectionController>() {
            {
                updateTitle("Load settings section \"" + settingsSection.label + "\"");
            }

            @Override
            protected AbstractSettingsSectionController call() throws Exception {
                return settingsSection.loadController();
            }

            @Override
            protected void succeeded() {
                sectionRootPane.getChildren().setAll(getValue().getContentPane());
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                log.error("Loading of settings section " + settingsSection + " failed", e);
                sectionRootPane.getChildren().setAll(new Label("Loading of settings section " + settingsSection + " failed: " + e));
            }

            @Override
            protected void cancelled() {
                log.error("Loading of settings section " + settingsSection + " was cancelled");
                sectionRootPane.getChildren().setAll(new Label("Loading of settings section " + settingsSection + " was cancelled"));
            }
        };
    }

    public Map<String, SettingsSection> getSections() {
        return sections;
    }

    public void selectSection(String section) {
        if (section == null) {
            sectionSelectionTreeView.getSelectionModel().clearSelection();
        }
        TreeItem<SettingsSection> itemToSelect = FxNodes.findTreeItem(sectionSelectionTreeView.getRoot(),
                (TreeItem<SettingsSection> item) -> item.getValue() != null ? section.equals(item.getValue().getName()) : false);
        sectionSelectionTreeView.getSelectionModel().select(itemToSelect);
    }

    public void confirmSaveSettings() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        FxNodes.fixAlertHeight(alert);
        alert.initOwner(getPrimaryStage());
        alert.setTitle("Save settings?");
        alert.setHeaderText("Do you want to save the current settings?");
        alert.setContentText("The current settings will be stored at " + getCustomSettingsPath() + ".");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            saveSettings();
        }
    }

    private void confirmSaveUnsavedSettings() throws ConfigurationException {
        if (defaultSettingsLoaded.get() || SettingsController.SETTINGS.changed()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            FxNodes.fixAlertHeight(alert);
            alert.initOwner(getPrimaryStage());
            alert.setTitle("Save settings?");
            alert.setHeaderText("Do you want to save the current settings?");
            alert.setContentText("You have unsaved changes in the settings. Do you want to save them at " + getCustomSettingsPath() + "?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES) {
                saveSettings();
            }
            else {
                log.debug("User chose not to save changed settings");
            }
        }
    }

    public void confirmRestoreLastSavedSettings() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        FxNodes.fixAlertHeight(alert);
        alert.initOwner(getPrimaryStage());
        alert.setTitle("Restore last saved settings?");
        alert.setHeaderText("Do you want restore the last saved settings?");
        alert.setContentText("The current settings will be replaced with the content of " + getCustomSettingsPath() + ".");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            loadSettings();
        }
    }

    public void confirmRestoreDefaultSettings() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        FxNodes.fixAlertHeight(alert);
        alert.initOwner(getPrimaryStage());
        alert.setTitle("Restore default settings?");
        alert.setHeaderText("Do you want to restore the default settings?");
        alert.setContentText(
                "The current settings will be replaced with the default settings.\nBut nothing will be saved to file until you choose to do so. You can always return to the last saved settings.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES) {
            loadDefaultSettings();
        }
    }

    public void saveSettings() {
        try {
            Path settingsFile = getCustomSettingsPath();

            // May create parent dir
            Files.createDirectories(settingsFile.getParent());

            SettingsController.SETTINGS.save(settingsFile);
            defaultSettingsLoaded.set(false);
            customSettingsExist.set(true);
        }
        catch (ConfigurationException | IOException e) {
            FxUtil.createExceptionAlert(null, "Failed to save settings", "Exception while saving settings to " + CUSTOM_SETTINGS_FILENAME, e).showAndWait();
        }
    }

    public void loadSettings() {
        Path settingsFile = getCustomSettingsPath();
        if (Files.exists(settingsFile, LinkOption.NOFOLLOW_LINKS)) {
            try {
                loadCustomSettings(settingsFile);
                return;
            }
            catch (Exception e) {
                log.error("Failed to load custom settings from " + settingsFile + ". Default settings will be used", e);
                FxUtil.createExceptionAlert(null,
                        "Failed to load custom settings",
                        "Failed to load custom settings from " + settingsFile
                                + ". Default settings will be used.\nIf you would like to try to fix the custom settings, close the application without saving the settings, edit the settings file and try again.",
                        e).showAndWait();
            }
        }
        else {
            log.debug("No custom settings found at {}. Default settings will be used", settingsFile);
        }

        // if did not hit "return;", load default settings
        loadDefaultSettings();
    }

    public void loadDefaultSettings() {
        try {
            // A resource has to be loaded via URL
            // because building a Path for a JAR intern resource file results in a FileSystem exception.
            SettingsController.SETTINGS.load(getDefaultSettingsUrl());
            defaultSettingsLoaded.set(true);
        }
        catch (Exception e) {
            log.error("Exception while loading default settings", e);
            FxUtil.createExceptionAlert(null, "Failed to load default settings", "Failed to load default settings from resource " + DEFAULT_SETTINGS_FILENAME, e).showAndWait();
        }
    }

    public void loadCustomSettings(Path file) throws ConfigurationException {
        log.debug("Loading custom settings from {}", file);
        SettingsController.SETTINGS.load(file);
        defaultSettingsLoaded.set(false);
        customSettingsExist.set(true);
    }

    public URL getDefaultSettingsUrl() {
        return Resources.getResource(DEFAULT_SETTINGS_FILENAME);
    }

    public Path getCustomSettingsPath() {
        Path localConfigDir = WatcherApp.getLocalConfigDirectory();
        return localConfigDir.resolve(CUSTOM_SETTINGS_FILENAME).toAbsolutePath();
    }

    @Override
    public void shutdown() throws Exception {
        confirmSaveUnsavedSettings();
    }

    public static class SettingsSection {
        private final String                                          name;
        private Supplier<? extends AbstractSettingsSectionController> controllerConstructor;
        private AbstractSettingsSectionController                     controller;
        private String                                                label;
        private ImageView                                             image;
        private String                                                fxml;
        private String                                                resourceBundle;

        public SettingsSection(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setControllerConstructor(Supplier<? extends AbstractSettingsSectionController> controllerConstructor) {
            this.controllerConstructor = controllerConstructor;
        }

        public Supplier<? extends AbstractSettingsSectionController> getControllerConstructor() {
            return controllerConstructor;
        }

        public boolean hasController() {
            return controllerConstructor != null;
        }

        /**
         * 
         * @return the controller or null if it was not loaded yet
         */
        public AbstractSettingsSectionController getController() {
            return controller;
        }

        /**
         * Should be executed in a background thread if controller wasn't loaded yet.
         * 
         * @return
         */
        public AbstractSettingsSectionController loadController() throws IOException {
            controller = controllerConstructor.get();
            loadController(fxml, resourceBundle, controller);
            return controller;
        }

        public boolean isControllerLoaded() {
            return controller != null;
        }

        private void loadController(String fxmlFilename, String resourceBaseName, AbstractSettingsSectionController ctrl) throws IOException {
            Node sectionNode = FxIO.loadView(fxmlFilename, ctrl, resourceBaseName, Locale.ENGLISH);
            AnchorPane.setTopAnchor(sectionNode, 0.0d);
            AnchorPane.setRightAnchor(sectionNode, 0.0d);
            AnchorPane.setBottomAnchor(sectionNode, 0.0d);
            AnchorPane.setLeftAnchor(sectionNode, 0.0d);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public ImageView getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = new ImageView(FxIO.loadImg(image));
        }

        public String getFxml() {
            return fxml;
        }

        public void setFxml(String fxml) {
            this.fxml = fxml;
        }

        public String getResourceBundle() {
            return resourceBundle;
        }

        public void setResourceBundle(String resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(SettingsSection.class).add("name", name).toString();
        }
    }

    private static class SettingsSectionTreeCell extends TreeCell<SettingsSection> {
        @Override
        protected void updateItem(SettingsSection item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            }
            else {
                setText(item.getLabel());
                setGraphic(item.getImage());
            }
        }
    }
}
