package de.subcentral.watcher.controller.settings;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import de.subcentral.core.correct.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.action.ActionList;
import de.subcentral.fx.action.FxActions;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.LanguageToTextMapping;
import de.subcentral.watcher.settings.LocaleLanguageReplacerSettings;
import de.subcentral.watcher.settings.PatternToLanguageMapping;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
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

public class SubtitleLanguageCorrectionSettingsController extends AbstractSettingsSectionController {
    @FXML
    private BorderPane                                         rootPane;
    @FXML
    private TextField                                          parsingLangsTxtFld;
    @FXML
    private Button                                             editParsingLangsBtn;
    @FXML
    private TableView<PatternToLanguageMapping>                textLangMappingsTableView;
    @FXML
    private TableColumn<PatternToLanguageMapping, UserPattern> textLangMappingsTextColumn;
    @FXML
    private TableColumn<PatternToLanguageMapping, Locale>      textLangMappingsLangColumn;
    @FXML
    private Button                                             addTextLangMappingBtn;
    @FXML
    private Button                                             editTextLangMappingBtn;
    @FXML
    private Button                                             removeTextLangMappingBtn;
    @FXML
    private Button                                             moveUpTextLangMappingBtn;
    @FXML
    private Button                                             moveDownTextLangMappingBtn;
    @FXML
    private ChoiceBox<LanguageFormat>                          outputFormatChoiceBox;
    @FXML
    private ComboBox<Locale>                                   outputLangComboBox;
    @FXML
    private TableView<LanguageToTextMapping>                   langTextMappingsTableView;
    @FXML
    private TableColumn<LanguageToTextMapping, Locale>         langTextMappingsLangColumn;
    @FXML
    private TableColumn<LanguageToTextMapping, String>         langTextMappingsTextColumn;
    @FXML
    private Button                                             addLangTextMappingBtn;
    @FXML
    private Button                                             editLangTextMappingBtn;
    @FXML
    private Button                                             removeLangTextMappingBtn;
    @FXML
    private TextField                                          testingInputTxtFld;
    @FXML
    private TextField                                          testingParsedLangTxtFld;
    @FXML
    private TextField                                          testingOutputTxtFld;

    public SubtitleLanguageCorrectionSettingsController(SettingsController settingsController) {
        super(settingsController);
    }

    @Override
    public BorderPane getContentPane() {
        return rootPane;
    }

    @Override
    protected void initialize() throws Exception {
        LocaleLanguageReplacerSettings settings = SettingsController.SETTINGS.getProcessingSettings().getSubtitleLanguageCorrectionSettings();

        // ParsingLangs
        final TextFormatter<ObservableList<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FxUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER);
        parsingLangsTextFormatter.valueProperty().bindBidirectional(settings.getParsingLanguages().property());
        parsingLangsTxtFld.setTextFormatter(parsingLangsTextFormatter);

        editParsingLangsBtn.setOnAction((ActionEvent evt) -> {
            Optional<List<Locale>> result = WatcherDialogs.showLocaleListEditView(parsingLangsTextFormatter.getValue(), getPrimaryStage());
            if (result.isPresent()) {
                parsingLangsTextFormatter.setValue(FXCollections.observableArrayList(result.get()));
            }
        });

        initTextLangMappingTableView();

        // OutputLangFormat
        outputFormatChoiceBox.getItems().setAll(LanguageFormat.values());
        outputFormatChoiceBox.setConverter(SubCentralFxUtil.LANGUAGE_FORMAT_STRING_CONVERTER);
        outputFormatChoiceBox.valueProperty().bindBidirectional(settings.getOutputFormat().property());

