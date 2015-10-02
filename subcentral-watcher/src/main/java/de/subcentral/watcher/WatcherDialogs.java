package de.subcentral.watcher;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.correction.ReleaseTagsCorrector;
import de.subcentral.core.correction.TagsReplacer;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.StandardRelease.Scope;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.release.TagUtil;
import de.subcentral.core.metadata.release.TagUtil.ReplaceMode;
import de.subcentral.core.metadata.release.TagUtil.SearchMode;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.UserPattern;
import de.subcentral.watcher.controller.AbstractController;
import de.subcentral.watcher.settings.LanguageTextMapping;
import de.subcentral.watcher.settings.LanguageUserPattern;
import de.subcentral.watcher.settings.ReleaseTagsCorrectionRuleSettingEntry;
import de.subcentral.watcher.settings.SeriesNameCorrectionRuleSettingEntry;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Window;

public class WatcherDialogs
{
	private static final Logger log = LogManager.getLogger(WatcherDialogs.class);

	static abstract class AbstractBeanEditController<T> extends AbstractController
	{
		// Model
		protected final T bean;

		// View
		protected final Dialog<T>	dialog	= new Dialog<>();
		@FXML
		protected Node				rootPane;

		private AbstractBeanEditController(T bean, Window owner)
		{
			this.bean = bean;
			this.dialog.initOwner(owner);
		}

		private Dialog<T> getDialog()
		{
			return dialog;
		}

		@Override
		protected final void doInitialize()
		{
			initDialog();
			initComponents();
		}

		protected void initDialog()
		{
			String title = getTitle();
			dialog.setTitle(title);
			String imgPath = getImagePath();
			if (imgPath != null)
			{
				dialog.setGraphic(new ImageView(FxUtil.loadImg(imgPath)));
			}
			dialog.setHeaderText(title);

			dialog.getDialogPane().getButtonTypes().addAll(getButtonTypes());
			dialog.getDialogPane().setContent(rootPane);

			Platform.runLater(() -> getDefaultFocusNode().requestFocus());
		}

		protected abstract String getTitle();

		protected String getImagePath()
		{
			return null;
		}

		protected ButtonType[] getButtonTypes()
		{
			return new ButtonType[]
			{ ButtonType.APPLY, ButtonType.CANCEL };
		}

		protected abstract Node getDefaultFocusNode();

		protected abstract void initComponents();
	}

	private static class StandardReleaseEditController extends AbstractBeanEditController<StandardRelease>
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

		private StandardReleaseEditController(StandardRelease commonReleaseDef, Window window)
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

	private static class CrossGroupCompatibilityEditController extends AbstractBeanEditController<CrossGroupCompatibility>
	{
		@FXML
		private TextField	compatibleGroupTxtFld;
		@FXML
		private TextField	sourceGroupTxtFld;
		@FXML
		private CheckBox	symmetricCheckBox;

