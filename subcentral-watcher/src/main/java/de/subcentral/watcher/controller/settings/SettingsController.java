package de.subcentral.watcher.controller.settings;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.controller.MainController;
import de.subcentral.watcher.settings.WatcherSettings;

public class SettingsController extends AbstractController
{
	private static final Logger								log											= LogManager.getLogger(SettingsController.class);

	public static final String								WATCH_SECTION								= "watch";
	public static final String								WATCH_SECTION_LBL							= "Watch";
	public static final String								PARSING_SECTION								= "parsing";
	public static final String								PARSING_SECTION_LBL							= "Parsing";

	public static final String								RELEASE_SECTION								= "release";
	public static final String								RELEASE_SECTION_LBL							= "Release";
	public static final String								RELEASE_DBS_SECTION							= "release.dbs";
	public static final String								RELEASE_DBS_SECTION_LBL						= "Databases";
	public static final String								RELEASE_GUESSING_SECTION					= "release.guessing";
	public static final String								RELEASE_GUESSING_SECTION_LBL				= "Guessing";
	public static final String								RELEASE_COMPATIBILITY_SECTION				= "release.compatibility";
	public static final String								RELEASE_COMPATIBILITY_SECTION_LBL			= "Compatibility";
	public static final String								STANDARDIZING_SECTION						= "standardizing";
	public static final String								STANDARDIZING_SECTION_LBL					= "Standardizing";
	public static final String								STANDARDIZING_PRE_METADATADB_SECTION		= "standardizing.premetadb";
	public static final String								STANDARDIZING_PRE_METADATADB_SECTION_LBL	= "Pre metadata database";
	public static final String								STANDARDIZING_POST_METADATADB_SECTION		= "standardizing.postmetadb";
	public static final String								STANDARDIZING_POST_METADATADB_SECTION_LBL	= "Post metadata database";
	public static final String								STANDARDIZING_SUBTITLE_LANGUAGE_SECTION		= "standardizing.subtitleLanguage";
	public static final String								STANDARDIZING_SUBTITLE_LANGUAGE_SECTION_LBL	= "Subtitle language";
	public static final String								NAMING_SECTION								= "naming";
	public static final String								NAMING_SECTION_LBL							= "Naming";
	public static final String								FILE_TRANSFORMATION_SECTION					= "filetransformation";
	public static final String								FILE_TRANSFORMATION_SECTION_LBL				= "File transformation";

	// Controlling properties
	private final MainController							mainController;
	private WatchSettingsController							watchSettingsController;
	private ParsingSettingsController						parsingSettingsController;
	private ReleaseSettingsController						releaseSettingsController;
	private ReleaseDbsSettingsController					releaseDbsSettingsController;
	private ReleaseGuessingSettingsController				releaseGuessingSettingsController;
	private ReleaseCompatibilitySettingsController			releaseCompatibilitySettingsController;
	private StandardizingSettingsController					preMetaDbStandardizingSettingsController;
	private StandardizingSettingsController					postMetaDbStandardizingSettingsController;
	private SubtitleLanguageStandardizingSettingsController	subtitleLanguageStandardizingSettingsController;
	private NamingSettingsController						namingSettingsController;
	private FileTransformationSettingsController			fileTransformationSettingsController;

	// View
	@FXML
	private TreeView<SettingsSection>						settingsSectionsTreeView;
	@FXML
	private AnchorPane										settingsSectionRootPane;

	public SettingsController(MainController mainController)
	{
		this.mainController = mainController;
	}

	public MainController getMainController()
	{
		return mainController;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		final TreeItem<SettingsSection> root = new TreeItem<>();
		settingsSectionsTreeView.setRoot(root);
		settingsSectionsTreeView.setShowRoot(false);

		TreeItem<SettingsSection> watchTreeItem = new TreeItem<>(new SettingsSection(WATCH_SECTION, WATCH_SECTION_LBL));

		TreeItem<SettingsSection> parsingTreeItem = new TreeItem<>(new SettingsSection(PARSING_SECTION, PARSING_SECTION_LBL));

		TreeItem<SettingsSection> releaseTreeItem = new TreeItem<>(new SettingsSection(RELEASE_SECTION, RELEASE_SECTION_LBL));
		TreeItem<SettingsSection> releaseDatabasesTreeItem = new TreeItem<>(new SettingsSection(RELEASE_DBS_SECTION, RELEASE_DBS_SECTION_LBL));
		TreeItem<SettingsSection> releaseGuessingTreeItem = new TreeItem<>(new SettingsSection(RELEASE_GUESSING_SECTION, RELEASE_GUESSING_SECTION_LBL));
		TreeItem<SettingsSection> releaseCompatibilityTreeItem = new TreeItem<>(new SettingsSection(RELEASE_COMPATIBILITY_SECTION,
				RELEASE_COMPATIBILITY_SECTION_LBL));

		TreeItem<SettingsSection> standardizingTreeItem = new TreeItem<>(new SettingsSection(STANDARDIZING_SECTION, STANDARDIZING_SECTION_LBL));
		TreeItem<SettingsSection> preMetadataDbStandardizingTreeItem = new TreeItem<>(new SettingsSection(STANDARDIZING_PRE_METADATADB_SECTION,
				STANDARDIZING_PRE_METADATADB_SECTION_LBL));
		TreeItem<SettingsSection> postMetadataDbStandardizingTreeItem = new TreeItem<>(new SettingsSection(STANDARDIZING_POST_METADATADB_SECTION,
				STANDARDIZING_POST_METADATADB_SECTION_LBL));
		TreeItem<SettingsSection> subtitleLanguageStandardizingTreeItem = new TreeItem<>(new SettingsSection(STANDARDIZING_SUBTITLE_LANGUAGE_SECTION,
				STANDARDIZING_SUBTITLE_LANGUAGE_SECTION_LBL));

		TreeItem<SettingsSection> namingTreeItem = new TreeItem<>(new SettingsSection(NAMING_SECTION, NAMING_SECTION_LBL));

		TreeItem<SettingsSection> filetransformationTreeItem = new TreeItem<>(new SettingsSection(FILE_TRANSFORMATION_SECTION,
				FILE_TRANSFORMATION_SECTION_LBL));

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
				.addListener((ObservableValue<? extends TreeItem<SettingsSection>> observable, TreeItem<SettingsSection> oldValue,
						TreeItem<SettingsSection> newValue) -> {
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
						mainController.getCommonExecutor().execute(createLoadSectionControllerTask(ctrlGetter));
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
			preMetaDbStandardizingSettingsController = new StandardizingSettingsController(this,
					WatcherSettings.INSTANCE.getPreMetadataDbStandardizers());
			loadSettingsSectionNode("StandardizingSettingsView.fxml", "PreMetadataDbStandardizingView", preMetaDbStandardizingSettingsController);
		}
		return preMetaDbStandardizingSettingsController;
	}

	public StandardizingSettingsController getPostMetaDbStandardizingSettingsController() throws IOException
	{
		if (postMetaDbStandardizingSettingsController == null)
		{
			postMetaDbStandardizingSettingsController = new StandardizingSettingsController(this,
					WatcherSettings.INSTANCE.getPostMetadataStandardizers());
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

	public static class SettingsSection
	{
		private final String	section;
		private final String	label;

		public SettingsSection(String section, String label)
		{
			this.section = section;
			this.label = label;
		}

		public String getSection()
		{
			return section;
		}

		public String getLabel()
		{
			return label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}
}
