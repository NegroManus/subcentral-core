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

import org.jsoup.Jsoup;
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
import de.subcentral.support.subcentralde.SubCentralHttpApi;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbThread;

public class ConsistencyChecker
{
	private final MigrationConfig	config;
	private final Path				logFile;
	private boolean					firstWrite		= true;
	private int						majorChapterNum	= 0;
	private int						minorChapterNum	= 0;

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
		// Read all necessary data
		SeriesListContent seriesListContent = getSeriesListContent();
		List<Series> quickJumpContent = getQuickJumpContent();
		WbbBoard subRepoBoard = getSubtitleRepositoryBoard();
		List<WbbThread> subRepoThreads = getSubtitleRepositoryThreads(subRepoBoard);
		List<Series> subRepoQuickJumpContent = getSubtitleRepositoryQuickJumpContent(subRepoBoard);
		List<WbbThread> subsThreads = getSubsThreads();

		// Perform consistency checks
		// Level: Series
		checkSeriesListAgainstQuickJump(seriesListContent, quickJumpContent);
		checkSeriesListAgainstBoards(seriesListContent);
		checkSeriesListAgainstSubRepoThreads(seriesListContent, subRepoThreads);
		checkSubRepoThreadsAgainstSubRepoQuickJump(subRepoThreads, subRepoQuickJumpContent);

