package de.subcentral.watcher.dialog;

import de.subcentral.core.metadata.release.CrossGroupCompatibilityRule;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.dialog.BeanEditController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class CrossGroupCompatibilityRuleEditController extends BeanEditController<CrossGroupCompatibilityRule>
{
	@FXML
	private TextField	compatibleGroupTxtFld;
	@FXML
	private TextField	sourceGroupTxtFld;
	@FXML
	private CheckBox	symmetricCheckBox;

	public CrossGroupCompatibilityRuleEditController(CrossGroupCompatibilityRule bean, Window window)
	{
		super(bean, window);
	}

	@Override
	protected String getTitle()
	{
		if (bean == null)
		{
			return "Add cross-group compatibility";
		}
		else
		{
			return "Edit cross-group compatibility";
		}
	}

	@Override
	protected String getImagePath()
	{
		return "couple_16.png";
	}

	@Override
	protected Node getDefaultFocusNode()
	{
		return sourceGroupTxtFld;
	}

	@Override
	protected void initComponents()
	{
		// Set initial values
		Group initialSourceGroup;
		Group initialCompatibleGroup;
		boolean initialSymmetric;
		if (bean == null)
		{
			initialSourceGroup = null;
			initialCompatibleGroup = null;
			initialSymmetric = false;
		}
		else
		{
			initialSourceGroup = bean.getSourceGroup();
			initialCompatibleGroup = bean.getCompatibleGroup();
			initialSymmetric = bean.isSymmetric();
		}
		Property<Group> sourceGroup = SubCentralFxUtil.groupPropertyForTextField(sourceGroupTxtFld, initialSourceGroup);
		Property<Group> compatibleGroup = SubCentralFxUtil.groupPropertyForTextField(compatibleGroupTxtFld, initialCompatibleGroup);
		symmetricCheckBox.setSelected(initialSymmetric);

		// Do Bindings
		Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
		applyButton.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(compatibleGroup, sourceGroup);
			}

			@Override
			protected boolean computeValue()
			{
				// Both groups need to be specified and not equal to each other
				return sourceGroup.getValue() == null || compatibleGroup.getValue() == null || sourceGroup.getValue().equals(compatibleGroup.getValue());
			}
		});

		// Set ResultConverter
		dialog.setResultConverter(dialogButton ->
		{
			if (dialogButton == ButtonType.APPLY)
			{
				Group sourceGrp = sourceGroup.getValue();
				Group compatibleGrp = compatibleGroup.getValue();
				boolean symmetric = symmetricCheckBox.isSelected();
				return new CrossGroupCompatibilityRule(sourceGrp, compatibleGrp, symmetric);
			}
			return null;
		});
	}
}