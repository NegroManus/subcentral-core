package de.subcentral.mig.process;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import de.subcentral.mig.parse.SeasonPostParser;
import de.subcentral.mig.parse.SeasonPostParser.SeasonPostData;
import de.subcentral.mig.parse.SeriesListParser;
import de.subcentral.mig.parse.SeriesListParser.SeriesListData;
import de.subcentral.mig.parse.SubberListParser;
import de.subcentral.mig.parse.SubberListParser.SubberListData;
import de.subcentral.mig.settings.MigrationEnvironmentSettings;
import de.subcentral.mig.settings.MigrationSettings;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class MigrationService implements AutoCloseable
{
	private static final Logger		log	= LogManager.getLogger(MigrationService.class);

	private final MigrationSettings	settings;
	private final DataSource		sourceDataSource;
	private final DataSource		targetDataSource;

	public MigrationService(MigrationSettings settings)
	{
		this.settings = Objects.requireNonNull(settings, "settings");
		this.sourceDataSource = createSourceDataSource();
		this.targetDataSource = createTargetDataSource();
	}

	private DataSource createSourceDataSource()
	{
		MigrationEnvironmentSettings env = settings.getEnvironmentSettings();
		return createDataSource(env.getSourceDbDriverClass(), env.getSourceDbUrl(), env.getSourceDbUser(), env.getSourceDbPassword());
	}

	private DataSource createTargetDataSource()
	{
		MigrationEnvironmentSettings env = settings.getEnvironmentSettings();
		return createDataSource(env.getTargetDbDriverClass(), env.getTargetDbUrl(), env.getTargetDbUser(), env.getTargetDbPassword());
	}

	private static DataSource createDataSource(String driverClass, String url, String user, String password)
	{
		try
		{
			ComboPooledDataSource ds = new ComboPooledDataSource();
			ds.setDriverClass(driverClass);
			ds.setJdbcUrl(url);
			ds.setUser(user);
			ds.setPassword(password);
			return ds;
		}
		catch (PropertyVetoException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	public DataSource getSourceDataSource()
	{
		return sourceDataSource;
	}

	public DataSource getTargetDataSource()
	{
		return targetDataSource;
	}

	public SeriesListData readSeriesList() throws SQLException
	{
		log.debug("Reading series list");
		long start = System.currentTimeMillis();
		try (Connection conn = sourceDataSource.getConnection())
		{
			WoltlabBurningBoard board = new WoltlabBurningBoard(conn);
			int postId = settings.getEnvironmentSettings().getSourceSeriesListPostId();
			WbbPost post = board.getPost(postId);
			SeriesListParser parser = new SeriesListParser();
			SeriesListData data = parser.parsePost(post);
			long duration = System.currentTimeMillis() - start;
			log.debug("Read series list in {} ms: {} series, {} seasons, {} networks", duration, data.getSeries().size(), data.getSeasons().size(), data.getNetworks().size());
			return data;
		}
	}

	public SubberListData readSubberList() throws SQLException
	{
		log.debug("Reading subber list");
		long start = System.currentTimeMillis();
		try (Connection conn = sourceDataSource.getConnection())
		{
			WoltlabBurningBoard board = new WoltlabBurningBoard(conn);
			int postId = settings.getEnvironmentSettings().getSourceSubberListPostId();
			WbbPost post = board.getPost(postId);
			SubberListParser parser = new SubberListParser();
			SubberListData data = parser.parsePost(post);
			long duration = System.currentTimeMillis() - start;
			log.debug("Read subber list in {} ms: {} subbers", duration, data.getSubbers().size());
			return data;
		}
	}

	public SeasonPostData readSeasonPost(int seasonThreadId) throws SQLException
	{
		log.debug("Reading season post");
		long start = System.currentTimeMillis();
		try (Connection conn = sourceDataSource.getConnection())
		{
			WoltlabBurningBoard board = new WoltlabBurningBoard(conn);
			WbbPost post = board.getFirstPost(seasonThreadId);
			if (post == null)
			{
				return null;
			}
			SeasonPostParser parser = new SeasonPostParser();
			SeasonPostData data = parser.parsePost(post);
			long duration = System.currentTimeMillis() - start;
			log.debug("Read season post in {} ms: {} seasons, {} episodes, {} subtitle releases", duration, data.getSeasons().size(), data.getEpisodes().size(), data.getSubtitleReleases().size());
			return data;
		}
	}

	@Override
	public void close() throws SQLException
	{
		DataSources.destroy(sourceDataSource);
		DataSources.destroy(targetDataSource);
	}
}
