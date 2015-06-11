package de.subcentral.watcher.controller.settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.settings.WatcherSettings;

public class FileTransformationSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane				fileTransformationSettingsPane;
	@FXML
	private TextField				targetDirTxtFld;
	@FXML
	private Button					chooseTargetDirBtn;
	@FXML
	private CheckBox				deleteSourceCheckBox;
	@FXML
	private CheckBox				packingEnabledCheckBox;
	@FXML
	private RadioButton				locateRadioBtn;
	@FXML
	private Button					testLocateBtn;
	@FXML
	private RadioButton				specifyRadioBtn;
	@FXML
	private TextField				rarExeTxtFld;
	@FXML
	private Button					chooseRarExeBtn;
	@FXML
	private ChoiceBox<DeletionMode>	packingSourceDeletionModeChoiceBox;

	public FileTransformationSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getSectionRootPane()
	{
		return fileTransformationSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		final TextFormatter<Path> targetDirFormatter = new TextFormatter<Path>(FxUtil.PATH_STRING_CONVERTER);
		targetDirFormatter.valueProperty().bindBidirectional(WatcherSettings.INSTANCE.targetDirProperty());
		targetDirTxtFld.setTextFormatter(targetDirFormatter);

		chooseTargetDirBtn.setOnAction((ActionEvent event) -> {
			DirectoryChooser dirChooser = new DirectoryChooser();
			dirChooser.setTitle("Choose target directory");
			Path currentValue = targetDirFormatter.getValue();
			if (currentValue != null && currentValue.isAbsolute())
			{
				dirChooser.setInitialDirectory(currentValue.toFile());
			}
			File selectedDirectory = dirChooser.showDialog(settingsController.getMainController().getPrimaryStage());
			if (selectedDirectory == null)
			{
				return;
			}
			Path newTargetDir = selectedDirectory.toPath();
			targetDirFormatter.setValue(newTargetDir);
		});

		deleteSourceCheckBox.selectedProperty().bindBidirectional(WatcherSettings.INSTANCE.deleteSourceProperty());

		packingEnabledCheckBox.selectedProperty().bindBidirectional(WatcherSettings.INSTANCE.packingEnabledProperty());

		ToggleGroup winRarLocateStrategy = new ToggleGroup();
		winRarLocateStrategy.getToggles().addAll(locateRadioBtn, specifyRadioBtn);
		winRarLocateStrategy.selectToggle(WatcherSettings.INSTANCE.isAutoLocateWinRar() ? locateRadioBtn : specifyRadioBtn);
		winRarLocateStrategy.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue)
			{
				WatcherSettings.INSTANCE.setAutoLocateWinRar(newValue == locateRadioBtn);
			}
		});

		final TextFormatter<Path> rarExeFormatter = new TextFormatter<Path>(FxUtil.PATH_STRING_CONVERTER);
		rarExeFormatter.valueProperty().bindBidirectional(WatcherSettings.INSTANCE.rarExeProperty());
		rarExeTxtFld.setTextFormatter(rarExeFormatter);

		testLocateBtn.setOnAction((ActionEvent event) -> {
			Path winRarLocation = WinRar.tryLocateRarExecutable();
			if (winRarLocation != null)
			{
				Alert locateDialog = new Alert(AlertType.INFORMATION);
				locateDialog.setTitle("Successfully located RAR executable");
				locateDialog.setHeaderText("Found RAR executable at: " + winRarLocation);
				locateDialog.setContentText("Do you want to remember this location?");
				locateDialog.getDialogPane().getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
				if (locateDialog.showAndWait().get() == ButtonType.YES)
				{
					winRarLocateStrategy.selectToggle(specifyRadioBtn);
					rarExeFormatter.setValue(winRarLocation);
				}
			}
			else
			{
				Alert locateDialog = new Alert(AlertType.WARNING);
				locateDialog.setTitle("Failed to locate RAR executable");
				locateDialog.setHeaderText("Could not locate RAR executable.");
				locateDialog.setContentText("Please install WinRAR or specify the path to the RAR executable.");
				locateDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
				locateDialog.showAndWait();
			}
		});

		chooseRarExeBtn.setOnAction((ActionEvent event) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select rar executable");
			Path currentValue = rarExeFormatter.getValue();
			if (currentValue != null)
			{
				Path potentialParentDir = currentValue.getParent();
				if (potentialParentDir != null && Files.isDirectory(potentialParentDir, LinkOption.NOFOLLOW_LINKS))
				{
					fileChooser.setInitialDirectory(potentialParentDir.toFile());
				}
			}
			ExtensionFilter exeFilter = new ExtensionFilter("RAR executable", WinRar.getRarExecutableFilename());
			fileChooser.getExtensionFilters().add(exeFilter);
			fileChooser.setSelectedExtensionFilter(exeFilter);

			File selectedFile = fileChooser.showOpenDialog(settingsController.getMainController().getPrimaryStage());
			if (selectedFile != null)
			{
				rarExeFormatter.setValue(selectedFile.toPath());
			}
		});

		packingSourceDeletionModeChoiceBox.setItems(FXCollections.observableArrayList(DeletionMode.values()));
		packingSourceDeletionModeChoiceBox.valueProperty().bindBidirectional(WatcherSettings.INSTANCE.packingSourceDeletionModeProperty());
		packingSourceDeletionModeChoiceBox.setConverter(SubCentralFxUtil.DELETION_MODE_STRING_CONVERTER);
	}
}
