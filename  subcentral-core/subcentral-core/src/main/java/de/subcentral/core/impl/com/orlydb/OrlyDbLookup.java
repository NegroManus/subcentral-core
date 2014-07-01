package de.subcentral.core.impl.com.orlydb;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

import de.subcentral.core.lookup.AbstractHttpLookup;
import de.subcentral.core.lookup.LookupQuery;
import de.subcentral.core.release.MediaRelease;

public class OrlyDbLookup extends AbstractHttpLookup<MediaRelease, OrlyDbLookupParameters>
{
	private static URL	host;
	static
	{
		try
		{
			host = new URL("http://www.orlydb.com/");
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
		return new OrlyDbQuery(query);
	}

	@Override
	protected URL buildQueryUrl(String query) throws Exception
	{
		return new URI("http", null, getHost().getHost(), -1, "/", encodeQuery(query), null).toURL();
	}

	@Override
	protected URL buildQueryUrlFromParameters(OrlyDbLookupParameters parameterBean) throws Exception
	{
		if (parameterBean == null)
		{
			return null;
		}
		StringBuilder path = new StringBuilder("/");
		if (!StringUtils.isBlank(parameterBean.getSection()))
		{
			path.append("s/");
			path.append(parameterBean.getSection());
		}
		String queryStr = encodeQuery(parameterBean.getQuery());
		URI uri = new URI("http", null, getHost().getHost(), -1, path.toString(), queryStr, null);
		return uri.toURL();
	}

	@Override
	public Class<OrlyDbLookupParameters> getParameterBeanClass()
	{
		return OrlyDbLookupParameters.class;
	}

	private static String encodeQuery(String queryStr) throws UnsupportedEncodingException
	{
		if (StringUtils.isBlank(queryStr))
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("q=");
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(URLEncoder.encode(queryStr, "UTF-8"));
		return sb.toString();
	}
}
