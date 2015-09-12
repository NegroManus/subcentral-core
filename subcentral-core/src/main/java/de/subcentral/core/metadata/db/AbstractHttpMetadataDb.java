package de.subcentral.core.metadata.db;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import de.subcentral.core.util.IOUtil;

public abstract class AbstractHttpMetadataDb<T> extends AbstractMetadataDb<T>
{
	public static final int DEFAULT_TIMEOUT = 10000;

	protected final URL	host;
	protected int		timeout	= DEFAULT_TIMEOUT;

	public AbstractHttpMetadataDb()
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
	public List<T> query(String query) throws MetadataDbUnavailableException, MetadataDbQueryException
	{
		try
		{
			return queryUrl(buildQueryUrl(query));
		}
		catch (MetadataDbUnavailableException ue)
		{
			throw ue;
		}
		catch (Exception e)
		{
			throw new MetadataDbQueryException(this, query, e);
		}
	}

	public abstract List<T> queryUrl(URL query) throws MetadataDbUnavailableException, MetadataDbQueryException;

	protected abstract URL buildQueryUrl(String query) throws Exception;

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
	protected URL buildUrl(String path, String query) throws UnsupportedEncodingException, URISyntaxException, MalformedURLException
	{
		URI uri = new URI(host.getProtocol(), host.getUserInfo(), host.getHost(), host.getPort(), path, query, null);
		return uri.toURL();
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
