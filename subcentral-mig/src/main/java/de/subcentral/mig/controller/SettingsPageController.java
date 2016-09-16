package de.subcentral.mig.controller;

import java.nio.file.Path;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.action.FxActions;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser.ExtensionFilter;

public class SettingsPageController extends AbstractPageController {
	// Model
	private final TextFormatter<Path>	envSettingsFileTextFormatter		= new TextFormatter<>(FxUtil.PATH_STRING_CONVERTER);
	private final TextFormatter<Path>	parsingSettingsFileTextFormatter	= new TextFormatter<>(FxUtil.PATH_STRING_CONVERTER);
	private BooleanBinding				nextButtonDisableBinding;

	// View
	@FXML
	private AnchorPane					rootPane;
	@FXML
	private GridPane					contentPane;
	@FXML
	private TextField					envSettingsFileTxtFld;
	@FXML
	private Button						chooseEnvSettingsFileBtn;
	@FXML
	private TextField					parsingSettingsFileTxtFld;
	@FXML
	private Button						chooseParsingSettingsFileBtn;

	public SettingsPageController(MigMainController migMainController) {
		super(migMainController);
	}

	@Override
	protected void initialize() throws Exception {
		envSettingsFileTxtFld.setTextFormatter(envSettingsFileTextFormatter);
		ExtensionFilter propertiesExtFilter = new ExtensionFilter("Properties file", "*.properties");
		chooseEnvSettingsFileBtn.setOnAction((ActionEvent evt) -> FxActions.chooseFile(envSettingsFileTextFormatter, getPrimaryStage(), "Choose environment settings file", propertiesExtFilter));

		parsingSettingsFileTxtFld.setTextFormatter(parsingSettingsFileTextFormatter);
		ExtensionFilter xmlExtFilter = new ExtensionFilter("XML file", "*.xml");
		chooseParsingSettingsFileBtn.setOnAction((ActionEvent evt) -> FxActions.chooseFile(parsingSettingsFileTextFormatter, getPrimaryStage(), "Choose parsing settings file", xmlExtFilter));

		nextButtonDisableBinding = new BooleanBinding() {
			{
				super.bind(envSettingsFileTextFormatter.valueProperty(), parsingSettingsFileTextFormatter.valueProperty());
			}

			@Override
			protected boolean computeValue() {
				return envSettingsFileTextFormatter.getValue() == null || parsingSettingsFileTextFormatter.getValue() == null;
			}
		};
	}

	@Override
	public String getTitle() {
		return "Load settings";
	}

	@Override
	public Pane getRootPane() {
		return rootPane;
	}

	@Override
	public Pane getContentPane() {
		return contentPane;
	}

	@Override
	public void onEnter() {
		// nothing todo
	}

	@Override
	public void onExit() {
		assistance.setEnvironmentSettingsFile(envSettingsFileTextFormatter.getValue());
		assistance.setParsingSettingsFile(parsingSettingsFileTextFormatter.getValue());
	}

	@Override
	public BooleanBinding nextButtonDisableBinding() {
		return nextButtonDisableBinding;
	}
}
