package de.subcentral.mig.settings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

public class MigrationParsingSettings
{
	public void load(Path file) throws IOException, ConfigurationException
	{
		XMLConfiguration cfg = new XMLConfiguration();
		FileHandler fileHandler = new FileHandler(cfg);
		fileHandler.load(Files.newBufferedReader(file, Charset.forName("UTF-8")));
		load(cfg);
	}

	public void load(XMLConfiguration cfg)
	{

	}
}
