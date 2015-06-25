package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.settings.ReleaseTagsStandardizerSettingEntry;
import de.subcentral.watcher.settings.SeriesNameStandardizerSettingEntry;
import de.subcentral.watcher.settings.StandardizerSettingEntry;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class StandardizingSettingsController extends AbstractSettingsSectionController
{
    private static final Logger log = LogManager.getLogger(StandardizingSettingsController.class);

    // Model
    private final ObservableList<StandardizerSettingEntry<?, ?>> standardizingRules;

    @FXML
    private GridPane                                                   standardizingSettingsPane;
    @FXML
    private TableView<StandardizerSettingEntry<?, ?>>                  rulesTableView;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, Boolean>       rulesEnabledColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>        rulesTypeColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>        rulesOperationColumn;
    @FXML
    private ChoiceBox<Class<? extends StandardizerSettingEntry<?, ?>>> ruleTypeChoiceBox;
    @FXML
    private Button                                                     addRuleButton;
    @FXML
    private Button                                                     editRuleButton;
    @FXML
    private Button                                                     removeRuleButton;

    public StandardizingSettingsController(SettingsController settingsController, ObservableList<StandardizerSettingEntry<?, ?>> standardizingRules)
    {
        super(settingsController);
        this.standardizingRules = standardizingRules;
    }

    @Override
    public GridPane getSectionRootPane()
    {
        return standardizingSettingsPane;
    }

    @Override
    protected void doInitialize() throws Exception
    {
        // Standardizers
        rulesTableView.setItems(standardizingRules);

        rulesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(rulesEnabledColumn));
        rulesEnabledColumn
                .setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, Boolean> param) -> param.getValue().enabledProperty());

        rulesTypeColumn.setCellValueFactory(
                (CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().standardizerTypeAsStringBinding());

        rulesOperationColumn
                .setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().ruleAsStringBinding());

        ruleTypeChoiceBox.getItems().add(SeriesNameStandardizerSettingEntry.class);
        ruleTypeChoiceBox.getItems().add(ReleaseTagsStandardizerSettingEntry.class);
        ruleTypeChoiceBox.setConverter(new StringConverter<Class<? extends StandardizerSettingEntry<?, ?>>>()
        {
            @Override
            public String toString(Class<? extends StandardizerSettingEntry<?, ?>> type)
            {
                return WatcherFxUtil.standardizingRuleTypeToString(type);
            }

            @Override
            public Class<? extends StandardizerSettingEntry<?, ?>> fromString(String string)
            {
                // not needed
                throw new UnsupportedOperationException();
            }
        });
        ruleTypeChoiceBox.getSelectionModel().selectFirst();

        addRuleButton.disableProperty().bind(ruleTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
        addRuleButton.setOnAction((ActionEvent event) -> {
            Class<? extends StandardizerSettingEntry<?, ?>> selectedStandardizerType = ruleTypeChoiceBox.getSelectionModel().getSelectedItem();
            Optional<? extends StandardizerSettingEntry<?, ?>> result;
            if (SeriesNameStandardizerSettingEntry.class == selectedStandardizerType)
            {
                result = WatcherDialogs.showSeriesNameStandardizerSettingEntryDialog();
            }
            else if (ReleaseTagsStandardizerSettingEntry.class == selectedStandardizerType)
            {
                result = WatcherDialogs.showReleaseTagsStandardizerSettingEntryDialog();
            }
            else
            {
                logWarningForUnknownStandardizerSettingEntryType(selectedStandardizerType);
                result = Optional.empty();
            }
            if (result.isPresent())
            {
                StandardizerSettingEntry<?, ?> newStandardizer = result.get();
                rulesTableView.getItems().add(newStandardizer);
                // so that the added item gets selected
                rulesTableView.getSelectionModel().selectLast();
            }
        });

        final BooleanBinding noSelection = rulesTableView.getSelectionModel().selectedItemProperty().isNull();

        editRuleButton.disableProperty().bind(noSelection);
        editRuleButton.setOnAction((ActionEvent event) -> {
            StandardizerSettingEntry<?, ?> selectedStandardizer = rulesTableView.getSelectionModel().getSelectedItem();
            Optional<? extends StandardizerSettingEntry<?, ?>> result;
            if (SeriesNameStandardizerSettingEntry.class == selectedStandardizer.getClass())
            {
                result = WatcherDialogs.showSeriesNameStandardizerSettingEntryDialog((SeriesNameStandardizerSettingEntry) selectedStandardizer);
            }
            else if (ReleaseTagsStandardizerSettingEntry.class == selectedStandardizer.getClass())
            {
                result = WatcherDialogs.showReleaseTagsStandardizerSettingEntryDialog((ReleaseTagsStandardizerSettingEntry) selectedStandardizer);
            }
            else
            {
                logWarningForUnknownStandardizerSettingEntryType(selectedStandardizer.getClass());
                result = Optional.empty();
            }
            if (result.isPresent())
            {
                StandardizerSettingEntry<?, ?> editedRule = result.get();
                int selectedIndex = rulesTableView.getSelectionModel().getSelectedIndex();
                rulesTableView.getItems().set(selectedIndex, editedRule);
                // if there is only one item and that is edited, then the selection somehow gets removed. so reselect
                rulesTableView.getSelectionModel().select(selectedIndex);
            }
        });

        removeRuleButton.disableProperty().bind(noSelection);
        removeRuleButton.setOnAction((ActionEvent event) -> {
            StandardizerSettingEntry<?, ?> selectedRule = rulesTableView.getSelectionModel().getSelectedItem();
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            alert.setResizable(true);
            alert.setTitle("Confirmation of removal of standardizing rule");
            alert.setHeaderText("Do you really want to remove this standardizing rule?");
            StringBuilder contentText = new StringBuilder();
            contentText.append("Rule type: ");
            contentText.append(selectedRule.standardizerTypeAsStringBinding().get());
            contentText.append("\n");
            contentText.append("Rule: ");
            contentText.append(selectedRule.ruleAsStringBinding().get());
            alert.setContentText(contentText.toString());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.YES)
            {
                int selectedIndex = rulesTableView.getSelectionModel().getSelectedIndex();
                rulesTableView.getItems().remove(selectedIndex);
            }
        });

        FxUtil.setStandardMouseAndKeyboardSupportForTableView(rulesTableView, editRuleButton, removeRuleButton);
    }

    private static void logWarningForUnknownStandardizerSettingEntryType(Class<? extends StandardizerSettingEntry> standardizerType)
    {
        log.warn("Unknown type of {}: {}. No dialog available for this standardizer",
                StandardizerSettingEntry.class.getName(),
                standardizerType.getName());
    }
}
