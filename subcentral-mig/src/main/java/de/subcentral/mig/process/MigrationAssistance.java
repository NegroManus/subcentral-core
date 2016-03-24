package de.subcentral.mig.process;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;

import de.subcentral.mig.parse.SeriesListParser.SeriesListData;
import de.subcentral.mig.settings.MigrationSettings;

public class MigrationAssistance
{
	private final MigrationSettings	settings	= new MigrationSettings();
	private Path					environmentSettingsFile;
	private Path					parsingSettingsFile;
	private SeriesListData			seriesListData;

	public MigrationSettings getSettings()
	{
		return settings;
	}

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

	public SeriesListData getSeriesListData()
	{
		return seriesListData;
	}

	public void setSeriesListData(SeriesListData seriesListData)
	{
		this.seriesListData = seriesListData;
	}

	// Convenience methods
	public void loadSettingsFromFiles() throws IOException, ConfigurationException
	{
		loadEnvironmentSettingsFromFile();
		loadParsingSettingsFromFile();
	}

	public void loadEnvironmentSettingsFromFile() throws IOException, ConfigurationException
	{
		settings.getEnvironmentSettings().load(environmentSettingsFile);
	}

	public void loadParsingSettingsFromFile() throws IOException, ConfigurationException
	{
		settings.getParsingSettings().load(parsingSettingsFile);
	}

	public void loadSeriesListData() throws SQLException
	{
		try (MigrationService service = new MigrationService(settings))
		{
			seriesListData = service.readSeriesList();
		}
	}
}
