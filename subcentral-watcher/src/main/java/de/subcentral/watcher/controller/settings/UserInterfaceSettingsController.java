package de.subcentral.watcher.controller.settings;

import de.subcentral.watcher.settings.WatcherSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

public class UserInterfaceSettingsController extends AbstractSettingsSectionController {
    @FXML
    private GridPane rootPane;
    @FXML
    private CheckBox warningsEnabledCheckBox;
    @FXML
    private CheckBox guessingWarningEnabledCheckBox;
    @FXML
    private CheckBox metaTaggedReleaseWarningEnabledCheckBox;
    @FXML
    private CheckBox nukedReleaseWarningEnabledCheckBox;
    @FXML
    private CheckBox systemTrayEnabledCheckBox;

    public UserInterfaceSettingsController(SettingsController settingsController) {
        super(settingsController);
    }

    @Override
    public GridPane getContentPane() {
        return rootPane;
    }

    @Override
    protected void initialize() throws Exception {
        WatcherSettings settings = SettingsController.SETTINGS;

        warningsEnabledCheckBox.selectedProperty().bindBidirectional(settings.getWarningsEnabled().property());
        guessingWarningEnabledCheckBox.selectedProperty().bindBidirectional(settings.getGuessingWarningEnabled().property());
        metaTaggedReleaseWarningEnabledCheckBox.selectedProperty().bindBidirectional(settings.getReleaseMetaTaggedWarningEnabled().property());
        nukedReleaseWarningEnabledCheckBox.selectedProperty().bindBidirectional(settings.getReleaseNukedWarningEnabled().property());

        final BooleanBinding warningsDisabledBinding = warningsEnabledCheckBox.selectedProperty().not();
        guessingWarningEnabledCheckBox.disableProperty().bind(warningsDisabledBinding);
        metaTaggedReleaseWarningEnabledCheckBox.disableProperty().bind(warningsDisabledBinding);
        nukedReleaseWarningEnabledCheckBox.disableProperty().bind(warningsDisabledBinding);

        systemTrayEnabledCheckBox.selectedProperty().bindBidirectional(settings.getSystemTrayEnabled().property());
    }
}
