package de.subcentral.mig.controller;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.io.FileHandler;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.fx.FxUtil;
import de.subcentral.mig.SeriesListParser;
import de.subcentral.mig.SeriesListParser.SeriesListContent;
import de.subcentral.mig.SubCentralBoardDbApi;
import de.subcentral.mig.SubCentralBoardDbApi.Post;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;

public class ConfigureController extends AbstractPageController
{
	// Model
	private BooleanBinding		nextButtonEnabledBinding	= FxUtil.constantBooleanBinding(false);

	// View
	@FXML
	private GridPane			rootPane;
	@FXML
	private Label				seriesListTitleLbl;
	@FXML
	private Label				seriesSelectionLbl;
	@FXML
	private ListView<Series>	seriesListView;

	public ConfigureController(MainController mainController, MigrationConfig config)
	{
		super(mainController, config);
	}

	@Override
	public void initialize() throws Exception
	{
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
						setText(item.getName() + " (" + item.getSeasons().size() + " seasons)");
					}
				}
			};
		});
		seriesListTitleLbl.textProperty().bind(new StringBinding()
		{
			{
				super.bind(seriesListView.getItems());
			}

			@Override
			protected String computeValue()
			{
				return "Found " + seriesListView.getItems().size() + " series:";
			}
		});
		seriesSelectionLbl.textProperty().bind(new StringBinding()
		{
			{
				super.bind(seriesListView.getSelectionModel().getSelectedIndices());
			}

			@Override
			protected String computeValue()
			{
				return "Selected " + seriesListView.getSelectionModel().getSelectedIndices().size() + " series";
			}
		});
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
				PropertiesConfiguration envSettings = new PropertiesConfiguration();
				FileHandler envSettingsFileHandler = new FileHandler(envSettings);
				envSettingsFileHandler.load(Files.newInputStream(config.getEnvironmentSettingsFile()), Charset.forName("UTF-8").name());
				config.setEnvironmentSettings(envSettings);

				XMLConfiguration parsingSettings = new XMLConfiguration();
				FileHandler parsingSettingsFileHandler = new FileHandler(envSettings);
				parsingSettingsFileHandler.load(Files.newInputStream(config.getParsingSettingsFile()), Charset.forName("UTF-8").name());
				config.setParsingSettings(parsingSettings);

				String url = config.getEnvironmentSettings().getString("sc.db.url");
				String user = config.getEnvironmentSettings().getString("sc.db.user");
				String password = config.getEnvironmentSettings().getString("sc.db.password");
				System.out.println("Connecting");
				try (Connection conn = DriverManager.getConnection(url, user, password))
				{
					System.out.println("Connected");
					int seriesListThreadId = config.getEnvironmentSettings().getInt("sc.serieslist.threadid");
					SubCentralBoardDbApi boardApi = new SubCentralBoardDbApi();
					boardApi.setConnection(conn);
					System.out.println("Retrieving series list post");
					Post seriesListPost = boardApi.getFirstPost(seriesListThreadId);
					System.out.println("Retrieved series list post");
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

		});
		task.setOnFailed((WorkerStateEvent evt) ->
		{
			Throwable e = task.getException();
			e.printStackTrace();
			Alert alert = FxUtil.createExceptionAlert("Exception", e.toString(), e);
			alert.show();
		});

		Thread thread = new Thread(task);
		thread.start();
	}

	@Override
	public void onExiting()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Node getRootPane()
	{
		return rootPane;
	}

	@Override
	public BooleanBinding nextButtonDisableBinding()
	{
		return nextButtonEnabledBinding;
	}

	@Override
	public void shutdown() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
