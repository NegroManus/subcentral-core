package de.subcentral.mig.parse;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.BiConsumer;
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
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.mig.Migration;
import de.subcentral.mig.ScContributor;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class SeasonPostParser {
    private static final Logger log = LogManager.getLogger(SeasonPostParser.class);

    private enum ColumnType {
        UNKNOWN, EPISODE, SUBS, SUBS_GERMAN, SUBS_ENGLISH, TRANSLATION, REVISION, TIMINGS, ADJUSTMENT, SOURCE
    };

    private static final String                   URL_TEMPLATE                              = "http://subcentral.de/index.php?page=Thread&postID=%d";

    private static final String                   NO_VALUE                                  = "-";

    private static final Splitter                 SPLITTER_DIVISON                          = Splitter.on(CharMatcher.anyOf("|/")).trimResults();
    private static final Splitter                 SPLITTER_LIST                             = Splitter.on(CharMatcher.anyOf(",&")).trimResults();

    private static final Pattern                  PATTERN_BBCODE                            = Pattern.compile("\\[(\\w+).*?\\](.*?)\\[/\\1\\]");

    /**
     * Numbered season:
     * 
     * <pre>
     * Mr. Robot - Staffel 1 - [DE-Subs: 10 | VO-Subs: 10] - [Komplett] - [+ Deleted Scenes]
     * </pre>
     */
    private static final Pattern                  PATTERN_POST_TOPIC_NUMBERED_SEASON        = Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+)\\s+-\\s+\\[.*");

    /**
     * Numbered season with title:
     * 
     * <pre>
     * American Horror Story - Staffel 1: Horror House - [DE-Subs: 12 | VO-Subs: 12] - [Komplett]
     * </pre>
     */
    private static final Pattern                  PATTERN_POST_TOPIC_NUMBERED_TITLED_SEASON = Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+):\\s+(.*?)\\s+-\\s+\\[.*");

    /**
     * Multiple seasons:
     * 
     * <pre>
     * Buffy the Vampire Slayer - Staffel 1-7 - [Komplett]
     * </pre>
     */
    private static final Pattern                  PATTERN_POST_TOPIC_MULTIPLE_SEASONS       = Pattern.compile("(.*?)\\s+-\\s+Staffel\\s+(\\d+)-(\\d+)\\s+-\\s+\\[.*");

    /**
     * Special season:
     * 
     * <pre>
     * Doctor Who - Klassische Folgen - [DE-Subs: 111 | VO-Subs: 160 | Aired: 160] - [+Specials]
     * Psych - Webisodes - [DE-Subs: 06 | VO-Subs: 06] - [Komplett]
     * </pre>
     */
    private static final Pattern                  PATTERN_POST_TOPIC_SPECIAL_SEASON         = Pattern.compile("(.*?)\\s+-\\s+(.*?)\\s+-\\s+\\[.*");

    private static final Map<Pattern, ColumnType> COLUMN_TYPE_PATTERNS                      = createColumnTypePatternMap();

    /**
     * <ul>
     * <li>E23-E24 - "Alternate Cut"</li>
     * <li>E15-E16 - "Psych - The Musical"</li>
     * <li>E23+E24 - "And Martha Stewart Have A Ball (1) + (2)"</li>
     * <li>E23 + E24 - "And Martha Stewart Have A Ball (1) + (2)"</li>
     * <ul>
     */
    private static final Pattern                  PATTERN_EPISODE_MULTI                     = Pattern.compile(".*?(?:S(\\d+)\\s*)?E(\\d+)\\s*[-+]\\s*E(\\d+)\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

    /**
     * <ul>
     * <li>E01 - "Pilot"</li>
     * <li>S04E03 - The Power of the Daleks (Verschollen)</li>
     * <ul>
     */
    private static final Pattern                  PATTERN_EPISODE_REGULAR                   = Pattern.compile(".*?(?:S(\\d+)\\s*)?E(\\d+)\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

    /**
     * <ul>
     * <li>Special - "Wicked is Coming"</li>
     * <ul>
     */
    private static final Pattern                  PATTERN_EPISODE_SPECIAL                   = Pattern.compile("Special\\s*-\\s*(?:\\\")?(.*?)(?:\\\")?");

    /**
     * <ul>
     * <li>S04E03</li>
     * <li>E01:</li>
     * <ul>
     */
    private static final Pattern                  PATTERN_EPISODE_ONLY_NUM                  = Pattern.compile(".*?(?:S(\\d+)\\s*)?E(\\d+)");

    /**
     * <pre>
     * 		<a href="http://www.subcentral.de/index.php?page=Attachment&attachmentID=8776&h=b690960cfcb569ab0b06e35ae2c0c2c5d867ddd7" target="_blank">NoTV</a> 
     * 		<a href="http://www.subcentral.de/index.php?page=Attachment&attachmentID=46749">POW4</a>
     * </pre>
     */
    private static final Pattern                  PATTERN_ATTACHMENT_ANCHOR                 = Pattern.compile("<a.*?page=Attachment.*?attachmentID=(\\d+).*?>(.*?)</a>");
    /**
     * <pre>
     * 	[url='http://www.subcentral.de/index.php?page=Attachment&attachmentID=65018']IMMERSE[/url]
     * </pre>
     */
    private static final Pattern                  PATTERN_ATTACHMENT_BBCODE                 = Pattern.compile("\\[url=.*?page=Attachment.*?attachmentID=(\\d+).*?\\](.*?)\\[/url\\]");
    private static final Pattern                  PATTERN_ATTACHMENT_ANCHOR_MARKED          = Pattern.compile("([*]+)?<a.*?page=Attachment.*?attachmentID=(\\d+).*?>([*]+)?(.*?)([*]+)?</a>([*]+)?");
    private static final Pattern                  PATTERN_ATTACHMENT_BBCODE_MARKED          = Pattern
            .compile("([*]+)?\\[url=.*?page=Attachment.*?attachmentID=(\\d+).*?\\]([*]+)?(.*?)([*]+)?\\[/url\\]([*]+)?");

    private final List<ContributorPattern>        contributorParsers;

    public SeasonPostParser() {
        this(ImmutableList.of());
    }

    public SeasonPostParser(Iterable<ContributorPattern> contributorParsers) {
        this.contributorParsers = ImmutableList.copyOf(contributorParsers);
    }

    private static Map<Pattern, ColumnType> createColumnTypePatternMap() {
        ImmutableMap.Builder<Pattern, ColumnType> map = ImmutableMap.builder();
        map.put(Pattern.compile("(Episode|Folge)(?:n)?"), ColumnType.EPISODE);
        map.put(Pattern.compile(".*?flags/de\\.png.*"), ColumnType.SUBS_GERMAN);
        map.put(Pattern.compile("Deutsch"), ColumnType.SUBS_GERMAN);
        map.put(Pattern.compile(".*?flags/(usa|uk|ca|aus|nz)\\.png.*"), ColumnType.SUBS_ENGLISH);
        map.put(Pattern.compile("(Englisch|VO)"), ColumnType.SUBS_ENGLISH);
        map.put(Pattern.compile("(Übersetzung|Übersetzer)"), ColumnType.TRANSLATION);
        map.put(Pattern.compile("(Korrektur|Überarbeitung)"), ColumnType.REVISION);
        map.put(Pattern.compile("Timings"), ColumnType.TIMINGS);
        map.put(Pattern.compile("Anpassung"), ColumnType.ADJUSTMENT);
        map.put(Pattern.compile("Quelle"), ColumnType.SOURCE);
        map.put(Pattern.compile(".*(HDTV|HDTVRip|720p|WEB-DL|DVD|DVDRip).*"), ColumnType.SUBS);
        map.put(Pattern.compile(".*HDTV.*"), ColumnType.SUBS);
        return map.build();
    }

    public List<ContributorPattern> getContributorParsers() {
        return contributorParsers;
    }

    public SeasonPostData getAndParse(int postId) throws IOException {
        return parseThreadPage(Jsoup.parse(new URL(String.format(URL_TEMPLATE, postId)), Migration.TIMEOUT_MILLIS));
    }

    public SeasonPostData parseThreadPage(Document threadHtml) {
        // Get topic and content of first post
        Element postTopicDiv = threadHtml.getElementsByClass("messageTitle").first();
        Element postContentDiv = threadHtml.getElementsByClass("messageBody").first();
        if (postTopicDiv == null || postContentDiv == null) {
            throw new IllegalArgumentException("Invalid thread html: No post found");
        }
        Element postTextDiv = postContentDiv.child(0);

        return parsePost(postTopicDiv.text(), postTextDiv.html());
    }

    public SeasonPostData parsePost(WbbPost post) {
        return parsePost(post.getTopic(), post.getMessage());
    }

    public SeasonPostData parsePostTopic(String postTopic) {
        WorkData data = new WorkData();
        data.postTopic = postTopic;
        parseTopic(data);

        return data.toSeasonPostData();
    }

    public SeasonPostData parsePost(String postTopic, String postMessage) {
        WorkData data = new WorkData();
        data.postTopic = postTopic;
        data.postContent = Jsoup.parse(postMessage, SubCentralDe.getSite().getLink());

        // Topic
        parseTopic(data);

        // Content
        parseSeasonHeaderImage(data);
        parseDescription(data);
        parseSubtitleTable(data);

        cleanupData(data);

        return data.toSeasonPostData();
    }

    /**
     * Trying to parse the post topic with the {@code PATTERN_POST_TOPIC*} patterns.
     */
    private static void parseTopic(WorkData data) {
        Matcher topicMatcher = PATTERN_POST_TOPIC_NUMBERED_SEASON.matcher(data.postTopic);
        if (topicMatcher.matches()) {
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
        if (topicMatcher.matches()) {
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
        topicMatcher.usePattern(PATTERN_POST_TOPIC_MULTIPLE_SEASONS);
        if (topicMatcher.matches()) {
            Series series = new Series(topicMatcher.group(1));
            series.setType(Series.TYPE_SEASONED);
            data.series = series;
            int firstSeasonNum = Integer.parseInt(topicMatcher.group(2));
            int lastSeasonNum = Integer.parseInt(topicMatcher.group(3));
            for (int i = firstSeasonNum; i <= lastSeasonNum; i++) {
                Season season = series.newSeason(i);
                data.seasons.put(season, season);
            }
            return;
        }

        topicMatcher.reset();
        topicMatcher.usePattern(PATTERN_POST_TOPIC_SPECIAL_SEASON);
        if (topicMatcher.matches()) {
            Series series = new Series(topicMatcher.group(1));
            data.series = series;
            Season season = series.newSeason();
            String title = topicMatcher.group(2).trim();
            season.setTitle(title);
            season.setSpecial(true);
            data.seasons.put(season, season);
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
    private static void parseSeasonHeaderImage(WorkData data) {
        Element headerImg = data.postContent.select("div.tbild > img").first();
        if (headerImg != null) {
            String headerUrl = headerImg.absUrl("src");
            for (Season season : data.seasons.keySet()) {
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
     *			<p>Offizielle Website: <a href="http://www.usanetwork.com/mrrobot" target="_blank" class="usa network">USA Network</a> <br> Weitere Informationen: <a href=
    "http://en.wikipedia.org/wiki/Mr._Robot_(TV_series)" target="_blank" class="wiki">Wikipedia (en)</a> und <a href="http://www.imdb.com/title/tt4158110/?ref_=nv_sr_1" target="_blank" class=
    "imdb">IMDb</a></p> 
     *		</div> 
     *	</div> 
     *  </div>
     *  }
     * </pre>
     * 
     * @param season
     * @param postContent
     */
    private static void parseDescription(WorkData data) {
        Elements descriptionDivs = data.postContent.select("div.inhalt, div.websites");
        StringJoiner joiner = new StringJoiner("\n");
        for (Element div : descriptionDivs) {
            joiner.add(div.html());
        }
        if (joiner.length() > 0) {
            String description = joiner.toString();
            for (Season season : data.seasons.keySet()) {
                season.setDescription(description);
            }
        }
    }

    private static void parseSubtitleTable(WorkData data) {
        Elements tables = data.postContent.getElementsByTag("table");
        for (Element table : tables) {
            data.currentTableNum++;
            try {
                parseStandardTable(data, table);
            }
            catch (Exception e) {
                log.warn("Failed to to parse subtitle table as standard table. Parsing as non-standard table (post title: \"{}\", table number: {}, exception: {})",
                        data.postTopic,
                        data.currentTableNum,
                        e.toString());
                parseNonStandardTable(data, table);
            }
        }
    }

    private static void parseStandardTable(WorkData data, Element table) {
        if (!table.getElementsByClass("sclink").isEmpty()) {
            log.trace("Skipping link table (post topic: \"{}\", table number: {})", data.postTopic, data.currentTableNum);
            return;
        }

        Elements thElems = table.getElementsByTag("th");
        if (thElems.isEmpty()) {
            throw new IllegalArgumentException("No th elements found");
        }

        ColumnType[] columns = new ColumnType[thElems.size()];
        for (int i = 0; i < thElems.size(); i++) {
            Element th = thElems.get(i);
            ColumnType colType = parseColumnType(th.html());
            if (ColumnType.UNKNOWN.equals(colType)) {
                throw new IllegalArgumentException("Column type could not be determined. Unknown column head: " + th.toString());
            }
            columns[i] = colType;
        }

        // Get rows and cells
        Element tbody = table.getElementsByTag("tbody").first();
        if (tbody == null) {
            throw new IllegalArgumentException("No tbody element found");
        }

        List<List<Element>> rows = new ArrayList<>();
        for (Element tr : tbody.getElementsByTag("tr")) {
            List<Element> tdElems = new ArrayList<>(tr.getElementsByTag("td"));
            rows.add(tdElems);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("No tr elements found inside the tbody element");
        }

        cleanupTable(data, rows, columns.length);

        for (List<Element> tdElems : rows) {
            parseStandardTableRow(data, tdElems, columns);
        }
    }

    private static ColumnType parseColumnType(String columnTitle) {
        for (Map.Entry<Pattern, ColumnType> entries : COLUMN_TYPE_PATTERNS.entrySet()) {
            if (entries.getKey().matcher(columnTitle).matches()) {
                return entries.getValue();
            }
        }
        return ColumnType.UNKNOWN;
    }

    private static void cleanupTable(WorkData data, List<List<Element>> rows, int numColumns) {
        // Stores for each columnIndex the current Element which should span several rows
        Element[] rowSpanCells = new Element[numColumns];
        // Stores for each columnIndex the remaining rows which the Element should span
        int[] rowspanRemainingRows = new int[numColumns];

        ListIterator<List<Element>> rowIter = rows.listIterator();
        while (rowIter.hasNext()) {
            List<Element> row = rowIter.next();
            if (row.isEmpty()) {
                log.debug("Skipping empty row (post topic: \"{}\", table number: {})", data.postTopic, data.currentTableNum);
                continue;
            }

            // Cleanup rowspan
            for (int i = 0; i < numColumns; i++) {
                Element rowSpanCell = rowSpanCells[i];
                if (rowSpanCell != null) {
                    row.add(i, rowSpanCell);
                    int remainingRows = rowspanRemainingRows[i] - 1;
                    rowspanRemainingRows[i] = remainingRows;
                    if (remainingRows == 0) {
                        rowSpanCells[i] = null;
                    }
                }
            }

            ListIterator<Element> cellIter = row.listIterator();
            int colIndex = 0;
            while (cellIter.hasNext()) {
                Element cell = cellIter.next();

                // Check for rowspan
                int rowspan = 1;
                String rowspanAttr = cell.attr("rowspan");
                if (!rowspanAttr.isEmpty()) {
                    rowspan = Integer.parseInt(rowspanAttr);
                    cell.attr("rowspan", "1");
                }
                if (rowspan > 1) {
                    rowSpanCells[colIndex] = cell.clone();
                    rowspanRemainingRows[colIndex] = rowspan - 1;
                }

                // Check for colspan
                int colspan = 1;
                String colspanAttr = cell.attr("colspan");
                if (!colspanAttr.isEmpty()) {
                    colspan = Integer.parseInt(colspanAttr);
                    cell.attr("colspan", "1");
                }

                // Cleanup colspan
                for (int i = 0; i < colspan - 1; i++) {
                    cellIter.add(cell);
                }

                colIndex++;
            }

            if (row.size() < numColumns) {
                log.warn("Row does not have the expected number of columns (expected: {}, actual: {}; post topic: \"{}\", table number: {}, columns: {})",
                        numColumns,
                        row.size(),
                        data.postTopic,
                        data.currentTableNum,
                        row);
            }
        }
    }

    private static void parseStandardTableRow(WorkData data, List<Element> tdElems, ColumnType[] columns) {
        List<Episode> parsedEpis = new ArrayList<>();
        // Each top-level list contains the columns,
        // each 2nd-level list contains the subtitles within a column
        List<List<MarkedValue<SubtitleRelease>>> parsedSubs = new ArrayList<>();
        // Each top-level list contains the columns,
        // each 2nd-level list contains the divisions within a column (the divisions are divided by "|"),
        // each 3rd-level list contains the contributions within a division
        List<List<List<MarkedValue<Contribution>>>> parsedContributions = new ArrayList<>();
        // Each top-level list contains the divisions inside the source column
        List<MarkedValue<Site>> parsedSources = new ArrayList<>();

        for (int i = 0; i < tdElems.size(); i++) {
            Element td = tdElems.get(i);
            ColumnType colType = columns[i];
            switch (colType) {
                case EPISODE:
                    parseEpisodeCell(parsedEpis, data, td);
                    break;
                case SUBS:
                    // fall through
                case SUBS_GERMAN:
                    // fall through
                case SUBS_ENGLISH:
                    parseSubtitlesCell(parsedSubs, td, colType);
                    break;
                case TRANSLATION:
                    // fall through
                case REVISION:
                    // fall through
                case TIMINGS:
                    // fall through
                case ADJUSTMENT:
                    parseContributorsCell(parsedContributions, td, colType);
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
        while (parsedEpisIter.hasNext()) {
            Episode parsedEpi = parsedEpisIter.next();
            Episode storedEpi = data.episodes.putIfAbsent(parsedEpi, parsedEpi);
            if (storedEpi != null) {
                parsedEpisIter.set(storedEpi);
            }
        }

        List<MarkedValue<SubtitleRelease>> allSubs = collectAllSubtitles(parsedSubs);
        int numSubColumns = parsedSubs.size();
        int numSubs = allSubs.size();

        // Add contributions to subtitles
        List<MarkedValue<Contribution>> allContributions = collectAllContributions(parsedContributions);
        // If any markers
        if (MarkedValue.anyValueMarked(allContributions)) {
            MarkedValue.mapMarkedValues(allSubs, allContributions, SeasonPostParser::addContribution);
        }
        else {
            // For each contribution column (each has a separate contribution type)
            for (List<List<MarkedValue<Contribution>>> contributionsInColumn : parsedContributions) {
                int numContributionDivisions = contributionsInColumn.size();
                // If number of contributionDivisions = 1
                // -> map all contributions in that division to all subtitles
                // [LOL][DIM] <-> [SubberA]
                // [LOL][DIM] <-> [SubberA, SubberB]
                if (numContributionDivisions == 1) {
                    for (List<MarkedValue<SubtitleRelease>> subsInColumn : parsedSubs) {
                        for (MarkedValue<SubtitleRelease> subAdj : subsInColumn) {
                            addContributions(subAdj.value, contributionsInColumn.get(0));
                        }
                    }
                }
                // If number of subColumns == number of contributionDivisions
                // -> map each contribution division to all subs in the corresponding subs column
                // [LOL][DIM] <-> [SubberA | SubberB]
                else if (numSubColumns == numContributionDivisions) {
                    for (int i = 0; i < numSubColumns; i++) {
                        List<MarkedValue<SubtitleRelease>> subsInColumn = parsedSubs.get(i);
                        List<MarkedValue<Contribution>> contributionsInDivision = contributionsInColumn.get(i);
                        for (MarkedValue<SubtitleRelease> markedSub : subsInColumn) {
                            addContributions(markedSub.value, contributionsInDivision);
                        }
                    }
                }
                // If number of subs == number of contribution divisions
                // -> map each contribution division to a sub
                // [DIM | WEB-DL] <-> [SubberA | SubberB]
                // [2HD] [PublicHD | DON] <-> [Grizzly | Eric | NegroManus]
                else if (numSubs == contributionsInColumn.size()) {
                    for (int i = 0; i < numSubs; i++) {
                        MarkedValue<SubtitleRelease> markedSub = allSubs.get(i);
                        List<MarkedValue<Contribution>> contributionsInDivision = contributionsInColumn.get(i);
                        addContributions(markedSub.value, contributionsInDivision);
                    }
                }
                // If more subColumns than contributionDivisions
                // -> Add the first contributionDivision to the subColumns from index 0 to n,
                // where n is = numSubColumns - numContributionDivisions
                // [720p][1080p][WEB-DL] <-> [SubberA | SubberB]
                else if (numSubColumns > numContributionDivisions && numContributionDivisions > 1) {
                    int diff = numSubColumns - numContributionDivisions;
                    for (int i = 0; i < numSubColumns; i++) {
                        List<MarkedValue<SubtitleRelease>> columnSubs = parsedSubs.get(i);
                        int contributionDivisionIndex = Math.max(0, i - diff);
                        List<MarkedValue<Contribution>> divisionContributions = contributionsInColumn.get(contributionDivisionIndex);
                        for (MarkedValue<SubtitleRelease> subAdj : columnSubs) {
                            addContributions(subAdj.value, divisionContributions);
                        }
                    }
                }
                // Else: Add every contribution to every sub
                else {
                    for (MarkedValue<SubtitleRelease> markedSubAdj : allSubs) {
                        for (List<MarkedValue<Contribution>> divisionContributions : contributionsInColumn) {
                            addContributions(markedSubAdj.value, divisionContributions);
                        }
                    }
                }
            }
        }

        // Add sources to subtitles
        if (!parsedSources.isEmpty()) {
            // If any markers
            if (MarkedValue.anyValueMarked(parsedSources)) {
                MarkedValue.mapMarkedValues(allSubs, parsedSources, SeasonPostParser::addSource);
            }
            // If only one source, add it to all the subtitles
            else if (parsedSources.size() == 1) {
                for (List<MarkedValue<SubtitleRelease>> columnSubs : parsedSubs) {
                    for (MarkedValue<SubtitleRelease> markedSubAdj : columnSubs) {
                        addSource(markedSubAdj.value, parsedSources.get(0).value);
                    }
                }
            }
            // If number of sub columns == number of sources
            // -> add each source to all subs in a column
            // [DIM][WEB-DL] <-> [Addic7ed.com | SubCentral.de]
            else if (numSubColumns == parsedSources.size()) {
                for (int i = 0; i < numSubColumns; i++) {
                    List<MarkedValue<SubtitleRelease>> columnSubs = parsedSubs.get(i);
                    MarkedValue<Site> markedSource = parsedSources.get(i);
                    for (MarkedValue<SubtitleRelease> markedSubAdj : columnSubs) {
                        addSource(markedSubAdj.value, markedSource.value);
                    }
                }
            }
            // If number of subs == number of contributionDivisions
            // -> map each source to a sub
            // [DIM | WEB-DL] <-> [Addic7ed.com | SubCentral.de]
            else if (numSubs == parsedSources.size()) {
                int index = 0;
                for (List<MarkedValue<SubtitleRelease>> columnSubs : parsedSubs) {
                    for (MarkedValue<SubtitleRelease> markedSubAdj : columnSubs) {
                        MarkedValue<Site> markedSource = parsedSources.get(index);
                        addSource(markedSubAdj.value, markedSource.value);
                        index++;
                    }
                }
            }
            // Else: Concat the sources and add the combined source to every sub
            else {
                StringJoiner combinedSourceJoiner = new StringJoiner(" & ");
                for (MarkedValue<Site> markedSource : parsedSources) {
                    combinedSourceJoiner.add(markedSource.value.getName());
                }
                String combinedSourceName = combinedSourceJoiner.toString();
                Site combinedSource = new Site(combinedSourceName);

                for (List<MarkedValue<SubtitleRelease>> columnSubs : parsedSubs) {
                    for (MarkedValue<SubtitleRelease> markedSubAdj : columnSubs) {
                        addSource(markedSubAdj.value, combinedSource);
                    }
                }
            }
        }

        // Add subtitles
        for (List<MarkedValue<SubtitleRelease>> columnSubs : parsedSubs) {
            for (MarkedValue<SubtitleRelease> subAdj : columnSubs) {
                Episode firstEpi = parsedEpis.get(0);
                subAdj.value.getFirstSubtitle().setMedia(firstEpi);
                data.subtitles.add(subAdj.value);

                // In case of a multiple episodes in that row, for each more episode,
                // add a clone of the parsed subtitle with that episode
                if (parsedEpis.size() > 1) {
                    for (int i = 1; i < parsedEpis.size(); i++) {
                        SubtitleRelease copy = SerializationUtils.clone(subAdj.value);
                        Episode epi = parsedEpis.get(i);
                        copy.getFirstSubtitle().setMedia(epi);
                        data.subtitles.add(copy);
                    }
                }
            }
        }
    }

    private static void parseEpisodeCell(List<Episode> epis, WorkData data, Element td) {
        String text = removeBBCodes(td.text());
        Matcher epiMatcher = PATTERN_EPISODE_MULTI.matcher(text);
        if (epiMatcher.matches()) {
            Season season = null;
            if (epiMatcher.group(1) != null) {
                Integer seasonNumber = Integer.valueOf(epiMatcher.group(1));
                season = new Season(data.series, seasonNumber);
            }
            int firstEpiNum = Integer.parseInt(epiMatcher.group(2));
            int lastEpiNum = Integer.parseInt(epiMatcher.group(3));
            String title = epiMatcher.group(4);
            for (int i = firstEpiNum; i <= lastEpiNum; i++) {
                epis.add(new Episode(data.series, season, i, title));
            }
            return;
        }

        epiMatcher.reset();
        epiMatcher.usePattern(PATTERN_EPISODE_REGULAR);
        if (epiMatcher.matches()) {
            Season season = null;
            if (epiMatcher.group(1) != null) {
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
        if (epiMatcher.matches()) {
            String title = epiMatcher.group(1);
            Episode epi = new Episode(data.series, title);
            epi.setSpecial(true);
            epis.add(epi);
            return;
        }

        epiMatcher.reset();
        epiMatcher.usePattern(PATTERN_EPISODE_ONLY_NUM);
        if (epiMatcher.matches()) {
            Season season = null;
            if (epiMatcher.group(1) != null) {
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

    private static void parseSubtitlesCell(List<List<MarkedValue<SubtitleRelease>>> subs, Element td, ColumnType colType) {
        String language;
        switch (colType) {
            case SUBS_GERMAN:
                language = Migration.SUBTITLE_LANGUAGE_GERMAN;
                break;
            case SUBS_ENGLISH:
                language = Migration.SUBTITLE_LANGUAGE_ENGLISH;
                break;
            default:
                language = null;
        }

        String cellContent = td.html();
        List<MarkedValue<SubtitleRelease>> subAdjs = new ArrayList<>();

        Matcher subLinkMatcher = PATTERN_ATTACHMENT_ANCHOR_MARKED.matcher(cellContent);
        while (subLinkMatcher.find()) {
            handleMarkedSubtitleLinkMatch(subLinkMatcher, language, subAdjs);
        }
        subLinkMatcher.usePattern(PATTERN_ATTACHMENT_BBCODE_MARKED);
        subLinkMatcher.reset();
        while (subLinkMatcher.find()) {
            handleMarkedSubtitleLinkMatch(subLinkMatcher, language, subAdjs);
        }

        // Only add the subColumn to the subColumn list if it is not empty
        // because empty columns are not respected in the contributionDivisions
        // F.e. [DIM][-][WEB-DL] <-> [- | Grollbringer] means that Grollbringer adjusted the WEB-DL,
        // not the non-existing sub in the empty column
        if (!subAdjs.isEmpty()) {
            subs.add(subAdjs);
        }
    }

    private static void handleMarkedSubtitleLinkMatch(Matcher matcher, String language, List<MarkedValue<SubtitleRelease>> subFiles) {
        String marker = null;
        // Test all possible marker groups: 1,3,5,6
        for (int i : new int[] { 1, 3, 5, 6 }) {
            if (matcher.start(i) != -1) {
                marker = matcher.group(i);
                break;
            }
        }

        Integer attachmentId = Integer.valueOf(matcher.group(2));
        String label = removeHtmlTagsAndBBCodes(matcher.group(4));

        Subtitle sub = new Subtitle();
        sub.setLanguage(language);

        Release rls = new Release(label);

        SubtitleRelease subAdj = new SubtitleRelease();
        subAdj.setSingleSubtitle(sub);
        subAdj.setSingleMatchingRelease(rls);
        subAdj.getAttributes().put(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID, attachmentId);

        MarkedValue<SubtitleRelease> markedSubAdj = new MarkedValue<>(subAdj, marker);
        subFiles.add(markedSubAdj);
    }

    /**
     * <ul>
     * <li>List 1: Columns</li>
     * <li>List 2: Divisions in a column, separated by "|"</li>
     * <li>List 3: List of contributors in a division</li>
     * </ul>
     * 
     * <pre>
     * - | Jesuxxx
     * </pre>
     * 
     * <pre>
     * Grollbringer | Negro & Sogge377
     * </pre>
     */
    private static void parseContributorsCell(List<List<List<MarkedValue<Contribution>>>> contributions, Element td, ColumnType colType) {
        String contributionType = contributionTypeFromColumnType(colType);
        List<List<MarkedValue<Contribution>>> contributionsInColumn = new ArrayList<>();
        String text = removeHtmlTagsAndBBCodes(td.text());
        for (String division : SPLITTER_DIVISON.split(text)) {
            if (division.isEmpty() || division.equals(NO_VALUE)) {
                contributionsInColumn.add(ImmutableList.of());
            }
            else {
                List<MarkedValue<Contribution>> contributionsInDiv = new ArrayList<>();
                for (String contributorText : SPLITTER_LIST.split(division)) {
                    MarkedValue<String> markedContributor = MarkedValue.parse(contributorText);
                    ScContributor contributor = new ScContributor(ScContributor.Type.SUBBER);
                    contributor.setName(markedContributor.value);
                    Contribution contribution = new Contribution(contributor, contributionType);
                    contributionsInDiv.add(new MarkedValue<Contribution>(contribution, markedContributor.marker));
                }
                contributionsInColumn.add(contributionsInDiv);
            }
        }
        contributions.add(contributionsInColumn);
    }

    private static String contributionTypeFromColumnType(ColumnType colType) {
        switch (colType) {
            case TRANSLATION:
                return Subtitle.CONTRIBUTION_TYPE_TRANSLATION;
            case REVISION:
                return Subtitle.CONTRIBUTION_TYPE_REVISION;
            case TIMINGS:
                return Subtitle.CONTRIBUTION_TYPE_TIMINGS;
            case ADJUSTMENT:
                return SubtitleRelease.CONTRIBUTION_TYPE_ADJUSTMENT;
            default:
                return null;
        }
    }

    private static void parseSourcesCell(List<MarkedValue<Site>> sources, Element td) {
        String text = removeBBCodes(td.text());
        Iterable<String> divisions = SPLITTER_DIVISON.split(text);
        for (String division : divisions) {
            if (!division.isEmpty() && !NO_VALUE.equals(division)) {
                MarkedValue<String> markedSourceName = MarkedValue.parse(division);
                Site source = new Site(markedSourceName.value, markedSourceName.value);
                MarkedValue<Site> markedSource = new MarkedValue<>(source, markedSourceName.marker);
                sources.add(markedSource);
            }
        }
    }

    private static void addSource(SubtitleRelease subAdj, Site source) {
        subAdj.getFirstSubtitle().setSource(source);
    }

    private static void addContributions(SubtitleRelease subRls, List<MarkedValue<Contribution>> markedContributions) {
        for (MarkedValue<Contribution> mv : markedContributions) {
            addContribution(subRls, mv.value);
        }
    }

    private static void addContribution(SubtitleRelease subRls, Contribution contribution) {
        if (Subtitle.CONTRIBUTION_TYPE_TRANSCRIPT.equals(contribution.getType()) || Subtitle.CONTRIBUTION_TYPE_TIMINGS.equals(contribution.getType())
                || Subtitle.CONTRIBUTION_TYPE_TRANSLATION.equals(contribution.getType()) || Subtitle.CONTRIBUTION_TYPE_REVISION.equals(contribution.getType())) {
            subRls.getFirstSubtitle().getContributions().add(contribution);
        }
        else {
            subRls.getContributions().add(contribution);
        }
    }

    private static List<MarkedValue<SubtitleRelease>> collectAllSubtitles(List<List<MarkedValue<SubtitleRelease>>> subs) {
        List<MarkedValue<SubtitleRelease>> allSubs = new ArrayList<>();
        for (List<MarkedValue<SubtitleRelease>> subsInDivision : subs) {
            allSubs.addAll(subsInDivision);
        }
        return allSubs;
    }

    private static List<MarkedValue<Contribution>> collectAllContributions(List<List<List<MarkedValue<Contribution>>>> contributions) {
        List<MarkedValue<Contribution>> allContributions = new ArrayList<>();
        for (List<List<MarkedValue<Contribution>>> contributionsInColumn : contributions) {
            for (List<MarkedValue<Contribution>> contributionsInDivision : contributionsInColumn) {
                allContributions.addAll(contributionsInDivision);
            }
        }
        return allContributions;
    }

    private static void parseNonStandardTable(WorkData data, Element table) {
        // Just add all attachment links as subtitles
        String content = table.html();
        Matcher subLinkMatcher = PATTERN_ATTACHMENT_ANCHOR.matcher(content);
        while (subLinkMatcher.find()) {
            handleSubtitleLinkMatch(subLinkMatcher, data);
        }
        subLinkMatcher.usePattern(PATTERN_ATTACHMENT_BBCODE);
        subLinkMatcher.reset();
        while (subLinkMatcher.find()) {
            handleSubtitleLinkMatch(subLinkMatcher, data);
        }
    }

    private static void handleSubtitleLinkMatch(Matcher subLinkMatcher, WorkData data) {
        Integer attachmentId = Integer.valueOf(subLinkMatcher.group(1));
        String label = removeHtmlTagsAndBBCodes(subLinkMatcher.group(2));

        Release rls = new Release(label);
        SubtitleRelease subRls = new SubtitleRelease();
        subRls.getMatchingReleases().add(rls);
        subRls.getAttributes().put(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID, attachmentId);
        data.subtitles.add(subRls);
    }

    private static void cleanupData(WorkData data) {
        // Add episodes to season
        // For single-season threads
        if (data.seasons.size() == 1) {
            Season season = data.seasons.keySet().iterator().next();
            for (Episode epi : data.episodes.keySet()) {
                epi.setSeason(season);
            }
        }
        // For multi-season threads
        else {
            for (Episode epi : data.episodes.keySet()) {
                if (epi.isPartOfSeason()) {
                    boolean foundSeason = false;
                    for (Season season : data.seasons.keySet()) {
                        if (season.getNumber() != null && season.getNumber().equals(epi.getSeason().getNumber())) {
                            epi.setSeason(season);
                            foundSeason = true;
                        }
                    }
                    if (!foundSeason) {
                        Season newSeason = epi.getSeason();
                        epi.setSeason(newSeason);
                        data.seasons.put(newSeason, newSeason);
                    }
                }
                else {
                    Season newUnknownSeason = new Season(data.series);
                    Season alreadyStoredSeason = data.seasons.putIfAbsent(newUnknownSeason, newUnknownSeason);
                    if (alreadyStoredSeason != null) {
                        alreadyStoredSeason.addEpisode(epi);
                    }
                    else {
                        newUnknownSeason.addEpisode(epi);
                    }
                }
            }
        }

        // For SubtitleAdjustments that reference the same Subtitle (same Episode, language and source)
        // link the Subtitle of the first SubtitleAdjustment of those matching SubtitleAdjustments to all the other SubtitleAdjustments, too
        // Because only for the first SubtitleAdjustment the Subtitle's contributions are specified. Not for all other SubtitleAdjustments
        Map<Subtitle, Subtitle> distinctSubs = new HashMap<>();
        for (SubtitleRelease subAdj : data.subtitles) {
            Subtitle sub = subAdj.getFirstSubtitle();
            // sub can be null if parsed from non-standard-table
            if (sub != null) {
                Subtitle storedSub = distinctSubs.putIfAbsent(sub, sub);
                if (storedSub != null) {
                    subAdj.setSingleSubtitle(storedSub);
                }
            }
        }

        // For all SubtitleAdjustments that reference the same attachmentID:
        // a) If the SubtitleAdjustments are equal -> remove all but the first (happens due to colspan)
        // b) If they have a different Subtitle (different Episode)
        // -> then add all other Subtitles to the first and remove all but the first (happens due to rowspan)
        // Map<AttachmentID->first SubtitleAdjustment with that Attachment-ID>
        Map<Integer, SubtitleRelease> mapAttachmentsToSubs = new HashMap<>();
        ListIterator<SubtitleRelease> subAdjIter = data.subtitles.listIterator();
        while (subAdjIter.hasNext()) {
            SubtitleRelease subAdj = subAdjIter.next();
            Integer attachmentId = subAdj.getFirstAttributeValue(Migration.SUBTITLE_FILE_ATTR_ATTACHMENT_ID);
            SubtitleRelease storedSubAdj = mapAttachmentsToSubs.putIfAbsent(attachmentId, subAdj);
            if (storedSubAdj != null) {
                // a) Equal -> remove duplicate
                if (subAdj.equals(storedSubAdj)) {
                    subAdjIter.remove();
                }
                // a) differ in Subtitle -> add other Subtitle to first SubtitleAdjustment
                else if (!subAdj.getSubtitles().isEmpty() && !subAdj.getFirstSubtitle().equals(storedSubAdj.getFirstSubtitle())) {
                    storedSubAdj.getSubtitles().add(subAdj.getFirstSubtitle());
                    subAdjIter.remove();
                }
            }
        }

        // Cleanup sub.source
        // add source=SubCentral.de to all german subs without a source
        for (SubtitleRelease subAdj : data.subtitles) {
            for (Subtitle sub : subAdj.getSubtitles()) {
                if (sub.getSource() == null) {
                    if (Migration.SUBTITLE_LANGUAGE_GERMAN.equals(sub.getLanguage())) {
                        sub.setSource(SubCentralDe.getSite());
                    }
                }
            }
        }

        // Sort subtitle releases
        data.subtitles.sort(null);
    }

    private static String removeHtmlTagsAndBBCodes(String text) {
        if (text == null) {
            return null;
        }
        String bbCodesRemoved = removeBBCodes(text);
        String bbCodesAndHtmlRemoved = Jsoup.parse(bbCodesRemoved).text();
        return bbCodesAndHtmlRemoved;
    }

    private static String removeBBCodes(String text) {
        if (text == null) {
            return null;
        }
        String bbCodesRemoved = PATTERN_BBCODE.matcher(text).replaceAll("$2");
        return bbCodesRemoved;
    }

    public static final class SeasonPostData {
        private final Series                series;
        private final List<Season>          seasons;
        private final List<Episode>         episodes;
        private final List<SubtitleRelease> subtitleReleases;

        public SeasonPostData(Series series, Iterable<Season> seasons, Iterable<Episode> episodes, Iterable<SubtitleRelease> subtitleAdjustments) {
            this.series = series;
            this.seasons = ImmutableList.copyOf(seasons);
            this.episodes = ImmutableList.copyOf(episodes);
            this.subtitleReleases = ImmutableList.copyOf(subtitleAdjustments);
        }

        public Series getSeries() {
            return series;
        }

        public List<Season> getSeasons() {
            return seasons;
        }

        public List<Episode> getEpisodes() {
            return episodes;
        }

        public List<SubtitleRelease> getSubtitleReleases() {
            return subtitleReleases;
        }
    }

    private static class WorkData {
        // input
        private String                            postTopic;
        private Document                          postContent;

        // state
        private int                               currentTableNum;

        // output
        private Series                            series    = null;
        private final SortedMap<Season, Season>   seasons   = new TreeMap<>();
        private final SortedMap<Episode, Episode> episodes  = new TreeMap<>();
        private final List<SubtitleRelease>       subtitles = new ArrayList<>();

        private SeasonPostData toSeasonPostData() {
            return new SeasonPostData(series, seasons.keySet(), episodes.keySet(), subtitles);
        }
    }

    /**
     * 
     * Some values can be marked (f.e. with an asterisk *). Those markers need to be stored because they are needed to match the contributions / sources with the subtitle adjustments.
     * 
     * @param <T>
     */
    private static class MarkedValue<T> {
        private static final char MARKER_CHAR = '*';

        private final T           value;
        private final String      marker;

        private MarkedValue(T value, String marker) {
            this.value = value;
            this.marker = marker;
        }

        private boolean isMarked() {
            return marker != null;
        }

        private static MarkedValue<String> parse(String s) {
            StringBuilder marker = null;
            // Get leading marker
            for (int i = 0; i < s.length(); i++) {
                if (MARKER_CHAR != s.charAt(i)) {
                    break;
                }
                else {
                    if (marker == null) {
                        marker = new StringBuilder(2);
                    }
                    marker.append(MARKER_CHAR);
                }
            }
            if (marker != null) {
                return new MarkedValue<String>(s.substring(marker.length()), marker.toString());
            }
            // if no leading marker, get trailing marker
            for (int i = s.length() - 1; i >= 0; i--) {
                if (MARKER_CHAR != s.charAt(i)) {
                    break;
                }
                else {
                    if (marker == null) {
                        marker = new StringBuilder(2);
                    }
                    marker.append(MARKER_CHAR);
                }
            }
            if (marker != null) {
                return new MarkedValue<String>(s.substring(0, s.length() - marker.length()), marker.toString());
            }
            return new MarkedValue<String>(s, null);
        }

        private static <U> boolean anyValueMarked(Collection<MarkedValue<U>> markedValues) {
            return markedValues.stream().anyMatch(MarkedValue::isMarked);
        }

        /**
         * 
         * @param referencingValues
         *            multiple referencing values can have the same marker if they all reference to the same target
         * @param referenceTargets
         *            each referenceTarget has to have an unique marker
         * @param combiner
         *            combines the referencing value with the reference target
         */
        private static <U, V> void mapMarkedValues(Iterable<MarkedValue<U>> referencingValues, Iterable<MarkedValue<V>> referenceTargets, BiConsumer<U, V> combiner) {
            // Map<Marker->V>
            Map<String, V> markerToRefTargetsMap = new HashMap<>(2);
            for (MarkedValue<V> mv : referenceTargets) {
                V storedVal = markerToRefTargetsMap.put(mv.marker, mv.value);
                if (storedVal != null) {
                    throw new IllegalArgumentException("Multiple values marked with same marker " + mv.marker + ": " + storedVal + ", " + mv.value);
                }
            }
            for (MarkedValue<U> mv : referencingValues) {
                combiner.accept(mv.value, markerToRefTargetsMap.get(mv.marker));
            }
        }
    }
}
