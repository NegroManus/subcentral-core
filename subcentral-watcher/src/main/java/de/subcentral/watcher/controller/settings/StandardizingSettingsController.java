package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.settings.ReleaseTagsStandardizerSettingEntry;
import de.subcentral.watcher.settings.SeriesNameStandardizerSettingEntry;
import de.subcentral.watcher.settings.StandardizerSettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.binding.BooleanBinding;
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
    @FXML
    private GridPane						       standardizingSettingsPane;
    @FXML
    private TableView<StandardizerSettingEntry<?, ?>>		       standardizersTableView;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>	       standardizersTypeColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, String>	       standardizersRuleColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, Boolean>       standardizersBeforeQueryingColumn;
    @FXML
    private TableColumn<StandardizerSettingEntry<?, ?>, Boolean>       standardizersAfterQueryingColumn;
    @FXML
    private ChoiceBox<Class<? extends StandardizerSettingEntry<?, ?>>> standardizerTypeChoiceBox;
    @FXML
    private Button						       addStandardizerButton;
    @FXML
    private Button						       editStandardizerButton;
    @FXML
    private Button						       removeStandardizerButton;

    public StandardizingSettingsController(SettingsController settingsController)
    {
	super(settingsController);
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
	standardizersTableView.setItems(WatcherSettings.INSTANCE.getProcessingSettings().standardizersProperty());

	standardizersTypeColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().standardizerTypeStringBinding());

	standardizersRuleColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, String> param) -> param.getValue().ruleStringBinding());

	standardizersBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(standardizersBeforeQueryingColumn));
	standardizersBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

	standardizersAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(standardizersAfterQueryingColumn));
	standardizersAfterQueryingColumn.setCellValueFactory((CellDataFeatures<StandardizerSettingEntry<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

	standardizerTypeChoiceBox.getItems().add(SeriesNameStandardizerSettingEntry.class);
	standardizerTypeChoiceBox.getItems().add(ReleaseTagsStandardizerSettingEntry.class);
	standardizerTypeChoiceBox.setConverter(new StringConverter<Class<? extends StandardizerSettingEntry<?, ?>>>()
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
	standardizerTypeChoiceBox.getSelectionModel().selectFirst();

	addStandardizerButton.disableProperty().bind(standardizerTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
	addStandardizerButton.setOnAction((ActionEvent event) -> {
	    Class<? extends StandardizerSettingEntry<?, ?>> selectedStandardizerType = standardizerTypeChoiceBox.getSelectionModel().getSelectedItem();
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
	    FxUtil.handleDistinctAdd(standardizersTableView, result);
	});

	final BooleanBinding noSelection = standardizersTableView.getSelectionModel().selectedItemProperty().isNull();

	editStandardizerButton.disableProperty().bind(noSelection);
	editStandardizerButton.setOnAction((ActionEvent event) -> {
	    StandardizerSettingEntry<?, ?> selectedStandardizer = standardizersTableView.getSelectionModel().getSelectedItem();
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
	    FxUtil.handleDistinctEdit(standardizersTableView, result);
	});

	removeStandardizerButton.disableProperty().bind(noSelection);
	removeStandardizerButton.setOnAction((ActionEvent event) -> {
	    FxUtil.handleDelete(standardizersTableView, "standardizing rule", new StringConverter<StandardizerSettingEntry<?, ?>>()
	    {
		@Override
		public String toString(StandardizerSettingEntry<?, ?> entry)
		{
		    StringBuilder sb = new StringBuilder();
		    sb.append("Rule type: ");
		    sb.append(entry.standardizerTypeStringBinding().get());
		    sb.append("\n");
		    sb.append("Rule: ");
		    sb.append(entry.ruleStringBinding().get());
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

	FxUtil.setStandardMouseAndKeyboardSupportForTableView(standardizersTableView, editStandardizerButton, removeStandardizerButton);
    }
}
