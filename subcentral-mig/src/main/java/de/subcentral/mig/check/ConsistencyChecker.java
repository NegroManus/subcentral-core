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
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.naming.NamingDefaults;
import de.subcentral.core.naming.SeasonNamer;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationApp;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeasonPostParser;
import de.subcentral.mig.process.SeasonPostParser.SeasonPostContent;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListContent;
import de.subcentral.support.subcentralde.SubCentralHttpApi;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbThread;

public class ConsistencyChecker
{
	private static final Logger		log					= LogManager.getLogger(MigrationApp.class);

	private static final Pattern	PATTERN_THREAD_ID	= Pattern.compile("threadID=(\\d+)");

	private final MigrationConfig	config;
	private final Path				logFile;
	private boolean					firstWrite			= true;
	private int						majorChapterNum		= 0;
	private int						minorChapterNum		= 0;

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

		// Perform consistency checks
		// Level: Series
		// Check series against Quickjump entries
		{
			List<Series> quickJumpContent = getQuickJumpContent();
			checkSeriesListAgainstQuickJump(seriesListContent, quickJumpContent);
		}

		loadSeriesBoards(seriesListContent.getSeries());
		checkSeriesListAgainstBoards(seriesListContent);

		// Check subtitle repository
		{
			WbbBoard subRepoBoard = getSubtitleRepositoryBoard();
			List<Series> subRepoQuickJumpContent = getSubtitleRepositoryQuickJumpContent(subRepoBoard);
			List<WbbThread> subRepoThreads = getSubtitleRepositoryThreads(subRepoBoard);
			checkSeriesListAgainstSubRepoThreads(seriesListContent, subRepoThreads);
			checkSubRepoThreadsAgainstSubRepoQuickJump(subRepoThreads, subRepoQuickJumpContent);
		}

