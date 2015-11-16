package de.subcentral.mig.controller;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import de.subcentral.mig.process.SubCentralBoard;
import de.subcentral.mig.process.SubCentralBoard.Post;
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
	public void initialize() throws Exception
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
		// Cancel loadTask if still running
		loadTask.cancel(true);

		// Store config
		config.setCompleteMigration(migrationModeToggleGrp.getSelectedToggle() == completeMigrationRadioBtn);
		config.setSelectedSeries(ImmutableList.copyOf(seriesListView.getSelectionModel().getSelectedItems()));
		config.setMigrateSubtitles(migrateSubtitlesCheckBox.isSelected());

		// Reset view
		rootPane.getChildren().clear();
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

	private class LoadConfigurePageTask extends Task<Void>
	{
		@Override
		protected Void call() throws Exception
		{
			updateTitle("Loading data for page \"" + ConfigurePageController.this.getTitle() + "\"");

			updateMessage("Reading settings ...");
			loadSettings();

			updateMessage("Connecting to database ...");
			createDateSource();

			updateMessage("Retrieving series list ...");
			loadSeriesListContent();

			return null;
		}

		private void loadSettings() throws ConfigurationException, IOException
		{
			loadEnvironmentSettings();
			loadParsingSettings();
		}

		private void loadEnvironmentSettings() throws ConfigurationException, IOException
		{
			PropertiesConfiguration settings = new PropertiesConfiguration();
			FileHandler fileHandler = new FileHandler(settings);
			fileHandler.load(Files.newInputStream(config.getEnvironmentSettingsFile()), Charset.forName("UTF-8").name());
			config.setEnvironmentSettings(settings);
		}

		private void loadParsingSettings() throws ConfigurationException, IOException
		{
			XMLConfiguration settings = new XMLConfiguration();
			FileHandler fileHandler = new FileHandler(settings);
			fileHandler.load(Files.newInputStream(config.getParsingSettingsFile()), Charset.forName("UTF-8").name());
			config.setParsingSettings(settings);
		}

		private void createDateSource() throws PropertyVetoException, SQLException
		{
			String driverClass = com.mysql.jdbc.Driver.class.getName();
			String url = config.getEnvironmentSettings().getString("sc.db.url");
			String user = config.getEnvironmentSettings().getString("sc.db.user");
			String password = config.getEnvironmentSettings().getString("sc.db.password");

			ComboPooledDataSource cpds = new ComboPooledDataSource();
			cpds.setDriverClass(driverClass); // loads the jdbc driver
			cpds.setJdbcUrl(url);
			cpds.setUser(user);
			cpds.setPassword(password);

			config.setDataSource(cpds);
		}

		private void loadSeriesListContent() throws SQLException
		{
			int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");

			Post seriesListPost;
			try (Connection conn = config.getDataSource().getConnection())
			{
				SubCentralBoard boardApi = new SubCentralBoard();
				boardApi.setConnection(conn);
				seriesListPost = boardApi.getPost(seriesListPostId);
			}
			String seriesListPostContent = seriesListPost.getMessage();
			SeriesListParser parser = new SeriesListParser();
			SeriesListContent seriesListContent = parser.parsePost(seriesListPostContent);

			config.setSeriesListContent(seriesListContent);
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
