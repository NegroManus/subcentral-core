package de.subcentral.watcher.dialog;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.fx.FxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import de.subcentral.watcher.settings.LanguageToTextMapping;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class LanguageTextMappingEditController extends BeanEditController<LanguageToTextMapping>
{
	@FXML
	private ComboBox<Locale>	langComboBox;
	@FXML
	private TextField			textTxtFld;

	public LanguageTextMappingEditController(LanguageToTextMapping bean, Window window)
	{
		super(bean, window);
	}

	@Override
	protected String getTitle()
	{
		return bean == null ? "Add language to text mapping" : "Edit language to text mapping";
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
		// Set initial values
		langComboBox.setItems(FxUtil.createListOfAvailableLocales(true, true, FxUtil.LOCALE_DISPLAY_NAME_COMPARATOR));
		langComboBox.setConverter(FxUtil.LOCALE_DISPLAY_NAME_CONVERTER);
		langComboBox.setValue(bean != null ? bean.getLanguage() : null);

		textTxtFld.setText(bean != null ? bean.getText() : "");

		// Bindings
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(langComboBox.valueProperty(), textTxtFld.textProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return langComboBox.getValue() == null || StringUtils.isBlank(textTxtFld.getText());
			}
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				return new LanguageToTextMapping(langComboBox.getValue(), textTxtFld.getText());
			}
			return null;
		});
	}
}