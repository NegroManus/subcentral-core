package de.subcentral.watcher.controller.settings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.settings.LanguageTextMapping;
import de.subcentral.watcher.settings.LanguageUserPattern;
import de.subcentral.watcher.settings.LocaleLanguageReplacerSettings;
import de.subcentral.watcher.settings.WatcherSettings;
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

public class SubtitleLanguageStandardizingSettingsController extends AbstractSettingsSectionController
{
    @FXML
    private BorderPane                                    subLangStandardizingSettingsPane;
    @FXML
    private TextField                                     parsingLangsTxtFld;
    @FXML
    private Button                                        editParsingLangsBtn;
    @FXML
    private TableView<LanguageUserPattern>                textLangMappingsTableView;
    @FXML
    private TableColumn<LanguageUserPattern, UserPattern> textLangMappingsTextColumn;
    @FXML
    private TableColumn<LanguageUserPattern, Locale>      textLangMappingsLangColumn;
    @FXML
    private Button                                        addTextLangMappingBtn;
    @FXML
    private Button                                        editTextLangMappingBtn;
    @FXML
    private Button                                        removeTextLangMappingBtn;
    @FXML
    private Button                                        moveUpTextLangMappingBtn;
    @FXML
    private Button                                        moveDownTextLangMappingBtn;
    @FXML
    private ChoiceBox<LanguageFormat>                     outputLangFormatChoiceBox;
    @FXML
    private ComboBox<Locale>                              outputLangComboBox;
    @FXML
    private TableView<LanguageTextMapping>                langTextMappingsTableView;
    @FXML
    private TableColumn<LanguageTextMapping, Locale>      langTextMappingsLangColumn;
    @FXML
    private TableColumn<LanguageTextMapping, String>      langTextMappingsTextColumn;
    @FXML
    private Button                                        addLangTextMappingBtn;
    @FXML
    private Button                                        editLangTextMappingBtn;
    @FXML
    private Button                                        removeLangTextMappingBtn;
    @FXML
    private TextField                                     testingInputTxtFld;
    @FXML
    private TextField                                     testingParsedLangTxtFld;
    @FXML
    private TextField                                     testingOutputTxtFld;

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
        final TextFormatter<ObservableList<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FxUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER);
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
        textLangMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguageUserPattern, UserPattern> param) -> {
            return FxUtil.constantBinding(param.getValue().getPattern());
        });
        textLangMappingsTextColumn.setCellFactory((TableColumn<LanguageUserPattern, UserPattern> param) -> {
            return new TableCell<LanguageUserPattern, UserPattern>()
            {
                @Override
                protected void updateItem(UserPattern pattern, boolean empty)
                {
                    super.updateItem(pattern, empty);
                    if (empty || pattern == null)
                    {
                        setText(null);
                        setGraphic(null);
                    }
                    else
                    {
                        setText(pattern.getPattern() + " (" + pattern.getMode() + ")");
                    }
                }
            };
        });
        textLangMappingsLangColumn.setCellValueFactory((CellDataFeatures<LanguageUserPattern, Locale> param) -> {
            return FxUtil.constantBinding(param.getValue().getLanguage());
        });
        textLangMappingsLangColumn.setCellFactory((TableColumn<LanguageUserPattern, Locale> param) -> {
            return new TableCell<LanguageUserPattern, Locale>()
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
                        setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
                    }
                }
            };
        });
        textLangMappingsTableView.setItems(settings.getCustomLanguagePatterns());

        addTextLangMappingBtn.setOnAction((ActionEvent evt) -> {
            Optional<LanguageUserPattern> result = WatcherDialogs.showTextLanguageMappingEditor();
            FxUtil.handleDistinctAdd(textLangMappingsTableView, result);
        });

        final BooleanBinding noTextLangMappingSelection = textLangMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
        editTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
        editTextLangMappingBtn.setOnAction((ActionEvent evt) -> {
            Optional<LanguageUserPattern> result = WatcherDialogs
                    .showTextLanguageMappingEditor(textLangMappingsTableView.getSelectionModel().getSelectedItem());
            FxUtil.handleDistinctEdit(textLangMappingsTableView, result);
        });

        removeTextLangMappingBtn.disableProperty().bind(noTextLangMappingSelection);
        removeTextLangMappingBtn.setOnAction((ActionEvent evt) -> {
            FxUtil.handleDelete(textLangMappingsTableView, "text to language mapping", LanguageUserPattern.STRING_CONVERTER);
        });

        FxUtil.setStandardMouseAndKeyboardSupportForTableView(textLangMappingsTableView, editTextLangMappingBtn, removeTextLangMappingBtn);
        FxUtil.bindMoveButtonsForSingleSelection(textLangMappingsTableView, moveUpTextLangMappingBtn, moveDownTextLangMappingBtn);

        // OutputLangFormat
        outputLangFormatChoiceBox.getItems().setAll(LanguageFormat.values());
        outputLangFormatChoiceBox.setConverter(SubCentralFxUtil.LANGUAGE_FORMAT_STRING_CONVERTER);
        outputLangFormatChoiceBox.valueProperty().bindBidirectional(settings.outputLanguageFormatProperty());

        // OutputLang
        outputLangComboBox.setItems(FxUtil.createListOfAvailableLocales(false, false, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
        outputLangComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);
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
            return FxUtil.constantBinding(param.getValue().getLanguage());
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
                        setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
                    }
                }
            };
        });
        langTextMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguageTextMapping, String> param) -> {
            return FxUtil.constantBinding(param.getValue().getText());
        });
        langTextMappingsTableView.setItems(settings.customLanguageTextMappingsProperty());

        addLangTextMappingBtn.setOnAction((ActionEvent) -> {
            Optional<LanguageTextMapping> result = WatcherDialogs.showLanguageTextMappingEditor();
            FxUtil.handleDistinctAdd(langTextMappingsTableView, result);
            FXCollections.sort(langTextMappingsTableView.getItems());
        });

        final BooleanBinding noLangTextMappingSelection = langTextMappingsTableView.getSelectionModel().selectedItemProperty().isNull();
        editLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
        editLangTextMappingBtn.setOnAction((ActionEvent) -> {
            Optional<LanguageTextMapping> result = WatcherDialogs
                    .showLanguageTextMappingEditor(langTextMappingsTableView.getSelectionModel().getSelectedItem());
            FxUtil.handleDistinctEdit(langTextMappingsTableView, result);
            FXCollections.sort(langTextMappingsTableView.getItems());
        });

        removeLangTextMappingBtn.disableProperty().bind(noLangTextMappingSelection);
        removeLangTextMappingBtn.setOnAction((ActionEvent) -> {
            FxUtil.handleDelete(langTextMappingsTableView, "language to text mapping", LanguageTextMapping.STRING_CONVERTER);
        });

        FxUtil.setStandardMouseAndKeyboardSupportForTableView(langTextMappingsTableView, editLangTextMappingBtn, removeLangTextMappingBtn);

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
}
