package de.subcentral.core.impl.to.xrel;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.AbstractHttpHtmlLookupQuery;
import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Media;
import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Release;

public class XRelLookupQuery extends AbstractHttpHtmlLookupQuery<MediaRelease>
{
	/**
	 * The date format is a german date and time string. Example: "09.01.14 04:14 Uhr"
	 */
	private static final DateTimeFormatter	DATE_TIME_FORMATTER	= DateTimeFormatter.ofPattern("dd.MM.yy HH:mm 'Uhr'", Locale.US);
	/**
	 * The server zime zone is Europe/Berlin.
	 */
	private static final ZoneId				TIME_ZONE			= ZoneId.of("Europe/Berlin");

	public XRelLookupQuery(URL url)
	{
		super(url);

	}

	@Override
	public List<MediaRelease> getResults(Document doc)
	{
		return parseReleases(getUrl(), doc);
	}

	/**
	 * <pre>
	 * 	<div id="search_result_frame">
	 * 		<div class="release_item release_us">
	 * 		...
	 * 		</div>
	 * 		...
	 * 	</div>
	 * </pre>
	 * 
	 * @param url
	 * @param doc
	 * @return
	 */
	public static List<MediaRelease> parseReleases(URL url, Document doc)
	{
		Element resultFrameDiv = doc.getElementById("search_result_frame");
		if (resultFrameDiv == null)
		{
			return ImmutableList.of();
		}
		// Search for elements with tag "div" and class "release_item" on the children list
		// If searched in resultFrameDiv, the resultFrameDiv itself will be returned too, if it also matches the criteria.
		Elements rlsDivs = resultFrameDiv.children().select("div.release_item");
		List<MediaRelease> rlss = new ArrayList<MediaRelease>(rlsDivs.size());
		for (Element rlsDiv : rlsDivs)
		{
			MediaRelease rls = parseRelease(url, rlsDiv);
			if (rls != null)
			{
				rlss.add(rls);
			}
		}
		return rlss;
	}

	/**
	 * Nuked and cropped Group name
	 * 
	 * <pre>
	 *  <div class="release_item release_us">
	 *             <div class="release_cat" style="background-image: url('//static.xrel.to/static/img/icons/cat/tv.png');">
	 *               <br />
	 *               <a href="/tvseries-release-list.html" class="sub_link"><span>Serie</span></a>
	 *             </div>
	 * 
	 *             <div class="release_date"> 09.01.14<br />
	 *               <span class="sub">04:14 Uhr</span>
	 *             </div>
	 * 
	 *             <div class="release_title">
	 *               <img src="//static.xrel.to/static/img/release_state/nuked.png" alt="" class="_nuke_icon" />
	 *               <a href="/tv/21258/Psych.html">Psych</a>
	 *               <span class= "sub">(S08 E01)</span><br />
	 *               <a href="/tv-nfo/730264/Psych-S08E01-HDTV-x264-EXCELLENCE.html" class="sub_link">
	 *               	<span id="_title730264">Psych.S08E01.HDTV.x264-EXCELLENCE</span>
	 *               </a>
	 *             </div>
	 * 
	 *             <div class="release_options">
	 *               <a href="/tv-nfo/730264/Psych-S08E01-HDTV-x264-EXCELLENCE.html" title="NFO ansehen">
	 *               	<img src="//static.xrel.to/static/img/icons/nfo.png" alt="NFO ansehen" />
	 *               </a>
	 *             </div>
	 * 
	 *             <div class="release_type">
	 *               HDTV<br />
	 *               <span class="sub">Stereo</span>
	 *             </div>
	 * 
	 *             <div class="release_grp">
	 *               <a href="/group-EXCELLENCE-release-list.html" title="EXCELLENCE">EXCELLEN...</a><br />
	 *               <span class="sub">300 MB</span>
	 *             </div>
	 * 
	 *             <div class="release_comments">
	 *               <a href= "/comments/730264/Psych-S08E01-HDTV-x264-EXCELLENCE.html">Kommentare (0)</a><br />
	 *               <span class="sub">- / - / <span class="rating_10">8,7</span></span>
	 *             </div>
	 *           </div>
	 * </pre>
	 * 
	 * @param url
	 * @param rlsDiv
	 * @return
	 */
	public static MediaRelease parseRelease(URL url, Element rlsDiv)
	{
		MediaRelease rls = new MediaRelease();

		/**
		 * media
		 * 
		 * <pre>
		 * <div class="release_cat" style="background-image: url('//static.xrel.to/static/img/icons/cat/tv.png');">
		 * 	<br />
		 * 	<a href="/tvseries-release-list.html" class="sub_link"><span>Serie</span></a>
		 * </div>
		 * </pre>
		 */
		Element categoryDiv = rlsDiv.select("div.release_cat").first();
		String category = categoryDiv.text();

		/**
		 * date
		 * 
		 * <pre>
		 * <div class="release_date"> 09.01.14<br /><span class="sub">04:14 Uhr</span> </div>
		 * </pre>
		 */
		Element dateDiv = rlsDiv.getElementsByClass("release_date").first();
		if (dateDiv != null)
		{
			String dateStr = dateDiv.ownText().trim();
			Element timeSpan = dateDiv.getElementsByTag("span").first();
			String timeStr = timeSpan.text().trim();
			ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.parse(dateStr + " " + timeStr, DATE_TIME_FORMATTER), TIME_ZONE);
			rls.setDate(dateTime);
		}

