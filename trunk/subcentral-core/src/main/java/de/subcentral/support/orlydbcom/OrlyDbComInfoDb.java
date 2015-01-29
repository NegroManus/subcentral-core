package de.subcentral.support.orlydbcom;

import java.net.MalformedURLException;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.infodb.AbstractHtmlHttpInfoDb;
import de.subcentral.core.metadata.infodb.InfoDbQueryException;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.util.ByteUtil;

/**
 * 
 * @implSpec #immutable #thread-safe
 *
 */
public class OrlyDbComInfoDb extends AbstractHtmlHttpInfoDb<Release>
{
	public static final String				DOMAIN				= "orlydb.com";

	private static final Logger				log					= LogManager.getLogger(OrlyDbComInfoDb.class);

	/**
	 * The release dates are ISO-formatted (without the 'T').
	 */
	private static final DateTimeFormatter	DATE_TIME_FORMATTER	= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId				TIME_ZONE			= ZoneId.of("UTC");

	@Override
	public String getName()
	{
		return "ORLYDB.com";
	}

	@Override
	public String getDomain()
	{
		return DOMAIN;
	}

	@Override
	protected URL initHost() throws MalformedURLException
	{
		return new URL("http://www.orlydb.com/");
	}

	@Override
	public Class<Release> getResultType()
	{
		return Release.class;
	}

	@Override
	protected String getDefaultQueryPath()
	{
		return "/";
	}

	@Override
	protected String getDefaultQueryPrefix()
	{
		return "q=";
	}

	// Querying
	@Override
	public List<Release> queryWithHtmlDoc(Document doc) throws InfoDbQueryException
	{
		try
		{
			return parseReleases(doc);
		}
		catch (Exception e)
		{
			throw new InfoDbQueryException(this, doc, e);
		}
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
	private static List<Release> parseReleases(Document doc)
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
	private static Release parseRelease(Document doc, Element rlsDiv)
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
				ZonedDateTime date = ZonedDateTime.of(LocalDateTime.parse(timestampSpan.text(), DATE_TIME_FORMATTER), TIME_ZONE);
				rls.setDate(date);
			}
			catch (DateTimeParseException e)
			{
				log.warn("Could not parse release date string '" + timestampSpan.text() + "'", e);
			}
		}

		if (infoSpan != null)
		{
			String info = infoSpan.text();
			rls.setNfo(info);
			// e.g. "349.3MB | 25F"
			Pattern sizeAndFilesPattern = Pattern.compile("(\\d+\\.\\d+MB)\\s*\\|\\s*(\\d+)F");
			Matcher mSizeAndFiles = sizeAndFilesPattern.matcher(info);
			if (mSizeAndFiles.matches())
			{
				// no NumberFormatExceptions can occur because then the pattern would not match in the first place
				long size = ByteUtil.parseBytes(mSizeAndFiles.group(1));
				rls.setSize(size);
				int fileCount = Integer.parseInt(mSizeAndFiles.group(2));
				rls.setFileCount(fileCount);
			}
		}

		if (nukeSpan != null)
		{
			rls.nuke(nukeSpan.text());
		}

		rls.getFurtherInfoLinks().add(doc.baseUri());

		return rls;
	}
}
