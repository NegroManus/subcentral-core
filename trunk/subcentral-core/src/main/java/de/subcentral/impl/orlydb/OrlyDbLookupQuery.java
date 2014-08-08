package de.subcentral.impl.orlydb;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
import de.subcentral.core.model.release.Release;
import de.subcentral.core.util.ByteUtil;

public class OrlyDbLookupQuery extends AbstractHttpHtmlLookupQuery<Release>
{
	/**
	 * The release dates are ISO-formatted (without the 'T').
	 */
	private static final DateTimeFormatter	DATE_TIME_FORMATTER	= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId				TIME_ZONE			= ZoneId.of("UTC");

	OrlyDbLookupQuery(URL url)
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
	 * <div id="releases">
	 * 		<div>
	 * 		...
	 * 		</div>
	 * ...
	 * </div>
	 * </pre>
	 * 
	 * @param doc
	 * @return
	 */
	private List<Release> parseReleases(Document doc)
	{
		Element rlssDiv = doc.getElementById("releases");
		if (rlssDiv == null)
		{
			return ImmutableList.of();
		}
		// Search for elements with tag "div" on the children list
		// If searched in rlssDiv, the rlssDiv itself will be returned too.
		Elements rlsDivs = rlssDiv.children().tagName("div");
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
	private Release parseRelease(Document doc, Element rlsDiv)
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

		Release rls = new Release();
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
			String info = infoSpan.text();
			rls.setInfo(info);
			// e.g. "349.3MB | 25F"
			Pattern sizeAndFilesPattern = Pattern.compile("(\\d+\\.\\d+MB)\\s*\\|\\s*(\\d+)F");
			Matcher mSizeAndFiles = sizeAndFilesPattern.matcher(info);
			if (mSizeAndFiles.matches())
			{
				long size = ByteUtil.parseBytes(mSizeAndFiles.group(1));
				rls.setSize(size);
				int files = Integer.parseInt(mSizeAndFiles.group(2));
				rls.setFileCount(files);
			}
		}

		if (nukeSpan != null)
		{
			rls.nuke(nukeSpan.text());
		}

		rls.setInfoUrl(doc.baseUri());

		return rls;
	}
}
