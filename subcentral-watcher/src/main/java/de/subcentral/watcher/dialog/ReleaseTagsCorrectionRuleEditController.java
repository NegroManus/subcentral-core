package de.subcentral.watcher.dialog;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.CollectionUtil.ReplaceMode;
import de.subcentral.core.util.CollectionUtil.SearchMode;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import de.subcentral.watcher.settings.ReleaseTagsCorrectorSettingsItem;
import javafx.beans.property.ListProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Window;

public class ReleaseTagsCorrectionRuleEditController extends BeanEditController<ReleaseTagsCorrectorSettingsItem> {
	@FXML
	private RadioButton	containRadioBtn;
	@FXML
	private RadioButton	equalRadioBtn;
	@FXML
	private TextField	searchTagsTxtFld;
	@FXML
	private CheckBox	ignoreOrderCheckBox;
	@FXML
	private RadioButton	matchRadioBtn;
	@FXML
	private RadioButton	completeRadioBtn;
	@FXML
	private TextField	replacementTxtFld;

	public ReleaseTagsCorrectionRuleEditController(ReleaseTagsCorrectorSettingsItem bean, Window window) {
		super(bean, window);
	}

	@Override
	protected String getTitle() {
		if (bean == null) {
			return "Add correction rule for: " + ReleaseTagsCorrectorSettingsItem.getRuleType();
		}
		else {
			return "Edit correction rule for: " + ReleaseTagsCorrectorSettingsItem.getRuleType();
		}
	}

	@Override
	protected String getImagePath() {
		return "edit_text_16.png";
	}

	@Override
	protected Node getDefaultFocusNode() {
		return searchTagsTxtFld;
	}

	@Override
	protected void initComponents() {
		ToggleGroup searchModeToggleGrp = new ToggleGroup();
		searchModeToggleGrp.getToggles().addAll(containRadioBtn, equalRadioBtn);

		ToggleGroup replaceModeToggleGrp = new ToggleGroup();
		replaceModeToggleGrp.getToggles().addAll(matchRadioBtn, completeRadioBtn);

		// Initial values
		Toggle initialSearchModeToggle;
		List<Tag> initialSearchTags;
		boolean initialIgnoreOrder;
		Toggle initialReplaceModeToggle;
		List<Tag> initialReplacement;
		if (bean == null) {
			initialSearchModeToggle = containRadioBtn;
			initialSearchTags = ImmutableList.of();
			initialIgnoreOrder = false;
			initialReplaceModeToggle = matchRadioBtn;
			initialReplacement = ImmutableList.of();
		}
		else {
			TagsReplacer replacer = (TagsReplacer) bean.getItem().getReplacer();
			switch (replacer.getSearchMode()) {
				case CONTAIN:
					initialSearchModeToggle = containRadioBtn;
					break;
				case EQUAL:
					initialSearchModeToggle = equalRadioBtn;
					break;
				default:
					initialSearchModeToggle = containRadioBtn;
			}
			initialSearchTags = replacer.getSearchTags();
			initialIgnoreOrder = replacer.getIgnoreOrder();
			switch (replacer.getReplaceMode()) {
				case MATCHED_SEQUENCE:
					initialReplaceModeToggle = matchRadioBtn;
					break;
				case COMPLETE_LIST:
					initialReplaceModeToggle = completeRadioBtn;
					break;
				default:
					initialReplaceModeToggle = matchRadioBtn;
			}
			initialReplacement = replacer.getReplacement();
		}
		searchModeToggleGrp.selectToggle(initialSearchModeToggle);
		ignoreOrderCheckBox.setSelected(initialIgnoreOrder);
		replaceModeToggleGrp.selectToggle(initialReplaceModeToggle);
		ListProperty<Tag> searchTags = SubCentralFxUtil.tagPropertyForTextField(searchTagsTxtFld, initialSearchTags);
		ListProperty<Tag> replacement = SubCentralFxUtil.tagPropertyForTextField(replacementTxtFld, initialReplacement);

		// Bindings
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(searchTags.emptyProperty());

		ignoreOrderCheckBox.disableProperty().bind(searchTags.sizeProperty().lessThan(2));

		matchRadioBtn.setDisable(equalRadioBtn.isSelected());
		equalRadioBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
			matchRadioBtn.setDisable(newValue);
			if (newValue) {
				replaceModeToggleGrp.selectToggle(completeRadioBtn);
			}
		});

		// ResultConverter
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.APPLY) {
				SearchMode searchMode;
				if (searchModeToggleGrp.getSelectedToggle() == equalRadioBtn) {
					searchMode = SearchMode.EQUAL;
				}
				else {
					searchMode = SearchMode.CONTAIN;
				}
				ReplaceMode replaceMode;
				if (replaceModeToggleGrp.getSelectedToggle() == completeRadioBtn) {
					replaceMode = ReplaceMode.COMPLETE_LIST;
				}
				else {
					replaceMode = ReplaceMode.MATCHED_SEQUENCE;
				}
				boolean ignoreOrder = ignoreOrderCheckBox.isSelected();
				boolean beforeQuerying = (bean == null ? true : bean.isBeforeQuerying());
				boolean afterQuerying = (bean == null ? true : bean.isAfterQuerying());
				return new ReleaseTagsCorrectorSettingsItem(new ReleaseTagsCorrector(new TagsReplacer(searchTags, replacement, searchMode, replaceMode, ignoreOrder)), beforeQuerying, afterQuerying);
			}
			return null;
		});
	}
}