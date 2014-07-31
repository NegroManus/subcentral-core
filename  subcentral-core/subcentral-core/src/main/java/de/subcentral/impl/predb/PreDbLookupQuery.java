package de.subcentral.impl.predb;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.AbstractHttpHtmlLookupQuery;
import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.media.StandardAvMediaItem;
import de.subcentral.core.model.media.StandardMediaItem;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Nuke;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.ByteUtil;

public class PreDbLookupQuery extends AbstractHttpHtmlLookupQuery<Release>
{
	// DateTimeFormatter not needed because using the epoch seconds
	// /**
	// * The release dates are ISO-formatted with an @. Example: "2011-11-10 @ 09:16:10 ( UTC )"
	// */
	// private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd '@' HH:mm:ss", Locale.US);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId	TIME_ZONE	= ZoneId.of("UTC");

	PreDbLookupQuery(URL url)
	{
		super(url);
	}

	@Override
	protected List<Release> getResults(Document doc)
	{
		return parseReleases(doc);
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
	 */
	private List<Release> parseReleases(Document doc)
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
			if (rls != null)
			{
				rlss.add(rls);
			}
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
	 */
	private Release parseRelease(Document doc, Element rlsDiv)
	{
		// the url where more details can be retrieved. Filled and used later
		String detailsUrl = null;
		Release rls = new Release();

		// select span that class attribute contains "time"
		Element timeSpan = rlsDiv.select("span[class*=time]").first();
		if (timeSpan != null)
		{
			// Note: attribute "data" stores the seconds since epoch
			long epochSec = Long.valueOf(timeSpan.attr("data"));
			ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSec), TIME_ZONE);
			rls.setDate(date);
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
			rls.setSection(Joiner.on('-').join(cats));
		}

		Element titleAnchor = rlsDiv.select("a[class*=title]").first();
		if (titleAnchor != null)
		{
			String title = titleAnchor.text();
			rls.setName(title);

			detailsUrl = titleAnchor.attr("abs:href");
		}

		Element nukedSpan = rlsDiv.select("span[class*=nuked]").first();
		if (nukedSpan != null)
		{
			String nukeSpanTitle = nukedSpan.attr("title");
			Pattern nukeReasonPattern = Pattern.compile("Nuked:\\s*(.+)", Pattern.CASE_INSENSITIVE);
			Matcher mNukeReason = nukeReasonPattern.matcher(nukeSpanTitle);
			if (mNukeReason.matches())
			{
				rls.nuke(mNukeReason.group(1));
			}
		}

