package de.subcentral.mig.controller;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class ConfigurePageController extends AbstractPageController
{
	// Model

	private BooleanBinding			nextButtonDisableBinding;
	// View
	@FXML
	private AnchorPane				rootPane;
	@FXML
	private GridPane				contentPane;

	private final ToggleGroup		migrationModeToggleGrp	= new ToggleGroup();
	@FXML
	private RadioButton				completeMigrationRadioBtn;
	@FXML
	private RadioButton				selectiveMigrationRadioBtn;
	@FXML
	private GridPane				selectiveMigrationGridPane;
	@FXML
	private TextField				seriesSearchTxtFld;
	@FXML
	private Label					seriesListTitleLbl;
	@FXML
	private ListView<Series>		seriesListView;
	@FXML
	private CheckBox				migrateSubtitlesCheckBox;

	// Control
	private LoadConfigurePageTask	loadTask;

	public ConfigurePageController(MainController mainController)
	{
		super(mainController);
	}

	@Override
	protected void initialize() throws Exception
	{
		migrationModeToggleGrp.getToggles().addAll(completeMigrationRadioBtn, selectiveMigrationRadioBtn);
		selectiveMigrationRadioBtn.setSelected(true);

		selectiveMigrationGridPane.disableProperty().bind(selectiveMigrationRadioBtn.selectedProperty().not());

		seriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		seriesListView.setCellFactory((ListView<Series> param) ->
		{
			return new ListCell<Series>()
			{
				@Override
				protected void updateItem(Series item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty || item == null)
					{
						setText(null);
					}
					else
					{
						setText(item.getName());
					}
				}
			};
		});

		seriesListTitleLbl.textProperty().bind(new StringBinding()
		{
			{
				super.bind(seriesListView.getItems(), seriesListView.getSelectionModel().getSelectedIndices());
			}

			@Override
			protected String computeValue()
			{
				return seriesListView.getSelectionModel().getSelectedIndices().size() + " / " + seriesListView.getItems().size() + " series selected";
			}
		});

		seriesSearchTxtFld.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
		{
			if (StringUtils.isNotBlank(newValue))
			{
				String searchText = newValue.toLowerCase();
				for (int i = 0; i < seriesListView.getItems().size(); i++)
				{
					Series series = seriesListView.getItems().get(i);
					if (series.getName().toLowerCase().startsWith(searchText))
					{
						seriesListView.scrollTo(i);
						break;
					}
				}
			}
		});

		nextButtonDisableBinding = new BooleanBinding()
		{
			{
				super.bind(migrationModeToggleGrp.selectedToggleProperty(), migrateSubtitlesCheckBox.selectedProperty(), seriesListView.getSelectionModel().getSelectedIndices());
			}

			@Override
			protected boolean computeValue()
			{
				if (migrationModeToggleGrp.getSelectedToggle() == completeMigrationRadioBtn)
				{
					return false;
				}
				else if (migrationModeToggleGrp.getSelectedToggle() == selectiveMigrationRadioBtn)
				{
					return seriesListView.getSelectionModel().isEmpty();
				}
				else
				{
					return true;
				}
			}
		};
	}

	@Override
	public String getTitle()
	{
		return "Configure migration";
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
		loadTask = new LoadConfigurePageTask();

		executeBlockingTask(loadTask);
	}

	@Override
	public void onExiting()
	{
		cancelLoadTask();

		// Store config
		config.setCompleteMigration(migrationModeToggleGrp.getSelectedToggle() == completeMigrationRadioBtn);
		config.setSelectedSeries(ImmutableList.copyOf(seriesListView.getSelectionModel().getSelectedItems()));
		config.setMigrateSubtitles(migrateSubtitlesCheckBox.isSelected());
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonDisableBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		cancelLoadTask();
	}

	private void cancelLoadTask()
	{
		// Cancel loadTask if still running
		if (loadTask != null)
		{
			loadTask.cancel(true);
		}
	}

	private class LoadConfigurePageTask extends Task<Void>
	{
		@Override
		protected Void call() throws Exception
		{
			updateTitle("Loading data for page \"" + ConfigurePageController.this.getTitle() + "\"");

			updateMessage("Reading settings ...");
			config.loadSettings();

			updateMessage("Connecting to database ...");
			config.createDateSource();

			updateMessage("Retrieving series list ...");
			config.loadSeriesListContent();

			return null;
		}

		@Override
		protected void succeeded()
		{
			SeriesListContent seriesListContent = config.getSeriesListContent();

			seriesListView.getItems().setAll(seriesListContent.getSeries());
			// Configure view according to config
			if (config.isCompleteMigration())
			{
				migrationModeToggleGrp.selectToggle(completeMigrationRadioBtn);
			}
			else
			{
				migrationModeToggleGrp.selectToggle(selectiveMigrationRadioBtn);
			}
			if (!config.getSelectedSeries().isEmpty())
			{
				for (int i = 0; i < seriesListView.getItems().size(); i++)
				{
					Series series = seriesListView.getItems().get(i);
					if (config.getSelectedSeries().contains(series))
					{
						seriesListView.getSelectionModel().select(i);
					}
				}
			}
			migrateSubtitlesCheckBox.setSelected(config.getMigrateSubtitles());
		}
	}
}
