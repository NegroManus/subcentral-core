package de.subcentral.watcher.controller.settings;

import java.util.Optional;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.StandardRelease;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.fx.FxUtil;
import de.subcentral.fx.SubCentralFxUtil;
import de.subcentral.watcher.WatcherDialogs;
import de.subcentral.watcher.settings.ProcessingSettings;
import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
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
    private GridPane				 releaseGuessingSettingsPane;
    @FXML
    private CheckBox				 enableGuessingCheckBox;
    @FXML
    private TableView<StandardRelease>		 standardReleasesTableView;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesTagsColumn;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesGroupColumn;
    @FXML
    private TableColumn<StandardRelease, String> standardReleasesAssumeExistenceColumn;

    @FXML
    private Button addStandardReleaseButton;
    @FXML
    private Button editStandardReleaseButton;
    @FXML
    private Button removeStandardReleaseButton;

    public ReleaseGuessingSettingsController(SettingsController settingsController)
    {
	super(settingsController);
    }

    @Override
    public GridPane getSectionRootPane()
    {
	return releaseGuessingSettingsPane;
    }

    @Override
    protected void doInitialize() throws Exception
    {
	final ProcessingSettings settings = WatcherSettings.INSTANCE.getProcessingSettings();

	enableGuessingCheckBox.selectedProperty().bindBidirectional(settings.guessingEnabledProperty());

	// Standard releases
	standardReleasesTableView.setItems(settings.standardReleasesProperty());

	standardReleasesTagsColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
	    return FxUtil.constantBinding(Tag.listToString(param.getValue().getRelease().getTags()));
	});
	standardReleasesGroupColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
	    return FxUtil.constantBinding(Group.toSafeString(param.getValue().getRelease().getGroup()));
	});
	standardReleasesAssumeExistenceColumn.setCellValueFactory((CellDataFeatures<StandardRelease, String> param) -> {
	    String value;
	    switch (param.getValue().getAssumeExistence())
	    {
		case IF_NONE_FOUND:
		    value = "Only if none found";
		    break;
		case ALWAYS:
		    value = "Always";
		    break;
		default:
		    value = param.getValue().getAssumeExistence().name();
	    }
	    return FxUtil.constantBinding(value);
	});

	addStandardReleaseButton.setOnAction((ActionEvent event) -> {
	    Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseDefinitionDialog();
	    FxUtil.handleDistinctAdd(standardReleasesTableView, result);
	});

	final BooleanBinding noSelection = standardReleasesTableView.getSelectionModel().selectedItemProperty().isNull();

	editStandardReleaseButton.disableProperty().bind(noSelection);
	editStandardReleaseButton.setOnAction((ActionEvent event) -> {
	    StandardRelease def = standardReleasesTableView.getSelectionModel().getSelectedItem();
	    Optional<StandardRelease> result = WatcherDialogs.showStandardReleaseDefinitionDialog(def);
	    FxUtil.handleDistinctEdit(standardReleasesTableView, result);
	});

	removeStandardReleaseButton.disableProperty().bind(noSelection);
	removeStandardReleaseButton.setOnAction((ActionEvent event) -> {
	    FxUtil.handleDelete(standardReleasesTableView, "standard release", SubCentralFxUtil.STANDARD_RELEASE_STRING_CONVERTER);
	});

	FxUtil.setStandardMouseAndKeyboardSupportForTableView(standardReleasesTableView, editStandardReleaseButton, removeStandardReleaseButton);
    }
}
