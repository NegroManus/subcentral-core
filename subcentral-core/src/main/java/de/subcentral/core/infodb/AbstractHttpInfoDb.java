package de.subcentral.core.infodb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public abstract class AbstractHttpInfoDb<R> extends AbstractInfoDb<R>
{
	public static final int	DEFAULT_TIMEOUT	= 10000;

	protected final URL		host;
	protected int			timeout			= DEFAULT_TIMEOUT;

	public AbstractHttpInfoDb()
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

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	@Override
	public boolean isInfoDbAvailable()
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) initHost().openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		}
		catch (IOException exception)
		{
			return false;
		}
	}

	@Override
	public List<R> query(String query) throws InfoDbUnavailableException, InfoDbQueryException
	{
		try
		{
			return queryWithUrl(buildDefaultQueryUrl(query));
		}
		catch (InfoDbUnavailableException ue)
		{
			throw ue;
		}
		catch (Exception e)
		{
			throw new InfoDbQueryException(this, query, e);
		}
	}

	public abstract List<R> queryWithUrl(URL query) throws InfoDbUnavailableException, InfoDbQueryException;

	/**
	 * Calls {@link #buildQueryUrl(String, String, String) buildQueryUrl(getDefaultQueryPath(), getDefaultQueryPrefix(), query)}.
	 * 
	 * @param queryString
	 *            The query string. For example "Psych S08E01".
	 * @return The generated URL for this query string.
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	protected URL buildDefaultQueryUrl(String queryString) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException
	{
		return buildQueryUrl(getDefaultQueryPath(), getDefaultQueryPrefix(), queryString);
	}

	/**
	 * 
	 * @param path
	 *            The path for the URL. Not null.
	 * @param queryPrefix
	 *            The prefix for the query string.
	 * @param queryString
	 *            The actual query string.
	 * @return An URL build of the host of this lookup and the given path, queryPrefix and queryString.
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws Exception
	 */
	protected URL buildQueryUrl(String path, String queryPrefix, String queryString) throws UnsupportedEncodingException, URISyntaxException,
			MalformedURLException
	{
		if (queryString == null)
		{
			return null;
		}
		URI uri = new URI(host.getProtocol(), host.getUserInfo(), host.getHost(), host.getPort(), path, buildQuery(queryPrefix, queryString), null);
		return uri.toURL();
	}

	/**
	 * 
	 * @return The default path for the query URL. Has to start with "/".
	 */
	protected abstract String getDefaultQueryPath();

	/**
	 * 
	 * @return The default prefix for the query string. Used to build the query for the query URL. For example "s=".
	 */
	protected abstract String getDefaultQueryPrefix();

	private String buildQuery(String queryPrefix, String queryStr) throws UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(queryPrefix);
		// URLEncoder is just for encoding queries, not for the whole URL
		sb.append(URLEncoder.encode(queryStr == null ? "" : queryStr, "UTF-8"));
		return sb.toString();
	}

}
