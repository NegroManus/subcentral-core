package de.subcentral.watcher.controller.settings;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.action.ActionList;
import de.subcentral.fx.action.FxActions;
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
		// Watch directories list
		watchDirectoriesListView.setItems(SettingsController.SETTINGS.getWatchDirectories().property());

		ActionList<Path> dirsActionList = new ActionList<>(watchDirectoriesListView);
		dirsActionList.setNewItemSupplier(() ->
		{
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle("Add watch directory");
			File file = dirChooser.showDialog(getPrimaryStage());
			return file != null ? Optional.of(file.toPath()) : Optional.empty();
		});
		dirsActionList.setDistincter(Objects::equals);
		dirsActionList.setSorter(ObjectUtil.getDefaultOrdering());
		dirsActionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "watch directory", FxUtil.PATH_STRING_CONVERTER));
		dirsActionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "watch directory", FxUtil.PATH_STRING_CONVERTER));

		dirsActionList.bindAddButton(addWatchDirectoryButton);
		dirsActionList.bindRemoveButton(removeWatchDirectoryButton);

		FxActions.setStandardMouseAndKeyboardSupport(watchDirectoriesListView, addWatchDirectoryButton, removeWatchDirectoryButton);

		// Other settings
		initialScanCheckBox.selectedProperty().bindBidirectional(SettingsController.SETTINGS.getInitialScan().property());
		rejectAlreadyProcessedFilesCheckBox.selectedProperty().bindBidirectional(SettingsController.SETTINGS.getRejectAlreadyProcessedFiles().property());
	}

	public void addWatchDirectory()
	{
		addWatchDirectoryButton.fire();
	}
}
