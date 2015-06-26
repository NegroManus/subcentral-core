package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ReleaseSettingsController extends AbstractSettingsSectionController
{
    @FXML
    private GridPane  releaseSettingsPane;
    @FXML
    private TextField metaTagsTextField;

    public ReleaseSettingsController(SettingsController settingsController)
    {
	super(settingsController);
    }

    @Override
    public GridPane getSectionRootPane()
    {
	return releaseSettingsPane;
    }

    @Override
    protected void doInitialize() throws Exception
    {
	final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

	// Meta tags
	SubCentralFxUtil.bindTagsToTextField(metaTagsTextField, settings.releaseMetaTagsProperty());
	metaTagsTextField.setPromptText(SubCentralFxUtil.DEFAULT_TAGS_PROMPT_TEXT);
    }
}
