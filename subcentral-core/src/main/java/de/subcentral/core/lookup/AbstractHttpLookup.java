package de.subcentral.core.lookup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

public abstract class AbstractHttpLookup<R, P> extends AbstractLookup<R, P>
{
	public static final int	DEFAULT_TIMEOUT	= 5000;

	protected final URL		host;
	private int				timeout			= DEFAULT_TIMEOUT;

	public AbstractHttpLookup()
	{
		this.host = initHost();
	}

	/**
	 * 
	 * @return the URL of the host of this lookup. Not null
	 */
	protected abstract URL initHost();

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
	public String getDomain()
	{
		return host.getHost();
	}

	@Override
	public boolean isLookupAvailable()
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
	public LookupQuery<R> createQuery(String query)
	{
		try
		{
			return createQuery(buildDefaultQueryUrl(query));
		}
		catch (Exception e)
		{
			throw new LookupException(query, e);
		}
	}

	@Override
	public LookupQuery<R> createQueryFromParameters(P parameterBean)
	{
		try
		{
			return createQuery(buildQueryUrlFromParameters(parameterBean));
		}
		catch (Exception e)
		{
			throw new LookupException(parameterBean, e);
		}
	}

	public abstract LookupQuery<R> createQuery(URL query);

	/**
	 * Calls {@link #buildQueryUrl(String, String, String) buildQueryUrl(getDefaultQueryPath(), getDefaultQueryPrefix(), query)}.
	 * 
	 * @param queryString
	 *            The query string. For example "Psych S08E01".
	 * @return The generated URL for this query string.
	 * @throws Exception
	 */
	protected URL buildDefaultQueryUrl(String queryString) throws Exception
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
	 * @throws Exception
	 */
	protected URL buildQueryUrl(String path, String queryPrefix, String queryString) throws Exception
	{
		if (queryString == null)
		{
			return null;
		}
		URI uri = new URI("http", null, initHost().getHost(), -1, path, buildQuery(queryPrefix, queryString), null);
		return uri.toURL();
	}

	protected abstract URL buildQueryUrlFromParameters(P parameterBean) throws Exception;

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
