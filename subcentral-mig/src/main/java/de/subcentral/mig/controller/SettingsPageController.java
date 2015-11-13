package de.subcentral.mig.controller;

import java.nio.file.Path;

import de.subcentral.fx.FxUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser.ExtensionFilter;

public class SettingsPageController extends AbstractPageController
{
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

	public SettingsPageController(MainController mainController, MigrationConfig config)
	{
		super(mainController, config);
	}

	@Override
	public void initialize() throws Exception
	{
		envSettingsFileTxtFld.setTextFormatter(envSettingsFileTextFormatter);
		ExtensionFilter propertiesExtFilter = new ExtensionFilter("Properties file", "*.properties");
		FxUtil.setChooseFileAction(chooseEnvSettingsFileBtn, envSettingsFileTextFormatter, mainController.getPrimaryStage(), "Choose environment settings file", propertiesExtFilter);

		parsingSettingsFileTxtFld.setTextFormatter(parsingSettingsFileTextFormatter);
		ExtensionFilter xmlExtFilter = new ExtensionFilter("XML file", "*.xml");
		FxUtil.setChooseFileAction(chooseParsingSettingsFileBtn, parsingSettingsFileTextFormatter, mainController.getPrimaryStage(), "Choose parsing settings file", xmlExtFilter);

		nextButtonDisableBinding = new BooleanBinding()
		{
			{
				super.bind(envSettingsFileTextFormatter.valueProperty(), parsingSettingsFileTextFormatter.valueProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return envSettingsFileTextFormatter.getValue() == null || parsingSettingsFileTextFormatter.getValue() == null;
			}
		};
	}

	@Override
	public String getTitle()
	{
		return "Load settings";
	}

	@Override
	public Pane getRootPane()
	{
		return rootPane;
	}

	@Override
	public Pane getContentPane()
	{
		return contentPane;
	}

	@Override
	public void onEntering()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onExiting()
	{
		config.setEnvironmentSettingsFile(envSettingsFileTextFormatter.getValue());
		config.setParsingSettingsFile(parsingSettingsFileTextFormatter.getValue());
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonDisableBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