        // OutputLang
        outputLangComboBox.setItems(FxUtil.createListOfAvailableLocales(false, false, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
        outputLangComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);
        outputLangComboBox.valueProperty().bindBidirectional(settings.getOutputLanguage().property());
        outputLangComboBox.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(outputFormatChoiceBox.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                if (outputFormatChoiceBox.getValue() == null) {
                    return true;
                }
                switch (outputFormatChoiceBox.getValue()) {
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

        initLangTextMappingTableView();

        // Testing
        testingOutputTxtFld.textProperty().bind(new StringBinding() {
            {
                super.bind(testingInputTxtFld.textProperty(), settings.subtitleLanguageStandardizerBinding());
            }

            @Override
            protected String computeValue() {
                return settings.subtitleLanguageStandardizerBinding().getValue().getReplacer().apply(testingInputTxtFld.getText());
            }
        });
    }

    private void initTextLangMappingTableView() {
        LocaleLanguageReplacerSettings settings = SettingsController.SETTINGS.getProcessingSettings().getSubtitleLanguageCorrectionSettings();

        // Columns
        textLangMappingsTextColumn.setCellValueFactory((CellDataFeatures<PatternToLanguageMapping, UserPattern> param) -> {
            return FxBindings.immutableObservableValue(param.getValue().getPattern());
        });
        textLangMappingsTextColumn.setCellFactory((TableColumn<PatternToLanguageMapping, UserPattern> param) -> new PatternToLanguageMappingPatternTableCell());

        textLangMappingsLangColumn.setCellValueFactory((CellDataFeatures<PatternToLanguageMapping, Locale> param) -> {
            return FxBindings.immutableObservableValue(param.getValue().getLanguage());
        });
        textLangMappingsLangColumn.setCellFactory((TableColumn<PatternToLanguageMapping, Locale> param) -> new PatternToLanguageMappingLanguageTableCell());

        textLangMappingsTableView.setItems(settings.getCustomLanguagePatterns().property());

        // Table buttons
        ActionList<PatternToLanguageMapping> actionList = new ActionList<>(textLangMappingsTableView);
        actionList.setNewItemSupplier(() -> WatcherDialogs.showTextLanguageMappingEditView(getPrimaryStage()));
        actionList.setItemEditer((PatternToLanguageMapping item) -> WatcherDialogs.showTextLanguageMappingEditView(item, getPrimaryStage()));
        actionList.setDistincter((PatternToLanguageMapping o1, PatternToLanguageMapping o2) -> o1.getPattern().equals(o2.getPattern()));
        actionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "text to language mapping", PatternToLanguageMapping.STRING_CONVERTER));
        actionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "text to language mapping", PatternToLanguageMapping.STRING_CONVERTER));

        actionList.bindAddButton(addTextLangMappingBtn);
        actionList.bindEditButton(editTextLangMappingBtn);
        actionList.bindRemoveButton(removeTextLangMappingBtn);
        actionList.bindMoveButtons(moveUpTextLangMappingBtn, moveDownTextLangMappingBtn);

        FxActions.setStandardMouseAndKeyboardSupport(textLangMappingsTableView, addTextLangMappingBtn, editTextLangMappingBtn, removeTextLangMappingBtn);
    }

    private void initLangTextMappingTableView() {
        LocaleLanguageReplacerSettings settings = SettingsController.SETTINGS.getProcessingSettings().getSubtitleLanguageCorrectionSettings();

        // Columns
        langTextMappingsLangColumn.setCellValueFactory((CellDataFeatures<LanguageToTextMapping, Locale> param) -> {
            return FxBindings.immutableObservableValue(param.getValue().getLanguage());
        });
        langTextMappingsLangColumn.setCellFactory((TableColumn<LanguageToTextMapping, Locale> param) -> new LanguageToTextMappingLanguageTableCell());

        langTextMappingsTextColumn.setCellValueFactory((CellDataFeatures<LanguageToTextMapping, String> param) -> {
            return FxBindings.immutableObservableValue(param.getValue().getText());
        });

        ObservableList<LanguageToTextMapping> mappings = settings.getCustomLanguageTextMappings().property();
        SortedList<LanguageToTextMapping> displayMappings = FxControlBindings.sortableTableView(langTextMappingsTableView, mappings);

        // Table buttons
        ActionList<LanguageToTextMapping> actionList = new ActionList<>(mappings, langTextMappingsTableView.getSelectionModel(), displayMappings);
        actionList.setNewItemSupplier(() -> WatcherDialogs.showLanguageTextMappingEditView(getPrimaryStage()));
        actionList.setItemEditer((LanguageToTextMapping item) -> WatcherDialogs.showLanguageTextMappingEditView(item, getPrimaryStage()));
        actionList.setDistincter((LanguageToTextMapping o1, LanguageToTextMapping o2) -> o1.getLanguage().equals(o2.getLanguage()));
        actionList.setSorter(ObjectUtil.getDefaultOrdering());
        actionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(), "language to text mapping", LanguageToTextMapping.STRING_CONVERTER));
        actionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(), "language to text mapping", LanguageToTextMapping.STRING_CONVERTER));

        actionList.bindAddButton(addLangTextMappingBtn);
        actionList.bindEditButton(editLangTextMappingBtn);
        actionList.bindRemoveButton(removeLangTextMappingBtn);

        FxActions.setStandardMouseAndKeyboardSupport(langTextMappingsTableView, addLangTextMappingBtn, editLangTextMappingBtn, removeLangTextMappingBtn);
    }

    private static class PatternToLanguageMappingPatternTableCell extends TableCell<PatternToLanguageMapping, UserPattern> {
        @Override
        protected void updateItem(UserPattern pattern, boolean empty) {
            super.updateItem(pattern, empty);
            if (empty || pattern == null) {
                setText(null);
            }
            else {
                setText(pattern.getPattern() + " (" + pattern.getMode() + ")");
            }
        }
    }

    private static class PatternToLanguageMappingLanguageTableCell extends TableCell<PatternToLanguageMapping, Locale> {
        @Override
        protected void updateItem(Locale lang, boolean empty) {
            super.updateItem(lang, empty);
            if (empty || lang == null) {
                setText(null);
            }
            else {
                setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
            }
        }
    }

    private static class LanguageToTextMappingLanguageTableCell extends TableCell<LanguageToTextMapping, Locale> {
        @Override
        protected void updateItem(Locale lang, boolean empty) {
            super.updateItem(lang, empty);
            if (empty || lang == null) {
                setText(null);
            }
            else {
                setText(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
            }
        }
    }
}
