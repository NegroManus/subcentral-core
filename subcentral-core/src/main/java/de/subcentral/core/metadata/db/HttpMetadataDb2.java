package de.subcentral.core.metadata.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.subcentral.core.util.NetUtil;
import de.subcentral.core.util.TimeUtil;

public abstract class HttpMetadataDb2 extends MetadataDb2Base
{
	public static final int DEFAULT_TIMEOUT = 10000;

	private static final Logger log = LogManager.getLogger(HttpMetadataDb2.class);

	protected int timeout = DEFAULT_TIMEOUT;

	// Metadata
	public abstract String getHost();

	@Override
	public String getName()
	{
		try
		{
			return NetUtil.getDomainName(getHost());
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
		return NetUtil.pingHttp(getHost(), timeout);
	}

	// Utility methods for child classes
	protected URL buildRelativeUrl(String path) throws IllegalArgumentException
	{
		return buildRelativeUrlImpl(path, null);
	}

	protected URL buildRelativeUrl(String queryKey, String queryValue) throws IllegalArgumentException
	{
		return buildRelativeUrlImpl(null, NetUtil.formatQueryString(queryKey, queryValue));
	}

	protected URL buildRelativeUrl(Map<String, String> queryKeyValuePairs) throws IllegalArgumentException
	{
		return buildRelativeUrlImpl(null, NetUtil.formatQueryString(queryKeyValuePairs));
	}

	protected URL buildRelativeUrl(String path, String queryKey, String queryValue) throws IllegalArgumentException
	{
		return buildRelativeUrlImpl(path, NetUtil.formatQueryString(queryKey, queryValue));
	}

	protected URL buildRelativeUrl(String path, Map<String, String> queryKeyValuePairs) throws IllegalArgumentException
	{
		return buildRelativeUrlImpl(path, NetUtil.formatQueryString(queryKeyValuePairs));
	}

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
	private URL buildRelativeUrlImpl(String path, String query) throws IllegalArgumentException
	{
		try
		{
			URI uri = new URI(getHost());
			uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, query, uri.getFragment());
			return uri.toURL();
		}
		catch (URISyntaxException | MalformedURLException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	protected Document getDocument(URL url) throws IOException
	{
		log.trace("Connecting to {}", url);
		long start = System.nanoTime();
		Connection con = setupConnection(url);
		Document doc = con.get();
		double duration = TimeUtil.durationMillis(start);
		log.printf(Level.DEBUG, "Retrieved contents of %s in %.0f ms", url, duration);
		log.printf(Level.TRACE, "Contents of %s were:%n%s%n", url, doc);
		return doc;
	}

	/**
	 * The default implementation sets up a connection with the timeout.
	 * <p>
	 * Subclasses may override this method, call super.setupConnection() and then set other configurations like cookies or user-agent info.
	 * </p>
	 * 
	 * @param url
	 *            thr url to connect to
	 * @return a connection
	 */
	protected Connection setupConnection(URL url)
	{
		return Jsoup.connect(url.toExternalForm()).timeout(timeout);
	}
}
