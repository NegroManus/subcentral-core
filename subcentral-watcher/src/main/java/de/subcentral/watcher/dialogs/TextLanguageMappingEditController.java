package de.subcentral.watcher.dialogs;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.settings.PatternToLanguageMapping;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class TextLanguageMappingEditController extends AbstractBeanEditController<PatternToLanguageMapping>
{
	@FXML
	private RadioButton			literalRadioBtn;
	@FXML
	private RadioButton			simplePatternRadioBtn;
	@FXML
	private RadioButton			regexRadioBtn;
	@FXML
	private TextField			textTxtFld;
	@FXML
	private Text				patternErrorTxt;
	@FXML
	private ComboBox<Locale>	langComboBox;

	public TextLanguageMappingEditController(PatternToLanguageMapping bean, Window window)
	{
		super(bean, window);
	}

	@Override
	protected String getTitle()
	{
		return bean == null ? "Add text to language mapping" : "Edit text to language mapping";
	}

	@Override
	protected String getImagePath()
	{
		return "usa_flag_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return textTxtFld;
	}

	@Override
	protected void initComponents()
	{
		// initialize
		langComboBox.setItems(FxUtil.createListOfAvailableLocales(false, true, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
		langComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);

		// Set initial values

		ToggleGroup modeToggleGrp = new ToggleGroup();
		modeToggleGrp.getToggles().setAll(literalRadioBtn, simplePatternRadioBtn, regexRadioBtn);

		if (bean != null)
		{
			switch (bean.getPattern().getMode())
			{
				case LITERAL:
					modeToggleGrp.selectToggle(literalRadioBtn);
					break;
				case SIMPLE:
					modeToggleGrp.selectToggle(simplePatternRadioBtn);
					break;
				case REGEX:
					modeToggleGrp.selectToggle(regexRadioBtn);
					break;
				default:
					modeToggleGrp.selectToggle(literalRadioBtn);
			}
			textTxtFld.setText(bean.getPattern().getPattern());
			langComboBox.setValue(bean.getLanguage());
		}
		else
		{
			modeToggleGrp.selectToggle(literalRadioBtn);
		}

		// Bindings
		final Binding<UserPattern> patternBinding = FxUtil.createUiPatternTextFieldBinding(modeToggleGrp, literalRadioBtn, simplePatternRadioBtn, regexRadioBtn, textTxtFld, patternErrorTxt);

		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(patternBinding, textTxtFld.textProperty(), langComboBox.valueProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return patternBinding.getValue() == null || StringUtils.isBlank(textTxtFld.getText()) || langComboBox.getValue() == null;
			}
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				return new PatternToLanguageMapping(patternBinding.getValue(), langComboBox.getValue());
			}
			return null;
		});
	}
}