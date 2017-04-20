package de.subcentral.watcher.controller.settings;

import java.util.Objects;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.fx.FxBindings;
import de.subcentral.fx.FxControlBindings;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.fx.action.ActionList;
import de.subcentral.fx.action.FxActions;
import de.subcentral.watcher.dialog.WatcherDialogs;
import de.subcentral.watcher.settings.ProcessingSettings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;

public class ReleaseGuessingSettingsController extends AbstractSettingsSectionController {
    @FXML
    private GridPane                             rootPane;
    @FXML
    private CheckBox                             enableGuessingCheckBox;
    @FXML
    private TableView<StandardRelease>           standardReleasesTableView;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesGroupColumn;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesTagsColumn;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesScopeColumn;

    @FXML
    private Button                               addStandardReleaseButton;
    @FXML
    private Button                               editStandardReleaseButton;
    @FXML
    private Button                               removeStandardReleaseButton;

    public ReleaseGuessingSettingsController(SettingsController settingsController) {
        super(settingsController);
    }

    @Override
    public GridPane getContentPane() {
        return rootPane;
    }

    @Override
    protected void initialize() throws Exception {
        final ProcessingSettings settings = SettingsController.SETTINGS.getProcessingSettings();

        enableGuessingCheckBox.selectedProperty().bindBidirectional(settings.getGuessingEnabled().property());

        // Standard releases table
        standardReleasesGroupColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
            return FxBindings.immutableObservableValue(Group.getName(param.getValue().getRelease().getGroup()));
        });
        standardReleasesTagsColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
            return FxBindings.immutableObservableValue(Tags.join(param.getValue().getRelease().getTags()));
        });
        standardReleasesScopeColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
            String value;
            switch (param.getValue().getScope()) {
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

        ObservableList<StandardRelease> stdRlss = settings.getStandardReleases().property();
        SortedList<StandardRelease> displayStdRlss = FxControlBindings.sortableTableView(standardReleasesTableView,
                stdRlss);

        // Standard release table buttons
        ActionList<StandardRelease> stdRlssActionList = new ActionList<>(stdRlss,
                standardReleasesTableView.getSelectionModel(),
                displayStdRlss);
        stdRlssActionList.setNewItemSupplier(() -> WatcherDialogs.showStandardReleaseEditView(getPrimaryStage()));
        stdRlssActionList.setItemEditer(
                (StandardRelease item) -> WatcherDialogs.showStandardReleaseEditView(item, getPrimaryStage()));
        stdRlssActionList.setDistincter(Objects::equals);
        stdRlssActionList.setSorter(ProcessingSettings.STANDARD_RELEASE_COMPARATOR);
        stdRlssActionList.setAlreadyContainedInformer(FxActions.createAlreadyContainedInformer(getPrimaryStage(),
                "standard release",
                SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER));
        stdRlssActionList.setRemoveConfirmer(FxActions.createRemoveConfirmer(getPrimaryStage(),
                "standard release",
                SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER));

        stdRlssActionList.bindAddButton(addStandardReleaseButton);
        stdRlssActionList.bindEditButton(editStandardReleaseButton);
        stdRlssActionList.bindRemoveButton(removeStandardReleaseButton);

        FxActions.setStandardMouseAndKeyboardSupport(standardReleasesTableView,
                addStandardReleaseButton,
                editStandardReleaseButton,
                removeStandardReleaseButton);
    }
}
