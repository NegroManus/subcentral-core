package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ReleaseSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane	rootPane;
	@FXML
	private TextField	metaTagsTextField;

	public ReleaseSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

		// Meta tags
		SubCentralFxUtil.bindTagsToTextField(metaTagsTextField, settings.releaseMetaTagsProperty());
		metaTagsTextField.setPromptText(SubCentralFxUtil.DEFAULT_TAGS_PROMPT_TEXT);
	}
}
