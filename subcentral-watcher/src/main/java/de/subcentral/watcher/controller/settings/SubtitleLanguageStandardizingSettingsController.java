package de.subcentral.watcher.controller.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.standardizing.LocaleLanguageReplacer;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguageFormat;
import de.subcentral.core.standardizing.LocaleLanguageReplacer.LanguagePattern;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.fx.FXUtil;
import de.subcentral.fx.SubCentralFXUtil;
import de.subcentral.fx.WatcherDialogs;
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
		final LocaleLanguageReplacer initialReplacer = WatcherSettings.INSTANCE.getLanguageStandardizer().getReplacer();

		// ParsingLangs
		final TextFormatter<List<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FXUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER,
				initialReplacer.getParsingLanguages());
		parsingLangsTxtFld.setTextFormatter(parsingLangsTextFormatter);

		editParsingLangsBtn.setOnAction((Actionevt) -> {
			Optional<List<Locale>> result = WatcherDialogs.showLocaleListEditor(parsingLangsTextFormatter.getValue());
			if (result.isPresent())
			{
				parsingLangsTextFormatter.setValue(result.get());
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
		textLangMappingsTableView.getItems().setAll(initialReplacer.getCustomLanguagePatterns());

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
		outputLangFormatChoiceBox.setValue(initialReplacer.getOutputLanguageFormat());

		// OutputLang
		outputLangComboBox.setItems(FXUtil.createListOfAvailableLocales(false, false, FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
		outputLangComboBox.setConverter(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		outputLangComboBox.setValue(initialReplacer.getOutputLanguage());
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
			return FXUtil.createConstantBinding(param.getValue().language);
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
			return FXUtil.createConstantBinding(param.getValue().text);
		});
		langTextMappingsTableView.setItems(convertToObservableMappingList(initialReplacer.getCustomLanguageTextMappings()));

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

		// Standardizer
		final Binding<LocaleSubtitleLanguageStandardizer> stdzerBinding = new ObjectBinding<LocaleSubtitleLanguageStandardizer>()
		{
			{
				// use getItems() because itemProperty() is an ObjectProperty, not a ListProperty
				super.bind(parsingLangsTextFormatter.valueProperty(),
						textLangMappingsTableView.getItems(),
						outputLangFormatChoiceBox.valueProperty(),
						outputLangComboBox.valueProperty(),
						langTextMappingsTableView.getItems());
			}

			@Override
			protected LocaleSubtitleLanguageStandardizer computeValue()
			{
				Map<Locale, String> langTextMappings = new HashMap<>();
				for (LanguageTextMapping p : langTextMappingsTableView.getItems())
				{
					langTextMappings.put(p.language, p.text);
				}
				return new LocaleSubtitleLanguageStandardizer(new LocaleLanguageReplacer(parsingLangsTextFormatter.getValue(),
						outputLangFormatChoiceBox.getValue(),
						outputLangComboBox.getValue(),
						textLangMappingsTableView.getItems(),
						langTextMappings));
			}
		};
		WatcherSettings.INSTANCE.languageStandardizerProperty().bind(stdzerBinding);

		// Testing
		testingOutputTxtFld.textProperty().bind(new StringBinding()
		{
			{
				super.bind(testingInputTxtFld.textProperty(), stdzerBinding);
			}

			@Override
			protected String computeValue()
			{
				return stdzerBinding.getValue().getReplacer().apply(testingInputTxtFld.getText());
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

	public final static class LanguageTextMapping implements Comparable<LanguageTextMapping>
	{
		private final Locale	language;
		private final String	text;

		public LanguageTextMapping(Locale language, String text)
		{
			this.language = Objects.requireNonNull(language, "language");
			this.text = Objects.requireNonNull(text, "text");
		}

		public Locale getLanguage()
		{
			return language;
		}

		public String getText()
		{
			return text;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof LanguageTextMapping)
			{
				LanguageTextMapping o = (LanguageTextMapping) obj;
				return language.equals(o.language);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(983, 133).append(language).toHashCode();
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(LanguageTextMapping.class).omitNullValues().add("language", language).add("text", text).toString();
		}

		@Override
		public int compareTo(LanguageTextMapping o)
		{
			// nulls first
			if (o == null)
			{
				return 1;
			}
			return FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR.compare(this.language, o.language);
		}
	}
}
