package de.subcentral.mig.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.parse.SeriesListParser.SeriesListData;
import de.subcentral.mig.process.MigrationService;
import de.subcentral.mig.settings.MigrationScopeSettings;
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

public class ScopePageController extends AbstractPageController
{
	// Model

	private BooleanBinding		nextButtonDisableBinding;
	// View
	@FXML
	private AnchorPane			rootPane;
	@FXML
	private GridPane			contentPane;

	private final ToggleGroup	migrationModeToggleGrp	= new ToggleGroup();
	@FXML
	private RadioButton			completeMigrationRadioBtn;
	@FXML
	private RadioButton			selectiveMigrationRadioBtn;
	@FXML
	private GridPane			selectiveMigrationGridPane;
	@FXML
	private TextField			seriesSearchTxtFld;
	@FXML
	private Label				seriesListTitleLbl;
	@FXML
	private ListView<Series>	seriesListView;
	@FXML
	private CheckBox			migrateSubtitlesCheckBox;

	// Control
	private InitPageDataTask	initPageDataTask;

	public ScopePageController(MainController mainController)
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
		return "Configure the scope";
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
	public void onEnter()
	{
		initPageDataTask = new InitPageDataTask();
		executeBlockingTask(initPageDataTask);
	}

	@Override
	public void onExit()
	{
		cancelInit();

		// Store config
		boolean includeAllSeries = migrationModeToggleGrp.getSelectedToggle() == completeMigrationRadioBtn;
		MigrationScopeSettings scope = assistance.getSettings().getScopeSettings();

		scope.setIncludeAllSeries(includeAllSeries);
		scope.setIncludedSeries(seriesListView.getSelectionModel().getSelectedItems());
		scope.setIncludeSubtitles(migrateSubtitlesCheckBox.isSelected());
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonDisableBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		cancelInit();
	}

	private void cancelInit()
	{
		// Cancel initPageDataTask if still running
		if (initPageDataTask != null)
		{
			initPageDataTask.cancel(true);
		}
	}

	private class InitPageDataTask extends Task<SeriesListData>
	{
		@Override
		protected SeriesListData call() throws Exception
		{
			updateTitle("Initializing page \"" + ScopePageController.this.getTitle() + "\"");

			updateMessage("Reading settings ...");
			assistance.loadSettingsFromFiles();

			updateMessage("Retrieving series list ...");
			try (MigrationService service = assistance.createMigrationService())
			{
				return service.readSeriesList();
			}
		}

		@Override
		protected void succeeded()
		{
			SeriesListData data = getValue();
			seriesListView.getItems().setAll(data.getSeries());

			// Configure view according to current values
			MigrationScopeSettings scope = assistance.getSettings().getScopeSettings();
			if (scope.getIncludeAllSeries())
			{
				migrationModeToggleGrp.selectToggle(completeMigrationRadioBtn);
			}
			else
			{
				migrationModeToggleGrp.selectToggle(selectiveMigrationRadioBtn);
			}
			List<Series> selectedSeries = scope.getIncludedSeries();
			if (!selectedSeries.isEmpty())
			{
				for (int i = 0; i < seriesListView.getItems().size(); i++)
				{
					Series series = seriesListView.getItems().get(i);
					if (selectedSeries.contains(series))
					{
						seriesListView.getSelectionModel().select(i);
					}
				}
			}
			migrateSubtitlesCheckBox.setSelected(scope.getIncludeSubtitles());
		}
	}
}
