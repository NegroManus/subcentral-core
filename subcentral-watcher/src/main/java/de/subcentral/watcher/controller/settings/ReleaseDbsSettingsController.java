package de.subcentral.watcher.controller.settings;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.settings.MetadataDbSettingsItem;
import de.subcentral.watcher.settings.MetadataDbSettingsItem.Availability;
import de.subcentral.watcher.settings.ProcessingSettings;
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
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class ReleaseDbsSettingsController extends AbstractSettingsSectionController
{
	private static final Logger																log	= LogManager.getLogger(ReleaseDbsSettingsController.class);

	@FXML
	private GridPane																		rootPane;
	@FXML
	private Button																			recheckAvailabilitiesButton;
	@FXML
	private TableView<MetadataDbSettingsItem<Release>>										releaseDbsTableView;
	@FXML
	private TableColumn<MetadataDbSettingsItem<Release>, Boolean>							releaseDbsEnabledColumn;
	@FXML
	private TableColumn<MetadataDbSettingsItem<Release>, MetadataDbSettingsItem<Release>>	releaseDbsNameColumn;
	@FXML
	private TableColumn<MetadataDbSettingsItem<Release>, Availability>						releaseDbsAvailableColumn;
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

	private static class AvailableTableCell extends TableCell<MetadataDbSettingsItem<Release>, Availability>
	{
		{
			// center the cell content
			super.setAlignment(Pos.CENTER);
		}

		protected void updateItem(MetadataDbSettingsItem.Availability item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty || item == null)
			{
				setGraphic(null);
				setTooltip(null);
				return;
			}
			switch (item)
			{
				case UNKNOWN:
					setGraphic(null);
					setTooltip(new Tooltip("Unknown"));
					break;
				case CHECKING:
					ProgressIndicator progressIndicator = new ProgressIndicator();
					progressIndicator.setPrefWidth(16d);
					progressIndicator.setPrefHeight(16d);
					setGraphic(progressIndicator);
					setTooltip(null);
					break;
				case AVAILABLE:
					setGraphic(new ImageView(FxUtil.loadImg("checked_16.png")));
					setTooltip(new Tooltip("Available: Accessible and searchable"));
					break;
				case LIMITED:
					setGraphic(new ImageView(FxUtil.loadImg("warning_16.png")));
					setTooltip(new Tooltip("Limited availibility: Reachable but not searchable"));
					break;
				case NOT_AVAILABLE:
					setGraphic(new ImageView(FxUtil.loadImg("cancel_16.png")));
					setTooltip(new Tooltip("Not available: Not reachable"));
					break;
				default:
					setGraphic(new Label(item.name()));
					setTooltip(null);
			}
		}
	}

	@Override
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

		releaseDbsTableView.setItems(settings.releaseDbsProperty());

		releaseDbsEnabledColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingsItem<Release>, Boolean> param) -> param.getValue().enabledProperty());
		releaseDbsEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(releaseDbsEnabledColumn));

		releaseDbsNameColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingsItem<Release>, MetadataDbSettingsItem<Release>> param) -> FxUtil.constantBinding(param.getValue()));
		releaseDbsNameColumn
				.setCellFactory((TableColumn<MetadataDbSettingsItem<Release>, MetadataDbSettingsItem<Release>> param) -> new NameTableCell(settingsController.getMainController().getCommonExecutor()));

		releaseDbsAvailableColumn.setCellValueFactory((CellDataFeatures<MetadataDbSettingsItem<Release>, Availability> param) -> param.getValue().availabilityProperty());
		releaseDbsAvailableColumn.setCellFactory((TableColumn<MetadataDbSettingsItem<Release>, Availability> param) -> new AvailableTableCell());

		recheckAvailabilitiesButton.setOnAction((ActionEvent event) -> updateAvailibities());

		// if the items change update the availibilities (happens on load of settings)
		// TODO: also on move up/move down but we can't distinguish that easily
		releaseDbsTableView.getItems().addListener((Observable o) -> updateAvailibities());

		FxUtil.bindMoveButtonsForSingleSelection(releaseDbsTableView, moveUpReleaseDbButton, moveDownReleaseDbButton);

		// initial update
		updateAvailibities();
	}

	private void updateAvailibities()
	{
		for (MetadataDbSettingsItem<Release> releaseDb : releaseDbsTableView.getItems())
		{
			releaseDb.updateAvailability(settingsController.getMainController().getCommonExecutor());
		}
	}

	private static class NameTableCell extends TableCell<MetadataDbSettingsItem<Release>, MetadataDbSettingsItem<Release>>
	{
		private final ExecutorService executor;

		private NameTableCell(ExecutorService executor)
		{
			this.executor = Objects.requireNonNull(executor, "executor");
		}

		@Override
		public void updateItem(MetadataDbSettingsItem<Release> item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty || item == null)
			{
				setGraphic(null);
			}
			else
			{
				MetadataDb db = item.getItem();

				HBox hbox = FxUtil.createDefaultHBox();
				Label name = new Label(db.getSite().getDisplayName());
				hbox.getChildren().add(name);
				try
				{
					URL host = new URL(db.getSite().getLink());
					Hyperlink link = FxUtil.createUrlHyperlink(host, executor);
					link.setMaxHeight(Double.MAX_VALUE);
					hbox.getChildren().add(link);
				}
				catch (MalformedURLException | URISyntaxException e)
				{
					log.warn("Could not create Hyperlink for release database " + item.getItem(), e);
				}
				setGraphic(hbox);
			}
		}
	}

}
