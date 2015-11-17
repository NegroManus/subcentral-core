package de.subcentral.mig.check;

import java.sql.Connection;

import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import de.subcentral.mig.process.SubCentralBoard;
import de.subcentral.mig.process.SubCentralBoard.Post;

public class SanityChecker
{
	private final MigrationConfig config;

	public SanityChecker(MigrationConfig config)
	{
		this.config = config;
	}

	public void check() throws Exception
	{
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postId");
		SeriesListContent seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			SubCentralBoard scBoard = new SubCentralBoard();
			scBoard.setConnection(conn);
			Post seriesListPost = scBoard.getPost(seriesListPostId);
			seriesListContent = new SeriesListParser().parsePost(seriesListPost.getMessage());
		}

	}

	public void checkSeriesListSeasons()
	{

	}
}
