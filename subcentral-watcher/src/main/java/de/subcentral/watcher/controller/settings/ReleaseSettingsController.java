package de.subcentral.watcher.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import de.subcentral.fx.SubCentralFXUtil;
import de.subcentral.watcher.settings.WatcherSettings;

public class ReleaseSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane	releaseSettingsPane;
	@FXML
	private TextField	metaTagsTextField;

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
		// Meta tags
		SubCentralFXUtil.bindTagsToTextField(metaTagsTextField, WatcherSettings.INSTANCE.releaseMetaTagsProperty());
		metaTagsTextField.setPromptText(SubCentralFXUtil.DEFAULT_TAGS_PROMPT_TEXT);
	}
}
