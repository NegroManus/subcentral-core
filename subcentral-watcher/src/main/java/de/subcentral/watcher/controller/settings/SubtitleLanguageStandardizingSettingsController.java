package de.subcentral.watcher.controller.settings;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

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
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.fx.FXUtil;
import de.subcentral.fx.SubCentralFXUtil;
import de.subcentral.fx.WatcherDialogs;
import de.subcentral.watcher.settings.LanguageTextMapping;
import de.subcentral.watcher.settings.LocaleLanguageReplacerSettings;
import de.subcentral.watcher.settings.WatcherSettings;

public class SubtitleLanguageStandardizingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private BorderPane									subLangStandardizingSettingsPane;
	@FXML
	private TextField									parsingLangsTxtFld;
	@FXML
	private Button										editParsingLangsBtn;
	@FXML
	private TableView<LanguagePattern>					textLangMappingsTableView;
	@FXML
	private TableColumn<LanguagePattern, Pattern>		textLangMappingsTextColumn;
	@FXML
	private TableColumn<LanguagePattern, Locale>		textLangMappingsLangColumn;
	@FXML
	private Button										addTextLangMappingBtn;
	@FXML
	private Button										editTextLangMappingBtn;
	@FXML
	private Button										removeTextLangMappingBtn;
	@FXML
	private Button										moveUpTextLangMappingBtn;
	@FXML
	private Button										moveDownTextLangMappingBtn;
	@FXML
	private ChoiceBox<LanguageFormat>					outputLangFormatChoiceBox;
	@FXML
	private ComboBox<Locale>							outputLangComboBox;
	@FXML
	private TableView<LanguageTextMapping>				langTextMappingsTableView;
	@FXML
	private TableColumn<LanguageTextMapping, Locale>	langTextMappingsLangColumn;
	@FXML
	private TableColumn<LanguageTextMapping, String>	langTextMappingsTextColumn;
	@FXML
	private Button										addLangTextMappingBtn;
	@FXML
	private Button										editLangTextMappingBtn;
	@FXML
	private Button										removeLangTextMappingBtn;
	@FXML
	private TextField									testingInputTxtFld;
	@FXML
	private TextField									testingParsedLangTxtFld;
	@FXML
	private TextField									testingOutputTxtFld;

	public SubtitleLanguageStandardizingSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public BorderPane getSectionRootPane()
	{
		return subLangStandardizingSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		LocaleLanguageReplacerSettings settings = WatcherSettings.INSTANCE.getSubtitleLanguageSettings();
		// ParsingLangs
		final TextFormatter<ObservableList<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FXUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER);
		parsingLangsTextFormatter.valueProperty().bindBidirectional(settings.parsingLanguagesProperty());
		parsingLangsTxtFld.setTextFormatter(parsingLangsTextFormatter);

		editParsingLangsBtn.setOnAction((Actionevt) -> {
			Optional<List<Locale>> result = WatcherDialogs.showLocaleListEditor(parsingLangsTextFormatter.getValue());
			if (result.isPresent())
			{
				parsingLangsTextFormatter.setValue(FXCollections.observableArrayList(result.get()));
			}
		});

		// TextLangMappings
		textLangMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguagePattern, Pattern> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getPattern());
		});
		textLangMappingsLangColumn.setCellValueFactory((CellDataFeatures<LanguagePattern, Locale> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getLanguage());
		});
		textLangMappingsLangColumn.setCellFactory((TableColumn<LanguagePattern, Locale> param) -> {
			return new TableCell<LanguagePattern, Locale>()
			{
				@Override
				protected void updateItem(Locale lang, boolean empty)
				{
					super.updateItem(lang, empty);
					if (empty || lang == null)
					{
						setText(null);
						setGraphic(null);
					}
					else
					{
						setText(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
					}
				}
			};
		});
		textLangMappingsTableView.setItems(settings.getCustomLanguagePatterns());

		addTextLangMappingBtn.setOnAction((ActionEvent evt) -> {});

		final BooleanBinding noTextLangMappingSelection = textLangMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
		editTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
		editTextLangMappingBtn.setOnAction((ActionEvent evt) -> {});

		removeTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
		removeTextLangMappingBtn.setOnAction((ActionEvent evt) -> {
			textLangMappingsTableView.getItems().remove(textLangMappingsTableView.getSelectionModel().getSelectedIndex());
		});

		FXUtil.setStandardMouseAndKeyboardSupportForTableView(textLangMappingsTableView, editTextLangMappingBtn, removeTextLangMappingBtn);
		FXUtil.bindMoveButtonsForSingleSelection(textLangMappingsTableView, moveUpTextLangMappingBtn, moveDownTextLangMappingBtn);

		// OutputLangFormat
		outputLangFormatChoiceBox.getItems().setAll(LanguageFormat.values());
		outputLangFormatChoiceBox.setConverter(SubCentralFXUtil.LANGUAGE_FORMAT_STRING_CONVERTER);
		outputLangFormatChoiceBox.valueProperty().bindBidirectional(settings.outputLanguageFormatProperty());

		// OutputLang
		outputLangComboBox.setItems(FXUtil.createListOfAvailableLocales(false, false, FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
		outputLangComboBox.setConverter(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		outputLangComboBox.valueProperty().bindBidirectional(settings.outputLanguageProperty());
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
		langTextMappingsLangColumn.setCellValueFactory((CellDataFeatures<LanguageTextMapping, Locale> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getLanguage());
		});
		langTextMappingsLangColumn.setCellFactory((TableColumn<LanguageTextMapping, Locale> param) -> {
			return new TableCell<LanguageTextMapping, Locale>()
			{
				@Override
				protected void updateItem(Locale lang, boolean empty)
				{
					super.updateItem(lang, empty);
					if (empty || lang == null)
					{
						setText(null);
						setGraphic(null);
					}
					else
					{
						setText(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
					}
				}
			};
		});
		langTextMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguageTextMapping, String> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getText());
		});
		langTextMappingsTableView.setItems(settings.customLanguageTextMappingsProperty());

		addLangTextMappingBtn.setOnAction((ActionEvent) -> {
			Optional<LanguageTextMapping> result = WatcherDialogs.showLanguageTextMappingEditor();
			if (result.isPresent())
			{
				langTextMappingsTableView.getItems().add(result.get());
				FXCollections.sort(langTextMappingsTableView.getItems());
			}
		});

		final BooleanBinding noLangTextMappingSelection = langTextMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
		editLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
		editLangTextMappingBtn.setOnAction((ActionEvent) -> {
			Optional<LanguageTextMapping> result = WatcherDialogs.showLanguageTextMappingEditor(langTextMappingsTableView.getSelectionModel()
					.getSelectedItem());
			if (result.isPresent())
			{
				langTextMappingsTableView.getItems().set(langTextMappingsTableView.getSelectionModel().getSelectedIndex(), result.get());
			}
		});

		removeLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
		removeLangTextMappingBtn.setOnAction((ActionEvent) -> {
			langTextMappingsTableView.getItems().remove(langTextMappingsTableView.getSelectionModel().getSelectedIndex());
		});

		FXUtil.setStandardMouseAndKeyboardSupportForTableView(langTextMappingsTableView, editLangTextMappingBtn, removeLangTextMappingBtn);

		// Testing
		testingOutputTxtFld.textProperty().bind(new StringBinding()
		{
			{
				super.bind(testingInputTxtFld.textProperty(), WatcherSettings.INSTANCE.getSubtitleLanguageStandardizerBinding());
			}

			@Override
			protected String computeValue()
			{
				return WatcherSettings.INSTANCE.getSubtitleLanguageStandardizerBinding().getValue().getReplacer().apply(testingInputTxtFld.getText());
			}
		});
	}

	private static ObservableList<LanguageTextMapping> convertToObservableMappingList(Map<Locale, String> map)
	{
		ObservableList<LanguageTextMapping> mappings = FXCollections.observableArrayList();
		for (Map.Entry<Locale, String> entry : map.entrySet())
		{
			mappings.add(new LanguageTextMapping(entry.getKey(), entry.getValue()));
		}
		FXCollections.sort(mappings);
		return mappings;
	}
}
