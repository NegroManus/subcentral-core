package de.subcentral.fx.settings;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;

public class ConfigurationHelper {
	public static void save(FileBased cfg, Path file) throws ConfigurationException {
		try {
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.save(Files.newOutputStream(file), Charset.forName("UTF-8").name());
		}
		catch (IOException e) {
			throw new ConfigurationException(e);
		}
	}

	public static XMLConfiguration load(URL file) throws ConfigurationException {
		try {
			XMLConfiguration cfg = new IndentingXMLConfiguration();
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.load(file.openStream(), Charset.forName("UTF-8").name());
			return cfg;
		}
		catch (IOException e) {
			throw new ConfigurationException(e);
		}
	}

	public static XMLConfiguration load(Path file) throws ConfigurationException {
		try {
			XMLConfiguration cfg = new IndentingXMLConfiguration();
			FileHandler cfgFileHandler = new FileHandler(cfg);
			cfgFileHandler.load(Files.newInputStream(file), Charset.forName("UTF-8").name());
			return cfg;
		}
		catch (IOException e) {
			throw new ConfigurationException(e);
		}
	}
}
