package de.subcentral.watcher.controller.settings;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.HttpMetadataDb;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.settings.MetadataDbSettingEntry;
import de.subcentral.watcher.settings.MetadataDbSettingEntry.Availability;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class ReleaseDbsSettingsController extends AbstractSettingsSectionController
{
	private static final Logger log = LogManager.getLogger(ReleaseDbsSettingsController.class);

	@FXML
	private GridPane																		rootPane;
	@FXML
	private Button																			recheckAvailabilitiesButton;
	@FXML
	private TableView<MetadataDbSettingEntry<Release>>										releaseDbsTableView;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, Boolean>							releaseDbsEnabledColumn;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>	releaseDbsNameColumn;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, Availability>						releaseDbsAvailableColumn;
	@FXML
	private Button																			moveUpReleaseDbButton;
	@FXML
	private Button																			moveDownReleaseDbButton;

	public ReleaseDbsSettingsController(SettingsController settingsController)
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

		releaseDbsTableView.setItems(settings.releaseDbsProperty());

		releaseDbsEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(releaseDbsEnabledColumn));
		releaseDbsEnabledColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, Boolean> param) -> param.getValue().enabledProperty());

		releaseDbsNameColumn.setCellFactory((TableColumn<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>> arg0) ->
		{
			return new TableCell<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>()
			{
				@Override
				public void updateItem(MetadataDbSettingEntry<Release> item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty || item == null)
					{
						setGraphic(null);
					}
					else
					{
						if (item.getValue() instanceof HttpMetadataDb)
						{
							try
							{
								HttpMetadataDb db = (HttpMetadataDb) item.getValue();

								HBox hbox = new HBox();
								hbox.setSpacing(5d);
								hbox.setAlignment(Pos.CENTER_LEFT);

								Label name = new Label(db.getDisplayName());

								URL host = db.getHostUrl();
								Hyperlink link = FxUtil.createUrlHyperlink(host, settingsController.getMainController().getCommonExecutor());
								link.setMaxHeight(Double.MAX_VALUE);

								hbox.getChildren().addAll(name, link);
								setGraphic(hbox);
							}
							catch (URISyntaxException e)
							{
								log.error("Could not create Hyperlink for release database " + item.getValue(), e);
							}
						}
					}
				}
			};
		});
		releaseDbsNameColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>> param) -> FxUtil.constantBinding(param.getValue()));

		releaseDbsAvailableColumn.setCellFactory(new Callback<TableColumn<MetadataDbSettingEntry<Release>, Availability>, TableCell<MetadataDbSettingEntry<Release>, Availability>>()
		{
			@Override
			public TableCell<MetadataDbSettingEntry<Release>, Availability> call(TableColumn<MetadataDbSettingEntry<Release>, Availability> param)
			{
				return new TableCell<MetadataDbSettingEntry<Release>, Availability>()
				{
					{
						// set height to limit the height of the process indicator
						super.setPrefHeight(16d);
						// center the cell content
						super.setAlignment(Pos.CENTER);
					}

					protected void updateItem(MetadataDbSettingEntry.Availability item, boolean empty)
					{
						super.updateItem(item, empty);
						if (empty || item == null)
						{
							setGraphic(null);
						}
						else
						{
							switch (item)
							{
							case UNKNOWN:
								setGraphic(null);
								break;
							case CHECKING:
								setGraphic(new ProgressIndicator());
								break;
							case AVAILABLE:
								setGraphic(new ImageView(FxUtil.loadImg("checked_16.png")));
								break;
							case NOT_AVAILABLE:
								setGraphic(new ImageView(FxUtil.loadImg("cross_16.png")));
								break;
							default:
								setGraphic(null);
							}
						}
					}
				};
			}
		});
		releaseDbsAvailableColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, Availability> param) -> param.getValue().availabilityProperty());

		recheckAvailabilitiesButton.setOnAction((ActionEvent event) -> updateAvailibities());

		updateAvailibities();
		// if the items change update the availibilities (happens on load of settings)
		releaseDbsTableView.getItems().addListener((Observable o) -> updateAvailibities());

		FxUtil.bindMoveButtonsForSingleSelection(releaseDbsTableView, moveUpReleaseDbButton, moveDownReleaseDbButton);
	}

	private void updateAvailibities()
	{
		for (MetadataDbSettingEntry<Release> releaseDb : releaseDbsTableView.getItems())
		{
			releaseDb.updateAvailability(settingsController.getMainController().getCommonExecutor());
		}
	}
}
