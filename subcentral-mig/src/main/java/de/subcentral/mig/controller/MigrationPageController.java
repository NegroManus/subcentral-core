package de.subcentral.mig.controller;

import de.subcentral.fx.FxBindings;
import de.subcentral.mig.settings.MigrationScopeSettings;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MigrationPageController extends AbstractPageController {
    // Model
    private MigrationTask task;

    // View
    @FXML
    private AnchorPane    rootPane;
    @FXML
    private GridPane      contentPane;
    @FXML
    private Label         configLbl;
    @FXML
    private Label         taskTitleLbl;
    @FXML
    private ProgressBar   taskProgressBar;
    @FXML
    private Label         taskMessageLbl;

    public MigrationPageController(MigMainController migMainController) {
        super(migMainController);
    }

    @Override
    protected void initialize() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getTitle() {
        return "Migration";
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }

    @Override
    public Pane getContentPane() {
        return contentPane;
    }

    @Override
    public void onEnter() {
        MigrationScopeSettings scope = assistance.getSettings().getScopeSettings();
        StringBuilder sb = new StringBuilder();
        sb.append("Migrating ");
        if (scope.getIncludeAllSeries()) {
            sb.append("all series");
        }
        else {
            sb.append(scope.getIncludedSeries().size());
            sb.append(" series");
        }
        if (scope.getIncludeSubtitles()) {
            sb.append(" (including the subtitles)");
        }
        else {
            sb.append(" (excluding the subtitles)");
        }
        configLbl.setText(sb.toString());

        task = new MigrationTask(assistance.getSettings());
        taskTitleLbl.textProperty().unbind();
        taskTitleLbl.textProperty().bind(task.titleProperty());
        taskProgressBar.progressProperty().unbind();
        taskProgressBar.progressProperty().bind(task.progressProperty());
        taskMessageLbl.textProperty().unbind();
        taskMessageLbl.textProperty().bind(task.messageProperty());

        execute(task);
    }

    @Override
    public void onExit() {
        cancelMigration();
    }

    private void cancelMigration() {
        if (task != null) {
            task.cancel(true);
        }
    }

    @Override
    public BooleanBinding nextButtonDisableBinding() {
        return FxBindings.immutableBooleanBinding(true);
    }

    @Override
    public void shutdown() {
        onExit();
    }
}
