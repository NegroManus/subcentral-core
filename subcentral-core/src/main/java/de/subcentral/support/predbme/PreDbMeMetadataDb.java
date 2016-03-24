package de.subcentral.support.predbme;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.db.HttpMetadataDb;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.GenericMedia;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.MediaUtil;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.media.StandaloneMedia;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.util.ByteUtil;

/**
 * @implSpec #immutable #thread-safe
 */
public class PreDbMeMetadataDb extends HttpMetadataDb
{
	private static final Logger	log			= LogManager.getLogger(PreDbMeMetadataDb.class);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId	TIME_ZONE	= ZoneId.of("UTC");

	@Override
	public Site getSite()
	{
		return PreDbMe.SITE;
	}

	@Override
	public Set<Class<?>> getSupportedRecordTypes()
	{
		return ImmutableSet.of(Release.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (recordType.isAssignableFrom(Release.class))
		{
			URL url = buildRelativeUrl("search", query);
			log.debug("Searching for releases with text query \"{}\" using url {}", query, url);
			return (List<T>) parseReleaseSearchResults(getDocument(url));
		}
		throw newRecordTypeNotSearchableException(recordType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> searchByObject(Object queryObj, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (Release.class.equals(recordType))
		{
			// Check whether the queryObj is a Media or an Iterable of a single Media
			Media media = MediaUtil.toSingletonMedia(queryObj);
			if (media != null)
			{
				if (media instanceof Episode)
				{
					Episode epi = (Episode) media;
					// Only if series name, season number and episode number are set
					// Otherwise predb.me mostly does not parse the release name properly
					if (epi.getSeries() != null && epi.getSeries().getName() != null && epi.isNumberedInSeason() && epi.isPartOfSeason() && epi.getSeason().isNumbered())
					{
						List<Release> results = new ArrayList<>();
						for (String seriesName : epi.getSeries().getAllNames())
						{
							List<Release> newResults = searchReleasesByEpisode(seriesName, epi.getSeason().getNumber(), epi.getNumberInSeason());
							ReleaseUtil.addAllDistinctByName(results, newResults);
						}
						return (List<T>) ImmutableList.copyOf(results);
					}
				}
				else if (media instanceof Season)
				{
					Season season = (Season) media;
					if (season.getSeries() != null && season.getSeries().getName() != null && season.isNumbered())
					{
						List<Release> results = new ArrayList<>();
						for (String seriesName : season.getSeries().getAllNames())
						{
							List<Release> newResults = searchReleasesBySeason(seriesName, season.getNumber());
							ReleaseUtil.addAllDistinctByName(results, newResults);
						}
						return (List<T>) ImmutableList.copyOf(results);
					}
				}
				else if (media instanceof Series)
				{
					Series series = (Series) media;
					if (series.getName() != null)
					{
						List<Release> results = new ArrayList<>();
						for (String seriesName : series.getAllNames())
						{
							List<Release> newResults = searchReleasesBySeries(seriesName);
							ReleaseUtil.addAllDistinctByName(results, newResults);
						}
						return (List<T>) ImmutableList.copyOf(results);
					}
				}
				else if (media instanceof Movie)
				{
					Movie mov = (Movie) media;
					if (mov.getName() != null)
					{
						List<Release> results = new ArrayList<>();
						for (String movName : mov.getAllNames())
						{
							List<Release> newResults = searchReleasesByMovie(movName);
							ReleaseUtil.addAllDistinctByName(results, newResults);
						}
						return (List<T>) ImmutableList.copyOf(results);
					}
				}
			}

			// Otherwise use the default implementation
			return super.searchByObject(queryObj, recordType);
		}
		throw newRecordTypeNotSearchableException(recordType);
	}

	public List<Release> searchReleasesByEpisode(String seriesName, int seasonNumber, int episodeNumber) throws IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		// IMPORTANT: DO NOT use "title" instead of search
		// because if the title is not matched exactly, predb.me just shows the main page which lists all recent results
		query.put("search", seriesName);
		query.put("season", Integer.toString(seasonNumber));
		query.put("episode", Integer.toString(episodeNumber));
		URL url = buildRelativeUrl(query.build());
		log.debug("Searching for releases by episode (seriesName={}, seasonNumber={}, episodeNumber={}) using url {}", seriesName, seasonNumber, episodeNumber, url);
		return parseReleaseSearchResults(getDocument(url));
	}

	public List<Release> searchReleasesBySeason(String seriesName, int seasonNumber) throws IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		query.put("search", seriesName);
		query.put("season", Integer.toString(seasonNumber));
		URL url = buildRelativeUrl(query.build());
		log.debug("Searching for releases by season (seriesName={}, seasonNumber={}) using url {}", seriesName, seasonNumber, url);
		return parseReleaseSearchResults(getDocument(url));
	}

	public List<Release> searchReleasesBySeries(String seriesName) throws IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		query.put("search", seriesName);
		query.put("cats", "tv");
		URL url = buildRelativeUrl(query.build());
		log.debug("Searching for releases by series (seriesName={}) using url {}", seriesName, url);
		return parseReleaseSearchResults(getDocument(url));
	}

