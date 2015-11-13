package de.subcentral.mig.controller;

import java.nio.file.Path;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;

public class MigrationConfig
{
	private Path					environmentSettingsFile;
	private Path					parsingSettingsFile;
	private PropertiesConfiguration	environmentSettings;
	private XMLConfiguration		parsingSettings;
	private ImmutableList<Series>	selectedSeries	= ImmutableList.of();

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

	public ImmutableList<Series> getSelectedSeries()
	{
		return selectedSeries;
	}

	public void setSelectedSeries(ImmutableList<Series> selectedSeries)
	{
		this.selectedSeries = selectedSeries;
	}
}
