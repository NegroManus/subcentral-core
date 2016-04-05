package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class ReleaseGuessingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane								rootPane;
	@FXML
	private CheckBox								enableGuessingCheckBox;
	@FXML
	private TableView<StandardRelease>				standardReleasesTableView;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesTagsColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesGroupColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesScopeColumn;

	@FXML
	private Button									addStandardReleaseButton;
	@FXML
	private Button									editStandardReleaseButton;
	@FXML
	private Button									removeStandardReleaseButton;

	public ReleaseGuessingSettingsController(SettingsController settingsController)
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
		final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

		enableGuessingCheckBox.selectedProperty().bindBidirectional(settings.guessingEnabledProperty());

		// Standard releases
		standardReleasesTableView.setItems(settings.standardReleasesProperty());

		standardReleasesTagsColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			return FxUtil.constantStringBinding(Tag.formatList(param.getValue().getRelease().getTags()));
		});
		standardReleasesGroupColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			return FxUtil.constantStringBinding(Group.toStringNullSafe(param.getValue().getRelease().getGroup()));
		});
		standardReleasesScopeColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			String value;
			switch (param.getValue().getScope())
			{
				case IF_GUESSING:
					value = "If guessing";
					break;
				case ALWAYS:
					value = "Always";
					break;
				default:
					value = param.getValue().getScope().name();
			}
			return FxUtil.constantStringBinding(value);
		});

		addStandardReleaseButton.setOnAction((ActionEvent event) ->
		{
			Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseEditView(settingsController.getMainController().getPrimaryStage());
			FxUtil.handleDistinctAdd(standardReleasesTableView, result);
		});

		final BooleanBinding noSelection = standardReleasesTableView.getSelectionModel().selectedItemProperty().isNull();

		editStandardReleaseButton.disableProperty().bind(noSelection);
		editStandardReleaseButton.setOnAction((ActionEvent event) ->
		{
			StandardRelease def = standardReleasesTableView.getSelectionModel().getSelectedItem();
			Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseEditView(def, settingsController.getMainController().getPrimaryStage());
			FxUtil.handleDistinctEdit(standardReleasesTableView, result);
		});

		removeStandardReleaseButton.disableProperty().bind(noSelection);
		removeStandardReleaseButton.setOnAction((ActionEvent event) ->
		{
			FxUtil.handleConfirmedDelete(standardReleasesTableView, "standard release", SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER);
		});

		FxUtil.setStandardMouseAndKeyboardSupport(standardReleasesTableView, editStandardReleaseButton, removeStandardReleaseButton);
	}
}
