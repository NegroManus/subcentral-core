package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CompatibilitySettingsItem;
import de.subcentral.watcher.settings.ProcessingSettings;
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

public class ReleaseCompatibilitySettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane										rootPane;
	@FXML
	private CheckBox										compatibilityEnabledCheckBox;
	@FXML
	private TableView<CompatibilitySettingsItem>			crossGroupCompatibilitiesTableView;
	@FXML
	private TableColumn<CompatibilitySettingsItem, Boolean>	crossGroupCompatibilitiesEnabledColumn;
	@FXML
	private TableColumn<CompatibilitySettingsItem, String>	crossGroupCompatibilitiesCompatibilityColumn;

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
	public GridPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

		compatibilityEnabledCheckBox.selectedProperty().bindBidirectional(settings.compatibilityEnabledProperty());

		crossGroupCompatibilitiesTableView.setItems(settings.compatibilitiesProperty());

		crossGroupCompatibilitiesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(crossGroupCompatibilitiesEnabledColumn));
		crossGroupCompatibilitiesEnabledColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		crossGroupCompatibilitiesCompatibilityColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingsItem, String> param) ->
		{
			return FxUtil.constantBinding(((CrossGroupCompatibility) param.getValue().getItem()).toShortString());
		});

		addCrossGroupCompatibility.setOnAction((ActionEvent event) ->
		{
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView(settingsController.getMainController().getPrimaryStage());
			FxUtil.handleDistinctAdd(crossGroupCompatibilitiesTableView, result, (CrossGroupCompatibility c) -> new CompatibilitySettingsItem(c, true));
		});

		BooleanBinding noSelection = crossGroupCompatibilitiesTableView.getSelectionModel().selectedItemProperty().isNull();

		editCrossGroupCompatibility.disableProperty().bind(noSelection);
		editCrossGroupCompatibility.setOnAction((ActionEvent event) ->
		{
			CompatibilitySettingsItem selectedEntry = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedItem();
			if (selectedEntry.getItem() instanceof CrossGroupCompatibility)
			{
				Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView((CrossGroupCompatibility) selectedEntry.getItem(),
						settingsController.getMainController().getPrimaryStage());
				FxUtil.handleDistinctEdit(crossGroupCompatibilitiesTableView, result, (CrossGroupCompatibility c) -> new CompatibilitySettingsItem(c, selectedEntry.isEnabled()));
			}
		});

		removeCrossGroupCompatibility.disableProperty().bind(noSelection);
		removeCrossGroupCompatibility.setOnAction((ActionEvent event) ->
		{
			FxUtil.handleConfirmedDelete(crossGroupCompatibilitiesTableView, "cross-group compatibility", CompatibilitySettingsItem.STRING_CONVERTER);
		});

		FxUtil.setStandardMouseAndKeyboardSupport(crossGroupCompatibilitiesTableView, editCrossGroupCompatibility, removeCrossGroupCompatibility);
	}
}
