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
import javafx.beans.property.ListProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private final ListProperty<StandardizerSettingEntry<?, ?>> standardizingRules;

    @FXML
    private GridPane						       standardizingSettingsPane;
    @FXML
    private TableView<StandardizerSettingEntry<?, ?>>		       rulesTableView;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, Boolean>       rulesEnabledColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>	       rulesTypeColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>	       rulesOperationColumn;
    @FXML
    private ChoiceBox<Class<? extends StandardizerSettingEntry<?, ?>>> ruleTypeChoiceBox;
    @FXML
    private Button						       addRuleButton;
    @FXML
    private Button						       editRuleButton;
    @FXML
    private Button						       removeRuleButton;

    public StandardizingSettingsController(SettingsController settingsController, ListProperty<StandardizerSettingEntry<?, ?>> standardizingRules)
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
	rulesEnabledColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, Boolean> param) -> param.getValue().enabledProperty());

	rulesTypeColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().standardizerTypeAsStringBinding());

	rulesOperationColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().ruleAsStringBinding());

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
		result = Optional.empty();
	    }
	    FxUtil.handleDistinctAdd(rulesTableView, result);
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
		result = Optional.empty();
	    }
	    FxUtil.handleDistinctEdit(rulesTableView, result);
	});

	removeRuleButton.disableProperty().bind(noSelection);
	removeRuleButton.setOnAction((ActionEvent event) -> {
	    FxUtil.handleDelete(rulesTableView, "standardizing rule", new StringConverter<StandardizerSettingEntry<?, ?>>()
	    {
		@Override
		public String toString(StandardizerSettingEntry<?, ?> entry)
		{
		    StringBuilder sb = new StringBuilder();
		    sb.append("Rule type: ");
		    sb.append(entry.standardizerTypeAsStringBinding().get());
		    sb.append("\n");
		    sb.append("Rule: ");
		    sb.append(entry.ruleAsStringBinding().get());
		    return sb.toString();
		}

		@Override
		public StandardizerSettingEntry<?, ?> fromString(String string)
		{
		    // not needed
		    throw new UnsupportedOperationException();
		}
	    });
	});

	FxUtil.setStandardMouseAndKeyboardSupportForTableView(rulesTableView, editRuleButton, removeRuleButton);
    }
}
