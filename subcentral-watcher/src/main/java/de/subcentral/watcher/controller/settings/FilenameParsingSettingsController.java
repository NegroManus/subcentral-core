package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.FxUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.watcher.settings.ParsingServiceSettingsItem;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class FilenameParsingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane											rootPane;
	@FXML
	private TextField											filenamePatternsTextField;
	@FXML
	private TableView<ParsingServiceSettingsItem>				parsingServicesTableView;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, Boolean>	parsingServicesEnabledColumn;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, String>		parsingServicesNameColumn;
	@FXML
	private TableColumn<ParsingServiceSettingsItem, String>		parsingServicesExampleColumn;

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
		final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

		// Filename patterns
		filenamePatternsTextField.textProperty().bindBidirectional(settings.filenamePatternsProperty());

		// Parsing services
		parsingServicesTableView.setItems(settings.filenameParsingServicesProperty());
		parsingServicesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(parsingServicesEnabledColumn));
		parsingServicesEnabledColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		parsingServicesNameColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, String> param) -> FxUtil.constantBinding(param.getValue().getItem().getDomain()));

		parsingServicesExampleColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingsItem, String> param) ->
		{
			String example;
			String domain = param.getValue().getItem().getDomain();
			if (Addic7edCom.getParsingService().getDomain().equals(domain))
			{
				example = "Parks and Recreation - 07x01 - 2017.LOL.English.C.orig.Addic7ed.com";
			}
			else if (ItalianSubsNet.getParsingService().getDomain().equals(domain))
			{
				example = "Parks.And.Recreation.s07e01.sub.itasa";
			}
			else if (SubCentralDe.getParsingService().getDomain().equals(domain))
			{
				example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL.de-SubCentral";
			}
			else if (ReleaseScene.getParsingService().getDomain().equals(domain))
			{
				example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL";
			}
			else
			{
				example = "";
			}
			return FxUtil.constantBinding(example);
		});
	}
}