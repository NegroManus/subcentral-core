package de.subcentral.mig.controller;

import de.subcentral.fx.FxUtil;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public abstract class AbstractPageController extends AbstractController
{
	protected MainController	mainController;
	protected MigrationConfig	config;

	public AbstractPageController(MainController mainController, MigrationConfig config)
	{
		this.mainController = mainController;
		this.config = config;
	}

	public MainController getMainController()
	{
		return mainController;
	}

	public MigrationConfig getConfig()
	{
		return config;
	}

	public abstract String getTitle();

	public abstract Pane getRootPane();

	public abstract Pane getContentPane();

	public abstract void onEntering();

	public abstract void onExiting();

	public abstract BooleanBinding nextButtonDisableBinding();

	protected void executeBlockingTask(Task<?> task)
	{
		VBox taskVBox = new VBox();
		taskVBox.setAlignment(Pos.CENTER);
		taskVBox.setSpacing(3d);
		Label titleLbl = new Label();
		titleLbl.setFont(Font.font(null, FontWeight.BOLD, -1));
		titleLbl.textProperty().bind(task.titleProperty());
		ProgressBar progressBar = new ProgressBar();
		progressBar.progressProperty().bind(task.progressProperty());
		Label messageLbl = new Label();
		messageLbl.textProperty().bind(task.messageProperty());
		taskVBox.getChildren().addAll(titleLbl, progressBar, messageLbl);

		StackPane blockingPane = new StackPane();
		AnchorPane.setTopAnchor(blockingPane, 0.0d);
		AnchorPane.setRightAnchor(blockingPane, 0.0d);
		AnchorPane.setBottomAnchor(blockingPane, 0.0d);
		AnchorPane.setLeftAnchor(blockingPane, 0.0d);
		blockingPane.getChildren().add(taskVBox);

		getRootPane().getChildren().setAll(blockingPane);

		task.stateProperty().addListener((ObservableValue<? extends State> observable, State oldValue, State newValue) ->
		{
			switch (newValue)
			{
				case SUCCEEDED:
					getRootPane().getChildren().setAll(getContentPane());
					break;
				case FAILED:
				{
					Throwable e = task.getException();
					e.printStackTrace();
					Alert alert = FxUtil.createExceptionAlert("Task failed", e.toString(), e);
					alert.show();
					getRootPane().getChildren().setAll(getContentPane());
					break;
				}
				case CANCELLED:
				{
					Alert alert = new Alert(AlertType.INFORMATION, "Task was cancelled");
					alert.show();
					getRootPane().getChildren().setAll(getContentPane());
					break;
				}
				default:
					break;
			}
		});
		mainController.getCommonExecutor().submit(task);
	}
}
