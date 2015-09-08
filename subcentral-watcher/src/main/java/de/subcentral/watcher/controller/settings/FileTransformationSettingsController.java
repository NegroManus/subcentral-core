package de.subcentral.watcher.controller.settings;

import java.nio.file.Path;

import com.google.common.collect.ImmutableMap;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.FxUtil.ToggleEnumBinding;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.support.winrar.WinRarPackager.LocateStrategy;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class FileTransformationSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane				rootPane;
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
	public GridPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

		final TextFormatter<Path> targetDirFormatter = FxUtil.bindPathToTextField(targetDirTxtFld, settings.targetDirProperty());
		FxUtil.setChooseDirectoryAction(chooseTargetDirBtn, targetDirFormatter, settingsController.getMainController().getPrimaryStage(), "Choose target directory");

		deleteSourceCheckBox.selectedProperty().bindBidirectional(settings.deleteSourceProperty());

		packingEnabledCheckBox.selectedProperty().bindBidirectional(settings.packingEnabledProperty());

		ToggleGroup winRarLocateStrategy = new ToggleGroup();
		winRarLocateStrategy.getToggles().addAll(locateRadioBtn, specifyRadioBtn);

		// bind toggle button to settings
		new ToggleEnumBinding<>(winRarLocateStrategy, settings.winRarLocateStrategyProperty(), ImmutableMap.of(locateRadioBtn, LocateStrategy.LOCATE, specifyRadioBtn, LocateStrategy.SPECIFY));

		final TextFormatter<Path> rarExeFormatter = FxUtil.bindPathToTextField(rarExeTxtFld, settings.rarExeProperty());
		testLocateBtn.setOnAction((ActionEvent event) ->
		{
			try
			{
				Path winRarLocation = WinRar.getInstance().getPackager(LocateStrategy.LOCATE).getRarExecutable();

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
			catch (Exception e)
			{
				Alert locateDialog = new Alert(AlertType.WARNING);
				locateDialog.setTitle("Failed to locate RAR executable");
				locateDialog.setHeaderText("Could not locate RAR executable.");
				locateDialog.setContentText("Please install WinRAR or specify the path to the RAR executable.");
				locateDialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
				locateDialog.showAndWait();
			}
		});

		FxUtil.setChooseFileAction(chooseRarExeBtn,
				rarExeFormatter,
				settingsController.getMainController().getPrimaryStage(),
				"Select rar executable",
				"RAR executable",
				WinRar.getInstance().getRarExecutableFilename().toString());

		packingSourceDeletionModeChoiceBox.setItems(FXCollections.observableArrayList(DeletionMode.values()));
		packingSourceDeletionModeChoiceBox.valueProperty().bindBidirectional(settings.packingSourceDeletionModeProperty());
		packingSourceDeletionModeChoiceBox.setConverter(SubCentralFxUtil.DELETION_MODE_STRING_CONVERTER);
	}
}
