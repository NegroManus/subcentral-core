package de.subcentral.fx;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.AssumeExistence;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.QueryMode;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.standardizing.ReleaseTagsStandardizer;
import de.subcentral.core.standardizing.TagsReplacer;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.settings.ReleaseTagsStandardizerSettingEntry;
import de.subcentral.watcher.settings.SeriesNameStandardizerSettingEntry;

public class WatcherDialogs
{
	private static final Logger	log	= LogManager.getLogger(WatcherDialogs.class);

	private static abstract class AbstractBeanDialogController<T> extends AbstractController
	{
		// Model
		protected final T			bean;

		// View
		protected final Dialog<T>	dialog	= new Dialog<>();
		@FXML
		protected GridPane			inputPane;

		public AbstractBeanDialogController(T bean)
		{
			this.bean = bean;
		}

		public Dialog<T> getDialog()
		{
			return dialog;
		}

		@Override
		protected final void doInitialize()
		{
			initDialog();
			initInputPaneControl();
		}

		private final void initDialog()
		{
			String title = buildTitle();
			dialog.setTitle(title);
			dialog.setHeaderText(title);
			dialog.getDialogPane().setMinWidth(350d);

			// Set the button types.
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(inputPane);

			Platform.runLater(() -> getDefaultFocusNode().requestFocus());
		}

		protected abstract String buildTitle();

		protected abstract Node getDefaultFocusNode();

		protected abstract void initInputPaneControl();
	}

	private static class StandardReleaseDialogController extends AbstractBeanDialogController<StandardRelease>
	{
		// View
		@FXML
		private TextField	tagsTxtFld;
		@FXML
		private TextField	groupTxtFld;
		@FXML
		private RadioButton	ifNoneFoundRadioBtn;
		@FXML
		private RadioButton	alwaysRadioBtn;

		public StandardReleaseDialogController(StandardRelease commonReleaseDef)
		{
			super(commonReleaseDef);
		}

		@Override
		protected String buildTitle()
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
		protected Node getDefaultFocusNode()
		{
			return tagsTxtFld;
		}

