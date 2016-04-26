package de.subcentral.watcher.controller.settings;

import java.util.Objects;
import java.util.Optional;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.action.ActionList;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CrossGroupCompatibilitySettingsItem;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
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
	private GridPane													rootPane;
	@FXML
	private CheckBox													compatibilityEnabledCheckBox;
	@FXML
	private TableView<CrossGroupCompatibilitySettingsItem>				crossGroupCompatibilitiesTableView;
	@FXML
	private TableColumn<CrossGroupCompatibilitySettingsItem, Boolean>	crossGroupCompatibilitiesEnabledColumn;
	@FXML
	private TableColumn<CrossGroupCompatibilitySettingsItem, String>	crossGroupCompatibilitiesCompatibilityColumn;

	@FXML
	private Button														addCrossGroupCompatibilityBtn;
	@FXML
	private Button														editCrossGroupCompatibilityBtn;
	@FXML
	private Button														removeCrossGroupCompatibilityBtn;

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

		compatibilityEnabledCheckBox.selectedProperty().bindBidirectional(settings.getCompatibilityEnabled().property());

		// Cross-group compatibilities table
		ObservableList<CrossGroupCompatibilitySettingsItem> comps = settings.getCrossGroupCompatibilities().property();
		SortedList<CrossGroupCompatibilitySettingsItem> displayComps = FxControlBindings.sortableTableView(crossGroupCompatibilitiesTableView, comps);

		crossGroupCompatibilitiesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(crossGroupCompatibilitiesEnabledColumn));
		crossGroupCompatibilitiesEnabledColumn.setCellValueFactory((CellDataFeatures<CrossGroupCompatibilitySettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		crossGroupCompatibilitiesCompatibilityColumn.setCellValueFactory((CellDataFeatures<CrossGroupCompatibilitySettingsItem, String> param) ->
		{
			return FxBindings.immutableObservableValue(((CrossGroupCompatibility) param.getValue().getItem()).toShortString());
		});

		// Table buttons
		ActionList<CrossGroupCompatibilitySettingsItem> compsActionList = new ActionList<>(comps, crossGroupCompatibilitiesTableView.getSelectionModel(), displayComps);
		compsActionList.setNewItemSupplier(() ->
		{
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView(getPrimaryStage());
			return result.isPresent() ? Optional.of(new CrossGroupCompatibilitySettingsItem(result.get(), true)) : Optional.empty();
		});
		compsActionList.setItemEditer((CrossGroupCompatibilitySettingsItem item) ->
		{
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView(item.getItem(), getPrimaryStage());
			return result.isPresent() ? Optional.of(new CrossGroupCompatibilitySettingsItem(result.get(), item.isEnabled())) : Optional.empty();
		});
		compsActionList.setDistincter(Objects::equals);
		compsActionList.setSorter(ObjectUtil.getDefaultOrdering());
		compsActionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "cross-group compatibility", CrossGroupCompatibilitySettingsItem.STRING_CONVERTER));
		compsActionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "cross-group compatibility", CrossGroupCompatibilitySettingsItem.STRING_CONVERTER));

		compsActionList.bindAddButton(addCrossGroupCompatibilityBtn);
		compsActionList.bindEditButton(editCrossGroupCompatibilityBtn);
		compsActionList.bindRemoveButton(removeCrossGroupCompatibilityBtn);

		FxActions.setStandardMouseAndKeyboardSupport(crossGroupCompatibilitiesTableView, addCrossGroupCompatibilityBtn, editCrossGroupCompatibilityBtn, removeCrossGroupCompatibilityBtn);
	}
}
