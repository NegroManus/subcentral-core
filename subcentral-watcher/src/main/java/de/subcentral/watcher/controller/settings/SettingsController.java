package de.subcentral.watcher.controller.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Resources;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    private static final String	SETTINGS_FILE	      = "watcher-settings.xml";
    private static final String	DEFAULT_SETTINGS_FILE = "watcher-settings-default.xml";
    private static final Logger	log		      = LogManager.getLogger(SettingsController.class);

    public static final String	WATCH_SECTION	    = "watch";
    private static final String	WATCH_SECTION_LBL   = "Watch";
    public static final String	PARSING_SECTION	    = "parsing";
    private static final String	PARSING_SECTION_LBL = "Parsing";

    public static final String	RELEASE_SECTION				    = "release";
    private static final String	RELEASE_SECTION_LBL			    = "Release";
    public static final String	RELEASE_DBS_SECTION			    = "release.dbs";
    private static final String	RELEASE_DBS_SECTION_LBL			    = "Databases";
    public static final String	RELEASE_GUESSING_SECTION		    = "release.guessing";
    private static final String	RELEASE_GUESSING_SECTION_LBL		    = "Guessing";
    public static final String	RELEASE_COMPATIBILITY_SECTION		    = "release.compatibility";
    private static final String	RELEASE_COMPATIBILITY_SECTION_LBL	    = "Compatibility";
    public static final String	STANDARDIZING_SECTION			    = "standardizing";
    private static final String	STANDARDIZING_SECTION_LBL		    = "Standardizing";
    public static final String	STANDARDIZING_PRE_METADATADB_SECTION	    = "standardizing.premetadb";
    private static final String	STANDARDIZING_PRE_METADATADB_SECTION_LBL    = "Pre metadata database";
    public static final String	STANDARDIZING_POST_METADATADB_SECTION	    = "standardizing.postmetadb";
    private static final String	STANDARDIZING_POST_METADATADB_SECTION_LBL   = "Post metadata database";
    public static final String	STANDARDIZING_SUBTITLE_LANGUAGE_SECTION	    = "standardizing.subtitleLanguage";
    private static final String	STANDARDIZING_SUBTITLE_LANGUAGE_SECTION_LBL = "Subtitle language";
    public static final String	NAMING_SECTION				    = "naming";
    private static final String	NAMING_SECTION_LBL			    = "Naming";
    public static final String	FILE_TRANSFORMATION_SECTION		    = "filetransformation";
    private static final String	FILE_TRANSFORMATION_SECTION_LBL		    = "File transformation";

    // Controlling properties
    private final MainController			    mainController;
    private WatchSettingsController			    watchSettingsController;
    private ParsingSettingsController			    parsingSettingsController;
    private ReleaseSettingsController			    releaseSettingsController;
    private ReleaseDbsSettingsController		    releaseDbsSettingsController;
    private ReleaseGuessingSettingsController		    releaseGuessingSettingsController;
    private ReleaseCompatibilitySettingsController	    releaseCompatibilitySettingsController;
    private StandardizingSettingsController		    preMetaDbStandardizingSettingsController;
    private StandardizingSettingsController		    postMetaDbStandardizingSettingsController;
    private SubtitleLanguageStandardizingSettingsController subtitleLanguageStandardizingSettingsController;
    private NamingSettingsController			    namingSettingsController;
    private FileTransformationSettingsController	    fileTransformationSettingsController;

    // View
    @FXML
    private TreeView<SettingsSection> settingsSectionsTreeView;
    @FXML
    private AnchorPane		      settingsSectionRootPane;

    public SettingsController(MainController mainController) throws Exception
    {
	this.mainController = mainController;
    }

    @Override
    protected void doInitialize() throws Exception
    {
	loadSettings();
	initSettingsTree();
    }

    private void initSettingsTree()
    {
	final TreeItem<SettingsSection> root = new TreeItem<>();
	settingsSectionsTreeView.setRoot(root);
	settingsSectionsTreeView.setCellFactory((TreeView<SettingsSection> param) -> {
	    return new TreeCell<SettingsSection>()
	    {
		@Override
		protected void updateItem(SettingsSection item, boolean empty)
		{
		    // calling super here is very important - don't skip this!
		    super.updateItem(item, empty);

		    if (empty || item == null)
		    {
			setText("");
			setGraphic(null);
		    }
		    else
		    {
			setText(item.getLabel());
			if (item.getImage() == null)
			{
			    setGraphic(null);
			}
			else
			{
			    setGraphic(new ImageView(FxUtil.loadImg(item.getImage())));
			}
		    }
		}
	    };
	});

	TreeItem<SettingsSection> watchTreeItem = new TreeItem<>(new SettingsSection(WATCH_SECTION, WATCH_SECTION_LBL, "iris_16.png"));

	TreeItem<SettingsSection> parsingTreeItem = new TreeItem<>(new SettingsSection(PARSING_SECTION, PARSING_SECTION_LBL, "file_search_16.png"));

	TreeItem<SettingsSection> releaseTreeItem = new TreeItem<>(new SettingsSection(RELEASE_SECTION, RELEASE_SECTION_LBL, "archive_16.png"));
	TreeItem<SettingsSection> releaseDatabasesTreeItem = new TreeItem<>(new SettingsSection(RELEASE_DBS_SECTION, RELEASE_DBS_SECTION_LBL, "database_16.png"));
	TreeItem<SettingsSection> releaseGuessingTreeItem = new TreeItem<>(new SettingsSection(RELEASE_GUESSING_SECTION, RELEASE_GUESSING_SECTION_LBL, "idea_16.png"));
	TreeItem<SettingsSection> releaseCompatibilityTreeItem = new TreeItem<>(new SettingsSection(RELEASE_COMPATIBILITY_SECTION, RELEASE_COMPATIBILITY_SECTION_LBL, "couple_16.png"));

	TreeItem<SettingsSection> standardizingTreeItem = new TreeItem<>(new SettingsSection(STANDARDIZING_SECTION, STANDARDIZING_SECTION_LBL, "exchange_16.png"));
	TreeItem<SettingsSection> preMetadataDbStandardizingTreeItem = new TreeItem<>(
		new SettingsSection(STANDARDIZING_PRE_METADATADB_SECTION, STANDARDIZING_PRE_METADATADB_SECTION_LBL, "exchange_16.png"));
	TreeItem<SettingsSection> postMetadataDbStandardizingTreeItem = new TreeItem<>(
		new SettingsSection(STANDARDIZING_POST_METADATADB_SECTION, STANDARDIZING_POST_METADATADB_SECTION_LBL, "exchange_16.png"));
	TreeItem<SettingsSection> subtitleLanguageStandardizingTreeItem = new TreeItem<>(
		new SettingsSection(STANDARDIZING_SUBTITLE_LANGUAGE_SECTION, STANDARDIZING_SUBTITLE_LANGUAGE_SECTION_LBL, "usa_16.png"));

	TreeItem<SettingsSection> namingTreeItem = new TreeItem<>(new SettingsSection(NAMING_SECTION, NAMING_SECTION_LBL, "font_16.png"));

	TreeItem<SettingsSection> filetransformationTreeItem = new TreeItem<>(new SettingsSection(FILE_TRANSFORMATION_SECTION, FILE_TRANSFORMATION_SECTION_LBL, "transform_16.png"));

	root.getChildren().add(watchTreeItem);
	root.getChildren().add(parsingTreeItem);
	// Release
	root.getChildren().add(releaseTreeItem);
	releaseTreeItem.getChildren().add(releaseDatabasesTreeItem);
	releaseTreeItem.getChildren().add(releaseGuessingTreeItem);
	releaseTreeItem.getChildren().add(releaseCompatibilityTreeItem);
	// Standardizing
	root.getChildren().add(standardizingTreeItem);
	standardizingTreeItem.getChildren().add(preMetadataDbStandardizingTreeItem);
	standardizingTreeItem.getChildren().add(postMetadataDbStandardizingTreeItem);
	standardizingTreeItem.getChildren().add(subtitleLanguageStandardizingTreeItem);

	// Naming
	root.getChildren().add(namingTreeItem);
	root.getChildren().add(filetransformationTreeItem);

	settingsSectionsTreeView.getSelectionModel()
		.selectedItemProperty()
		.addListener((ObservableValue<? extends TreeItem<SettingsSection>> observable, TreeItem<SettingsSection> oldValue, TreeItem<SettingsSection> newValue) -> {
		    if (newValue == null)
		    {
			settingsSectionRootPane.getChildren().clear();
			return;
		    }

		    AbstractSettingsSectionController ctrl = null;
		    Callable<AbstractSettingsSectionController> ctrlGetter = null;
		    switch (newValue.getValue().getSection())
		    {
			case WATCH_SECTION:
			    if (watchSettingsController != null)
			    {
				ctrl = watchSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getWatchSettingsController;
			    }
			    break;
			case PARSING_SECTION:
			    if (parsingSettingsController != null)
			    {
				ctrl = parsingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getParsingSettingsController;
			    }
			    break;
			case RELEASE_SECTION:
			    if (releaseSettingsController != null)
			    {
				ctrl = releaseSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getReleaseSettingsController;
			    }
			    break;
			case RELEASE_DBS_SECTION:
			    if (releaseDbsSettingsController != null)
			    {
				ctrl = releaseDbsSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getReleaseDbsSettingsController;
			    }
			    break;
			case RELEASE_GUESSING_SECTION:
			    if (releaseGuessingSettingsController != null)
			    {
				ctrl = releaseGuessingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getReleaseGuessingSettingsController;
			    }
			    break;
			case RELEASE_COMPATIBILITY_SECTION:
			    if (releaseCompatibilitySettingsController != null)
			    {
				ctrl = releaseCompatibilitySettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getReleaseCompatibilitySettingsController;
			    }
			    break;
			case STANDARDIZING_PRE_METADATADB_SECTION:
			    if (preMetaDbStandardizingSettingsController != null)
			    {
				ctrl = preMetaDbStandardizingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getPreMetaDbStandardizingSettingsController;
			    }
			    break;
			case STANDARDIZING_POST_METADATADB_SECTION:
			    if (postMetaDbStandardizingSettingsController != null)
			    {
				ctrl = postMetaDbStandardizingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getPostMetaDbStandardizingSettingsController;
			    }
			    break;
			case STANDARDIZING_SUBTITLE_LANGUAGE_SECTION:
			    if (subtitleLanguageStandardizingSettingsController != null)
			    {
				ctrl = subtitleLanguageStandardizingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getSubtitleLanguageStandardizingSettingsController;
			    }
			    break;
			case NAMING_SECTION:
			    if (namingSettingsController != null)
			    {
				ctrl = namingSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getNamingSettingsController;
			    }
			    break;
			case FILE_TRANSFORMATION_SECTION:
			    if (fileTransformationSettingsController != null)
			    {
				ctrl = fileTransformationSettingsController;
			    }
			    else
			    {
				ctrlGetter = SettingsController.this::getFileTransformationSettingsController;
			    }
			    break;
			default:
			    break;
		    }

		    // If the SectionController is already loaded, just display his SectionPane
		    if (ctrl != null)
		    {
			settingsSectionRootPane.getChildren().setAll(ctrl.getSectionRootPane());
		    }
		    // If the SectionController is not loaded yet but can be loaded, load it in a background thread
		    else if (ctrlGetter != null)
		    {
			settingsSectionRootPane.getChildren().setAll(createLoadingIndicatorNode());
			Task<AbstractSettingsSectionController> loadTask = createLoadSectionControllerTask(ctrlGetter);
			// if still on application startup, do not load in background thread
			if (Thread.currentThread().getName().equals("JavaFX-Launcher"))
			{
			    loadTask.run();
			}
			else
			{
			    mainController.getCommonExecutor().execute(loadTask);
			}
		    }
		    // If there is no matching SectionController for the chosen section, clear the sectionRootPane
		    else
		    {
			settingsSectionRootPane.getChildren().clear();
		    }
		});

	// Select the first
	settingsSectionsTreeView.getSelectionModel().selectFirst();
    }

    public MainController getMainController()
    {
	return mainController;
    }

    public void selectSection(String section)
    {
	if (section == null)
	{
	    settingsSectionsTreeView.getSelectionModel().clearSelection();
	}
	TreeItem<SettingsSection> itemToSelect = FxUtil.findTreeItem(settingsSectionsTreeView.getRoot(),
		(TreeItem<SettingsSection> item) -> item.getValue() != null ? section.equals(item.getValue().getSection()) : false);
	settingsSectionsTreeView.getSelectionModel().select(itemToSelect);
    }

    public void loadSettings() throws Exception
    {
	Path settingsFile = Paths.get(SETTINGS_FILE).toAbsolutePath();
	if (Files.exists(settingsFile, LinkOption.NOFOLLOW_LINKS))
	{
	    try
	    {
		log.debug("Loading custom settings from {}", settingsFile);
		WatcherSettings.INSTANCE.load(settingsFile);
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
    }

    private void maySaveSettings() throws ConfigurationException, IOException
    {
	if (WatcherSettings.INSTANCE.getChanged())
	{
	    Alert alert = new Alert(AlertType.CONFIRMATION);
	    // alert.getDialogPane().setPrefWidth(600d);
	    alert.setTitle("Save watcher settings?");
	    alert.setHeaderText("Save watcher settings?");
	    alert.setContentText("The settings have changed. Do you want to save them?");
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

    public void saveSettings() throws ConfigurationException
    {
	WatcherSettings.INSTANCE.save(Paths.get(SETTINGS_FILE));
    }

    public WatchSettingsController getWatchSettingsController() throws IOException
    {
	if (watchSettingsController == null)
	{
	    watchSettingsController = new WatchSettingsController(this);
	    loadSettingsSectionNode("WatchSettingsView.fxml", null, watchSettingsController);
	}
	return watchSettingsController;
    }

    public ParsingSettingsController getParsingSettingsController() throws IOException
    {
	if (parsingSettingsController == null)
	{
	    parsingSettingsController = new ParsingSettingsController(this);
	    loadSettingsSectionNode("ParsingSettingsView.fxml", null, parsingSettingsController);
	}
	return parsingSettingsController;
    }

    public ReleaseSettingsController getReleaseSettingsController() throws IOException
    {
	if (releaseSettingsController == null)
	{
	    releaseSettingsController = new ReleaseSettingsController(this);
	    loadSettingsSectionNode("ReleaseSettingsView.fxml", null, releaseSettingsController);
	}
	return releaseSettingsController;
    }

    public ReleaseDbsSettingsController getReleaseDbsSettingsController() throws IOException
    {
	if (releaseDbsSettingsController == null)
	{
	    releaseDbsSettingsController = new ReleaseDbsSettingsController(this);
	    loadSettingsSectionNode("ReleaseDbsSettingsView.fxml", null, releaseDbsSettingsController);
	}
	return releaseDbsSettingsController;
    }

    public ReleaseGuessingSettingsController getReleaseGuessingSettingsController() throws IOException
    {
	if (releaseGuessingSettingsController == null)
	{
	    releaseGuessingSettingsController = new ReleaseGuessingSettingsController(this);
	    loadSettingsSectionNode("ReleaseGuessingSettingsView.fxml", null, releaseGuessingSettingsController);
	}
	return releaseGuessingSettingsController;
    }

    public ReleaseCompatibilitySettingsController getReleaseCompatibilitySettingsController() throws IOException
    {
	if (releaseCompatibilitySettingsController == null)
	{
	    releaseCompatibilitySettingsController = new ReleaseCompatibilitySettingsController(this);
	    loadSettingsSectionNode("ReleaseCompatibilitySettingsView.fxml", null, releaseCompatibilitySettingsController);
	}
	return releaseCompatibilitySettingsController;
    }

    public StandardizingSettingsController getPreMetaDbStandardizingSettingsController() throws IOException
    {
	if (preMetaDbStandardizingSettingsController == null)
	{
	    preMetaDbStandardizingSettingsController = new StandardizingSettingsController(this, WatcherSettings.INSTANCE.getProcessingSettings().getPreMetadataDbStandardizers());
	    loadSettingsSectionNode("StandardizingSettingsView.fxml", "PreMetadataDbStandardizingView", preMetaDbStandardizingSettingsController);
	}
	return preMetaDbStandardizingSettingsController;
    }

    public StandardizingSettingsController getPostMetaDbStandardizingSettingsController() throws IOException
    {
	if (postMetaDbStandardizingSettingsController == null)
	{
	    postMetaDbStandardizingSettingsController = new StandardizingSettingsController(this, WatcherSettings.INSTANCE.getProcessingSettings().getPostMetadataStandardizers());
	    loadSettingsSectionNode("StandardizingSettingsView.fxml", "PostMetadataDbStandardizingView", postMetaDbStandardizingSettingsController);
	}
	return postMetaDbStandardizingSettingsController;
    }

    public SubtitleLanguageStandardizingSettingsController getSubtitleLanguageStandardizingSettingsController() throws IOException
    {
	if (subtitleLanguageStandardizingSettingsController == null)
	{
	    subtitleLanguageStandardizingSettingsController = new SubtitleLanguageStandardizingSettingsController(this);
	    loadSettingsSectionNode("SubtitleLanguageStandardizingSettingsView.fxml", null, subtitleLanguageStandardizingSettingsController);
	}
	return subtitleLanguageStandardizingSettingsController;
    }

    public NamingSettingsController getNamingSettingsController() throws IOException
    {
	if (namingSettingsController == null)
	{
	    namingSettingsController = new NamingSettingsController(this);
	    loadSettingsSectionNode("NamingSettingsView.fxml", null, namingSettingsController);
	}
	return namingSettingsController;
    }

    public FileTransformationSettingsController getFileTransformationSettingsController() throws IOException
    {
	if (fileTransformationSettingsController == null)
	{
	    fileTransformationSettingsController = new FileTransformationSettingsController(this);
	    loadSettingsSectionNode("FileTransformationSettingsView.fxml", null, fileTransformationSettingsController);
	}
	return fileTransformationSettingsController;
    }

    private void loadSettingsSectionNode(String fxmlFilename, String resourceBaseName, AbstractSettingsSectionController ctrl) throws IOException
    {
	Node sectionNode = FxUtil.loadFromFxml(fxmlFilename, resourceBaseName, Locale.ENGLISH, ctrl);
	AnchorPane.setTopAnchor(sectionNode, 0.0d);
	AnchorPane.setRightAnchor(sectionNode, 0.0d);
	AnchorPane.setBottomAnchor(sectionNode, 0.0d);
	AnchorPane.setLeftAnchor(sectionNode, 0.0d);
    }

    private StackPane createLoadingIndicatorNode()
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

    private Task<AbstractSettingsSectionController> createLoadSectionControllerTask(final Callable<AbstractSettingsSectionController> ctrlGetter)
    {
	return new Task<AbstractSettingsSectionController>()
	{
	    @Override
	    protected AbstractSettingsSectionController call() throws Exception
	    {
		return ctrlGetter.call();
	    }

	    @Override
	    protected void succeeded()
	    {
		log.debug("Loaded new settings section. Controller={}", getValue());
		settingsSectionRootPane.getChildren().setAll(getValue().getSectionRootPane());
	    }

	    @Override
	    protected void failed()
	    {
		settingsSectionRootPane.getChildren().clear();
		log.error("Loading of settings section failed", getException());
	    }

	    @Override
	    protected void cancelled()
	    {
		settingsSectionRootPane.getChildren().clear();
		log.warn("Loading of settings section was cancelled");
	    }
	};
    }

    @Override
    public void shutdown() throws Exception
    {
	maySaveSettings();
    }

    public static class SettingsSection
    {
	private final String section;
	private final String label;
	private final String image;

	public SettingsSection(String section, String label, String image)
	{
	    this.section = section;
	    this.label = label;
	    this.image = image;
	}

	public String getSection()
	{
	    return section;
	}

	public String getLabel()
	{
	    return label;
	}

	public String getImage()
	{
	    return image;
	}

	@Override
	public String toString()
	{
	    return label;
	}
    }
}
