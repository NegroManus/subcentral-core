package de.subcentral.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NetUtil
{
	public static String formatQueryString(Map<String, String> keyValuePairs)
	{
		StringBuilder query = new StringBuilder();
		for (Map.Entry<String, String> pair : keyValuePairs.entrySet())
		{
			if (query.length() > 0)
			{
				query.append("&");
			}
			appendToQueryBuilder(query, pair.getKey(), pair.getValue());
		}
		return query.toString();
	}

	public static String formatQueryString(String key, String value)
	{
		return appendToQueryBuilder(new StringBuilder(), key, value).toString();
	}

	private static StringBuilder appendToQueryBuilder(StringBuilder builder, String key, String value)
	{
		try
		{
			builder.append(key);
			builder.append('=');
			// URLEncoder is just for encoding queries, not for the whole URL
			if (value != null)
			{
				builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
			}
			return builder;
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	public static String getDomainName(String url) throws URISyntaxException
	{
		return getDomainName(new URI(url));
	}

	public static String getDomainName(URL url) throws URISyntaxException
	{
		return getDomainName(url.toURI());
	}

	public static String getDomainName(URI uri)
	{
		String domain = uri.getHost();
		return domain.startsWith("www.") ? domain.substring(4) : domain;
	}

	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in the 200-399 range.
	 * 
	 * @param url
	 *            The HTTP URL to be pinged.
	 * @param timeout
	 *            The timeout in millis for both the connection timeout and the response read timeout. Note that the total timeout is effectively two times the given timeout.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the given timeout, otherwise <code>false</code>.
	 */
	public static boolean pingHttp(String url, int timeout)
	{
		// Otherwise an exception may be thrown on invalid SSL certificates:
		url = url.replaceFirst("^https", "http");

		try
		{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		}
		catch (IOException exception)
		{
			return false;
		}
	}

	public static boolean pingHttp(URL url, int timeout)
	{
		return pingHttp(url.toExternalForm(), timeout);
	}
}