		private CrossGroupCompatibilityEditController(CrossGroupCompatibility bean, Window window)
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
					return new CrossGroupCompatibility(sourceGrp, compatibleGrp, symmetric);
				}
				return null;
			});
		}
	}

	private static class SeriesNameCorrectionRuleEditController extends AbstractBeanEditController<SeriesNameCorrectionRuleSettingEntry>
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
		private Text		patternErrorTxt;
		@FXML
		private TextField	nameReplacementTxtFld;

		private SeriesNameCorrectionRuleEditController(SeriesNameCorrectionRuleSettingEntry bean, Window window)
		{
			super(bean, window);
		}

		@Override
		protected String getTitle()
		{
			if (bean == null)
			{
				return "Add correction rule for: " + SeriesNameCorrectionRuleSettingEntry.getRuleType();
			}
			else
			{
				return "Edit correction rule for: " + SeriesNameCorrectionRuleSettingEntry.getRuleType();
			}
		}

		@Override
		protected String getImagePath()
		{
			return "edit_16.png";
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
			if (bean == null)
			{
				initialPatternMode = literalRadioBtn;
				initialNamePattern = null;
				initialNameReplacement = null;
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
				initialNameReplacement = bean.getValue().getNameReplacement();
			}
			patternModeToggleGrp.selectToggle(initialPatternMode);
			namePatternTxtFld.setText(initialNamePattern);
			nameReplacementTxtFld.setText(initialNameReplacement);

			// Bindings
			Binding<UserPattern> namePatternBinding = FxUtil.createUiPatternTextFieldBinding(patternModeToggleGrp,
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
					String nameReplacement = StringUtils.trimToNull(nameReplacementTxtFld.getText());
					boolean beforeQuerying = (bean == null ? true : bean.isBeforeQuerying());
					boolean afterQuerying = (bean == null ? true : bean.isAfterQuerying());
					return new SeriesNameCorrectionRuleSettingEntry(namePatternBinding.getValue(), nameReplacement, beforeQuerying, afterQuerying);
				}
				return null;
			});

		}
	}

	private static class ReleaseTagsCorrectionRuleEditController extends AbstractBeanEditController<ReleaseTagsCorrectionRuleSettingEntry>
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

		private ReleaseTagsCorrectionRuleEditController(ReleaseTagsCorrectionRuleSettingEntry bean, Window window)
		{
			super(bean, window);
		}

		@Override
		protected String getTitle()
		{
			if (bean == null)
			{
				return "Add correction rule for: " + ReleaseTagsCorrectionRuleSettingEntry.getRuleType();
			}
			else
			{
				return "Edit correction rule for: " + ReleaseTagsCorrectionRuleSettingEntry.getRuleType();
			}
		}

		@Override
		protected String getImagePath()
		{
			return "edit_16.png";
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
				TagsReplacer replacer = bean.getValue().getReplacer();
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
					return new ReleaseTagsCorrectionRuleSettingEntry(new ReleaseTagsCorrector(new TagsReplacer(queryTags, replacement, queryMode, replaceWith, ignoreOrder)),
							beforeQuerying,
							afterQuerying);
				}
				return null;
			});
		}
	}

	private static class LocaleListEditController extends AbstractBeanEditController<List<Locale>>
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

		private LocaleListEditController(List<Locale> bean, Window window)
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

	private static class TextLanguageMappingEditController extends AbstractBeanEditController<LanguageUserPattern>
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

		private TextLanguageMappingEditController(LanguageUserPattern bean, Window window)
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
					return new LanguageUserPattern(patternBinding.getValue(), langComboBox.getValue());
				}
				return null;
			});
		}
	}

	private static class LanguageTextMappingEditController extends AbstractBeanEditController<LanguageTextMapping>
	{
		@FXML
		private ComboBox<Locale>	langComboBox;
		@FXML
		private TextField			textTxtFld;

		private LanguageTextMappingEditController(LanguageTextMapping bean, Window window)
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
					return new LanguageTextMapping(langComboBox.getValue(), textTxtFld.getText());
				}
				return null;
			});
		}
	}

	public static Optional<StandardRelease> showStandardReleaseEditView(Window window)
	{
		return showStandardReleaseEditView(null, window);
	}

	public static Optional<StandardRelease> showStandardReleaseEditView(StandardRelease standardRls, Window window)
	{
		StandardReleaseEditController ctrl = new StandardReleaseEditController(standardRls, window);
		return showEditViewAndWait(ctrl, "StandardReleaseEditView.fxml");
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityEditView(Window window)
	{
		return showCrossGroupCompatibilityEditView(null, window);
	}

	public static Optional<CrossGroupCompatibility> showCrossGroupCompatibilityEditView(CrossGroupCompatibility crossGroupCompatibility, Window window)
	{
		CrossGroupCompatibilityEditController ctrl = new CrossGroupCompatibilityEditController(crossGroupCompatibility, window);
		return showEditViewAndWait(ctrl, "CrossGroupCompatibilityEditView.fxml");
	}

	public static Optional<SeriesNameCorrectionRuleSettingEntry> showSeriesNameCorrectionRuleEditView(Window window)
	{
		return showSeriesNameCorrectionRuleEditView(null, window);
	}

	public static Optional<SeriesNameCorrectionRuleSettingEntry> showSeriesNameCorrectionRuleEditView(SeriesNameCorrectionRuleSettingEntry entry, Window window)
	{
		SeriesNameCorrectionRuleEditController ctrl = new SeriesNameCorrectionRuleEditController(entry, window);
		return showEditViewAndWait(ctrl, "SeriesNameCorrectionRuleEditView.fxml");
	}

	public static Optional<ReleaseTagsCorrectionRuleSettingEntry> showReleaseTagsCorrectionRuleEditView(Window window)
	{
		return showReleaseTagsCorrectionRuleEditView(null, window);
	}

	public static Optional<ReleaseTagsCorrectionRuleSettingEntry> showReleaseTagsCorrectionRuleEditView(ReleaseTagsCorrectionRuleSettingEntry entry, Window window)
	{
		ReleaseTagsCorrectionRuleEditController ctrl = new ReleaseTagsCorrectionRuleEditController(entry, window);
		return showEditViewAndWait(ctrl, "ReleaseTagsCorrectionRuleEditView.fxml");
	}

	public static Optional<List<Locale>> showLocaleListEditView(List<Locale> languages, Window window)
	{
		LocaleListEditController ctrl = new LocaleListEditController(languages, window);
		return showEditViewAndWait(ctrl, "LocaleListEditView.fxml");
	}

	public static Optional<LanguageUserPattern> showTextLanguageMappingEditView(Window window)
	{
		return showTextLanguageMappingEditView(null, window);
	}

	public static Optional<LanguageUserPattern> showTextLanguageMappingEditView(LanguageUserPattern mapping, Window window)
	{
		TextLanguageMappingEditController ctrl = new TextLanguageMappingEditController(mapping, window);
		return showEditViewAndWait(ctrl, "TextLanguageMappingEditView.fxml");
	}

	public static Optional<LanguageTextMapping> showLanguageTextMappingEditView(Window window)
	{
		return showLanguageTextMappingEditView(null, window);
	}

	public static Optional<LanguageTextMapping> showLanguageTextMappingEditView(LanguageTextMapping mapping, Window window)
	{
		LanguageTextMappingEditController ctrl = new LanguageTextMappingEditController(mapping, window);
		return showEditViewAndWait(ctrl, "LanguageTextMappingEditView.fxml");
	}

	private static <T> Optional<T> showEditViewAndWait(AbstractBeanEditController<T> ctrl, String fxmlFilename)
	{
		try
		{
			FxUtil.loadFromFxml(fxmlFilename, null, null, ctrl);
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
