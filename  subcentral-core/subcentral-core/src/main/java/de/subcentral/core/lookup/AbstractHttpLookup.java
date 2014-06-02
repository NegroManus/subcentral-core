package de.subcentral.core.lookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
	public LookupResult<R> lookup(String query) throws IOException
	{
		return lookupByUrl(buildQueryUrl(query));
	}

	@Override
	public LookupResult<R> lookup(Q query) throws IOException
	{
		return lookupByUrl(buildQueryUrl(query));
	}

	public LookupResult<R> lookupByUrl(String subPath) throws IOException
	{
		URL url = new URL(host, subPath);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream())))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}
		}
		return parseResponse(sb.toString());
	}

	protected abstract String buildQueryUrl(Q query);

	protected abstract String buildQueryUrl(String query);

	protected abstract LookupResult<R> parseResponse(String response);
}
