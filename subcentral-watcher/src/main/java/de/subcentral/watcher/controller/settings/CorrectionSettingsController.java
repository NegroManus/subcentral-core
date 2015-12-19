package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.WatcherDialogs;
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
	private TableView<CorrectionRuleSettingEntry<?, ?>>						correctorsTableView;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, String>			correctorsTypeColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, String>			correctorsRuleColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, Boolean>			correctorsBeforeQueryingColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingEntry<?, ?>, Boolean>			correctorsAfterQueryingColumn;
	@FXML
	private ChoiceBox<Class<? extends CorrectionRuleSettingEntry<?, ?>>>	correctorTypeChoiceBox;
	@FXML
	private Button															addCorrectorButton;
	@FXML
	private Button															editCorrectorButton;
	@FXML
	private Button															removeCorrectorButton;

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
		// Correctors
		correctorsTableView.setItems(WatcherSettings.INSTANCE.getProcessingSettings().correctionRulesProperty());

		correctorsTypeColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, String> param) -> param.getValue().ruleTypeBinding());

		correctorsRuleColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, String> param) -> param.getValue().ruleBinding());

		correctorsBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsBeforeQueryingColumn));
		correctorsBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

		correctorsAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsAfterQueryingColumn));
		correctorsAfterQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingEntry<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

		correctorTypeChoiceBox.getItems().add(SeriesNameCorrectionRuleSettingEntry.class);
		correctorTypeChoiceBox.getItems().add(ReleaseTagsCorrectionRuleSettingEntry.class);
		correctorTypeChoiceBox.setConverter(new StringConverter<Class<? extends CorrectionRuleSettingEntry<?, ?>>>()
		{
			@Override
			public String toString(Class<? extends CorrectionRuleSettingEntry<?, ?>> type)
			{
				if (type == null)
				{
					return "";
				}
				else if (type == SeriesNameCorrectionRuleSettingEntry.class)
				{
					return SeriesNameCorrectionRuleSettingEntry.getRuleType();
				}
				else if (type == ReleaseTagsCorrectionRuleSettingEntry.class)
				{
					return ReleaseTagsCorrectionRuleSettingEntry.getRuleType();
				}
				return type.getSimpleName();
			}

			@Override
			public Class<? extends CorrectionRuleSettingEntry<?, ?>> fromString(String string)
			{
				// not needed
				throw new UnsupportedOperationException();
			}
		});
		correctorTypeChoiceBox.getSelectionModel().selectFirst();

		addCorrectorButton.disableProperty().bind(correctorTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
		addCorrectorButton.setOnAction((ActionEvent event) ->
		{
			Class<? extends CorrectionRuleSettingEntry<?, ?>> selectedCorrectorType = correctorTypeChoiceBox.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingEntry<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingEntry.class == selectedCorrectorType)
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingEntry.class == selectedCorrectorType)
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else
			{
				result = Optional.empty();
			}
			FxUtil.handleDistinctAdd(correctorsTableView, result);
		});

		final BooleanBinding noSelection = correctorsTableView.getSelectionModel().selectedItemProperty().isNull();

		editCorrectorButton.disableProperty().bind(noSelection);
		editCorrectorButton.setOnAction((ActionEvent event) ->
		{
			CorrectionRuleSettingEntry<?, ?> selectedCorrector = correctorsTableView.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingEntry<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingEntry.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView((SeriesNameCorrectionRuleSettingEntry) selectedCorrector, settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingEntry.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView((ReleaseTagsCorrectionRuleSettingEntry) selectedCorrector, settingsController.getMainController().getPrimaryStage());
			}
			else
			{
				result = Optional.empty();
			}
			FxUtil.handleDistinctEdit(correctorsTableView, result);
		});

		removeCorrectorButton.disableProperty().bind(noSelection);
		removeCorrectorButton.setOnAction((ActionEvent event) ->
		{
			FxUtil.handleConfirmedDelete(correctorsTableView, "correction rule", new StringConverter<CorrectionRuleSettingEntry<?, ?>>()
			{
				@Override
				public String toString(CorrectionRuleSettingEntry<?, ?> entry)
				{
					StringBuilder sb = new StringBuilder();
					sb.append("Rule type: ");
					sb.append(entry.ruleTypeBinding().get());
					sb.append("\n");
					sb.append("Rule: ");
					sb.append(entry.ruleBinding().get());
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

		FxUtil.setStandardMouseAndKeyboardSupport(correctorsTableView, editCorrectorButton, removeCorrectorButton);
	}
}
