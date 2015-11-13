package de.subcentral.mig.controller;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.fx.FxUtil;
import de.subcentral.mig.SeriesListParser;
import de.subcentral.mig.SeriesListParser.SeriesListContent;
import de.subcentral.mig.SubCentralBoardDbApi;
import de.subcentral.mig.SubCentralBoardDbApi.Post;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
	private CheckBox			migrateMediaCheckBox;
	@FXML
	private TextField			seriesSearchTxtFld;
	@FXML
	private Label				seriesListTitleLbl;
	@FXML
	private ListView<Series>	seriesListView;
	@FXML
	private CheckBox			migrateSubbersCheckBox;
	@FXML
	private CheckBox			migrateSubsCheckBox;

	public ConfigurePageController(MainController mainController, MigrationConfig config)
	{
		super(mainController, config);
	}

	@Override
	public void initialize() throws Exception
	{
		migrationModeToggleGrp.getToggles().addAll(completeMigrationRadioBtn, selectiveMigrationRadioBtn);
		selectiveMigrationRadioBtn.setSelected(true);

		selectiveMigrationGridPane.disableProperty().bind(selectiveMigrationRadioBtn.selectedProperty().not());

		BooleanBinding migrateMediaNotSelected = migrateMediaCheckBox.selectedProperty().not();
		seriesListView.disableProperty().bind(migrateMediaNotSelected);

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

		seriesListTitleLbl.disableProperty().bind(migrateMediaNotSelected);
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
				super.bind(migrationModeToggleGrp.selectedToggleProperty(),
						migrateMediaCheckBox.selectedProperty(),
						migrateSubbersCheckBox.selectedProperty(),
						migrateSubsCheckBox.selectedProperty(),
						seriesListView.getSelectionModel().getSelectedIndices());
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
					return !((migrateMediaCheckBox.isSelected() && !seriesListView.getSelectionModel().isEmpty()) || migrateSubbersCheckBox.isSelected() || migrateSubsCheckBox.isSelected());
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
		seriesListView.getItems().clear();

		Task<List<Series>> task = new Task<List<Series>>()
		{
			@Override
			protected List<Series> call() throws Exception
			{
				updateTitle("Populating view");
				updateMessage("Reading settings ...");

				PropertiesConfiguration envSettings = new PropertiesConfiguration();
				FileHandler envSettingsFileHandler = new FileHandler(envSettings);
				envSettingsFileHandler.load(Files.newInputStream(config.getEnvironmentSettingsFile()), Charset.forName("UTF-8").name());
				config.setEnvironmentSettings(envSettings);

				XMLConfiguration parsingSettings = new XMLConfiguration();
				FileHandler parsingSettingsFileHandler = new FileHandler(envSettings);
				parsingSettingsFileHandler.load(Files.newInputStream(config.getParsingSettingsFile()), Charset.forName("UTF-8").name());
				config.setParsingSettings(parsingSettings);

				updateMessage("Retrieving series list ...");

				String url = config.getEnvironmentSettings().getString("sc.db.url");
				String user = config.getEnvironmentSettings().getString("sc.db.user");
				String password = config.getEnvironmentSettings().getString("sc.db.password");
				try (Connection conn = DriverManager.getConnection(url, user, password))
				{
					int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
					SubCentralBoardDbApi boardApi = new SubCentralBoardDbApi();
					boardApi.setConnection(conn);
					Post seriesListPost = boardApi.getPost(seriesListPostId);
					String seriesListPostContent = seriesListPost.getMessage();
					SeriesListParser parser = new SeriesListParser();
					SeriesListContent seriesListContent = parser.parsePost(seriesListPostContent);

					return seriesListContent.getSeries();
				}
			}
		};

		task.setOnSucceeded((WorkerStateEvent evt) ->
		{
			List<Series> foundSeries = task.getValue();
			seriesListView.getItems().addAll(foundSeries);
			// Select
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

		});
		task.setOnFailed((WorkerStateEvent evt) ->
		{
			Throwable e = task.getException();
			e.printStackTrace();
			Alert alert = FxUtil.createExceptionAlert("Exception", e.toString(), e);
			alert.show();
		});

		executeBlockingTask(task);
	}

	@Override
	public void onExiting()
	{
		config.setSelectedSeries(ImmutableList.copyOf(seriesListView.getSelectionModel().getSelectedItems()));
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonDisableBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
