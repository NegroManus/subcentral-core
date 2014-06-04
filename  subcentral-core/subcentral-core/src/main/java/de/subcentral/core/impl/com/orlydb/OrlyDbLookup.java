package de.subcentral.core.impl.com.orlydb;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.AbstractHttpHtmlLookup;
import de.subcentral.core.lookup.LookupException;
import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.media.Media;
import de.subcentral.core.naming.NamingService;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbLookup extends AbstractHttpHtmlLookup<MediaRelease, OrlyDbQuery>
{
	private NamingService	namingService	= null;

	public NamingService getNamingService()
	{
		return namingService;
	}

	public void setNamingService(NamingService namingService)
	{
		this.namingService = namingService;
	}

	public OrlyDbLookup()
	{
		try
		{
			setHost("http://orlydb.com/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public OrlyDbQuery createQuery(String queryString)
	{
		return createQuery(queryString, null);
	}

	public OrlyDbQuery createQuery(String query, String section)
	{
		return new OrlyDbQuery(query, section);
	}

	public OrlyDbQuery createQuery(Media media)
	{
		return createQuery(media, null);
	}

	public OrlyDbQuery createQuery(Media media, String section)
	{
		return new OrlyDbQuery(namingService.name(media), section);
	}

	public OrlyDbQuery createQuery(MediaRelease mediaRelease)
	{
		return createQuery(mediaRelease, null);
	}

	public OrlyDbQuery createQuery(MediaRelease mediaRelease, String section)
	{
		return new OrlyDbQuery(namingService.name(mediaRelease), section);
	}

	@Override
	public Class<MediaRelease> getResultClass()
	{
		return MediaRelease.class;
	}

	@Override
	public OrlyDbLookupResult lookup(OrlyDbQuery query) throws LookupException
	{
		return (OrlyDbLookupResult) super.lookup(query);
	}

	@Override
	public OrlyDbLookupResult lookup(String query) throws LookupException
	{
		return (OrlyDbLookupResult) super.lookup(query);
	}

	@Override
	public OrlyDbLookupResult lookupByUrl(URL url) throws LookupException
	{
		return (OrlyDbLookupResult) super.lookupByUrl(url);
	}

	@Override
	public OrlyDbLookupResult lookupByUrl(String url) throws LookupException
	{
		return (OrlyDbLookupResult) super.lookupByUrl(url);
	}

	@Override
	protected URL buildQueryUrl(OrlyDbQuery query) throws URISyntaxException, UnsupportedEncodingException, MalformedURLException
	{
		if (query == null)
		{
			return null;
		}
		StringBuilder path = new StringBuilder("/");
		if (query.getSection() != null)
		{
			path.append("s/");
			path.append(query.getSection());
		}
		String queryStr = encodeQuery(query.getQuery());
		URI uri = new URI("http", null, getHost().getHost(), -1, path.toString(), queryStr, null);
		return uri.toURL();
	}

	private static String encodeQuery(String queryStr) throws UnsupportedEncodingException
	{
		if (queryStr == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("q=");
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(URLEncoder.encode(queryStr, "UTF-8"));
		return sb.toString();
	}

	@Override
	protected LookupResult<MediaRelease> parseDocument(URL url, Document doc) throws Exception
	{
		return new OrlyDbLookupResult(url, parseReleases(doc));
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
	private static List<MediaRelease> parseReleases(Document doc)
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
			MediaRelease rls = parseRelease(rlsDiv);
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
	private static MediaRelease parseRelease(Element rlsDiv)
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
		rls.setExplicitName(releaseSpan.text());
		rls.setSection(sectionSpan.text());
		rls.setDate(ZonedDateTime.of(LocalDateTime.parse(timestampSpan.text().replace(' ', 'T')), ZoneId.of("UTC")));
		if (infoSpan != null)
		{
			rls.setInfo(infoSpan.text());
		}
		if (nukeSpan != null)
		{
			rls.setNukeReason(nukeSpan.text());
		}
		return rls;
	}

}
