package de.subcentral.support.orlydbcom;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.db.HttpMetadataDb;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.util.ByteUtil;

/**
 * 
 * @implSpec #immutable #thread-safe
 *
 */
public class OrlyDbComMetadataDb extends HttpMetadataDb
{
	private static final Logger log = LogManager.getLogger(OrlyDbComMetadataDb.class);

	/**
	 * The release dates are ISO-formatted (without the 'T').
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US);

	/**
	 * The release dates are in UTC.
	 */
	private static final ZoneId TIME_ZONE = ZoneId.of("UTC");

	@Override
	public String getSiteId()
	{
		return OrlyDbCom.SITE_ID;
	}

	@Override
	public String getDisplayName()
	{
		return "ORLYDB.com";
	}

	@Override
	public String getHost()
	{
		return "http://www.orlydb.com/";
	}

	@Override
	public Set<Class<?>> getRecordTypes()
	{
		return ImmutableSet.of(Release.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String query, Class<T> recordType) throws UnsupportedOperationException, IOException
	{
		if (Release.class.equals(recordType))
		{
			URL url = buildRelativeUrl("q", query);
			log.debug("Searching for releases with text query \"{}\" using url {}", query, url);
			return (List<T>) parseReleaseSearchResults(getDocument(url));
		}
		throw createRecordTypeNotSearchableException(recordType);
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
	protected List<Release> parseReleaseSearchResults(Document doc)
	{
		Elements rlsDivs = doc.select("div#releases > div");
		ImmutableList.Builder<Release> results = ImmutableList.builder();
		for (Element rlsDiv : rlsDivs)
		{
			Release rls = parseReleaseSearchResult(doc, rlsDiv);
			if (rls != null)
			{
				results.add(rls);
			}
		}
		return results.build();
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
	private static Release parseReleaseSearchResult(Document doc, Element rlsDiv)
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
			rls.setCategory(sectionSpan.text());
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
