package de.subcentral.watcher.controller.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer.LanguageFormat;
import de.subcentral.core.standardizing.LocaleSubtitleLanguageStandardizer.LanguagePattern;
import de.subcentral.fx.FXUtil;
import de.subcentral.watcher.settings.WatcherSettings;

public class SubtitleLanguageStandardizingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private ScrollPane									subLangStandardizingSettingsPane;
	@FXML
	private ListView<Locale>							parsingLangsListView;
	@FXML
	private Button										addParsingLangBtn;
	@FXML
	private Button										editParsingLangBtn;
	@FXML
	private Button										deleteParsingLangBtn;
	@FXML
	private TableView<LanguagePattern>					langPatternsTableView;
	@FXML
	private TableColumn<LanguagePattern, Pattern>		langPatternsPatternColumn;
	@FXML
	private TableColumn<LanguagePattern, Locale>		langPatternsLangColumn;
	@FXML
	private Button										addLangPatternBtn;
	@FXML
	private Button										editLangPatternBtn;
	@FXML
	private Button										deleteLangPatternBtn;
	@FXML
	private Button										moveUpLangPatternBtn;
	@FXML
	private Button										moveDownLangPatternBtn;
	@FXML
	private ChoiceBox<LanguageFormat>					outputLangFormatChoiceBox;
	@FXML
	private ComboBox<Locale>							outputLangComboBox;
	@FXML
	private TableView<LangName>							langNamesTableView;
	@FXML
	private TableColumn<LangName, Locale>				langNamesLangColumn;
	@FXML
	private TableColumn<LangName, String>				langNamesNameColumn;
	@FXML
	private Button										addLangNameBtn;
	@FXML
	private Button										editLangNameBtn;
	@FXML
	private Button										deleteLangNameBtn;
	@FXML
	private TextField									testingInputTxtFld;
	@FXML
	private TextField									testingOutputTxtFld;

	private Binding<LocaleSubtitleLanguageStandardizer>	standardizer;

	public SubtitleLanguageStandardizingSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public ScrollPane getSectionRootPane()
	{
		return subLangStandardizingSettingsPane;
	}

	@Override
	protected void doInitialize() throws Exception
	{
		initComponents();
		initData();
		initBindings();
	}

	private void initComponents()
	{
		parsingLangsListView.setCellFactory(new Callback<ListView<Locale>, ListCell<Locale>>()
		{
			@Override
			public ListCell<Locale> call(ListView<Locale> p)
			{
				return new ListCell<Locale>()
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
			}
		});

		langPatternsPatternColumn.setCellValueFactory(new Callback<CellDataFeatures<LanguagePattern, Pattern>, ObservableValue<Pattern>>()
		{
			@Override
			public ObservableValue<Pattern> call(CellDataFeatures<LanguagePattern, Pattern> param)
			{
				return FXUtil.createConstantBinding(param.getValue().getPattern());
			}
		});
		langPatternsLangColumn.setCellValueFactory(new Callback<CellDataFeatures<LanguagePattern, Locale>, ObservableValue<Locale>>()
		{
			@Override
			public ObservableValue<Locale> call(CellDataFeatures<LanguagePattern, Locale> param)
			{
				return FXUtil.createConstantBinding(param.getValue().getLanguage());
			}
		});
		langPatternsLangColumn.setCellFactory(new Callback<TableColumn<LanguagePattern, Locale>, TableCell<LanguagePattern, Locale>>()
		{
			@Override
			public TableCell<LanguagePattern, Locale> call(TableColumn<LanguagePattern, Locale> param)
			{
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
			}
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

		outputLangFormatChoiceBox.getItems().addAll(LanguageFormat.values());

		outputLangComboBox.setConverter(FXUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		outputLangComboBox.getItems().setAll(Locale.getAvailableLocales());
		FXCollections.sort(outputLangComboBox.getItems(), FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR);

		langNamesLangColumn.setCellValueFactory(new Callback<CellDataFeatures<LangName, Locale>, ObservableValue<Locale>>()
		{
			@Override
			public ObservableValue<Locale> call(CellDataFeatures<LangName, Locale> param)
			{
				return FXUtil.createConstantBinding(param.getValue().lang);
			}
		});
		langNamesLangColumn.setCellFactory(new Callback<TableColumn<LangName, Locale>, TableCell<LangName, Locale>>()
		{
			@Override
			public TableCell<LangName, Locale> call(TableColumn<LangName, Locale> param)
			{
				return new TableCell<LangName, Locale>()
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
			}
		});

		langNamesNameColumn.setCellValueFactory(new Callback<CellDataFeatures<LangName, String>, ObservableValue<String>>()
		{
			@Override
			public ObservableValue<String> call(CellDataFeatures<LangName, String> param)
			{
				return FXUtil.createConstantBinding(param.getValue().name);
			}
		});

	}

	private void initData()
	{
		LocaleSubtitleLanguageStandardizer stdzer = WatcherSettings.INSTANCE.getLanguageStandardizer();

		SortedList<Locale> sortedParsingLangs = new SortedList<>(FXCollections.observableArrayList(stdzer.getParsingLanguages()),
				FXUtil.LOCALE_DISPLAY_NAME_COMPARATOR);
		parsingLangsListView.setItems(sortedParsingLangs);

		langPatternsTableView.getItems().setAll(stdzer.getCustomLanguagePatterns());

		outputLangFormatChoiceBox.setValue(stdzer.getOutputLanguageFormat());

		outputLangComboBox.setValue(stdzer.getOutputLanguage());

		ObservableList<LangName> langNames = FXCollections.observableArrayList();
		for (Map.Entry<Locale, String> customName : stdzer.getCustomLanguageNames().entrySet())
		{
			langNames.add(new LangName(customName.getKey(), customName.getValue()));
		}
		SortedList<LangName> sortedLangNames = new SortedList<>(langNames);
		langNamesTableView.setItems(sortedLangNames);
	}

	private void initBindings()
	{
		updateMoveUpAndDownLangPatternButtons();
		langPatternsTableView.getSelectionModel()
				.selectedIndexProperty()
				.addListener((Observable observable) -> updateMoveUpAndDownLangPatternButtons());

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

		standardizer = new ObjectBinding<LocaleSubtitleLanguageStandardizer>()
		{
			{
				super.bind(parsingLangsListView.itemsProperty(),
						langPatternsTableView.itemsProperty(),
						outputLangFormatChoiceBox.valueProperty(),
						outputLangComboBox.valueProperty(),
						langNamesTableView.itemsProperty());
			}

			@Override
			protected LocaleSubtitleLanguageStandardizer computeValue()
			{
				Map<Locale, String> names = new HashMap<>();
				for (LangName p : langNamesTableView.getItems())
				{
					names.put(p.lang, p.name);
				}
				return new LocaleSubtitleLanguageStandardizer(parsingLangsListView.getItems(),
						outputLangFormatChoiceBox.getValue(),
						outputLangComboBox.getValue(),
						langPatternsTableView.getItems(),
						names);
			}
		};
		WatcherSettings.INSTANCE.languageStandardizerProperty().bind(standardizer);

		testingOutputTxtFld.textProperty().bind(new StringBinding()
		{
			{
				super.bind(testingInputTxtFld.textProperty(), standardizer);
			}

			@Override
			protected String computeValue()
			{
				Subtitle sub = new Subtitle(null, testingInputTxtFld.getText());
				standardizer.getValue().standardize(sub, new ArrayList<>(1));
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

	private final static class LangName implements Comparable<LangName>
	{
		private final Locale	lang;
		private final String	name;

		private LangName(Locale lang, String name)
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
			if (obj instanceof LangName)
			{
				LangName o = (LangName) obj;
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
			return MoreObjects.toStringHelper(LangName.class).omitNullValues().add("lang", lang).add("name", name).toString();
		}

		@Override
		public int compareTo(LangName o)
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
