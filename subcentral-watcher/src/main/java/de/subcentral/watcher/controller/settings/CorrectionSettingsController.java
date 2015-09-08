package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.WatcherFxUtil;
import de.subcentral.watcher.settings.CorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingEntry;
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

public class CorrectionSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane														rootPane;
	@FXML
	private TableView<CorrectionRuleSettingEntry<?, ?>>						standardizersTableView;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, String>			standardizersTypeColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, String>			standardizersRuleColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, Boolean>			standardizersBeforeQueryingColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, Boolean>			standardizersAfterQueryingColumn;
	@FXML
	private ChoiceBox<Class<? extends CorrectionRuleSettingEntry<?, ?>>>	standardizerTypeChoiceBox;
	@FXML
	private Button															addStandardizerButton;
	@FXML
	private Button															editStandardizerButton;
	@FXML
	private Button															removeStandardizerButton;

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
	protected void doInitialize() throws Exception
	{
		// Standardizers
		standardizersTableView.setItems(WatcherSettings.INSTANCE.getProcessingSettings().correctionRulesProperty());

		standardizersTypeColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, String> param) -> param.getValue().ruleTypeStringBinding());

		standardizersRuleColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, String> param) -> param.getValue().ruleStringBinding());

		standardizersBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(standardizersBeforeQueryingColumn));
		standardizersBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

		standardizersAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(standardizersAfterQueryingColumn));
		standardizersAfterQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

		standardizerTypeChoiceBox.getItems().add(SeriesNameCorrectionRuleSettingEntry.class);
		standardizerTypeChoiceBox.getItems().add(ReleaseTagsCorrectionRuleSettingEntry.class);
		standardizerTypeChoiceBox.setConverter(new StringConverter<Class<? extends CorrectionRuleSettingEntry<?, ?>>>()
		{
			@Override
			public String toString(Class<? extends CorrectionRuleSettingEntry<?, ?>> type)
			{
				return WatcherFxUtil.standardizingRuleTypeToString(type) + " rule";
			}

			@Override
			public Class<? extends CorrectionRuleSettingEntry<?, ?>> fromString(String string)
			{
				// not needed
				throw new UnsupportedOperationException();
			}
		});
		standardizerTypeChoiceBox.getSelectionModel().selectFirst();

		addStandardizerButton.disableProperty().bind(standardizerTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
		addStandardizerButton.setOnAction((ActionEvent event) ->
		{
			Class<? extends CorrectionRuleSettingEntry<?, ?>> selectedStandardizerType = standardizerTypeChoiceBox.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingEntry<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingEntry.class == selectedStandardizerType)
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingEntry.class == selectedStandardizerType)
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else
			{
				result = Optional.empty();
			}
			FxUtil.handleDistinctAdd(standardizersTableView, result);
		});

		final BooleanBinding noSelection = standardizersTableView.getSelectionModel().selectedItemProperty().isNull();

		editStandardizerButton.disableProperty().bind(noSelection);
		editStandardizerButton.setOnAction((ActionEvent event) ->
		{
			CorrectionRuleSettingEntry<?, ?> selectedStandardizer = standardizersTableView.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingEntry<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingEntry.class == selectedStandardizer.getClass())
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView((SeriesNameCorrectionRuleSettingEntry) selectedStandardizer, settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingEntry.class == selectedStandardizer.getClass())
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView((ReleaseTagsCorrectionRuleSettingEntry) selectedStandardizer, settingsController.getMainController().getPrimaryStage());
			}
			else
			{
				result = Optional.empty();
			}
			FxUtil.handleDistinctEdit(standardizersTableView, result);
		});

		removeStandardizerButton.disableProperty().bind(noSelection);
		removeStandardizerButton.setOnAction((ActionEvent event) ->
		{
			FxUtil.handleDelete(standardizersTableView, "standardizing rule", new StringConverter<CorrectionRuleSettingEntry<?, ?>>()
			{
				@Override
				public String toString(CorrectionRuleSettingEntry<?, ?> entry)
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Rule type: ");
					sb.append(entry.ruleTypeStringBinding().get());
					sb.append("\n");
					sb.append("Rule: ");
					sb.append(entry.ruleStringBinding().get());
					return sb.toString();
				}

				@Override
				public CorrectionRuleSettingEntry<?, ?> fromString(String string)
				{
					// not needed
					throw new UnsupportedOperationException();
				}
			});
		});

		FxUtil.setStandardMouseAndKeyboardSupportForTableView(standardizersTableView, editStandardizerButton, removeStandardizerButton);
	}
}
