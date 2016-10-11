package de.subcentral.fx.dialog;

import de.subcentral.fx.FxIO;
import de.subcentral.fx.ctrl.Controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class DialogController<T> extends Controller {
    // View
    protected final Dialog<T> dialog = new Dialog<>();
    @FXML
    protected Node            rootPane;

    public DialogController(Window owner) {
        this.dialog.initOwner(owner);
    }

    public Dialog<T> getDialog() {
        return dialog;
    }

    @Override
    public Stage getPrimaryStage() {
        return (Stage) dialog.getDialogPane().getScene().getWindow();
    }

    @Override
    public final void initialize() {
        initDialog();
        initComponents();
    }

    protected void initDialog() {
        String title = getTitle();
        dialog.setTitle(title);
        String imgPath = getImagePath();
        if (imgPath != null) {
            dialog.setGraphic(new ImageView(FxIO.loadImg(imgPath)));
        }
        dialog.setHeaderText(title);
        dialog.setResizable(true);

        dialog.getDialogPane().getButtonTypes().addAll(getButtonTypes());
        dialog.getDialogPane().setContent(rootPane);
        dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Platform.runLater(() -> getDefaultFocusNode().requestFocus());
    }

    protected abstract String getTitle();

    protected String getImagePath() {
        return null;
    }

    protected ButtonType[] getButtonTypes() {
        return new ButtonType[] { ButtonType.APPLY, ButtonType.CANCEL };
    }

    protected abstract Node getDefaultFocusNode();

    protected abstract void initComponents();
}