package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.watcher.dialog.ImportSettingEntriesController.ImportSettingEntriesParameters;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.CorrectorSettingsItem;
import de.subcentral.watcher.settings.ReleaseTagsCorrectorSettingsItem;
import de.subcentral.watcher.settings.SeriesNameCorrectorSettingsItem;
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
		// Correctors
		correctorsTableView.setItems(SettingsController.SETTINGS.getProcessingSettings().getCorrectionRules().property());

		correctorsTypeColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, String> param) -> param.getValue().ruleTypeBinding());

		correctorsRuleColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, String> param) -> param.getValue().ruleBinding());

		correctorsBeforeQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsBeforeQueryingColumn));
		correctorsBeforeQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, Boolean> param) -> param.getValue().beforeQueryingProperty());

		correctorsAfterQueryingColumn.setCellFactory(CheckBoxTableCell.forTableColumn(correctorsAfterQueryingColumn));
		correctorsAfterQueryingColumn.setCellValueFactory((CellDataFeatures<CorrectorSettingsItem<?, ?>, Boolean> param) -> param.getValue().afterQueryingProperty());

		correctorTypeChoiceBox.getItems().add(SeriesNameCorrectorSettingsItem.class);
		correctorTypeChoiceBox.getItems().add(ReleaseTagsCorrectorSettingsItem.class);
		correctorTypeChoiceBox.setConverter(new CorrectionRuleTypeStringConverter());
		correctorTypeChoiceBox.getSelectionModel().selectFirst();

		addCorrectorButton.disableProperty().bind(correctorTypeChoiceBox.getSelectionModel().selectedItemProperty().isNull());
		addCorrectorButton.setOnAction((ActionEvent event) ->
		{
			Class<? extends CorrectorSettingsItem<?, ?>> selectedCorrectorType = correctorTypeChoiceBox.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectorSettingsItem<?, ?>> result;
			if (SeriesNameCorrectorSettingsItem.class == selectedCorrectorType)
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView(settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectorSettingsItem.class == selectedCorrectorType)
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
			CorrectorSettingsItem<?, ?> selectedCorrector = correctorsTableView.getSelectionModel().getSelectedItem();
			Optional<? extends CorrectorSettingsItem<?, ?>> result;
			if (SeriesNameCorrectorSettingsItem.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showSeriesNameCorrectionRuleEditView((SeriesNameCorrectorSettingsItem) selectedCorrector, settingsController.getMainController().getPrimaryStage());
			}
			else if (ReleaseTagsCorrectorSettingsItem.class == selectedCorrector.getClass())
			{
				result = WatcherDialogs.showReleaseTagsCorrectionRuleEditView((ReleaseTagsCorrectorSettingsItem) selectedCorrector, settingsController.getMainController().getPrimaryStage());
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

	private class CorrectionRuleTypeStringConverter extends StringConverter<Class<? extends CorrectorSettingsItem<?, ?>>>
	{
		@Override
		public String toString(Class<? extends CorrectorSettingsItem<?, ?>> type)
		{
			if (type == null)
			{
				return "";
			}
			else if (type == SeriesNameCorrectorSettingsItem.class)
			{
				return SeriesNameCorrectorSettingsItem.getRuleType();
			}
			else if (type == ReleaseTagsCorrectorSettingsItem.class)
			{
				return ReleaseTagsCorrectorSettingsItem.getRuleType();
			}
			return type.getSimpleName();
		}

		@Override
		public Class<? extends CorrectorSettingsItem<?, ?>> fromString(String string)
		{
			// not needed
			throw new UnsupportedOperationException();
		}
	}

	private class CorrectionRuleStringConverter extends StringConverter<CorrectorSettingsItem<?, ?>>
	{
		@Override
		public String toString(CorrectorSettingsItem<?, ?> entry)
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
		public CorrectorSettingsItem<?, ?> fromString(String string)
		{
			// not needed
			throw new UnsupportedOperationException();
		}
	}
}