		// Parse details
		if (detailsUrl != null)
		{
			try
			{
				Document detailsDoc = getDocument(new URL(detailsUrl));
				parseReleaseDetails(detailsDoc, rls);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return rls;
	}

	/**
	 * Only parses the information that are not in the overview (size, fileCount, media, lang)
	 * 
	 * Nuked:
	 * 
	 * <pre>
	 * <div class="content">
	 * 			<div class="post-list single">
	 * 				<div class="pl-body">
	 * 					<div class="post block">
	 * 						<div class="p-head block-head">
	 * 							<div class="p-c p-c-time">
	 * 								<span class="p-time" data="1320916570" title="2011-11-10 @ 09:16:10 ( UTC )"><span class='t-d'>2011-Nov-10</span></span>
	 * 							</div>
	 * 							<div class="p-c p-c-cat">
	 * 								<span class="p-cat c-5 c-6 "><a href="http://predb.me?cats=tv" class="c-adult">TV</a><a href="http://predb.me?cats=tv-sd" class="c-child">SD</a></span>
	 * 							</div>
	 * 							<div class="p-c p-c-title">
	 * 								<h1>
	 * 									<span class="p-title">Psych.S06E05.HDTV.XviD-P0W4</span>
	 * 								</h1>
	 * 							</div>
	 * 						</div>
	 * 						<div class="p-body">
	 * 							<div alt='' class='pb-img placeholder'></div>
	 * 							<div class="post-body-table img-adjacent">
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Rlsname</div>
	 * 									<div class="pb-c  pb-d">Psych.S06E05.HDTV.XviD-P0W4</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Group</div>
	 * 									<div class="pb-c  pb-d">
	 * 										<a class='term t-g' href='http://predb.me/?group=p0w4'>P0W4</a>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Tags</div>
	 * 									<div class="pb-c  pb-d">
	 * 										<a class='term' href='http://predb.me/?tag=aired'>Aired</a>, <a class='term' href='http://predb.me/?tag=xvid'>XviD</a>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Size</div>
	 * 									<div class="pb-c  pb-d">
	 * 										349.3 MB <small>in</small> 25 <small>files</small>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Nukes</div>
	 * 									<div class="pb-c  pb-d">
	 * 										<ol class='nuke-list'>
	 * 											<li value='1'><span class='nuked'>contains.promo.38m.57s.to.39m.17s_get.FQM.proper</span></li>
	 * 										</ol>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l pb-s"></div>
	 * 									<div class="pb-c  pb-d pb-s"></div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Title</div>
	 * 									<div class="pb-c  pb-d">
	 * 										<a class='term t-t' href='http://predb.me/?title=psych'>Psych</a>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l">Episode</div>
	 * 									<div class="pb-c  pb-d">
	 * 										( <a class='term t-s' href='http://predb.me/?season=6&title=psych'>S06</a> - <a class='term t-e' href='http://predb.me/?episode=5&season=6&title=psych'>E05</a> )
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l pb-s"></div>
	 * 									<div class="pb-c  pb-d pb-s"></div>
	 * 								</div>
	 * 								<div class="pb-r ">
	 * 									<div class="pb-c  pb-l pb-e">Links</div>
	 * 									<div class="pb-c  pb-d pb-e">
	 * 										<small>&middot;&middot;&middot;</small>
	 * 									</div>
	 * 								</div>
	 * 								<div class="pb-r pb-search">
	 * 									<div class="pb-c  pb-l">Search</div>
	 * 									<div class="pb-c  pb-d">
	 * 										<a rel='nofollow' target='_blank' class='ext-link' href='http://thepiratebay.se/search/Psych.S06E05.HDTV.XviD-P0W4'>Torrent</a>, <a rel='nofollow' target='_blank' class='ext-link'
	 * 											href='http://nzbindex.nl/search/?q=Psych.S06E05.HDTV.XviD-P0W4'>Usenet</a>
	 * 									</div>
	 * 								</div>
	 * 							</div>
	 * 						</div>
	 * 					</div>
	 * 				</div>
	 * 			</div>
	 * 		</div>
	 * </pre>
	 * 
	 * @param doc
	 * @param rls
	 * @return
	 * @throws IOException
	 */
	protected Release parseReleaseDetails(Document doc, Release rls)
	{
		Elements keyValueDivs = doc.getElementsByClass("pb-r");
		Media media = null;
		String mediaTitle = null;
		String plot = null;
		List<String> genres = null;
		List<String> furtherInfoUrls = null;
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
						long size = ByteUtil.parseBytes(mSize.group(1));
						rls.setSize(size);
						int files = Integer.parseInt(mSize.group(2));
						rls.setFileCount(files);
					}
				}
				else if ("Nukes".equals(key))
				{
					/**
					 * <pre>
					 * <div class="pb-r ">
					 * 	<div class="pb-c  pb-l">Nukes</div>
					 * 	<div class="pb-c  pb-d">
					 * 		<ol class='nuke-list'>
					 * 				<li value='2'><span class='nuked'>get.dirfix</span> <small class='nuke-time' data='1405980397.037'>- <span class='t-n-d'>2.8</span> <span class='t-u'>days</span></small></li>
					 * 				<li value='1'><span class='nuked'>mislabeled.2014</span> <small class='nuke-time' data='1405980000.098'>- <span class='t-n-d'>2.8</span> <span class='t-u'>days</span></small></li>
					 * 			</ol>
					 * 		</div>
					 * 	</div>
					 * </div>
					 * </pre>
					 */

					Element nukeListOl = valueDiv.getElementsByClass("nuke-list").first();
					if (nukeListOl != null)
					{
						Elements nukeLis = nukeListOl.getElementsByTag("li");
						List<Nuke> nukes = new ArrayList<>(nukeLis.size());
						for (Element nukeLi : nukeLis)
						{
							String nukeReason = nukeLi.getElementsByClass("nuked").first().text();
							Nuke nuke = new Nuke(nukeReason);
							Element nukeTimeSpan = nukeLi.getElementsByClass("nuke-time").first();
							if (nukeTimeSpan != null)
							{
								double nukeTimeEpochSeconds = Double.parseDouble(nukeTimeSpan.attr("data"));
								long nukeTimeEpochMillis = (long) nukeTimeEpochSeconds * 1000L;
								nuke.setDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(nukeTimeEpochMillis), TIME_ZONE));
							}
							nukes.add(nuke);
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
					 * <div class="pb-c  pb-l">Episode</div>
					 * 	<div class="pb-c  pb-d">
					 * 			( <a class='term t-s' href='http://predb.me/?season=1&title=icarly'>S01</a> - <a class='term t-e' href='http://predb.me/?episode=10&season=1&title=icarly'>E10</a> ) - <a rel='nofollow'
					 * 											target='_blank' class='ext-link' href='http://www.tvrage.com/iCarly/episodes/626951'>iWant a World Record</a> - <small class='airdate'>2007-11-17</small>
					 * 	</div>
					 * </pre>
					 */
					Series series = new Series(mediaTitle);
					Episode epi = series.newEpisode();
					String seasonEpisodeTxt = value;
					Pattern pSeason = Pattern.compile("S(\\d+)");
					Matcher mSeason = pSeason.matcher(seasonEpisodeTxt);
					if (mSeason.find())
					{
						Season season = series.newSeason(Integer.parseInt(mSeason.group(1)));
						epi.setSeason(season);
					}
					Pattern pEpi = Pattern.compile("E(\\d+)");
					Matcher mEpi = pEpi.matcher(seasonEpisodeTxt);
					if (mEpi.find())
					{
						epi.setNumberInSeason(Integer.parseInt(mEpi.group(1)));
					}
					// Episode title and furtherInfoUrl
					Element episodeTitleAnchor = valueDiv.select("a.ext-link").first();
					if (episodeTitleAnchor != null)
					{
						epi.setTitle(episodeTitleAnchor.text());
						epi.getFurtherInfoUrls().add(episodeTitleAnchor.attr("href"));
					}
					Element airdateElement = valueDiv.getElementsByClass("airdate").first();
					if (airdateElement != null)
					{
						epi.setDate(LocalDate.parse(airdateElement.text()));
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
					 * <div class="pb-c  pb-l">Genres</div>
					 * 		<div class="pb-c  pb-d">
					 * 			<a class='term t-gn' href='http://predb.me/?genre=trance'>Trance</a>
					 * 		</div>
					 * 	</div>
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
					 * <div class="pb-c  pb-l">Links</div>
					 * 	<div class="pb-c  pb-d">
					 * 			<a rel='nofollow' target='_blank' class='ext-link' href='http://www.tvrage.com/iCarly'>TVRage</a>, <a rel='nofollow' target='_blank' class='ext-link'
					 * 			href='http://en.wikipedia.org/wiki/ICarly'>Wikipedia</a>
					 * 		</div>
					 * </pre>
					 */
					Elements extLinksAnchors = valueDiv.select("a.ext-link");
					furtherInfoUrls = new ArrayList<>(extLinksAnchors.size());
					for (Element extLinkAnchor : extLinksAnchors)
					{
						furtherInfoUrls.add(extLinkAnchor.attr("href"));
					}
				}
			}
		}

		// If media was not determined yet (e.g. Episode),
		// try to determine it via the section (category)
		if (media == null && rls.getSection() != null)
		{
			String section = rls.getSection();
			if (section.startsWith("Movies"))
			{
				media = new Movie(mediaTitle);
			}
			else if (section.startsWith("Music"))
			{
				StandardAvMediaItem avMediaItem = new StandardAvMediaItem(mediaTitle);
				avMediaItem.setMediaContentType(Media.MEDIA_CONTENT_TYPE_AUDIO);
				media = avMediaItem;
			}
			else if (section.startsWith("TV"))
			{
				StandardAvMediaItem avMediaItem = new StandardAvMediaItem(mediaTitle);
				avMediaItem.setMediaContentType(Media.MEDIA_CONTENT_TYPE_VIDEO);
				media = avMediaItem;
			}
			else
			{
				media = new StandardMediaItem(mediaTitle);
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
			if (furtherInfoUrls != null)
			{
				// the ext-links for episode releases belong to the series
				epi.getSeries().getFurtherInfoUrls().addAll(furtherInfoUrls);
			}
		}
		else if (media instanceof Movie)
		{
			Movie movie = (Movie) media;
			if (plot != null)
			{
				movie.setDescription(plot);
			}
			if (genres != null)
			{
				movie.getGenres().addAll(genres);
			}
			if (furtherInfoUrls != null)
			{
				movie.getFurtherInfoUrls().addAll(furtherInfoUrls);
			}
		}
		else if (media instanceof StandardMediaItem)
		{
			// also for StandardAvMediaItem
			StandardMediaItem stdMediaItem = (StandardMediaItem) media;
			if (plot != null)
			{
				stdMediaItem.setDescription(plot);
			}
			if (genres != null)
			{
				stdMediaItem.getGenres().addAll(genres);
			}
			if (furtherInfoUrls != null)
			{
				stdMediaItem.getFurtherInfoUrls().addAll(furtherInfoUrls);
			}
		}

		rls.setSingleMedia(media);
		return rls;
	}
}
