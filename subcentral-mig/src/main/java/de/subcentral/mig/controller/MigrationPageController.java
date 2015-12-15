package de.subcentral.mig.controller;

import de.subcentral.fx.FxUtil;
import de.subcentral.mig.process.MigrationTask;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class MigrationPageController extends AbstractPageController
{
	// Model
	private MigrationTask	task;

	// View
	@FXML
	private AnchorPane		rootPane;
	@FXML
	private GridPane		contentPane;
	@FXML
	private Label			configLbl;
	@FXML
	private Label			taskTitleLbl;
	@FXML
	private ProgressBar		taskProgressBar;
	@FXML
	private Label			taskMessageLbl;

	public MigrationPageController(MainController mainController)
	{
		super(mainController);
	}

	@Override
	protected void initialize() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getTitle()
	{
		return "Migration";
	}

	@Override
	public Pane getRootPane()
	{
		return rootPane;
	}

	@Override
	public Pane getContentPane()
	{
		return contentPane;
	}

	@Override
	public void onEntering()
	{
		if (config.isCompleteMigration())
		{
			configLbl.setText("Complete migration");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Migrating ");
			sb.append(config.getSelectedSeries().size());
			sb.append(" series");
			if (config.getMigrateSubtitles())
			{
				sb.append(" (including the subtitles)");
			}
			else
			{
				sb.append(" (excluding the subtitles)");
			}
			configLbl.setText(sb.toString());
		}

		task = new MigrationTask(config);
		taskTitleLbl.textProperty().unbind();
		taskTitleLbl.textProperty().bind(task.titleProperty());
		taskProgressBar.progressProperty().unbind();
		taskProgressBar.progressProperty().bind(task.progressProperty());
		taskMessageLbl.textProperty().unbind();
		taskMessageLbl.textProperty().bind(task.messageProperty());

		mainController.getCommonExecutor().submit(task);
	}

	@Override
	public void onExiting()
	{
		if (task != null)
		{
			task.cancel(true);
		}
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return FxUtil.constantBooleanBinding(true);
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
