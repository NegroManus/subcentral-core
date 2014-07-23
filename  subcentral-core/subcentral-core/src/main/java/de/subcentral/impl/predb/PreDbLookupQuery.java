package de.subcentral.impl.predb;

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
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;

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
}
