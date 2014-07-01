package de.subcentral.core.impl.to.xrel;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.release.MediaRelease;

public class XRelLookup extends AbstractHttpLookup<MediaRelease, String>
{
	private static URL	host;
	static
	{
		try
		{
			host = new URL("http://www.xrel.to/");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected URL getHost()
	{
		return host;
	}

	@Override
	public Class<MediaRelease> getResultClass()
	{
		return MediaRelease.class;
	}

	@Override
	public LookupQuery<MediaRelease> createQuery(URL query)
	{
		return new XRelLookupQuery(query);
	}

	@Override
	protected URL buildQueryUrl(String query) throws Exception
	{
		if (query == null)
		{
			return null;
		}
		String path = "/search.html";
		String queryStr = encodeQuery(query);
		URI uri = new URI("http", null, getHost().getHost(), -1, path.toString(), queryStr, null);
		return uri.toURL();
	}

	@Override
	protected URL buildQueryUrlFromParameters(String parameterBean) throws Exception
	{
		return buildQueryUrl(parameterBean);
	}

	@Override
	public Class<String> getParameterBeanClass()
	{
		return String.class;
	}

	private static String encodeQuery(String queryStr) throws UnsupportedEncodingException
	{
		if (queryStr == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		// Do not use "mode=rls", because then results are displayed different
		// and the info url is only available via an ajax call.
		sb.append("xrel_search_query=");
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(URLEncoder.encode(queryStr, "UTF-8"));
		return sb.toString();
	}
}
