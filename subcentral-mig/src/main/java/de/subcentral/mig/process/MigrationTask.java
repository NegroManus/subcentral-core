package de.subcentral.mig.process;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeasonThreadParser.SeasonThreadContent;
import de.subcentral.mig.process.SubCentralBoard.Post;
import de.subcentral.mig.repo.MigrationRepo;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class MigrationTask extends Task<Void>
{
	private final MigrationRepo			repo					= new MigrationRepo();
	private final MigrationConfig		config;

	private long						numMigratedSeries;
	private long						numSeriesToProcess;

	private final MigrateSeriesTask		seriesMigrationTask		= new MigrateSeriesTask();
	private final ParseSeasonThreadTask	seasonThreadParserTask	= new ParseSeasonThreadTask();
	private final SeasonThreadParser	seasonThreadParser		= new SeasonThreadParser();

	public MigrationTask(MigrationConfig config)
	{
		this.config = config;
	}

	@Override
	protected Void call() throws Exception
	{
		updateTitle("Migrating series");
		migrateSeries();
		return null;
	}

	private void seriesProcessed()
	{
		Platform.runLater(() ->
		{
			numMigratedSeries++;
			if (numMigratedSeries < numSeriesToProcess)
			{
				updateMessage("Migrating series " + (numMigratedSeries + 1) + " / " + numSeriesToProcess);
			}
			else
			{
				updateMessage("Migration of series finished");
			}
			updateProgress(numMigratedSeries, numSeriesToProcess);
		});
	}

	private void migrateSeries()
	{
		List<Series> seriesToProcess;
		if (config.isCompleteMigration())
		{
			seriesToProcess = ImmutableList.copyOf(config.getSeriesListContent().getSeries());
		}
		else
		{
			seriesToProcess = ImmutableList.copyOf(config.getSelectedSeries());
		}
		numSeriesToProcess = seriesToProcess.size();

		int numSeasonsSuccessfulMigrated = seriesToProcess.stream().map(seriesMigrationTask).mapToInt((Boolean success) ->
		{
			seriesProcessed();
			return Boolean.TRUE.equals(success) ? 1 : 0;
		}).sum();
	}

	private class MigrateSeriesTask implements Function<Series, Boolean>
	{
		@Override
		public Boolean apply(Series series)
		{
			try
			{
				List<Season> seasonsDistinctByThreadId = distinctByThreadId(series.getSeasons());
				List<ParsedSeason> parsedSeasons = seasonsDistinctByThreadId.stream().map(seasonThreadParserTask).collect(Collectors.toList());

				System.out.println("Migrated series: " + series);
				for (ParsedSeason parsedSeason : parsedSeasons)
				{
					System.out.println("Season from series list:");
					System.out.println(parsedSeason.seasonFromSeriesList);
					System.out.println("Seasons from season thread:");
					for (Season seasonFromSeasonThread : parsedSeason.seasonThreadContent.getSeasons())
					{
						System.out.println(seasonFromSeasonThread);
					}
					System.out.println();
				}
				System.out.println();
				System.out.println();

				return Boolean.TRUE;
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				return Boolean.FALSE;
			}
		}
	}

	private class ParseSeasonThreadTask implements Function<Season, ParsedSeason>
	{
		@Override
		public ParsedSeason apply(Season season)
		{
			try
			{
				Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
				if (seasonThreadId == null)
				{
					throw new IllegalArgumentException("No threadID given for season: " + season);
				}

				Post post;
				try (Connection conn = config.getDataSource().getConnection())
				{
					SubCentralBoard scBoard = new SubCentralBoard();
					scBoard.setConnection(conn);
					post = scBoard.getFirstPost(seasonThreadId);
				}

				SeasonThreadContent content = seasonThreadParser.parse(post.getTopic(), post.getMessage());
				return new ParsedSeason(season, content);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
				return null;
			}
		}
	}

	private static Integer getThreadId(Season season)
	{
		return season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
	}

	private static List<Season> distinctByThreadId(Iterable<Season> seasons)
	{
		// Filter out seasons with same thread id
		Map<Integer, Integer> threadIds = new HashMap<>();
		ImmutableList.Builder<Season> builder = ImmutableList.builder();
		for (Season season : seasons)
		{
			Integer threadId = getThreadId(season);
			if (threadId == null || threadIds.putIfAbsent(threadId, threadId) == null)
			{
				builder.add(season);
			}
		}

		return builder.build();
	}

	private class ParsedSeason
	{
		private final Season				seasonFromSeriesList;
		private final SeasonThreadContent	seasonThreadContent;

		public ParsedSeason(Season seasonFromSeriesList, SeasonThreadContent seasonThreadContent)
		{
			this.seasonFromSeriesList = seasonFromSeriesList;
			this.seasonThreadContent = seasonThreadContent;
		}
	}
}
