package de.subcentral.mig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

	private static final Pattern					PATTERN_POST_TITLE_NUMBERED_SEASON	= Pattern.compile("(.*)\\s+-\\s*Staffel\\s+(\\d+)\\s*-\\s*.*");
	private static final Pattern					PATTERN_POST_TITLE_SPECIAL_SEASON	= Pattern.compile("(.*)\\s+-\\s*([\\w\\s]+)\\s*-\\s*.*");
	private static final Pattern					PATTERN_POST_TITLE_MULTIPLE_SEASONS	= Pattern.compile("(.*)\\s+-\\s*Staffel\\s+(\\d+)\\s+bis\\s+Staffel\\s+(\\d+)\\s*-\\s*.*");
	private static final Pattern					PATTERN_EPISODE						= Pattern.compile("E(\\d+)\\s*-\\s*(?:\\\")?(.*)(?:\\\")?");

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

	public Season getAndParse(int threadId, SubCentralApi api) throws IOException
	{
		Document doc = api.getContent("index.php?page=Thread&threadID=" + threadId);
		return parse(doc);
	}

	public Season parse(Document threadHtml)
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

	public Season parse(String postTitle, String postContent)
	{
		Season season = new Season();

		// Title
		parsePostTitle(season, postTitle);

		// Content
		Document content = Jsoup.parse(postContent, SubCentralHttpApi.getHost().toExternalForm());
		parseSeasonHeader(season, content);
		parseDescription(season, content);
		parseSubtitles(season, content);

		System.out.println(postContent);

		return season;
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
	private static void parsePostTitle(Season season, String postTitle)
	{
		Matcher numberedMatcher = PATTERN_POST_TITLE_NUMBERED_SEASON.matcher(postTitle);
		if (numberedMatcher.matches())
		{
			Series series = new Series(numberedMatcher.group(1));
			season.setSeries(series);
			Integer number = Integer.valueOf(numberedMatcher.group(2));
			season.setNumber(number);
			return;
		}

		Matcher specialMatcher = PATTERN_POST_TITLE_SPECIAL_SEASON.matcher(postTitle);
		if (specialMatcher.matches())
		{
			Series series = new Series(specialMatcher.group(1));
			season.setSeries(series);
			String title = specialMatcher.group(2);
			season.setTitle(title);
			season.setSpecial(true);
			return;
		}

		Matcher multipleMatcher = PATTERN_POST_TITLE_MULTIPLE_SEASONS.matcher(postTitle);
		if (multipleMatcher.matches())
		{
			Series series = new Series(multipleMatcher.group(1));
			season.setSeries(series);
			String title = specialMatcher.group(2);
			season.setTitle(title);
			season.setSpecial(true);
			return;
		}

		log.warn("Could not parse post title: " + postTitle);
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
	private static void parseSeasonHeader(Season season, Document postContent)
	{
		Element headerImg = postContent.select("div.tbild > img").first();
		if (headerImg != null)
		{
			String headerUrl = headerImg.absUrl("src");
			season.getImages().put(Migration.IMG_TYPE_SEASON_HEADER, headerUrl);
		}
		else
		{
			log.warn("Could not find season header image");
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
	private static void parseDescription(Season season, Document postContent)
	{
		Elements descriptionDivs = postContent.select("div.inhalt, div.websites");
		StringJoiner joiner = new StringJoiner("\n");
		for (Element div : descriptionDivs)
		{
			joiner.add(div.html());
		}
		if (joiner.length() > 0)
		{
			season.setDescription(joiner.toString());
		}
	}

	private static void parseSubtitles(Season season, Document postContent)
	{
		Elements tables = postContent.getElementsByTag("table");
		for (Element table : tables)
		{
			boolean success = parseStandardTable(season, table);
			if (!success)
			{
				parseNonStandardTable(season, table);
			}
		}
	}

	private static boolean parseStandardTable(Season season, Element table)
	{
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
			if (ColumnType.UNKNOWN == colType)
			{
				return false;
			}
			columns[i] = colType;
		}
		Element tbody = table.getElementsByTag("tbody").first();
		if (tbody == null)
		{
			return false;
		}
		for (Element tr : tbody.getElementsByTag("tr"))
		{
			boolean success = parseStandardTableRow(season, tr, columns);
			if (!success)
			{
				return false;
			}
		}
		return true;
	}

	private static boolean parseStandardTableRow(Season season, Element tr, ColumnType[] columns)
	{
		Episode epi = null;
		// Each top-level list represents a column, each nested List represents the subtitles in that column
		List<List<MarkedValue<SubtitleAdjustment>>> subAdjColumns = new ArrayList<List<MarkedValue<SubtitleAdjustment>>>();
		// A list of contributor lists
		List<List<MarkedValue<Contribution>>> contributionColumns = new ArrayList<List<MarkedValue<Contribution>>>();
		String source = null;

		Elements tdElems = tr.getElementsByTag("td");
		for (int i = 0; i < tdElems.size(); i++)
		{
			Element td = tdElems.get(i);
			ColumnType colType = columns[i];
			switch (colType)
			{
				case EPISODE:
				{
					Matcher epiMatcher = PATTERN_EPISODE.matcher(td.text());
					if (epiMatcher.matches())
					{
						Integer numberInSeason = Integer.valueOf(epiMatcher.group(1));
						String title = epiMatcher.group(2);
						epi = season.newEpisode(numberInSeason, title);
					}
					break;
				}
				case GERMAN_SUBS:
					// fall through
				case ENGLISH_SUBS:
					subAdjColumns.add(parseSubtitleAdjustmentCell(td, colType));
					break;
				case TRANSLATION:
					// fall through
				case REVISION:
					// fall through
				case ADJUSTMENT:
					// contributionColumns.addAll(parseCo)
					break;
				case SOURCE:
				{
					source = parseSourceCell(td);
					break;
				}
				default:
				{
					return false;
				}
			}
		}
		return true;
	}

	private static List<MarkedValue<SubtitleAdjustment>> parseSubtitleAdjustmentCell(Element td, ColumnType colType)
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

			System.out.println(subAdj);

			MarkedValue<SubtitleAdjustment> markedSubAdj = new MarkedValue<>(subAdj, marker);

			subAdjs.add(markedSubAdj);
		}

		return subAdjs;
	}

	private static List<MarkedValue<Contribution>> parseContributionCell(Element td, ColumnType colType)
	{
		return null;
	}

	private static String parseSourceCell(Element td)
	{
		return td.text();
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

	private static void parseNonStandardTable(Season season, Element table)
	{
		System.out.println("Non standard table");
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
