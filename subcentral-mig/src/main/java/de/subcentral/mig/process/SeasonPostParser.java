package de.subcentral.mig.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleFile;
import de.subcentral.mig.Migration;
import de.subcentral.support.subcentralde.SubCentralApi;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class SeasonPostParser
{
	private static final Logger log = LogManager.getLogger(SeasonPostParser.class);

	private static enum ColumnType
	{
		UNKNOWN, EPISODE, GERMAN_SUBS, ENGLISH_SUBS, TRANSLATION, REVISION, TIMINGS, ADJUSTMENT, SOURCE
	};

	private static final Splitter					SPLITTER_DIVISON							= Splitter.on(CharMatcher.anyOf("|/")).trimResults();
	private static final Splitter					SPLITTER_LIST								= Splitter.on(CharMatcher.anyOf(",&")).trimResults();

	private static final Pattern					PATTERN_BBCODE								= Pattern.compile("\\[(\\w+).*?\\](.*?)\\[/\\1\\]");

	/**
	 * Numbered season:
	 * 
	 * <pre>
	 * Mr. Robot - Staffel 1 - [DE-Subs: 10 | VO-Subs: 10] - [Komplett] - [+ Deleted Scenes]
	 * </pre>
	 */
	private static final Pattern					PATTERN_POST_TOPIC_NUMBERED_SEASON			= Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+)\\s+-\\s*.*");

	/**
	 * Numbered season with title:
	 * 
	 * <pre>
	 * American Horror Story - Staffel 1: Horror House - [DE-Subs: 12 | VO-Subs: 12] - [Komplett]
	 * </pre>
	 */
	private static final Pattern					PATTERN_POST_TOPIC_NUMBERED_TITLED_SEASON	= Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+):\\s+([^-]+)\\s+-\\s*.*");

	/**
	 * Miniseries:
	 * 
	 * <pre>
	 * Band Of Brothers - Miniserie - [DE-Subs: 10 | VO-Subs: 10] - [Komplett]
	 * </pre>
	 */
	private static final Pattern					PATTERN_POST_TOPIC_MINI_SERIES				= Pattern.compile("(.*?)\\s+-\\s+Miniserie\\s+-\\s*.*");

	/**
	 * Special season:
	 * 
	 * <pre>
	 * Doctor Who - Klassische Folgen - [DE-Subs: 111 | VO-Subs: 160 | Aired: 160] - [+Specials]
	 * Psych - Webisodes - [DE-Subs: 06 | VO-Subs: 06] - [Komplett]
	 * </pre>
	 */
	private static final Pattern					PATTERN_POST_TOPIC_SPECIAL_SEASON			= Pattern.compile("(.*?)\\s+-\\s+([^-]+)\\s+-\\s*.*");

	/**
	 * Multiple seasons:
	 * 
	 * <pre>
	 * Buffy the Vampire Slayer - Staffel 1 bis Staffel 7 - Komplett
	 * </pre>
	 */
	private static final Pattern					PATTERN_POST_TOPIC_MULTIPLE_SEASONS			= Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+)\\s+bis\\s+Staffel\\s+(\\d+)\\s+-\\s*.*");

	private static final Map<Pattern, ColumnType>	COLUMN_TYPE_PATTERNS						= createColumnTypePatternMap();

	/**
	 * <ul>
	 * <li>E23-E24 - "Alternate Cut"</li>
	 * <li>E15-E16 - "Psych - The Musical"</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_MULTI						= Pattern.compile("(?:S(\\d+))?E(\\d+)-E(\\d+)\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

	/**
	 * <ul>
	 * <li>E01 - "Pilot"</li>
	 * <li>S04E03 - The Power of the Daleks (Verschollen)</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_REGULAR						= Pattern.compile("(?:S(\\d+))?E(\\d+)\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

	/**
	 * <ul>
	 * <li>Special - "Wicked is Coming"</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_SPECIAL						= Pattern.compile("Special\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

	/**
	 * <ul>
	 * <li>S04E03</li>
	 * <li>E01:</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_ONLY_NUM					= Pattern.compile("(?:S(\\d+))?E(\\d+)");

	/**
	 * <pre>
	 * 		<a href="http://www.subcentral.de/index.php?page=Attachment&attachmentID=46749">POW4</a>
	 * </pre>
	 */
	private static final Pattern					PATTERN_ATTACHMENT_ANCHOR					= Pattern.compile("<a.*?page=Attachment.*?attachmentID=(\\d+).*?>(.*?)</a>");
	/**
	 * <pre>
	 * 	[url='http://www.subcentral.de/index.php?page=Attachment&attachmentID=65018']IMMERSE[/url]
	 * </pre>
	 */
	private static final Pattern					PATTERN_ATTACHMENT_BBCODE					= Pattern.compile("\\[url=.*?page=Attachment.*?attachmentID=(\\d+).*?\\](.*?)\\[/url\\]");
	private static final Pattern					PATTERN_ATTACHMENT_ANCHOR_MARKED			= Pattern.compile("([*])?<a.*?page=Attachment.*?attachmentID=(\\d+).*?>([*])?(.*?)([*])?</a>([*])?");
	private static final Pattern					PATTERN_ATTACHMENT_BBCODE_MARKED			= Pattern
			.compile("([*])?\\[url=.*?page=Attachment.*?attachmentID=(\\d+).*?\\]([*])?(.*?)([*])?\\[/url\\]([*])?");

	private static Map<Pattern, ColumnType> createColumnTypePatternMap()
	{
		ImmutableMap.Builder<Pattern, ColumnType> map = ImmutableMap.builder();
		map.put(Pattern.compile("Episode"), ColumnType.EPISODE);
		map.put(Pattern.compile(".*?flags/de\\.png.*"), ColumnType.GERMAN_SUBS);
		map.put(Pattern.compile("Deutsch"), ColumnType.GERMAN_SUBS);
		map.put(Pattern.compile(".*?flags/(usa|uk|ca|aus)\\.png.*"), ColumnType.ENGLISH_SUBS);
		map.put(Pattern.compile("(Englisch|VO)"), ColumnType.ENGLISH_SUBS);
		map.put(Pattern.compile("Übersetzung"), ColumnType.TRANSLATION);
		map.put(Pattern.compile("(Korrektur|Überarbeitung)"), ColumnType.REVISION);
		map.put(Pattern.compile("Timings"), ColumnType.TIMINGS);
		map.put(Pattern.compile("Anpassung"), ColumnType.ADJUSTMENT);
		map.put(Pattern.compile("Quelle"), ColumnType.SOURCE);
		return map.build();
	}

	public SeasonPostContent getAndParse(int threadId, SubCentralApi api) throws IOException
	{
		Document doc = api.getContent("index.php?page=Thread&threadID=" + threadId);
		return parse(doc);
	}

	public SeasonPostContent parse(Document threadHtml)
	{
		// Get title and content of first post
		Element postTitleDiv = threadHtml.getElementsByClass("messageTitle").first();
		Element postContentDiv = threadHtml.getElementsByClass("messageBody").first();
		if (postTitleDiv == null || postContentDiv == null)
		{
			throw new IllegalArgumentException("Invalid thread html: No post found");
		}
		Element postTextDiv = postContentDiv.child(0);

		return parse(postTitleDiv.text(), postTextDiv.html());
	}

	public SeasonPostContent parse(WbbPost post)
	{
		return parse(post.getTopic(), post.getMessage());
	}

	public SeasonPostContent parse(String postTopic, String postContent)
	{
		Data data = new Data();
		data.postTopic = postTopic;
		data.postContent = Jsoup.parse(postContent, Migration.SUBCENTRAL_HOST);

		// Title
		parsePostTopic(data);

		// Content
		parseSeasonHeader(data);
		parseDescription(data);
		parseSubtitleTable(data);

		cleanupData(data);

		return new SeasonPostContent(data.series, data.seasons.keySet(), data.subtitles);
	}

	/**
	 * Trying to parse the post topic with the {@code PATTERN_POST_TOPIC*} patterns.
	 */
	private static void parsePostTopic(Data data)
	{
		Matcher topicMatcher = PATTERN_POST_TOPIC_NUMBERED_SEASON.matcher(data.postTopic);
		if (topicMatcher.matches())
		{
			Series series = new Series(topicMatcher.group(1));
			series.setType(Series.TYPE_SEASONED);
			data.series = series;
			Season season = series.newSeason();
			Integer number = Integer.valueOf(topicMatcher.group(2));
			season.setNumber(number);
			data.seasons.put(season, season);
			return;
		}

		topicMatcher.reset();
		topicMatcher.usePattern(PATTERN_POST_TOPIC_NUMBERED_TITLED_SEASON);
		if (topicMatcher.matches())
		{
			Series series = new Series(topicMatcher.group(1));
			series.setType(Series.TYPE_SEASONED);
			data.series = series;
			Season season = series.newSeason();
			Integer number = Integer.valueOf(topicMatcher.group(2));
			String title = topicMatcher.group(3);
			season.setNumber(number);
			season.setTitle(title);
			data.seasons.put(season, season);
			return;
		}

		topicMatcher.reset();
		topicMatcher.usePattern(PATTERN_POST_TOPIC_MINI_SERIES);
		if (topicMatcher.matches())
		{
			Series series = new Series(topicMatcher.group(1));
			series.setType(Series.TYPE_MINI_SERIES);
			data.series = series;
			Season season = series.newSeason(1);
			data.seasons.put(season, season);
			return;
		}

		topicMatcher.reset();
		topicMatcher.usePattern(PATTERN_POST_TOPIC_SPECIAL_SEASON);
		if (topicMatcher.matches())
		{
			Series series = new Series(topicMatcher.group(1));
			data.series = series;
			Season season = series.newSeason();
			String title = topicMatcher.group(2).trim();
			season.setTitle(title);
			season.setSpecial(true);
			data.seasons.put(season, season);
			return;
		}

		topicMatcher.reset();
		topicMatcher.usePattern(PATTERN_POST_TOPIC_MULTIPLE_SEASONS);
		if (topicMatcher.matches())
		{
			Series series = new Series(topicMatcher.group(1));
			series.setType(Series.TYPE_SEASONED);
			data.series = series;
			int firstSeasonNum = Integer.parseInt(topicMatcher.group(2));
			int lastSeasonNum = Integer.parseInt(topicMatcher.group(3));
			for (int i = firstSeasonNum; i <= lastSeasonNum; i++)
			{
				Season season = series.newSeason(i);
				data.seasons.put(season, season);
			}
			return;
		}

		log.warn("Could not parse post topic \"" + data.postTopic + "\". No pattern matched");
	}

	/**
	 * <pre>
	 * {@code
	 * <div class="baseSC"> 
	 * 	<div class="tbild">
	 * 		<img src="bilder/header_mrrobot_s01.jpg" alt="Mr. Robot">
	 * 	</div> 
	 * </div>}
	 * </pre>
	 * 
	 * @param season
	 * @param postContent
	 */
	private static void parseSeasonHeader(Data data)
	{
		Element headerImg = data.postContent.select("div.tbild > img").first();
		if (headerImg != null)
		{
			String headerUrl = headerImg.absUrl("src");
			for (Season season : data.seasons.keySet())
			{
				season.getImages().put(Migration.SEASON_IMG_TYPE_HEADER, headerUrl);
			}
		}
	}

	/**
	 * <pre>
	 * {@code
	 * <div class="baseSC"> 
	 * 	<div class="ztext"> 
	 *		<div class="inhalt"> 
	 *			<p>» Bei „Mr. Robot“ wird Elliot, der tagsüber Programmierer für eine Internetsicherheitsfirma ist und nachts als Selbstjustizler-Hacker agiert, von einer anarchistischen Untergrundorganisation rekrutiert, um die Firma zu zerstören, für deren Schutz er bezahlt wird.<br> Elliot muss entscheiden, wie weit er gehen kann, um die Mächte zu entlarven, von denen er glaubt, dass sie die Welt regieren und zerstören.<br> Dabei steht er noch dem Problem gegenüber, dass er an einer antisozialen Persönlichkeitsstörung leidet und nur Verbindungen zu anderen Menschen aufbauen kann, indem er sie hackt. Er hackt sie aber nicht nur, um sich Informationen zu beschaffen oder zur Überwachung. Er nutzt seine Fähigkeiten, um die Menschen um ihn herum zu beschützen. «</p> 
	 *		</div> 
	 *		<!-- Hier Bilderserie (Bilder-Modul) einfügen. [Optinal] --> 
	 *		<div class="websites"> 
	 *			<p>Offizielle Website: <a href="http://www.usanetwork.com/mrrobot" target="_blank" class="usa network">USA Network</a> <br> Weitere Informationen: <a href="http://en.wikipedia.org/wiki/Mr._Robot_(TV_series)" target="_blank" class="wiki">Wikipedia (en)</a> und <a href="http://www.imdb.com/title/tt4158110/?ref_=nv_sr_1" target="_blank" class="imdb">IMDb</a></p> 
	 *		</div> 
	 *	</div> 
	 *  </div>
	 *  }
	 * </pre>
	 * 
	 * @param season
	 * @param postContent
	 */
	private static void parseDescription(Data data)
	{
		Elements descriptionDivs = data.postContent.select("div.inhalt, div.websites");
		StringJoiner joiner = new StringJoiner("\n");
		for (Element div : descriptionDivs)
		{
			joiner.add(div.html());
		}
		if (joiner.length() > 0)
		{
			String description = joiner.toString();
			for (Season season : data.seasons.keySet())
			{
				season.setDescription(description);
			}
		}
	}

	private static void parseSubtitleTable(Data data)
	{
		Elements tables = data.postContent.getElementsByTag("table");
		for (Element table : tables)
		{
			data.currentTableNum++;
			try
			{
				parseStandardTable(data, table);
			}
			catch (Exception e)
			{
				log.warn("Exception while trying to parse subtitle table as standard table. Parsing as non-standard table (post title: \"{}\", table number: {}, exception: {})",
						data.postTopic,
						data.currentTableNum,
						e.toString());
				parseNonStandardTable(data, table);
			}
		}
	}

	private static void parseStandardTable(Data data, Element table)
	{
		// Determine ColumnTypes
		Element thead = table.getElementsByTag("thead").first();
		if (thead == null)
		{
			throw new IllegalArgumentException("No thead element found");
		}
		Elements thElems = thead.getElementsByTag("th");
		if (thElems.isEmpty())
		{
			throw new IllegalArgumentException("No th elements found");
		}
		ColumnType[] columns = new ColumnType[thElems.size()];
		for (int i = 0; i < thElems.size(); i++)
		{
			Element th = thElems.get(i);
			ColumnType colType = determineColumnType(th.html());
			if (ColumnType.UNKNOWN.equals(colType))
			{
				throw new IllegalArgumentException("Column type could not be determined: " + th);
			}
			columns[i] = colType;
		}

		// Get rows and cells
		Element tbody = table.getElementsByTag("tbody").first();
		if (tbody == null)
		{
			throw new IllegalArgumentException("No tbody element found");
		}
		List<List<Element>> rows = new ArrayList<>();
		for (Element tr : tbody.getElementsByTag("tr"))
		{
			List<Element> tdElems = new ArrayList<>(tr.getElementsByTag("td"));
			rows.add(tdElems);
		}

		cleanupTable(data, rows, columns.length);

		for (List<Element> tdElems : rows)
		{
			parseStandardTableRow(data, tdElems, columns);
		}
	}

	private static ColumnType determineColumnType(String columnTitle)
	{
		for (Map.Entry<Pattern, ColumnType> entries : COLUMN_TYPE_PATTERNS.entrySet())
		{
			if (entries.getKey().matcher(columnTitle).matches())
			{
				return entries.getValue();
			}
		}
		return ColumnType.UNKNOWN;
	}

	private static void cleanupTable(Data data, List<List<Element>> rows, int numColumns)
	{
		// Stores for each columnIndex the current Element which should span several rows
		Element[] rowSpanCells = new Element[numColumns];
		// Stores for each columnIndex the remaining rows which the Element should span
		int[] rowspanRemainingRows = new int[numColumns];

		ListIterator<List<Element>> rowIter = rows.listIterator();
		while (rowIter.hasNext())
		{
			List<Element> row = rowIter.next();
			if (row.isEmpty())
			{
				log.warn("Skipping empty row (post title: \"{}\", table number: {})", data.postTopic, data.currentTableNum);
				continue;
			}

			// Cleanup rowspan
			for (int i = 0; i < numColumns; i++)
			{
				Element rowSpanCell = rowSpanCells[i];
				if (rowSpanCell != null)
				{
					row.add(i, rowSpanCell);
					int remainingRows = rowspanRemainingRows[i] - 1;
					rowspanRemainingRows[i] = remainingRows;
					if (remainingRows == 0)
					{
						rowSpanCells[i] = null;
					}
				}
			}

			ListIterator<Element> cellIter = row.listIterator();
			int colIndex = 0;
			while (cellIter.hasNext())
			{
				Element cell = cellIter.next();

				// Check for rowspan
				int rowspan = 1;
				String rowspanAttr = cell.attr("rowspan");
				if (!rowspanAttr.isEmpty())
				{
					rowspan = Integer.parseInt(rowspanAttr);
					cell.attr("rowspan", "1");
				}
				if (rowspan > 1)
				{
					rowSpanCells[colIndex] = cell.clone();
					rowspanRemainingRows[colIndex] = rowspan - 1;
				}

				// Check for colspan
				int colspan = 1;
				String colspanAttr = cell.attr("colspan");
				if (!colspanAttr.isEmpty())
				{
					colspan = Integer.parseInt(colspanAttr);
					cell.attr("colspan", "1");
				}

				// Cleanup colspan
				for (int i = 0; i < colspan - 1; i++)
				{
					cellIter.add(cell);
				}

				colIndex++;
			}

			if (row.size() < numColumns)
			{
				log.warn("Row does not have the expected number of columns (expected: {}, actual: {}; post title: \"{}\", table number: {}, columns: {})",
						numColumns,
						row.size(),
						data.postTopic,
						data.currentTableNum,
						row);
			}
		}
	}

	private static void parseStandardTableRow(Data data, List<Element> tdElems, ColumnType[] columns)
	{
		List<Episode> parsedEpis = new ArrayList<>();
		// Each top-level list represents a column,
		// each 2nd-level list represents the subtitles in that column
		List<List<MarkedValue<SubtitleFile>>> parsedSubs = new ArrayList<>();
		// Each top-level list represents a column,
		// each 2nd-level list represents a division within that column (the divisions are divided by "|")
		// each 3rd-level list represents the contributions inside a division
		List<List<List<Contribution>>> parsedContributions = new ArrayList<>();
		// Each top-level list represents a division
		List<MarkedValue<String>> parsedSources = new ArrayList<>();

		for (int i = 0; i < tdElems.size(); i++)
		{
			Element td = tdElems.get(i);
			ColumnType colType = columns[i];
			switch (colType)
			{
				case EPISODE:
					parseEpisodeCell(parsedEpis, data, td);
					break;
				case GERMAN_SUBS:
					// fall through
				case ENGLISH_SUBS:
					parseSubtitlesCell(parsedSubs, td, colType);
					break;
				case TRANSLATION:
					// fall through
				case REVISION:
					// fall through
				case TIMINGS:
					// fall through
				case ADJUSTMENT:
					parseContributionsCell(parsedContributions, td, colType);
					break;
				case SOURCE:
					parseSourcesCell(parsedSources, td);
					break;
				default:
					// Cannot happen
					throw new IllegalArgumentException("Unknown column type: " + colType);
			}
		}

		// Add episode (if not added yet)
		ListIterator<Episode> parsedEpisIter = parsedEpis.listIterator();
		while (parsedEpisIter.hasNext())
		{
			Episode parsedEpi = parsedEpisIter.next();
			Episode storedEpi = data.episodes.putIfAbsent(parsedEpi, parsedEpi);
			if (storedEpi != null)
			{
				parsedEpisIter.set(storedEpi);
			}
		}

		// Add contributions to subtitles
		int numSubColumns = parsedSubs.size();
		int numSubs = parsedSubs.stream().mapToInt((List<MarkedValue<SubtitleFile>> subsPerColumn) -> subsPerColumn.size()).sum();
		// For each contribution column
		for (List<List<Contribution>> columnContributions : parsedContributions)
		{
			int numContributionDivisions = columnContributions.size();
			// If number of contributionDivisions = 1
			// -> map all contributions in that division to all subtitles
			// [LOL][DIM] <-> [SubberA]
			// [LOL][DIM] <-> [SubberA, SubberB]
			if (numContributionDivisions == 1)
			{
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> subAdj : columnSubs)
					{
						addContributions(subAdj.value, columnContributions.get(0));
					}
				}
			}
			// If number of subColumns == number of contributionDivisions
			// -> map each contributionDivision to a sub in a column
			// [LOL][DIM] <-> [SubberA | SubberB]
			else if (numSubColumns == numContributionDivisions)
			{
				for (int i = 0; i < numSubColumns; i++)
				{
					List<MarkedValue<SubtitleFile>> columnSubs = parsedSubs.get(i);
					List<Contribution> divisionContributions = columnContributions.get(i);
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						addContributions(markedSubAdj.value, divisionContributions);
					}
				}
			}
			// If number of subs == number of contributionDivisions
			// -> map each contributionDivision to a sub
			// [DIM | WEB-DL] <-> [SubberA | SubberB]
			// [2HD] [PublicHD | DON] <-> [Grizzly | Eric | NegroManus]
			else if (numSubs == columnContributions.size())
			{
				int index = 0;
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						List<Contribution> divisionContributions = columnContributions.get(index);
						addContributions(markedSubAdj.value, divisionContributions);
						index++;
					}
				}
			}
			// If more subColumns than contributionDivisions
			// -> Add the first contributionDivision to the subColumns from index 0 to n,
			// where n is = numSubColumns - numContributionDivisions
			// [720p][1080p][WEB-DL] <-> [SubberA | SubberB]
			else if (numSubColumns > numContributionDivisions && numContributionDivisions > 1)
			{
				int diff = numSubColumns - numContributionDivisions;
				for (int i = 0; i < numSubColumns; i++)
				{
					List<MarkedValue<SubtitleFile>> columnSubs = parsedSubs.get(i);
					int contributionDivisionIndex = Math.max(0, i - diff);
					List<Contribution> divisionContributions = columnContributions.get(contributionDivisionIndex);
					for (MarkedValue<SubtitleFile> subAdj : columnSubs)
					{
						addContributions(subAdj.value, divisionContributions);
					}
				}
			}
			// Else: Add every contribution to every sub
			else
			{
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						for (List<Contribution> divisionContributions : columnContributions)
						{
							addContributions(markedSubAdj.value, divisionContributions);
						}
					}
				}
			}
		}

		// Add sources to subtitles
		if (!parsedSources.isEmpty())
		{
			boolean someSourcesMarked = parsedSources.stream().mapToInt((MarkedValue<String> source) -> source.marker == null ? 0 : 1).sum() > 0;
			// If any markers
			if (someSourcesMarked)
			{
				// Map<Marker->List<Sources>)
				Map<String, String> mapMarkerToSource = new HashMap<>(2);
				for (MarkedValue<String> source : parsedSources)
				{
					String storedSource = mapMarkerToSource.put(source.marker, source.value);
					if (storedSource != null)
					{
						throw new IllegalArgumentException("Multiple sources marked with marker " + source.marker + ": " + storedSource + ", " + source);
					}
				}
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> subAdj : columnSubs)
					{
						addSource(subAdj.value, mapMarkerToSource.get(subAdj.marker));
					}
				}
			}
			// If only one source, add it to all the subtitles
			else if (parsedSources.size() == 1)
			{
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						addSource(markedSubAdj.value, parsedSources.get(0).value);
					}
				}
			}
			// If number of sub columns == number of sources
			// -> add each source to all subs in a column
			// [DIM][WEB-DL] <-> [Addic7ed.com | SubCentral.de]
			else if (numSubColumns == parsedSources.size())
			{
				for (int i = 0; i < numSubColumns; i++)
				{
					List<MarkedValue<SubtitleFile>> columnSubs = parsedSubs.get(i);
					MarkedValue<String> markedSource = parsedSources.get(i);
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						addSource(markedSubAdj.value, markedSource.value);
					}
				}
			}
			// If number of subs == number of contributionDivisions
			// -> map each source to a sub
			// [DIM | WEB-DL] <-> [Addic7ed.com | SubCentral.de]
			else if (numSubs == parsedSources.size())
			{
				int index = 0;
				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						MarkedValue<String> markedSource = parsedSources.get(index);
						addSource(markedSubAdj.value, markedSource.value);
						index++;
					}
				}
			}
			// Else: Concat the sources and add the combined source to every sub
			else
			{
				StringJoiner combinedSourceJoiner = new StringJoiner(" & ");
				for (MarkedValue<String> markedSource : parsedSources)
				{
					combinedSourceJoiner.add(markedSource.value);
				}
				String combinedSource = combinedSourceJoiner.toString();

				for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleFile> markedSubAdj : columnSubs)
					{
						addSource(markedSubAdj.value, combinedSource);
					}
				}
			}
		}

		// Add subtitles
		for (List<MarkedValue<SubtitleFile>> columnSubs : parsedSubs)
		{
			for (MarkedValue<SubtitleFile> subAdj : columnSubs)
			{
				Episode firstEpi = parsedEpis.get(0);
				subAdj.value.getFirstSubtitle().setMedia(firstEpi);
				data.subtitles.add(subAdj.value);

				// In case of a multiple episodes in that row, for each more episode,
				// add a clone of the parsed subtitle with that episode
				if (parsedEpis.size() > 1)
				{
					for (int i = 1; i < parsedEpis.size(); i++)
					{
						SubtitleFile copy = SerializationUtils.clone(subAdj.value);
						Episode epi = parsedEpis.get(i);
						copy.getFirstSubtitle().setMedia(epi);
						data.subtitles.add(copy);
					}
				}
			}
		}
	}

	private static void parseEpisodeCell(List<Episode> epis, Data data, Element td)
	{
		String text = removeBBCodes(td.text());
		Matcher epiMatcher = PATTERN_EPISODE_MULTI.matcher(text);
		if (epiMatcher.matches())
		{
			Season season = null;
			if (epiMatcher.group(1) != null)
			{
				Integer seasonNumber = Integer.valueOf(epiMatcher.group(1));
				season = new Season(data.series, seasonNumber);
			}
			int firstEpiNum = Integer.parseInt(epiMatcher.group(2));
			int lastEpiNum = Integer.parseInt(epiMatcher.group(3));
			String title = epiMatcher.group(4);
			for (int i = firstEpiNum; i <= lastEpiNum; i++)
			{
				epis.add(new Episode(data.series, season, i, title));
			}
			return;
		}

		epiMatcher.reset();
		epiMatcher.usePattern(PATTERN_EPISODE_REGULAR);
		if (epiMatcher.matches())
		{
			Season season = null;
			if (epiMatcher.group(1) != null)
			{
				Integer seasonNumber = Integer.valueOf(epiMatcher.group(1));
				season = new Season(data.series, seasonNumber);
			}
			Integer numberInSeason = Integer.valueOf(epiMatcher.group(2));
			String title = epiMatcher.group(3);
			epis.add(new Episode(data.series, season, numberInSeason, title));
			return;
		}

		epiMatcher.reset();
		epiMatcher.usePattern(PATTERN_EPISODE_SPECIAL);
		if (epiMatcher.matches())
		{
			String title = epiMatcher.group(1);
			Episode epi = new Episode(data.series, title);
			epi.setSpecial(true);
			epis.add(epi);
			return;
		}

		epiMatcher.reset();
		epiMatcher.usePattern(PATTERN_EPISODE_ONLY_NUM);
		if (epiMatcher.matches())
		{
			Season season = null;
			if (epiMatcher.group(1) != null)
			{
				Integer seasonNumber = Integer.valueOf(epiMatcher.group(1));
				season = new Season(data.series, seasonNumber);
			}
			Integer numberInSeason = Integer.valueOf(epiMatcher.group(2));
			epis.add(new Episode(data.series, season, numberInSeason));
			return;
		}
		String title = text;
		Episode epi = new Episode(data.series, title);
		epi.setSpecial(true);
		epis.add(epi);
		return;
	}

	private static void parseSubtitlesCell(List<List<MarkedValue<SubtitleFile>>> subs, Element td, ColumnType colType)
	{
		String language;
		switch (colType)
		{
			case GERMAN_SUBS:
				language = Migration.SUBTITLE_LANGUAGE_GERMAN;
				break;
			case ENGLISH_SUBS:
				language = Migration.SUBTITLE_LANGUAGE_ENGLISH;
				break;
			default:
				language = null;
		}

		String cellContent = td.html();
		List<MarkedValue<SubtitleFile>> subAdjs = new ArrayList<>();

		Matcher subLinkMatcher = PATTERN_ATTACHMENT_ANCHOR_MARKED.matcher(cellContent);
		while (subLinkMatcher.find())
		{
			handleMarkedSubtitleLinkMatch(subLinkMatcher, language, subAdjs);
		}
		subLinkMatcher.usePattern(PATTERN_ATTACHMENT_BBCODE_MARKED);
		subLinkMatcher.reset();
		while (subLinkMatcher.find())
		{
			handleMarkedSubtitleLinkMatch(subLinkMatcher, language, subAdjs);
		}

		// Only add the subColumn to the subColumn list if it is not empty
		// because empty columns are not respected in the contributionDivisions
		// F.e. [DIM][-][WEB-DL] <-> [- | Grollbringer] means that Grollbringer adjusted the WEB-DL,
		// not the non-existing sub in the empty column
		if (!subAdjs.isEmpty())
		{
			subs.add(subAdjs);
		}
	}

	private static void handleMarkedSubtitleLinkMatch(Matcher matcher, String language, List<MarkedValue<SubtitleFile>> subFiles)
	{
		String marker;
		if (matcher.group(1) != null)
		{
			marker = matcher.group(1);
		}
		else if (matcher.group(3) != null)
		{
			marker = matcher.group(3);
		}
		else if (matcher.group(5) != null)
		{
			marker = matcher.group(5);
		}
		else if (matcher.group(6) != null)
		{
			marker = matcher.group(6);
		}
		else
		{
			marker = null;
		}
		Integer attachmentId = Integer.valueOf(matcher.group(2));
		String label = removeHtmlTagsAndBBCodes(matcher.group(4));

		Subtitle sub = new Subtitle();
		sub.setLanguage(language);

		Release rls = new Release(label);

		SubtitleFile subAdj = new SubtitleFile();
		subAdj.setSingleSubtitle(sub);
		subAdj.setSingleMatchingRelease(rls);
		subAdj.getAttributes().put(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID, attachmentId);

		MarkedValue<SubtitleFile> markedSubAdj = new MarkedValue<>(subAdj, marker);
		subFiles.add(markedSubAdj);
	}

	/**
	 * <pre>
	 * - | Jesuxxx
	 * </pre>
	 * 
	 * <pre>
	 * Grollbringer | Negro & Sogge377
	 * </pre>
	 */
	private static void parseContributionsCell(List<List<List<Contribution>>> contributions, Element td, ColumnType colType)
	{
		String contributionType;
		switch (colType)
		{
			case TRANSLATION:
				contributionType = Subtitle.CONTRIBUTION_TYPE_TRANSLATION;
				break;
			case REVISION:
				contributionType = Subtitle.CONTRIBUTION_TYPE_REVISION;
				break;
			case TIMINGS:
				contributionType = Subtitle.CONTRIBUTION_TYPE_TIMINGS;
				break;
			case ADJUSTMENT:
				contributionType = SubtitleFile.CONTRIBUTION_TYPE_ADJUSTMENT;
				break;
			default:
				contributionType = null;
		}
		List<List<Contribution>> contributionsForColumn = new ArrayList<>();
		String text = removeBBCodes(td.text());
		for (String division : SPLITTER_DIVISON.split(text))
		{
			if (division.isEmpty() || division.equals("-"))
			{
				contributionsForColumn.add(ImmutableList.of());
			}
			else
			{
				List<Contribution> divContributions = new ArrayList<>();
				for (String contributorName : SPLITTER_LIST.split(division))
				{
					Subber contributor = new Subber();
					contributor.setName(contributorName);
					divContributions.add(new Contribution(contributor, contributionType));
				}
				contributionsForColumn.add(divContributions);
			}
		}
		contributions.add(contributionsForColumn);
	}

	private static void parseSourcesCell(List<MarkedValue<String>> sources, Element td)
	{
		String text = removeBBCodes(td.text());
		Iterable<String> divisions = SPLITTER_DIVISON.split(text);
		for (String division : divisions)
		{
			String marker = null;
			String source = division;
			if (division.startsWith("*"))
			{
				int endOfMarker = division.lastIndexOf('*') + 1;
				marker = division.substring(0, endOfMarker);
				source = division.substring(endOfMarker);
			}
			else if (division.endsWith("*"))
			{
				int startOfMarker = division.indexOf('*');
				marker = division.substring(startOfMarker);
				source = division.substring(0, startOfMarker);
			}
			sources.add(new MarkedValue<String>(source, marker));
		}
	}

	private static void addSource(SubtitleFile subAdj, String source)
	{
		subAdj.getFirstSubtitle().setSource(source);
	}

	private static void addContributions(SubtitleFile subAdj, List<Contribution> contributions)
	{
		for (Contribution c : contributions)
		{
			if (Subtitle.CONTRIBUTION_TYPE_TRANSCRIPT.equals(c.getType()) || Subtitle.CONTRIBUTION_TYPE_TIMINGS.equals(c.getType()) || Subtitle.CONTRIBUTION_TYPE_TRANSLATION.equals(c.getType())
					|| Subtitle.CONTRIBUTION_TYPE_REVISION.equals(c.getType()))
			{
				subAdj.getFirstSubtitle().getContributions().add(c);
			}
			else
			{
				subAdj.getContributions().add(c);
			}
		}
	}

	private static void parseNonStandardTable(Data data, Element table)
	{
		// Just add all attachment links as subtitles
		String content = table.html();
		Matcher subLinkMatcher = PATTERN_ATTACHMENT_ANCHOR.matcher(content);
		while (subLinkMatcher.find())
		{
			handleSubtitleLinkMatch(subLinkMatcher, data);
		}
		subLinkMatcher.usePattern(PATTERN_ATTACHMENT_BBCODE);
		subLinkMatcher.reset();
		while (subLinkMatcher.find())
		{
			handleSubtitleLinkMatch(subLinkMatcher, data);
		}
	}

	private static void handleSubtitleLinkMatch(Matcher subLinkMatcher, Data data)
	{
		Integer attachmentId = Integer.valueOf(subLinkMatcher.group(1));
		String label = removeHtmlTagsAndBBCodes(subLinkMatcher.group(2));
		SubtitleFile subAdj = new SubtitleFile();
		subAdj.setSingleMatchingRelease(new Release(label));
		subAdj.getAttributes().put(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID, attachmentId);
		data.subtitles.add(subAdj);
	}

	private static void cleanupData(Data data)
	{
		// Add episodes to season
		// For single-season threads
		if (data.seasons.size() == 1)
		{
			Season season = data.seasons.keySet().iterator().next();
			data.episodes.keySet().stream().forEach((Episode epi) -> season.addEpisode(epi));
		}
		// For multi-season threads
		else
		{
			for (Episode epi : data.episodes.keySet())
			{
				if (epi.isPartOfSeason())
				{
					boolean foundSeason = false;
					for (Season season : data.seasons.keySet())
					{
						if (season.getNumber().equals(epi.getSeason().getNumber()))
						{
							season.addEpisode(epi);
							foundSeason = true;
						}
					}
					if (!foundSeason)
					{
						Season newSeason = epi.getSeason();
						newSeason.addEpisode(epi);
						data.seasons.put(newSeason, newSeason);
					}
				}
				else
				{
					Season newUnknownSeason = new Season(data.series, Migration.UNKNOWN_SEASON);
					Season alreadyStoredSeason = data.seasons.putIfAbsent(newUnknownSeason, newUnknownSeason);
					if (alreadyStoredSeason != null)
					{
						alreadyStoredSeason.addEpisode(epi);
					}
					else
					{
						newUnknownSeason.addEpisode(epi);
					}
				}
			}
		}

		// For SubtitleAdjustments that reference the same Subtitle (same Episode, language and source)
		// link the Subtitle of the first SubtitleAdjustment of those matching SubtitleAdjustments to all the other SubtitleAdjustments, too
		// Because only for the first SubtitleAdjustment the Subtitle's contributions are specified. Not for all other SubtitleAdjustments
		Map<Subtitle, Subtitle> distinctSubs = new HashMap<>();
		for (SubtitleFile subAdj : data.subtitles)
		{
			Subtitle sub = subAdj.getFirstSubtitle();
			// sub can be null if parsed from non-standard-table
			if (sub != null)
			{
				Subtitle storedSub = distinctSubs.putIfAbsent(sub, sub);
				if (storedSub != null)
				{
					subAdj.setSingleSubtitle(storedSub);
				}
			}
		}

		// For all SubtitleAdjustments that reference the same attachmentID:
		// a) If the SubtitleAdjustments are equal -> remove all but the first (happens due to colspan)
		// b) If they have a different Subtitle (different Episode)
		// -> then add all other Subtitles to the first and remove all but the first (happens due to rowspan)
		// Map<AttachmentID->first SubtitleAdjustment with that Attachment-ID>
		Map<Integer, SubtitleFile> mapAttachmentsToSubs = new HashMap<>();
		ListIterator<SubtitleFile> subAdjIter = data.subtitles.listIterator();
		while (subAdjIter.hasNext())
		{
			SubtitleFile subAdj = subAdjIter.next();
			Integer attachmentId = subAdj.getAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID);
			SubtitleFile storedSubAdj = mapAttachmentsToSubs.putIfAbsent(attachmentId, subAdj);
			if (storedSubAdj != null)
			{
				// a) Equal -> remove duplicate
				if (subAdj.equals(storedSubAdj))
				{
					subAdjIter.remove();
				}
				// a) differ in Subtitle -> add other Subtitle to first SubtitleAdjustment
				else if (!subAdj.getSubtitles().isEmpty() && !subAdj.getFirstSubtitle().equals(storedSubAdj.getFirstSubtitle()))
				{
					storedSubAdj.getSubtitles().add(subAdj.getFirstSubtitle());
					subAdjIter.remove();
				}
			}
		}

		// Cleanup sub.source
		// 1) lower-case all sources
		// Add source=SubCentral.de to all german subs without a source
		for (SubtitleFile subAdj : data.subtitles)
		{
			for (Subtitle sub : subAdj.getSubtitles())
			{
				if (sub.getSource() == null)
				{
					if (Migration.SUBTITLE_LANGUAGE_GERMAN.equals(sub.getLanguage()))
					{
						sub.setSource(SubCentralDe.SITE_ID);
					}
				}
				else
				{
					sub.setSource(sub.getSource().toLowerCase(Migration.LOCALE_GERMAN));
				}
			}
		}

		// Sort subtitleAdjustments
		data.subtitles.sort(null);
	}

	private static String removeHtmlTagsAndBBCodes(String text)
	{
		if (text == null)
		{
			return null;
		}
		String bbCodesRemoved = removeBBCodes(text);
		String bbCodesAndHtmlRemoved = Jsoup.parse(bbCodesRemoved).text();
		return bbCodesAndHtmlRemoved;
	}

	private static String removeBBCodes(String text)
	{
		if (text == null)
		{
			return null;
		}
		String bbCodesRemoved = PATTERN_BBCODE.matcher(text).replaceAll("$2");
		return bbCodesRemoved;
	}

	public static final class SeasonPostContent
	{
		private final Series						series;
		private final ImmutableList<Season>			seasons;
		private final ImmutableList<SubtitleFile>	subtitleFiles;

		public SeasonPostContent(Series series, Iterable<Season> seasons, Iterable<SubtitleFile> subtitleAdjustments)
		{
			this.series = Objects.requireNonNull(series, "series");
			this.seasons = ImmutableList.copyOf(seasons);
			this.subtitleFiles = ImmutableList.copyOf(subtitleAdjustments);
		}

		public Series getSeries()
		{
			return series;
		}

		public ImmutableList<Season> getSeasons()
		{
			return seasons;
		}

		public ImmutableList<SubtitleFile> getSubtitleFiles()
		{
			return subtitleFiles;
		}
	}

	private static final class Data
	{
		// input
		private String								postTopic;
		private Document							postContent;

		// state
		private int									currentTableNum;

		// output
		private Series								series		= new Series(Migration.UNKNOWN_SERIES);
		private final SortedMap<Season, Season>		seasons		= new TreeMap<>();
		private final SortedMap<Episode, Episode>	episodes	= new TreeMap<>();
		private final List<SubtitleFile>			subtitles	= new ArrayList<>();
	}

	/**
	 * 
	 * Some values can be marked (f.e. with an asterisk *). Those markers need to be stored because they are needed to match the contributions / sources with the subtitle adjustments.
	 * 
	 * @param <T>
	 */
	private static final class MarkedValue<T>
	{
		private final T			value;
		private final String	marker;

		private MarkedValue(T value, String marker)
		{
			this.value = value;
			this.marker = marker;
		}
	}
}
