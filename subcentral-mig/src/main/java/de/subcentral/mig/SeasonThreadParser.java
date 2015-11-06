package de.subcentral.mig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringJoiner;
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
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class SeasonThreadParser
{
	private static final Logger log = LogManager.getLogger(SeasonThreadParser.class);

	private static enum ColumnType
	{
		UNKNOWN, EPISODE, GERMAN_SUBS, ENGLISH_SUBS, TRANSLATION, REVISION, ADJUSTMENT, SOURCE
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
	 * <li>S04E03</li>
	 * <li>E01:</li>
	 * <ul>
	 */
	private static final Pattern					PATTERN_EPISODE_ONLY_NUM			= Pattern.compile("S(\\d+)E(\\d+)");

	private static final Map<Pattern, ColumnType>	COLUMN_TYPE_PATTERNS				= createColumnTypePatternMap();

	private static Map<Pattern, ColumnType> createColumnTypePatternMap()
	{
		ImmutableMap.Builder<Pattern, ColumnType> map = ImmutableMap.builder();
		map.put(Pattern.compile("Episode"), ColumnType.EPISODE);
		map.put(Pattern.compile(".*?flags/de\\.png.*"), ColumnType.GERMAN_SUBS);
		map.put(Pattern.compile(".*?flags/(usa|uk|ca|aus)\\.png.*"), ColumnType.ENGLISH_SUBS);
		map.put(Pattern.compile("Übersetzung"), ColumnType.TRANSLATION);
		map.put(Pattern.compile("Korrektur"), ColumnType.REVISION);
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

		return new SeasonThreadData(data.seasons.keySet(), data.subtitleAdjustments);
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
			boolean success = parseStandardTable(data, table);
			if (!success)
			{
				parseNonStandardTable(data, table);
			}
		}
	}

	private static boolean parseStandardTable(Data data, Element table)
	{
		// Determine ColumnTypes
		Element thead = table.getElementsByTag("thead").first();
		if (thead == null)
		{
			return false;
		}
		Elements thElems = thead.getElementsByTag("th");
		if (thElems.isEmpty())
		{
			return false;
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
			return false;
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
			boolean success = parseStandardTableRow(data, tdElems, columns);
			if (!success)
			{
				return false;
			}
		}
		return true;
	}

	protected static void cleanupTable(List<List<Element>> rows, int numColumns)
	{
		// Stores for each columnIndex the current Element which should span several rows
		Element[] rowSpanCells = new Element[numColumns];
		// Stores for each columnIndex the remaining rows which the Element should span
		int[] rowspanRemainingRows = new int[numColumns];

		int colIndex;
		ListIterator<List<Element>> rowIter = rows.listIterator();
		while (rowIter.hasNext())
		{
			colIndex = 0;
			List<Element> row = rowIter.next();
			ListIterator<Element> cellIter = row.listIterator();
			while (cellIter.hasNext())
			{
				Element cell = cellIter.next();

				// Cleanup rowspan
				Element rowSpanCell = rowSpanCells[colIndex];
				if (rowSpanCell != null)
				{
					cellIter.add(rowSpanCell);
					int remainingRows = rowspanRemainingRows[colIndex] - 1;
					rowspanRemainingRows[colIndex] = remainingRows;
					if (remainingRows == 0)
					{
						rowSpanCells[colIndex] = null;
					}
				}

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

				// Cleanup colspan
				int colspan = 1;
				String colspanAttr = cell.attr("colspan");
				if (!rowspanAttr.isEmpty())
				{
					colspan = Integer.parseInt(colspanAttr);
					cell.attr("colspan", "1");
				}
				// insert columns
				for (int i = 0; i < colspan - 1; i++)
				{
					cellIter.add(cell);
				}

				colIndex++;
			}
		}
	}

	private static boolean parseStandardTableRow(Data data, List<Element> tdElems, ColumnType[] columns)
	{
		Episode parsedEpi = null;
		// Each top-level list represents a column,
		// each 2nd-level list represents the subtitles in that column
		List<List<MarkedValue<SubtitleAdjustment>>> parsedSubs = new ArrayList<>();
		// Each top-level list represents a column,
		// each 2nd-level list represents a division within that column (the divisions are divided by "|")
		// each 3rd-level list represents the contributions inside a division
		List<List<List<Contribution>>> parsedContributions = new ArrayList<>();
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
				case ADJUSTMENT:
					parseContributionsCell(parsedContributions, td, colType);
					break;
				case SOURCE:
					parseSourcesCell(parsedSources, td);
					break;
				default:
					// cancel on UNKNOWN columns
					return false;
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
						SubtitleAdjustment subAdj = markedSubAdj.value;
						addContributions(subAdj, divisionContributions);
					}
				}
			}
			// If only one sub column AND number of subDivision = number of contributionDivisions
			// -> map each contributionDivision to a subDivision
			// [DIM | WEB-DL] <-> [SubberA | SubberB]
			else if (numSubColumns == 1 && parsedSubs.get(0).size() == columnContributions.size())
			{
				List<MarkedValue<SubtitleAdjustment>> columnSubs = parsedSubs.get(0);
				for (int i = 0; i < columnSubs.size(); i++)
				{
					MarkedValue<SubtitleAdjustment> subAdj = columnSubs.get(i);
					List<Contribution> divisionContributions = columnContributions.get(i);
					addContributions(subAdj.value, divisionContributions);
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
			// Add every contribution to every sub
			else
			{
				for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
				{
					for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
					{
						for (List<Contribution> divisionContributions : columnContributions)
						{
							addContributions(subAdj.value, divisionContributions);
						}
					}
				}
			}
		}

		// Add subtitles
		for (List<MarkedValue<SubtitleAdjustment>> columnSubs : parsedSubs)
		{
			for (MarkedValue<SubtitleAdjustment> subAdj : columnSubs)
			{
				subAdj.value.getFirstSubtitle().setMedia(epi);
				data.subtitleAdjustments.add(subAdj.value);
			}
		}

		return true;
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
		return new Episode(data.series, season, numberInSeason, title);
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

		subs.add(subAdjs);
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
		System.out.println("Non standard table");
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
		private Series							series				= new Series(Migration.UNKNOWN_SERIES);
		private final HashMap<Season, Season>	seasons				= new HashMap<>();
		private final HashMap<Episode, Episode>	episodes			= new HashMap<>();
		private final List<SubtitleAdjustment>	subtitleAdjustments	= new ArrayList<>();
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
