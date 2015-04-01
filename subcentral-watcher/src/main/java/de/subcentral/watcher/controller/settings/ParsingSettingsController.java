package de.subcentral.watcher.controller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import de.subcentral.fx.FXUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.italiansubsnet.ItalianSubsNet;
import de.subcentral.support.releasescene.ReleaseScene;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.watcher.settings.ParsingServiceSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;

public class ParsingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane											parsingSettingsPane;
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
	public GridPane getSectionRootPane()
	{
		return parsingSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		// Filename patterns
		filenamePatternsTextField.textProperty().bindBidirectional(WatcherSettings.INSTANCE.filenamePatternsProperty());

		// Parsing services
		parsingServicesTableView.setItems(WatcherSettings.INSTANCE.getFilenameParsingServices());
		parsingServicesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(parsingServicesEnabledColumn));
		parsingServicesEnabledColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, Boolean> param) -> param.getValue()
				.enabledProperty());
		parsingServicesNameColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, String> param) -> FXUtil.createConstantBinding(param.getValue()
				.getValue()
				.getDomain()));

		parsingServicesExampleColumn.setCellValueFactory((CellDataFeatures<ParsingServiceSettingEntry, String> param) -> {
			String example;
			switch (param.getValue().getValue().getDomain())
			{
				case Addic7edCom.DOMAIN:
					example = "Parks and Recreation - 07x01 - 2017.LOL.English.C.orig.Addic7ed.com";
					break;
				case ItalianSubsNet.DOMAIN:
					example = "Parks.And.Recreation.s07e01.sub.itasa";
					break;
				case SubCentralDe.DOMAIN:
					example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL.de-SubCentral";
					break;
				case ReleaseScene.DOMAIN:
					example = "Parks.and.Recreation.S07E01.HDTV.x264-LOL";
					break;
				default:
					example = "";
			}
			return FXUtil.createConstantBinding(example);
		});
	}
}