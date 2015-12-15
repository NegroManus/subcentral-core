package de.subcentral.watcher.controller.settings;

import de.subcentral.fx.FxUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.watcher.settings.ParsingServiceSettingEntry;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class ParsingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane											rootPane;
	@FXML
	private TextField											filenamePatternsTextField;
	@FXML
	private TableView<ParsingServiceSettingEntry>				parsingServicesTableView;
	@FXML
	private TableColumn<ParsingServiceSettingEntry, Boolean>	parsingServicesEnabledColumn;
	@FXML
	private TableColumn<ParsingServiceSettingEntry, String>		parsingServicesNameColumn;
	@FXML
	private TableColumn<ParsingServiceSettingEntry, String>		parsingServicesExampleColumn;

	public ParsingSettingsController(SettingsController settingsController)
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
		parsingServicesEnabledColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, Boolean> param) -> param.getValue().enabledProperty());
		parsingServicesNameColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, String> param) -> FxUtil.constantBinding(param.getValue().getValue().getDomain()));

		parsingServicesExampleColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, String> param) ->
		{
			String example;
			switch (param.getValue().getValue().getDomain())
			{
				case Addic7edCom.SITE_ID:
					example = "Parks and Recreation - 07x01 - 2017.LOL.English.C.orig.Addic7ed.com";
					break;
				case ItalianSubsNet.SITE_ID:
					example = "Parks.And.Recreation.s07e01.sub.itasa";
					break;
				case SubCentralDe.SITE_ID:
					example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL.de-SubCentral";
					break;
				case ReleaseScene.SOURCE_ID:
					example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL";
					break;
				default:
					example = "";
			}
			return FxUtil.constantBinding(example);
		});
	}
}