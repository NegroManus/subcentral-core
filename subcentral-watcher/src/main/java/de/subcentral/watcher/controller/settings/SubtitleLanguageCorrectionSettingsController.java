package de.subcentral.watcher.controller.settings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.LanguageToTextMapping;
import de.subcentral.watcher.settings.LocaleLanguageReplacerSettings;
import de.subcentral.watcher.settings.PatternToLanguageMapping;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;

public class SubtitleLanguageCorrectionSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private BorderPane											rootPane;
	@FXML
	private TextField											parsingLangsTxtFld;
	@FXML
	private Button												editParsingLangsBtn;
	@FXML
	private TableView<PatternToLanguageMapping>					textLangMappingsTableView;
	@FXML
	private TableColumn<PatternToLanguageMapping, UserPattern>	textLangMappingsTextColumn;
	@FXML
	private TableColumn<PatternToLanguageMapping, Locale>		textLangMappingsLangColumn;
	@FXML
	private Button												addTextLangMappingBtn;
	@FXML
	private Button												editTextLangMappingBtn;
	@FXML
	private Button												removeTextLangMappingBtn;
	@FXML
	private Button												moveUpTextLangMappingBtn;
	@FXML
	private Button												moveDownTextLangMappingBtn;
	@FXML
	private ChoiceBox<LanguageFormat>							outputLangFormatChoiceBox;
	@FXML
	private ComboBox<Locale>									outputLangComboBox;
	@FXML
	private TableView<LanguageToTextMapping>					langTextMappingsTableView;
	@FXML
	private TableColumn<LanguageToTextMapping, Locale>			langTextMappingsLangColumn;
	@FXML
	private TableColumn<LanguageToTextMapping, String>			langTextMappingsTextColumn;
	@FXML
	private Button												addLangTextMappingBtn;
	@FXML
	private Button												editLangTextMappingBtn;
	@FXML
	private Button												removeLangTextMappingBtn;
	@FXML
	private TextField											testingInputTxtFld;
	@FXML
	private TextField											testingParsedLangTxtFld;
	@FXML
	private TextField											testingOutputTxtFld;

	public SubtitleLanguageCorrectionSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public BorderPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void initialize() throws Exception
	{
		final LocaleLanguageReplacerSettings settings = SettingsController.SETTINGS.getProcessingSettings().getSubtitleLanguageCorrectionSettings();

		// ParsingLangs
		final TextFormatter<ObservableList<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FxUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER);
		parsingLangsTextFormatter.valueProperty().bindBidirectional(settings.getParsingLanguages().property());
		parsingLangsTxtFld.setTextFormatter(parsingLangsTextFormatter);

		editParsingLangsBtn.setOnAction((ActionEvent evt) ->
		{
			Optional<List<Locale>> result = WatcherDialogs.showLocaleListEditView(parsingLangsTextFormatter.getValue(), settingsController.getMainController().getPrimaryStage());
			if (result.isPresent())
			{
				parsingLangsTextFormatter.setValue(FXCollections.observableArrayList(result.get()));
			}
		});

		// TextLangMappings
		textLangMappingsTextColumn.setCellValueFactory((CellDataFeatures<PatternToLanguageMapping, UserPattern> param) ->
		{
			return FxBindings.immutableObservableValue(param.getValue().getPattern());
		});
		textLangMappingsTextColumn.setCellFactory((TableColumn<PatternToLanguageMapping, UserPattern> param) -> new PatternToLanguageMappingPatternTableCell());

		textLangMappingsLangColumn.setCellValueFactory((CellDataFeatures<PatternToLanguageMapping, Locale> param) ->
		{
			return FxBindings.immutableObservableValue(param.getValue().getLanguage());
		});
		textLangMappingsLangColumn.setCellFactory((TableColumn<PatternToLanguageMapping, Locale> param) -> new PatternToLanguageMappingLanguageTableCell());

		textLangMappingsTableView.setItems(settings.getCustomLanguagePatterns().property());

		addTextLangMappingBtn.setOnAction((ActionEvent evt) ->
		{
			Optional<PatternToLanguageMapping> result = WatcherDialogs.showTextLanguageMappingEditView(settingsController.getMainController().getPrimaryStage());
			FxActions.handleDistinctAdd(textLangMappingsTableView, result);
		});

		final BooleanBinding noTextLangMappingSelection = textLangMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
		editTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
		editTextLangMappingBtn.setOnAction((ActionEvent evt) ->
		{
			Optional<PatternToLanguageMapping> result = WatcherDialogs.showTextLanguageMappingEditView(textLangMappingsTableView.getSelectionModel().getSelectedItem(),
					settingsController.getMainController().getPrimaryStage());
			FxActions.handleDistinctEdit(textLangMappingsTableView, result);
		});

		removeTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
		removeTextLangMappingBtn.setOnAction((ActionEvent evt) ->
		{
			FxActions.handleConfirmedRemove(textLangMappingsTableView, "text to language mapping", PatternToLanguageMapping.createStringConverter());
		});

		FxActions.setStandardMouseAndKeyboardSupport(textLangMappingsTableView, editTextLangMappingBtn, removeTextLangMappingBtn);
		FxActions.bindMoveButtonsForSingleSelection(textLangMappingsTableView, moveUpTextLangMappingBtn, moveDownTextLangMappingBtn);

		// OutputLangFormat
		outputLangFormatChoiceBox.getItems().setAll(LanguageFormat.values());
		outputLangFormatChoiceBox.setConverter(SubCentralFxUtil.LANGUAGE_FORMAT_STRING_CONVERTER);
		outputLangFormatChoiceBox.valueProperty().bindBidirectional(settings.getOutputLanguageFormat().property());

		// OutputLang
		outputLangComboBox.setItems(FxUtil.createListOfAvailableLocales(false, false, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
		outputLangComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		outputLangComboBox.valueProperty().bindBidirectional(settings.getOutputLanguage().property());
		outputLangComboBox.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(outputLangFormatChoiceBox.valueProperty());
			}

			@Override
			protected boolean computeValue()
			{
				if (outputLangFormatChoiceBox.getValue() == null)
				{
					return true;
				}
				switch (outputLangFormatChoiceBox.getValue())
				{
					case NAME:
						// fall through
					case LANGUAGE_TAG:
						// fall through
					case ISO2:
						// fall through
					case ISO3:
						return true;
					case DISPLAY_NAME:
						// fall through
					case DISPLAY_LANGUAGE:
						return false;
					default:
						return true;
				}
			}
		});

		// LangTextMappings
		langTextMappingsLangColumn.setCellValueFactory((CellDataFeatures<LanguageToTextMapping, Locale> param) ->
		{
			return FxBindings.immutableObservableValue(param.getValue().getLanguage());
		});
		langTextMappingsLangColumn.setCellFactory((TableColumn<LanguageToTextMapping, Locale> param) -> new LanguageToTextMappingLanguageTableCell());

		langTextMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguageToTextMapping, String> param) ->
		{
			return FxBindings.immutableObservableValue(param.getValue().getText());
		});

		langTextMappingsTableView.setItems(settings.getCustomLanguageTextMappings().property());

		addLangTextMappingBtn.setOnAction((ActionEvent) ->
		{
			Optional<LanguageToTextMapping> result = WatcherDialogs.showLanguageTextMappingEditView(settingsController.getMainController().getPrimaryStage());
			FxActions.handleDistinctAdd(langTextMappingsTableView, result);
			FXCollections.sort(langTextMappingsTableView.getItems());
		});

		final BooleanBinding noLangTextMappingSelection = langTextMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
		editLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
		editLangTextMappingBtn.setOnAction((ActionEvent) ->
		{
			Optional<LanguageToTextMapping> result = WatcherDialogs.showLanguageTextMappingEditView(langTextMappingsTableView.getSelectionModel().getSelectedItem(),
					settingsController.getMainController().getPrimaryStage());
			FxActions.handleDistinctEdit(langTextMappingsTableView, result);
			FXCollections.sort(langTextMappingsTableView.getItems());
		});

		removeLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
		removeLangTextMappingBtn.setOnAction((ActionEvent) ->
		{
			FxActions.handleConfirmedRemove(langTextMappingsTableView, "language to text mapping", LanguageToTextMapping.createStringConverter());
		});

		FxActions.setStandardMouseAndKeyboardSupport(langTextMappingsTableView, editLangTextMappingBtn, removeLangTextMappingBtn);

		// Testing
		testingOutputTxtFld.textProperty().bind(new StringBinding()
		{
			{
				super.bind(testingInputTxtFld.textProperty(), settings.subtitleLanguageStandardizerBinding());
			}

			@Override
			protected String computeValue()
			{
				return settings.subtitleLanguageStandardizerBinding().getValue().getReplacer().apply(testingInputTxtFld.getText());
			}
		});
	}

	private static class PatternToLanguageMappingPatternTableCell extends TableCell<PatternToLanguageMapping, UserPattern>
	{
		@Override
		protected void updateItem(UserPattern pattern, boolean empty)
		{
			super.updateItem(pattern, empty);
			if (empty || pattern == null)
			{
				setText(null);
			}
			else
			{
				setText(pattern.getPattern() + " (" + pattern.getMode() + ")");
			}
		}
	}

	private static class PatternToLanguageMappingLanguageTableCell extends TableCell<PatternToLanguageMapping, Locale>
	{
		@Override
		protected void updateItem(Locale lang, boolean empty)
		{
			super.updateItem(lang, empty);
			if (empty || lang == null)
			{
				setText(null);
			}
			else
			{
				setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
			}
		}
	}

	private static class LanguageToTextMappingLanguageTableCell extends TableCell<LanguageToTextMapping, Locale>
	{
		@Override
		protected void updateItem(Locale lang, boolean empty)
		{
			super.updateItem(lang, empty);
			if (empty || lang == null)
			{
				setText(null);
			}
			else
			{
				setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
			}
		}
	}
}