		@Override
		protected void initInputPaneControl()
		{
			ToggleGroup assumeExistenceToggleGrp = new ToggleGroup();
			assumeExistenceToggleGrp.getToggles().addAll(ifNoneFoundRadioBtn, alwaysRadioBtn);

			// Set initial values
			List<Tag> initialTags;
			Group initialGroup;
			Toggle initialAssumeExistence;
			if (bean == null)
			{
				initialTags = ImmutableList.of();
				initialGroup = null;
				initialAssumeExistence = ifNoneFoundRadioBtn;
			}
			else
			{
				initialTags = bean.getStandardRelease().getTags();
				initialGroup = bean.getStandardRelease().getGroup();
				switch (bean.getAssumeExistence())
				{
					case IF_NONE_FOUND:
						initialAssumeExistence = ifNoneFoundRadioBtn;
						break;
					case ALWAYS:
						initialAssumeExistence = alwaysRadioBtn;
						break;
					default:
						initialAssumeExistence = ifNoneFoundRadioBtn;
				}
			}
			ListProperty<Tag> tags = SubCentralFXUtil.tagPropertyForTextField(tagsTxtFld, initialTags);
			Property<Group> group = SubCentralFXUtil.groupPropertyForTextField(groupTxtFld, initialGroup);
			assumeExistenceToggleGrp.selectToggle(initialAssumeExistence);

			// Bindings
			Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
			// At least tags need to be specified
			applyButton.disableProperty().bind(tags.emptyProperty());

			// ResultConverter
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.APPLY)
				{
					AssumeExistence assumeExistence;
					if (assumeExistenceToggleGrp.getSelectedToggle() == alwaysRadioBtn)
					{
						assumeExistence = AssumeExistence.ALWAYS;
					}
					else
					{
						assumeExistence = AssumeExistence.IF_NONE_FOUND;
					}
					return new StandardRelease(tags.get(), group.getValue(), assumeExistence);
				}
				return null;
			});
		}
	}

	private static class CrossGroupCompatibilityDialogController extends AbstractBeanDialogController<CrossGroupCompatibility>
	{
		@FXML
		private TextField	compatibleGroupTxtFld;
		@FXML
		private TextField	sourceGroupTxtFld;
		@FXML
		private CheckBox	symmetricCheckBox;

		public CrossGroupCompatibilityDialogController(CrossGroupCompatibility bean)
		{
			super(bean);
		}

		@Override
		protected String buildTitle()
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
		protected Node getDefaultFocusNode()
		{
			return compatibleGroupTxtFld;
		}

		@Override
		protected void initInputPaneControl()
		{
			// Set initial values
			Group initialCompatibleGroup = null;
			Group initialSourceGroup = null;
			boolean initialSymmetric = false;
			if (bean == null)
			{
				initialCompatibleGroup = null;
				initialSourceGroup = null;
				initialSymmetric = false;
			}
			else
			{
				initialCompatibleGroup = bean.getCompatibleGroup();
				initialSourceGroup = bean.getSourceGroup();
				initialSymmetric = bean.isSymmetric();
			}
			Property<Group> compatibleGroup = SubCentralFXUtil.groupPropertyForTextField(compatibleGroupTxtFld, initialCompatibleGroup);
			Property<Group> sourceGroup = SubCentralFXUtil.groupPropertyForTextField(sourceGroupTxtFld, initialSourceGroup);
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
					return compatibleGroup.getValue() == null || sourceGroup.getValue() == null
							|| compatibleGroup.getValue().equals(sourceGroup.getValue());
				}
			});

			// Set ResultConverter
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.APPLY)
				{
					Group sourceGrp = sourceGroup.getValue();
					Group compatibleGrp = compatibleGroup.getValue();
					boolean symmetric = symmetricCheckBox.isSelected();
					return new CrossGroupCompatibility(sourceGrp, compatibleGrp, symmetric);
				}
				return null;
			});
		}
	}

	private static class SeriesNameStandardizerSettingEntryDialogController extends AbstractBeanDialogController<SeriesNameStandardizerSettingEntry>
	{
		@FXML
		private RadioButton	literalRadioBtn;
		@FXML
		private RadioButton	simplePatternRadioBtn;
		@FXML
		private RadioButton	regexRadioBtn;
		@FXML
		private TextField	namePatternTxtFld;
		@FXML
		private Label		patternErrorLbl;
		@FXML
		private TextField	nameReplacementTxtFld;

		public SeriesNameStandardizerSettingEntryDialogController(SeriesNameStandardizerSettingEntry bean)
		{
			super(bean);
		}

		@Override
		protected String buildTitle()
		{
			if (bean == null)
			{
				return "Add standardizing rule for: " + SeriesNameStandardizerSettingEntry.getStandardizerTypeString();
			}
			else
			{
				return "Edit standardizing rule for: " + SeriesNameStandardizerSettingEntry.getStandardizerTypeString();
			}
		}

		@Override
		protected Node getDefaultFocusNode()
		{
			return namePatternTxtFld;
		}

		@Override
		protected void initInputPaneControl()
		{
			ToggleGroup patternModeToggleGrp = new ToggleGroup();
			patternModeToggleGrp.getToggles().setAll(literalRadioBtn, simplePatternRadioBtn, regexRadioBtn);

			// Initial values
			Toggle initialPatternMode;
			String initialNamePattern;
			String initialNameReplacement;
			if (bean == null)
			{
				initialPatternMode = literalRadioBtn;
				initialNamePattern = null;
				initialNameReplacement = null;
			}
			else
			{
				switch (bean.getNameUiPattern().getPatternMode())
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
				initialNamePattern = bean.getNameUiPattern().getPattern();
				initialNameReplacement = bean.getValue().getNameReplacement();
			}
			patternModeToggleGrp.selectToggle(initialPatternMode);
			namePatternTxtFld.setText(initialNamePattern);
			nameReplacementTxtFld.setText(initialNameReplacement);

			// Bindings
			Binding<UiPattern> namePatternBinding = FXUtil.createUiPatternTextFieldBinding(patternModeToggleGrp,
					literalRadioBtn,
					simplePatternRadioBtn,
					regexRadioBtn,
					namePatternTxtFld,
					patternErrorLbl);

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
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.APPLY)
				{
					String nameReplacement = StringUtils.trimToNull(namePatternTxtFld.getText());
					boolean enabled = (bean == null ? true : bean.isEnabled());
					return new SeriesNameStandardizerSettingEntry(namePatternBinding.getValue(), nameReplacement, enabled);
				}
				return null;
			});

		}
	}

	private static class ReleaseTagsStandardizerSettingEntryDialogController extends
			AbstractBeanDialogController<ReleaseTagsStandardizerSettingEntry>
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

		public ReleaseTagsStandardizerSettingEntryDialogController(ReleaseTagsStandardizerSettingEntry bean)
		{
			super(bean);
		}

		@Override
		protected String buildTitle()
		{
			if (bean == null)
			{
				return "Add standardizing rule for: " + ReleaseTagsStandardizerSettingEntry.getStandardizerTypeString();
			}
			else
			{
				return "Edit standardizing rule for: " + ReleaseTagsStandardizerSettingEntry.getStandardizerTypeString();
			}
		}

		@Override
		protected Node getDefaultFocusNode()
		{
			return queryTagsTxtFld;
		}

		@Override
		protected void initInputPaneControl()
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
			if (bean == null)
			{
				initialQueryModeToggle = containRadioBtn;
				initialQueryTags = ImmutableList.of();
				initialIgnoreOrder = false;
				initialReplaceWithToggle = matchRadioBtn;
			}
			else
			{
				TagsReplacer replacer = bean.getValue().getReplacer();
				switch (replacer.getQueryMode())
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
				initialQueryTags = replacer.getQueryTags();
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
			}
			queryModeToggleGrp.selectToggle(initialQueryModeToggle);
			ignoreOrderCheckBox.setSelected(initialIgnoreOrder);
			replaceWithToggleGrp.selectToggle(initialReplaceWithToggle);
			ListProperty<Tag> queryTags = SubCentralFXUtil.tagPropertyForTextField(queryTagsTxtFld, initialQueryTags);
			ListProperty<Tag> replacement = SubCentralFXUtil.tagPropertyForTextField(replacementTxtFld, initialQueryTags);

			// Bindings
			Node applyButton = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
			applyButton.disableProperty().bind(queryTags.emptyProperty());

			ignoreOrderCheckBox.disableProperty().bind(queryTags.sizeProperty().lessThan(2));

			matchRadioBtn.setDisable(equalRadioBtn.isSelected());
			equalRadioBtn.selectedProperty().addListener((observable, oldValue, newValue) -> {
				matchRadioBtn.setDisable(newValue);
				if (newValue)
				{
					replaceWithToggleGrp.selectToggle(completeRadioBtn);
				}
			});

			// ResultConverter
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == ButtonType.APPLY)
				{
					TagUtil.QueryMode queryMode;
					if (queryModeToggleGrp.getSelectedToggle() == equalRadioBtn)
					{
						queryMode = QueryMode.EQUAL;
					}
					else
					{
						queryMode = QueryMode.CONTAIN;
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
					boolean enabled = bean == null ? true : bean.isEnabled();
					return new ReleaseTagsStandardizerSettingEntry(new ReleaseTagsStandardizer(new TagsReplacer(queryTags,
							replacement,
							queryMode,
							replaceWith,
							ignoreOrder)), enabled);
				}
				return null;
			});
		}
	}

	public static Optional<StandardRelease> showStandardReleaseDefinitionDialog()
	{
		return showStandardReleaseDefinitionDialog(null);
	}

	public static Optional<StandardRelease> showStandardReleaseDefinitionDialog(StandardRelease standardRls)
	{
		StandardReleaseDialogController ctrl = new StandardReleaseDialogController(standardRls);
		return showDialogAndWait(ctrl, "StandardReleaseDialog.fxml");
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityDialog()
	{
		return showCrossGroupCompatibilityDialog(null);
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityDialog(CrossGroupCompatibility crossGroupCompatibility)
	{
		CrossGroupCompatibilityDialogController ctrl = new CrossGroupCompatibilityDialogController(crossGroupCompatibility);
		return showDialogAndWait(ctrl, "CrossGroupCompatibilityDialog.fxml");
	}

	public static Optional<SeriesNameStandardizerSettingEntry> showSeriesNameStandardizerSettingEntryDialog()
	{
		return showSeriesNameStandardizerSettingEntryDialog(null);
	}

	public static Optional<SeriesNameStandardizerSettingEntry> showSeriesNameStandardizerSettingEntryDialog(SeriesNameStandardizerSettingEntry entry)
	{
		SeriesNameStandardizerSettingEntryDialogController ctrl = new SeriesNameStandardizerSettingEntryDialogController(entry);
		return showDialogAndWait(ctrl, "SeriesNameStandardizerSettingEntryDialog.fxml");
	}

	public static Optional<ReleaseTagsStandardizerSettingEntry> showReleaseTagsStandardizerSettingEntryDialog()
	{
		return showReleaseTagsStandardizerSettingEntryDialog(null);
	}

	public static Optional<ReleaseTagsStandardizerSettingEntry> showReleaseTagsStandardizerSettingEntryDialog(
			ReleaseTagsStandardizerSettingEntry entry)
	{
		ReleaseTagsStandardizerSettingEntryDialogController ctrl = new ReleaseTagsStandardizerSettingEntryDialogController(entry);
		return showDialogAndWait(ctrl, "ReleaseTagsStandardizerSettingEntryDialog.fxml");
	}

	private static <T> Optional<T> showDialogAndWait(AbstractBeanDialogController<T> ctrl, String fxmlFilename)
	{
		try
		{
			FXUtil.loadFromFxml(fxmlFilename, null, null, ctrl);
		}
		catch (IOException e)
		{
			log.error("Error while loading FXML " + fxmlFilename + " with controller " + ctrl, e);
			return Optional.empty();
		}
		return ctrl.getDialog().showAndWait();
	}

	private WatcherDialogs()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

}
