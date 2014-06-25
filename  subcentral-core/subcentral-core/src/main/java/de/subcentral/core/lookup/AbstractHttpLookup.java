package de.subcentral.core.lookup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractHttpLookup<R, Q> implements Lookup<R, Q>
{
	public static final int	DEFAULT_TIMEOUT	= 5000;
	private URL				host;
	private int				timeout			= DEFAULT_TIMEOUT;

	public URL getHost()
	{
		return host;
	}

	public void setHost(URL host)
	{
		this.host = host;
	}

	public int getTimeout()
	{
		return timeout;
	}

	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public void setHost(String hostname) throws MalformedURLException
	{
		setHost(new URL(hostname));
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
			HttpURLConnection connection = (HttpURLConnection) host.openConnection();
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
	public LookupResult<R> lookup(Q query) throws LookupException
	{
		try
		{
			return lookupByUrl(buildQueryUrl(query));
		}
		catch (Exception e)
		{
			throw new LookupException(query, e);
		}
	}

	public abstract LookupResult<R> lookupByUrl(URL url) throws LookupException;

	public LookupResult<R> lookupByUrl(String url) throws LookupException
	{
		try
		{
			return lookupByUrl(new URL(getHost(), url));
		}
		catch (Exception e)
		{
			throw new LookupException(this, url, e);
		}
	}

	protected abstract URL buildQueryUrl(Q query) throws Exception;
}
