package de.subcentral.mig.parse;

import java.io.IOException;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.mig.Migration;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.woltlab.WoltlabBurningBoard.WbbPost;

public class SeriesListParser {
	private static final Logger	log	= LogManager.getLogger(SeriesListParser.class);

	private static final String	URL	= "http://subcentral.de/index.php?page=Thread&postID=29261";

	public SeriesListData getAndParse() throws IOException {
		return parseThreadPage(Jsoup.parse(new URL(URL), Migration.TIMEOUT_MILLIS));
	}

	public SeriesListData parseThreadPage(Document thread) {
		return parsePost(thread.outerHtml());
	}

	public SeriesListData parsePost(WbbPost post) {
		return parsePost(post.getMessage());
	}

	public SeriesListData parsePost(String postMessage) {
		Document doc = Jsoup.parse(postMessage, SubCentralDe.getSite().getLink());

		final Pattern boardIdPattern = Pattern.compile("boardID=(\\d+)");
		final Pattern threadIdPattern = Pattern.compile("threadID=(\\d+)");
		final Pattern yearsPattern = Pattern.compile("(\\d+)(?:-)?(\\d+)?");
		// (1+2)
		final Pattern seasonAdditionPattern = Pattern.compile("(\\d+)\\s*\\+\\s*(\\d+)");
		// (1-2)
		final Pattern seasonRangePattern = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)");
		//
		final Splitter listSplitter = Splitter.on(CharMatcher.anyOf("/,+")).trimResults().omitEmptyStrings();

		final SortedSet<Series> seriesList = new TreeSet<>();
		final SortedSet<Season> seasonList = new TreeSet<>();
		// have to use a map for putIfAbsent() method
		final SortedMap<Network, Network> networkList = new TreeMap<>();

		Element table = doc.getElementById("qltable");
		Elements rows = table.getElementsByTag("tr");
		for (Element row : rows) {
			Elements cells = row.getElementsByTag("td");
			if (!cells.isEmpty()) {
				Iterator<Element> iter = cells.iterator();
				Series series = new Series();

				/**
				 * Series (name, boardId, logo):<br/>
				 * 
				 * <pre>
				 * <td class="vorschau">10 Things I Hate About You
				 * <a href="index.php?page=WbbBoard&boardID=427" ><img src="bilder/sc_logo_10things.jpg" height="35" width="220" /></a></td>
				 * </pre>
				 */
				Element seriesCell = iter.next();
				series.setName(seriesCell.text());
				String boardUrl = seriesCell.getElementsByTag("a").first().attr("href");
				Matcher boardIdMatcher = boardIdPattern.matcher(boardUrl);
				if (boardIdMatcher.find()) {
					series.getAttributes().put(Migration.SERIES_ATTR_BOARD_ID, Integer.parseInt(boardIdMatcher.group(1)));
				}
				else {
					log.warn("Couldn't find a board ID for series {}. Content of series cell: {}", series.getName(), seriesCell);
				}
				series.getImages().put(Migration.SERIES_IMG_TYPE_LOGO, seriesCell.getElementsByTag("img").attr("src"));

				/**
				 * Years:<br/>
				 * 
				 * <pre>
				 * <td>2009-2010</td>
				 * </pre>
				 */
				Element yearsCell = iter.next();
				Matcher yearsMatcher = yearsPattern.matcher(yearsCell.text());
				if (yearsMatcher.find()) {
					String startYear = yearsMatcher.group(1);
					series.setDate(Year.parse(startYear));
					String endYear = yearsMatcher.group(2);
					if (endYear != null) {
						series.setFinaleDate(Year.parse(endYear));
					}
				}
				else {
					log.warn("Couldn't parse years for series {}. Content of years cell: {}", series.getName(), yearsCell);
				}

				/**
				 * Seasons:<br/>
				 * 
				 * <pre>
				 * <td>(<a href="index.php?page=WbbThread&threadID=10476" >1</a>)</td>
				 * </pre>
				 */
				Element seasonsCell = iter.next();
				Elements seasonAnchors = seasonsCell.getElementsByTag("a");
				for (Element seasonAnchor : seasonAnchors) {
					List<Season> seasonsInAnchor = new ArrayList<>();
					String seasonLabel = seasonAnchor.text();
					try {
						seasonsInAnchor.add(series.newSeason(Integer.parseInt(seasonLabel)));
					}
					catch (NumberFormatException e) {
						// try to parse the season label
						for (;;) {
							Matcher seasonAdditionMatcher = seasonAdditionPattern.matcher(seasonLabel);
							if (seasonAdditionMatcher.find()) {
								seasonsInAnchor.add(series.newSeason(Integer.parseInt(seasonAdditionMatcher.group(1))));
								seasonsInAnchor.add(series.newSeason(Integer.parseInt(seasonAdditionMatcher.group(2))));
								break;
							}

							Matcher seasonRangeMatcher = seasonRangePattern.matcher(seasonLabel);
							if (seasonRangeMatcher.find()) {
								int start = Integer.parseInt(seasonRangeMatcher.group(1));
								int end = Integer.parseInt(seasonRangeMatcher.group(2));
								for (int i = start; i <= end; i++) {
									seasonsInAnchor.add(series.newSeason(i));
								}
								break;
							}

							Season specialSeason = series.newSeason(seasonLabel);
							specialSeason.setSpecial(true);
							seasonsInAnchor.add(specialSeason);
							break;
						}
					}

					for (Season season : seasonsInAnchor) {
						String threadUrl = seasonAnchor.attr("href");
						Matcher threadIdMatcher = threadIdPattern.matcher(threadUrl);
						if (threadIdMatcher.find()) {
							season.getAttributes().put(Migration.SEASON_ATTR_THREAD_ID, Integer.parseInt(threadIdMatcher.group(1)));
						}
						else {
							log.warn("Couldn't find a thread ID for season {}. Content of season anchor: {}", season, seasonAnchor);
						}
					}
					series.getSeasons().addAll(seasonsInAnchor);
					seasonList.addAll(seasonsInAnchor);
				}

				/**
				 * Genres:<br/>
				 * 
				 * <pre>
				 * <td>Dramedy</td>
				 * </pre>
				 * 
				 */
				Element genresCell = iter.next();
				// Genres may be delimited by '/' or ',' and/or whitespace
				series.setGenres(listSplitter.splitToList(genresCell.text()));

				// state
				iter.next();
				// type
				iter.next();

				/**
				 * Network:<br/>
				 * <pre
				 * <td>ABC Family</td>
				 * </pre>
				 */
				Element networkCell = iter.next();
				for (String networkName : listSplitter.split(networkCell.text())) {
					Network network = new Network(networkName);
					Network previousValue = networkList.putIfAbsent(network, network);
					series.getNetworks().add(previousValue == null ? network : previousValue);
				}

				seriesList.add(series);
			}
		}

		return new SeriesListData(seriesList, seasonList, networkList.keySet());
	}

	public static class SeriesListData {
		private final List<Series>	series;
		private final List<Season>	seasons;
		private final List<Network>	networks;

		public SeriesListData(Iterable<Series> series, Iterable<Season> seasons, Iterable<Network> networks) {
			this.series = ImmutableList.copyOf(series);
			this.seasons = ImmutableList.copyOf(seasons);
			this.networks = ImmutableList.copyOf(networks);
		}

		public List<Series> getSeries() {
			return series;
		}

		public List<Season> getSeasons() {
			return seasons;
		}

		public List<Network> getNetworks() {
			return networks;
		}
	}
}
