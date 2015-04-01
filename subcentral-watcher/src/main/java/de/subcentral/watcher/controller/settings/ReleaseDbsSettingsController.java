package de.subcentral.watcher.controller.settings;

import java.net.URISyntaxException;
import java.net.URL;

import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.AbstractHttpMetadataDb;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.fx.FXUtil;
import de.subcentral.watcher.settings.MetadataDbSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;

public class ReleaseDbsSettingsController extends AbstractSettingsSectionController
{
	private static final Logger																log	= LogManager.getLogger(ReleaseDbsSettingsController.class);

	@FXML
	private GridPane																		releaseDbsSettingsPane;
	@FXML
	private Button																			recheckAvailabilitiesButton;
	@FXML
	private TableView<MetadataDbSettingEntry<Release>>										releaseDbsTableView;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, Boolean>							releaseDbsEnabledColumn;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>	releaseDbsNameColumn;
	@FXML
	private TableColumn<MetadataDbSettingEntry<Release>, Boolean>							releaseDbsAvailableColumn;
	@FXML
	private Button																			moveUpReleaseDbButton;
	@FXML
	private Button																			moveDownReleaseDbButton;

	public ReleaseDbsSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getSectionRootPane()
	{
		return releaseDbsSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		releaseDbsTableView.setItems(WatcherSettings.INSTANCE.getReleaseDbs());

		releaseDbsEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(releaseDbsEnabledColumn));
		releaseDbsEnabledColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, Boolean> param) -> param.getValue()
				.enabledProperty());

		releaseDbsNameColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>> param) -> {
			return new ObjectBinding<MetadataDbSettingEntry<Release>>()
			{
				@Override
				protected MetadataDbSettingEntry<Release> computeValue()
				{
					return param.getValue();
				}
			};
		});
		releaseDbsNameColumn.setCellFactory(new Callback<TableColumn<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>, TableCell<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>>()
		{
			@Override
			public TableCell<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>> call(
					TableColumn<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>> arg0)
			{
				return new TableCell<MetadataDbSettingEntry<Release>, MetadataDbSettingEntry<Release>>()
				{
					@Override
					public void updateItem(MetadataDbSettingEntry<Release> item, boolean empty)
					{
						super.updateItem(item, empty);
						if (empty || item == null)
						{
							setText(null);
							setGraphic(null);
						}
						else
						{
							setText(item.getValue().getName());
							if (item.getValue() instanceof AbstractHttpMetadataDb)
							{
								try
								{
									AbstractHttpMetadataDb<Release> rlsDb = (AbstractHttpMetadataDb<Release>) item.getValue();
									URL rlsDbUrl = rlsDb.getHost();
									Hyperlink link = FXUtil.createUrlHyperlink(rlsDbUrl, settingsController.getMainController().getCommonExecutor());
									setGraphic(link);
								}
								catch (URISyntaxException e)
								{
									log.error("Could not create Hyperlink for release database " + item.getValue(), e);
								}
							}
						}
					}
				};
			}
		});

		releaseDbsAvailableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(releaseDbsAvailableColumn));
		releaseDbsAvailableColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingEntry<Release>, Boolean> param) -> param.getValue()
				.availableProperty());

		recheckAvailibities();
		recheckAvailabilitiesButton.setOnAction((ActionEvent event) -> recheckAvailibities());

		FXUtil.bindMoveButtonsForSingleSelection(releaseDbsTableView, moveUpReleaseDbButton, moveDownReleaseDbButton);
	}

	private void recheckAvailibities()
	{
		for (MetadataDbSettingEntry<Release> releaseDb : releaseDbsTableView.getItems())
		{
			releaseDb.recheckAvailability(settingsController.getMainController().getCommonExecutor());
		}
	}
}
