package de.subcentral.watcher.controller.settings;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.fx.FxActions;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.action.AddAction;
import de.subcentral.fx.action.EditAction;
import de.subcentral.fx.action.RemoveAction;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class ReleaseGuessingSettingsController extends AbstractSettingsSectionController
{
	@FXML
	private GridPane								rootPane;
	@FXML
	private CheckBox								enableGuessingCheckBox;
	@FXML
	private TableView<StandardRelease>				standardReleasesTableView;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesGroupColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesTagsColumn;
	@FXML
	private TableColumn<StandardRelease, String>	standardReleasesScopeColumn;

	@FXML
	private Button									addStandardReleaseButton;
	@FXML
	private Button									editStandardReleaseButton;
	@FXML
	private Button									removeStandardReleaseButton;

	public ReleaseGuessingSettingsController(SettingsController settingsController)
	{
		super(settingsController);
	}

	@Override
	public GridPane getContentPane()
	{
		return rootPane;
	}

	@Override
	protected void initialize() throws Exception
	{
		final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

		enableGuessingCheckBox.selectedProperty().bindBidirectional(settings.getGuessingEnabled().property());

		// Standard releases
		standardReleasesTableView.setItems(settings.getStandardReleases().property());

		standardReleasesGroupColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			return FxBindings.immutableObservableValue(Group.toStringNullSafe(param.getValue().getRelease().getGroup()));
		});
		standardReleasesTagsColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			return FxBindings.immutableObservableValue(Tag.formatList(param.getValue().getRelease().getTags()));
		});
		standardReleasesScopeColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) ->
		{
			String value;
			switch (param.getValue().getScope())
			{
				case IF_GUESSING:
					value = "If guessing";
					break;
				case ALWAYS:
					value = "Always";
					break;
				default:
					value = param.getValue().getScope().name();
			}
			return FxBindings.immutableObservableValue(value);
		});

		BooleanBinding noSelection = standardReleasesTableView.getSelectionModel().selectedItemProperty().isNull();
		Comparator<StandardRelease> comparator = ObjectUtil.getDefaultOrdering();
		boolean distinct = true;
		Consumer<StandardRelease> alreadyExistedInformer = FxActions.createAlreadyExistedInformer(getPrimaryStage(), "standard release", SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER);
		Predicate<StandardRelease> removeConfirmer = FxActions.createRemoveConfirmer(getPrimaryStage(), "standard release", SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER);

		AddAction<StandardRelease> addAction = new AddAction<>(standardReleasesTableView, () -> WatcherDialogs.showStandardReleaseEditView(getPrimaryStage()));
		addAction.setComparator(comparator);
		addAction.setDistinct(distinct);
		addAction.setAlreadyExistedInformer(alreadyExistedInformer);
		addStandardReleaseButton.setOnAction(addAction);

		editStandardReleaseButton.disableProperty().bind(noSelection);
		EditAction<StandardRelease> editAction = new EditAction<>(standardReleasesTableView, (StandardRelease item) -> WatcherDialogs.showStandardReleaseEditView(item, getPrimaryStage()));
		editAction.setComparator(comparator);
		editAction.setDistinct(distinct);
		editAction.setAlreadyExistedInformer(alreadyExistedInformer);
		editStandardReleaseButton.setOnAction(editAction);

		removeStandardReleaseButton.disableProperty().bind(noSelection);
		RemoveAction<StandardRelease> removeAction = new RemoveAction<>(standardReleasesTableView);
		removeAction.setRemoveConfirmer(removeConfirmer);
		removeStandardReleaseButton.setOnAction(removeAction);

		FxActions.setStandardMouseAndKeyboardSupport(standardReleasesTableView, addStandardReleaseButton, editStandardReleaseButton, removeStandardReleaseButton);
	}
}
