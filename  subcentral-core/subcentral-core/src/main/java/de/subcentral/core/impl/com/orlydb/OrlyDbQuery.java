package de.subcentral.core.impl.com.orlydb;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.AbstractHttpHtmlLookupQuery;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbQuery extends AbstractHttpHtmlLookupQuery<MediaRelease>
{
	private static final DateTimeFormatter	DATE_TIME_FORMATTER	= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);
	private static final ZoneId				TIME_ZONE			= ZoneId.of("UTC");

	public OrlyDbQuery(URL url)
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
	 * <div id="releases">
	 * ...
	 * </div>
	 * </pre>
	 * 
	 * @param doc
	 * @return
	 */
	private static List<MediaRelease> parseReleases(URL url, Document doc)
	{
		Element rlssDiv = doc.getElementById("releases");
		if (rlssDiv == null)
		{
			return ImmutableList.of();
		}
		// Search for elements with tag "div" on the children list
		// If searched in rlssDiv, the rlssDiv itself will be returned too.
		Elements rlsDivs = rlssDiv.children().tagName("div");
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
	 * <pre>
	 * <div>
	 * 	<span class="timestamp">2011-11-10 04:16:48</span>
	 * 	<span class="section"><a href="/s/tv-xvid">TV-XVID</a></span>
	 * 	<span class="release">Psych.S06E05.HDTV.XviD-P0W4</span>
	 * 	<a href="/dl/Psych.S06E05.HDTV.XviD-P0W4/" class="dlright"><span class="dl">DL</span></a>
	 * 	
	 * 		<span class="inforight"><span class="info">349.3MB | 25F</span></span>
	 * 	
	 * 	
	 * 		<span class="nukeright"><span class="nuke">contains.promo.38m.57s.to.39m.17s_get.FQM.proper</span></span>
	 * 	
	 * </div>
	 * </pre>
	 * 
	 * @param rlsDiv
	 * @return
	 */
	private static MediaRelease parseRelease(URL url, Element rlsDiv)
	{
		Element timestampSpan = rlsDiv.getElementsByClass("timestamp").first();
		Element sectionSpan = rlsDiv.getElementsByClass("section").first();
		Element releaseSpan = rlsDiv.getElementsByClass("release").first();
		Element infoSpan = rlsDiv.getElementsByClass("info").first();
		Element nukeSpan = rlsDiv.getElementsByClass("nuke").first();

		if (releaseSpan == null)
		{
			return null;
		}

		MediaRelease rls = new MediaRelease();
		rls.setName(releaseSpan.text());
		if (sectionSpan != null)
		{
			rls.setSection(sectionSpan.text());
		}
		if (timestampSpan != null)
		{
			try
			{
				rls.setDate(ZonedDateTime.of(LocalDateTime.parse(timestampSpan.text(), DATE_TIME_FORMATTER), TIME_ZONE));
			}
			catch (DateTimeParseException e)
			{
				e.printStackTrace();
			}
		}

		if (infoSpan != null)
		{
			rls.setInfo(infoSpan.text());
		}
		if (nukeSpan != null)
		{
			rls.setNukeReason(nukeSpan.text());
		}
		rls.setInfoUrl(url.toExternalForm());
		return rls;
	}
}