		// Level: Season
		checkSeriesListSeasonsAgainstSubsThreads(seriesListContent, subsThreads);
	}

	/*
	 * Get necessary data
	 */
	private SeriesListContent getSeriesListContent() throws SQLException
	{
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
		SeriesListContent seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
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
			throw new IllegalStateException("Quickjump <select> element could not be found");
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

	private WbbBoard getSubtitleRepositoryBoard() throws SQLException
	{
		int subRepoBoardId = config.getEnvironmentSettings().getInt("sc.subrepo.boardId");
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getBoard(subRepoBoardId);
		}
	}

	/**
	 * <pre>
	 * <form>
	 *	<select id="subablage-select" name="Subberablage QuickJump" ONCHANGE="location = this.options[this.selectedIndex].value;">
	 *		<option value="">QuickJump Untertitelablage</option>
	 *		<optgroup label="--- 0-9 ---">
	 *		<option value="index.php?page=Thread&threadID=10872">10 Things I Hate About You</option>
	 * </pre>
	 * 
	 * @param subRepoBoard
	 * @return
	 */
	private List<Series> getSubtitleRepositoryQuickJumpContent(WbbBoard subRepoBoard)
	{
		Document description = Jsoup.parse(subRepoBoard.getDescription(), Migration.SUBCENTRAL_HOST);
		Element quickJumpSelect = description.select("select#subablage-select").first();
		if (quickJumpSelect == null)
		{
			throw new IllegalStateException("Subtitle Repository Quickjump <select> element could not be found");
		}
		Elements options = quickJumpSelect.getElementsByTag("option");
		List<Series> seriesList = new ArrayList<>(options.size() - 1);
		for (Element option : options)
		{
			String value = option.attr("value");
			if (value.isEmpty())
			{
				continue;
			}
			String seriesName = option.text();
			Integer threadId = Integer.valueOf(value.substring(value.indexOf("threadID=") + 9, value.length()));
			Series series = new Series(seriesName);
			series.addAttributeValue(Migration.SERIES_ATTR_SUB_REPO_THREAD_ID, threadId);
			seriesList.add(series);
		}
		return ImmutableList.copyOf(seriesList);
	}

	private List<WbbThread> getSubtitleRepositoryThreads(WbbBoard subRepoBoard) throws SQLException
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getThreadsByBoardId(subRepoBoard.getId());
		}
	}

	private List<WbbThread> getSubsThreads() throws SQLException
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getThreadsByPrefix("[Subs]");
		}
	}

	/*
	 * Perform consistency checks
	 */
	private void checkSeriesListAgainstQuickJump(SeriesListContent seriesListContent, List<Series> quickJumpSeries) throws IOException
	{
		List<String> entries = new ArrayList<>();
		List<Series> seriesOnlyInSeriesList = new ArrayList<>(seriesListContent.getSeries());
		seriesOnlyInSeriesList.removeAll(quickJumpSeries);
		for (Series series : seriesOnlyInSeriesList)
		{
			entries.add(formatSeries(series));
		}
		appendChapter("SeriesList <-> QuickJump", "Series only in SeriesList", entries);

		entries.clear();
		List<Series> seriesOnlyInQuickJump = new ArrayList<>(quickJumpSeries);
		seriesOnlyInQuickJump.removeAll(seriesListContent.getSeries());
		for (Series series : seriesOnlyInQuickJump)
		{
			entries.add(formatSeries(series));
		}
		appendChapter("Series only in QuickJump", entries);
	}

	private void checkSeriesListAgainstBoards(SeriesListContent seriesListContent) throws IOException, SQLException
	{
		List<String> entries = new ArrayList<>();

		for (Series series : seriesListContent.getSeries())
		{
			int seriesBoardId = series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			try (Connection conn = config.getDataSource().getConnection())
			{
				WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
				boardApi.setConnection(conn);
				WbbBoard board = boardApi.getBoard(seriesBoardId);
				if (!series.getName().equals(board.getTitle()))
				{
					entries.add(formatSeries(series) + " != " + formatBoard(board));
				}
			}
		}
		appendChapter("SeriesList <-> Boards", "Series name <-> Board title", entries);
	}

	private void checkSeriesListAgainstSubRepoThreads(SeriesListContent seriesListContent, List<WbbThread> subRepoThreads) throws IOException
	{
		List<String> entries = new ArrayList<>();
		for (Series series : seriesListContent.getSeries())
		{
			boolean matchingSubRepoThreadFound = false;
			for (WbbThread thread : subRepoThreads)
			{
				if (series.getName().equals(thread.getTopic()))
				{
					matchingSubRepoThreadFound = true;
					break;
				}
			}
			if (!matchingSubRepoThreadFound)
			{
				entries.add(formatSeries(series));
			}
		}
		appendChapter("SeriesList <-> Subtitle repository threads", "Series only in SeriesList", entries);

		entries.clear();
		for (WbbThread thread : subRepoThreads)
		{
			boolean matchingSeriesFound = false;
			for (Series series : seriesListContent.getSeries())
			{
				if (thread.getTopic().equals(series.getName()))
				{
					matchingSeriesFound = true;
					break;
				}
			}
			if (!matchingSeriesFound)
			{
				entries.add(formatThread(thread));
			}
		}
		appendChapter("Series only in Subtitle repository", entries);
	}

	private void checkSubRepoThreadsAgainstSubRepoQuickJump(List<WbbThread> subRepoThreads, List<Series> subRepoQuickJumpContent) throws IOException
	{
		List<String> entries = new ArrayList<>();

		appendChapter("Subtitle repository threads <-> Subtitle repository QuickJump", "Threads that have no entry in QuickJump", entries);
		entries.clear();
		appendChapter("QuickJump entries that ", entries);
	}

	private void checkSeriesListSeasonsAgainstSubsThreads(SeriesListContent seriesListContent, List<WbbThread> subsThreads) throws IOException
	{
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

		final List<String> entries = new ArrayList<>();
		final Consumer<Season> addSeasonEntries = (Season season) ->
		{
			entries.add(NamingDefaults.getDefaultSeasonNamer().name(season) + " (threadID=" + season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID) + ")");
		};
		final Consumer<WbbThread> addThreadEntries = (WbbThread thread) ->
		{
			entries.add(formatThread(thread));
		};

		seriesListContent.getSeasons().stream().filter(containedInSubsThreads).forEach(addSeasonEntries);
		appendChapter("SeriesList <-> [Subs] Threads", "Season threads without the [Subs] prefix", entries);

		entries.clear();
		subsThreads.stream().filter(containedInSeasons).forEach(addThreadEntries);
		appendChapter("Threads with the [Subs] prefix that are not in in the SeriesList", entries);
	}

	/*
	 * Append to log
	 */
	private void appendChapter(String minorChapter, List<String> entries) throws IOException
	{
		appendChapter(null, minorChapter, entries);
	}

	private void appendChapter(String majorChapter, String minorChapter, List<String> entries) throws IOException
	{
		// format entries
		entries.replaceAll((String entry) -> "\t" + entry);

		if (majorChapter != null)
		{
			majorChapterNum++;
			minorChapterNum = 0;
			entries.add(0, majorChapterNum + "." + minorChapterNum + ": minorChapter");
			entries.add(0, majorChapterNum + ": " + majorChapter);
			if (!firstWrite)
			{
				entries.add(0, "");
				entries.add(0, "");
			}
		}
		else
		{
			minorChapterNum++;
			entries.add(0, majorChapterNum + "." + minorChapterNum + ": minorChapter");
			if (!firstWrite)
			{
				entries.add(0, "");
			}
		}

		appendLogLines(entries);
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

	private static String formatSeries(Series series)
	{
		return series.getName();
	}

	private static String formatBoard(WbbBoard board)
	{
		return "\"" + board.getTitle() + "\" (boardID=" + board.getId() + ")";
	}

	private static String formatThread(WbbThread thread)
	{
		return "\"" + thread.getTopic() + "\" (threadID=" + thread.getId() + ")";
	}

}
