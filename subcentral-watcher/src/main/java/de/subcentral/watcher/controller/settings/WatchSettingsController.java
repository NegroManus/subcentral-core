package de.subcentral.watcher.controller.settings;

import java.io.File;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import de.subcentral.watcher.settings.WatcherSettings;

public class WatchSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane		watchSettingsPane;
	@FXML
	private ListView<Path>	watchDirectoriesListView;
	@FXML
	private Button			addWatchDirectoryButton;
	@FXML
	private Button			removeWatchDirectoryButton;
	@FXML
	private CheckBox		initialScanCheckBox;

	public WatchSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getSectionRootPane()
	{
		return watchSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		watchDirectoriesListView.setItems(WatcherSettings.INSTANCE.watchDirectoriesProperty());

		addWatchDirectoryButton.setOnAction((ActionEvent event) -> {
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle("Add watch directory");
			File selectedDirectory = dirChooser.showDialog(settingsController.getMainController().getPrimaryStage());
			if (selectedDirectory == null)
			{
				return;
			}
			Path newWatchDir = selectedDirectory.toPath();
			if (watchDirectoriesListView.getItems().contains(newWatchDir))
			{
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Chosen directory already on watch list");
				alert.setHeaderText("The chosen directory is already on the watch list.");
				alert.setContentText("The directory " + newWatchDir + " is already on the watch list.");
				alert.show();
				return;
			}
			watchDirectoriesListView.getItems().add(newWatchDir);
		});

		removeWatchDirectoryButton.disableProperty().bind(watchDirectoriesListView.getSelectionModel().selectedItemProperty().isNull());
		removeWatchDirectoryButton.setOnAction((ActionEvent event) -> {
			watchDirectoriesListView.getItems().remove(watchDirectoriesListView.getSelectionModel().getSelectedIndex());
		});

		initialScanCheckBox.selectedProperty().bindBidirectional(WatcherSettings.INSTANCE.initialScanProperty());
	}
}
