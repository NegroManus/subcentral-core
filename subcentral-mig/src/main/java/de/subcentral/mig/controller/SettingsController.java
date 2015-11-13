package de.subcentral.mig.controller;

import java.nio.file.Path;

import de.subcentral.fx.FxUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser.ExtensionFilter;

public class SettingsController extends AbstractPageController
{
	// Model
	private final TextFormatter<Path>	envSettingsFileTextFormatter		= new TextFormatter<>(FxUtil.PATH_STRING_CONVERTER);
	private final TextFormatter<Path>	parsingSettingsFileTextFormatter	= new TextFormatter<>(FxUtil.PATH_STRING_CONVERTER);
	private BooleanBinding				nextButtonEnabledBinding;

	// View
	@FXML
	private GridPane					rootPane;
	@FXML
	private TextField					envSettingsFileTxtFld;
	@FXML
	private Button						chooseEnvSettingsFileBtn;
	@FXML
	private TextField					parsingSettingsFileTxtFld;
	@FXML
	private Button						chooseParsingSettingsFileBtn;

	public SettingsController(MainController mainController, MigrationConfig config)
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

		nextButtonEnabledBinding = new BooleanBinding()
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
	public Node getRootPane()
	{
		return rootPane;
	}

	public Property<Path> environmentSettingsFileProperty()
	{
		return envSettingsFileTextFormatter.valueProperty();
	}

	public Property<Path> parsingSettingsFileProperty()
	{
		return parsingSettingsFileTextFormatter.valueProperty();
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonEnabledBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
