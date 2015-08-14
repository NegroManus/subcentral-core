package de.subcentral.watcher.controller.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Resources;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class SettingsController extends AbstractController
{
    private static final Logger log = LogManager.getLogger(SettingsController.class);

    private static final String	SETTINGS_FILE	      = "watcher-settings.xml";
    private static final String	DEFAULT_SETTINGS_FILE = "watcher-settings-default.xml";

    public static final String WATCH_SECTION			       = "watch";
    public static final String PARSING_SECTION			       = "parsing";
    public static final String RELEASE_SECTION			       = "release";
    public static final String RELEASE_DBS_SECTION		       = "release.dbs";
    public static final String RELEASE_GUESSING_SECTION		       = "release.guessing";
    public static final String RELEASE_COMPATIBILITY_SECTION	       = "release.compatibility";
    public static final String STANDARDIZING_SECTION		       = "standardizing";
    public static final String STANDARDIZING_SUBTITLE_LANGUAGE_SECTION = "standardizing.subtitleLanguage";
    public static final String NAMING_SECTION			       = "naming";
    public static final String FILE_TRANSFORMATION_SECTION	       = "filetransformation";
    public static final String UI_SECTION			       = "ui";

    // Controllers
    private final MainController       mainController;
    private final Map<String, Section> sections;

    // View
    @FXML
    private TreeView<Section> sectionSelectionTreeView;
    @FXML
    private AnchorPane	      sectionRootPane;
    @FXML
    private Button	      saveBtn;
    @FXML
    private Button	      restoreLastSavedBtn;
    @FXML
    private Button	      restoreDefaultsBtn;

    private BooleanProperty defaultSettingsLoaded = new SimpleBooleanProperty();
    private BooleanProperty customSettingsExist	  = new SimpleBooleanProperty();

    public SettingsController(MainController mainController) throws Exception
    {
	this.mainController = mainController;
	sections = initSections();
    }

    private Map<String, Section> initSections()
    {
	Map<String, Section> ctrls = new HashMap<>();

	Section watchSection = new Section(WATCH_SECTION);
	watchSection.setLabel("Watch");
	watchSection.setImage("iris_16.png");
	watchSection.setControllerConstructor(() -> new WatchSettingsController(this));
	watchSection.setFxml("WatchSettingsView.fxml");
	ctrls.put(watchSection.getName(), watchSection);

	Section parsingSection = new Section(PARSING_SECTION);
	parsingSection.setLabel("Parsing");
	parsingSection.setImage("file_search_16.png");
	parsingSection.setControllerConstructor(() -> new ParsingSettingsController(this));
	parsingSection.setFxml("ParsingSettingsView.fxml");
	ctrls.put(parsingSection.getName(), parsingSection);

	Section releaseSection = new Section(RELEASE_SECTION);
	releaseSection.setLabel("Release");
	releaseSection.setImage("release_16.png");
	releaseSection.setControllerConstructor(() -> new ReleaseSettingsController(this));
	releaseSection.setFxml("ReleaseSettingsView.fxml");
	ctrls.put(releaseSection.getName(), releaseSection);

	Section releaseDbsSection = new Section(RELEASE_DBS_SECTION);
	releaseDbsSection.setLabel("Databases");
	releaseDbsSection.setImage("database_16.png");
	releaseDbsSection.setControllerConstructor(() -> new ReleaseDbsSettingsController(this));
	releaseDbsSection.setFxml("ReleaseDbsSettingsView.fxml");
	ctrls.put(releaseDbsSection.getName(), releaseDbsSection);

	Section releaseGuessingSection = new Section(RELEASE_GUESSING_SECTION);
	releaseGuessingSection.setLabel("Guessing");
	releaseGuessingSection.setImage("idea_16.png");
	releaseGuessingSection.setControllerConstructor(() -> new ReleaseGuessingSettingsController(this));
	releaseGuessingSection.setFxml("ReleaseGuessingSettingsView.fxml");
	ctrls.put(releaseGuessingSection.getName(), releaseGuessingSection);

	Section releaseCompatibilitySection = new Section(RELEASE_COMPATIBILITY_SECTION);
	releaseCompatibilitySection.setLabel("Compatibility");
	releaseCompatibilitySection.setImage("couple_16.png");
	releaseCompatibilitySection.setControllerConstructor(() -> new ReleaseCompatibilitySettingsController(this));
	releaseCompatibilitySection.setFxml("ReleaseCompatibilitySettingsView.fxml");
	ctrls.put(releaseCompatibilitySection.getName(), releaseCompatibilitySection);

	Section standardizingSection = new Section(STANDARDIZING_SECTION);
	standardizingSection.setLabel("Correction");
	standardizingSection.setImage("edit_16.png");
	standardizingSection.setControllerConstructor(() -> new CorrectionSettingsController(this));
	standardizingSection.setFxml("CorrectionSettingsView.fxml");
	ctrls.put(standardizingSection.getName(), standardizingSection);

	Section standardizingSubtitleLanguageSection = new Section(STANDARDIZING_SUBTITLE_LANGUAGE_SECTION);
	standardizingSubtitleLanguageSection.setLabel("Subtitle language");
	standardizingSubtitleLanguageSection.setImage("usa_flag_16.png");
	standardizingSubtitleLanguageSection.setControllerConstructor(() -> new SubtitleLanguageCorrectionSettingsController(this));
	standardizingSubtitleLanguageSection.setFxml("SubtitleLanguageCorrectionSettingsView.fxml");
	ctrls.put(standardizingSubtitleLanguageSection.getName(), standardizingSubtitleLanguageSection);

	Section namingSection = new Section(NAMING_SECTION);
	namingSection.setLabel("Naming");
	namingSection.setImage("font_16.png");
	namingSection.setControllerConstructor(() -> new NamingSettingsController(this));
	namingSection.setFxml("NamingSettingsView.fxml");
	ctrls.put(namingSection.getName(), namingSection);

	Section fileTransformationSection = new Section(FILE_TRANSFORMATION_SECTION);
	fileTransformationSection.setLabel("File transformation");
	fileTransformationSection.setImage("copy_file_16.png");
	fileTransformationSection.setControllerConstructor(() -> new FileTransformationSettingsController(this));
	fileTransformationSection.setFxml("FileTransformationSettingsView.fxml");
	ctrls.put(fileTransformationSection.getName(), fileTransformationSection);

	Section uiSection = new Section(UI_SECTION);
	uiSection.setLabel("User interface");
	uiSection.setImage("ui_16.png");
	uiSection.setControllerConstructor(() -> new UserInterfaceSettingsController(this));
	uiSection.setFxml("UserInterfaceSettingsView.fxml");
	ctrls.put(uiSection.getName(), uiSection);

	return ctrls;
    }

    @Override
    protected void doInitialize() throws Exception
    {
	loadSettings();
	initSettingsTree();
	initBottomButtonPane();
    }

    private void initSettingsTree()
    {
	final TreeItem<Section> root = new TreeItem<>();
	sectionSelectionTreeView.setRoot(root);
	sectionSelectionTreeView.setCellFactory((TreeView<Section> param) -> {
	    return new TreeCell<Section>()
	    {
		@Override
		protected void updateItem(Section item, boolean empty)
		{
		    super.updateItem(item, empty);

		    if (empty || item == null)
		    {
			setText("");
			setGraphic(null);
		    }
		    else
		    {
			setText(item.getLabel());
			setGraphic(item.getImage());
		    }
		}
	    };
	});

	TreeItem<Section> watchTreeItem = new TreeItem<>(sections.get(WATCH_SECTION));

	TreeItem<Section> parsingTreeItem = new TreeItem<>(sections.get(PARSING_SECTION));

	TreeItem<Section> releaseTreeItem = new TreeItem<>(sections.get(RELEASE_SECTION));
	TreeItem<Section> releaseDatabasesTreeItem = new TreeItem<>(sections.get(RELEASE_DBS_SECTION));
	TreeItem<Section> releaseGuessingTreeItem = new TreeItem<>(sections.get(RELEASE_GUESSING_SECTION));
	TreeItem<Section> releaseCompatibilityTreeItem = new TreeItem<>(sections.get(RELEASE_COMPATIBILITY_SECTION));

	TreeItem<Section> standardizingTreeItem = new TreeItem<>(sections.get(STANDARDIZING_SECTION));
	TreeItem<Section> subtitleLanguageStandardizingTreeItem = new TreeItem<>(sections.get(STANDARDIZING_SUBTITLE_LANGUAGE_SECTION));

	TreeItem<Section> namingTreeItem = new TreeItem<>(sections.get(NAMING_SECTION));

	TreeItem<Section> filetransformationTreeItem = new TreeItem<>(sections.get(FILE_TRANSFORMATION_SECTION));

	TreeItem<Section> uiTreeItem = new TreeItem<>(sections.get(UI_SECTION));

	// Watch
	root.getChildren().add(watchTreeItem);
	// Parsing
	root.getChildren().add(parsingTreeItem);
	// Release
	root.getChildren().add(releaseTreeItem);
	releaseTreeItem.getChildren().add(releaseDatabasesTreeItem);
	releaseTreeItem.getChildren().add(releaseGuessingTreeItem);
	releaseTreeItem.getChildren().add(releaseCompatibilityTreeItem);
	// Standardizing
	root.getChildren().add(standardizingTreeItem);
	standardizingTreeItem.getChildren().add(subtitleLanguageStandardizingTreeItem);
	// Naming
	root.getChildren().add(namingTreeItem);
	// File transformation
	root.getChildren().add(filetransformationTreeItem);
	// UI
	root.getChildren().add(uiTreeItem);

	sectionSelectionTreeView.getSelectionModel()
		.selectedItemProperty()
		.addListener((ObservableValue<? extends TreeItem<Section>> observable, TreeItem<Section> oldValue, TreeItem<Section> newValue) -> {
		    if (newValue != null)
		    {
			showSection(newValue.getValue().getName());
		    }
		    else
		    {
			// no section was selected -> clear
			sectionRootPane.getChildren().clear();
		    }
		});

	// Select the first section
	sectionSelectionTreeView.getSelectionModel().selectFirst();
    }

    private void initBottomButtonPane()
    {
	saveBtn.disableProperty().bind(new BooleanBinding()
	{
	    {
		super.bind(WatcherSettings.INSTANCE.changedProperty(), customSettingsExist, defaultSettingsLoaded);
	    }

	    @Override
	    protected boolean computeValue()
	    {
		// disable if nothing has changed and there exist custom settings and the default settings were not loaded
		return !WatcherSettings.INSTANCE.getChanged() && customSettingsExist.get() && !defaultSettingsLoaded.get();
	    }
	});
	saveBtn.setOnAction((ActionEvent e) -> confirmSaveSettings());

	restoreLastSavedBtn.disableProperty().bind(new BooleanBinding()
	{
	    {
		super.bind(WatcherSettings.INSTANCE.changedProperty(), customSettingsExist, defaultSettingsLoaded);
	    }

	    @Override
	    protected boolean computeValue()
	    {
		// disable if nothing has changed or no custom settings exist to restore
		return !WatcherSettings.INSTANCE.getChanged() && !defaultSettingsLoaded.get() || !customSettingsExist.get();
	    }
	});
	restoreLastSavedBtn.setOnAction((ActionEvent e) -> confirmRestoreLastSavedSettings());

	restoreDefaultsBtn.disableProperty().bind(new BooleanBinding()
	{
	    {
		super.bind(WatcherSettings.INSTANCE.changedProperty(), defaultSettingsLoaded);
	    }

	    @Override
	    protected boolean computeValue()
	    {
		// disable if nothing has changed and the default settings were not loaded
		return !WatcherSettings.INSTANCE.getChanged() && defaultSettingsLoaded.get();
	    }
	});
	restoreDefaultsBtn.setOnAction((ActionEvent e) -> confirmRestoreDefaultSettings());
    }

    private void showSection(String sectionName)
    {
	Section section = sections.get(sectionName);
	if (section != null && section.hasController())
	{
	    if (section.isControllerLoaded())
	    {
		sectionRootPane.getChildren().setAll(section.getController().getSectionRootPane());
	    }
	    else
	    {
		sectionRootPane.getChildren().setAll(createLoadingIndicator());
		mainController.getCommonExecutor().submit(createLoadSectionControllerTask(section));
	    }
	}
	else
	{
	    // no matching section or section has no controller (empty section)
	    // -> clear
	    sectionRootPane.getChildren().clear();
	}
    }

    private StackPane createLoadingIndicator()
    {
	StackPane loadingPane = new StackPane();
	AnchorPane.setTopAnchor(loadingPane, 0.0d);
	AnchorPane.setRightAnchor(loadingPane, 0.0d);
	AnchorPane.setBottomAnchor(loadingPane, 0.0d);
	AnchorPane.setLeftAnchor(loadingPane, 0.0d);
	ProgressIndicator loadingIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
	loadingIndicator.setMaxHeight(50d);
	loadingIndicator.setMaxWidth(50d);
	// "Loading ..."
	StackPane.setAlignment(loadingIndicator, Pos.CENTER);
	loadingPane.getChildren().add(loadingIndicator);
	return loadingPane;
    }

    private Task<AbstractSettingsSectionController> createLoadSectionControllerTask(final Section section)
    {
	return new Task<AbstractSettingsSectionController>()
	{
	    @Override
	    protected AbstractSettingsSectionController call() throws Exception
	    {
		return section.loadController();
	    }

	    @Override
	    protected void succeeded()
	    {
		log.debug("Loaded settings section {}", section);
		sectionRootPane.getChildren().setAll(getValue().getSectionRootPane());
	    }

	    @Override
	    protected void failed()
	    {
		sectionRootPane.getChildren().clear();
		log.error("Loading of settings section " + section + "failed", getException());
	    }

	    @Override
	    protected void cancelled()
	    {
		sectionRootPane.getChildren().clear();
		log.warn("Loading of settings section {} was cancelled", section);
	    }
	};
    }

    public MainController getMainController()
    {
	return mainController;
    }

    public void selectSection(String section)
    {
	if (section == null)
	{
	    sectionSelectionTreeView.getSelectionModel().clearSelection();
	}
	TreeItem<Section> itemToSelect = FxUtil.findTreeItem(sectionSelectionTreeView.getRoot(),
		(TreeItem<Section> item) -> item.getValue() != null ? section.equals(item.getValue().getName()) : false);
	sectionSelectionTreeView.getSelectionModel().select(itemToSelect);
    }

    public void confirmSaveSettings()
    {
	try
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Save settings?");
	    alert.setHeaderText("Do you want to save the current settings?");
	    alert.setContentText("The current settings will be stored in the settings file.");
	    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.get() == ButtonType.YES)
	    {
		saveSettings();
	    }
	}
	catch (Exception e1)
	{
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    public void confirmRestoreLastSavedSettings()
    {
	try
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    alert.setTitle("Restore last saved settings?");
	    alert.setHeaderText("Do you want restore the last saved settings?");
	    alert.setContentText("The current settings will be replaced with the content of the settings file.");
	    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.get() == ButtonType.YES)
	    {
		loadSettings();
	    }
	}
	catch (Exception e1)
	{
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    public void confirmRestoreDefaultSettings()
    {
	try
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    // alert.getDialogPane().setPrefWidth(600d);
	    alert.setTitle("Restore defaults?");
	    alert.setHeaderText("Do you want to restore the default settings?");
	    alert.setContentText(
		    "The current settings will be replaced with the default settings.\nBut nothing will be saved until you choose to do so. You can always return to the last saved settings.");
	    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.get() == ButtonType.YES)
	    {
		loadDefaultSettings();
	    }
	}
	catch (Exception e1)
	{
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
    }

    public void saveSettings() throws ConfigurationException, IOException
    {
	WatcherSettings.INSTANCE.save(Paths.get(SETTINGS_FILE));
	defaultSettingsLoaded.set(false);
	customSettingsExist.set(true);
    }

    public void loadSettings() throws Exception
    {
	Path settingsFile = Paths.get(SETTINGS_FILE).toAbsolutePath();
	if (Files.exists(settingsFile, LinkOption.NOFOLLOW_LINKS))
	{
	    try
	    {
		loadCustomSettings(settingsFile);
		return;
	    }
	    catch (Exception e)
	    {
		log.error("Failed to load custom settings from " + settingsFile + ". Will load default settings", e);
	    }
	}
	else
	{
	    log.debug("No custom settings found at {}. Will load default settings", settingsFile);
	}
	loadDefaultSettings();
    }

    public void loadDefaultSettings() throws Exception
    {
	WatcherSettings.INSTANCE.load(Resources.getResource(DEFAULT_SETTINGS_FILE));
	defaultSettingsLoaded.set(true);
    }

    public void loadCustomSettings(Path settingsFile) throws Exception
    {
	log.debug("Loading custom settings from {}", settingsFile);
	WatcherSettings.INSTANCE.load(settingsFile);
	defaultSettingsLoaded.set(false);
	customSettingsExist.set(true);
    }

    private void confirmSaveUnsavedSettings() throws ConfigurationException, IOException
    {
	if (defaultSettingsLoaded.get() || WatcherSettings.INSTANCE.getChanged())
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    // alert.getDialogPane().setPrefWidth(600d);
	    alert.setTitle("Save watcher settings?");
	    alert.setHeaderText("Save watcher settings?");
	    alert.setContentText("You have unsaved changes in the settings. Do you want to save them?");
	    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

	    Optional<ButtonType> result = alert.showAndWait();
	    if (result.get() == ButtonType.YES)
	    {
		saveSettings();
	    }
	    else
	    {
		log.debug("User chose not to save changed settings");
	    }
	}
    }

    @Override
    public void shutdown() throws Exception
    {
	confirmSaveUnsavedSettings();
    }

    public static class Section
    {
	private final String					      name;
	private Supplier<? extends AbstractSettingsSectionController> controllerConstructor;
	private AbstractSettingsSectionController		      controller;
	private String						      label;
	private ImageView					      image;
	private String						      fxml;
	private String						      resourceBundle;

	public Section(String name)
	{
	    this.name = name;
	}

	public String getName()
	{
	    return name;
	}

	public void setControllerConstructor(Supplier<? extends AbstractSettingsSectionController> controllerConstructor)
	{
	    this.controllerConstructor = controllerConstructor;
	}

	public Supplier<? extends AbstractSettingsSectionController> getControllerConstructor()
	{
	    return controllerConstructor;
	}

	public boolean hasController()
	{
	    return controllerConstructor != null;
	}

	/**
	 * 
	 * @return the controller or null if it was not loaded yet
	 */
	public AbstractSettingsSectionController getController()
	{
	    return controller;
	}

	/**
	 * Should be executed in a background thread if controller wasn't loaded yet.
	 * 
	 * @return
	 */
	public AbstractSettingsSectionController loadController() throws IOException
	{
	    controller = controllerConstructor.get();
	    loadController(fxml, resourceBundle, controller);
	    return controller;
	}

	public boolean isControllerLoaded()
	{
	    return controller != null;
	}

	private void loadController(String fxmlFilename, String resourceBaseName, AbstractSettingsSectionController ctrl) throws IOException
	{
	    Node sectionNode = FxUtil.loadFromFxml(fxmlFilename, resourceBaseName, Locale.ENGLISH, ctrl);
	    AnchorPane.setTopAnchor(sectionNode, 0.0d);
	    AnchorPane.setRightAnchor(sectionNode, 0.0d);
	    AnchorPane.setBottomAnchor(sectionNode, 0.0d);
	    AnchorPane.setLeftAnchor(sectionNode, 0.0d);
	}

	public String getLabel()
	{
	    return label;
	}

	public void setLabel(String label)
	{
	    this.label = label;
	}

	public ImageView getImage()
	{
	    return image;
	}

	public void setImage(String image)
	{
	    this.image = new ImageView(FxUtil.loadImg(image));
	}

	public String getFxml()
	{
	    return fxml;
	}

	public void setFxml(String fxml)
	{
	    this.fxml = fxml;
	}

	public String getResourceBundle()
	{
	    return resourceBundle;
	}

	public void setResourceBundle(String resourceBundle)
	{
	    this.resourceBundle = resourceBundle;
	}
    }
}
