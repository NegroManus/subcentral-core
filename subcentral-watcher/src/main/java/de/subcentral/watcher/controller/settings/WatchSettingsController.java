package de.subcentral.watcher.controller.settings;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.action.AddAction;
import de.subcentral.fx.action.RemoveAction;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;

public class WatchSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane		rootPane;
	@FXML
	private ListView<Path>	watchDirectoriesListView;
	@FXML
	private Button			addWatchDirectoryButton;
	@FXML
	private Button			removeWatchDirectoryButton;
	@FXML
	private CheckBox		initialScanCheckBox;
	@FXML
	private CheckBox		rejectAlreadyProcessedFilesCheckBox;

	public WatchSettingsController(SettingsController settingsController)
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
		watchDirectoriesListView.setItems(SettingsController.SETTINGS.getWatchDirectories().property());

		AddAction<Path> addAction = new AddAction<>(watchDirectoriesListView, () ->
		{
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle("Add watch directory");
			File file = dirChooser.showDialog(getPrimaryStage());
			return file != null ? Optional.of(file.toPath()) : Optional.empty();
		});
		addAction.setComparator(ObjectUtil.getDefaultOrdering());
		addAction.setDistinct(true);
		addAction.setAlreadyExistedInformer(FxActions.createAlreadyExistedInformer(getPrimaryStage(), "watch directory", FxUtil.PATH_STRING_CONVERTER));
		addWatchDirectoryButton.setOnAction(addAction);

		removeWatchDirectoryButton.disableProperty().bind(watchDirectoriesListView.getSelectionModel().selectedItemProperty().isNull());
		RemoveAction<Path> removeAction = new RemoveAction<>(watchDirectoriesListView);
		removeAction.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "watch directory", FxUtil.PATH_STRING_CONVERTER));
		removeWatchDirectoryButton.setOnAction(removeAction);
		FxActions.setStandardMouseAndKeyboardSupport(watchDirectoriesListView, addWatchDirectoryButton, removeWatchDirectoryButton);

		initialScanCheckBox.selectedProperty().bindBidirectional(SettingsController.SETTINGS.getInitialScan().property());
		rejectAlreadyProcessedFilesCheckBox.selectedProperty().bindBidirectional(SettingsController.SETTINGS.getRejectAlreadyProcessedFiles().property());
	}

	public void addWatchDirectory()
	{
		addWatchDirectoryButton.fire();
	}
}
