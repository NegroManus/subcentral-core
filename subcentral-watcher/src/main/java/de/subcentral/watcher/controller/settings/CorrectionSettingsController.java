package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.dialogs.WatcherDialogs;
import de.subcentral.watcher.dialogs.ImportSettingEntriesController.ImportSettingEntriesParameters;
import de.subcentral.watcher.settings.CorrectionRuleSettingsItem;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingsItem;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingsItem;
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
	private TableView<CorrectionRuleSettingsItem<?, ?>>						correctorsTableView;
	@FXML
	private TableColumn<CorrectionRuleSettingsItem<?, ?>, String>			correctorsTypeColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingsItem<?, ?>, String>			correctorsRuleColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingsItem<?, ?>, Boolean>			correctorsBeforeQueryingColumn;
	@FXML
	private TableColumn<CorrectionRuleSettingsItem<?, ?>, Boolean>			correctorsAfterQueryingColumn;
	@FXML
	private ChoiceBox<Class<? extends CorrectionRuleSettingsItem<?, ?>>>	correctorTypeChoiceBox;
	@FXML
	private Button															addCorrectorButton;
	@FXML
	private Button															editCorrectorButton;
	@FXML
	private Button															removeCorrectorButton;
	@FXML
	private Button															importCorrectorsButton;

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

		correctorsTypeColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingsItem<?, ?>, String> param) -> param.getValue().ruleTypeBinding());

		correctorsRuleColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingsItem<?, ?>, String> param) -> param.getValue().ruleBinding());

		correctorsBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsBeforeQueryingColumn));
		correctorsBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingsItem<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

		correctorsAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsAfterQueryingColumn));
		correctorsAfterQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectionRuleSettingsItem<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

		correctorTypeChoiceBox.getItems().add(SeriesNameCorrectionRuleSettingsItem.class);
		correctorTypeChoiceBox.getItems().add(ReleaseTagsCorrectionRuleSettingsItem.class);
		correctorTypeChoiceBox.setConverter(new CorrectionRuleTypeStringConverter());
		correctorTypeChoiceBox.getSelectionModel().selectFirst();

		addCorrectorButton.disableProperty().bind(correctorTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
		addCorrectorButton.setOnAction((ActionEvent event) ->
		{
			Class<? extends CorrectionRuleSettingsItem<?, ?>> selectedCorrectorType = correctorTypeChoiceBox.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingsItem<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingsItem.class == selectedCorrectorType)
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingsItem.class == selectedCorrectorType)
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
			CorrectionRuleSettingsItem<?, ?> selectedCorrector = correctorsTableView.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectionRuleSettingsItem<?, ?>> result;
			if (SeriesNameCorrectionRuleSettingsItem.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView((SeriesNameCorrectionRuleSettingsItem) selectedCorrector, settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectionRuleSettingsItem.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView((ReleaseTagsCorrectionRuleSettingsItem) selectedCorrector, settingsController.getMainController().getPrimaryStage());
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
			FxUtil.handleConfirmedDelete(correctorsTableView, "correction rule", new CorrectionRuleStringConverter());
		});

		importCorrectorsButton.setOnAction((ActionEvent event) ->
		{
			Optional<ImportSettingEntriesParameters> result = WatcherDialogs.showImportSettingEntriesView(settingsController.getMainController().getPrimaryStage());
			if (result != null)
			{
				System.out.println(result);
			}
		});

		FxUtil.setStandardMouseAndKeyboardSupport(correctorsTableView, editCorrectorButton, removeCorrectorButton);
	}

	private class CorrectionRuleTypeStringConverter extends StringConverter<Class<? extends CorrectionRuleSettingsItem<?, ?>>>
	{
		@Override
		public String toString(Class<? extends CorrectionRuleSettingsItem<?, ?>> type)
		{
			if (type == null)
			{
				return "";
			}
			else if (type == SeriesNameCorrectionRuleSettingsItem.class)
			{
				return SeriesNameCorrectionRuleSettingsItem.getRuleType();
			}
			else if (type == ReleaseTagsCorrectionRuleSettingsItem.class)
			{
				return ReleaseTagsCorrectionRuleSettingsItem.getRuleType();
			}
			return type.getSimpleName();
		}

		@Override
		public Class<? extends CorrectionRuleSettingsItem<?, ?>> fromString(String string)
		{
			// not needed
			throw new UnsupportedOperationException();
		}
	}

	private class CorrectionRuleStringConverter extends StringConverter<CorrectionRuleSettingsItem<?, ?>>
	{
		@Override
		public String toString(CorrectionRuleSettingsItem<?, ?> entry)
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
		public CorrectionRuleSettingsItem<?, ?> fromString(String string)
		{
			// not needed
			throw new UnsupportedOperationException();
		}
	}
}
