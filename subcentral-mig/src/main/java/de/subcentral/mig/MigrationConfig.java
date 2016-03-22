package de.subcentral.mig;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

import com.google.common.collect.ImmutableList;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListData;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class MigrationConfig
{
	// Settings config
	private Path					environmentSettingsFile;
	private Path					parsingSettingsFile;
	private PropertiesConfiguration	environmentSettings;
	private XMLConfiguration		parsingSettings;

	// Configure config
	// Datasource
	private DataSource				dataSource;
	// Config
	private boolean					completeMigration;
	private ImmutableList<Series>	selectedSeries	= ImmutableList.of();
	private boolean					migrateSubtitles;
	// First migration result
	private SeriesListData			seriesListContent;

	public Path getEnvironmentSettingsFile()
	{
		return environmentSettingsFile;
	}

	public void setEnvironmentSettingsFile(Path environmentSettingsFile)
	{
		this.environmentSettingsFile = environmentSettingsFile;
	}

	public Path getParsingSettingsFile()
	{
		return parsingSettingsFile;
	}

	public void setParsingSettingsFile(Path parsingSettingsFile)
	{
		this.parsingSettingsFile = parsingSettingsFile;
	}

	public PropertiesConfiguration getEnvironmentSettings()
	{
		return environmentSettings;
	}

	public void setEnvironmentSettings(PropertiesConfiguration environmentSettings)
	{
		this.environmentSettings = environmentSettings;
	}

	public XMLConfiguration getParsingSettings()
	{
		return parsingSettings;
	}

	public void setParsingSettings(XMLConfiguration parsingSettings)
	{
		this.parsingSettings = parsingSettings;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean isCompleteMigration()
	{
		return completeMigration;
	}

	public void setCompleteMigration(boolean completeMigration)
	{
		this.completeMigration = completeMigration;
	}

	public ImmutableList<Series> getSelectedSeries()
	{
		return selectedSeries;
	}

	public void setSelectedSeries(ImmutableList<Series> selectedSeries)
	{
		this.selectedSeries = selectedSeries;
	}

	public boolean getMigrateSubtitles()
	{
		return migrateSubtitles;
	}

	public void setMigrateSubtitles(boolean migrateSubtitles)
	{
		this.migrateSubtitles = migrateSubtitles;
	}

	public SeriesListData getSeriesListContent()
	{
		return seriesListContent;
	}

	public void setSeriesListContent(SeriesListData seriesListContent)
	{
		this.seriesListContent = seriesListContent;
	}

	public void loadSettings() throws ConfigurationException, IOException
	{
		loadEnvironmentSettings();
		loadParsingSettings();
	}

	public void loadEnvironmentSettings() throws ConfigurationException, IOException
	{
		PropertiesConfiguration settings = new PropertiesConfiguration();
		FileHandler fileHandler = new FileHandler(settings);
		fileHandler.load(Files.newInputStream(environmentSettingsFile), Charset.forName("UTF-8").name());
		setEnvironmentSettings(settings);
	}

	public void loadParsingSettings() throws ConfigurationException, IOException
	{
		XMLConfiguration settings = new XMLConfiguration();
		FileHandler fileHandler = new FileHandler(settings);
		fileHandler.load(Files.newInputStream(parsingSettingsFile), Charset.forName("UTF-8").name());
		setParsingSettings(settings);
	}

	public void createDateSource() throws PropertyVetoException, SQLException
	{
		closeDataSource();

		String driverClass = com.mysql.jdbc.Driver.class.getName();
		String url = environmentSettings.getString("sc.db.url");
		String user = environmentSettings.getString("sc.db.user");
		String password = environmentSettings.getString("sc.db.password");

		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass(driverClass); // loads the jdbc driver
		cpds.setJdbcUrl(url);
		cpds.setUser(user);
		cpds.setPassword(password);

		setDataSource(cpds);
	}

	public void closeDataSource() throws SQLException
	{
		if (dataSource != null)
		{
			DataSources.destroy(dataSource);
		}
	}

	public void loadSeriesListContent() throws SQLException
	{
		int seriesListPostId = environmentSettings.getInt("sc.serieslist.postid");

		WbbPost seriesListPost;
		try (Connection conn = dataSource.getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			seriesListPost = boardApi.getPost(seriesListPostId);
		}
		String seriesListPostContent = seriesListPost.getMessage();
		SeriesListParser parser = new SeriesListParser();
		SeriesListData seriesListContent = parser.parsePost(seriesListPostContent);

		setSeriesListContent(seriesListContent);
	}
}
