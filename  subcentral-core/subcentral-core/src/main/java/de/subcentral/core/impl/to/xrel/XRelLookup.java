package de.subcentral.core.impl.to.xrel;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.lookup.AbstractHttpHtmlLookup;
import de.subcentral.core.lookup.LookupResult;
import de.subcentral.core.release.MediaRelease;

public class XRelLookup extends AbstractHttpHtmlLookup<XRelLookupResult, String>
{
	public XRelLookup()
	{
		try
		{
			setHost("http://www.xrel.to/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Class<XRelLookupResult> getResultClass()
	{
		return XRelLookupResult.class;
	}

	@Override
	public String createQuery(String queryString)
	{
		return queryString;
	}

	@Override
	protected LookupResult<XRelLookupResult> parseDocument(URL url, Document doc) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected URL buildQueryUrl(String query) throws Exception
	{
		if (query == null)
		{
			return null;
		}
		String path = "search.html";
		String queryStr = encodeQuery(query);
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
		sb.append("xrel_search_query=");
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(URLEncoder.encode(queryStr, "UTF-8"));
		return sb.toString();
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
	private static List<MediaRelease> parseReleases(URL url, Document doc)
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
	 * @param resultFrameDiv
	 * @return
	 */
	private static MediaRelease parseRelease(URL url, Element resultFrameDiv)
	{
		Element dateDiv = resultFrameDiv.getElementsByClass("release_date").first();
		Element timeSpan = dateDiv == null ? null : dateDiv.getElementsByTag("span").first();
		Element titleDiv = resultFrameDiv.getElementsByClass("release_title").first();
		Element nukeImg = titleDiv.select("img._nuke_icon").first();

		// TODO

		return null;
	}
}
