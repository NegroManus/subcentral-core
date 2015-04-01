package de.subcentral.watcher.controller.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer.LanguageFormat;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer.LanguagePattern;
import de.subcentral.fx.FXUtil;
import de.subcentral.fx.SubCentralFXUtil;
import de.subcentral.watcher.settings.WatcherSettings;

public class SubtitleLanguageStandardizingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private BorderPane								subLangStandardizingSettingsPane;
	@FXML
	private TextField								parsingLangsTxtFld;
	@FXML
	private Button									editParsingLangsBtn;
	@FXML
	private TableView<LanguagePattern>				langPatternsTableView;
	@FXML
	private TableColumn<LanguagePattern, Pattern>	langPatternsPatternColumn;
	@FXML
	private TableColumn<LanguagePattern, Locale>	langPatternsLangColumn;
	@FXML
	private Button									addLangPatternBtn;
	@FXML
	private Button									editLangPatternBtn;
	@FXML
	private Button									deleteLangPatternBtn;
	@FXML
	private Button									moveUpLangPatternBtn;
	@FXML
	private Button									moveDownLangPatternBtn;
	@FXML
	private ChoiceBox<LanguageFormat>				outputLangFormatChoiceBox;
	@FXML
	private ComboBox<Locale>						outputLangComboBox;
	@FXML
	private TableView<LanguageName>					langNamesTableView;
	@FXML
	private TableColumn<LanguageName, Locale>		langNamesLangColumn;
	@FXML
	private TableColumn<LanguageName, String>		langNamesNameColumn;
	@FXML
	private Button									addLangNameBtn;
	@FXML
	private Button									editLangNameBtn;
	@FXML
	private Button									deleteLangNameBtn;
	@FXML
	private TextField								testingInputTxtFld;
	@FXML
	private TextField								testingOutputTxtFld;

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
		LocaleSubtitleLanguageStandardizer initialStdzer = WatcherSettings.INSTANCE.getLanguageStandardizer();

		// ParsingLangs
		TextFormatter<List<Locale>> parsingLangsTextFormatter = new TextFormatter<>(FXUtil.LOCALE_LIST_DISPLAY_NAME_CONVERTER,
				initialStdzer.getParsingLanguages());
		parsingLangsTxtFld.setTextFormatter(parsingLangsTextFormatter);

		// LangPatterns
		langPatternsPatternColumn.setCellValueFactory((CellDataFeatures<LanguagePattern, Pattern> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getPattern());
		});
		langPatternsLangColumn.setCellValueFactory((CellDataFeatures<LanguagePattern, Locale> param) -> {
			return FXUtil.createConstantBinding(param.getValue().getLanguage());
		});
		langPatternsLangColumn.setCellFactory((TableColumn<LanguagePattern, Locale> param) -> {
			return new TableCell<LanguagePattern, Locale>()
			{
				@Override
				protected void updateItem(Locale lang, boolean empty)
				{
					super.updateItem(lang, empty);
					if (lang != null)
					{
						setText(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
					}
				}
			};
		});

		moveUpLangPatternBtn.setOnAction((ActionEvent event) -> {
			int selectedIndex = langPatternsTableView.getSelectionModel().getSelectedIndex();
			Collections.swap(langPatternsTableView.getItems(), selectedIndex, selectedIndex - 1);
			langPatternsTableView.getSelectionModel().select(selectedIndex - 1);
		});
		moveDownLangPatternBtn.setOnAction((ActionEvent event) -> {
			int selectedIndex = langPatternsTableView.getSelectionModel().getSelectedIndex();
			Collections.swap(langPatternsTableView.getItems(), selectedIndex, selectedIndex + 1);
			langPatternsTableView.getSelectionModel().select(selectedIndex + 1);
		});
		updateMoveUpAndDownLangPatternButtons();
		langPatternsTableView.getSelectionModel()
				.selectedIndexProperty()
				.addListener((Observable observable) -> updateMoveUpAndDownLangPatternButtons());
		langPatternsTableView.getItems().setAll(initialStdzer.getCustomLanguagePatterns());

		// OutputLangFormat
		outputLangFormatChoiceBox.getItems().setAll(LanguageFormat.values());
		outputLangFormatChoiceBox.setConverter(SubCentralFXUtil.LANGUAGE_FORMAT_STRING_CONVERTER);
		outputLangFormatChoiceBox.setValue(initialStdzer.getOutputLanguageFormat());

		// OutputLang
		outputLangComboBox.setConverter(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		List<Locale> allLocales = Arrays.asList(Locale.getAvailableLocales());
		allLocales.sort(FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR);
		outputLangComboBox.getItems().setAll(allLocales);
		outputLangComboBox.setValue(initialStdzer.getOutputLanguage());
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

		// LangNames
		langNamesLangColumn.setCellValueFactory((CellDataFeatures<LanguageName, Locale> param) -> {
			return FXUtil.createConstantBinding(param.getValue().lang);
		});
		langNamesLangColumn.setCellFactory((TableColumn<LanguageName, Locale> param) -> {
			return new TableCell<LanguageName, Locale>()
			{
				@Override
				protected void updateItem(Locale lang, boolean empty)
				{
					super.updateItem(lang, empty);
					if (lang != null)
					{
						setText(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER.toString(lang));
					}
				}
			};
		});
		langNamesNameColumn.setCellValueFactory((CellDataFeatures<LanguageName, String> param) -> {
			return FXUtil.createConstantBinding(param.getValue().name);
		});
		langNamesTableView.setItems(convertToLangNameList(initialStdzer.getCustomLanguageNames()));

		// Standardizer
		final Binding<LocaleSubtitleLanguageStandardizer> stdzerBinding = new ObjectBinding<LocaleSubtitleLanguageStandardizer>()
		{
			{
				super.bind(parsingLangsTextFormatter.valueProperty(),
						langPatternsTableView.itemsProperty(),
						outputLangFormatChoiceBox.valueProperty(),
						outputLangComboBox.valueProperty(),
						langNamesTableView.itemsProperty());
			}

			@Override
			protected LocaleSubtitleLanguageStandardizer computeValue()
			{
				Map<Locale, String> names = new HashMap<>();
				for (LanguageName p : langNamesTableView.getItems())
				{
					names.put(p.lang, p.name);
				}
				return new LocaleSubtitleLanguageStandardizer(parsingLangsTextFormatter.getValue(),
						outputLangFormatChoiceBox.getValue(),
						outputLangComboBox.getValue(),
						langPatternsTableView.getItems(),
						names);
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
				Subtitle sub = new Subtitle(null, testingInputTxtFld.getText());
				stdzerBinding.getValue().standardize(sub, new ArrayList<>(1));
				return sub.getLanguage();
			}
		});
	}

	private void updateMoveUpAndDownLangPatternButtons()
	{
		int selectedIndex = langPatternsTableView.getSelectionModel().getSelectedIndex();
		moveUpLangPatternBtn.setDisable(selectedIndex < 1);
		moveDownLangPatternBtn.setDisable(selectedIndex >= langPatternsTableView.getItems().size() - 1 || selectedIndex < 0);
	}

	private static ObservableList<LanguageName> convertToLangNameList(Map<Locale, String> languageNames)
	{
		ObservableList<LanguageName> langNamesList = FXCollections.observableArrayList();
		for (Map.Entry<Locale, String> name : languageNames.entrySet())
		{
			langNamesList.add(new LanguageName(name.getKey(), name.getValue()));
		}
		return new SortedList<>(langNamesList);
	}

	private final static class LanguageName implements Comparable<LanguageName>
	{
		private final Locale	lang;
		private final String	name;

		private LanguageName(Locale lang, String name)
		{
			this.lang = Objects.requireNonNull(lang, "lang");
			this.name = Objects.requireNonNull(name, "name");
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof LanguageName)
			{
				LanguageName o = (LanguageName) obj;
				return lang.equals(o.lang);
			}
			return false;
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(983, 133).append(lang).toHashCode();
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(LanguageName.class).omitNullValues().add("lang", lang).add("name", name).toString();
		}

		@Override
		public int compareTo(LanguageName o)
		{
			// nulls first
			if (o == null)
			{
				return 1;
			}
			return FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR.compare(this.lang, o.lang);
		}
	}
}
