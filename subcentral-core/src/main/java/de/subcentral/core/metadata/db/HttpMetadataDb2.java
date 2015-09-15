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

public abstract class HttpMetadataDb2 extends MetadataDb2Base
{
	public static final int DEFAULT_TIMEOUT = 10000;

	protected final URL	host;
	protected int		timeout	= DEFAULT_TIMEOUT;

	public HttpMetadataDb2()
	{
		this.host = initHost();
	}

	// Metadata
	/**
	 * 
	 * @return the URL of the host of this lookup, not null
	 * @throws MalformedURLException
	 */
	protected abstract URL initHost();

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

	// Config
	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	// Status
	@Override
	public boolean isAvailable()
	{
		return IOUtil.pingHttp(host, timeout);
	}

	// Search
	@Override
	public <T> List<T> search(String query, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return parseSearchResults(buildSearchUrl(query, recordType), recordType);
	}

	protected abstract URL buildSearchUrl(String query, Class<?> recordType) throws IllegalArgumentException, IOException;

	@Override
	public <T> List<T> searchWithObject(Object queryObj, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return parseSearchResults(buildSearchUrl(queryObj, recordType), recordType);
	}

	protected abstract URL buildSearchUrl(Object queryObj, Class<?> recordType) throws IllegalArgumentException, IOException;

	public abstract <T> List<T> parseSearchResults(URL query, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Get
	public <T> T get(String id, Class<T> recordType) throws IllegalArgumentException, IOException
	{
		return parseRecord(buildGetUrl(id, recordType), recordType);
	}

	protected abstract <T> URL buildGetUrl(String id, Class<T> recordType) throws IllegalArgumentException, IOException;

	protected abstract <T> T parseRecord(URL url, Class<T> recordType) throws IllegalArgumentException, IOException;

	// Utility methods for child classes
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
	protected URL buildUrl(String path, String query) throws URISyntaxException, MalformedURLException
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
