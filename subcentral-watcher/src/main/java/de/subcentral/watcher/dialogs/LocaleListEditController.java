package de.subcentral.watcher.dialogs;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Window;

public class LocaleListEditController extends BeanEditController<List<Locale>>
{
	@FXML
	private ListView<Locale>	langsListView;
	@FXML
	private Button				moveDownLangBtn;
	@FXML
	private Button				moveUpLangBtn;
	@FXML
	private ComboBox<Locale>	addableLangsComboBox;
	@FXML
	private Button				addLangBtn;
	@FXML
	private Button				removeLangBtn;

	LocaleListEditController(List<Locale> bean, Window window)
	{
		super(Objects.requireNonNull(bean), window);
	}

	@Override
	protected String getTitle()
	{
		return "Edit languages";
	}

	@Override
	protected String getImagePath()
	{
		return "usa_flag_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return langsListView;
	}

	@Override
	protected void initComponents()
	{
		// Set initial values
		langsListView.setItems(initLangList());
		langsListView.setCellFactory((ListView<Locale> param) ->
		{
			return new ListCell<Locale>()
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

		addableLangsComboBox.setItems(initAddableLangList());
		addableLangsComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);

		FxUtil.bindMoveButtonsForSingleSelection(langsListView, moveUpLangBtn, moveDownLangBtn);

		// Bindings
		addLangBtn.disableProperty().bind(addableLangsComboBox.getSelectionModel().selectedItemProperty().isNull());
		addLangBtn.setOnAction((ActionEvent) ->
		{
			// remove lang from addable langs
			Locale langToAdd = FxUtil.handleDelete(addableLangsComboBox);
			// add lang to lang list
			FxUtil.handleDistinctAdd(langsListView, Optional.of(langToAdd));
		});

		removeLangBtn.disableProperty().bind(langsListView.getSelectionModel().selectedItemProperty().isNull());
		removeLangBtn.setOnAction((ActionEvent) ->
		{
			// remove lang from lang list
			Locale removedLang = FxUtil.handleDelete(langsListView);
			// add lang to addable langs
			addableLangsComboBox.getItems().add(removedLang);
			// After adding a language to the addable language that list needs to be sorted again
			addableLangsComboBox.getItems().sort(FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR);
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				return langsListView.getItems();
			}
			return null;
		});
	}

	private ObservableList<Locale> initLangList()
	{
		return FXCollections.observableArrayList(bean);
	}

	private ObservableList<Locale> initAddableLangList()
	{
		ObservableList<Locale> addableLangList = FxUtil.createListOfAvailableLocales(false, false, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR);
		// already selected langs are not addable
		addableLangList.removeAll(bean);
		return addableLangList;
	}
}