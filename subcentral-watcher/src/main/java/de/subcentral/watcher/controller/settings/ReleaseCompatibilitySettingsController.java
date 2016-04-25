package de.subcentral.watcher.controller.settings;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.action.AddAction;
import de.subcentral.fx.action.EditAction;
import de.subcentral.fx.action.RemoveAction;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CompatibilitySettingsItem;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.beans.binding.BooleanBinding;
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

		compatibilityEnabledCheckBox.selectedProperty().bindBidirectional(settings.getCompatibilityEnabled().property());

		// Table
		crossGroupCompatibilitiesTableView.setItems(settings.getCompatibilities().property());

		crossGroupCompatibilitiesEnabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(crossGroupCompatibilitiesEnabledColumn));
		crossGroupCompatibilitiesEnabledColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingsItem, Boolean> param) -> param.getValue().enabledProperty());
		crossGroupCompatibilitiesCompatibilityColumn.setCellValueFactory((CellDataFeatures<CompatibilitySettingsItem, String> param) ->
		{
			return FxBindings.immutableObservableValue(((CrossGroupCompatibility) param.getValue().getItem()).toShortString());
		});

		// Buttons
		BooleanBinding noSelection = crossGroupCompatibilitiesTableView.getSelectionModel().selectedItemProperty().isNull();

		Comparator<CompatibilitySettingsItem> comparator = ObjectUtil.getDefaultOrdering();
		boolean distinct = true;
		Consumer<CompatibilitySettingsItem> alreadyExistedInformer = FxActions.createAlreadyExistedInformer(getPrimaryStage(), "cross-group compatibility", CompatibilitySettingsItem.STRING_CONVERTER);
		Predicate<CompatibilitySettingsItem> removeConfirmer = FxActions.createRemoveConfirmer(getPrimaryStage(), "cross-group compatibility", CompatibilitySettingsItem.STRING_CONVERTER);

		AddAction<CompatibilitySettingsItem> addAction = new AddAction<CompatibilitySettingsItem>(crossGroupCompatibilitiesTableView, () ->
		{
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView(getPrimaryStage());
			return result.isPresent() ? Optional.of(new CompatibilitySettingsItem(result.get(), true)) : Optional.empty();
		});
		addAction.setComparator(comparator);
		addAction.setDistinct(distinct);
		addAction.setAlreadyExistedInformer(alreadyExistedInformer);
		addCrossGroupCompatibility.setOnAction(addAction);

		editCrossGroupCompatibility.disableProperty().bind(noSelection);
		EditAction<CompatibilitySettingsItem> editAction = new EditAction<CompatibilitySettingsItem>(crossGroupCompatibilitiesTableView, (CompatibilitySettingsItem item) ->
		{
			Optional<CrossGroupCompatibility> result = WatcherDialogs.showCrossGroupCompatibilityEditView(item.getItem(), getPrimaryStage());
			return result.isPresent() ? Optional.of(new CompatibilitySettingsItem(result.get(), item.isEnabled())) : Optional.empty();
		});
		editAction.setComparator(comparator);
		editAction.setDistinct(distinct);
		editAction.setAlreadyExistedInformer(alreadyExistedInformer);
		editCrossGroupCompatibility.setOnAction(editAction);

		removeCrossGroupCompatibility.disableProperty().bind(noSelection);
		RemoveAction<CompatibilitySettingsItem> removeAction = new RemoveAction<>(crossGroupCompatibilitiesTableView);
		removeAction.setRemoveConfirmer(removeConfirmer);
		removeCrossGroupCompatibility.setOnAction(removeAction);

		FxActions.setStandardMouseAndKeyboardSupport(crossGroupCompatibilitiesTableView, addCrossGroupCompatibility, editCrossGroupCompatibility, removeCrossGroupCompatibility);
	}
}
