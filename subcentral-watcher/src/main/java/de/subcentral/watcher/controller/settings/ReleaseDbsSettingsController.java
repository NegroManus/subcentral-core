package de.subcentral.watcher.controller.settings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.core.metadata.service.MetadataService;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.FxIO;
import de.subcentral.fx.FxNodes;
import de.subcentral.fx.action.FxActions;
import de.subcentral.watcher.settings.MetadataServiceSettingsItem;
import de.subcentral.watcher.settings.MetadataServiceSettingsItem.Availability;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

public class ReleaseDbsSettingsController extends AbstractSettingsSectionController {
    private static final Logger                                                   log = LogManager.getLogger(ReleaseDbsSettingsController.class);

    @FXML
    private GridPane                                                              rootPane;
    @FXML
    private Button                                                                recheckAvailabilitiesButton;
    @FXML
    private TableView<MetadataServiceSettingsItem>                                releaseDbsTableView;
    @FXML
    private TableColumn<MetadataServiceSettingsItem, Boolean>                     releaseDbsEnabledColumn;
    @FXML
    private TableColumn<MetadataServiceSettingsItem, MetadataServiceSettingsItem> releaseDbsNameColumn;
    @FXML
    private TableColumn<MetadataServiceSettingsItem, Availability>                releaseDbsAvailableColumn;
    @FXML
    private Button                                                                moveUpReleaseDbBtn;
    @FXML
    private Button                                                                moveDownReleaseDbBtn;

    public ReleaseDbsSettingsController(SettingsController settingsController) {
        super(settingsController);
    }

    @Override
    public GridPane getContentPane() {
        return rootPane;
    }

    @Override
    protected void initialize() throws Exception {
        final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

        releaseDbsTableView.setItems(settings.getReleaseDbs().property());

        releaseDbsEnabledColumn.setCellValueFactory((CellDataFeatures<MetadataServiceSettingsItem, Boolean> param) -> param.getValue().enabledProperty());
        releaseDbsEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(releaseDbsEnabledColumn));

        releaseDbsNameColumn.setCellValueFactory((CellDataFeatures<MetadataServiceSettingsItem, MetadataServiceSettingsItem> param) -> FxBindings.immutableObservableValue(param.getValue()));
        releaseDbsNameColumn.setCellFactory((TableColumn<MetadataServiceSettingsItem, MetadataServiceSettingsItem> param) -> new NameTableCell(getExecutor()));

        releaseDbsAvailableColumn.setCellValueFactory((CellDataFeatures<MetadataServiceSettingsItem, Availability> param) -> param.getValue().availabilityProperty());
        releaseDbsAvailableColumn.setCellFactory((TableColumn<MetadataServiceSettingsItem, Availability> param) -> new AvailableTableCell());

        recheckAvailabilitiesButton.setOnAction((ActionEvent event) -> updateAvailibities());

        // if the items change, update the availibilities
        // * happens on load of settings
        // TODO: * also happens on move up/move down (remove, add)
        releaseDbsTableView.getItems().addListener(new ListChangeListener<MetadataServiceSettingsItem>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends MetadataServiceSettingsItem> c) {
                while (c.next()) {
                    if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            c.getList().get(i).updateAvailability(getExecutor());
                        }
                    }
                    else if (c.wasAdded()) {
                        for (MetadataServiceSettingsItem addItem : c.getAddedSubList()) {
                            addItem.updateAvailability(getExecutor());
                        }
                    }
                }
            }
        });

        FxActions.bindMoveButtons(releaseDbsTableView, moveUpReleaseDbBtn, moveDownReleaseDbBtn);

        // initial update
        updateAvailibities();
    }

    private void updateAvailibities() {
        for (MetadataServiceSettingsItem releaseDb : releaseDbsTableView.getItems()) {
            releaseDb.updateAvailability(getExecutor());
        }
    }

    private static class AvailableTableCell extends TableCell<MetadataServiceSettingsItem, Availability> {
        {
            // center the cell content
            super.setAlignment(Pos.CENTER);
        }

        protected void updateItem(MetadataServiceSettingsItem.Availability item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setTooltip(null);
                return;
            }
            switch (item.getCode()) {
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
                    setGraphic(createSymbolAndTextGraphic("checked_16.png", item));
                    setTooltip(new Tooltip("Available: Reachable and searchable"));
                    break;
                case LIMITED:
                    setGraphic(createSymbolAndTextGraphic("warning_16.png", item));
                    setTooltip(new Tooltip("Limited availability: Reachable but probably not searchable"));
                    break;
                case NOT_AVAILABLE:
                    setGraphic(createSymbolAndTextGraphic("cancel_16.png", item));
                    setTooltip(new Tooltip("Not available: Not reachable"));
                    break;
                default:
                    setGraphic(new Label(item.toString()));
                    setTooltip(null);
            }
        }

        private static Node createSymbolAndTextGraphic(String img, MetadataServiceSettingsItem.Availability availability) {
            ImageView imgView = new ImageView(FxIO.loadImg(img));
            if (availability.getStatus() != null && availability.getStatus().getResponseTime() >= 0) {
                Label lbl = new Label("(" + availability.getStatus().getResponseTime() + " ms)");
                HBox hbox = new HBox(imgView, lbl);
                hbox.setSpacing(5d);
                return hbox;
            }
            return imgView;
        }
    }

    private static class NameTableCell extends TableCell<MetadataServiceSettingsItem, MetadataServiceSettingsItem> {
        private final ExecutorService executor;

        private NameTableCell(ExecutorService executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
        }

        @Override
        public void updateItem(MetadataServiceSettingsItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            }
            else {
                MetadataService db = item.getItem();

                HBox hbox = FxNodes.createDefaultHBox();
                Label name = new Label(db.getSite().getDisplayName());
                hbox.getChildren().add(name);
                try {
                    URL host = new URL(db.getSite().getLink());
                    Hyperlink link = FxControlBindings.createUrlHyperlink(host, executor);
                    link.setMaxHeight(Double.MAX_VALUE);
                    hbox.getChildren().add(link);
                }
                catch (MalformedURLException e) {
                    log.warn("Could not create Hyperlink for release database " + item.getItem(), e);
                }
                setGraphic(hbox);
            }
        }
    }
}