		// Level: Season
		loadSeasonThreads(seriesListContent.getSeasons());
		loadSeasonPosts(seriesListContent.getSeasons());
		// Check seasons against [Subs] threads
		{
			List<WbbThread> subsPrefixThreads = getSubsPrefixThreads();
			checkSeriesListSeasonsAgainstSubsPrefixThreads(seriesListContent, subsPrefixThreads);
		}
		// Check seasons against sticky threads
		checkSeriesListSeasonsAgainstStickyThreads(seriesListContent);
		// Check series/seasons against post topics
		checkSeriesListAgainstSeasonPostTopics(seriesListContent);
	}

	private void checkSeriesListSeasonsAgainstStickyThreads(SeriesListContent seriesListContent) throws SQLException, IOException
	{
		List<Season> seasonsNotSticky = new ArrayList<>();
		List<WbbThread> threadsNotSeason = new ArrayList<>();
		for (Series series : seriesListContent.getSeries())
		{
			int boardId = series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			List<WbbThread> stickyThreads = getStickyThreads(boardId);
			ListMatchResult<Season, WbbThread> seasonsToStickyThreads = matchSeasonsAgainstThreads(seriesListContent.getSeasons(), stickyThreads);
			seasonsNotSticky.addAll(seasonsToStickyThreads.onlyInFirstList);
			threadsNotSeason.addAll(seasonsToStickyThreads.onlyInSecondList);
		}

		appendChapter("SeriesList seasons <-> Sticky threads", "Season threads that are not sticky", seasonsNotSticky.stream().map(ConsistencyChecker::formatSeason).collect(Collectors.toList()));
		appendChapter("Sticky threads that are no season threads", threadsNotSeason.stream().map(ConsistencyChecker::formatThread).collect(Collectors.toList()));
	}

	private void checkSeriesListAgainstSeasonPostTopics(SeriesListContent seriesListContent) throws IOException
	{
		List<String> entries = new ArrayList<>();

		// Post -> Seasons
		ListMultimap<WbbPost, Season> postsToSeasons = LinkedListMultimap.create();
		for (Season season : seriesListContent.getSeasons())
		{
			WbbPost post = season.getAttributeValue(Migration.SEASON_ATTR_POST);
			if (post != null)
			{
				postsToSeasons.put(post, season);
			}
		}
		for (WbbPost post : postsToSeasons.keySet())
		{
			List<Season> seriesListSeasons = postsToSeasons.get(post);
			// Parse season post
			SeasonPostContent parsedPost = new SeasonPostParser().parse(post);
			// Compare seasons
			if (!seriesListSeasons.equals(parsedPost.getSeasons()))
			{
				entries.add(joinSeasons(seriesListSeasons) + " != " + joinSeasons(parsedPost.getSeasons()) + " (postID=" + post.getId() + ")");
			}
		}
		appendChapter("Series list <-> season posts", "Seasons from series list != Seasons listed in post topic", entries);
	}

	private static String joinSeasons(Iterable<Season> seasons)
	{
		StringJoiner joiner = new StringJoiner(", ");
		boolean first = true;
		for (Season season : seasons)
		{
			if (first)
			{
				joiner.add(NamingDefaults.getDefaultSeasonNamer().name(season));
				first = false;
			}
			else
			{
				joiner.add(NamingDefaults.getDefaultSeasonNamer().name(season, ImmutableMap.of(SeasonNamer.PARAM_INCLUDE_SERIES, Boolean.FALSE)));
			}
		}
		return joiner.toString();
	}

	private List<WbbThread> getStickyThreads(int boardId) throws SQLException
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			return scBoard.getStickyThreads(boardId);
		}
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

	private void loadSeriesBoards(Iterable<Series> series) throws SQLException
	{
		for (Series srs : series)
		{
			Integer seriesBoardId = srs.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			if (seriesBoardId != null)
			{
				try (Connection conn = config.getDataSource().getConnection())
				{
					WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
					boardApi.setConnection(conn);
					WbbBoard board = boardApi.getBoard(seriesBoardId);
					srs.addAttributeValue(Migration.SERIES_ATTR_BOARD, board);
				}
			}
		}
	}

	private void loadSeasonThreads(Iterable<Season> seasons)
	{
		// Several seasons can point to the same thread (multi season thread)
		// In that case, don't load the thread several time but assign the same WbbThread to all seasons
		Map<Integer, WbbThread> threadCache = new HashMap<>();
		for (Season season : seasons)
		{
			Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
			if (seasonThreadId != null)
			{
				WbbThread thread = threadCache.computeIfAbsent(seasonThreadId, this::loadThread);
				season.addAttributeValue(Migration.SEASON_ATTR_THREAD, thread);
			}
		}
	}

	private WbbThread loadThread(Integer threadId)
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getThread(threadId);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void loadSeasonPosts(Iterable<Season> seasons) throws SQLException
	{
		// Several seasons can point to the same thread (multi season thread)
		// In that case, don't load the post several time but assign the same WbbPost to all seasons
		Map<Integer, WbbPost> postCache = new HashMap<>();
		for (Season season : seasons)
		{
			WbbThread seasonThread = season.getAttributeValue(Migration.SEASON_ATTR_THREAD);
			if (seasonThread != null)
			{
				WbbPost post = postCache.computeIfAbsent(seasonThread.getFirstPostId(), this::loadPost);
				season.addAttributeValue(Migration.SEASON_ATTR_POST, post);
			}
		}
	}

	private WbbPost loadPost(int postId)
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getPost(postId);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
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
		int subRepoBoardId = config.getEnvironmentSettings().getInt("sc.subrepo.boardid");
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
		Matcher threadIdMatcher = PATTERN_THREAD_ID.matcher("");
		for (Element option : options)
		{
			String value = option.attr("value");
			if (value.isEmpty())
			{
				continue;
			}
			String seriesName = option.text();
			Integer threadId;
			if (threadIdMatcher.reset(value).find())
			{
				threadId = Integer.valueOf(threadIdMatcher.group(1));
			}
			else
			{
				threadId = null;
				log.warn("Subtitle repository QuickJump entry's link contains no threadID:" + option.outerHtml());
			}
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

	private List<WbbThread> getSubsPrefixThreads() throws SQLException
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
			WbbBoard board = series.getAttributeValue(Migration.SERIES_ATTR_BOARD);
			if (!series.getName().equals(board.getTitle()))
			{
				entries.add(formatSeries(series) + " != " + formatBoard(board));
			}
		}
		appendChapter("SeriesList <-> Boards", "Series name <-> Board title", entries);
	}

	private void checkSeriesListAgainstSubRepoThreads(SeriesListContent seriesListContent, List<WbbThread> subRepoThreads) throws IOException
	{
		ListMatchResult<Series, WbbThread> seriesListToSubRepoThreads = matchLists(seriesListContent.getSeries(),
				subRepoThreads,
				(Series series, WbbThread repoThread) -> series.getName().equals(repoThread.getTopic()));

		appendChapter("SeriesList <-> Subtitle repository threads",
				"Series only in SeriesList",
				seriesListToSubRepoThreads.onlyInFirstList.stream().map((ConsistencyChecker::formatSeries)).collect(Collectors.toList()));
		appendChapter("Series only in Subtitle repository", seriesListToSubRepoThreads.onlyInSecondList.stream().map(ConsistencyChecker::formatThread).collect(Collectors.toList()));
	}

	private void checkSubRepoThreadsAgainstSubRepoQuickJump(List<WbbThread> subRepoThreads, List<Series> subRepoQuickJumpContent) throws IOException
	{
		ListMatchResult<WbbThread, Series> threadToQjEntries = matchLists(subRepoThreads, subRepoQuickJumpContent, (WbbThread repoThread, Series qjEntry) ->
		{
			Integer qjEntryThreadId = qjEntry.getAttributeValue(Migration.SERIES_ATTR_SUB_REPO_THREAD_ID);
			return qjEntryThreadId != null && qjEntryThreadId.intValue() == repoThread.getId();
		});

		List<String> unmatchedThreadsEntries = threadToQjEntries.onlyInFirstList.stream().map((WbbThread t) -> formatThread(t)).collect(Collectors.toList());
		appendChapter("Subtitle repository threads <-> Subtitle repository QuickJump", "Threads that have no entry in QuickJump", unmatchedThreadsEntries);

		List<String> unmatchedQjEntriesEntries = threadToQjEntries.onlyInSecondList.stream().map((Series s) -> formatSeries(s)).collect(Collectors.toList());
		appendChapter("QuickJump entries that don't link to a repository thread", unmatchedQjEntriesEntries);

		List<String> inconsistentTitles = threadToQjEntries.matchingElems.entrySet()
				.stream()
				.filter((Map.Entry<WbbThread, Series> entry) -> !entry.getKey().getTopic().equals(entry.getValue().getName()))
				.map((Map.Entry<WbbThread, Series> entry) -> formatThread(entry.getKey()) + " != " + formatSeries(entry.getValue()))
				.collect(Collectors.toList());
		appendChapter("Thread topic != QuickJump entry", inconsistentTitles);
	}

	private void checkSeriesListSeasonsAgainstSubsPrefixThreads(SeriesListContent seriesListContent, List<WbbThread> subsThreads) throws IOException
	{
		ListMatchResult<Season, WbbThread> seasonsToSubsThreads = matchSeasonsAgainstThreads(seriesListContent.getSeasons(), subsThreads);

		appendChapter("SeriesList <-> [Subs] Threads",
				"Season threads without the [Subs] prefix",
				seasonsToSubsThreads.onlyInFirstList.stream().map(ConsistencyChecker::formatSeason).collect(Collectors.toList()));

		appendChapter("Threads with the [Subs] prefix that are not in in the SeriesList",
				seasonsToSubsThreads.onlyInSecondList.stream().map(ConsistencyChecker::formatThread).collect(Collectors.toList()));
	}

	/*
	 * Matching
	 */
	private static ListMatchResult<Season, WbbThread> matchSeasonsAgainstThreads(List<Season> seasons, List<WbbThread> threads)
	{
		return matchLists(seasons, threads, (Season season, WbbThread thread) ->
		{
			Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
			return seasonThreadId != null && seasonThreadId.intValue() == thread.getId();
		});
	}

	private static <T, U> ListMatchResult<T, U> matchLists(List<T> firstList, List<U> secondList, BiPredicate<T, U> matcher)
	{
		Map<T, U> matchingElems = new HashMap<>();
		List<T> onlyInFirstList = new ArrayList<>();
		List<U> onlyInSecondList = new ArrayList<>(secondList);

		boolean matchFound;
		for (T firstElem : firstList)
		{
			matchFound = false;
			ListIterator<U> secondIter = onlyInSecondList.listIterator();
			while (secondIter.hasNext())
			{
				U secondElem = secondIter.next();
				if (matcher.test(firstElem, secondElem))
				{
					matchFound = true;
					matchingElems.put(firstElem, secondElem);
					secondIter.remove();
					break;
				}
			}
			if (!matchFound)
			{
				onlyInFirstList.add(firstElem);
			}
		}
		return new ListMatchResult<>(matchingElems, onlyInFirstList, onlyInSecondList);
	}

	private static class ListMatchResult<T, U>
	{
		private final Map<T, U>	matchingElems;
		private final List<T>	onlyInFirstList;
		private final List<U>	onlyInSecondList;

		public ListMatchResult(Map<T, U> matchingElems, List<T> onlyInFirstList, List<U> onlyInSecondList)
		{
			this.matchingElems = matchingElems;
			this.onlyInFirstList = onlyInFirstList;
			this.onlyInSecondList = onlyInSecondList;
		}
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
			minorChapterNum = 1;
			entries.add(0, majorChapterNum + "." + minorChapterNum + ": " + minorChapter);
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
			entries.add(0, majorChapterNum + "." + minorChapterNum + ": " + minorChapter);
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

	private static String formatSeason(Season season)
	{
		return NamingDefaults.getDefaultSeasonNamer().name(season) + " (threadID=" + season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID) + ")";
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
