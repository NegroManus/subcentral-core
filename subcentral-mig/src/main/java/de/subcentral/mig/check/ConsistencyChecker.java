package de.subcentral.mig.check;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import de.subcentral.mig.process.SubCentralBoard;
import de.subcentral.mig.process.SubCentralBoard.WbbBoard;
import de.subcentral.mig.process.SubCentralBoard.WbbPost;
import de.subcentral.mig.process.SubCentralBoard.WbbThread;
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class ConsistencyChecker
{
	private final MigrationConfig	config;
	private final Path				logFile;
	private boolean					firstWrite		= true;
	private int						majorChapter	= 0;
	private int						minorChapter	= 0;

	public ConsistencyChecker(MigrationConfig config) throws IOException
	{
		this.config = config;
		this.logFile = resolveLogFile();
	}

	private Path resolveLogFile() throws IOException
	{
		Path logDir = Paths.get(config.getEnvironmentSettings().getString("log.dir"));
		if (Files.notExists(logDir))
		{
			throw new NoSuchFileException(logDir.toString());
		}
		return logDir.resolve("consistencycheck.log");
	}

	public void check() throws Exception
	{
		SeriesListContent seriesListContent = getSeriesListContent();
		List<Series> quickJumpContent = getQuickJumpContent();
		List<WbbThread> subsThreads = getSubsThreads();

		checkSeriesListAgainstQuickJump(seriesListContent.getSeries(), quickJumpContent);
		checkSeriesListAgainstBoards(seriesListContent);
		checkSeriesListSeasonsAgainstSubsThreads(seriesListContent, subsThreads);
	}

	private void checkSeriesListAgainstQuickJump(List<Series> seriesListSeries, List<Series> quickJumpSeries) throws IOException
	{
		List<String> logLines = new ArrayList<>();
		logLines.add(formatMajorChapter("SeriesList <-> QuickJump"));

		logLines.add(formatMinorChapter("Series only in SeriesList"));
		List<Series> seriesOnlyInSeriesList = new ArrayList<>(seriesListSeries);
		seriesOnlyInSeriesList.removeAll(quickJumpSeries);
		for (Series series : seriesOnlyInSeriesList)
		{
			logLines.add("\t" + series.getName());
		}
		logLines.add("");

		logLines.add(formatMinorChapter("Series only in QuickJump"));
		List<Series> seriesOnlyInQuickJump = new ArrayList<>(quickJumpSeries);
		seriesOnlyInQuickJump.removeAll(seriesListSeries);
		for (Series series : seriesOnlyInQuickJump)
		{
			logLines.add("\t" + series.getName());
		}

		logLines.add("");
		logLines.add("");
		appendLogLines(logLines);
	}

	private void checkSeriesListAgainstBoards(SeriesListContent seriesListContent) throws IOException, SQLException
	{
		List<String> seriesListSeriesNameBoardTitleMismatch = new ArrayList<>();

		for (Series series : seriesListContent.getSeries())
		{
			int seriesBoardId = series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			try (Connection conn = config.getDataSource().getConnection())
			{
				SubCentralBoard boardApi = new SubCentralBoard();
				boardApi.setConnection(conn);
				WbbBoard board = boardApi.getBoard(seriesBoardId);
				if (!series.getName().equals(board.getTitle()))
				{
					seriesListSeriesNameBoardTitleMismatch.add("\t\"" + series.getName() + "\" != \"" + board.getTitle() + "\"");
				}
			}
		}
		List<String> logLines = new ArrayList<>();
		logLines.add(formatMajorChapter("SeriesList <-> Boards"));
		logLines.add(formatMinorChapter("Series name <-> Board title"));
		logLines.addAll(seriesListSeriesNameBoardTitleMismatch);

		logLines.add("");
		logLines.add("");
		appendLogLines(logLines);
	}

	private void checkSeriesListSeasonsAgainstSubsThreads(SeriesListContent seriesListContent, List<WbbThread> subsThreads) throws IOException
	{
		List<String> logLines = new ArrayList<>();

		final Predicate<Season> containedInSubsThreads = (Season season) ->
		{
			for (WbbThread thread : subsThreads)
			{
				Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
				if (seasonThreadId.intValue() == thread.getId())
				{
					return false;
				}
			}
			return true;
		};
		final Predicate<WbbThread> containedInSeasons = (WbbThread thread) ->
		{
			for (Season season : seriesListContent.getSeasons())
			{
				Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
				if (seasonThreadId.intValue() == thread.getId())
				{
					return false;
				}
			}
			return true;
		};
		final Consumer<Season> addSeasonToLogLines = (Season season) ->
		{
			logLines.add("\t" + NamingDefaults.getDefaultSeasonNamer().name(season) + " (threadID=" + season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID) + ")");
		};
		final Consumer<WbbThread> addThreadToLogLines = (WbbThread thread) ->
		{
			logLines.add("\t\"" + thread.getTopic() + "\" (threadID=" + thread.getId() + ")");
		};

		logLines.add(formatMajorChapter("SeriesList <-> [Subs] Threads"));
		logLines.add(formatMinorChapter("Season threads without the [Subs] prefix"));
		seriesListContent.getSeasons().stream().filter(containedInSubsThreads).forEach(addSeasonToLogLines);
		logLines.add("");

		logLines.add(formatMinorChapter("Threads with the [Subs] prefix that are not in in the SeriesList"));
		subsThreads.stream().filter(containedInSeasons).forEach(addThreadToLogLines);

		logLines.add("");
		logLines.add("");
		appendLogLines(logLines);
	}

	private SeriesListContent getSeriesListContent() throws SQLException
	{
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
		SeriesListContent seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			SubCentralBoard scBoard = new SubCentralBoard();
			scBoard.setConnection(conn);
			WbbPost seriesListPost = scBoard.getPost(seriesListPostId);
			seriesListContent = new SeriesListParser().parsePost(seriesListPost.getMessage());
			return seriesListContent;
		}
	}

	/**
	 * <pre>
	 * <form method="get" action="index.php" class="quickJump">
	 *  	<input type="hidden" name="page" value="WbbBoard" />
	 *		<select name="boardID" onchange="if (this.options[this.selectedIndex].value != 0) this.form.submit()" id="QJselect">
	 *			<option value=""> Serien-QuickJump </option>
	 *			<optgroup label="-- 0-9 --">
	 *				<option value="427">10 Things I Hate About You</option>
	 * </pre>
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<Series> getQuickJumpContent() throws IOException
	{
		SubCentralHttpApi api = new SubCentralHttpApi();
		Document mainPage = api.getContent("index.php");
		Element quickJumpSelect = mainPage.getElementById("QJselect");
		if (quickJumpSelect == null)
		{
			throw new IllegalStateException("Quickjump form could not be found");
		}
		Elements options = quickJumpSelect.getElementsByTag("option");
		// "- 1" because one option is the empty value
		List<Series> seriesList = new ArrayList<>(options.size() - 1);
		for (Element option : options)
		{
			String value = option.attr("value");
			if (value.isEmpty())
			{
				continue;
			}
			String seriesName = option.text();
			Integer boardId = Integer.valueOf(value);
			Series series = new Series(seriesName);
			series.addAttributeValue(Migration.SERIES_ATTR_BOARD_ID, boardId);
			seriesList.add(series);
		}
		return ImmutableList.copyOf(seriesList);
	}

	private List<WbbThread> getSubsThreads() throws SQLException
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			SubCentralBoard boardApi = new SubCentralBoard();
			boardApi.setConnection(conn);
			return boardApi.getThreadsByPrefix("[Subs]");
		}
	}

	private String formatMajorChapter(String title)
	{
		minorChapter = 0;
		return (++majorChapter) + ": " + title;
	}

	private String formatMinorChapter(String title)
	{
		return majorChapter + "." + (++minorChapter) + ": " + title;
	}

	private void appendLogLines(Iterable<String> logLines) throws IOException
	{
		if (firstWrite)
		{
			Files.write(logFile, logLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			firstWrite = false;
		}
		else
		{
			Files.write(logFile, logLines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		}
	}
}
