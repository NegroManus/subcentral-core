package de.subcentral.watcher.controller.settings;

import java.util.Objects;
import java.util.Optional;

import de.subcentral.core.metadata.release.CrossGroupCompatibilityRule;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.action.ActionList;
import de.subcentral.fx.action.FxActions;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CrossGroupCompatibilityRuleSettingsItem;
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
	private GridPane														rootPane;
	@FXML
	private CheckBox														compatibilityEnabledCheckBox;
	@FXML
	private TableView<CrossGroupCompatibilityRuleSettingsItem>				crossGroupCompatibilityRulesTableView;
	@FXML
	private TableColumn<CrossGroupCompatibilityRuleSettingsItem, Boolean>	crossGroupCompatibilityRulesEnabledColumn;
	@FXML
	private TableColumn<CrossGroupCompatibilityRuleSettingsItem, String>	crossGroupCompatibilityRulesRuleColumn;

	@FXML
	private Button															addCrossGroupCompatibilityRuleBtn;
	@FXML
	private Button															editCrossGroupCompatibilityRuleBtn;
	@FXML
	private Button															removeCrossGroupCompatibilityRuleBtn;

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
		ObservableList<CrossGroupCompatibilityRuleSettingsItem> comps = settings.getCrossGroupCompatibilityRules().property();
		SortedList<CrossGroupCompatibilityRuleSettingsItem> displayComps = FxControlBindings.sortableTableView(crossGroupCompatibilityRulesTableView, comps);

		crossGroupCompatibilityRulesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(crossGroupCompatibilityRulesEnabledColumn));
		crossGroupCompatibilityRulesEnabledColumn.setCellValueFactory((CellDataFeatures<CrossGroupCompatibilityRuleSettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		crossGroupCompatibilityRulesRuleColumn.setCellValueFactory((CellDataFeatures<CrossGroupCompatibilityRuleSettingsItem, String> param) ->
		{
			return FxBindings.immutableObservableValue(((CrossGroupCompatibilityRule) param.getValue().getItem()).toShortString());
		});

		// Table buttons
		ActionList<CrossGroupCompatibilityRuleSettingsItem> compsActionList = new ActionList<>(comps, crossGroupCompatibilityRulesTableView.getSelectionModel(), displayComps);
		compsActionList.setNewItemSupplier(() ->
		{
			Optional<CrossGroupCompatibilityRule> result = WatcherDialogs.showCrossGroupCompatibilityRuleEditView(getPrimaryStage());
			return result.isPresent() ? Optional.of(new CrossGroupCompatibilityRuleSettingsItem(result.get(), true)) : Optional.empty();
		});
		compsActionList.setItemEditer((CrossGroupCompatibilityRuleSettingsItem item) ->
		{
			Optional<CrossGroupCompatibilityRule> result = WatcherDialogs.showCrossGroupCompatibilityRuleEditView(item.getItem(), getPrimaryStage());
			return result.isPresent() ? Optional.of(new CrossGroupCompatibilityRuleSettingsItem(result.get(), item.isEnabled())) : Optional.empty();
		});
		compsActionList.setDistincter(Objects::equals);
		compsActionList.setSorter(ObjectUtil.getDefaultOrdering());
		compsActionList
				.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "cross-group compatibility rule", CrossGroupCompatibilityRuleSettingsItem.STRING_CONVERTER));
		compsActionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "cross-group compatibility rule", CrossGroupCompatibilityRuleSettingsItem.STRING_CONVERTER));

		compsActionList.bindAddButton(addCrossGroupCompatibilityRuleBtn);
		compsActionList.bindEditButton(editCrossGroupCompatibilityRuleBtn);
		compsActionList.bindRemoveButton(removeCrossGroupCompatibilityRuleBtn);

		FxActions.setStandardMouseAndKeyboardSupport(crossGroupCompatibilityRulesTableView,
				addCrossGroupCompatibilityRuleBtn,
				editCrossGroupCompatibilityRuleBtn,
				removeCrossGroupCompatibilityRuleBtn);
	}
}
