package de.subcentral.core.lookup;

import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class AbstractHttpHtmlLookup<R, Q> extends AbstractHttpLookup<R, Q>
{
	@Override
	public LookupResult<R> lookupByUrl(URL url) throws LookupException
	{
		try
		{
			Connection con = setupConnection(url);
			Document doc = con.get();
			return parseDocument(url, doc);
		}
		catch (Exception e)
		{
			throw new LookupException(url, e);
		}
	}

	protected Connection setupConnection(URL url) throws Exception
	{
		// Sets up a connection without cookies or user-agent info
		return Jsoup.connect(url.toExternalForm());
	}

	protected abstract LookupResult<R> parseDocument(URL url, Document doc) throws Exception;
}
