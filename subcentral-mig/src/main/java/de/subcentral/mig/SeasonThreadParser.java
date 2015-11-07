package de.subcentral.mig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.Contribution;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.support.subcentralde.SubCentralApi;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class SeasonThreadParser
{
	private static final Logger log = LogManager.getLogger(SeasonThreadParser.class);

	private static enum ColumnType
	{
		UNKNOWN, EPISODE, GERMAN_SUBS, ENGLISH_SUBS, TRANSLATION, REVISION, TIMINGS, ADJUSTMENT, SOURCE
	};

	private static final Splitter					SPLITTER_PIPE						= Splitter.on('|').trimResults();
	private static final Splitter					SPLITTER_LIST						= Splitter.on(Pattern.compile("(?:,|&)")).trimResults();
	private static final Pattern					PATTERN_POST_TITLE_NUMBERED_SEASON	= Pattern.compile("(.*)\\s+-\\s*Staffel\\s+(\\d+)\\s*-\\s*.*");
	private static final Pattern					PATTERN_POST_TITLE_SPECIAL_SEASON	= Pattern.compile("(.*)\\s+-\\s*([\\w\\s]+)\\s*-\\s*.*");
	private static final Pattern					PATTERN_POST_TITLE_MULTIPLE_SEASONS	= Pattern.compile("(.*)\\s+-\\s*Staffel\\s+(\\d+)\\s+bis\\s+Staffel\\s+(\\d+)\\s*-\\s*.*");
	/**
	 * <ul>
	 * <li>E01 - "Pilot"</li>
	 * <li>030 - S04E03 - The Power of the Daleks (Verschollen)</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_WITH_TITLE			= Pattern.compile("(?:S(\\d+))?E(\\d+)\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

	/**
	 * <ul>
	 * <li>Special - "Wicked is Coming"</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_SPECIAL				= Pattern.compile("Special\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

	/**
	 * <ul>
	 * <li>S04E03</li>
	 * <li>E01:</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_ONLY_NUM			= Pattern.compile("(?:S(\\d+))?E(\\d+)");

	private static final Map<Pattern, ColumnType>	COLUMN_TYPE_PATTERNS				= createColumnTypePatternMap();

	private static Map<Pattern, ColumnType> createColumnTypePatternMap()
	{
		ImmutableMap.Builder<Pattern, ColumnType> map = ImmutableMap.builder();
		map.put(Pattern.compile("Episode"), ColumnType.EPISODE);
		map.put(Pattern.compile(".*?flags/de\\.png.*"), ColumnType.GERMAN_SUBS);
		map.put(Pattern.compile(".*?flags/(usa|uk|ca|aus)\\.png.*"), ColumnType.ENGLISH_SUBS);
		map.put(Pattern.compile("Übersetzung"), ColumnType.TRANSLATION);
		map.put(Pattern.compile("Korrektur"), ColumnType.REVISION);
		map.put(Pattern.compile("Timings"), ColumnType.TIMINGS);
		map.put(Pattern.compile("Anpassung"), ColumnType.ADJUSTMENT);
		map.put(Pattern.compile("Quelle"), ColumnType.SOURCE);
		return map.build();
	}

	public SeasonThreadData getAndParse(int threadId, SubCentralApi api) throws IOException
	{
		Document doc = api.getContent("index.php?page=Thread&threadID=" + threadId);
		return parse(doc);
	}

	public SeasonThreadData parse(Document threadHtml)
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

	public SeasonThreadData parse(String postTitle, String postContent)
	{
		// Title
		Data data = new Data();
		parsePostTitle(data, postTitle);

		// Content
		Document content = Jsoup.parse(postContent, SubCentralHttpApi.getHost().toExternalForm());
		parseSeasonHeader(data, content);
		parseDescription(data, content);
		parseSubtitleTable(data, content);

		cleanupData(data);

		return new SeasonThreadData(data.seasons.keySet(), data.subtitleAdjustments);
	}

	private void cleanupData(Data data)
	{
		// Add episodes to season
		// For single-season threads
		if (data.seasons.size() == 1)
		{
			Season season = data.seasons.keySet().iterator().next();
			data.episodes.keySet().stream().forEach((Episode epi) ->
			{
				season.addEpisode(epi);
			});
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
		for (SubtitleAdjustment subAdj : data.subtitleAdjustments)
		{
			Subtitle sub = subAdj.getFirstSubtitle();
			Subtitle storedSub = distinctSubs.putIfAbsent(sub, sub);
			if (storedSub != null)
			{
				subAdj.setSingleSubtitle(storedSub);
			}
		}

		// For all SubtitleAdjustments that reference the same attachmentID:
		// a) If the SubtitleAdjustments are equal -> remove all but the first (happens due to colspan)
		// b) If they have a different Subtitle (different Episode)
		// -> then add all other Subtitles to the first and remove all but the first (happens due to rowspan)
		// Map<AttachmentID->first SubtitleAdjustment with that Attachment-ID>
		Map<Integer, SubtitleAdjustment> mapAttachmentsToSubs = new HashMap<>();
		ListIterator<SubtitleAdjustment> subAdjIter = data.subtitleAdjustments.listIterator();
		while (subAdjIter.hasNext())
		{
			SubtitleAdjustment subAdj = subAdjIter.next();
			Integer attachmentId = subAdj.getAttributeValue(Migration.SUBTITLE_ADJUSTMENT_ATTR_ATTACHMENT_ID);
			SubtitleAdjustment storedSubAdj = mapAttachmentsToSubs.putIfAbsent(attachmentId, subAdj);
			if (storedSubAdj != null)
			{
				// a) Equal -> remove duplicate
				if (subAdj.equals(storedSubAdj))
				{
					subAdjIter.remove();
				}
				// a) differ in Subtitle -> add other Subtitle to first SubtitleAdjustment
				else if (!subAdj.getFirstSubtitle().equals(storedSubAdj.getFirstSubtitle()))
				{
					storedSubAdj.getSubtitles().add(subAdj.getFirstSubtitle());
					subAdjIter.remove();
				}
			}
		}

		// Cleanup sub.source
		// 1) lower-case all sources
		// Add source=SubCentral.de to all german subs without a source
		for (SubtitleAdjustment subAdj : data.subtitleAdjustments)
		{
			for (Subtitle sub : subAdj.getSubtitles())
			{
				if (sub.getSource() == null)
				{
					if (Migration.LANGUAGE_GERMAN.equals(sub.getLanguage()))
					{
						sub.setSource(SubCentralDe.SOURCE_ID);
					}
				}
				else
				{
					sub.setSource(sub.getSource().toLowerCase(Migration.LOCALE_GERMAN));
				}
			}
		}

		// Sort subtitleAdjustments
		data.subtitleAdjustments.sort(null);
	}

	/**
	 * <pre>
	 * Numbered:
	 * Mr. Robot - Staffel 1 - [DE-Subs: 10 | VO-Subs: 10] - [Komplett] - [+ Deleted Scenes]
	 * 
	 * Special with title:
	 * Doctor Who - Klassische Folgen - [DE-Subs: 111 | VO-Subs: 160 | Aired: 160] - [+Specials]
	 * Psych - Webisodes - [DE-Subs: 06 | VO-Subs: 06] - [Komplett]
	 * 
	 * Multiple seasons in one thread:
	 * Buffy the Vampire Slayer - Staffel 1 bis Staffel 7 - Komplett
	 * </pre>
	 * 
	 */
	private void parsePostTitle(Data data, String postTitle)
	{
		Matcher numberedMatcher = PATTERN_POST_TITLE_NUMBERED_SEASON.matcher(postTitle);
		if (numberedMatcher.matches())
		{
			Series series = new Series(numberedMatcher.group(1));
			data.series = series;
			Season season = series.newSeason();
			Integer number = Integer.valueOf(numberedMatcher.group(2));
			season.setNumber(number);
			data.seasons.put(season, season);
			return;
		}

		Matcher specialMatcher = PATTERN_POST_TITLE_SPECIAL_SEASON.matcher(postTitle);
		if (specialMatcher.matches())
		{
			Series series = new Series(specialMatcher.group(1));
			data.series = series;
			Season season = series.newSeason();
			String title = specialMatcher.group(2);
			season.setTitle(title);
			season.setSpecial(true);
			data.seasons.put(season, season);
			return;
		}

		Matcher multipleMatcher = PATTERN_POST_TITLE_MULTIPLE_SEASONS.matcher(postTitle);
		if (multipleMatcher.matches())
		{
			Series series = new Series(multipleMatcher.group(1));
			data.series = series;
			int firstSeasonNum = Integer.parseInt(multipleMatcher.group(2));
			int lastSeasonNum = Integer.parseInt(multipleMatcher.group(3));
			for (int i = firstSeasonNum; i <= lastSeasonNum; i++)
			{
				Season season = series.newSeason(i);
				data.seasons.put(season, season);
			}
			return;
		}
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
	private static void parseSeasonHeader(Data data, Document postContent)
	{
		Element headerImg = postContent.select("div.tbild > img").first();
		if (headerImg != null)
		{
			String headerUrl = headerImg.absUrl("src");
			for (Season season : data.seasons.keySet())
			{
				season.getImages().put(Migration.IMG_TYPE_SEASON_HEADER, headerUrl);
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
	private static void parseDescription(Data data, Document postContent)
	{
		Elements descriptionDivs = postContent.select("div.inhalt, div.websites");
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

	private static void parseSubtitleTable(Data data, Document postContent)
	{
		Elements tables = postContent.getElementsByTag("table");
		for (Element table : tables)
		{
			try
			{
				parseStandardTable(data, table);
			}
			catch (Exception e)
			{
				log.warn("Exception while trying to parse table as standard table. Parsing as non-standard table", e);
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
			throw new IllegalArgumentException("no thead element found");
		}
		Elements thElems = thead.getElementsByTag("th");
		if (thElems.isEmpty())
		{
			throw new IllegalArgumentException("no th elements found");
		}
		ColumnType[] columns = new ColumnType[thElems.size()];
		for (int i = 0; i < thElems.size(); i++)
		{
			ColumnType colType = determineColumnType(thElems.get(i).html());
			columns[i] = colType;
		}

		// Get rows and cells
		Element tbody = table.getElementsByTag("tbody").first();
		if (tbody == null)
		{
			throw new IllegalArgumentException("no tbody element found");
		}
		List<List<Element>> rows = new ArrayList<>();
		for (Element tr : tbody.getElementsByTag("tr"))
		{
			List<Element> tdElems = new ArrayList<>(tr.getElementsByTag("td"));
			rows.add(tdElems);
		}
		cleanupTable(rows, columns.length);

		for (List<Element> tdElems : rows)
		{
			parseStandardTableRow(data, tdElems, columns);
		}
	}

	protected static void cleanupTable(List<List<Element>> rows, int numColumns)
	{
		// Stores for each columnIndex the current Element which should span several rows
		Element[] rowSpanCells = new Element[numColumns];
		// Stores for each columnIndex the remaining rows which the Element should span
		int[] rowspanRemainingRows = new int[numColumns];

		for (List<Element> row : rows)
		{
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
			if (row.size() != numColumns)
			{
				throw new IllegalArgumentException("row does not have the same number of columns (" + row.size() + ") as it should have (" + numColumns + "). row: " + row);
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
					rowSpanCells[colIndex] = cell;
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
		}
	}

	private static void parseStandardTableRow(Data data, List<Element> tdElems, ColumnType[] columns)
	{
		Episode parsedEpi = null;
		// Each top-level list represents a column,
		// each 2nd-level list represents the subtitles in that column
		List<List<MarkedValue<SubtitleAdjustment>>> parsedSubs = new ArrayList<>();
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
					parsedEpi = parseEpisodeCell(data, td.text());
					break;
				case GERMAN_SUBS:
					// fall through
				case ENGLISH_SUBS:
					parseSubsCell(parsedSubs, td, colType);
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
					// cancel on UNKNOWN columns
					throw new IllegalArgumentException("Unknown column");
			}
		}

		// Add episode
		Episode epi = data.episodes.putIfAbsent(parsedEpi, parsedEpi);
		if (epi == null)
		{
			epi = parsedEpi;
		}

		// Add contributions to subtitles
		int numSubColumns = parsedSubs.size();
		int numSubs = parsedSubs.stream().mapToInt((List<MarkedValue<SubtitleAdjustment>> subsPerColumn) -> subsPerColumn.size()).sum();
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
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
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
					List<MarkedValue<SubtitleAdjustment>> columnSubs = parsedSubs.get(i);
					List<Contribution> divisionContributions = columnContributions.get(i);
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
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
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
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
					List<MarkedValue<SubtitleAdjustment>> columnSubs = parsedSubs.get(i);
					int contributionDivisionIndex = Math.max(0, i - diff);
					List<Contribution> divisionContributions = columnContributions.get(contributionDivisionIndex);
					for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
					{
						addContributions(subAdj.value, divisionContributions);
					}
				}
			}
			// Else: Add every contribution to every sub
			else
			{
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
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
						throw new IllegalArgumentException("Multiple sources marked with marker " + source.marker + ":" + storedSource + ", " + source);
					}
				}
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
					{
						addSource(subAdj.value, mapMarkerToSource.get(subAdj.marker));
					}
				}
			}
			// If only one source, add it to all the subtitles
			else if (parsedSources.size() == 1)
			{
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
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
					List<MarkedValue<SubtitleAdjustment>> columnSubs = parsedSubs.get(i);
					MarkedValue<String> markedSource = parsedSources.get(i);
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
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
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
					{
						MarkedValue<String> markedSource = parsedSources.get(index);
						addSource(markedSubAdj.value, markedSource.value);
						index++;
					}
				}
			}
			// Else: Add every source to every sub
			else
			{
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> markedSubAdj : columnSubs)
					{
						for (MarkedValue<String> markedSource : parsedSources)
						{
							addSource(markedSubAdj.value, markedSource.value);
						}
					}
				}
			}
		}

		// Add episodes
		data.episodes.put(epi, epi);

		// Add subtitles
		for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
		{
			for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
			{
				subAdj.value.getFirstSubtitle().setMedia(epi);
				data.subtitleAdjustments.add(subAdj.value);
			}
		}
	}

	private static void addSource(SubtitleAdjustment subAdj, String source)
	{
		subAdj.getFirstSubtitle().setSource(source);
	}

	private static void addContributions(SubtitleAdjustment subAdj, List<Contribution> contributions)
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

	private static Episode parseEpisodeCell(Data data, String text)
	{
		Integer seasonNumber = null;
		Integer numberInSeason = null;
		String title = null;
		boolean special = false;

		boolean parseSuccessful = false;
		Matcher epiMatcher = PATTERN_EPISODE_WITH_TITLE.matcher(text);
		if (epiMatcher.matches())
		{
			if (epiMatcher.group(1) != null)
			{
				seasonNumber = Integer.valueOf(epiMatcher.group(1));
			}
			numberInSeason = Integer.valueOf(epiMatcher.group(2));
			title = epiMatcher.group(3);
			parseSuccessful = true;
		}
		if (!parseSuccessful)
		{
			epiMatcher.reset();
			epiMatcher.usePattern(PATTERN_EPISODE_SPECIAL);
			if (epiMatcher.matches())
			{
				title = epiMatcher.group(1);
				special = true;
			}
			parseSuccessful = true;
		}
		if (!parseSuccessful)
		{
			epiMatcher.reset();
			epiMatcher.usePattern(PATTERN_EPISODE_ONLY_NUM);
			if (epiMatcher.matches())
			{
				if (epiMatcher.group(1) != null)
				{
					seasonNumber = Integer.valueOf(epiMatcher.group(1));
				}
				numberInSeason = Integer.valueOf(epiMatcher.group(2));
			}
			parseSuccessful = true;
		}
		if (!parseSuccessful)
		{
			title = text;
		}

		Season season = null;
		if (seasonNumber != null)
		{
			season = new Season(data.series, seasonNumber);
		}
		Episode epi = new Episode(data.series, season, numberInSeason, title);
		epi.setSpecial(special);
		return epi;
	}

	private static void parseSubsCell(List<List<MarkedValue<SubtitleAdjustment>>> subs, Element td, ColumnType colType)
	{
		String language;
		switch (colType)
		{
			case GERMAN_SUBS:
				language = Migration.LANGUAGE_GERMAN;
				break;
			case ENGLISH_SUBS:
				language = Migration.LANGUAGE_ENGLISH;
				break;
			default:
				language = null;
		}

		String cellContent = td.html();
		List<MarkedValue<SubtitleAdjustment>> subAdjs = new ArrayList<>();
		Pattern PATTERN_SUB_ADJ_ANCHOR = Pattern.compile("([*])?<a href=.*?attachmentID=(\\d+).*?>([*])?(.*?)([*])?</a>([*])?");

		Matcher subAdjAnchorMatcher = PATTERN_SUB_ADJ_ANCHOR.matcher(cellContent);
		while (subAdjAnchorMatcher.find())
		{
			String marker;
			if (subAdjAnchorMatcher.group(1) != null)
			{
				marker = subAdjAnchorMatcher.group(1);
			}
			else if (subAdjAnchorMatcher.group(3) != null)
			{
				marker = subAdjAnchorMatcher.group(3);
			}
			else if (subAdjAnchorMatcher.group(5) != null)
			{
				marker = subAdjAnchorMatcher.group(5);
			}
			else if (subAdjAnchorMatcher.group(6) != null)
			{
				marker = subAdjAnchorMatcher.group(6);
			}
			else
			{
				marker = null;
			}
			Integer attachmentId = Integer.valueOf(subAdjAnchorMatcher.group(2));
			String label = subAdjAnchorMatcher.group(4);

			Subtitle sub = new Subtitle();
			sub.setLanguage(language);

			Release rls = new Release(label);

			SubtitleAdjustment subAdj = new SubtitleAdjustment();
			subAdj.setSingleSubtitle(sub);
			subAdj.setSingleMatchingRelease(rls);
			subAdj.getAttributes().put(Migration.SUBTITLE_ADJUSTMENT_ATTR_ATTACHMENT_ID, attachmentId);

			MarkedValue<SubtitleAdjustment> markedSubAdj = new MarkedValue<>(subAdj, marker);
			subAdjs.add(markedSubAdj);
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
				contributionType = SubtitleAdjustment.CONTRIBUTION_TYPE_ADJUSTMENT;
				break;
			default:
				contributionType = null;
		}
		List<List<Contribution>> contributionsForColumn = new ArrayList<>();
		String text = td.text();
		for (String division : SPLITTER_PIPE.split(text))
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
		String text = td.text();
		Iterable<String> divisions = SPLITTER_PIPE.split(text);
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

	private static void parseNonStandardTable(Data data, Element table)
	{
		// TODO
	}

	public static class SeasonThreadData
	{
		private final ImmutableList<Season>				seasons;
		private final ImmutableList<SubtitleAdjustment>	subtitleAdjustments;

		public SeasonThreadData(Iterable<Season> seasons, Iterable<SubtitleAdjustment> subtitleAdjustments)
		{
			this.seasons = ImmutableList.copyOf(seasons);
			this.subtitleAdjustments = ImmutableList.copyOf(subtitleAdjustments);
		}

		public ImmutableList<Season> getSeasons()
		{
			return seasons;
		}

		public ImmutableList<SubtitleAdjustment> getSubtitleAdjustments()
		{
			return subtitleAdjustments;
		}
	}

	private static final class Data
	{
		private Series								series				= new Series(Migration.UNKNOWN_SERIES);
		private final SortedMap<Season, Season>		seasons				= new TreeMap<>();
		private final SortedMap<Episode, Episode>	episodes			= new TreeMap<>();
		private final List<SubtitleAdjustment>		subtitleAdjustments	= new ArrayList<>();
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
