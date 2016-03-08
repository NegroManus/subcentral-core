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
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
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
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.NamingDefaults;
import de.subcentral.core.name.ReleaseNamer;
import de.subcentral.core.name.SeasonNamer;
import de.subcentral.mig.Migration;
import de.subcentral.mig.MigrationConfig;
import de.subcentral.mig.process.SeasonPostParser;
import de.subcentral.mig.process.SeasonPostParser.SeasonPostData;
import de.subcentral.mig.process.SeriesListParser;
import de.subcentral.mig.process.SeriesListParser.SeriesListData;
import de.subcentral.support.subcentralde.SubCentralHttpApi;
import de.subcentral.support.woltlab.WoltlabBurningBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbBoard;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbThread;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WcfAttachment;

public class ConsistencyChecker
{
	private static final Logger		log					= LogManager.getLogger(ConsistencyChecker.class);

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
		Path logFile = logDir.resolve("consistencycheck.log");
		log.info("Writing the results to {}", logFile);
		return logFile;
	}

	public void check() throws Exception
	{
		// Read all necessary data
		SeriesListData seriesListContent = readSeriesListContent();

		// Perform consistency checks
		// Level: Series
		// Check series against Quickjump entries
		{
			List<Series> quickJumpContent = readQuickJumpContent();
			checkSeriesListAgainstQuickJump(seriesListContent, quickJumpContent);
		}

		readSeriesBoards(seriesListContent.getSeries());
		checkSeriesListAgainstBoards(seriesListContent);

		// Check subtitle repository
		{
			WbbBoard subRepoBoard = readSubtitleRepositoryBoard();
			List<Series> subRepoQuickJumpContent = readSubtitleRepositoryQuickJumpContent(subRepoBoard);
			List<WbbThread> subRepoThreads = readSubtitleRepositoryThreads(subRepoBoard);
			checkSeriesListAgainstSubRepo(seriesListContent, subRepoThreads);
			checkSubRepoThreadsAgainstSubRepoQuickJump(subRepoThreads, subRepoQuickJumpContent);
		}

		// Level: Season
		{
			ListMultimap<WbbPost, Season> postsToSeasons = readSeasonPosts(seriesListContent.getSeasons());
			List<WbbPost> seasonPosts = ImmutableList.copyOf(postsToSeasons.keySet());
			// Check seasons against [Subs] threads
			{
				List<WbbThread> subsPrefixThreads = readSubsPrefixThreads();
				checkSeriesListAgainstSubsPrefixThreads(seasonPosts, subsPrefixThreads);
			}
			// Check seasons against sticky threads
			checkSeriesListAgainstStickyThreads(seriesListContent, seasonPosts);
			{
				List<WcfAttachment> subRepoAttachments = readSubRepoAttachments();
				// Check series/seasons against post topics
				// Check attachments of all season posts against attachments of subtitle repository
				checkSeriesListAgainstSeasonPostsAgainstSubRepo(postsToSeasons, subRepoAttachments);
			}
		}
	}

	/*
	 * Read necessary data
	 */
	private SeriesListData readSeriesListContent() throws SQLException
	{
		log.debug("Reading SeriesList content");
		int seriesListPostId = config.getEnvironmentSettings().getInt("sc.serieslist.postid");
		SeriesListData seriesListContent;
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			WbbPost seriesListPost = scBoard.getPost(seriesListPostId);
			seriesListContent = new SeriesListParser().parsePost(seriesListPost.getMessage());
			log.debug("Read SeriesList content: {} series, {} seasons, {} networks",
					seriesListContent.getSeries().size(),
					seriesListContent.getSeasons().size(),
					seriesListContent.getNetworks().size());
			return seriesListContent;
		}
	}

	private void readSeriesBoards(List<Series> series) throws SQLException
	{
		log.debug("Reading series boards");
		series.parallelStream().forEach(this::readSeriesBoard);
		log.debug("Read series boards");
	}

	private void readSeriesBoard(Series series)
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			Integer seriesBoardId = series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			if (seriesBoardId != null)
			{
				WbbBoard board = boardApi.getBoard(seriesBoardId);
				if (board != null)
				{
					series.addAttributeValue(Migration.SERIES_ATTR_BOARD, board);
				}
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private ListMultimap<WbbPost, Season> readSeasonPosts(List<Season> seasons) throws SQLException
	{
		log.debug("Reading season posts");
		// Several seasons can point to the same thread (multi season thread)
		// In that case, don't load the post several time but assign the same WbbPost to all seasons
		Map<Integer, WbbPost> postCache = new ConcurrentHashMap<>();
		ListMultimap<WbbPost, Season> postsToSeasons = Multimaps.synchronizedListMultimap(LinkedListMultimap.create());
		// To increase performance, parallelStream() can be used. But then the post are not sorted
		seasons.stream().forEach((Season season) ->
		{
			Integer seasonThreadId = season.getAttributeValue(Migration.SEASON_ATTR_THREAD_ID);
			if (seasonThreadId != null)
			{
				WbbPost post = postCache.computeIfAbsent(seasonThreadId, this::loadFirstPost);
				if (post != null)
				{
					season.addAttributeValue(Migration.SEASON_ATTR_POST, post);
					postsToSeasons.put(post, season);
				}
			}
		});
		log.debug("Read {} season posts", postCache.size());
		return ImmutableListMultimap.copyOf(postsToSeasons);
	}

	private WbbPost loadFirstPost(Integer threadId)
	{
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			return boardApi.getFirstPost(threadId);
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
	private List<Series> readQuickJumpContent() throws IOException
	{
		log.debug("Reading QuickJump content");
		SubCentralHttpApi api = new SubCentralHttpApi();
		Document mainPage = api.getContent("index.php");
		Element quickJumpSelect = mainPage.getElementById("QJselect");
		if (quickJumpSelect == null)
		{
			throw new IllegalStateException("Quickjump <select> element could not be found");
		}
		Elements options = quickJumpSelect.getElementsByTag("option");
		// "- 1" because one option is the empty value
		List<Series> entries = new ArrayList<>(options.size() - 1);
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
			entries.add(series);
		}
		log.debug("Read QuickJump content: {} entries", entries.size());
		return ImmutableList.copyOf(entries);
	}

	private WbbBoard readSubtitleRepositoryBoard() throws SQLException
	{
		log.debug("Reading subtitle repository board");
		int subRepoBoardId = config.getEnvironmentSettings().getInt("sc.subrepo.boardid");
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			WbbBoard board = boardApi.getBoard(subRepoBoardId);
			log.debug("Read subtitle repository board");
			return board;
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
	private List<Series> readSubtitleRepositoryQuickJumpContent(WbbBoard subRepoBoard)
	{
		log.debug("Reading subtitle repository QuickJump content");
		Document description = Jsoup.parse(subRepoBoard.getDescription(), Migration.SUBCENTRAL_HOST);
		Element quickJumpSelect = description.select("select#subablage-select").first();
		if (quickJumpSelect == null)
		{
			throw new IllegalStateException("Subtitle Repository Quickjump <select> element could not be found");
		}
		Elements options = quickJumpSelect.getElementsByTag("option");
		List<Series> entries = new ArrayList<>(options.size() - 1);
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
				log.warn("Subtitle repository QuickJump entry's link contains no threadID: " + option.outerHtml());
			}
			Series series = new Series(seriesName);
			series.addAttributeValue(Migration.SERIES_ATTR_SUB_REPO_THREAD_ID, threadId);
			entries.add(series);
		}
		log.debug("Read subtitle repository QuickJump content: {} entries", entries.size());
		return ImmutableList.copyOf(entries);
	}

	private List<WbbThread> readSubtitleRepositoryThreads(WbbBoard subRepoBoard) throws SQLException
	{
		log.debug("Reading subtitle repository threads");
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			List<WbbThread> threads = boardApi.getThreadsByBoard(subRepoBoard.getId());
			log.debug("Read {} subtitle repository threads", threads.size());
			return threads;
		}
	}

	private List<WbbThread> readSubsPrefixThreads() throws SQLException
	{
		log.debug("Reading [Subs] threads");
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard boardApi = new WoltlabBurningBoard();
			boardApi.setConnection(conn);
			List<WbbThread> threads = boardApi.getThreadsByPrefix("[Subs]");
			log.debug("Read {} [Subs] threads", threads.size());
			return threads;
		}
	}

	private List<WbbThread> readStickyThreads(int boardId)
	{
		log.trace("Reading sticky threads of board {}", boardId);
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			List<WbbThread> threads = scBoard.getStickyThreads(boardId);
			log.trace("Read {} sticky threads of board {}", threads.size(), boardId);
			return threads;
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	private List<WcfAttachment> readSubRepoAttachments() throws SQLException
	{
		log.trace("Reading subtitle repository attachments");
		int subRepoBoardId = config.getEnvironmentSettings().getInt("sc.subrepo.boardid");
		try (Connection conn = config.getDataSource().getConnection())
		{
			WoltlabBurningBoard scBoard = new WoltlabBurningBoard();
			scBoard.setConnection(conn);
			List<WcfAttachment> atts = scBoard.getAttachmentsByBoard(subRepoBoardId);
			log.trace("Read {} sticky threads of subtitle repository (boardID={})", atts.size(), subRepoBoardId);
			return atts;
		}
	}

	/*
	 * Perform consistency checks
	 */
	private void checkSeriesListAgainstQuickJump(SeriesListData seriesListContent, List<Series> quickJumpSeries) throws IOException
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

	private void checkSeriesListAgainstBoards(SeriesListData seriesListContent) throws IOException, SQLException
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

	private void checkSeriesListAgainstSubRepo(SeriesListData seriesListContent, List<WbbThread> subRepoThreads) throws IOException
	{
		ListMatchResult<Series, WbbThread> seriesListToSubRepoThreads = matchLists(seriesListContent.getSeries(),
				subRepoThreads,
				(Series series, WbbThread repoThread) -> series.getName().equals(repoThread.getTopic()));

		appendChapter("SeriesList <-> Subtitle repository",
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
		appendChapter("Subtitle repository: Threads <-> QuickJump", "Threads that have no entry in QuickJump", unmatchedThreadsEntries);

		List<String> unmatchedQjEntriesEntries = threadToQjEntries.onlyInSecondList.stream().map((Series s) -> formatSeries(s)).collect(Collectors.toList());
		appendChapter("QuickJump entries that don't link to a repository thread", unmatchedQjEntriesEntries);

		List<String> inconsistentTitles = threadToQjEntries.matchingElems.entrySet()
				.stream()
				.filter((Map.Entry<WbbThread, Series> entry) -> !entry.getKey().getTopic().equals(entry.getValue().getName()))
				.map((Map.Entry<WbbThread, Series> entry) -> formatThread(entry.getKey()) + " != " + formatSeries(entry.getValue()))
				.collect(Collectors.toList());
		appendChapter("Thread topic != QuickJump entry", inconsistentTitles);
	}

	private void checkSeriesListAgainstSubsPrefixThreads(List<WbbPost> seasonPosts, List<WbbThread> subsThreads) throws IOException
	{
		ListMatchResult<WbbPost, WbbThread> seasonPostsToSubsThreads = matchPostsToThreads(seasonPosts, subsThreads);

		appendChapter("SeriesList <-> [Subs] Threads",
				"Season threads without the [Subs] prefix",
				seasonPostsToSubsThreads.onlyInFirstList.stream().map(ConsistencyChecker::formatPostAsThread).collect(Collectors.toList()));

		appendChapter("Threads with the [Subs] prefix that are not in in the SeriesList",
				seasonPostsToSubsThreads.onlyInSecondList.stream().map(ConsistencyChecker::formatThread).collect(Collectors.toList()));
	}

	private void checkSeriesListAgainstStickyThreads(SeriesListData seriesListContent, List<WbbPost> seriesListSeasonPosts) throws SQLException, IOException
	{
		// Read all sticky threads
		List<WbbThread> allStickyThreads = seriesListContent.getSeries().parallelStream().map((Series series) ->
		{
			int boardId = series.getAttributeValue(Migration.SERIES_ATTR_BOARD_ID);
			List<WbbThread> boardStickyThreads = readStickyThreads(boardId);
			return boardStickyThreads;
		}).flatMap((List<WbbThread> list) -> list.stream()).collect(Collectors.toList());

		ListMatchResult<WbbPost, WbbThread> seasonsToStickyThreads = matchPostsToThreads(seriesListSeasonPosts, allStickyThreads);
		appendChapter("SeriesList seasons <-> Sticky threads",
				"Season threads that are not sticky",
				seasonsToStickyThreads.onlyInFirstList.stream().map(ConsistencyChecker::formatPostAsThread).collect(Collectors.toList()));
		appendChapter("Sticky threads that are no season threads", seasonsToStickyThreads.onlyInSecondList.stream().map(ConsistencyChecker::formatThread).collect(Collectors.toList()));
	}

	private void checkSeriesListAgainstSeasonPostsAgainstSubRepo(ListMultimap<WbbPost, Season> postsToSeasons, List<WcfAttachment> subRepoAttachments) throws IOException
	{
		Map<Integer, WcfAttachment> attachments = subRepoAttachments.stream().collect(Collectors.toMap((WcfAttachment att) -> att.getId(), (WcfAttachment att) -> att));
		List<String> topicEntries = new ArrayList<>();
		List<String> subsNotInRepoEntries = new ArrayList<>();
		for (WbbPost post : postsToSeasons.keySet())
		{
			List<Season> seriesListSeasons = postsToSeasons.get(post);
			// Parse season post
			SeasonPostData parsedTopic = new SeasonPostParser().parseTopic(post.getTopic());
			SeasonPostData parsedPost = new SeasonPostParser().parse(post);
			// Compare seasons only of topic
			if (!seriesListSeasons.equals(parsedTopic.getSeasons()))
			{
				topicEntries.add(joinSeasons(seriesListSeasons) + " != " + joinSeasons(parsedPost.getSeasons()) + " (post: " + formatPost(post) + ")");
			}
			// Compare attachments
			List<SubtitleRelease> subsNotInSubRepo = new ArrayList<>();
			for (SubtitleRelease subFile : parsedPost.getSubtitleFiles())
			{
				Integer attId = subFile.getAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID);
				if (attachments.containsKey(attId))
				{
					// set to null to signal that a link to this attachment is contained in a season post
					attachments.put(attId, null);
				}
				else
				{
					subsNotInSubRepo.add(subFile);
				}
			}
			if (!subsNotInSubRepo.isEmpty())
			{
				subsNotInRepoEntries.add(formatPost(post));
				for (SubtitleRelease subFile : subsNotInSubRepo)
				{
					subsNotInRepoEntries.add("- " + formatSubtitleFile(subFile));
				}
			}
		}
		List<String> subsNotInSeasonPost = attachments.values()
				.stream()
				.filter(Objects::nonNull)
				.map(ConsistencyChecker::formatAttachment)
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());

		appendChapter("SeriesList <-> Season posts", "Seasons declared in SeriesList != Seasons declared in post topic", topicEntries);

		appendChapter("Season posts <-> Subtitle repository", "Subtitles from season posts that are not attached in the subtitle repository", subsNotInRepoEntries);
		appendChapter("Subtitles from subtitle repository that are not linked in any season post", subsNotInSeasonPost);
	}

	/*
	 * Matching
	 */
	private static ListMatchResult<WbbPost, WbbThread> matchPostsToThreads(List<WbbPost> postList, List<WbbThread> threadList)
	{
		return matchLists(postList, threadList, (WbbPost post, WbbThread thread) ->
		{
			return post.getThreadId() == thread.getId();
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

	private static String joinSeasons(Iterable<Season> seasons)
	{
		StringJoiner joiner = new StringJoiner(", ");
		boolean first = true;
		for (Season season : seasons)
		{
			if (first)
			{
				joiner.add(NamingDefaults.getDefaultSeasonNamer().name(season, ImmutableMap.of(SeasonNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE)));
				first = false;
			}
			else
			{
				joiner.add(NamingDefaults.getDefaultSeasonNamer().name(season, ImmutableMap.of(SeasonNamer.PARAM_ALWAYS_INCLUDE_TITLE, Boolean.TRUE, SeasonNamer.PARAM_INCLUDE_SERIES, Boolean.FALSE)));
			}
		}
		return joiner.toString();
	}

	private static String formatSubtitleFile(SubtitleRelease subFile)
	{
		return NamingDefaults.getDefaultNamingService().name(subFile.getSubtitles()) + " "
				+ NamingDefaults.getDefaultNamingService().name(subFile.getMatchingReleases(), ImmutableMap.of(ReleaseNamer.PARAM_PREFER_NAME, Boolean.TRUE)) + " (attachmentID="
				+ subFile.getAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID) + ")";
	}

	private static String formatBoard(WbbBoard board)
	{
		return "\"" + board.getTitle() + "\" (boardID=" + board.getId() + ")";
	}

	private static String formatThread(WbbThread thread)
	{
		return "\"" + thread.getTopic() + "\" (threadID=" + thread.getId() + ")";
	}

	private static String formatPost(WbbPost post)
	{
		return "\"" + post.getTopic() + "\" (postID=" + post.getId() + ")";
	}

	private static String formatPostAsThread(WbbPost post)
	{
		return "\"" + post.getTopic() + "\" (threadId=" + post.getThreadId() + ")";
	}

	private static String formatAttachment(WcfAttachment attachment)
	{
		return "\"" + attachment.getName() + "\" (attachmentId=" + attachment.getId() + ")";
	}
}
