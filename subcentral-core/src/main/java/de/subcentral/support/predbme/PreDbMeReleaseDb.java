package de.subcentral.support.predbme;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.db.AbstractHtmlHttpMetadataDb;
import de.subcentral.core.metadata.db.MetadataDbQueryException;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.RegularMedia;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Unnuke;
import de.subcentral.core.util.ByteUtil;

/**
 * @implSpec #immutable #thread-safe
 */
public class PreDbMeReleaseDb extends AbstractHtmlHttpMetadataDb<Release>
{
	public static final String DOMAIN = "predb.me";

	private static final Logger log = LogManager.getLogger(PreDbMeReleaseDb.class);

	// DateTimeFormatter not needed because using the epoch seconds
	// /**
	// * The release dates are ISO-formatted with an @. Example: "2011-11-10 @ 09:16:10 ( UTC )"
	// */
	// private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd '@' HH:mm:ss", Locale.US);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId TIME_ZONE = ZoneId.of("UTC");

	/**
	 * Whether the detailed page should be opened and parsed. TODO: make it configurable.
	 */
	private boolean parseDetails = false;

	@Override
	public String getName()
	{
		return "PreDB.me";
	}

	@Override
	public String getDomain()
	{
		return DOMAIN;
	}

	@Override
	protected URL initHost() throws MalformedURLException
	{
		return new URL("http://www.predb.me/");
	}

	@Override
	public Class<Release> getResultType()
	{
		return Release.class;
	}

	@Override
	protected URL buildQueryUrl(String query) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException
	{
		return buildUrl("/", formatQuery("search=", query));
	}