		/**
		 * nukeReason, media, name
		 * 
		 * <pre>
		 * <div class="release_title">
		 * 	<img src="//static.xrel.to/static/img/release_state/nuked.png" alt="" class="_nuke_icon" />
		 * 	<a href="/tv/21258/Psych.html">Psych</a>
		 * 	<span class= "sub">(S08 E01)</span><br />
		 *  	<a href="/tv-nfo/730264/Psych-S08E01-HDTV-x264-EXCELLENCE.html" class="sub_link">
		 *       	<span id="_title730264">Psych.S08E01.HDTV.x264-EXCELLENCE</span>
		 * 		</a>
		 * </div>
		 * </pre>
		 */
		// div class can be "release_title" or "release_title_p2p"
		// -> query for <div>s which class attribute starts with "release_title"
		Element titleDiv = rlsDiv.select("div[class^=release_title]").first();

		// if nuked, a nuke icon is present
		Element nukeImg = titleDiv.select("img._nuke_icon").first();
		if (nukeImg != null)
		{
			rls.setNukeReason(Release.UNKNOWN_NUKE_REASON);
		}
		Element nukeReasonSpan = titleDiv.select("span._nuke_icon_dummy").first();
		if (nukeReasonSpan != null)
		{
			rls.setNukeReason(nukeReasonSpan.attr("title"));
		}

		// Get the info about the media
		Element mediaAnchor = titleDiv.getElementsByTag("a").first();
		String mediaUrl = mediaAnchor.attr("href");
		Pattern mediaSectionPattern = Pattern.compile("/([^/]+)/.*");
		Matcher mMediaSection = mediaSectionPattern.matcher(mediaUrl);
		String mediaSection = null;
		if (mMediaSection.matches())
		{
			mediaSection = mMediaSection.group(1);
		}
		String mediaTitle = mediaAnchor.text();

		// If series, season and episode number are printed
		Elements spans = titleDiv.getElementsByTag("span");
		Pattern seasonAndEpisodeNumsPattern = Pattern.compile("\\(S(\\d{2}) E(\\d{2})\\)");
		String seasonNumber = null;
		String episodeNumber = null;
		for (Element span : spans)
		{
			Matcher mSeasonEpisode = seasonAndEpisodeNumsPattern.matcher(span.text());
			if (mSeasonEpisode.matches())
			{
				seasonNumber = mSeasonEpisode.group(1);
				episodeNumber = mSeasonEpisode.group(2);
				break;
			}
		}

		Media media = parseMedia(category, mediaSection, mediaTitle, seasonNumber, episodeNumber);
		rls.setMaterial(media);

		// If the title (release name) is too long, the title in the titleIdSpan is cropped
		// and the uncropped is in the "title" attribute of the nfoAnchor.
		// second anchor is the nfoAnchor
		Element nfoAnchor = titleDiv.getElementsByTag("a").get(1);
		String title = nfoAnchor.attr("title");
		if (title.isEmpty())
		{
			Element titleIdSpan = titleDiv.select("span[id^=_title]").first();
			title = titleIdSpan.text();
		}
		rls.setName(title);

		/**
		 * group
		 * 
		 * <pre>
		 * <div class="release_grp">
		 *  	<a href="/group-EXCELLENCE-release-list.html" title="EXCELLENCE">EXCELLEN...</a><br />
		 * 	<span class="sub">300 MB</span>
		 * </div>
		 * </pre>
		 */
		Element groupDiv = rlsDiv.select("div.release_grp").first();
		Element groupAnchor = groupDiv.getElementsByTag("a").first();
		// If the group name is too long, the text in the groupAnchor is cropped
		// and the uncropped is in the "title" attribute of the groupAnchor.
		String groupName = groupAnchor.attr("title");
		if (groupName.isEmpty())
		{
			groupName = groupAnchor.text();
		}
		Group group = new Group(groupName);
		rls.setGroup(group);

		return rls;
	}

	private static Media parseMedia(String releaseCategory, String mediaSection, String title, String seasonNumber, String episodeNumber)
	{
		if (seasonNumber != null && episodeNumber != null)
		{
			return Episode.newSeasonedEpisode(title, Integer.parseInt(seasonNumber), Integer.parseInt(episodeNumber));
		}
		return null;
	}
}
