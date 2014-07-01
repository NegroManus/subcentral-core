package de.subcentral.core.lookup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class AbstractHttpLookup<R, P> extends AbstractLookup<R, P>
{
	public static final int	DEFAULT_TIMEOUT	= 5000;
	private int				timeout			= DEFAULT_TIMEOUT;

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
		return getHost().getHost();
	}

	@Override
	public boolean isLookupAvailable()
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) getHost().openConnection();
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
			return createQuery(buildQueryUrl(query));
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
	 * 
	 * @return The URL of the host of this lookup. Not null.
	 */
	protected abstract URL getHost();

	protected abstract URL buildQueryUrl(String query) throws Exception;

	protected abstract URL buildQueryUrlFromParameters(P parameterBean) throws Exception;
}