	@Override
	public List<Release> queryDocument(Document doc) throws MetadataDbQueryException
	{
		if (doc == null)
		{
			return ImmutableList.of();
		}
		try
		{
			return parseReleases(doc);
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, doc, e);
		}
	}

	@Override
	public Release get(String id) throws MetadataDbQueryException
	{
		Document doc = null;
		try
		{
			String url = "http://predb.me?post=" + id;
			doc = getDocument(new URL(url));
			Release rls = new Release();
			parseReleaseDetails(doc, rls);
			return rls;
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, doc, e);
		}
	}

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
	private List<Release> parseReleases(Document doc) throws MalformedURLException, IOException
	{
		Elements rlsDivs = doc.getElementsByClass("post");
		if (rlsDivs.isEmpty())
		{
			return ImmutableList.of();
		}
		List<Release> rlss = new ArrayList<Release>(rlsDivs.size());
		for (Element rlsDiv : rlsDivs)
		{
			Release rls = parseRelease(doc, rlsDiv);
			rlss.add(rls);
		}
		return rlss;
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
	private Release parseRelease(Document doc, Element rlsDiv) throws MalformedURLException, IOException
	{
		// the url where more details can be retrieved. Filled and used later
		String detailsUrl = null;
		Release rls = new Release();

		// select span that class attribute contains "time"
		Element timeSpan = rlsDiv.select("span[class*=time]").first();
		if (timeSpan != null)
		{
			try
			{
				// Note: attribute "data" stores the seconds since epoch
				long epochSec = Long.parseLong(timeSpan.attr("data"));
				ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSec), TIME_ZONE);
				rls.setDate(date);
			}
			catch (NumberFormatException e)
			{
				log.warn("Could not parse release date epoch seconds string '" + timeSpan.attr("data") + "'", e);
			}
		}

		// Example: If parent cat = TV, and child cat = SD -> section=TV-SD
		Element catSpan = rlsDiv.select("span[class*=cat]").first();
		if (catSpan != null)
		{
			Elements catAnchors = catSpan.getElementsByTag("a");
			List<String> cats = new ArrayList<>(2);
			for (Element catAnchor : catAnchors)
			{
				cats.add(catAnchor.text());
			}
			rls.setCategory(Joiner.on('-').join(cats));
		}

		Element titleAnchor = rlsDiv.select("a[class*=title]").first();
		if (titleAnchor != null)
		{
			String title = titleAnchor.text();
			rls.setName(title);

			detailsUrl = titleAnchor.attr("abs:href");
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

		// Parse details
		if (parseDetails && detailsUrl != null)
		{
			Document detailsDoc = getDocument(new URL(detailsUrl));
			parseReleaseDetails(detailsDoc, rls);
		}

		return rls;
	}

	/**
	 * Only parses the information that are not in the overview (size, fileCount, media, lang)
	 * 
	 * Nuked:
	 * 
	 * <pre>
	 * <div class="content"> <div class="post-list single"> <div class="pl-body"> <div class="post block"> <div class="p-head block-head"> <div class=
	 * "p-c p-c-time" > <span class="p-time" data="1320916570" title= "2011-11-10 @ 09:16:10 ( UTC )" ><span class='t-d'>2011-Nov-10</span></span> </div> <div class="p-c p-c-cat"> <span class=
	 * "p-cat c-5 c-6 " ><a href="http://predb.me?cats=tv" class="c-adult">TV</a> <a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span> </div> <div class="p-c p-c-title">
	 * <h1><span class="p-title">Psych.S06E05.HDTV.XviD-P0W4</span></h1> </div> </div> <div class="p-body"> <div alt='' class='pb-img placeholder'></div> <div class=
	 * "post-body-table img-adjacent" > <div class="pb-r "> <div class= "pb-c  pb-l">Rlsname</div> <div class="pb-c  pb-d">Psych.S06E05.HDTV.XviD-P0W4</div> </div> <div class="pb-r "> <div class=
	 * "pb-c  pb-l" >Group</div> <div class= "pb-c  pb-d"> <a class='term t-g' href='http://predb.me/?group=p0w4'>P0W4</a> </div> </div> <div class= "pb-r " > <div class="pb-c  pb-l">Tags</div>
	 * <div class= "pb-c  pb-d" > <a class='term' href='http://predb.me/?tag=aired'>Aired</a>, <a class='term' href='http://predb.me/?tag=xvid'>XviD</a> </div> </div> <div class="pb-r "> <div class=
	 * "pb-c  pb-l">Size</div> <div class="pb-c  pb-d"> 349.3 MB <small>in</small> 25 <small>files</small> </div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Nukes</div> <div class=
	 * "pb-c  pb-d" >
	 * <ol class='nuke-list'> <li value='1'><span class='nuked'>contains.promo.38m.57s.to.39m.17s_get.FQM.proper</span></li> </ol> </div> </div> <div class="pb-r ">
	 * <div class="pb-c  pb-l pb-s"></div> <div class="pb-c  pb-d pb-s"></div> </div> <div class="pb-r "> <div class="pb-c  pb-l">Title</div> <div class=
	 * "pb-c  pb-d" > <a class='term t-t' href='http://predb.me/?title=psych'>Psych</a> </div> </div> <div class="pb-r "> <div class= "pb-c  pb-l">Episode</div> <div class="pb-c  pb-d"> (
	 * <a class='term t-s' href='http://predb.me/?season=6&title=psych'>S06</a> - <a class='term t-e' href='http://predb.me/?episode=5&season=6&title=psych'>E05</a> ) </div> </div> <div class=
	 * "pb-r " > <div class="pb-c  pb-l pb-s"></div> <div class="pb-c  pb-d pb-s"></div> </div> <div class="pb-r "> <div class="pb-c  pb-l pb-e">Links</div> <div class=
	 * "pb-c  pb-d pb-e" > <small>&middot;&middot;&middot;</small> </div> </div> <div class="pb-r pb-search"> <div class="pb-c  pb-l">Search</div> <div class=
	 * "pb-c  pb-d" > <a rel='nofollow' target='_blank' class='ext-link' href='http://thepiratebay.se/search/Psych.S06E05.HDTV.XviD-P0W4'>Torrent</a>, <a rel='nofollow' target='_blank'
	 * class='ext-link' href='http://nzbindex.nl/search/?q=Psych.S06E05.HDTV.XviD-P0W4'>Usenet</a> </div> </div> </div> </div> </div> </div> </div> </div> </pre>
	 * 
	 * @param doc
	 * @param rls
	 * @return
	 * @throws IOException
	 */
	protected Release parseReleaseDetails(Document doc, Release rls)
	{
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
		List<String> seriesInfoUrls = null;
		for (Element keyValueDiv : keyValueDivs)
		{
			Element keyDiv = keyValueDiv.getElementsByClass("pb-l").first();
			Element valueDiv = keyValueDiv.getElementsByClass("pb-d").first();
			if (keyDiv != null && valueDiv != null)
			{
				String key = keyDiv.text();
				String value = valueDiv.text();
				if ("Group".equals(key))
				{
					Group grp = new Group(value);
					rls.setGroup(grp);
				}
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
					 * <span class='t-u'>days</span></small></li> </ol> </div> </div> </div> </pre>
					 * 
					 * Unnuked <pre> <div class="pb-c  pb-l">
					 * Nukes</div> <div class="pb-c  pb-d">
					 * <ol class='nuke-list'> <li value='2'><span class='unnuked'>fine_ar.is.acceptable</span> <small class='nuke-time' data='1363455322'>- <span
					 * class='t-d'>2013-Mar-16</span></small></li> <li value='1'><span class='unnuked'>ar.is.within.rules</span> <small class='nuke-time' data='1363301166'>- <span
					 * class='t-d'>2013-Mar-14</span></small></li> </ol> </div> </pre>
					 */

					Element nukeListOl = valueDiv.getElementsByClass("nuke-list").first();
					if (nukeListOl != null)
					{
						Elements nukeLis = nukeListOl.getElementsByTag("li");
						List<Nuke> nukes = new ArrayList<>(nukeLis.size());
						List<Unnuke> unnukes = new ArrayList<>(nukeLis.size());
						for (Element nukeLi : nukeLis)
						{
							String nukeReason = null;
							String unnukeReason = null;
							Element nukedSpan = nukeLi.getElementsByClass("nuked").first();
							if (nukedSpan != null)
							{
								nukeReason = nukedSpan.text();
							}
							Element unnukedSpan = nukeLi.getElementsByClass("unnuked").first();
							if (unnukedSpan != null)
							{
								unnukeReason = unnukedSpan.text();
							}

							ZonedDateTime nukeDate = null;
							Element nukeTimeElem = nukeLi.getElementsByClass("nuke-time").first();
							if (nukeTimeElem != null)
							{
								try
								{
									double nukeTimeEpochSeconds = Double.parseDouble(nukeTimeElem.attr("data"));
									long nukeTimeEpochMillis = (long) nukeTimeEpochSeconds * 1000L;
									nukeDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nukeTimeEpochMillis), TIME_ZONE);
								}
								catch (NumberFormatException e)
								{
									log.warn("Could not parse release nuke date epoch seconds string '" + nukeTimeElem.attr("data") + "'", e);
								}
							}
							if (nukeReason != null)
							{
								nukes.add(new Nuke(nukeReason, nukeDate));
							}
							else if (unnukeReason != null)
							{
								unnukes.add(new Unnuke(unnukeReason, nukeDate));
							}
							else
							{
								log.warn("No nuke reason given: " + nukeLi);
							}
						}
						rls.setNukes(nukes);
						rls.setUnnukes(unnukes);
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
						epi.getFurtherInfo().add(episodeTitleAnchor.attr("href"));
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
					 * <div class="pb-c  pb-l">Genres</div> <div class= "pb-c  pb-d" > <a class='term t-gn' href='http://predb.me/?genre=trance'>Trance</a> </div> </div> </pre>
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
					 * href='http://en.wikipedia.org/wiki/ICarly'>Wikipedia</a> </div> </pre>
					 */
					Elements extLinksAnchors = valueDiv.select("a.ext-link");
					seriesInfoUrls = new ArrayList<>(extLinksAnchors.size());
					for (Element extLinkAnchor : extLinksAnchors)
					{
						seriesInfoUrls.add(extLinkAnchor.attr("href"));
					}
				}
			}
		}

		// If media was not determined yet (e.g. Episode),
		// try to determine it via the section (category)
		if (media == null && rls.getCategory() != null)
		{
			String section = rls.getCategory();
			if (section.startsWith("Movies"))
			{
				RegularMedia movie = new RegularMedia(mediaTitle);
				movie.setMediaType(Media.MEDIA_TYPE_MOVIE);
				movie.setMediaContentType(Media.MEDIA_CONTENT_TYPE_VIDEO);
				media = movie;
			}
			else if (section.startsWith("Music"))
			{
				RegularMedia regularMedia = new RegularMedia(mediaTitle);
				regularMedia.setMediaContentType(Media.MEDIA_CONTENT_TYPE_AUDIO);
				media = regularMedia;
			}
			else if (section.startsWith("TV"))
			{
				RegularMedia regularMedia = new RegularMedia(mediaTitle);
				regularMedia.setMediaContentType(Media.MEDIA_CONTENT_TYPE_VIDEO);
				media = regularMedia;
			}
			else
			{
				media = new RegularMedia(mediaTitle);
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
			if (seriesInfoUrls != null)
			{
				// the ext-links for episode releases belong to the series
				epi.getSeries().getFurtherInfo().addAll(seriesInfoUrls);
			}
			if (mediaImageUrl != null)
			{
				epi.getSeries().getImages().put(Media.MEDIA_IMAGE_TYPE_POSTER_HORIZONTAL, mediaImageUrl);
			}
		}
		else if (media instanceof RegularMedia)
		{
			// also for RegularMedia
			RegularMedia regularMediaItem = (RegularMedia) media;
			if (plot != null)
			{
				regularMediaItem.setDescription(plot);
			}
			if (genres != null)
			{
				regularMediaItem.getGenres().addAll(genres);
			}
			if (seriesInfoUrls != null)
			{
				regularMediaItem.getFurtherInfo().addAll(seriesInfoUrls);
			}
			if (mediaImageUrl != null)
			{
				regularMediaItem.getImages().put(Media.MEDIA_IMAGE_TYPE_POSTER_VERTICAL, mediaImageUrl);
			}
		}

		rls.setSingleMedia(media);

		return rls;
	}

}