	public List<Release> searchReleasesByMovie(String movieName) throws IOException
	{
		ImmutableMap.Builder<String, String> query = ImmutableMap.builder();
		query.put("search", movieName);
		query.put("cats", "movie");
		URL url = buildRelativeUrl(query.build());
		log.debug("Searching for releases by movie (movieName={}) using url {}", movieName, url);
		return parseReleaseSearchResults(getDocument(url));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String id, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (recordType.isAssignableFrom(Release.class))
		{
			URL url = buildRelativeUrl("post", id);
			log.debug("Getting release with id {} using url {}", id, url);
			return (T) parseReleaseRecord(getDocument(url));
		}
		throw newUnsupportedRecordTypeException(recordType);
	}

	// Not needed currently because searching with an explicit title yields strange results sometimes
	// private String normalizeTitleQueryValue(String title)
	// {
	// return NamingDefaults.getDefaultNormalizingFormatter().apply(title).replace(' ', '-');
	// }

	/**
	 * <pre>
	 * <div class="content">
	 * 			<div class="post-list">
	 * 				<div class="pl-terms">psych s06e05</div>
	 * 				<div class="pl-body">
	 * 					<div class="post" id="5043024">
	 * 					...
	 * 					</div>
	 * 					...
	 * 				</div>
	 * 			</div>
	 * 		</div>
	 * </pre>
	 * 
	 * @param doc
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	protected List<Release> parseReleaseSearchResults(Document doc)
	{
		Elements rlsDivs = doc.getElementsByClass("post");
		ImmutableList.Builder<Release> results = ImmutableList.builder();
		for (Element rlsDiv : rlsDivs)
		{
			Release rls = parseReleaseSearchResult(rlsDiv);
			results.add(rls);
		}
		return results.build();
	}

	/**
	 * Normal:
	 * 
	 * <pre>
	 * <div class="post" id="5043024">
	 * 			<div class="p-head">
	 * 					<div class="p-c p-c-time">
	 * 						<span class="p-time" data="1400827242" title="2014-05-23 @ 06:40:42 ( UTC )"><span class='t-d'>2014-May-23</span></span>
	 * 					</div>
	 * 					<div class="p-c p-c-cat">
	 * 						<span class="p-cat c-5 c-6 "><a href="http://predb.me?cats=tv" class="c-adult">TV</a><a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span>
	 * 					</div>
	 * 					<div class="p-c p-c-title">
	 * 						<h2>
	 * 							<a class="p-title" href="http://predb.me?post=5043024">Psych.S06E05.German.DVDRiP.x264-RIPLEY</a>
	 * 						</h2>
	 * 						<a rel="nofollow" href="http://predb.me?post=5043024" class="tb tb-perma" title="Visit the permanent page for this release."></a>
	 * 					</div>
	 * 				</div>
	 * 			</div>
	 * </pre>
	 * 
	 * Nuked:
	 * 
	 * <pre>
	 * 		<div class="post" id="4097303">
	 * 			<div class="p-head">
	 * 				<div class="p-c p-c-time">
	 * 					<span class="p-time" data="1320916570" title="2011-11-10 @ 09:16:10 ( UTC )"><span class='t-d'>2011-Nov-10</span></span>
	 * 				</div>
	 * 				<div class="p-c p-c-cat">
	 * 					<span class="p-cat c-5 c-6 "><a href="http://predb.me?cats=tv" class="c-adult">TV</a><a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span>
	 * 				</div>
	 * 				<div class="p-c p-c-title">
	 * 					<h2>
	 * 						<a class="p-title" href="http://predb.me?post=4097303">Psych.S06E05.HDTV.XviD-P0W4</a>
	 * 					</h2>
	 * 					<span rel="nofollow" class="tb tb-nuked" title="Nuked: contains.promo.38m.57s.to.39m.17s_get.FQM.proper"></span><a rel="nofollow" href="http://predb.me?post=4097303" class="tb tb-perma"
	 * 						title="Visit the permanent page for this release."></a>
	 * 				</div>
	 * 			</div>
	 * 		</div>
	 * </pre>
	 * 
	 * @param rlsDiv
	 * @return
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private Release parseReleaseSearchResult(Element rlsDiv)
	{
		Release rls = new Release();

		String id = rlsDiv.attr("id");
		rls.setId(PreDbMe.SITE, id);

		// the url where more details can be retrieved. Filled and used later
		String detailsUrl = null;

		// select span that class attribute contains "time"
		Element timeSpan = rlsDiv.select("span[class*=time]").first();
		rls.setDate(parseReleaseDate(timeSpan));

		Element catSpan = rlsDiv.select("span[class*=cat]").first();
		rls.setCategory(parseReleaseCategory(catSpan));

		Element titleAnchor = rlsDiv.select("a[class*=title]").first();
		if (titleAnchor != null)
		{
			String title = titleAnchor.text();
			rls.setName(title);

			detailsUrl = titleAnchor.absUrl("href");
			rls.setId(PreDbMe.SITE, parseId(titleAnchor));
		}

		/**
		 * 
		 * <pre>
		 * <span rel="nofollow" class="tb tb-nuked" title="Nuked: invalid.proper_TRiPS.AR.is.within.rules"></span>
		 * </pre>
		 * 
		 * <pre>
		 * <span rel="nofollow" class="tb tb-unnuked" title="Unnuked: fine_ar.is.acceptable"></span>
		 * </pre>
		 */
		Element nukedSpan = rlsDiv.select("span[class*=nuked]").first();
		if (nukedSpan != null)
		{
			String nukeSpanTitle = nukedSpan.attr("title");
			Pattern pUnnukeReason = Pattern.compile("Unnuked:\\s*(.+)", Pattern.CASE_INSENSITIVE);
			Matcher mUnnukeReason = pUnnukeReason.matcher(nukeSpanTitle);
			if (mUnnukeReason.matches())
			{
				rls.unnuke(mUnnukeReason.group(1));
			}
			Pattern pNukeReason = Pattern.compile("Nuked:\\s*(.+)", Pattern.CASE_INSENSITIVE);
			Matcher mNukeReason = pNukeReason.matcher(nukeSpanTitle);
			if (mNukeReason.matches())
			{
				rls.nuke(mNukeReason.group(1));
			}
		}

