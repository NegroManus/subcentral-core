package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FXUtil;
import de.subcentral.fx.WatcherDialogs;
import de.subcentral.watcher.settings.WatcherSettings;

public class ReleaseGuessingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane								releaseGuessingSettingsPane;
	@FXML
	private CheckBox								enableGuessingCheckBox;
	@FXML
	private TableView<StandardRelease>				standardReleasesTableView;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesTagsColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesGroupColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesAssumeExistenceColumn;

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
	public GridPane getSectionRootPane()
	{
		return releaseGuessingSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		enableGuessingCheckBox.selectedProperty().bindBidirectional(WatcherSettings.INSTANCE.guessingEnabledProperty());

		// Standard releases
		standardReleasesTableView.setItems(WatcherSettings.INSTANCE.getStandardReleases());

		standardReleasesTagsColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
			return FXUtil.createConstantBinding(Tag.listToString(param.getValue().getStandardRelease().getTags()));
		});
		standardReleasesGroupColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
			return FXUtil.createConstantBinding(Group.toSafeString(param.getValue().getStandardRelease().getGroup()));
		});
		standardReleasesAssumeExistenceColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
			String value;
			switch (param.getValue().getAssumeExistence())
			{
				case IF_NONE_FOUND:
					value = "Only if none found";
					break;
				case ALWAYS:
					value = "Always";
					break;
				default:
					value = param.getValue().getAssumeExistence().name();
			}
			return FXUtil.createConstantBinding(value);
		});

		addStandardReleaseButton.setOnAction((ActionEvent event) -> {
			Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseDefinitionDialog();
			if (result.isPresent())
			{
				standardReleasesTableView.getItems().add(result.get());
			}
		});

		final BooleanBinding noSelection = standardReleasesTableView.getSelectionModel().selectedItemProperty().isNull();

		editStandardReleaseButton.disableProperty().bind(noSelection);
		editStandardReleaseButton.setOnAction((ActionEvent event) -> {
			StandardRelease def = standardReleasesTableView.getSelectionModel().getSelectedItem();
			Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseDefinitionDialog(def);
			if (result.isPresent())
			{
				int selectedIndex = standardReleasesTableView.getSelectionModel().getSelectedIndex();
				standardReleasesTableView.getItems().set(selectedIndex, result.get());
			}
		});

		removeStandardReleaseButton.disableProperty().bind(noSelection);
		removeStandardReleaseButton.setOnAction((ActionEvent event) -> {
			StandardRelease selectedStandardRls = standardReleasesTableView.getSelectionModel().getSelectedItem();
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
			alert.setResizable(true);
			alert.setTitle("Confirmation of removal of standard release");
			alert.setHeaderText("Do you really want to remove this standard release?");
			String contentText = selectedStandardRls.toString();
			alert.setContentText(contentText);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.YES)
			{
				int selectedIndex = standardReleasesTableView.getSelectionModel().getSelectedIndex();
				standardReleasesTableView.getItems().remove(selectedIndex);
			}
		});

		FXUtil.setStandardMouseAndKeyboardSupportForTableView(standardReleasesTableView, editStandardReleaseButton, removeStandardReleaseButton);
	}
}
