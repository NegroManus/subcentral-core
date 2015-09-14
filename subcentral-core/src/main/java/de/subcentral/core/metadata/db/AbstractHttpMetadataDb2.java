package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import de.subcentral.core.util.IOUtil;

public abstract class AbstractHttpMetadataDb2 extends AbstractMetadataDb2
{
	public static final int DEFAULT_TIMEOUT = 10000;

	protected final URL	host;
	protected int		timeout	= DEFAULT_TIMEOUT;

	public AbstractHttpMetadataDb2()
	{
		try
		{
			this.host = initHost();
		}
		catch (MalformedURLException e)
		{
			throw new AssertionError("Host URL malformed", e);
		}
	}

	/**
	 * 
	 * @return the URL of the host of this lookup, not null
	 * @throws MalformedURLException
	 */
	protected abstract URL initHost() throws MalformedURLException;

	public URL getHost()
	{
		return host;
	}

	@Override
	public String getDomain()
	{
		try
		{
			return IOUtil.getDomainName(host);
		}
		catch (URISyntaxException e)
		{
			throw new IllegalStateException("Invalid host", e);
		}
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public boolean isAvailable()
	{
		return IOUtil.pingHttp(host, timeout);
	}

	@Override
	public <T> List<T> search(String query, Class<T> resultType) throws IllegalArgumentException, IOException
	{
		return searchWithUrl(buildSearchUrl(query, resultType), resultType);
	}

	public abstract <T> List<T> searchWithUrl(URL query, Class<T> resultType) throws IllegalArgumentException, IOException;

	protected abstract URL buildSearchUrl(String query, Class<?> resultType) throws IllegalArgumentException;

	/**
	 * 
	 * @param path
	 *            the path for the URL. Not null. Must start with "/"
	 * @param query
	 *            the query for the URL. Not null
	 * @return An URL build of the host of this lookup and the given path and query
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	protected URL buildUrl(String path, String query) throws IllegalArgumentException
	{
		try
		{
			URI uri = new URI(host.getProtocol(), host.getUserInfo(), host.getHost(), host.getPort(), path, query, null);
			return uri.toURL();
		}
		catch (URISyntaxException | MalformedURLException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	protected String formatQuery(String queryPrefix, String queryStr) throws UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(queryPrefix);
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(queryStr == null ? "" : URLEncoder.encode(queryStr, "UTF-8"));
		return sb.toString();
	}
}
