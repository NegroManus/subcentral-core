package de.subcentral.watcher.dialogs;

import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Window;

public class StandardReleaseEditController extends BeanEditController<StandardRelease>
{
	// View
	@FXML
	private TextField	tagsTxtFld;
	@FXML
	private TextField	groupTxtFld;
	@FXML
	private RadioButton	ifGuessingRadioBtn;
	@FXML
	private RadioButton	alwaysRadioBtn;

	public StandardReleaseEditController(StandardRelease commonReleaseDef, Window window)
	{
		super(commonReleaseDef, window);
	}

	@Override
	protected String getTitle()
	{
		if (bean == null)
		{
			return "Add standard release";
		}
		else
		{
			return "Edit standard release";
		}
	}

	@Override
	protected String getImagePath()
	{
		return "release_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return tagsTxtFld;
	}

	@Override
	protected void initComponents()
	{
		ToggleGroup scopeToggleGrp = new ToggleGroup();
		scopeToggleGrp.getToggles().addAll(ifGuessingRadioBtn, alwaysRadioBtn);

		// Set initial values
		List<Tag> initialTags;
		Group initialGroup;
		Toggle initialScope;
		if (bean == null)
		{
			initialTags = ImmutableList.of();
			initialGroup = null;
			initialScope = ifGuessingRadioBtn;
		}
		else
		{
			initialTags = bean.getRelease().getTags();
			initialGroup = bean.getRelease().getGroup();
			switch (bean.getScope())
			{
				case IF_GUESSING:
					initialScope = ifGuessingRadioBtn;
					break;
				case ALWAYS:
					initialScope = alwaysRadioBtn;
					break;
				default:
					initialScope = ifGuessingRadioBtn;
			}
		}
		ListProperty<Tag> tags = SubCentralFxUtil.tagPropertyForTextField(tagsTxtFld, initialTags);
		Property<Group> group = SubCentralFxUtil.groupPropertyForTextField(groupTxtFld, initialGroup);
		scopeToggleGrp.selectToggle(initialScope);

		// Bindings
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		// At least tags need to be specified
		applyButton.disableProperty().bind(tags.emptyProperty());

		// ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				Scope scope;
				if (scopeToggleGrp.getSelectedToggle() == alwaysRadioBtn)
				{
					scope = Scope.ALWAYS;
				}
				else
				{
					scope = Scope.IF_GUESSING;
				}
				return new StandardRelease(tags.get(), group.getValue(), scope);
			}
			return null;
		});
	}
}