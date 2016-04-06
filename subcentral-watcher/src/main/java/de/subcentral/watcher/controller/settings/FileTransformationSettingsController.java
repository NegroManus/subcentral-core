package de.subcentral.watcher.controller.settings;

import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableMap;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.support.winrar.WinRar;
import de.subcentral.support.winrar.WinRarPackConfig.DeletionMode;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.ProcessingSettings.WinRarLocateStrategy;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;

public class FileTransformationSettingsController extends AbstractSettingsSectionController
{
	private static final Logger		log				= LogManager.getLogger(FileTransformationSettingsController.class);

	// Model
	private Path					locatedRarExe	= null;

	// View
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
	private RadioButton				autoLocateRadioBtn;
	@FXML
	private HBox					locateRarResultRootPane;
	@FXML
	private Button					rememberRarLocationBtn;
	@FXML
	private Button					retryLocateRarBtn;
	@FXML
	private RadioButton				specifyRadioBtn;
	@FXML
	private TextField				specifiedRarTxtFld;
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
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

		final TextFormatter<Path> targetDirFormatter = FxUtil.bindTextFieldToPath(targetDirTxtFld, settings.targetDirProperty());
		FxUtil.setChooseDirectoryAction(chooseTargetDirBtn, targetDirFormatter, settingsController.getMainController().getPrimaryStage(), "Choose target directory");

		deleteSourceCheckBox.selectedProperty().bindBidirectional(settings.deleteSourceProperty());

		packingEnabledCheckBox.selectedProperty().bindBidirectional(settings.packingEnabledProperty());

		ToggleGroup winRarLocateStrategy = new ToggleGroup();
		winRarLocateStrategy.getToggles().addAll(autoLocateRadioBtn, specifyRadioBtn);

		// bind toggle button to settings
		FxUtil.bindToggleGroupToEnumProp(winRarLocateStrategy,
				settings.winRarLocateStrategyProperty(),
				ImmutableMap.of(autoLocateRadioBtn, WinRarLocateStrategy.AUTO_LOCATE, specifyRadioBtn, WinRarLocateStrategy.SPECIFY));

		final TextFormatter<Path> specifiedRarFormatter = FxUtil.bindTextFieldToPath(specifiedRarTxtFld, settings.rarExeProperty());

		rememberRarLocationBtn.setDisable(true);
		rememberRarLocationBtn.setOnAction((ActionEvent evt) ->
		{
			winRarLocateStrategy.selectToggle(specifyRadioBtn);
			specifiedRarFormatter.setValue(locatedRarExe);
		});

		retryLocateRarBtn.setOnAction((ActionEvent evt) -> locateWinRar());

		ExtensionFilter[] filters;
		try
		{
			filters = new ExtensionFilter[] { new ExtensionFilter("RAR executable", settingsController.getMainController().getWinRar().getRarExecutableFilename().toString()) };
		}
		catch (UnsupportedOperationException e)
		{
			filters = new ExtensionFilter[] {};
		}

		FxUtil.setChooseFileAction(chooseRarExeBtn, specifiedRarFormatter, settingsController.getMainController().getPrimaryStage(), "Select rar executable", filters);

		packingSourceDeletionModeChoiceBox.setItems(FXCollections.observableArrayList(DeletionMode.values()));
		packingSourceDeletionModeChoiceBox.valueProperty().bindBidirectional(settings.packingSourceDeletionModeProperty());
		packingSourceDeletionModeChoiceBox.setConverter(SubCentralFxUtil.DELETION_MODE_STRING_CONVERTER);

		// init
		locateWinRar();
	}

	private void locateWinRar()
	{
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefWidth(16d);
		progressIndicator.setPrefHeight(16d);
		locateRarResultRootPane.getChildren().setAll(progressIndicator);
		Task<Path> locateWinRarTask = new Task<Path>()
		{
			@Override
			protected Path call() throws Exception
			{
				return WinRar.getInstance().locateRarExecutable();
			}

			@Override
			protected void succeeded()
			{
				locatedRarExe = getValue();
				updateUi();
			}

			@Override
			protected void cancelled()
			{
				locatedRarExe = null;
				updateUi();
			}

			@Override
			protected void failed()
			{
				log.error("Exception while locating WinRAR", getException());
				locatedRarExe = null;
				updateUi();
			}

			private void updateUi()
			{
				if (locatedRarExe != null)
				{
					ImageView img = new ImageView(FxUtil.loadImg("checked_16.png"));
					Hyperlink hl = FxUtil.createParentDirectoryHyperlink(locatedRarExe, settingsController.getMainController().getCommonExecutor());
					hl.setMaxHeight(Double.MAX_VALUE);
					locateRarResultRootPane.getChildren().setAll(img, hl);
				}
				else
				{
					ImageView img = new ImageView(FxUtil.loadImg("cross_16.png"));
					Label lbl = new Label("Could not locate WinRar");
					lbl.setMaxHeight(Double.MAX_VALUE);
					locateRarResultRootPane.getChildren().setAll(img, lbl);
				}

				// Enabled/Disabled "Remember" button
				rememberRarLocationBtn.setDisable(locatedRarExe == null);
			}
		};
		settingsController.getMainController().getCommonExecutor().submit(locateWinRarTask);
	}
}
