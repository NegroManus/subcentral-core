package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.fx.FXUtil;
import de.subcentral.fx.WatcherDialogs;
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
			return FXUtil.createConstantBinding(((CrossGroupCompatibility) param.getValue().getValue()).toShortString());
		});

		addCrossGroupCompatibility.setOnAction((ActionEvent event) -> {
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityDialog();
			if (result.isPresent())
			{
				crossGroupCompatibilitiesTableView.getItems().add(new CompatibilitySettingEntry(result.get(), true));
			}
		});

		BooleanBinding noSelection = crossGroupCompatibilitiesTableView.getSelectionModel().selectedItemProperty().isNull();

		editCrossGroupCompatibility.disableProperty().bind(noSelection);
		editCrossGroupCompatibility.setOnAction((ActionEvent event) -> {
			CompatibilitySettingEntry selectedEntry = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedItem();
			if (selectedEntry.getValue() instanceof CrossGroupCompatibility)
			{
				Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityDialog((CrossGroupCompatibility) selectedEntry.getValue());
				if (result.isPresent())
				{
					int selectedIndex = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedIndex();
					crossGroupCompatibilitiesTableView.getItems().set(selectedIndex,
							new CompatibilitySettingEntry(result.get(), selectedEntry.isEnabled()));
				}
			}
		});

		removeCrossGroupCompatibility.disableProperty().bind(noSelection);
		removeCrossGroupCompatibility.setOnAction((ActionEvent event) -> {
			CompatibilitySettingEntry selectedCompatibility = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedItem();
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
			alert.setResizable(true);
			alert.setTitle("Confirmation of removal of cross-group compatibility");
			alert.setHeaderText("Do you really want to remove this cross-group compatibility?");
			String contentText = ((CrossGroupCompatibility) selectedCompatibility.getValue()).toShortString();
			alert.setContentText(contentText);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.YES)
			{
				int selectedIndex = crossGroupCompatibilitiesTableView.getSelectionModel().getSelectedIndex();
				crossGroupCompatibilitiesTableView.getItems().remove(selectedIndex);
			}
		});

		FXUtil.setStandardMouseAndKeyboardSupportForTableView(crossGroupCompatibilitiesTableView,
				editCrossGroupCompatibility,
				removeCrossGroupCompatibility);
	}
}
