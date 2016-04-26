package de.subcentral.watcher.controller.settings;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.action.ActionList;
import de.subcentral.fx.settings.ConfigurationHelper;
import de.subcentral.watcher.dialog.ImportSettingItemsController.ImportSettingItemsParameters;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CorrectorSettingsItem;
import de.subcentral.watcher.settings.ReleaseTagsCorrectorSettingsItem;
import de.subcentral.watcher.settings.SeriesNameCorrectorSettingsItem;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.GridPane;

public class CorrectionSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane												rootPane;
	@FXML
	private TableView<CorrectorSettingsItem<?, ?>>					correctorsTableView;
	@FXML
	private TableColumn<CorrectorSettingsItem<?, ?>, String>		correctorsTypeColumn;
	@FXML
	private TableColumn<CorrectorSettingsItem<?, ?>, String>		correctorsRuleColumn;
	@FXML
	private TableColumn<CorrectorSettingsItem<?, ?>, Boolean>		correctorsBeforeQueryingColumn;
	@FXML
	private TableColumn<CorrectorSettingsItem<?, ?>, Boolean>		correctorsAfterQueryingColumn;
	@FXML
	private ChoiceBox<Class<? extends CorrectorSettingsItem<?, ?>>>	correctorTypeChoiceBox;
	@FXML
	private Button													addCorrectorButton;
	@FXML
	private Button													editCorrectorButton;
	@FXML
	private Button													removeCorrectorButton;
	@FXML
	private Button													importCorrectorsButton;

	public CorrectionSettingsController(SettingsController settingsController)
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
		// Correctors table
		correctorsTypeColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, String> param) -> param.getValue().ruleType());

		correctorsRuleColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, String> param) -> param.getValue().rule());

		correctorsBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsBeforeQueryingColumn));
		correctorsBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

		correctorsAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsAfterQueryingColumn));
		correctorsAfterQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

		ObservableList<CorrectorSettingsItem<?, ?>> correctors = SettingsController.SETTINGS.getProcessingSettings().getCorrectionRules().property();
		SortedList<CorrectorSettingsItem<?, ?>> displayCorrectors = FxControlBindings.sortableTableView(correctorsTableView, correctors);

		correctorTypeChoiceBox.getItems().add(SeriesNameCorrectorSettingsItem.class);
		correctorTypeChoiceBox.getItems().add(ReleaseTagsCorrectorSettingsItem.class);
		correctorTypeChoiceBox.setConverter(CorrectorSettingsItem.TYPE_STRING_CONVERTER);
		correctorTypeChoiceBox.getSelectionModel().selectFirst();

		// Correctors table buttons
		ActionList<CorrectorSettingsItem<?, ?>> correctorsActionList = new ActionList<>(correctors, correctorsTableView.getSelectionModel(), displayCorrectors);
		correctorsActionList.setNewItemSupplier(() ->
		{
			Class<? extends CorrectorSettingsItem<?, ?>> selectedCorrectorType = correctorTypeChoiceBox.getSelectionModel().getSelectedItem();
			if (SeriesNameCorrectorSettingsItem.class == selectedCorrectorType)
			{
				Optional<SeriesNameCorrectorSettingsItem> r = WatcherDialogs.showSeriesNameCorrectionRuleEditView(getPrimaryStage());
				return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
			}
			else if (ReleaseTagsCorrectorSettingsItem.class == selectedCorrectorType)
			{
				Optional<ReleaseTagsCorrectorSettingsItem> r = WatcherDialogs.showReleaseTagsCorrectionRuleEditView(getPrimaryStage());
				return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
			}
			return Optional.empty();
		});
		correctorsActionList.setItemEditer((CorrectorSettingsItem<?, ?> item) ->
		{
			if (SeriesNameCorrectorSettingsItem.class == item.getClass())
			{
				Optional<SeriesNameCorrectorSettingsItem> r = WatcherDialogs.showSeriesNameCorrectionRuleEditView((SeriesNameCorrectorSettingsItem) item, getPrimaryStage());
				return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
			}
			else if (ReleaseTagsCorrectorSettingsItem.class == item.getClass())
			{
				Optional<ReleaseTagsCorrectorSettingsItem> r = WatcherDialogs.showReleaseTagsCorrectionRuleEditView((ReleaseTagsCorrectorSettingsItem) item, getPrimaryStage());
				return r.isPresent() ? Optional.of(r.get()) : Optional.empty();
			}
			return Optional.empty();
		});
		correctorsActionList.setDistincter(Objects::equals);
		correctorsActionList.setSorter(ObjectUtil.getDefaultOrdering());
		correctorsActionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "correction rule", CorrectorSettingsItem.TYPE_AND_RULE_STRING_CONVERTER));
		correctorsActionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "correction rule", CorrectorSettingsItem.TYPE_AND_RULE_STRING_CONVERTER));

		correctorsActionList.bindAddButton(addCorrectorButton);
		correctorsActionList.bindEditButton(editCorrectorButton);
		correctorsActionList.bindRemoveButton(removeCorrectorButton);

		FxActions.setStandardMouseAndKeyboardSupport(correctorsTableView, addCorrectorButton, editCorrectorButton, removeCorrectorButton);

		importCorrectorsButton.setOnAction((ActionEvent event) ->
		{
			Optional<ImportSettingItemsParameters> result = WatcherDialogs.showImportSettingItemsView(getPrimaryStage(), "Import correction rules");
			if (result.isPresent())
			{
				ImportSettingItemsParameters params = result.get();
				Task<XMLConfiguration> importCorrectorsTask = new Task<XMLConfiguration>()
				{
					@Override
					protected XMLConfiguration call() throws ConfigurationException
					{
						XMLConfiguration cfg;
						switch (params.getSourceType())
						{
							case DEFAULT_SETTINGS:
								cfg = ConfigurationHelper.load(parent.getDefaultSettingsUrl());
								break;
							case FILE:
								cfg = ConfigurationHelper.load(params.getFile());
								break;
							case URL:
								cfg = ConfigurationHelper.load(params.getUrl());
								break;
							default:
								throw new AssertionError();
						}
						return cfg;
					}

					@Override
					protected void succeeded()
					{
						XMLConfiguration cfg = getValue();
						SettingsController.SETTINGS.getProcessingSettings().getCorrectionRules().update(cfg, params.isAddItems(), params.isReplaceItems(), params.isRemoveItems(), Objects::equals);
					}
				};
				execute(importCorrectorsTask);
			}
		});
	}
}
