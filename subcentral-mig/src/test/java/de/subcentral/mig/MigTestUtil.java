package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

public class MigTestUtil
{
	static Document parseDoc(Class<?> testClass, String filename) throws IOException
	{
		return Jsoup.parse(Resources.getResource(testClass, filename).openStream(), StandardCharsets.UTF_8.name(), "http://subcentral.de");
	}

	public static PropertiesConfiguration readPropertiesConfig(String resourceName) throws ConfigurationException
	{
		PropertiesConfiguration cfg = new PropertiesConfiguration();
		FileHandler fileHandler = new FileHandler(cfg);
		fileHandler.load(Resources.getResource(MigTestUtil.class, resourceName));
		return cfg;
	}

	public static Connection connect() throws SQLException, ConfigurationException
	{
		PropertiesConfiguration cfg = readPropertiesConfig("/de/subcentral/mig/migration-env-settings.properties");
		String url = cfg.getString("source.db.url");
		String user = cfg.getString("source.db.user");
		String password = cfg.getString("source.db.password");
		return DriverManager.getConnection(url, user, password);
	}
}
