package de.subcentral.mig.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.repo.MigrationRepo;
import javafx.concurrent.Task;

public class MigrationTask extends Task<Void>
{
	private final MigrationRepo		repo	= new MigrationRepo();
	private final MigrationConfig	config;
	private Connection				conn;

	public MigrationTask(MigrationConfig config)
	{
		this.config = config;
	}

	@Override
	protected Void call() throws Exception
	{
		connect();
		try
		{
			migrateSeries();
			return null;
		}
		finally
		{
			disconnect();
		}
	}

	private void connect() throws SQLException
	{
		String url = config.getEnvironmentSettings().getString("sc.db.url");
		String user = config.getEnvironmentSettings().getString("sc.db.user");
		String password = config.getEnvironmentSettings().getString("sc.db.password");
		conn = DriverManager.getConnection(url, user, password);
	}

	private void disconnect() throws SQLException
	{
		if (conn != null)
		{
			conn.close();
			conn = null;
		}
	}

	private void migrateSeries()
	{
		List<Series> seriesToMigrate;
		if (config.isCompleteMigration())
		{
			seriesToMigrate = ImmutableList.copyOf(config.getSeriesListContent().getSeries());
		}
		else
		{
			seriesToMigrate = ImmutableList.copyOf(config.getSelectedSeries());
		}

	}

}
