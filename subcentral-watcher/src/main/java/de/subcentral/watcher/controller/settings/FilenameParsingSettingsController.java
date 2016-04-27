package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.FxBindings;
import de.subcentral.fx.action.FxActions;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.watcher.settings.ParsingServiceSettingsItem;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class FilenameParsingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane									rootPane;
	@FXML
	private TextField									filenamePatternsTextField;
	@FXML
	private TableView<ParsingServiceSettingsItem>				parsersTableView;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, Boolean>	parsersEnabledColumn;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, String>		parsersNameColumn;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, String>		parsersExampleColumn;
	@FXML
	private Button										moveUpParserBtn;
	@FXML
	private Button										moveDownParserBtn;

	public FilenameParsingSettingsController(SettingsController settingsController)
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

		// Filename patterns
		filenamePatternsTextField.textProperty().bindBidirectional(settings.getFilenamePatterns().property());

		// Parsing services
		parsersTableView.setItems(settings.getFilenameParsers().property());
		parsersEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(parsersEnabledColumn));
		parsersEnabledColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		parsersNameColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, String> param) -> FxBindings.immutableObservableValue(param.getValue().getItem().getName()));

		parsersExampleColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, String> param) ->
		{
			String example;
			String domain = param.getValue().getItem().getName();
			if (Addic7edCom.getParsingService().getName().equals(domain))
			{
				example = "Parks and Recreation - 07x01 - 2017.LOL.English.C.orig.Addic7ed.com";
			}
			else if (ItalianSubsNet.getParsingService().getName().equals(domain))
			{
				example = "Parks.And.Recreation.s07e01.sub.itasa";
			}
			else if (SubCentralDe.getParsingService().getName().equals(domain))
			{
				example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL.de-SubCentral";
			}
			else if (ReleaseScene.getParsingService().getName().equals(domain))
			{
				example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL";
			}
			else
			{
				example = "";
			}
			return FxBindings.immutableObservableValue(example);
		});

		FxActions.bindMoveButtons(parsersTableView, moveUpParserBtn, moveDownParserBtn);
	}
}