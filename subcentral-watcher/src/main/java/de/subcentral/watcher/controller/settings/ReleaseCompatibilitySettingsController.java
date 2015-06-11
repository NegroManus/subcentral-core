package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.settings.CompatibilitySettingEntry;
import de.subcentral.watcher.settings.WatcherSettings;

public class ReleaseCompatibilitySettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane										releaseCompatibilitySettingsPane;
	@FXML
	private CheckBox										compatibilityEnabledCheckBox;
	@FXML
	private TableView<CompatibilitySettingEntry>			crossGroupCompatibilitiesTableView;
	@FXML
	private TableColumn<CompatibilitySettingEntry, Boolean>	crossGroupCompatibilitiesEnabledColumn;
	@FXML
	private TableColumn<CompatibilitySettingEntry, String>	crossGroupCompatibilitiesCompatibilityColumn;

	@FXML
	private Button											addCrossGroupCompatibility;
	@FXML
	private Button											editCrossGroupCompatibility;
	@FXML
	private Button											removeCrossGroupCompatibility;

	public ReleaseCompatibilitySettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getSectionRootPane()
	{
		return releaseCompatibilitySettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		compatibilityEnabledCheckBox.selectedProperty().bindBidirectional(WatcherSettings.INSTANCE.compatibilityEnabledProperty());

		crossGroupCompatibilitiesTableView.setItems(WatcherSettings.INSTANCE.getCompatibilities());

		crossGroupCompatibilitiesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(crossGroupCompatibilitiesEnabledColumn));
		crossGroupCompatibilitiesEnabledColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingEntry, Boolean> param) -> param.getValue()
				.enabledProperty());
		crossGroupCompatibilitiesCompatibilityColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingEntry, String> param) -> {
			return FxUtil.createConstantBinding(((CrossGroupCompatibility) param.getValue().getValue()).toShortString());
		});

		addCrossGroupCompatibility.setOnAction((ActionEvent event) -> {
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityDialog();
			FxUtil.handleDistinctAdd(crossGroupCompatibilitiesTableView,
					result,
					(CrossGroupCompatibility c) -> new CompatibilitySettingEntry(c, true));
		});

		BooleanBinding noSelection = crossGroupCompatibilitiesTableView.getSelectionModel().selectedItemProperty().isNull();

		editCrossGroupCompatibility.disableProperty().bind(noSelection);
		editCrossGroupCompatibility.setOnAction((ActionEvent event) -> {

			CompatibilitySettingEntry selectedEntry = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedItem();
			if (selectedEntry.getValue() instanceof CrossGroupCompatibility)
			{
				Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityDialog((CrossGroupCompatibility) selectedEntry.getValue());
				FxUtil.handleDistinctEdit(crossGroupCompatibilitiesTableView, result, (CrossGroupCompatibility c) -> new CompatibilitySettingEntry(c,
						selectedEntry.isEnabled()));
			}
		});

		removeCrossGroupCompatibility.disableProperty().bind(noSelection);
		removeCrossGroupCompatibility.setOnAction((ActionEvent event) -> {
			FxUtil.handleDelete(crossGroupCompatibilitiesTableView, "cross-group compatibility", CompatibilitySettingEntry.STRING_CONVERTER);
		});

		FxUtil.setStandardMouseAndKeyboardSupportForTableView(crossGroupCompatibilitiesTableView,
				editCrossGroupCompatibility,
				removeCrossGroupCompatibility);
	}
}
