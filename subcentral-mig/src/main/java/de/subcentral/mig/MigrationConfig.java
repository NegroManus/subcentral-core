package de.subcentral.mig;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;

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
	private SeriesListContent		seriesListContent;

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

	public SeriesListContent getSeriesListContent()
	{
		return seriesListContent;
	}

	public void setSeriesListContent(SeriesListContent seriesListContent)
	{
		this.seriesListContent = seriesListContent;
	}
}
