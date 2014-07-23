package de.subcentral.impl.predb;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
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
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
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

		// If parent cat = TV, and child cat = SD -> section=TV-SD
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

			String detailsUrl = titleAnchor.attr("abs:href");
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

		Element nukedSpan = rlsDiv.select("span[class*=nuked]").first();
		if (nukedSpan != null)
		{
			String nukeSpanTitle = nukedSpan.attr("title");
			Pattern nukeReasonPattern = Pattern.compile("Nuked:\\s*(.+)", Pattern.CASE_INSENSITIVE);
			Matcher mNukeReason = nukeReasonPattern.matcher(nukeSpanTitle);
			if (mNukeReason.matches())
			{
				Releases.nuke(rls, mNukeReason.group(1));
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
				else if ("Title".equals(key))
				{
					mediaTitle = value;
				}
				else if ("Episode".equals(key))
				{
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
					media = epi;
				}
				else if ("Plot".equals(key))
				{
					if (media == null)
					{
						// do nothing
					}
					else if (media instanceof Episode)
					{
						((Episode) media).setDescription(value);
					}
					else if (media instanceof Movie)
					{
						((Movie) media).setDescription(value);
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
		}

		rls.setSingleMedia(media);
		return rls;
	}
}
