package de.subcentral.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
				builder.append(URLEncoder.encode(value, "UTF-8"));
			}
			return builder;
		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException(e);
		}
	}
}
