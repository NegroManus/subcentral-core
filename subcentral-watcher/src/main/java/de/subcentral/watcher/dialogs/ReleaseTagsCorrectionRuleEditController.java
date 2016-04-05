package de.subcentral.watcher.dialogs;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.correct.ReleaseTagsCorrector;
import de.subcentral.core.correct.TagsReplacer;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingsItem;
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

public class ReleaseTagsCorrectionRuleEditController extends BeanEditController<ReleaseTagsCorrectionRuleSettingsItem>
{
	@FXML
	private RadioButton	containRadioBtn;
	@FXML
	private RadioButton	equalRadioBtn;
	@FXML
	private TextField	queryTagsTxtFld;
	@FXML
	private CheckBox	ignoreOrderCheckBox;
	@FXML
	private RadioButton	matchRadioBtn;
	@FXML
	private RadioButton	completeRadioBtn;
	@FXML
	private TextField	replacementTxtFld;

	public ReleaseTagsCorrectionRuleEditController(ReleaseTagsCorrectionRuleSettingsItem bean, Window window)
	{
		super(bean, window);
	}

	@Override
	protected String getTitle()
	{
		if (bean == null)
		{
			return "Add correction rule for: " + ReleaseTagsCorrectionRuleSettingsItem.getRuleType();
		}
		else
		{
			return "Edit correction rule for: " + ReleaseTagsCorrectionRuleSettingsItem.getRuleType();
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
		return queryTagsTxtFld;
	}

	@Override
	protected void initComponents()
	{
		ToggleGroup queryModeToggleGrp = new ToggleGroup();
		queryModeToggleGrp.getToggles().addAll(containRadioBtn, equalRadioBtn);

		ToggleGroup replaceWithToggleGrp = new ToggleGroup();
		replaceWithToggleGrp.getToggles().addAll(matchRadioBtn, completeRadioBtn);

		// Initial values
		Toggle initialQueryModeToggle;
		List<Tag> initialQueryTags;
		boolean initialIgnoreOrder;
		Toggle initialReplaceWithToggle;
		List<Tag> initialReplacement;
		if (bean == null)
		{
			initialQueryModeToggle = containRadioBtn;
			initialQueryTags = ImmutableList.of();
			initialIgnoreOrder = false;
			initialReplaceWithToggle = matchRadioBtn;
			initialReplacement = ImmutableList.of();
		}
		else
		{
			TagsReplacer replacer = (TagsReplacer) bean.getValue().getReplacer();
			switch (replacer.getSearchMode())
			{
				case CONTAIN:
					initialQueryModeToggle = containRadioBtn;
					break;
				case EQUAL:
					initialQueryModeToggle = equalRadioBtn;
					break;
				default:
					initialQueryModeToggle = containRadioBtn;
			}
			initialQueryTags = replacer.getSearchTags();
			initialIgnoreOrder = replacer.getIgnoreOrder();
			switch (replacer.getReplaceMode())
			{
				case MATCHED_SEQUENCE:
					initialReplaceWithToggle = matchRadioBtn;
					break;
				case COMPLETE_LIST:
					initialReplaceWithToggle = completeRadioBtn;
					break;
				default:
					initialReplaceWithToggle = matchRadioBtn;
			}
			initialReplacement = replacer.getReplacement();
		}
		queryModeToggleGrp.selectToggle(initialQueryModeToggle);
		ignoreOrderCheckBox.setSelected(initialIgnoreOrder);
		replaceWithToggleGrp.selectToggle(initialReplaceWithToggle);
		ListProperty<Tag> queryTags = SubCentralFxUtil.tagPropertyForTextField(queryTagsTxtFld, initialQueryTags);
		ListProperty<Tag> replacement = SubCentralFxUtil.tagPropertyForTextField(replacementTxtFld, initialReplacement);

		// Bindings
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(queryTags.emptyProperty());

		ignoreOrderCheckBox.disableProperty().bind(queryTags.sizeProperty().lessThan(2));

		matchRadioBtn.setDisable(equalRadioBtn.isSelected());
		equalRadioBtn.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			matchRadioBtn.setDisable(newValue);
			if (newValue)
			{
				replaceWithToggleGrp.selectToggle(completeRadioBtn);
			}
		});

		// ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				TagUtil.SearchMode queryMode;
				if (queryModeToggleGrp.getSelectedToggle() == equalRadioBtn)
				{
					queryMode = SearchMode.EQUAL;
				}
				else
				{
					queryMode = SearchMode.CONTAIN;
				}
				ReplaceMode replaceWith;
				if (replaceWithToggleGrp.getSelectedToggle() == completeRadioBtn)
				{
					replaceWith = ReplaceMode.COMPLETE_LIST;
				}
				else
				{
					replaceWith = ReplaceMode.MATCHED_SEQUENCE;
				}
				boolean ignoreOrder = ignoreOrderCheckBox.isSelected();
				boolean beforeQuerying = (bean == null ? true : bean.isBeforeQuerying());
				boolean afterQuerying = (bean == null ? true : bean.isAfterQuerying());
				return new ReleaseTagsCorrectionRuleSettingsItem(new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceWith, ignoreOrder)),
						beforeQuerying,
						afterQuerying);
			}
			return null;
		});
	}
}