		rls.getFurtherInfoLinks().add(detailsUrl);
		return rls;
	}

	/**
	 * Parses a release record.
	 * 
	 * Nuked:
	 * 
	 * <pre>
	 * <div class="post block"> <div class="p-head block-head"> <div class="p-c p-c-time"> <span class="p-time" data="1320916570" title=
	 * "2011-11-10 @ 09:16:10 ( UTC )" ><span class='t-d'>2011-Nov-10</span></span> </div> <div class="p-c p-c-cat"> <span class=
	 * "p-cat c-5 c-6 " ><a href="http://predb.me?cats=tv" class="c-adult">TV</a> <a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span> </div> <div class="p-c p-c-title">
	 * <h1><span class="p-title">Psych.S06E05.HDTV.XviD-P0W4</span></h1> </div> </div> <div class="p-body"> <div alt='' class='pb-img placeholder'></div> <div class=
	 * "post-body-table img-adjacent" > <div class="pb-r "> <div class="pb-c  pb-l">Rlsname</div> <div class="pb-c  pb-d">Psych.S06E05.HDTV.XviD-P0W4</div> </div> <div class="pb-r "> <div class=
	 * "pb-c  pb-l">Group</div> <div class= "pb-c  pb-d"> <a class='term t-g' href='http://predb.me/?group=p0w4'>P0W4</a> </div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Tags</div>
	 * <div class="pb-c  pb-d" > <a class='term' href='http://predb.me/?tag=aired'>Aired</a>, <a class='term' href='http://predb.me/?tag=xvid'>XviD</a> </div> </div> <div class="pb-r "> <div class=
	 * "pb-c  pb-l">Size</div> <div class="pb-c  pb-d"> 349.3 MB <small>in</small> 25 <small>files</small> </div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Nukes</div> <div class=
	 * "pb-c  pb-d" >
	 * <ol class='nuke-list'> <li value='1'><span class='nuked'>contains.promo.38m.57s.to.39m.17s_get.FQM.proper</span></li> </ol> </div> </div> <div class="pb-r ">
	 * <div class="pb-c  pb-l pb-s"></div> <div class="pb-c  pb-d pb-s"></div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Title</div> <div class=
	 * "pb-c  pb-d" > <a class='term t-t' href='http://predb.me/?title=psych'>Psych</a> </div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Episode</div> <div class="pb-c  pb-d"> (
	 * <a class='term t-s' href='http://predb.me/?season=6&title=psych'>S06</a> - <a class='term t-e' href='http://predb.me/?episode=5&season=6&title=psych'>E05</a> ) </div> </div> <div class=
	 * "pb-r " > <div class="pb-c  pb-l pb-s"></div> <div class="pb-c  pb-d pb-s"></div> </div> <div class="pb-r "> <div class="pb-c  pb-l pb-e">Links</div> <div class=
	 * "pb-c  pb-d pb-e" > <small>&middot;&middot;&middot;</small> </div> </div> <div class="pb-r pb-search"> <div class="pb-c  pb-l">Search</div> <div class=
	 * "pb-c  pb-d" > <a rel='nofollow' target='_blank' class='ext-link' href='http://thepiratebay.se/search/Psych.S06E05.HDTV.XviD-P0W4'>Torrent</a>, <a rel='nofollow' target='_blank'
	 * class='ext-link' href='http://nzbindex.nl/search/?q=Psych.S06E05.HDTV.XviD-P0W4'>Usenet</a> </div> </div> </div> </div> </div>
	 * </pre>
	 * 
	 * @param doc
	 * @param rls
	 * @return
	 * @throws IOException
	 */
	protected Release parseReleaseRecord(Document doc)
	{
		Release rls = new Release();

		/**
		 * <pre>
		 * <div class="p-c p-c-time"> <span class="p-time" data="1320916570" title="2011-11-10 @ 09:16:10 ( UTC )"><span class='t-d'>2011-Nov-10</span></span> </div>
		 * </pre>
		 */
		Element timeSpan = doc.select("span[class*=time]").first();
		rls.setDate(parseReleaseDate(timeSpan));

		/**
		 * <pre>
		<div class="p-c p-c-cat">
		<span class="p-cat c-5 c-6 "><a
			href="http://predb.me?cats=tv" class="c-adult">TV</a><a
			href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span>
		</div>
		 * </pre>
		 */
		Element catSpan = doc.select("span[class*=cat]").first();
		rls.setCategory(parseReleaseCategory(catSpan));

		/**
		 * <pre>
		 * <div class="p-body">
		 * 							<img alt='' class='pb-img' src='http://images.tvrage.com/shows/35/34590.jpg' />
		 * 							<div class="post-body-table img-adjacent">
		 * 								<div class="pb-r ">
		 * 									<div class="pb-c  pb-l">Rlsname</div>
		 * </pre>
		 */
		String mediaImageUrl = null;
		Element mediaImg = doc.select("img.pb-img").first();
		if (mediaImg != null)
		{
			mediaImageUrl = mediaImg.attr("src");
		}

		Elements keyValueDivs = doc.getElementsByClass("pb-r");
		Media media = null;
		String mediaTitle = null;
		String plot = null;
		List<String> genres = null;
		List<String> links = null;
		for (Element keyValueDiv : keyValueDivs)
		{
			Element keyDiv = keyValueDiv.getElementsByClass("pb-l").first();
			Element valueDiv = keyValueDiv.getElementsByClass("pb-d").first();
			if (keyDiv != null && valueDiv != null)
			{
				String key = keyDiv.text();
				String value = valueDiv.text();
				if ("Rlsname".equals(key))
				{
					rls.setName(value);
				}
				else if ("Group".equals(key))
				{
					Group grp = new Group(value);
					rls.setGroup(grp);
				}
				// Don't parse "Tags" because they are predb.me specific tags and not the release tags
				else if ("Lang".equals(key))
				{
					rls.getLanguages().add(value);
				}
				else if ("Size".equals(key))
				{
					String sizeTxt = value;
					Pattern pSize = Pattern.compile("([\\d\\.\\w\\s]+)\\s+in\\s+(\\d+)\\s+files");
					Matcher mSize = pSize.matcher(sizeTxt);
					if (mSize.find())
					{
						// no NumberFormatExceptions can occur because then the pattern would not match in the first place
						long size = ByteUtil.parseBytes(mSize.group(1));
						rls.setSize(size);
						int fileCount = Integer.parseInt(mSize.group(2));
						rls.setFileCount(fileCount);
					}
				}
				else if ("Nukes".equals(key))
				{
					/**
					 * <pre>
					 * <div class="pb-r "> <div class="pb-c  pb-l">Nukes</div> <div class="pb-c  pb-d">
					 * <ol class='nuke-list'> <li value='2'><span class='nuked'>get.dirfix</span> <small class='nuke-time' data='1405980397.037'>- <span class='t-n-d'>2.8</span> <span
					 * class='t-u'>days</span></small></li> <li value='1'><span class='nuked'>mislabeled.2014</span> <small class='nuke-time' data='1405980000.098'>- <span class='t-n-d'>2.8</span>
					 * <span class='t-u'>days</span></small></li> </ol> </div> </div> </div>
					 * </pre>
					 * 
					 * Unnuked
					 * 
					 * <pre>
					 *  <div class="pb-c  pb-l">
					 * Nukes</div> <div class="pb-c  pb-d">
					 * <ol class='nuke-list'> <li value='2'><span class='unnuked'>fine_ar.is.acceptable</span> <small class='nuke-time' data='1363455322'>- <span
					 * class='t-d'>2013-Mar-16</span></small></li> <li value='1'><span class='unnuked'>ar.is.within.rules</span> <small class='nuke-time' data='1363301166'>- <span
					 * class='t-d'>2013-Mar-14</span></small></li> </ol> </div>
					 * </pre>
					 */

					Element nukeListOl = valueDiv.getElementsByClass("nuke-list").first();
					if (nukeListOl != null)
					{
						Elements nukeLis = nukeListOl.getElementsByTag("li");
						List<Nuke> nukes = new ArrayList<>(nukeLis.size());
						for (Element nukeLi : nukeLis)
						{
							String nukeReason = null;
							Element nukedSpan = nukeLi.getElementsByClass("nuked").first();
							if (nukedSpan != null)
							{
								nukeReason = nukedSpan.text();
							}
							String unnukeReason = null;
							Element unnukedSpan = nukeLi.getElementsByClass("unnuked").first();
							if (unnukedSpan != null)
							{
								unnukeReason = unnukedSpan.text();
							}

							ZonedDateTime nukeDate = null;
							Element nukeTimeElem = nukeLi.getElementsByClass("nuke-time").first();
							if (nukeTimeElem != null)
							{
								nukeDate = parseTimestamp(nukeTimeElem.attr("data"));
							}

							if (nukeReason != null)
							{
								nukes.add(new Nuke(nukeReason, nukeDate));
							}
							else if (unnukeReason != null)
							{
								nukes.add(new Nuke(unnukeReason, nukeDate, true));
							}
							else
							{
								log.warn("No nuke reason given: " + nukeLi);
							}
						}
						rls.setNukes(nukes);
					}
				}
				else if ("Title".equals(key))
				{
					mediaTitle = value;
				}
				else if ("Episode".equals(key))
				{
					/**
					 * <pre>
					 * <div class="pb-c  pb-l">Episode</div> <div class="pb-c  pb-d"> (
					 * <a class='term t-s' href='http://predb.me/?season=1&title=icarly'>S01</a> - <a class='term t-e' href='http://predb.me/?episode=10&season=1&title=icarly'>E10</a> ) - <a
					 * rel='nofollow' target='_blank' class='ext-link' href='http://www.tvrage.com/iCarly/episodes/626951'>iWant a World Record</a> - <small class='airdate'>2007-11-17</small> </div>
					 * </pre>
					 */
					Series series = new Series(mediaTitle);
					Episode epi = new Episode(series);
					String seasonEpisodeTxt = value;
					Pattern pSeason = Pattern.compile("S(\\d+)");
					Matcher mSeason = pSeason.matcher(seasonEpisodeTxt);
					if (mSeason.find())
					{
						Season season = new Season(series, Integer.parseInt(mSeason.group(1)));
						epi.setSeason(season);
					}
					Pattern pEpi = Pattern.compile("E(\\d+)");
					Matcher mEpi = pEpi.matcher(seasonEpisodeTxt);
					if (mEpi.find())
					{
						epi.setNumberInSeason(Integer.parseInt(mEpi.group(1)));
					}
					// Episode title and furtherInfo
					Element episodeTitleAnchor = valueDiv.select("a.ext-link").first();
					if (episodeTitleAnchor != null)
					{
						epi.setTitle(episodeTitleAnchor.text());
						epi.getFurtherInfoLinks().add(episodeTitleAnchor.attr("href"));
					}
					Element airdateElement = valueDiv.getElementsByClass("airdate").first();
					if (airdateElement != null)
					{
						try
						{
							epi.setDate(LocalDate.parse(airdateElement.text()));
						}
						catch (DateTimeParseException e)
						{
							log.warn("Could not parse episode date string '" + airdateElement.text() + "'", e);
						}
					}
					media = epi;
				}
				else if ("Plot".equals(key))
				{
					plot = value;
				}
				else if ("Genres".equals(key))
				{
					/**
					 * <pre>
					 * <div class="pb-c  pb-l">Genres</div> <div class= "pb-c  pb-d" > <a class='term t-gn' href='http://predb.me/?genre=trance'>Trance</a> </div> </div>
					 * </pre>
					 */
					Elements genreAnchors = valueDiv.getElementsByTag("a");
					genres = new ArrayList<>(genreAnchors.size());
					for (Element genreAnchor : genreAnchors)
					{
						genres.add(genreAnchor.text());
					}
				}
				else if ("Links".equals(key))
				{
					/**
					 * <pre>
					 * <div class="pb-c  pb-l">Links</div> <div class=
					 * "pb-c  pb-d" > <a rel='nofollow' target='_blank' class='ext-link' href='http://www.tvrage.com/iCarly'>TVRage</a>, <a rel='nofollow' target='_blank' class='ext-link'
					 * href='http://en.wikipedia.org/wiki/ICarly'>Wikipedia</a> </div>
					 * </pre>
					 */
					Elements extLinksAnchors = valueDiv.select("a.ext-link");
					links = new ArrayList<>(extLinksAnchors.size());
					for (Element extLinkAnchor : extLinksAnchors)
					{
						links.add(extLinkAnchor.attr("href"));
					}
				}
			}
		}

		// If no name could be parsed, this document is no valid release record HTML
		if (rls.getName() == null)
		{
			log.warn("The parsed document did not contain valid release record HTML");
			return null;
		}

		// If media was not determined yet (e.g. Episode),
		// try to determine it via the section (category)
		if (media == null && rls.getCategory() != null)
		{
			String section = rls.getCategory();
			if (section.startsWith("movies"))
			{
				Movie movie = new Movie(mediaTitle);
				media = movie;
			}
			else if (section.startsWith("music"))
			{
				GenericMedia genericMedia = new GenericMedia(mediaTitle);
				genericMedia.setMediaContentType(Media.MEDIA_CONTENT_TYPE_AUDIO);
				media = genericMedia;
			}
			else if (section.startsWith("tv"))
			{
				GenericMedia genericMedia = new GenericMedia(mediaTitle);
				genericMedia.setMediaContentType(Media.MEDIA_CONTENT_TYPE_VIDEO);
				media = genericMedia;
			}
			else
			{
				media = new GenericMedia(mediaTitle);
			}
		}

		// set plot, genres and furtherInfoUrls if available
		if (media instanceof Episode)
		{
			Episode epi = (Episode) media;
			if (plot != null)
			{
				epi.setDescription(plot);
			}
			if (genres != null)
			{
				epi.getSeries().getGenres().addAll(genres);
			}
			if (links != null)
			{
				// the ext-links for episode releases belong to the series
				epi.getSeries().getFurtherInfoLinks().addAll(links);
			}
			if (mediaImageUrl != null)
			{
				epi.getSeries().getImages().put(Media.MEDIA_IMAGE_TYPE_POSTER_HORIZONTAL, mediaImageUrl);
			}
		}
		// For both Movie and GenericMedia
		else if (media instanceof StandaloneMedia)
		{
			StandaloneMedia regularMediaItem = (StandaloneMedia) media;
			if (plot != null)
			{
				regularMediaItem.setDescription(plot);
			}
			if (genres != null)
			{
				regularMediaItem.getGenres().addAll(genres);
			}
			if (links != null)
			{
				regularMediaItem.getFurtherInfoLinks().addAll(links);
			}
			if (mediaImageUrl != null)
			{
				regularMediaItem.getImages().put(Media.MEDIA_IMAGE_TYPE_POSTER_VERTICAL, mediaImageUrl);
			}
		}

		rls.setSingleMedia(media);

		return rls;
	}

	/**
	 * <a rel="nofollow" href="http://predb.me?post=4097303" class="tb tb-perma" title="Visit the permanent page for this release."></a>
	 */
	private static String parseId(Element postAnchor)
	{
		if (postAnchor != null)
		{
			String postLink = postAnchor.attr("href");
			return postLink.substring(postLink.indexOf("post=") + 5); // 5 = length of string "post="
		}
		return null;
	}

	private static ZonedDateTime parseReleaseDate(Element timeSpan)
	{
		if (timeSpan != null)
		{
			return parseTimestamp(timeSpan.attr("data"));
		}
		return null;
	}

	private static ZonedDateTime parseTimestamp(String epochSecond)
	{
		try
		{
			double epochSecs = Double.parseDouble(epochSecond);
			long epochMillis = (long) epochSecs * 1000L;
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), TIME_ZONE);
		}
		catch (NullPointerException | NumberFormatException | DateTimeException e)
		{
			log.warn("Could not parse epoch seconds string '" + epochSecond + "'", e);
			return null;
		}
	}

	/**
	 * Can have an adult category and a child category or just a child category. If existing, the child category is more specific.
	 * 
	 * <pre>
	 * <div class="p-c p-c-cat">
	 * 					<span class="p-cat c-5 c-6 "><a href="http://predb.me?cats=tv" class="c-adult">TV</a><a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span>
	 * 				</div>
	 * </pre>
	 */
	private static String parseReleaseCategory(Element catSpan)
	{
		Element childCatAnchor = catSpan.select("a[class=c-child]").first();
		String cat = parseCategory(childCatAnchor);
		if (cat != null)
		{
			return cat;
		}
		else
		{
			Element adultCatAnchor = catSpan.select("a[class=c-adult]").first();
			return parseCategory(adultCatAnchor);
		}
	}

	private static String parseCategory(Element categoryAnchor)
	{
		if (categoryAnchor != null)
		{
			String catLink = categoryAnchor.attr("href");
			return catLink.substring(catLink.indexOf("cats=") + 5); // 5 = length of string "cats="
		}
		return null;
	}
}
