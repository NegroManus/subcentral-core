package de.subcentral.watcher.dialog;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.fx.action.FxActions;
import de.subcentral.fx.dialog.BeanEditController;
import de.subcentral.watcher.settings.SeriesNameCorrectorSettingsItem;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class SeriesNameCorrectionRuleEditController extends BeanEditController<SeriesNameCorrectorSettingsItem>
{
	@FXML
	private RadioButton			literalRadioBtn;
	@FXML
	private RadioButton			simplePatternRadioBtn;
	@FXML
	private RadioButton			regexRadioBtn;
	@FXML
	private TextField			namePatternTxtFld;
	@FXML
	private Text				patternErrorTxt;
	@FXML
	private TextField			nameReplacementTxtFld;
	@FXML
	private ListView<String>	aliasNamesReplacementListView;
	@FXML
	private Button				addNameBtn;
	@FXML
	private Button				editNameBtn;
	@FXML
	private Button				removeNameBtn;

	public SeriesNameCorrectionRuleEditController(SeriesNameCorrectorSettingsItem bean, Window window)
	{
		super(bean, window);
	}

	@Override
	protected String getTitle()
	{
		if (bean == null)
		{
			return "Add correction rule for: " + SeriesNameCorrectorSettingsItem.getRuleType();
		}
		else
		{
			return "Edit correction rule for: " + SeriesNameCorrectorSettingsItem.getRuleType();
		}
	}

	@Override
	protected String getImagePath()
	{
		return "edit_text_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return namePatternTxtFld;
	}

	@Override
	protected void initComponents()
	{
		ToggleGroup patternModeToggleGrp = new ToggleGroup();
		patternModeToggleGrp.getToggles().setAll(literalRadioBtn, simplePatternRadioBtn, regexRadioBtn);

		// Initial values
		Toggle initialPatternMode;
		String initialNamePattern;
		String initialNameReplacement;
		ObservableList<String> aliasNamesReplacement;
		if (bean == null)
		{
			initialPatternMode = literalRadioBtn;
			initialNamePattern = null;
			initialNameReplacement = null;
			aliasNamesReplacement = FXCollections.observableArrayList();
		}
		else
		{
			switch (bean.getNameUserPattern().getMode())
			{
				case LITERAL:
					initialPatternMode = literalRadioBtn;
					break;
				case SIMPLE:
					initialPatternMode = simplePatternRadioBtn;
					break;
				case REGEX:
					initialPatternMode = regexRadioBtn;
					break;
				default:
					initialPatternMode = literalRadioBtn;
			}
			initialNamePattern = bean.getNameUserPattern().getPattern();
			initialNameReplacement = bean.getItem().getNameReplacement();
			aliasNamesReplacement = FXCollections.observableArrayList(bean.getItem().getAliasNamesReplacement());
		}
		patternModeToggleGrp.selectToggle(initialPatternMode);
		namePatternTxtFld.setText(initialNamePattern);

		nameReplacementTxtFld.setText(initialNameReplacement);

		// replacement names
		aliasNamesReplacementListView.setCellFactory(TextFieldListCell.forListView(FxUtil.REJECT_BLANK_STRING_CONVERTER));
		aliasNamesReplacementListView.setItems(aliasNamesReplacement);
		addNameBtn.setOnAction((ActionEvent evt) ->
		{
			String newAliasName = StringUtils.isBlank(nameReplacementTxtFld.getText()) ? "alias name" : nameReplacementTxtFld.getText();
			aliasNamesReplacement.add(newAliasName);
			aliasNamesReplacementListView.getSelectionModel().selectLast();
		});

		final BooleanBinding noSelection = aliasNamesReplacementListView.getSelectionModel().selectedItemProperty().isNull();

		editNameBtn.disableProperty().bind(noSelection);
		editNameBtn.setOnAction((ActionEvent evt) ->
		{
			aliasNamesReplacementListView.edit(aliasNamesReplacementListView.getSelectionModel().getSelectedIndex());
		});
		removeNameBtn.disableProperty().bind(noSelection);
		removeNameBtn.setOnAction((ActionEvent evt) ->
		{
			FxActions.remove(aliasNamesReplacementListView);
		});

		FxActions.setStandardMouseAndKeyboardSupport(aliasNamesReplacementListView, addNameBtn, editNameBtn, removeNameBtn, true);

		// Bindings
		Binding<UserPattern> namePatternBinding = FxControlBindings.createUiPatternTextFieldBinding(patternModeToggleGrp,
				literalRadioBtn,
				simplePatternRadioBtn,
				regexRadioBtn,
				namePatternTxtFld,
				patternErrorTxt);

		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(namePatternBinding, nameReplacementTxtFld.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return namePatternBinding.getValue() == null || StringUtils.isBlank(nameReplacementTxtFld.getText());
			}
		});

		// ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				boolean beforeQuerying = (bean == null ? true : bean.isBeforeQuerying());
				boolean afterQuerying = (bean == null ? true : bean.isAfterQuerying());
				return new SeriesNameCorrectorSettingsItem(namePatternBinding.getValue(), nameReplacementTxtFld.getText(), aliasNamesReplacementListView.getItems(), beforeQuerying, afterQuerying);
			}
			return null;
		});

	}
}